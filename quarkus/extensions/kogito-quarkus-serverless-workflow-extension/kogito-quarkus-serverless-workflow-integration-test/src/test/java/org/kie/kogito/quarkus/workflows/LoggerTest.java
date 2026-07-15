package org.kie.kogito.quarkus.workflows;

import java.util.List;
import java.util.logging.LogRecord;

import org.jboss.logmanager.ExtLogRecord;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.process.instance.impl.MDCHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.test.InMemoryLogHandler;
import io.quarkus.test.junit.QuarkusTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
public class LoggerTest {

    private final static Logger logger = LoggerFactory.getLogger(LoggerTest.class);
    private static final String EXPECTED_INSTANCE_ID = "Luke Skywalker";
    private static final String EXPECTED_PROCESS_ID = "Padawan";
    private static final String EXPECTED_BUSINESS_KEY = "Jedi Business";
    private static final String EXPECTED_DESCRIPTION = "Star Wars Soap Opera";
    private static final String EXPECTED_PARENT_INSTANCE_ID = "Darth Vader";
    private static final String EXPECTED_ROOT_ID = "Master";
    private static final String EXPECTED_ROOT_INSTANCE_ID = "Palpatine";
    private static final String EXPECTED_NAME = "Han Solo";
    private static final String EXPECTED_VERSION = "Episode IV";
    private static final String EXPECTED_DEPLOYMENT_ID = "Use to be in the cinemas";

    private InMemoryLogHandler logHandler;
    private org.jboss.logmanager.Logger logContext;
    private ProcessInstance pi;

    @BeforeEach
    void setup() {
        logHandler = new InMemoryLogHandler(r -> true);
        logContext = org.jboss.logmanager.LogContext.getLogContext().getLogger(logger.getName());
        logContext.addHandler(logHandler);
        pi = mock(ProcessInstance.class);
        when(pi.getProcessId()).thenReturn(EXPECTED_PROCESS_ID);
        when(pi.getId()).thenReturn(EXPECTED_INSTANCE_ID);
        when(pi.getBusinessKey()).thenReturn(EXPECTED_BUSINESS_KEY);
        when(pi.getDescription()).thenReturn(EXPECTED_DESCRIPTION);
        when(pi.getParentProcessInstanceId()).thenReturn(EXPECTED_PARENT_INSTANCE_ID);
        when(pi.getRootProcessId()).thenReturn(EXPECTED_ROOT_ID);
        when(pi.getRootProcessInstanceId()).thenReturn(EXPECTED_ROOT_INSTANCE_ID);
        when(pi.getProcessName()).thenReturn(EXPECTED_NAME);
        when(pi.getProcessVersion()).thenReturn(EXPECTED_VERSION);
        when(pi.getDeploymentId()).thenReturn(EXPECTED_DEPLOYMENT_ID);
        MDCHelper.fillMDC(pi);
    }

    @AfterEach
    void close() {
        logContext.removeHandler(logHandler);
        MDCHelper.clearMDC(pi);
    }

    @Test
    void testMaskAndMDC() {
        logger.info("Los españoles son muy españoles y mucho españoles");
        List<LogRecord> records = logHandler.getRecords();
        assertThat(records).hasSize(1);
        ExtLogRecord record = (ExtLogRecord) records.get(0);
        assertThat(record.getMessage()).isEqualTo("Los ... son muy ... y mucho ...");
        assertThat(record.getMdc(MDCHelper.INSTANCE_ID)).isEqualTo(EXPECTED_INSTANCE_ID);
        assertThat(record.getMdc(MDCHelper.PROCESS_ID)).isEqualTo(EXPECTED_PROCESS_ID);
        assertThat(record.getMdc(MDCHelper.BUSINESS_KEY)).isEqualTo(EXPECTED_BUSINESS_KEY);
        assertThat(record.getMdc(MDCHelper.PROCESS_ID)).isEqualTo(EXPECTED_PROCESS_ID);
        assertThat(record.getMdc(MDCHelper.PARENT_INSTANCE_ID)).isEqualTo(EXPECTED_PARENT_INSTANCE_ID);
        assertThat(record.getMdc(MDCHelper.ROOT_INSTANCE_ID)).isEqualTo(EXPECTED_ROOT_INSTANCE_ID);
        assertThat(record.getMdc(MDCHelper.ROOT_PROCESS_ID)).isEqualTo(EXPECTED_ROOT_ID);
        assertThat(record.getMdc(MDCHelper.PROCESS_NAME)).isEqualTo(EXPECTED_NAME);
        assertThat(record.getMdc(MDCHelper.PROCESS_VERSION)).isEqualTo(EXPECTED_VERSION);
        assertThat(record.getMdc(MDCHelper.DESCRIPTION)).isEqualTo(EXPECTED_DESCRIPTION);
        assertThat(record.getMdc(MDCHelper.DEPLOYMENT_ID)).isEqualTo(EXPECTED_DEPLOYMENT_ID);
        assertThat(record.getMdc("Master")).isEqualTo("Javierito");
    }
}
