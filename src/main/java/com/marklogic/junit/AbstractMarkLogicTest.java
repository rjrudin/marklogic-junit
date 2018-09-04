package com.marklogic.junit;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.ext.helper.ClientHelper;
import com.marklogic.client.io.BytesHandle;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

/**
 * Abstract base class for writing JUnit tests that depend on a connection to MarkLogic via a DatabaseClient. Provides
 * support for the following:
 * <ol>
 * <li>Delete all or a subset of documents in the test database before each test method runs</li>
 * <li>Methods for reading XML or a document at a URI into a Fragment object</li>
 * <li>Methods for making common assertions on collections, permissions, and document properties</li>
 * </ol>
 * <p>
 * This class depends on a DatabaseClient, and how that is provided must be defined by the subclass.
 * </p>
 */
public abstract class AbstractMarkLogicTest {

	/**
	 * Subclass must define how a connection is made to (presumably) the test database.
	 *
	 * @return
	 */
	protected abstract DatabaseClient getDatabaseClient();

	/**
	 * Before a test method runs, delete all of the documents in the database that match the query defined by
	 * getQueryForDeletingDocumentsBeforeTestRuns.
	 */
	@BeforeEach
	public void deleteDocumentsBeforeTestRuns() {
		getDatabaseClient().newServerEval().xquery(getQueryForDeletingDocumentsBeforeTestRuns());
	}

	/**
	 * Protected so a subclass can modify this to, e.g., not delete every document.
	 */
	protected String getQueryForDeletingDocumentsBeforeTestRuns() {
		return "cts:uris((), (), cts:and-query(())) ! xdmp:document-delete(.)";
	}

	/**
	 * Used to construct a Fragment with the returned NamespaceProvider associated with it,
	 * thereby making the namespaces available for XPath expressions.
	 *
	 * @return
	 */
	protected NamespaceProvider getNamespaceProvider() {
		return new MarkLogicNamespaceProvider();
	}

	/**
	 * Parse the given XML and return a Fragment for marking assertions on the contents of the XML.
	 *
	 * @param xml
	 * @return
	 */
	protected Fragment parseXml(String xml) {
		return new Fragment(xml, getNamespaceProvider().getNamespaces());
	}

	/**
	 * Read the XML document at the given URI and return a Fragment for making assertions on the contents of the XML.
	 *
	 * @param uri
	 * @param expectedCollections If any are specified, the document is verified to be in each of the given collections
	 * @return
	 */
	protected Fragment readXmlDocument(String uri, String... expectedCollections) {
		String xml = getDatabaseClient().newXMLDocumentManager().read(uri, new StringHandle()).get();
		if (expectedCollections != null) {
			assertInCollections(uri, expectedCollections);
		}
		return new Fragment(uri, xml, getNamespaceProvider().getNamespaces());
	}

	/**
	 * Read the JSON document at the given URI and return a JsonNode.
	 *
	 * @param uri
	 * @param expectedCollections If any are specified, the document is verified to be in each of the given collections
	 * @return
	 */
	protected JsonNode readJsonDocument(String uri, String... expectedCollections) {
		JsonNode json = getDatabaseClient().newJSONDocumentManager().read(uri, new JacksonHandle()).get();
		if (expectedCollections != null) {
			assertInCollections(uri, expectedCollections);
		}
		return json;
	}

	/**
	 * Verify that the document at the given URI is in the given collections.
	 *
	 * @param uri
	 * @param collections
	 */
	protected void assertInCollections(String uri, String... collections) {
		List<String> colls = new ClientHelper(getDatabaseClient()).getCollections(uri);
		for (String c : collections) {
			Assertions.assertTrue(colls.contains(c), String.format("Expected URI %s to be in collection %s", uri, c));
		}
	}

	/**
	 * Verify that the document at the given URI is not in any of the given collections.
	 *
	 * @param message
	 * @param uri
	 * @param collections
	 */
	protected void assertNotInCollections(String uri, String... collections) {
		List<String> colls = new ClientHelper(getDatabaseClient()).getCollections(uri);
		for (String c : collections) {
			Assertions.assertFalse(colls.contains(c), String.format("Expected URI %s to not be in collection %s", uri, c));
		}
	}

	/**
	 * Verify the size of the given collection.
	 *
	 * @param message
	 * @param collection
	 * @param size
	 */
	protected void assertCollectionSize(String message, String collection, int size) {
		Assertions.assertEquals(size, new ClientHelper(getDatabaseClient()).getCollectionSize(collection), message);
	}

	/**
	 * Get all URIs in the given collection, verifying the count at the same time.
	 *
	 * @param collectionName
	 * @param expectedCount
	 * @return
	 */
	protected List<String> getUrisInCollection(String collectionName, int expectedCount) {
		List<String> uris = new ClientHelper(getDatabaseClient()).getUrisInCollection(collectionName, expectedCount + 1);
		Assertions.assertEquals(expectedCount, uris.size(), String.format("Expected %d uris in collection %s", expectedCount, collectionName));
		return uris;
	}

	/**
	 * Returns a PermissionsTester object based on the permissions on the document at the given URI, which provides
	 * convenience methods for asserting on the permissions on a document.
	 *
	 * @param uri
	 * @param t
	 * @return
	 */
	protected PermissionsTester getDocumentPermissions(String uri, DatabaseClient client) {
		DocumentMetadataHandle metadata = new DocumentMetadataHandle();
		client.newDocumentManager().read(uri, metadata, new BytesHandle());
		return new PermissionsTester(metadata.getPermissions());
	}

	/**
	 * Convenience method for getting the properties for a document as a fragment.
	 *
	 * @param uri
	 * @param client
	 * @return
	 */
	protected Fragment getDocumentProperties(String uri, DatabaseClient client) {
		return new Fragment(client.newServerEval().xquery(String.format("xdmp:document-properties('%s')", uri)).evalAs(String.class));
	}

}
