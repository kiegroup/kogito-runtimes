package org.kie.kogito.jobs.api.event.serialization;

import org.kie.api.definition.process.KogitoProcessId;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

abstract class KogitoProcessIdMixin {
    @JsonCreator
    public static KogitoProcessId from(@JsonProperty("processId") String id, @JsonProperty(value = "version", required = false) String version) {
        return null;
    }
}
