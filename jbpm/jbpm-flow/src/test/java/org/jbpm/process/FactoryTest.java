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
package org.jbpm.process;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.ruleflow.core.WorkflowElementIdentifierFactory;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.jupiter.api.Test;
import org.kie.api.definition.process.WorkflowElementIdentifier;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.slf4j.LoggerFactory;

public class FactoryTest extends AbstractBaseTest {

    private static WorkflowElementIdentifier one = WorkflowElementIdentifierFactory.fromExternalFormat("one");
    private static WorkflowElementIdentifier two = WorkflowElementIdentifierFactory.fromExternalFormat("two");
    private static WorkflowElementIdentifier three = WorkflowElementIdentifierFactory.fromExternalFormat("three");
    private static WorkflowElementIdentifier four = WorkflowElementIdentifierFactory.fromExternalFormat("four");

    public void addLogger() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Test
    public void test() {
        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("ExampleProcess");
        factory.variable("x", new ObjectDataType("java.lang.String"));
        factory.variable("y", new ObjectDataType("java.lang.String"));
        factory.variable("list", new ObjectDataType("java.util.List"));
        factory.variable("listOut", new ObjectDataType("java.util.List"));
        factory.name("Example Process");
        factory.packageName("org.drools.bpmn2");
        factory.dynamic(false);
        factory.version("1.0");
        factory.visibility("Private");
        factory.metaData("TargetNamespace", "http://www.example.org/MinimalExample");
        factory.startNode(one)
                .name("StartProcess")
                .done();

        factory.dynamicNode(two)
                .metaData("MICollectionOutput", "_2_listOutOutput")
                .metaData("x", 96)
                .metaData("y", 16)
                .activationExpression(kcontext -> Objects.equals(kcontext.getVariable("x"), kcontext.getVariable("oldValue")))
                .variable("x", new ObjectDataType("java.lang.String"))
                .exceptionHandler(RuntimeException.class.getName(), "java", "System.out.println(\"Error\");")
                .autoComplete(true)
                .language("java")
                .done();

        factory.humanTaskNode(three)
                .name("Task")
                .taskName("Task Name")
                .actorId("Actor")
                .comment("Hey")
                .content("Some content")
                .workParameter("x", "Parameter")
                .inMapping("x", "y")
                .outMapping("y", "x")
                .waitForCompletion(true)
                .timer("1s", null, "java", "")
                .done();

        factory.faultNode(four).name("Fault")
                .faultName("Fault Name")
                .faultVariable("x")
                .done();

        factory.connection(one, two, "_1-_2")
                .connection(two, three, "_2-_3")
                .connection(three, four, "_3-_4");

        factory.validate();

        List<String> list = new ArrayList<String>();
        list.add("first");
        list.add("second");
        List<String> listOut = new ArrayList<String>();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("x", "oldValue");
        parameters.put("list", list);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime(factory.getProcess());

        kruntime.startProcess("ExampleProcess", parameters);
    }

}
