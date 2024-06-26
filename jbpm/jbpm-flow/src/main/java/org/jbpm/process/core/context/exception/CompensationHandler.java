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
package org.jbpm.process.core.context.exception;

import java.io.Serializable;

import org.kie.api.definition.process.Node;

public class CompensationHandler implements ExceptionHandler, Serializable {

    private static final long serialVersionUID = 510l;

    private Node node;

    public Node getnode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public String toString() {
        return "Compensation Handler [" + this.node.getName() + ", " + this.node.getUniqueId() + "]";
    }

}
