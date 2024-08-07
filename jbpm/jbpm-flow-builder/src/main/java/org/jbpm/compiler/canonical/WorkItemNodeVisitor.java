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
package org.jbpm.compiler.canonical;

import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import org.jbpm.compiler.canonical.descriptors.ExpressionUtils;
import org.jbpm.compiler.canonical.descriptors.TaskDescriptor;
import org.jbpm.compiler.canonical.descriptors.TaskDescriptorBuilder;
import org.jbpm.process.core.Work;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.WorkItemNodeFactory;
import org.jbpm.workflow.core.node.WorkItemNode;

import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import static org.jbpm.ruleflow.core.factory.WorkItemNodeFactory.METHOD_WORK_NAME;
import static org.jbpm.ruleflow.core.factory.WorkItemNodeFactory.METHOD_WORK_PARAMETER;

public class WorkItemNodeVisitor<T extends WorkItemNode> extends AbstractNodeVisitor<T> {

    private static final String METHOD_WORK_PARAMETER_FACTORY = "workParameterFactory";

    private enum ParamType {
        BOOLEAN(Boolean.class.getSimpleName()),
        INTEGER(Integer.class.getSimpleName()),
        FLOAT(Float.class.getSimpleName());

        final String name;

        public String getName() {
            return name;
        }

        ParamType(String name) {
            this.name = name;
        }

        public static ParamType fromString(String name) {
            for (ParamType p : ParamType.values()) {
                if (Objects.equals(p.name, name)) {
                    return p;
                }
            }
            return null;
        }
    }

    public WorkItemNodeVisitor(ClassLoader contextClassLoader) {
        super(contextClassLoader);
    }

    @Override
    protected String getNodeKey() {
        return "workItemNode";
    }

    @Override
    public void visitNode(String factoryField, T node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        Work work = node.getWork();
        String workName = node.getWork().getName();
        final String nodeId = getNodeId(node);

        if (TaskDescriptorBuilder.isBuilderSupported(workName)) {
            final TaskDescriptor taskDescriptor = new TaskDescriptorBuilder(workName)
                    .withProcessMetadata(metadata)
                    .withWorkItemNode(node)
                    .withClassloader(getClassLoader())
                    .build();
            workName = taskDescriptor.getName();
            metadata.getGeneratedHandlers().put(workName, taskDescriptor.generateHandlerClassForService());
        }

        body.addStatement(getAssignedFactoryMethod(factoryField, WorkItemNodeFactory.class, getNodeId(node), getNodeKey(), getWorkflowElementConstructor(node.getId())))
                .addStatement(getNameMethod(node, work.getName()))
                .addStatement(getFactoryMethod(nodeId, METHOD_WORK_NAME, new StringLiteralExpr(workName)));

        if (work.getWorkParametersFactory() != null) {
            body.addStatement(getFactoryMethod(nodeId, METHOD_WORK_PARAMETER_FACTORY, ExpressionUtils.getLiteralExpr(work.getWorkParametersFactory())));
        }

        addWorkItemParameters(work, body, nodeId);
        addNodeMappings(node, body, nodeId);

        body.addStatement(getDoneMethod(nodeId));

        visitMetaData(node.getMetaData(), body, nodeId);

        metadata.getWorkItems().add(workName);
    }

    protected void addWorkItemParameters(Work work, BlockStmt body, String variableName) {
        // This is to ensure that each run of the generator produces the same code.
        final List<Entry<String, Object>> sortedParameters = work.getParameters().entrySet().stream().sorted(Entry.comparingByKey()).toList();
        for (Entry<String, Object> entry : sortedParameters) {
            if (entry.getValue() == null) {
                continue; // interfaceImplementationRef ?
            }
            String paramType = null;
            if (work.getParameterDefinition(entry.getKey()) != null) {
                paramType = work.getParameterDefinition(entry.getKey()).getType().getStringType();
            }
            body.addStatement(getFactoryMethod(variableName, METHOD_WORK_PARAMETER, new StringLiteralExpr(entry.getKey()), getParameterExpr(paramType, entry.getValue())));
        }
    }

    private Expression getParameterExpr(String type, Object value) {
        if (value == null) {
            return new NullLiteralExpr();
        }
        ParamType pType = ParamType.fromString(type);
        if (pType == null) {
            return ExpressionUtils.getLiteralExpr(value);
        }
        switch (pType) {
            case BOOLEAN:
                return new BooleanLiteralExpr(asBoolean(value));
            case FLOAT:
                return new MethodCallExpr()
                        .setScope(new NameExpr(Float.class.getName()))
                        .setName("parseFloat")
                        .addArgument(new StringLiteralExpr(value.toString()));
            case INTEGER:
                return new IntegerLiteralExpr(asInteger(value));
            default:
                return new StringLiteralExpr(value.toString());
        }
    }

    private Boolean asBoolean(Object value) {
        return value instanceof Boolean ? (Boolean) value : Boolean.parseBoolean(value.toString());
    }

    private Integer asInteger(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(value.toString());
    }
}
