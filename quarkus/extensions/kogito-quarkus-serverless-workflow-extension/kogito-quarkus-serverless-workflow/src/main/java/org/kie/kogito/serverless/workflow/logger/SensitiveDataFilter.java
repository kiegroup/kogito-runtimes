package org.kie.kogito.serverless.workflow.logger;

import java.util.Set;
import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.logging.LoggingFilter;

@LoggingFilter(name = SensitiveDataFilter.FILTER_NAME)
public class SensitiveDataFilter implements Filter {

    static final String FILTER_NAME = "sensitive-data-filter";
    private static final String CONFIG_PREFIX = "org.sonataflow.logger." + FILTER_NAME + ".";
    private static final String DEFAULT_MASK = "***MASKED***";

    private final Set<Pattern> patterns;
    private final String mask;

    public SensitiveDataFilter(@ConfigProperty(name = CONFIG_PREFIX + "patterns", defaultValue = "(Authorization\\s*[:=]\\s*)(Bearer\\s+)?[A-Za-z0-9\\-_\\.]+" + ","
            + "(X-Authorization[A-Za-z\\-]*\\s*[:=]\\s*)(Bearer\\s+)?[A-Za-z0-9\\-_\\.]+" + ","
            + "eyJ[A-Za-z0-9\\-_]+\\.eyJ[A-Za-z0-9\\-_]+\\.[A-Za-z0-9\\-_]+" + "," +
            "gh[os]_[A-Za-z0-9]{20,}" + "," +
            "(\"[^\"]*[Tt]oken\"\\s*:\\s*\")(gh[os]_[A-Za-z0-9]+)(\")") Set<String> patterns, @ConfigProperty(name = CONFIG_PREFIX + "mask", defaultValue = DEFAULT_MASK) String mask) {
        this.patterns = patterns.stream().map(Pattern::compile).collect(Collectors.toSet());
        this.mask = mask;
    }

    @Override
    public boolean isLoggable(LogRecord record) {
        if (record.getMessage() != null) {
            record.setMessage(maskSensitiveData(record.getMessage()));
        }
        Object[] params = record.getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof String param) {
                    params[i] = maskSensitiveData(param);
                }
            }
        }
        return true;
    }

    private String maskSensitiveData(String message) {
        String result = message;
        for (Pattern pattern : patterns) {
            result = pattern.matcher(result).replaceAll(match -> {
                if (match.groupCount() == 0) {
                    return mask;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= match.groupCount(); i++) {
                    if (match.group(i) != null) {
                        sb.append(i % 2 != 0 ? match.group(i) : mask);
                    }
                }
                return sb.length() > 0 ? sb.toString() : mask;
            });
        }
        return result;
    }
}
