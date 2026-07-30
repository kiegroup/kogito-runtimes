package org.kie.kogito.jobs.api.event.serialization;

import org.kie.api.definition.process.KogitoProcessId;

import com.fasterxml.jackson.databind.Module.SetupContext;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JobJacksonModule extends SimpleModule {
    @Override
    public void setupModule(SetupContext context) {
        super.setMixInAnnotation(KogitoProcessId.class, KogitoProcessIdMixin.class);
        super.setupModule(context);
    }

}
