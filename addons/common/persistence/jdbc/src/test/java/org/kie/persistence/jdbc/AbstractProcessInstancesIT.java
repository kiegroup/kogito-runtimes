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
package org.kie.persistence.jdbc;

import javax.sql.DataSource;

import org.kie.flyway.initializer.KieFlywayInitializer;
import org.kie.kogito.auth.IdentityProviders;
import org.kie.kogito.auth.SecurityPolicy;
import org.kie.kogito.internal.process.workitem.Policy;

abstract class AbstractProcessInstancesIT {

    public static final String TEST_ID = "02ac3854-46ee-42b7-8b63-5186c9889d96";
    public static Policy securityPolicy = SecurityPolicy.of(IdentityProviders.of("john"));

    public static void initMigration(DataSource dataSource) {
        KieFlywayInitializer.builder()
                .withDatasource(dataSource)
                .build()
                .migrate();
    }

    abstract DataSource getDataSource();

}
