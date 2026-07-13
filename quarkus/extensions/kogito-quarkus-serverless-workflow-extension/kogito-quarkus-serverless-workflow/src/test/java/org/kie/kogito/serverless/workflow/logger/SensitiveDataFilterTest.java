package org.kie.kogito.serverless.workflow.logger;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;

import io.smallrye.config.common.utils.StringUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class SensitiveDataFilterTest {

    @Test
    void testMask() {
        SensitiveDataFilter filter = new SensitiveDataFilter(Set.of("Javierito", "Fulanito"), "...");
        LogRecord record = new LogRecord(Level.INFO, "Javierito and Fulanito are uncommon names");
        filter.isLoggable(record);
        assertThat(record.getMessage()).isEqualTo("... and ... are uncommon names");
    }

    @Test
    void testDefaultPattern() {
        SensitiveDataFilter filter = new SensitiveDataFilter(Set.of(StringUtil.split(SensitiveDataFilter.DEFAULT_PATTERNS)), "****");
        LogRecord record = new LogRecord(Level.INFO,
                "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        filter.isLoggable(record);
        assertThat(record.getMessage()).isEqualTo("Authorization: Bearer ****");
    }
}
