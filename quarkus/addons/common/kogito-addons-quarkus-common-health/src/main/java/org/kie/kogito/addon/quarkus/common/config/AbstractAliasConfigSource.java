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

package org.kie.kogito.addon.quarkus.common.config;

import java.util.HashSet;
import java.util.Set;

import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.usertask.UserTaskAssignmentStrategy;
import org.kie.kogito.usertask.UserTaskInstance;

public abstract class AbstractAliasConfigSource implements ConfigSource {

    protected int ordinal;
    protected String configValue;
    protected final Set<String> propertyNames = new HashSet<>();

    public AbstractAliasConfigSource(Set<String> propertyNames) {
        this.propertyNames.addAll(propertyNames);
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public abstract String getConfigAlias();

    @Override
    public Set<String> getPropertyNames() {
        return propertyNames;
    }

    @Override
    public String getValue(String propertyName) {
        return propertyNames.contains(propertyName) ? configValue : null;
    }

    @Override
    public Optional<String> computeAssignment(UserTaskInstance userTaskInstance, IdentityProvider identityProvider) {
        Set<String> users = new HashSet<>(userTaskInstance.getPotentialUsers());
        users.removeAll(userTaskInstance.getExcludedUsers());
        if (users.size() == 1) {
            return Optional.of(users.iterator().next());
        }
        return Optional.empty();
    }

}
