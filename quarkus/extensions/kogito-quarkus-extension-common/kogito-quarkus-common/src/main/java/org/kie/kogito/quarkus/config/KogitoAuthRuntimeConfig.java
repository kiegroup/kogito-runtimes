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

package org.kie.kogito.quarkus.config;

import java.util.Optional;

import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

public interface KogitoAuthRuntimeConfig {

    /**
     * Enables using the application security context when resolving current User Identity.
     * <p>
     * When {@code true} (the default), the acting identity for process and user-task operations is taken
     * from the authenticated security context. The {@code user}/{@code group} REST query parameters are
     * then only honored for callers that hold one of the roles listed in
     * {@code kogito.security.auth.impersonation.allowed-for-roles}.
     * <p>
     * Setting this to {@code false} restores the legacy behavior in which the {@code user}/{@code group}
     * query parameters are trusted verbatim as the acting identity. That effectively disables task-level
     * authorization for any caller able to reach the REST API (CWE-639) and must not be used in
     * production deployments.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * Comma-separated list of roles that allow identity impersonation when resolving the actual User Identity.
     */
    @WithName("impersonation.allowed-for-roles")
    Optional<String> rolesThatAllowImpersonation();
}
