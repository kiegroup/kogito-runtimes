package $Package$;

import org.kie.kogito.process.Process;
import org.kie.kogito.serverless.workflow.models.JsonNodeModel;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@ApplicationScoped
public class AliasProducerQuarkusTemplate {

	@Produces
	@Named("$targetName$")
	Process<JsonNodeModel> workflowAlias(@Named("$sourceName$") Process<JsonNodeModel> process) {
		return process;
	}
	
}
