package org.kie.kogito.jobs.api.event.serialization;

import java.io.IOException;

import org.kie.api.definition.process.KogitoProcessId;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.TextNode;

public class KogitoProcessIdDeserializer extends StdDeserializer<KogitoProcessId> {

    protected KogitoProcessIdDeserializer() {
        super(KogitoProcessId.class);
    }

    @Override
    public KogitoProcessId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.readValueAsTree();
        return new KogitoProcessId(node.get("processId").textValue(), node.get("version") instanceof TextNode version ? version.textValue() : null);
    }

}
