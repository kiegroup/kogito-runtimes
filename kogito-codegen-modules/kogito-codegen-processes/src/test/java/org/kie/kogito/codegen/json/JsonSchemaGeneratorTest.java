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
package org.kie.kogito.codegen.json;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.drools.codegen.common.GeneratedFile;
import org.jbpm.util.JsonSchemaUtil;
import org.junit.jupiter.api.Test;
import org.kie.kogito.ProcessInput;
import org.kie.kogito.codegen.VariableInfo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.victools.jsonschema.generator.SchemaVersion;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaGeneratorTest {

    private static final String ALL_OF = "allOf";
    private static final String REF = "$ref";
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";

    private enum Color {
        GREEN,
        WHITE
    }

    @ProcessInput(processName = "com.example.workflow")
    private static class ProcessWithDotsInName {

        @VariableInfo
        private String workflowId;

        @VariableInfo
        private int priority;
    }

    @ProcessInput(processName = "processName")
    private static class ProcessInputModel {

        @VariableInfo
        private Color color;

        @VariableInfo
        private Person person;
    }

    @ProcessInput(processName = "emptyProcessName")
    private static class EmptyProcessInputModel {

        private Color color;

        private Person person;
    }

    private static class Person {

        @SuppressWarnings("unused")
        private String name;
        @SuppressWarnings("unused")
        private int age;
        @SuppressWarnings("unused")
        private Address address;
        @SuppressWarnings("unused")
        private Person parent;
    }

    private static class Address {

        @SuppressWarnings("unused")
        private String street;
        @SuppressWarnings("unused")
        private Date date;
    }

    @Test
    public void testJsonSchemaGenerationForProcess() throws IOException {
        Collection<GeneratedFile> files = new JsonSchemaGenerator.ClassBuilder(Stream.of(ProcessInputModel.class)).build().generate();
        assertThat(files).hasSize(1);
        assertProcessSchema("processName.json", files.iterator().next(), SchemaVersion.DRAFT_2019_09);
    }

    @Test
    public void testJsonSchemaGenerationForEmptyProcessModel() throws IOException {
        Collection<GeneratedFile> files = new JsonSchemaGenerator.ClassBuilder(Stream.of(EmptyProcessInputModel.class)).build().generate();
        assertThat(files).hasSize(1);

        assertEmptyProcessSchema("emptyProcessName.json", files.iterator().next(), SchemaVersion.DRAFT_2019_09);
    }

    private void assertEmptyProcessSchema(String fileName, GeneratedFile file, SchemaVersion schemaVersion) throws IOException {
        assertThat(Path.of(file.relativePath())).isEqualTo(JsonSchemaUtil.getJsonDir().resolve(fileName));

        ObjectReader reader = new ObjectMapper().reader();
        JsonNode node = reader.readTree(file.contents());

        assertThat(node.get("$schema").asText()).isEqualTo(schemaVersion.getIdentifier());
        assertThat(node.get("type").asText()).isEqualTo("object");
        assertThat(node.get("properties")).isNull();
    }

    private void assertProcessSchema(String fileName, GeneratedFile file, SchemaVersion schemaVersion) throws IOException {
        assertThat(Path.of(file.relativePath())).isEqualTo(JsonSchemaUtil.getJsonDir().resolve(fileName));
        ObjectReader reader = new ObjectMapper().reader();
        JsonNode node = reader.readTree(file.contents());
        assertThat(node.get("$schema").asText()).isEqualTo(schemaVersion.getIdentifier());

        // assert definitions
        String definitionsPath = resolveDefinitionsProperty(schemaVersion);
        assertThat(node.get(definitionsPath).size()).isEqualTo(3);
        assertPersonNode(node.get(definitionsPath).get("Person"), definitionsPath);
        assertAddressNode(node.get(definitionsPath).get("Address"));
        assertColorNode(node.get(definitionsPath).get("Color"));

        assertThat(node.get("type").asText()).isEqualTo("object");
        JsonNode properties = node.get("properties");
        assertThat(properties.size()).isEqualTo(2);
        JsonNode color = properties.get("color");
        assertThat(color.get("$ref").asText()).isEqualTo("#/" + definitionsPath + "/Color");
        JsonNode address = properties.get("person");
        assertThat(address.get("$ref").asText()).isEqualTo("#/" + definitionsPath + "/Person");
    }

    private void assertPersonNode(JsonNode personNode, String definitionsPath) {
        assertThat(personNode).isNotNull();

        assertThat(personNode.get("type").asText()).isEqualTo("object");
        JsonNode personProperties = personNode.get("properties");
        assertThat(personProperties.get("name").get("type").asText()).isEqualTo("string");
        assertThat(personProperties.get("age").get("type").asText()).isEqualTo("integer");
        assertThat(personProperties.get("address").get(REF).asText()).isEqualTo("#/" + definitionsPath + "/Address");
        assertThat(personProperties.get("parent").get(REF).asText()).isEqualTo("#/" + definitionsPath + "/Person");
    }

    private void assertAddressNode(JsonNode addressNode) {
        assertThat(addressNode).isNotNull();

        assertThat(addressNode.get("type").asText()).isEqualTo("object");
        JsonNode addressProperties = addressNode.get("properties");
        assertThat(addressProperties.get("street").get("type").asText()).isEqualTo("string");
        JsonNode dateNode = addressProperties.get("date");
        assertThat(dateNode.get("type").asText()).isEqualTo("string");
        assertThat(dateNode.get("format").asText()).isEqualTo("date-time");
    }

    private void assertColorNode(JsonNode colorNode) {
        assertThat(colorNode).isNotNull();

        assertThat(colorNode.get("type").asText()).isEqualTo("string");
        assertThat(colorNode.get("enum")).isInstanceOf(ArrayNode.class);
        ArrayNode colors = (ArrayNode) colorNode.get("enum");
        Set<Color> colorValues = EnumSet.noneOf(Color.class);
        colors.forEach(x -> colorValues.add(Color.valueOf(x.asText())));
        assertThat(colorValues.toArray()).containsExactly(Color.values());
    }

    private String resolveDefinitionsProperty(SchemaVersion schemaVersion) {
        return SchemaVersion.DRAFT_2019_09.equals(schemaVersion) ? "$defs" : "definitions";
    }
}
