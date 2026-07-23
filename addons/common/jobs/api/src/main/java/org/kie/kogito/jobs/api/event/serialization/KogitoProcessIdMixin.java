package org.kie.kogito.jobs.api.event.serialization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

abstract class KogitoProcessIdMixin {
    @JsonCreator
    public KogitoProcessIdMixin(@JsonProperty("processId") String id, @JsonProperty("version") String version) {
    }
}
