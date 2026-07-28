package org.kie.kogito.codegen.process;

import java.util.NoSuchElementException;

import org.kie.kogito.codegen.api.context.KogitoBuildContext;
import org.kie.kogito.codegen.api.template.TemplatedGenerator;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcess;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.StringLiteralExpr;

import static org.kie.kogito.internal.utils.ConversionUtils.sanitizeClassName;

public record AliasGenerator(String code, String path) {

    public static AliasGenerator generate(KogitoBuildContext context, KogitoWorkflowProcess process) {

        String resourceClazzName = sanitizeClassName(process.getId() + "WorkflowAliasProducer");
        String relativePath = process.getPackageName().replace(".", "/") + "/" + resourceClazzName + ".java";
        CompilationUnit cu = TemplatedGenerator.builder().withPackageName(process.getPackageName()).build(context, "AliasProducer").compilationUnitOrThrow();
        ClassOrInterfaceDeclaration template = cu
                .findFirst(ClassOrInterfaceDeclaration.class)
                .orElseThrow(() -> new NoSuchElementException("Compilation unit doesn't contain a class or interface declaration!"));
        template.setName(resourceClazzName);
        template.findAll(StringLiteralExpr.class).forEach(s -> interpolateStrings(s, process));
        return new AliasGenerator(cu.toString(), relativePath);

    }

    private static void interpolateStrings(StringLiteralExpr expr, KogitoWorkflowProcess process) {
        expr.setValue(expr.getValue().replace("$targetName$", process.getId()).replace("$sourceName$", process.getProcessId().toString()));
    }

}
