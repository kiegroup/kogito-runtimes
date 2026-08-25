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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kie.kogito.process.Process;
import org.kogito.workitem.rest.RestWorkItemHandler;

import io.quarkus.security.Authenticated;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Authenticated
@Path("/_dynamic")
public class ProcessInstanceDynamicCallsResource {

    /**
     * Comma-separated list of hosts that dynamic REST calls are allowed to target.
     * When not set (the default), every dynamic call is rejected.
     */
    static final String ALLOWED_HOSTS_PROPERTY = "kogito.dynamic.allowed-hosts";

    /**
     * When true, a dynamic REST call may mark a non-dynamic process instance as dynamic (legacy behavior).
     * Defaults to false: only processes declared dynamic (ad-hoc) in their definition accept dynamic calls.
     */
    static final String MARK_INSTANCES_DYNAMIC_PROPERTY = "kogito.dynamic.mark-instances-dynamic";

    /** Default target host applied by {@link RestWorkItemHandler} when none is supplied. */
    private static final String DEFAULT_HOST = "localhost";

    @Inject
    Vertx vertx;
    @Inject
    WebClientOptions sslOptions;
    @Inject
    @ConfigProperty(name = ALLOWED_HOSTS_PROPERTY)
    Optional<List<String>> allowedHosts;
    @Inject
    @ConfigProperty(name = MARK_INSTANCES_DYNAMIC_PROPERTY, defaultValue = "false")
    boolean markInstancesDynamic;
    private RestWorkItemHandler handler;
    private Collection<Process<?>> processes;

    @Inject
    ProcessInstanceDynamicCallsResource(Instance<Process<?>> processes) {
        this.processes = processes.stream().collect(Collectors.toUnmodifiableList());
    }

    @PostConstruct
    void init() {
        handler = new RestWorkItemHandler(WebClient.create(vertx), WebClient.create(vertx, sslOptions));
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{processId}/{processInstanceId}/rest")
    public Response executeRestCall(@PathParam("processId") String processId, @PathParam("processInstanceId") String processInstanceId, RestCallInfo input) {
        checkTargetHostAllowed(input);
        ProcessInstanceDynamicCallHelper.executeRestCall(handler, processes, processId, processInstanceId, input, markInstancesDynamic);
        return Response.status(200).build();
    }

    private void checkTargetHostAllowed(RestCallInfo input) {
        String effectiveHost = resolveEffectiveHost(input);
        if (!isHostAllowed(effectiveHost, allowedHosts)) {
            String message = "Dynamic REST calls to host '" + effectiveHost + "' are not permitted. External calls are denied unless the target host is listed in '"
                    + ALLOWED_HOSTS_PROPERTY + "' (property unset means all dynamic calls are denied).";
            throw new WebApplicationException(message, Response.status(Response.Status.FORBIDDEN).entity(message).type(MediaType.TEXT_PLAIN).build());
        }
    }

    /**
     * Resolves the host a dynamic call would actually target, mirroring {@link RestWorkItemHandler}'s own
     * resolution order: an absolute URL in the {@code endpoint} wins over the {@code host} field, which in
     * turn wins over the handler's {@code localhost} default.
     */
    static String resolveEffectiveHost(RestCallInfo input) {
        String host = input.getHost();
        if (input.getEndpoint() != null) {
            try {
                host = new URL(input.getEndpoint()).getHost();
            } catch (MalformedURLException ex) {
                // not an absolute URL, endpoint is a path relative to the host parameter
            }
        }
        return host == null || host.isBlank() ? DEFAULT_HOST : host;
    }

    static boolean isHostAllowed(String effectiveHost, Optional<List<String>> allowedHosts) {
        return allowedHosts.map(hosts -> hosts.stream().anyMatch(effectiveHost::equalsIgnoreCase)).orElse(false);
    }
}
