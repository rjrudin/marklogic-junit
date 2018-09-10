This project shows a bare minimum setup for writing JUnit tests with marklogic-junit against a Data Hub Framework (DHF)
application. The data is healthcare data borrowed from the [DHF healthcare example project](https://github.com/marklogic/marklogic-data-hub/tree/master/examples/healthcare). 

## Trying the project out locally

To try this project out locally, assuming you've cloned this repository, just run the following steps:

First, deploy the application, including the additional test resources (a database and an app server) - this uses 
the Gradle wrapper, though you can use your own version of Gradle, as long as it's 3.4 or higher:

    ./gradlew mlDeploy hubDeployTestResources hubLoadTestModules

Then ingest the sample data into your staging database:

    ./gradlew ingestNppes

And then run the tests:

    ./gradlew test

You should see output ending with "BUILD SUCCESSFUL", indicating that all of the tests passed successfully.


## Using marklogic-junit in your own DHF project

Read through each of the following steps to use marklogic-junit in your own project. 

### Configure build.gradle

The following 2 dependencies are needed to write JUnit tests using marklogic-junit:

    testCompile "com.marklogic:marklogic-data-hub:3.0.0"
    testCompile "com.marklogic:marklogic-junit:0.10.0"
    
In addition, the build.gradle file contains a set of tasks and classes for deploying test resources to a DHF application.
These tasks and classes [can be copied from this gist](https://gist.github.com/rjrudin/ce347cd657b3768332c17641fdb12907). 

Finally, if you're using Gradle 4.5 or older (if you're using the Gradle wrapper that DHF projects include, then you're
most likely using 3.4), you have to add a few more items to build.gradle as [documented here](https://www.petrikainulainen.net/programming/testing/junit-5-tutorial-running-unit-tests-with-gradle/).

First, you need the following block at the top of your build.gradle file (before the plugins block):

    buildscript {
    	repositories {
    		mavenCentral()
    	}
    	dependencies {
    		classpath 'org.junit.platform:junit-platform-gradle-plugin:1.2.0'
    	}
    }

Then you need to add this plugin (after the plugins block):

    apply plugin: "org.junit.platform.gradle.plugin"

And finally, add this dependency:

    testRuntime "org.junit.jupiter:junit-jupiter-engine:5.3.0"

This will allow for JUnit 5 tests to be picked up by Gradle's "test" task. As noted in the article above, if you're 
using Gradle 4.6 or later, then support for JUnit 5 is already included in the "test" task. 

### Configure gradle.properties

The custom tasks for deploying test resources depend on the following properties in gradle.properties:

    mlTestDbName=data-hub-TEST
    mlTestDbFilename=final-database.json
    mlTestServerName=data-hub-TEST
    mlTestServerFilename=final-server.json
    mlTestPort=8315

You can specify any name for the test database and server, as well as the port. The filenames however should be either "final-database.json" 
and "final-server.json" (the expected values) or "staging-database.json" and "staging-server.json" (unusual, as it's 
unlikely that you want to run tests against data in your ingest database). 

### Add a Logback configuration file for logging

If you aren't already configuring Logback for logging purposes, then consider copying the src/test/resources/logback.xml 
file in this project into the same location in your own project. This step isn't required, but it's typically useful to 
log some information in certain tests.

### Setup your test resources

Run the following Gradle tasks to setup your test database and server and then load all of your modules so that any 
search options are available via your test server:

    ./gradlew hubDeployTestResources hubLoadTestModules

Assuming mlTestPort has a value of 8315, you can go to http://localhost:8315/v1/search to search your new test database
(there won't be any data in it yet).

### Populate the staging database

A typical testing approach for DHF involves populating the staging database and then running harmonize flows that write
data to your test database instead of your final database. In this example project, run the following task to 
populate your staging database with 4 JSON documents:

    ./gradlew ingestNppes

### Create a test class and run it

Now that you've properly configured your Gradle files and ingested data into your staging dataase, you can easily 
create test classes that can run harmonize flows and make assertions on the results. 

The class HarmonizeNppesTest under src/test/java/org/example shows a very basic test that:

1. Runs the harmonize flow named "nppes" for the "Patients" entity
1. Uses a MarkLogic QueryManager to query the data using the final-entity-options options

You can run this test via an IDE, or just run the following task:

    ./gradlew test
