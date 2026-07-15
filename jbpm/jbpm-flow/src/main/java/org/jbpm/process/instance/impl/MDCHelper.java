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
package org.jbpm.process.instance.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jbpm.process.instance.MDCPopulator;
import org.jbpm.process.instance.ProcessInstance;
import org.slf4j.MDC;

public class MDCHelper {

    private MDCHelper() {
    }

    private static Map<String, Set<String>> addedKeys = new ConcurrentHashMap<>();

    private static ServiceLoader<MDCPopulator> service = ServiceLoader.load(MDCPopulator.class);

    public final static String PROCESS_ID = "processId";
    public final static String INSTANCE_ID = "instanceId";
    public final static String ROOT_PROCESS_ID = "rootProcessId";
    public final static String ROOT_INSTANCE_ID = "rootInstanceId";
    public final static String PARENT_INSTANCE_ID = "parentInstanceId";
    public final static String DESCRIPTION = "description";
    public static final String PROCESS_NAME = "name";
    public static final String PROCESS_VERSION = "version";
    public static final String DEPLOYMENT_ID = "deploymentId";
    public static final String REFERENCE_ID = "referenceId";
    public static final String BUSINESS_KEY = "businessKey";

    public static void fillMDC(ProcessInstance pi) {
        if (pi.getId() != null) {
            Set<String> keys = addedKeys.computeIfAbsent(pi.getId(), k -> new HashSet<>());
            putMDC(keys, INSTANCE_ID, pi.getId());
            putMDC(keys, PROCESS_ID, pi.getProcessId());
            putMDC(keys, ROOT_INSTANCE_ID, pi.getRootProcessInstanceId());
            putMDC(keys, ROOT_PROCESS_ID, pi.getRootProcessId());
            putMDC(keys, PARENT_INSTANCE_ID, pi.getParentProcessInstanceId());
            putMDC(keys, DESCRIPTION, pi.getDescription());
            putMDC(keys, PROCESS_NAME, pi.getProcessName());
            putMDC(keys, PROCESS_VERSION, pi.getProcessVersion());
            putMDC(keys, DEPLOYMENT_ID, pi.getDeploymentId());
            putMDC(keys, REFERENCE_ID, pi.getReferenceId());
            putMDC(keys, BUSINESS_KEY, pi.getBusinessKey());
            service.forEach(p -> p.info2Add(pi).forEach((k, v) -> putMDC(keys, k, v)));
        }
    }

    private static void putMDC(Set<String> keys, String key, String value) {
        if (value != null) {
            keys.add(key);
            MDC.put(key, value);
        }
    }

    public static void clearMDC(ProcessInstance pi) {
        Set<String> keys = addedKeys.remove(pi.getId());
        if (keys != null) {
            keys.forEach(MDC::remove);
        }
    }
}
