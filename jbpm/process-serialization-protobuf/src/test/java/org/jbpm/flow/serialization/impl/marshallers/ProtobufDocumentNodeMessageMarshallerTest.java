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
package org.jbpm.flow.serialization.impl.marshallers;

import org.jbpm.flow.serialization.ProcessInstanceMarshallerException;
import org.jbpm.flow.serialization.protobuf.KogitoTypesProtobuf;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import com.google.protobuf.Any;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProtobufDocumentNodeMessageMarshallerTest {

    private final ProtobufDocumentNodeMessageMarshaller marshaller = new ProtobufDocumentNodeMessageMarshaller();

    @Test
    void testUnmarshallsValidDocument() {
        Any data = Any.pack(KogitoTypesProtobuf.Document.newBuilder()
                .setContent("<root><child>text</child></root>")
                .build());

        Object result = marshaller.unmarshall(data);

        assertThat(result).isInstanceOf(Document.class);
        assertThat(((Document) result).getDocumentElement().getNodeName()).isEqualTo("root");
    }

    @Test
    void testRejectsDoctypeDeclarationOnUnmarshall() {
        String xmlWithDoctype = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE root [ <!ENTITY x \"expanded\"> ]>"
                + "<root>&x;</root>";
        Any data = Any.pack(KogitoTypesProtobuf.Document.newBuilder()
                .setContent(xmlWithDoctype)
                .build());

        assertThatThrownBy(() -> marshaller.unmarshall(data))
                .isInstanceOf(ProcessInstanceMarshallerException.class)
                .cause().isInstanceOf(org.xml.sax.SAXParseException.class);
    }
}
