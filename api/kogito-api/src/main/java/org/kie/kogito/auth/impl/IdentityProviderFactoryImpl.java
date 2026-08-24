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

package org.kie.kogito.auth.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.auth.IdentityProviderFactory;
import org.kie.kogito.auth.IdentityProviders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentityProviderFactoryImpl implements IdentityProviderFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityProviderFactoryImpl.class);

    private static final AtomicBoolean AUTH_DISABLED_WARNED = new AtomicBoolean();

    private final IdentityProvider identityProvider;
    private final KogitoAuthConfig config;

    public IdentityProviderFactoryImpl(IdentityProvider identityProvider, KogitoAuthConfig config) {
        this.identityProvider = identityProvider;
        this.config = config;
    }

    private static void warnAuthDisabledOnce() {
        if (AUTH_DISABLED_WARNED.compareAndSet(false, true)) {
            LOGGER.warn("Kogito security context resolution is DISABLED ({}=false): caller-supplied 'user' and 'group' request parameters are trusted " +
                    "verbatim as the acting identity, so task-level authorization can be bypassed by any client able to reach the REST API. " +
                    "This setting is only intended for local development; do not use it in production.",
                    IdentityProviderFactory.KOGITO_SECURITY_AUTH_ENABLED);
        }
    }

    @Override
    public IdentityProvider getOrImpersonateIdentity(String user, Collection<String> roles) {

        if (!config.isEnabled()) {
            warnAuthDisabledOnce();
            return IdentityProviders.of(user, roles);
        }

        if (!Collections.disjoint(config.getRolesThatAllowImpersonation(), identityProvider.getRoles())
                && user != null && !user.isBlank()
                && !identityProvider.getName().equals(user)) {
            return IdentityProviders.of(user, roles);
        }

        return identityProvider;
    }

    @Override
    public IdentityProvider getIdentity(String user, Collection<String> roles) {

        if (!config.isEnabled()) {
            warnAuthDisabledOnce();
            return IdentityProviders.of(user, roles);
        }
        return identityProvider;
    }
}
