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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.auth.IdentityProviderFactory;
import org.kie.kogito.auth.IdentityProviders;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

public class IdentityProviderFactoryImplTest {

    private static final String KOGITO_IDENTITY_USER = "john";
    private static final Collection<String> KOGITO_IDENTITY_ROLES = List.of("IT", "task-operator");
    private static final Collection<String> KOGITO_IDENTITY_IMPERSONATOR_ROLES = List.of("root", "task-admin");
    private static final String TEST_USER = "katty";
    private static final Collection<String> TEST_ROLES = List.of("HR", "task-operator");

    private Logger implLogger;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    public void setUp() throws Exception {
        // The "auth disabled" warning is emitted at most once per JVM via a static flag; reset it so each
        // test starts from a clean slate and the once-semantics can be asserted deterministically.
        resetAuthDisabledWarnedFlag();

        implLogger = (Logger) LoggerFactory.getLogger(IdentityProviderFactoryImpl.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        implLogger.addAppender(logAppender);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (implLogger != null && logAppender != null) {
            implLogger.detachAppender(logAppender);
        }
        resetAuthDisabledWarnedFlag();
    }

    private static void resetAuthDisabledWarnedFlag() throws Exception {
        Field field = IdentityProviderFactoryImpl.class.getDeclaredField("AUTH_DISABLED_WARNED");
        field.setAccessible(true);
        ((AtomicBoolean) field.get(null)).set(false);
    }

    private long warnCount() {
        return logAppender.list.stream()
                .filter(event -> event.getLevel() == Level.WARN)
                .filter(event -> event.getFormattedMessage().contains(IdentityProviderFactory.KOGITO_SECURITY_AUTH_ENABLED))
                .count();
    }

    @Test
    public void testResolveIdentityWithAuthDisabled() {
        KogitoAuthConfig config = new KogitoAuthConfig(false, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_ROLES), config);

        Assertions.assertThat(identityProviderFactory.getOrImpersonateIdentity(TEST_USER, TEST_ROLES))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", TEST_USER)
                .matches(identityProvider -> identityProvider.getRoles().containsAll(TEST_ROLES));
    }

    @Test
    public void testResolveIdentityWithAuthEnabled() {
        KogitoAuthConfig config = new KogitoAuthConfig(true, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_ROLES), config);

