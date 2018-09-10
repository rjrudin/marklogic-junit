package com.marklogic.junit5.spring;

import com.marklogic.client.ext.helper.DatabaseClientProvider;
import com.marklogic.client.ext.helper.LoggingObject;
import com.marklogic.test.unit.TestManager;
import com.marklogic.test.unit.TestModule;
import com.marklogic.test.unit.TestResult;
import com.marklogic.test.unit.TestSuiteResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Stream;

/**
 * This is a JUnit 5 parameterized test that invokes every test module defined by the REST endpoint provided by the
 * ml-unit-test framework - https://github.com/marklogic-community/ml-unit-test . This class is abstract so that it
 * is not run when executing tests for the marklogic-junit project - it is instead expected to be extended in a project
 * that depends on marklogic-junit.
 *
 * To make use of this in your own project, simply create a class that extends this in your src/test/java directory (
 * or whatever you store the source of test classes) so that it'll be executed by your IDE / Maven / Gradle / etc.
 */
@TestExecutionListeners(value = {MlUnitTestListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public abstract class MlUnitTestsTest extends AbstractSpringMarkLogicTest {

	static TestManager testManager;

	@ParameterizedTest
	@ArgumentsSource(TestModuleProvider.class)
	public void test(TestModule testModule) {
		TestSuiteResult result = testManager.run(testModule);
		for (TestResult testResult : result.getTestResults()) {
			String failureXml = testResult.getFailureXml();
			if (failureXml != null) {
				Assertions.fail(String.format("Test %s in suite %s failed, cause: %s", testResult.getName(), testModule.getSuite(), failureXml));
			}
		}
	}
}

/**
 * Spring TestExecutionListener implementation that sets MlUnitTest.testManager at a key point in time - after the
 * Spring container has been created, but before JUnit 5 instantiates the class defined by an ArgumentsSource
 * annotation. This then provides a mechanism for the TestModuleProvider below to use an instance of TestManager, which
 * depends on a DatabaseClient, which is provided via the Spring container.
 */
class MlUnitTestListener extends LoggingObject implements TestExecutionListener {

	@Override
	public void beforeTestClass(TestContext testContext) {
		DatabaseClientProvider databaseClientProvider = testContext.getApplicationContext().getBean(DatabaseClientProvider.class);
		MlUnitTestsTest.testManager = new TestManager(databaseClientProvider.getDatabaseClient());
		logger.info("Instantiated TestManager");
	}
}

/**
 * JUnit 5 ArgumentsProvider that depends on the MlUnitTest.testManager field having been set already. If set, a Stream
 * of TestModule instances is returned, one for each ml-unit-test test.
 */
class TestModuleProvider extends LoggingObject implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Assert.notNull(MlUnitTestsTest.testManager, "A static TestManager must be set");
		try {
			List<TestModule> testModules = MlUnitTestsTest.testManager.list();
			return Stream.of(testModules.toArray(new TestModule[]{})).map(Arguments::of);
		} catch (Exception ex) {
			logger.error("Could not obtain a list of ml-unit-test modules; " +
				"please verify that the ml-unit-test library has been properly loaded and that /v1/resources/ml-unit-test is accessible");
			return Stream.of();
		}
	}
}
