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
package org.jbpm.compiler.canonical.builtin;

import org.jbpm.process.core.ContextResolver;
import org.jbpm.process.instance.impl.ExpressionReturnValueEvaluator;
import org.kie.kogito.internal.utils.ConversionUtils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;

public class JsonpathReturnValueEvaluatorBuilder implements ReturnValueEvaluatorBuilder {

    @Override
    public boolean accept(String dialect) {
        return dialect.toLowerCase().contains("jsonpath");
    }

    @Override
    public Expression build(ContextResolver resolver, String expression, Class<?> type, String rootName) {
        NodeList<Expression> arguments = new NodeList<>();
        arguments.add(new StringLiteralExpr("jsonpath"));
        arguments.add(new StringLiteralExpr(ConversionUtils.sanitizeString(expression)));
        arguments.add(new StringLiteralExpr(rootName));
        arguments.add(new ClassExpr(StaticJavaParser.parseClassOrInterfaceType(type.getName())));

        return new ObjectCreationExpr(null, StaticJavaParser.parseClassOrInterfaceType(ExpressionReturnValueEvaluator.class.getName()), arguments);

    }

}
