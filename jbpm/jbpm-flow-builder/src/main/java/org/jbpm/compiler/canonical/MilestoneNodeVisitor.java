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

import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.MilestoneNodeFactory;
import org.jbpm.workflow.core.node.MilestoneNode;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

import static org.jbpm.ruleflow.core.factory.MilestoneNodeFactory.METHOD_CONDITION;

public class MilestoneNodeVisitor extends AbstractNodeVisitor<MilestoneNode> {

    public MilestoneNodeVisitor(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    protected String getNodeKey() {
        return "milestoneNode";
    }

    @Override
    public void visitNode(String factoryField, MilestoneNode node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        body.addStatement(getAssignedFactoryMethod(factoryField, MilestoneNodeFactory.class, getNodeId(node), getNodeKey(), getWorkflowElementConstructor(node.getId())))
                .addStatement(getNameMethod(node, "Milestone"));
        if (node.getCondition() != null && !node.getCondition().trim().isEmpty()) {
            body.addStatement(getConditionStatement(node, variableScope));
        }
        body.addStatement(getDoneMethod(getNodeId(node)));
        visitMetaData(node.getMetaData(), body, getNodeId(node));
    }

    private MethodCallExpr getConditionStatement(MilestoneNode node, VariableScope scope) {
        return getFactoryMethod(getNodeId(node), METHOD_CONDITION, createLambdaExpr(node.getCondition(), scope));
    }

}
