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

import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.jbpm.ruleflow.core.RuleFlowProcessFactory;
import org.jbpm.ruleflow.core.WorkflowElementIdentifierFactory;
import org.jbpm.test.util.AbstractBaseTest;
import org.junit.jupiter.api.Test;
import org.kie.api.definition.process.WorkflowElementIdentifier;
import org.kie.kogito.internal.process.runtime.KogitoProcessRuntime;
import org.slf4j.LoggerFactory;

public class ForEachTest extends AbstractBaseTest {

    private static WorkflowElementIdentifier one = WorkflowElementIdentifierFactory.fromExternalFormat("one");
    private static WorkflowElementIdentifier two = WorkflowElementIdentifierFactory.fromExternalFormat("two");
    private static WorkflowElementIdentifier three = WorkflowElementIdentifierFactory.fromExternalFormat("three");
    private static WorkflowElementIdentifier five = WorkflowElementIdentifierFactory.fromExternalFormat("five");

    public void addLogger() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Test
    public void test() {
        RuleFlowProcessFactory factory = RuleFlowProcessFactory.createProcess("ParentProcess");
        factory.variable("x", new ObjectDataType("java.lang.String"));
        factory.variable("y", new ObjectDataType("java.lang.String"));
        factory.variable("list", new ObjectDataType("java.util.List"));
        factory.variable("listOut", new ObjectDataType("java.util.List"));
        factory.name("Parent Process");
        factory.packageName("org.drools.bpmn2");
        factory.dynamic(false);
        factory.version("1.0");
        factory.visibility("Private");
        factory.metaData("TargetNamespace", "http://www.example.org/MinimalExample");
        org.jbpm.ruleflow.core.factory.StartNodeFactory startNode1 = factory.startNode(one);
        startNode1.name("StartProcess");
        startNode1.done();
        org.jbpm.ruleflow.core.factory.ForEachNodeFactory forEachNode2 = factory.forEachNode(two);
        forEachNode2.metaData("MICollectionOutput", "_2_listOutOutput");
        forEachNode2.metaData("x", 96);
        forEachNode2.metaData("width", 110);
        forEachNode2.metaData("y", 16);
        forEachNode2.metaData("MICollectionInput", "_2_input");
        forEachNode2.metaData("height", 48);
        forEachNode2.collectionExpression("list");
        forEachNode2.variable("x", new ObjectDataType("java.lang.String"));
        forEachNode2.outputCollectionExpression("listOut");
        forEachNode2.outputVariable("y", new ObjectDataType("java.lang.String"));

        forEachNode2.actionNode(five).action((kcontext) -> System.out.println(kcontext.getVariable("x"))).done();
        forEachNode2.linkIncomingConnections(five);
        forEachNode2.linkOutgoingConnections(five);

        forEachNode2.done();
        org.jbpm.ruleflow.core.factory.EndNodeFactory endNode3 = factory.endNode(three);
        endNode3.name("EndProcess");
        endNode3.terminate(true);
        endNode3.done();
        factory.connection(one, two, "_1-_2");
        factory.connection(two, three, "_2-_3");
        factory.validate();

        List<String> list = new ArrayList<String>();
        list.add("first");
        list.add("second");
        List<String> listOut = new ArrayList<String>();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("x", "oldValue");
        parameters.put("list", list);
        parameters.put("listOut", listOut);

        KogitoProcessRuntime kruntime = createKogitoProcessRuntime(factory.getProcess());

        kruntime.startProcess("ParentProcess", parameters);
    }

}
