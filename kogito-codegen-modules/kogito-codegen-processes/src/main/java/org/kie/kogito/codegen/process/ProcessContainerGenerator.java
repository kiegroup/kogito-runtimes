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

import org.kie.api.definition.process.KogitoProcessId;
import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;
import org.kie.kogito.codegen.core.AbstractApplicationSection;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.UnknownType;

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
            BlockStmt body = new BlockStmt();
            ClassOrInterfaceDeclaration clazz = compilationUnit.findFirst(ClassOrInterfaceDeclaration.class).orElseThrow();
            clazz.addMethod("registerProcesses").setBody(body).setName("registerProcesses").setPrivate(true);
            for (ProcessGenerator process : processes) {
                MethodCallExpr newProcess = new MethodCallExpr(new ObjectCreationExpr()
                        .setType(process.targetCanonicalName())
                        .addArgument("application")
                        .addArgument(new NullLiteralExpr()), "configure");
                body.addStatement(new MethodCallExpr(new NameExpr("mappedProcesses"), "computeIfAbsent").addArgument(
                        new ObjectCreationExpr().setType(KogitoProcessId.class).addArgument(new StringLiteralExpr(process.processId()))
                                .addArgument(new StringLiteralExpr(process.getProcess().getVersion())))
                        .addArgument(new LambdaExpr(new Parameter(new UnknownType(), "k"), newProcess)));
            }
        }
        return compilationUnit;
    }

}
