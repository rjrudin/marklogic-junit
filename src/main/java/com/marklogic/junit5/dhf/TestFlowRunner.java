package com.marklogic.junit5.dhf;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.datamovement.JobTicket;
import com.marklogic.client.ext.helper.LoggingObject;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.hub.FlowManager;
import com.marklogic.hub.HubConfig;
import com.marklogic.hub.HubConfigBuilder;
import com.marklogic.hub.flow.Flow;
import com.marklogic.hub.flow.FlowRunner;
import com.marklogic.hub.flow.FlowType;
import com.marklogic.hub.job.JobStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class for simplifying the running of a harmonize flow within a test environment.
 */
public class TestFlowRunner extends LoggingObject {

	private FlowManager flowManager;
	private DatabaseClient sourceClient;
	private DatabaseClient jobsClient;
	private FlowRunnerCallback flowRunnerCallback;
	private String testDatabaseName;
	private boolean failOnHarmonizeFlowError = true;

	public TestFlowRunner(DatabaseClient sourceClient, String testDatabaseName) {
		this(sourceClient, testDatabaseName, "local");
	}

	/**
	 * @param sourceClient     specifies the source database, which could be your staging or final database
	 * @param testDatabaseName
	 * @param environmentName
	 */
	public TestFlowRunner(DatabaseClient sourceClient, String testDatabaseName, String environmentName) {
		this.sourceClient = sourceClient;
		this.testDatabaseName = testDatabaseName;
		initializeFlowManager(environmentName);
	}

	/**
	 * Constructs a FlowRunner to run the flow associated with the given entity and flow names.
	 * <p>
	 * If a FlowRunnerCallback has been set, it will be invoked after the FlowRunner is constructed but before the
	 * Flow is run, providing a chance for a client to further configure the FlowRunner.
	 *
	 * @param entityName
	 * @param flowName
	 * @param optionKeysAndValues
	 * @return
	 */
	public JobTicket runHarmonizeFlow(String entityName, String flowName, String... optionKeysAndValues) {
		Flow harmonizeFlow = flowManager.getFlow(entityName, flowName, FlowType.HARMONIZE);

		FlowRunner flowRunner = flowManager.newFlowRunner()
			.withFlow(harmonizeFlow)
			.withOptions(convertStringsToMap(optionKeysAndValues))
			.withStopOnFailure(true)
			.withSourceClient(sourceClient)
			.withDestinationDatabase(testDatabaseName);

		if (flowRunnerCallback != null) {
			flowRunnerCallback.beforeFlowIsRun(flowRunner, entityName, flowName);
		}

		long start = System.currentTimeMillis();
		JobTicket jobTicket = flowRunner.run();
		flowRunner.awaitCompletion();
		logger.info("Finished flow " + flowName + "; duration: " + (System.currentTimeMillis() - start));

		if (failOnHarmonizeFlowError) {
			verifyHarmonizeFlowSucceeded(jobTicket);
		}

		return jobTicket;
	}

	protected void initializeFlowManager(String environmentName) {
		if (logger.isInfoEnabled()) {
			logger.info("Initializing HubConfig and FlowManager for environment name: " + environmentName);
		}
		HubConfig dataHubConfig = HubConfigBuilder.newHubConfigBuilder(".")
			.withPropertiesFromEnvironment(environmentName)
			.build();
		flowManager = FlowManager.create(dataHubConfig);
		if (logger.isInfoEnabled()) {
			logger.info("Initialized HubConfig and FlowManager for environment name: " + environmentName);
		}
	}

	/**
	 * Simple convenience method for converting a String array into a map.
	 *
	 * @param optionKeysAndValues
	 * @return
	 */
	protected Map<String, Object> convertStringsToMap(String... optionKeysAndValues) {
		Map<String, Object> options = new HashMap<>();
		if (optionKeysAndValues != null) {
			for (int i = 0; i < optionKeysAndValues.length; i += 2) {
				options.put(optionKeysAndValues[i], optionKeysAndValues[i + 1]);
			}
		}
		return options;
	}

	/**
	 * Reaches into the jobs database to find the job report associated with the given JobTicket. If it's determined
	 * that the job failed, a RuntimeException is thrown.
	 *
	 * @param jobTicket
	 */
	protected void verifyHarmonizeFlowSucceeded(JobTicket jobTicket) {
		if (jobsClient == null) {
			logger.warn("Unable to verify that the harmonize flow succeeded because no DatabaseClient is available for " +
				"connecting to the jobs database; use setJobsClient to provide one.");
		}

		final String jobsUri = buildJobUri(jobTicket);
		JsonNode json = jobsClient.newJSONDocumentManager().read(jobsUri, new JacksonHandle()).get();
		JobStatus status = JobStatus.valueOf(json.get("status").asText());
		if (JobStatus.FINISHED_WITH_ERRORS.equals(status) || JobStatus.FAILED.equals(status) || JobStatus.STOP_ON_ERROR.equals(status)) {
			throw new RuntimeException("Harmonize flow failed; contents of job report: " + json);
		}
	}

	/**
	 * Knows how a URI is constructed for a JobTicket.
	 *
	 * @param jobTicket
	 * @return
	 */
	protected String buildJobUri(JobTicket jobTicket) {
		return "/jobs/" + jobTicket.getJobId() + ".json";
	}

	public FlowManager getFlowManager() {
		return flowManager;
	}

	public void setSourceClient(DatabaseClient sourceClient) {
		this.sourceClient = sourceClient;
	}

	public void setTestDatabaseName(String testDatabaseName) {
		this.testDatabaseName = testDatabaseName;
	}

	public void setJobsClient(DatabaseClient jobsClient) {
		this.jobsClient = jobsClient;
	}

	public void setFailOnHarmonizeFlowError(boolean failOnHarmonizeFlowError) {
		this.failOnHarmonizeFlowError = failOnHarmonizeFlowError;
	}

	public void setFlowRunnerCallback(FlowRunnerCallback flowRunnerCallback) {
		this.flowRunnerCallback = flowRunnerCallback;
	}
}
