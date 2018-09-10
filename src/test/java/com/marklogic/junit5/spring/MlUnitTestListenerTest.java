package com.marklogic.junit5.spring;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifies that MlUnitTestListener correctly sets a TestManager on MlUnitTestsTest. It's unfortunately not very easy to
 * test the rest of MlUnitTestsTest until a full application is stood up for this project for testing.
 */
@TestExecutionListeners(value = TestListener.class, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class MlUnitTestListenerTest extends AbstractSpringMarkLogicTest {

	TestContext testContext;

	@Test
	public void test() {
		assertNull(MlUnitTestsTest.testManager);

		MlUnitTestListener listener = new MlUnitTestListener();
		listener.beforeTestClass(testContext);

		assertNotNull(MlUnitTestsTest.testManager);
	}

}

class TestListener implements TestExecutionListener {

	@Override
	public void prepareTestInstance(TestContext testContext) {
		((MlUnitTestListenerTest) testContext.getTestInstance()).testContext = testContext;
	}
}