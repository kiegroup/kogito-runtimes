package org.kie.kogito.jobs.api.event.serialization;

import java.io.IOException;

import org.kie.api.definition.process.KogitoProcessId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class KogitoProcessIdSerializer extends JsonSerializer<KogitoProcessId> {
    @Override
    public void serialize(KogitoProcessId value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("processId", value.id());
        if (value.version() != null) {
            gen.writeStringField("version", value.version());
        }
        gen.writeEndObject();
    }
}
