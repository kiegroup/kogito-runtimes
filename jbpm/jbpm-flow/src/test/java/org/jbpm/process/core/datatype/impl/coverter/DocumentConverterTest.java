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
package org.jbpm.process.core.datatype.impl.coverter;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentConverterTest {

    private final DocumentConverter converter = new DocumentConverter();

    @Test
    void testParsesInlineXmlContent() {
        Object result = converter.apply("<root><child>text</child></root>");

        assertThat(result).isInstanceOf(Document.class);
        Document document = (Document) result;
        assertThat(document.getDocumentElement().getNodeName()).isEqualTo("root");
        assertThat(document.getDocumentElement().getTextContent()).isEqualTo("text");
    }

    @Test
    void testRejectsDoctypeDeclaration() {
        String xmlWithDoctype = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE root [ <!ENTITY x \"expanded\"> ]>"
                + "<root>&x;</root>";

        assertThatThrownBy(() -> converter.apply(xmlWithDoctype))
                .isInstanceOf(RuntimeException.class);
    }
}
