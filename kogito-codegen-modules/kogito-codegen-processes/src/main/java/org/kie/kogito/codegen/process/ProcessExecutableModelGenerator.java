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
package org.kie.kogito.codegen.process;

import java.util.Map;
import java.util.Optional;

import org.jbpm.compiler.canonical.ProcessMetaData;
import org.jbpm.compiler.canonical.ProcessToExecModelGenerator;
import org.kie.api.definition.process.KogitoProcessId;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;

public class ProcessExecutableModelGenerator {

    private final KogitoWorkflowProcess workFlowProcess;
    private String processFilePath;
    private ProcessMetaData processMetaData;

    public ProcessExecutableModelGenerator(KogitoWorkflowProcess workFlowProcess, ProcessToExecModelGenerator execModelGenerator, Map<KogitoProcessId, KogitoWorkflowProcess> processes) {
        this.workFlowProcess = workFlowProcess;
        this.processMetaData = execModelGenerator.generate(workFlowProcess, processes);
        String processClazzName = processMetaData.getProcessClassName();
        processFilePath = processClazzName.replace('.', '/') + ".java";

    }

    public boolean isPublic() {
        return KogitoWorkflowProcess.PUBLIC_VISIBILITY.equalsIgnoreCase(workFlowProcess.getVisibility());
    }

    public ProcessMetaData generate() {
        return processMetaData;
    }

    public String description() {
        return Optional.ofNullable(workFlowProcess.getMetaData().get("Description"))
                .map(Object::toString).orElse("Executes " + workFlowProcess.getName());
    }

    public String className() {
        return processMetaData.getProcessClassName();
    }

    public String generatedFilePath() {
        return processFilePath;
    }

    public String extractedProcessId() {
        return ProcessToExecModelGenerator.extractProcessId(workFlowProcess.getProcessId());
    }

    public KogitoProcessId getProcessId() {
        return workFlowProcess.getProcessId();
    }

    public KogitoWorkflowProcess process() {
        return workFlowProcess;
    }
}
