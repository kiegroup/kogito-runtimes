package org.kie.kogito.internal.process.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.kie.api.definition.process.KogitoProcessId;

import static org.assertj.core.api.Assertions.assertThat;

public class KogitoWorkflowIdTest {

    @Test
    void testVersionOrder() {
        List<KogitoProcessId> unsortedIds = new ArrayList<>(List.of(new KogitoProcessId("1", "1.0.1"), new KogitoProcessId("1", "1.0.0"), new KogitoProcessId("1", "2.0")));
        Collections.sort(unsortedIds);
        assertThat(unsortedIds).isEqualTo(List.of(new KogitoProcessId("1", "1.0.0"), new KogitoProcessId("1", "1.0.1"), new KogitoProcessId("1", "2.0")));
    }
}
