This project shows an example of using both marklogic-junit and marklogic-unit-test within a 
Data Hub Framework version 4 project. 

To try this out locally, run the following:

    ./gradlew mlDeploy hubDeployTestResources

There are two marklogic-unit-test test modules under ./src/main/ml-modules/root/test. You can run these 
via Gradle:

    ./gradlew test

Or import this project into your favorite IDE and execute "RunDataHubUnitTestsTest". Each test module 
will be executed as a separate JUnit test. 
