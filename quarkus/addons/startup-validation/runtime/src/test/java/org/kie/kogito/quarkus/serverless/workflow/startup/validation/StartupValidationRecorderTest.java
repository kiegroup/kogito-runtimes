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
package org.kie.kogito.quarkus.serverless.workflow.startup.validation;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import io.quarkus.runtime.RuntimeValue;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartupValidationRecorderTest {

    @ParameterizedTest
    @MethodSource("getCases")
    void executeValidation(StartupValidationConfig validationConfig, Map<String, List<String>> generatedWorkflowVersions, int wantedHalts) {
        StartupValidationRecorder recorder = spy(new StartupValidationRecorder(new RuntimeValue<>(validationConfig)));
        lenient().doNothing().when(recorder).doHalt();
        recorder.executeValidation(generatedWorkflowVersions);
        verify(recorder, times(wantedHalts)).doHalt();
    }

    private static Stream<Arguments> getCases() {
        return Stream.of(
                Arguments.of(mockValidationConfig("hello-world", List.of("1.0")), Map.of("hello-world", List.of("1.0")), 0),
                Arguments.of(mockValidationConfig("hello-world", List.of("1.0")), Map.of("hello-world", List.of("2.0")), 1),
                Arguments.of(mockValidationConfig("hello-world", List.of("1.0", "2.0")), Map.of("hello-world", List.of("1.0")), 1),
                Arguments.of(mockValidationConfig("hello-world", List.of("1.0", "2.0")), Map.of("hello-world", List.of("2.0")), 1),
                Arguments.of(mockValidationConfig("hello-world", List.of("1.0", "2.0")), Map.of("hello-world", List.of("1.0", "2.0")), 0),
                Arguments.of(mockValidationConfig("hello-world", List.of("1.0", "2.0")), Map.of("hello-world", List.of("1.0", "2.0", "3.0")), 0),
                Arguments.of(mock(StartupValidationConfig.class), Map.of("hello-world", List.of("1.0", "2.0", "3.0")), 0));
    }

    private static StartupValidationConfig mockValidationConfig(String workflowId, List<String> versions) {
        StartupValidationConfig.StartupValidationConfigGroup validationConfigGroup = mock(StartupValidationConfig.StartupValidationConfigGroup.class);
        doReturn(versions).when(validationConfigGroup).versions();
        StartupValidationConfig validationConfig = mock(StartupValidationConfig.class);
        doReturn(Map.of(workflowId, validationConfigGroup)).when(validationConfig).swf();
        return validationConfig;
    }
}
