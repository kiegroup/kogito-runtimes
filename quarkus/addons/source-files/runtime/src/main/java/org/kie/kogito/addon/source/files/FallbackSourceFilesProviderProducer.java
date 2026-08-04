package org.kie.kogito.addon.source.files;

import org.kie.kogito.source.files.SourceFilesProvider;
import org.kie.kogito.source.files.SourceFilesProviderImpl;

import io.quarkus.arc.DefaultBean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class FallbackSourceFilesProviderProducer {

    @Produces
    @DefaultBean
    @ApplicationScoped
    public SourceFilesProvider fallbackSourceFileProvider() {
        return new SourceFilesProviderImpl();
    }
}
