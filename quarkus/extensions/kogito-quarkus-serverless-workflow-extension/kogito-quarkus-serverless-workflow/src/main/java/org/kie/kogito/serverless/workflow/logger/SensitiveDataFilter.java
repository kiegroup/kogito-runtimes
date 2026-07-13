package org.kie.kogito.serverless.workflow.logger;

import java.util.Set;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logmanager.filters.AllFilter;
import org.jboss.logmanager.filters.SubstituteFilter;

import io.quarkus.logging.LoggingFilter;

@LoggingFilter(name = SensitiveDataFilter.FILTER_NAME)
public class SensitiveDataFilter implements Filter {

    static final String FILTER_NAME = "sensitive-data-filter";
    static final String DEFAULT_PATTERNS = "(Authorization\\s*[:=]\\s*)(Bearer\\s+)?[A-Za-z0-9\\-_\\.]+" + ","
            + "(X-Authorization[A-Za-z\\-]*\\s*[:=]\\s*)(Bearer\\s+)?[A-Za-z0-9\\-_\\.]+" + ","
            + "eyJ[A-Za-z0-9\\-_]+\\.eyJ[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+" + "," +
            "(\"[^\"]*[Tt]oken\"\\s*:\\s*\")(gh[os]_[A-Za-z0-9]+)(\")" + ","
            + "gh[os]_[A-Za-z0-9]{20\\,}";
    private static final String CONFIG_PREFIX = "org.sonataflow.logger." + FILTER_NAME + ".";
    private static final String DEFAULT_MASK = "***MASKED***";

    private final AllFilter filter;

    public SensitiveDataFilter(@ConfigProperty(name = CONFIG_PREFIX + "patterns", defaultValue = DEFAULT_PATTERNS) Set<String> patterns,
            @ConfigProperty(name = CONFIG_PREFIX + "mask", defaultValue = DEFAULT_MASK) String mask) {
        this.filter = new AllFilter(patterns.stream().map(p -> new SubstituteFilter(p, mask, true)).toArray(Filter[]::new));
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        return filter.isLoggable(record);
    }
}
