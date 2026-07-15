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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.template.InvalidTemplateException;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;
import org.kie.kogito.codegen.core.AbstractApplicationSection;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;

public class ProcessContainerGenerator extends AbstractApplicationSection {

    public static final String SECTION_CLASS_NAME = "Processes";

    private final List<ProcessGenerator> processes;
    private final TemplatedGenerator templatedGenerator;

    public ProcessContainerGenerator(KogitoBuildContext context) {
        super(context, SECTION_CLASS_NAME);
        this.processes = new ArrayList<>();
        this.templatedGenerator = TemplatedGenerator.builder()
                .withTargetTypeName(SECTION_CLASS_NAME)
                .build(context, "ProcessContainer");
    }

    public void addProcess(ProcessGenerator p) {
        processes.add(p);
    }

    public List<ProcessGenerator> getProcesses() {
        return Collections.unmodifiableList(this.processes);
    }

    @Override
    public CompilationUnit compilationUnit() {
        CompilationUnit compilationUnit = templatedGenerator.compilationUnitOrThrow("Invalid Template: No CompilationUnit");
        if (!context.hasDI()) {
            BlockStmt body = compilationUnit.findFirst(ConstructorDeclaration.class).map(ConstructorDeclaration::getBody)
                    .orElseThrow(() -> new InvalidTemplateException(
                            templatedGenerator,
                            "Cannot find consctructor method body"));
            for (ProcessGenerator process : processes) {
                ObjectCreationExpr newProcess = new ObjectCreationExpr()
                        .setType(process.targetCanonicalName())
                        .addArgument("application")
                        .addArgument(new NullLiteralExpr());
                MethodCallExpr method = new MethodCallExpr(null, "registerProcess").addArgument(new MethodCallExpr(newProcess, "configure"));
                body.addStatement(method);
            }
        }
        return compilationUnit;
    }

}
