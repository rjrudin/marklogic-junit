## What is ml-junit?

The ml-junit module is designed to simplify writing JUnit 4+ tests that run against a MarkLogic instance. A typical test 
that uses ml-junit classes follows this pattern:

1. Load some documents into the test database
1. Call one or more HTTP endpoints to manipulate that data
1. Load one or more documents from the test database and make XPath-based assertions on them

Such tests are usually called "integration" tests instead of "unit" tests. A unit test for MarkLogic is typically 
something written in XQuery that invokes other XQuery modules. Supporting those tests is not the intent of ml-junit. 

ml-junit makes heavy use of [Spring's support for JUnit tests](http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#testcontext-framework). You don't need to use Spring - you can write a test that extends BaseTestHelper instead of AbstractSpringTest - but I recommend at least considering Spring's support for JUnit, as it provides a lot of useful features. 

## Getting started with ml-junit

The best way to get started with ml-junit is to setup the sample-project application in 
the ml-gradle repository](https://github.com/rjrudin/ml-gradle) (clone that repository, then cd into sample-project and run "gradle mlDeploy") and 
then examine and run the tests in that project. Start 
with [AbstractSampleProjectTest](https://github.com/rjrudin/ml-gradle/blob/master/examples/sample-project/src/test/java/sample/AbstractSampleProjectTest.java) to see how to create in your 
project a base test class that extends the ml-junit infrastructure. Then read through each of the tests 
to see different ml-junit features in action - [WriteAndReadDocumentTest](https://github.com/rjrudin/ml-gradle/blob/master/examples/sample-project/src/test/java/sample/WriteAndReadDocumentTest.java) is a good one to start with. 
You can of course run the tests via "gradle test", or you can run "gradle eclipse" to setup Eclipse project 
files and then run each test individually within Eclipse.

## Understanding the test hierarchy

To use ml-junit, you'll usually extend AbstractSpringTest, which has a fairly deep class hierarchy. Each level of 
that hierarchy provides a set of features, as described below:

### XmlHelper

1. Extends JUnit's Assert class, thus making all of the commonly used assert* methods available
1. Provides methods for parsing XML into a Fragment object, which uses JDOM2 to provide a bunch of assertion methods using XPath
1. Provides a NamespaceProvider, which provides a set of namespaces and prefixes to be used by Fragment objects
1. Provides some methods for using XMLUnit to assert on XML fragments being equal or similar
1. Provides some methods for common tasks such as loading test files from the classpath

### BaseTestHelper (extends XmlHelper)

1. Provides access to a DatabaseClient, which is the main interface in the ML Java API for talking to a REST API server. 
Note that you don't need to use a DatabaseClient - you can always use something 
like [RestAssured](https://code.google.com/p/rest-assured/) or Spring's RestTemplate instead for 
making HTTP requests to MarkLogic. 
1. Provides an instance of XccTemplate, which provides an easy way of talking to an XDBC server
1. Provides a method for instantiating TestHelper implementations, which typically need a DatabaseClient and an XccTemplate

### AbstractSpringTest (extends BaseTestHelper)

1. Tells JUnit to use Spring's JUnit class runner, which will use other annotations on the class to determine 
how to build a Spring container (very briefly - a Spring container handles instantiating objects and 
wiring together any dependencies between those objects)
1. Obtains a DatabaseClientProvider from the Spring container so that it provide a DatabaseClient to 
TestHelper implementations
1. Assumes that all documents in the database should be deleted before a test is run - this method can 
be overridden to change this behavior

### Abstract(ProjectName)Test (extends AbstractSpringTest)

1. Uses @ContextConfiguration to tell Spring which classes define the objects to be loaded into the Spring container
1. Uses ConfigurerTestExecutionListener to process the @Configurers annotation. **This tells the test framework 
what directories to look at for loading new/modified XQuery modules ** (this is one of the most important things 
that the test framework does for you). 
1. Overrides getNamespaceProvider to return an implementation of NamespaceProvider that most likely 
extends MarkLogicNamespaceProvider and registers additional namespaces that will be used in XPath expressions. 

