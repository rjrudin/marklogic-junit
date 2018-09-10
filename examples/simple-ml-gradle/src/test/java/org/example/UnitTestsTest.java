package org.example;

import com.marklogic.junit5.spring.MlUnitTestsTest;

/**
 * This class is needed so that when running tests in an IDE / Maven / Gradle / etc, the test runner is able to find
 * a class that knows how to execute ml-unit-test tests. That knowledge is contained in MlUnitTestsTest such that this
 * subclass does not need to do anything beyond extending it. But this subclass must exist so that the test runner
 * can find it.
 */
public class UnitTestsTest extends MlUnitTestsTest {

}
