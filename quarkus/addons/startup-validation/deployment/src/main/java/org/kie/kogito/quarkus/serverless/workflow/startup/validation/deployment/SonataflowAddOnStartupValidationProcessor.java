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
package org.kie.kogito.quarkus.serverless.workflow.startup.validation.deployment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.kogito.codegen.process.ProcessExecutableModelGenerator;
import org.kie.kogito.codegen.process.ProcessGenerator;
import org.kie.kogito.quarkus.addons.common.deployment.KogitoCapability;
import org.kie.kogito.quarkus.addons.common.deployment.OneOfCapabilityKogitoAddOnProcessor;
import org.kie.kogito.quarkus.extensions.spi.deployment.KogitoProcessContainerGeneratorBuildItem;
import org.kie.kogito.quarkus.serverless.workflow.startup.validation.StartupValidationRecorder;

import io.quarkus.agroal.spi.JdbcDataSourceBuildItem;
import io.quarkus.datasource.deployment.spi.DefaultDataSourceDbKindBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Produce;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ServiceStartBuildItem;
import io.quarkus.smallrye.reactivemessaging.deployment.items.ConnectorBuildItem;

public class SonataflowAddOnStartupValidationProcessor extends OneOfCapabilityKogitoAddOnProcessor {

    private static final String FEATURE = "sonataflow-addons-quarkus-startup-validation";

    public SonataflowAddOnStartupValidationProcessor() {
        super(KogitoCapability.SERVERLESS_WORKFLOW);
    }

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Produce(DefaultDataSourceDbKindBuildItem.class)
    @Produce(JdbcDataSourceBuildItem.class)
    @Produce(ConnectorBuildItem.class)
    @Record(ExecutionTime.RUNTIME_INIT)
    public ServiceStartBuildItem recordValidation(StartupValidationRecorder recorder,
            List<KogitoProcessContainerGeneratorBuildItem> processContainerGeneratorBuildItems) {

        Map<String, List<String>> generatedWorkflowVersions = new HashMap<>();
        processContainerGeneratorBuildItems.stream().flatMap(it -> it.getProcessContainerGenerators().stream())
                .flatMap(processContainerGenerator -> processContainerGenerator.getProcesses().stream())
                .map(ProcessGenerator::getProcessExecutable)
                .map(ProcessExecutableModelGenerator::process)
                .forEach(process -> generatedWorkflowVersions.computeIfAbsent(process.getId(), s -> new ArrayList<>()).add(process.getVersion()));
        recorder.executeValidation(generatedWorkflowVersions);

        // Ensure this runs before StartupEvents.
        return new ServiceStartBuildItem("sonataflow-swf-startup-validation");
    }
}
