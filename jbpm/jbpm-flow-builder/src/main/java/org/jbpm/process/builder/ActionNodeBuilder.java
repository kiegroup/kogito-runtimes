/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.builder;

import java.util.HashMap;
import java.util.Map;

import org.drools.compiler.lang.descr.ActionDescr;
import org.drools.compiler.lang.descr.ProcessDescr;
import org.jbpm.process.builder.dialect.ProcessDialect;
import org.jbpm.process.builder.dialect.ProcessDialectRegistry;
import org.jbpm.process.core.impl.DataTransformerRegistry;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.core.impl.DroolsConsequenceAction;
import org.jbpm.workflow.core.impl.NodeImpl;
import org.jbpm.workflow.core.node.ActionNode;
import org.jbpm.workflow.core.node.Transformation;
import org.kie.api.definition.process.Node;
import org.kie.api.definition.process.Process;
import org.kie.api.runtime.process.DataTransformer;

public class ActionNodeBuilder extends ExtendedNodeBuilder {

    public void build(Process process,
            ProcessDescr processDescr,
            ProcessBuildContext context,
            Node node) {
        super.build(process, processDescr, context, node);
        ActionNode actionNode = (ActionNode) node;
        DroolsConsequenceAction action = (DroolsConsequenceAction) actionNode.getAction();
        ActionDescr actionDescr = new ActionDescr();
        actionDescr.setText(action.getConsequence());
        actionDescr.setResource(processDescr.getResource());

        ProcessDialect dialect = ProcessDialectRegistry.getDialect(action.getDialect());
        dialect.getActionBuilder().build(context, action, actionDescr, (NodeImpl) node);

        Transformation transformation = (Transformation) node.getMetaData().get("Transformation");
        if (transformation != null) {
            WorkflowProcess wfProcess = (WorkflowProcess) process;
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("imports", wfProcess.getImports());
            parameters.put("classloader", context.getConfiguration().getClassLoader());

            DataTransformer transformer = DataTransformerRegistry.get().find(transformation.getLanguage());
            transformation.setCompiledExpression(transformer.compile(transformation.getExpression(), parameters));
        }
    }

}
