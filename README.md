## Easy JUnit testing with MarkLogic

Want to write JUnit tests that verify the behavior of endpoints in [MarkLogic](https://www.marklogic.com/), 
including applications using the [Data Hub Framework](https://marklogic.github.io/marklogic-data-hub/)? 
This library makes that as simple as possible by providing the following support:

1. Connect to MarkLogic with the [MarkLogic Java Client](https://developer.marklogic.com/products/java) by reusing
configuration you've already defined in your project
1. Clear your test database before a test run so it always runs in a known state
1. Easily read and make assertions on JSON and XML documents, including support for XPath-based assertions
1. And for Data Hub Framework users - run harmonize flows against your staging database and easily verify results in a test database that mirrors your final database

## Getting started on a Data Hub Framework project

Assuming you have a Data Hub Framework (DHF) project [setup already](https://marklogic.github.io/marklogic-data-hub/project/gradle/) 
and you're using Gradle, first add this project's library to the list of dependencies in your build.gradle file:

    dependencies {
      testCompile "com.marklogic:marklogic-junit:0.1.0"
    } 

Next, let's assume you have a harmonize flow.

## Getting started on an ml-gradle project

