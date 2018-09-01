package com.marklogic.junit.spring;

import com.marklogic.junit.BaseTestHelper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

/**
 * Base class for tests that utilize Spring's support for JUnit. Extends BaseTestHelper so that all of the convenience
 * methods available within that are available to subclasses.
 * <p>
 * Aside from using the Spring plumbing, the main purpose of this class is to clear the test database in some fashion.
 * It defaults to deleting every document in the database, but the exact query that's executed can be specified by
 * overriding getClearDatabaseXquery.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({LoggingTestExecutionListener.class, DependencyInjectionTestExecutionListener.class})
public abstract class AbstractSpringTest extends BaseTestHelper implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Before
	public void deleteDocumentsBeforeTestRuns() {
		getClient().newServerEval().xquery(getClearDatabaseXquery());
	}

	/**
	 * Protected so a subclass can modify this to, e.g., not delete every document.
	 */
	protected String getClearDatabaseXquery() {
		return "cts:uris((), (), cts:and-query(())) ! xdmp:document-delete(.)";
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	protected ApplicationContext getApplicationContext() {
		return applicationContext;
	}
}