        Assertions.assertThat(identityProviderFactory.getOrImpersonateIdentity(TEST_USER, TEST_ROLES))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", KOGITO_IDENTITY_USER)
                .matches(identityProvider -> identityProvider.getRoles().containsAll(KOGITO_IDENTITY_ROLES));
    }

    @Test
    public void testResolveImpersonatedIdentityWithAuthEnabled() {
        KogitoAuthConfig config = new KogitoAuthConfig(true, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_IMPERSONATOR_ROLES), config);

        Assertions.assertThat(identityProviderFactory.getOrImpersonateIdentity(TEST_USER, TEST_ROLES))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", TEST_USER)
                .matches(identityProvider -> identityProvider.getRoles().containsAll(TEST_ROLES));
    }

    @Test
    public void testGetOrImpersonateIdentityWithNullUser() {
        KogitoAuthConfig config = new KogitoAuthConfig(true, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(
                IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_IMPERSONATOR_ROLES), config);

        Assertions.assertThat(identityProviderFactory.getOrImpersonateIdentity(null, TEST_ROLES))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", KOGITO_IDENTITY_USER)
                .matches(identityProvider -> identityProvider.getRoles().containsAll(KOGITO_IDENTITY_IMPERSONATOR_ROLES));
    }

    @Test
    public void testGetOrImpersonateIdentityWithBlankUser() {
        KogitoAuthConfig config = new KogitoAuthConfig(true, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(
                IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_IMPERSONATOR_ROLES), config);

        Assertions.assertThat(identityProviderFactory.getOrImpersonateIdentity("  ", TEST_ROLES))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", KOGITO_IDENTITY_USER)
                .matches(identityProvider -> identityProvider.getRoles().containsAll(KOGITO_IDENTITY_IMPERSONATOR_ROLES));
    }

    @Test
    public void testGetOrImpersonateIdentityWithSameUser() {
        KogitoAuthConfig config = new KogitoAuthConfig(true, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(
                IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_IMPERSONATOR_ROLES), config);

        Assertions.assertThat(identityProviderFactory.getOrImpersonateIdentity(KOGITO_IDENTITY_USER, TEST_ROLES))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", KOGITO_IDENTITY_USER)
                .matches(identityProvider -> identityProvider.getRoles().containsAll(KOGITO_IDENTITY_IMPERSONATOR_ROLES));
    }

    @Test
    public void testGetOrImpersonateIdentityWithoutImpersonationRole() {
        KogitoAuthConfig config = new KogitoAuthConfig(true, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(
                IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_ROLES), config);

        Assertions.assertThat(identityProviderFactory.getOrImpersonateIdentity(TEST_USER, TEST_ROLES))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", KOGITO_IDENTITY_USER)
                .matches(identityProvider -> identityProvider.getRoles().containsAll(KOGITO_IDENTITY_ROLES));
    }

    @Test
    public void testGetOrImpersonateIdentityWithNullRoles() {
        KogitoAuthConfig config = new KogitoAuthConfig(true, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(
                IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_IMPERSONATOR_ROLES), config);

        Assertions.assertThat(identityProviderFactory.getOrImpersonateIdentity(TEST_USER, null))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", TEST_USER)
                .matches(identityProvider -> identityProvider.getRoles().isEmpty());
    }

    @Test
    public void testGetOrImpersonateIdentityWithEmptyRoles() {
        KogitoAuthConfig config = new KogitoAuthConfig(true, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(
                IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_IMPERSONATOR_ROLES), config);

        Assertions.assertThat(identityProviderFactory.getOrImpersonateIdentity(TEST_USER, List.of()))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", TEST_USER)
                .matches(identityProvider -> identityProvider.getRoles().isEmpty());
    }

    @Test
    public void testGetOrImpersonateIdentityWithPartialImpersonationRole() {
        Collection<String> partialRoles = List.of("IT", "task-admin"); // task-admin is an impersonation role
        KogitoAuthConfig config = new KogitoAuthConfig(true, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(
                IdentityProviders.of(KOGITO_IDENTITY_USER, partialRoles), config);

        Assertions.assertThat(identityProviderFactory.getOrImpersonateIdentity(TEST_USER, TEST_ROLES))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", TEST_USER)
                .matches(identityProvider -> identityProvider.getRoles().containsAll(TEST_ROLES));
    }

    @Test
    public void testGetOrImpersonateIdentityWithEmptyImpersonationRolesConfig() {
        KogitoAuthConfig config = new KogitoAuthConfig(true, List.of());
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(
                IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_ROLES), config);

        Assertions.assertThat(identityProviderFactory.getOrImpersonateIdentity(TEST_USER, TEST_ROLES))
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", KOGITO_IDENTITY_USER)
                .matches(identityProvider -> identityProvider.getRoles().containsAll(KOGITO_IDENTITY_ROLES));
    }

    @Test
    public void testWarnsOnceWhenAuthDisabled() {
        KogitoAuthConfig config = new KogitoAuthConfig(false, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_ROLES), config);

        // Multiple identity resolutions with auth disabled must still emit the warning only once.
        identityProviderFactory.getOrImpersonateIdentity(TEST_USER, TEST_ROLES);
        identityProviderFactory.getIdentity(TEST_USER, TEST_ROLES);
        identityProviderFactory.getOrImpersonateIdentity(TEST_USER, TEST_ROLES);

        Assertions.assertThat(warnCount()).isEqualTo(1);
    }

    @Test
    public void testDoesNotWarnWhenAuthEnabled() {
        KogitoAuthConfig config = new KogitoAuthConfig(true, KOGITO_IDENTITY_IMPERSONATOR_ROLES);
        IdentityProviderFactoryImpl identityProviderFactory = new IdentityProviderFactoryImpl(IdentityProviders.of(KOGITO_IDENTITY_USER, KOGITO_IDENTITY_ROLES), config);

        identityProviderFactory.getOrImpersonateIdentity(TEST_USER, TEST_ROLES);
        identityProviderFactory.getIdentity(TEST_USER, TEST_ROLES);

        Assertions.assertThat(warnCount()).isZero();
    }

}
