package com.marklogic.junit5.dhf;

import com.marklogic.hub.flow.FlowRunner;

/**
 * Callback interface for configuring a FlowRunner before a flow is run.
 */
public interface FlowRunnerCallback {

	void beforeFlowIsRun(FlowRunner flowRunner, String entityName, String flowName);

}
