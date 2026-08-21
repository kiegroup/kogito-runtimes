<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
  -->
# KIE Process Instance Dynamic Calls Add-On (Experimental)

> ⚠️ **EXPERIMENTAL — NOT FOR PRODUCTION USE**
>
> This add-on is an experiment and is **not** meant to be used in production.
> It is **excluded from the default build/release reactor** — it is not compiled,
> published, or shipped in any KIE/SonataFlow/Kogito distribution or container image.
> No SonataFlow/Kogito runtime, the `kn-plugin-workflow` CLI, or any generated
> application depends on it.

## What it does

The add-on exposes a REST resource (`/_dynamic/{processId}/{instanceId}/rest`) that lets a
caller trigger an outbound REST invocation on behalf of an existing process instance and
write the response back into that instance's variables.

## Status

This module was written as a proof-of-concept. It is **not hardened** and is unsuitable for
any real deployment as-is. It is kept in the repository for reference and experimentation
only, and is intentionally left out of the build so it cannot be published or shipped.

## Building it locally (experiment only)

Because the module is commented out of the reactor
(`quarkus/addons/pom.xml` and `addons/common/pom.xml`), it is not built by a normal
`mvn install`. To build it in isolation for experimentation, re-enable those `<module>`
entries locally — **do not** commit that change or ship the resulting artifacts.
