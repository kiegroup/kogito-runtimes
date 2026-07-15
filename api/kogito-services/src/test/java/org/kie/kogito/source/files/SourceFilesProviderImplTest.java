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
package org.kie.kogito.source.files;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.api.definition.process.KogitoProcessId;

import static org.assertj.core.api.Assertions.assertThat;

class SourceFilesProviderImplTest {

    private SourceFilesProviderImpl sourceFilesProvider;

    @BeforeEach
    public void setup() {
        sourceFilesProvider = new SourceFilesProviderImpl();
    }

    @Test
    void addSourceFile() {
        KogitoProcessId id = new KogitoProcessId("a_process");
        sourceFilesProvider.addSourceFile(id, new SourceFile("myworkflow.sw.json"));

        assertThat(sourceFilesProvider.getProcessSourceFiles(id))
                .contains(new SourceFile("myworkflow.sw.json"));
    }

    @Test
    void getSourceFilesByProcessId() {
        KogitoProcessId id = new KogitoProcessId("a_process");
        KogitoProcessId anotherId = new KogitoProcessId("a_process", "1.0.0");
        sourceFilesProvider.addSourceFile(id, new SourceFile("myworkflow.sw.json"));
        sourceFilesProvider.addSourceFile(id, new SourceFile("myworkflow.sw.yaml"));

        sourceFilesProvider.addSourceFile(anotherId, new SourceFile("myanotherworkflow.sw.json"));
        sourceFilesProvider.addSourceFile(anotherId, new SourceFile("myanotherworkflow.sw.yaml"));

        assertThat(sourceFilesProvider.getProcessSourceFiles(id))
                .containsExactlyInAnyOrder(
                        new SourceFile("myworkflow.sw.json"),
                        new SourceFile("myworkflow.sw.yaml"));

        assertThat(sourceFilesProvider.getProcessSourceFiles(anotherId))
                .containsExactlyInAnyOrder(
                        new SourceFile("myanotherworkflow.sw.json"),
                        new SourceFile("myanotherworkflow.sw.yaml"));
    }

    @Test
    void getSourceFilesByProcessIdShouldNotReturnNull() {
        assertThat(sourceFilesProvider.getProcessSourceFiles(new KogitoProcessId("a_process")))
                .isEmpty();
    }

    @Test
    void getValidSourceFileDefinitionByProcessIdTest() {
        KogitoProcessId petStoreJson = new KogitoProcessId("petstore_json_process");
        KogitoProcessId petStoreYaml = new KogitoProcessId("petstore_yaml_process");
        KogitoProcessId petStoreSWJson = new KogitoProcessId("petstore_sw_json_process");
        SourceFile petstoreJson = new SourceFile("petstore.json");
        SourceFile petstoreSwJson = new SourceFile("petstore.sw.json");
        SourceFile ymlgreetSwYml = new SourceFile("ymlgreet.sw.yml");

        sourceFilesProvider.addSourceFile(petStoreJson, petstoreJson);
        sourceFilesProvider.addSourceFile(petStoreSWJson, petstoreSwJson);
        sourceFilesProvider.addSourceFile(petStoreYaml, ymlgreetSwYml);

        assertThat(sourceFilesProvider.getProcessSourceFile(petStoreSWJson)).contains(petstoreSwJson);
        assertThat(sourceFilesProvider.getProcessSourceFile(petStoreYaml)).contains(ymlgreetSwYml);

    }

    @Test
    void getInvalidSourceFileDefinitionByProcessIdTest() {
        KogitoProcessId petStoreJson = new KogitoProcessId("petstore_json_process");
        KogitoProcessId invalid = new KogitoProcessId("invalid_process");
        sourceFilesProvider.addSourceFile(petStoreJson, new SourceFile("petstore.json"));

        //invalid extension
        assertThat(sourceFilesProvider.getProcessSourceFile(petStoreJson)).isEmpty();
        //invalid process
        assertThat(sourceFilesProvider.getProcessSourceFile(invalid)).isEmpty();
    }
}
