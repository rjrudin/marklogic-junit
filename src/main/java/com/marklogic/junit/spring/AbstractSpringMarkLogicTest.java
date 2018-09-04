package com.marklogic.junit.spring;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.ext.helper.DatabaseClientProvider;
import com.marklogic.junit.AbstractMarkLogicTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Simple extension of AbstractMarkLogicTest that uses Spring's SpringExtension class to run with JUnit 5, and uses
 * SimpleTestConfig to define how a DatabaseClient is constructed to connect to a test database.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SimpleTestConfig.class})
public abstract class AbstractSpringMarkLogicTest extends AbstractMarkLogicTest {

	@Autowired
	protected DatabaseClientProvider databaseClientProvider;

	@Override
	protected DatabaseClient getDatabaseClient() {
		return databaseClientProvider.getDatabaseClient();
	}

}
