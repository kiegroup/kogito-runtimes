package org.kie.kogito.quarkus.workflows;

import java.util.logging.LogRecord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.InMemoryLogHandler;
import io.quarkus.test.junit.QuarkusTest;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class SensitiveDataFilterTest {

    private final static Logger logger = LoggerFactory.getLogger(SensitiveDataFilterTest.class);

    private InMemoryLogHandler logHandler;
    private org.jboss.logmanager.Logger logContext;

    @BeforeEach
    void setup() {
        logHandler = new InMemoryLogHandler(r -> true);
        logContext = org.jboss.logmanager.LogContext.getLogContext().getLogger(logger.getName());
        logContext.addHandler(logHandler);
    }

    @AfterEach
    void close() {
        logContext.removeHandler(logHandler);
    }

    @Test
    void testLogger() {
        logger.info("Los españoles son muy españoles y mucho españoles");
        assertThat(logHandler.getRecords()).singleElement().extracting(LogRecord::getMessage).isEqualTo("Los ... son muy ... y mucho ...");
    }
}
