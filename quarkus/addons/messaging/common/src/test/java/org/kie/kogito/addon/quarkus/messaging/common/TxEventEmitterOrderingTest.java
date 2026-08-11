/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kie.kogito.addon.quarkus.messaging.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.addon.quarkus.common.reactive.messaging.MessageDecoratorProvider;
import org.kie.kogito.event.CloudEventMarshaller;
import org.kie.kogito.event.DataEvent;
import org.kie.kogito.event.EventMarshaller;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.TransactionSynchronizationRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TxEventEmitterOrderingTest {

    private Emitter<String> emitter;
    private MessageDecoratorProvider messageDecorator;
    private TransactionSynchronizationRegistry txSyncRegistry;
    private Map<Object, Object> txResources;
    private List<Synchronization> registeredSyncs;
    private List<String> sentPayloads;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        emitter = mock(Emitter.class);
        messageDecorator = mock(MessageDecoratorProvider.class);
        txSyncRegistry = mock(TransactionSynchronizationRegistry.class);
        txResources = new HashMap<>();
        registeredSyncs = new ArrayList<>();
        sentPayloads = new ArrayList<>();

        when(txSyncRegistry.getResource(any())).thenAnswer(inv -> txResources.get(inv.getArgument(0)));
        doAnswer(inv -> {
            txResources.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(txSyncRegistry).putResource(any(), any());
        doAnswer(inv -> {
            registeredSyncs.add(inv.getArgument(0));
            return null;
        }).when(txSyncRegistry).registerInterposedSynchronization(any());

        doAnswer(inv -> {
            Message<String> msg = inv.getArgument(0);
            sentPayloads.add(msg.getPayload());
            return null;
        }).when(emitter).send(any(Message.class));

        when(messageDecorator.decorate(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void eventsArePublishedInEmitOrder() {
        TestTxEventEmitter txEmitter = new TestTxEventEmitter(emitter, messageDecorator, txSyncRegistry);

        txEmitter.emit(createMockEvent("processing-2"));
        txEmitter.emit(createMockEvent("processing-3"));
        txEmitter.emit(createMockEvent("output-1"));
        txEmitter.emit(createMockEvent("output"));

        assertThat(sentPayloads).as("no events should be published before transaction commit").isEmpty();
        assertThat(registeredSyncs).as("exactly one synchronization should be registered").hasSize(1);

        registeredSyncs.get(0).afterCompletion(Status.STATUS_COMMITTED);

        assertThat(sentPayloads).containsExactly("processing-2", "processing-3", "output-1", "output");
    }

    @Test
    void eventsAreNotPublishedOnRollback() {
        TestTxEventEmitter txEmitter = new TestTxEventEmitter(emitter, messageDecorator, txSyncRegistry);

        txEmitter.emit(createMockEvent("event-1"));
        txEmitter.emit(createMockEvent("event-2"));

        registeredSyncs.get(0).afterCompletion(Status.STATUS_ROLLEDBACK);

        assertThat(sentPayloads).as("no events should be published on rollback").isEmpty();
    }

    @Test
    void singleEventPreservesOrder() {
        TestTxEventEmitter txEmitter = new TestTxEventEmitter(emitter, messageDecorator, txSyncRegistry);

        txEmitter.emit(createMockEvent("only-event"));

        registeredSyncs.get(0).afterCompletion(Status.STATUS_COMMITTED);

        assertThat(sentPayloads).containsExactly("only-event");
    }

    private DataEvent<?> createMockEvent(String type) {
        DataEvent<?> event = mock(DataEvent.class);
        when(event.getType()).thenReturn(type);
        return event;
    }

    /**
     * Mirrors the generated TxEventEmitter logic from TxEventEmitterQuarkusTemplate
     */
    static class TestTxEventEmitter extends AbstractQuarkusCloudEventEmitter<String> {

        private final Emitter<String> emitter;
        private final MessageDecoratorProvider messageDecorator;
        private final TransactionSynchronizationRegistry txSyncRegistry;

        TestTxEventEmitter(Emitter<String> emitter, MessageDecoratorProvider messageDecorator,
                TransactionSynchronizationRegistry txSyncRegistry) {
            this.emitter = emitter;
            this.messageDecorator = messageDecorator;
            this.txSyncRegistry = txSyncRegistry;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void emit(DataEvent<?> dataEvent) {
            List<DataEvent<?>> bufferedEvents = (List<DataEvent<?>>) txSyncRegistry.getResource(this);
            if (bufferedEvents == null) {
                bufferedEvents = new ArrayList<>();
                txSyncRegistry.putResource(this, bufferedEvents);
                final List<DataEvent<?>> events = bufferedEvents;
                txSyncRegistry.registerInterposedSynchronization(new Synchronization() {
                    @Override
                    public void beforeCompletion() {
                    }

                    @Override
                    public void afterCompletion(int status) {
                        if (status == Status.STATUS_COMMITTED) {
                            for (DataEvent<?> event : events) {
                                try {
                                    Message<String> message = messageDecorator.decorate(getMessage(event));
                                    emitter.send(message);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                });
            }
            bufferedEvents.add(dataEvent);
        }

        @Override
        protected <T> Message<String> getMessage(DataEvent<T> event) {
            return Message.of(event.getType());
        }

        @Override
        protected EventMarshaller<String> getEventMarshaller() {
            return null;
        }

        @Override
        protected CloudEventMarshaller<String> getCloudEventMarshaller() {
            return null;
        }
    }
}