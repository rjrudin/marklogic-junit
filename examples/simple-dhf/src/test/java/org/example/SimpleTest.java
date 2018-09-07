package org.example;

import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;
import com.marklogic.junit5.dhf.AbstractDataHubTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SimpleTest extends AbstractDataHubTest {

	/**
	 * Simple test to verify that a connection can be made to MarkLogic.
	 */
	@Test
	public void verifyDatabaseClientCanBeConstructed() {
		assertNotNull(getDatabaseClient());
	}

	/**
	 * Simple test to verify that the database is empty when a test method starts. A "BeforeEach" method in the parent
	 * class is expected to clear the database before a test method starts.
	 */
	@Test
	public void testDatabaseShouldBeClearedWhenTestStarts() {
		QueryManager queryManager = getDatabaseClient().newQueryManager();
		StringQueryDefinition def = queryManager.newStringDefinition("final-entity-options");
		SearchHandle searchHandle = queryManager.search(def, new SearchHandle());
		assertEquals(0, searchHandle.getTotalResults());
	}
}
