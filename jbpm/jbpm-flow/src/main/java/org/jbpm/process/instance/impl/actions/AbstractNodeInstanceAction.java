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
package org.jbpm.process.instance.impl.actions;

import java.io.Serializable;
import java.util.Collection;

import org.jbpm.process.instance.impl.Action;
import org.jbpm.workflow.instance.node.CompositeNodeInstance;
import org.kie.api.runtime.process.NodeInstance;
import org.kie.api.runtime.process.NodeInstanceContainer;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;

public abstract class AbstractNodeInstanceAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    private String attachedToNodeId;

    protected AbstractNodeInstanceAction(String attachedToNodeId) {
        this.attachedToNodeId = attachedToNodeId;
    }

    @Override
    public void execute(KogitoProcessContext context) throws Exception {
        NodeInstanceContainer pi = context.getNodeInstance().getNodeInstanceContainer();
        NodeInstance nodeInstance = findNodeByUniqueId(pi.getNodeInstances(), attachedToNodeId);
        if (nodeInstance != null) {
            execute(nodeInstance);
        }
    }

    protected abstract void execute(NodeInstance nodeInstance);

    private static NodeInstance findNodeByUniqueId(Collection<NodeInstance> nodeInstances, String uniqueId) {
        if (nodeInstances != null && !nodeInstances.isEmpty()) {
            for (NodeInstance nInstance : nodeInstances) {
                String nodeUniqueId = nInstance.getNode().getUniqueId();
                if (uniqueId.equals(nodeUniqueId)) {
                    return nInstance;
                }
                if (nInstance instanceof CompositeNodeInstance) {
                    NodeInstance nodeInstance = findNodeByUniqueId(((CompositeNodeInstance) nInstance).getNodeInstances(), uniqueId);
                    if (nodeInstance != null) {
                        return nodeInstance;
                    }
                }
            }
        }
        return null;
    }
}