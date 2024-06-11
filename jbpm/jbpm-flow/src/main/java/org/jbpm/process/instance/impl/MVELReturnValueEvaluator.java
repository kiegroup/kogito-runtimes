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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.drools.base.definitions.impl.KnowledgePackageImpl;
import org.drools.base.definitions.rule.impl.RuleImpl;
import org.drools.base.rule.accessor.GlobalResolver;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.mvel.MVELDialectRuntimeData;
import org.drools.mvel.expr.MVELCompilationUnit;
import org.drools.mvel.expr.MVELCompileable;
import org.jbpm.workflow.instance.impl.MVELProcessHelper;
import org.kie.api.definition.KiePackage;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.mvel2.integration.VariableResolverFactory;

public class MVELReturnValueEvaluator extends AbstractReturnValueEvaluator implements MVELCompileable {

    private MVELCompilationUnit unit;

    private Serializable compiledExpression;

    public MVELReturnValueEvaluator(MVELCompilationUnit unit) {
        super("MVEL", unit.getExpression());
        this.unit = unit;
    }

    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        unit = (MVELCompilationUnit) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(unit);
    }

    public void compile(MVELDialectRuntimeData data) {
        compiledExpression = unit.getCompiledExpression(data);
    }

    public void compile(MVELDialectRuntimeData data, RuleImpl rule) {
        compiledExpression = unit.getCompiledExpression(data);
    }

    public Object evaluate(KogitoProcessContext context) {
        int length = unit.getOtherIdentifiers().length;
        Object[] vars = new Object[length];
        if (unit.getOtherIdentifiers() != null) {
            for (int i = 0; i < length; i++) {
                vars[i] = context.getVariable(unit.getOtherIdentifiers()[i]);
            }
        }

        InternalWorkingMemory internalWorkingMemory = (InternalWorkingMemory) context.getKieRuntime();

        VariableResolverFactory factory = unit.getFactory(context,
                null, // No previous declarations
                null, // No rule
                null, // No "right object" 
                null, // No (left) tuples
                vars,
                internalWorkingMemory,
                (GlobalResolver) context.getKieRuntime().getGlobals());

        // do we have any functions for this namespace?
        KiePackage pkg = context.getKieRuntime().getKieBase().getKiePackage("MAIN");
        if (pkg instanceof KnowledgePackageImpl) {
            MVELDialectRuntimeData data = (MVELDialectRuntimeData) ((KnowledgePackageImpl) pkg).getDialectRuntimeRegistry().getDialectData(dialect());
            factory.setNextFactory(data.getFunctionFactory());
        }

        Object value = MVELProcessHelper.evaluator().executeExpression(compiledExpression, null, factory);

        if (!(value instanceof Boolean)) {
            throw new RuntimeException("Constraints must return boolean values: " +
                    unit.getExpression() + " returns " + value +
                    (value == null ? "" : " (type=" + value.getClass()));
        }
        return ((Boolean) value).booleanValue();

    }

}
