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
package org.kie.kogito.process.dynamic;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessInstanceDynamicCallsResourceTest {

    private static RestCallInfo info(String host, String endpoint) {
        RestCallInfo input = new RestCallInfo();
        input.setHost(host);
        input.setEndpoint(endpoint);
        return input;
    }

    @Test
    void testUnsetAllowlistDeniesEveryHost() {
        assertThat(ProcessInstanceDynamicCallsResource.isHostAllowed("localhost", Optional.empty())).isFalse();
        assertThat(ProcessInstanceDynamicCallsResource.isHostAllowed("169.254.169.254", Optional.empty())).isFalse();
    }

    @Test
    void testHostNotInAllowlistIsDenied() {
        Optional<List<String>> allowed = Optional.of(List.of("localhost"));
        assertThat(ProcessInstanceDynamicCallsResource.isHostAllowed("169.254.169.254", allowed)).isFalse();
    }

    @Test
    void testHostInAllowlistIsAllowed() {
        Optional<List<String>> allowed = Optional.of(List.of("localhost", "api.internal"));
        assertThat(ProcessInstanceDynamicCallsResource.isHostAllowed("localhost", allowed)).isTrue();
        assertThat(ProcessInstanceDynamicCallsResource.isHostAllowed("API.INTERNAL", allowed)).isTrue();
    }

    @Test
    void testEffectiveHostFromAbsoluteEndpointUrlWinsOverHostField() {
        // an absolute URL in the endpoint must be the host that gets allowlist-checked (SSRF via endpoint)
        assertThat(ProcessInstanceDynamicCallsResource.resolveEffectiveHost(info("localhost", "http://169.254.169.254/latest/meta-data")))
                .isEqualTo("169.254.169.254");
    }

    @Test
    void testEffectiveHostFromHostFieldWhenEndpointIsRelativePath() {
        assertThat(ProcessInstanceDynamicCallsResource.resolveEffectiveHost(info("api.internal", "/some/path")))
                .isEqualTo("api.internal");
    }

    @Test
    void testEffectiveHostDefaultsToLocalhostWhenUnspecified() {
        assertThat(ProcessInstanceDynamicCallsResource.resolveEffectiveHost(info(null, null))).isEqualTo("localhost");
    }
}
