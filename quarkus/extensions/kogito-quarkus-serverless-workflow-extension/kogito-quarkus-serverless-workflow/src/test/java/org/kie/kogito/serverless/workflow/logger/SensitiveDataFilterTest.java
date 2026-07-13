package org.kie.kogito.serverless.workflow.logger;

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

    private final static InMemoryLogHandler logHandler = new InMemoryLogHandler(r -> true);
    private final static org.jboss.logmanager.Logger logContext = org.jboss.logmanager.LogContext.getLogContext().getLogger(logger.getName());

    @BeforeEach
    void setup() {
        logContext.addHandler(logHandler);
    }

    @AfterEach
    void close() {
        logContext.removeHandler(logHandler);
    }

    @Test
    void testLogger() {
        logger.info("Fulanito es un mierda, cabron y bastardo sevillista");
        assertThat(logHandler.getRecords()).singleElement().extracting(LogRecord::getMessage).isEqualTo("Fulanito es un ..., ... y ... sevillista");
    }

}
