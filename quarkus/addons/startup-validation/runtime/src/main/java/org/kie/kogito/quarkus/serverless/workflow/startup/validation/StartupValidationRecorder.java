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
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class StartupValidationRecorder {

    private final Logger LOGGER = LoggerFactory.getLogger(StartupValidationRecorder.class);

    private RuntimeValue<StartupValidationConfig> configRuntimeValue;

    public StartupValidationRecorder(RuntimeValue<StartupValidationConfig> configRuntimeValue) {
        this.configRuntimeValue = configRuntimeValue;
    }

    public void executeValidation(Map<String, List<String>> generatedWorkflowVersions) {
        AtomicBoolean halt = new AtomicBoolean();
        StartupValidationConfig config = configRuntimeValue.getValue();
        outerLoop: if (config.swf() != null) {
            for (Map.Entry<String, StartupValidationConfig.StartupValidationConfigGroup> entry : config.swf().entrySet()) {
                List<String> generatedVersions = generatedWorkflowVersions.get(entry.getKey());
                if (generatedVersions == null) {
                    System.err.println(">>> [FATAL] Runtime Startup Validation failed: workflow: '" + entry.getKey() + "' is not present in current runtime.");
                    halt.set(true);
                    break outerLoop;
                } else {
                    for (String configuredVersion : entry.getValue().versions()) {
                        if (!generatedVersions.contains(configuredVersion)) {
                            System.err.println(
                                    ">>> [FATAL] Runtime Startup Validation failed: version: '" + configuredVersion + "' of workflow: '" + entry.getKey() + "' is not present in current runtime.");
                            halt.set(true);
                            break outerLoop;
                        }
                    }
                }
            }
        }
        if (halt.get()) {
            if (generatedWorkflowVersions.isEmpty()) {
                System.err.println(">>> [FATAL] There are no workflows in current runtime.");
            } else {
                System.err.println(">>> [FATAL] You must execute any of the available workflow versions:");
                for (Map.Entry<String, List<String>> entry : generatedWorkflowVersions.entrySet()) {
                    System.out.println(" - > workflow: '" + entry.getKey() + "', versions: " + entry.getValue().stream().map(version -> "'" + version + "'").toList());
                }
            }
            System.err.println(">>> [FATAL] Halting JVM before runtime starts!.");
            doHalt();
        } else {
            for (Map.Entry<String, StartupValidationConfig.StartupValidationConfigGroup> entry : config.swf().entrySet()) {
                LOGGER.debug("Workflow: " + entry.getKey() + " is available to execute the configured versions in current runtime: " + entry.getValue().versions());
            }
        }
    }

    /**
     * Facilitates testing.
     */
    void doHalt() {
        throw new Error("Invalid workflow version configuration.");
    }
}
