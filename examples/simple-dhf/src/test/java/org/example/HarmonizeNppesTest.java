package org.example;

import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.query.FacetResult;
import com.marklogic.client.query.FacetValue;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;
import com.marklogic.junit5.dhf.AbstractDataHubTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Shows an example of running a harmonize flow and then querying /v1/search with the search options associated with
 * the final database.
 */
public class HarmonizeNppesTest extends AbstractDataHubTest {

	/**
	 * This test assumes that the staging database has been populated with the 4 documents created via mlcp.
	 */
	@Test
	public void test() {
		assertCollectionSize("Collection should be empty at the start of the test", "Patients", 0);

		runHarmonizeFlow("Patients", "nppes");

		assertCollectionSize("Collection should now have 4 documents in it", "Patients", 4);

		QueryManager queryManager = getDatabaseClient().newQueryManager();
		StringQueryDefinition def = queryManager.newStringDefinition("final-entity-options");
		SearchHandle searchHandle = queryManager.search(def, new SearchHandle());
		assertEquals(4, searchHandle.getTotalResults(),
			"The harmonize flow should produce 4 documents in the test database, one for each document in the staging database");

		FacetResult cityResult = searchHandle.getFacetResult("City");
		assertEquals(4, cityResult.getFacetValues().length);
		assertEquals("ARCADE", cityResult.getFacetValues()[0].getLabel());
		assertEquals("SPRAKERS", cityResult.getFacetValues()[1].getLabel());
		assertEquals("STUYVESANT", cityResult.getFacetValues()[2].getLabel());
		assertEquals("WEST EDMESTON", cityResult.getFacetValues()[3].getLabel());

		FacetResult stateResult = searchHandle.getFacetResult("State");
		assertEquals(1, stateResult.getFacetValues().length);
		FacetValue facetValue = stateResult.getFacetValues()[0];
		assertEquals("NY", facetValue.getLabel());
		assertEquals(4, facetValue.getCount());
	}

	/**
	 * Verifies that multiple harmonize flows can be run in the same test (i.e. without a DatabaseClient being released
	 * before the second flow is run).
	 */
	@Test
	public void harmonizeTwice() {
		assertCollectionSize("Collection should be empty at the start of the test", "Patients", 0);

		runHarmonizeFlow("Patients", "nppes");
		runHarmonizeFlow("Patients", "nppes");

		assertCollectionSize("Collection still only have 4 documents in it", "Patients", 4);
	}
}
