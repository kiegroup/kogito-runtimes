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

package org.kie.kogito.event.impl.adapter;

import java.util.HashMap;
import java.util.Map;

import org.kie.api.runtime.process.ProcessInstance;
import org.kie.kogito.event.process.ProcessInstanceEventMetadata;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcessInstance;
import org.kie.kogito.internal.utils.ConversionUtils;

public class AdapterHelper {

    public static Map<String, Object> buildProcessMetadata(KogitoWorkflowProcessInstance pi) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(ProcessInstanceEventMetadata.PROCESS_INSTANCE_ID_META_DATA, pi.getId());
        metadata.put(ProcessInstanceEventMetadata.PROCESS_VERSION_META_DATA, pi.getProcessVersion());
        metadata.put(ProcessInstanceEventMetadata.PROCESS_ID_META_DATA, pi.getProcessId());
        metadata.put(ProcessInstanceEventMetadata.PROCESS_INSTANCE_STATE_META_DATA, String.valueOf(pi.getState()));
        metadata.put(ProcessInstanceEventMetadata.PROCESS_TYPE_META_DATA, pi.getProcess().getType());
        metadata.put(ProcessInstanceEventMetadata.PARENT_PROCESS_INSTANCE_ID_META_DATA, pi.getParentProcessInstanceId());
        metadata.put(ProcessInstanceEventMetadata.ROOT_PROCESS_ID_META_DATA, pi.getRootProcessId());
        metadata.put(ProcessInstanceEventMetadata.ROOT_PROCESS_INSTANCE_ID_META_DATA, pi.getRootProcessInstanceId());
        return metadata;
    }

    public static String extractRuntimeSource(String service, Map<String, String> metadata) {
        return buildSource(service, metadata.get(ProcessInstanceEventMetadata.PROCESS_ID_META_DATA), metadata.get(ProcessInstanceEventMetadata.PROCESS_VERSION_META_DATA));
    }

    public static String buildSource(String service, ProcessInstance instance) {
        return buildSource(service, instance.getProcessId(), instance.getProcessVersion());
    }

    public static String buildSource(String service, String id, String version) {
        StringBuilder sb = new StringBuilder();
        if (service != null) {
            sb.append(service);
        }
        if (id != null) {
            appendSeparator(sb).append(ConversionUtils.sanitizeToSimpleName(id));
            if (version != null) {
                appendSeparator(sb).append(version);
            }
        }
        return sb.toString();
    }

    private static StringBuilder appendSeparator(StringBuilder sb) {
        int size = sb.length();
        return size == 0 || sb.charAt(size - 1) != '/' ? sb.append('/') : sb;
    }
}
