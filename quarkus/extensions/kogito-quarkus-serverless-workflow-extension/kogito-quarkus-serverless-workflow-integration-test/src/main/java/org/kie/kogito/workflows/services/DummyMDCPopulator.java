package org.kie.kogito.workflows.services;

import java.util.Map;

import org.jbpm.process.instance.MDCPopulator;
import org.jbpm.process.instance.ProcessInstance;

public class DummyMDCPopulator implements MDCPopulator {

    @Override
    public Map<String, String> info2Add(ProcessInstance processIntance) {
        return Map.of("Master", "Javierito");
    }
}
