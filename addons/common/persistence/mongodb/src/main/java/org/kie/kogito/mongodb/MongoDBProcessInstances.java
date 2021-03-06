/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.kogito.mongodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.kie.kogito.Model;
import org.kie.kogito.mongodb.transaction.MongoDBTransactionManager;
import org.kie.kogito.process.MutableProcessInstances;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.ProcessInstanceDuplicatedException;
import org.kie.kogito.process.ProcessInstanceReadMode;
import org.kie.kogito.process.impl.AbstractProcessInstance;
import org.kie.kogito.serialization.process.MarshallerContextName;
import org.kie.kogito.serialization.process.ProcessInstanceMarshallerService;

import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

import static java.util.Collections.singletonMap;
import static org.kie.kogito.mongodb.utils.DocumentConstants.PROCESS_INSTANCE_ID;
import static org.kie.kogito.mongodb.utils.DocumentUtils.getCollection;
import static org.kie.kogito.process.ProcessInstanceReadMode.MUTABLE;

public class MongoDBProcessInstances<T extends Model> implements MutableProcessInstances<T> {

    private static final String VERSION = "version";
    private org.kie.kogito.process.Process<?> process;
    private ProcessInstanceMarshallerService marshaller;
    private final MongoCollection<Document> collection;
    private MongoDBTransactionManager transactionManager;
    private final boolean lock;

    public MongoDBProcessInstances(MongoClient mongoClient, org.kie.kogito.process.Process<?> process, String dbName, MongoDBTransactionManager transactionManager, boolean lock) {
        this.process = process;
        this.collection = getCollection(mongoClient, process.id(), dbName);
        this.marshaller = ProcessInstanceMarshallerService.newBuilder()
                .withDefaultObjectMarshallerStrategies()
                .withContextEntries(singletonMap(MarshallerContextName.MARSHALLER_FORMAT, "json"))
                .build();
        this.transactionManager = transactionManager;
        this.lock = lock;
    }

    @Override
    public Optional<ProcessInstance<T>> findById(String id, ProcessInstanceReadMode mode) {
        Document piDoc = find(id);
        if (piDoc != null) {
            ProcessInstance<T> instance = unmarshall(piDoc, mode);
            ((AbstractProcessInstance<?>) instance).setVersion(piDoc.getLong(VERSION));
            return Optional.of(instance);
        }
        return Optional.empty();
    }

    @Override
    public Collection<ProcessInstance<T>> values(ProcessInstanceReadMode mode) {
        FindIterable<Document> docs = Optional.ofNullable(transactionManager.getClientSession())
                .map(collection::find)
                .orElseGet(collection::find);
        List<ProcessInstance<T>> list = new ArrayList<>();
        try (MongoCursor<Document> cursor = docs.iterator()) {
            while (cursor.hasNext()) {
                list.add(unmarshall(cursor.next(), mode));
            }
        }
        return list;
    }

    private ProcessInstance<T> unmarshall(Document document, ProcessInstanceReadMode mode) {
        byte[] content = document.toJson().getBytes();
        return mode == MUTABLE ? (ProcessInstance<T>) marshaller.unmarshallProcessInstance(content, process) : (ProcessInstance<T>) marshaller.unmarshallReadOnlyProcessInstance(content, process);
    }

    @Override
    public void create(String id, ProcessInstance<T> instance) {
        updateStorage(id, instance, true);
    }

    @Override
    public void update(String id, ProcessInstance<T> instance) {
        updateStorage(id, instance, false);
    }

    private RuntimeException uncheckedException(Exception ex, String message, Object... param) {
        return new RuntimeException(String.format(message, param), ex);
    }

    protected void updateStorage(String id, ProcessInstance<T> instance, boolean checkDuplicates) {
        if (!isActive(instance)) {
            reloadProcessInstance(instance, id);
            return;
        }

        ClientSession clientSession = transactionManager.getClientSession();
        Document doc = Document.parse(new String(marshaller.marshallProcessInstance(instance)));
        if (checkDuplicates) {
            createInternal(id, clientSession, doc);
        } else {
            updateInternal(id, instance, clientSession, doc);
        }
        reloadProcessInstance(instance, id);
    }

    private void createInternal(String id, ClientSession clientSession, Document doc) {
        if (exists(id)) {
            throw new ProcessInstanceDuplicatedException(id);
        } else {
            doc.put(VERSION, 1L);
            if (clientSession != null) {
                collection.insertOne(clientSession, doc);
            } else {
                collection.insertOne(doc);
            }
        }
    }

    private void updateInternal(String id, ProcessInstance<T> instance, ClientSession clientSession, Document doc) {
        Bson filters = Filters.eq(PROCESS_INSTANCE_ID, id);
        UpdateResult result = null;
        if (lock) {
            doc.put(VERSION, instance.version() + 1);
            filters = Filters.and(Filters.eq(PROCESS_INSTANCE_ID, id), Filters.eq(VERSION, instance.version()));
        }
        if (clientSession != null) {
            result = collection.replaceOne(clientSession, filters, doc);
        } else {
            result = collection.replaceOne(filters, doc);
        }
        if (lock && result.getModifiedCount() != 1) {
            throw uncheckedException(null, "The document with ID: %s was updated or deleted by other request.", id);
        }
    }

    private Document find(String id) {
        return Optional.ofNullable(transactionManager.getClientSession())
                .map(r -> collection.find(r, Filters.eq(PROCESS_INSTANCE_ID, id)).first())
                .orElseGet(() -> collection.find(Filters.eq(PROCESS_INSTANCE_ID, id)).first());
    }

    @Override
    public boolean exists(String id) {
        return find(id) != null;
    }

    @Override
    public void remove(String id) {
        ClientSession clientSession = transactionManager.getClientSession();
        DeleteResult result = null;
        if (clientSession != null) {
            result = collection.deleteOne(clientSession, Filters.eq(PROCESS_INSTANCE_ID, id));
        } else {
            result = collection.deleteOne(Filters.eq(PROCESS_INSTANCE_ID, id));
        }
        if (lock && result.getDeletedCount() != 1) {
            throw uncheckedException(null, "The document with ID: %s was deleted by other request.", id);
        }
    }

    private void reloadProcessInstance(ProcessInstance<T> instance, String id) {
        Supplier<byte[]> supplier = () -> {
            Document reloaded = find(id);
            if (reloaded != null) {
                ((AbstractProcessInstance<?>) instance).setVersion(reloaded.getLong(VERSION));
                return reloaded.toJson().getBytes();
            } else {
                throw new IllegalArgumentException("process instance id " + id + " does not exists in mongodb");
            }
        };
        ((AbstractProcessInstance<?>) instance).internalRemoveProcessInstance(marshaller.createdReloadFunction(supplier));
    }

    @Override
    public Integer size() {
        return Optional.ofNullable(transactionManager.getClientSession())
                .map(r -> (int) collection.countDocuments(r))
                .orElseGet(() -> (int) collection.countDocuments());
    }

    @Override
    public boolean lock() {
        return this.lock;
    }
}
