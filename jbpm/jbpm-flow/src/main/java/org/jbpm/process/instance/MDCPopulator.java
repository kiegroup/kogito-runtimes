package org.jbpm.process.instance;

import java.util.Map;

public interface MDCPopulator {
    Map<String, String> info2Add(ProcessInstance processIntance);
}
