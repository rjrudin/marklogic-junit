package com.marklogic.junit;

import com.marklogic.client.ext.helper.DatabaseClientProvider;
import com.marklogic.xcc.template.XccTemplate;

/**
 * Interface for objects that help with writing tests.
 * <ol>
 * <li>A DatabaseClientProvider facilities accessing a MarkLogic REST API server via the MarkLogic Java API.</li>
 * <li>An XccTemplate typically connects to an XDBC server that points to the same database as the REST API server. The
 * XccTemplate is useful for performing operations not yet exposed via the REST API server. Note though that with
 * MarkLogic 8 and greater, this is typically not needed because a DatabaseClient can be used to accomplish the
 * same goals via the /v1/eval endpoint.</li>
 * <li>The NamespaceProvider provides an array of JDOM namespaces that can be used in XPath namespaces via JDOM.</li>
 * </ol>
 */
public interface TestHelper {

	void setDatabaseClientProvider(DatabaseClientProvider provider);

	void setXccTemplate(XccTemplate xccTemplate);

	void setNamespaceProvider(NamespaceProvider namespaceProvider);
}
