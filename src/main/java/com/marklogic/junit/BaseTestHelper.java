package com.marklogic.junit;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.extensions.ResourceManager;
import com.marklogic.client.ext.helper.DatabaseClientProvider;
import com.marklogic.client.ext.helper.ResourceExtension;
import com.marklogic.xcc.template.XccTemplate;

/**
 * Provides convenience methods for instantiating new TestHelper and ResourceManager implementations. Also extends
 * XmlHelper so that this can be used as a base class for test classes.
 */
public class BaseTestHelper extends XmlHelper implements TestHelper {

    private DatabaseClientProvider clientProvider;
    private XccTemplate xccTemplate;
    private NamespaceProvider namespaceProvider;

    protected BaseTestHelper() {
        namespaceProvider = new MarkLogicNamespaceProvider();
    }

    protected <T extends TestHelper> T newHelper(Class<T> clazz) {
        T helper = null;
        try {
            helper = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        helper.setDatabaseClientProvider(getClientProvider());
        if (xccTemplate != null) {
            helper.setXccTemplate(xccTemplate);
        }
        helper.setNamespaceProvider(getNamespaceProvider());
        return helper;
    }

    protected <T extends ResourceManager> T newResource(Class<T> clazz) {
        T resource = null;
        try {
            resource = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (resource instanceof ResourceExtension) {
            ((ResourceExtension) resource).setDatabaseClient(clientProvider.getDatabaseClient());
        }
        return resource;
    }

    /**
     * Convenience method for getting the permissions for a document as a fragment.
     * 
     * @param uri
     * @param t
     * @return
     */
    protected PermissionsFragment getDocumentPermissions(String uri, XccTemplate t) {
        String xquery = format("for $perm in xdmp:document-get-permissions('%s') ", uri);
        xquery += "return element {fn:node-name($perm)} {";
        xquery += "  $perm/*,";
        xquery += "  xdmp:eval('import module namespace sec=\"http://marklogic.com/xdmp/security\" at \"/MarkLogic/security.xqy\"; sec:get-role-names(' || $perm/sec:role-id/fn:string() || ')', (), ";
        xquery += "    <options xmlns='xdmp:eval'><database>{xdmp:security-database()}</database></options>) }";
        return new PermissionsFragment(parse("<permissions>" + t.executeAdhocQuery(xquery) + "</permissions>"));
    }

    /**
     * Convenience method for getting the properties for a document as a fragment.
     * 
     * @param uri
     * @param t
     * @return
     */
    protected Fragment getDocumentProperties(String uri, XccTemplate t) {
        return new Fragment(t.executeAdhocQuery(format("xdmp:document-properties('%s')", uri)));
    }

    @Override
    public void setDatabaseClientProvider(DatabaseClientProvider provider) {
        this.clientProvider = provider;
    }

    @Override
    public void setXccTemplate(XccTemplate xccTemplate) {
        this.xccTemplate = xccTemplate;
    }

    @Override
    public void setNamespaceProvider(NamespaceProvider namespaceProvider) {
        this.namespaceProvider = namespaceProvider;
    }

    protected NamespaceProvider getNamespaceProvider() {
        return this.namespaceProvider;
    }

    protected DatabaseClient getClient() {
        return clientProvider.getDatabaseClient();
    }

    protected DatabaseClientProvider getClientProvider() {
        return clientProvider;
    }

    protected XccTemplate getXccTemplate() {
        return xccTemplate;
    }
}
