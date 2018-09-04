package com.marklogic.junit;

import com.marklogic.client.io.DocumentMetadataHandle;
import org.junit.jupiter.api.Assertions;

import java.util.Set;

/**
 * Convenience class for making assertions on the permissions on a document.
 */
public class PermissionsTester {

	private DocumentMetadataHandle.DocumentPermissions documentPermissions;

	public PermissionsTester(DocumentMetadataHandle.DocumentPermissions documentPermissions) {
		this.documentPermissions = documentPermissions;
	}

	public void assertExecutePermissionExists(String message, String role) {
		assertCapabilityExists(message, role, DocumentMetadataHandle.Capability.EXECUTE);
	}

	public void assertInsertPermissionExists(String message, String role) {
		assertCapabilityExists(message, role, DocumentMetadataHandle.Capability.INSERT);
	}

	public void assertNodeUpdatePermissionExists(String message, String role) {
		assertCapabilityExists(message, role, DocumentMetadataHandle.Capability.NODE_UPDATE);
	}

	public void assertReadPermissionExists(String message, String role) {
		assertCapabilityExists(message, role, DocumentMetadataHandle.Capability.READ);
	}

	public void assertUpdatePermissionExists(String message, String role) {
		assertCapabilityExists(message, role, DocumentMetadataHandle.Capability.UPDATE);
	}

	private void assertCapabilityExists(String message, String role, DocumentMetadataHandle.Capability capability) {
		Set<DocumentMetadataHandle.Capability> capabilities = this.documentPermissions.get(role);
		Assertions.assertTrue(capabilities != null && capabilities.contains(capability), message);
	}

	public DocumentMetadataHandle.DocumentPermissions getDocumentPermissions() {
		return documentPermissions;
	}
}
