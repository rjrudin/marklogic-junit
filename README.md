## Easy JUnit 5 testing with MarkLogic

Want to write JUnit 5 tests that verify the behavior of endpoints in [MarkLogic](https://www.marklogic.com/), 
including applications using the [Data Hub Framework](https://marklogic.github.io/marklogic-data-hub/)? 
This library makes that as simple as possible by providing the following support:

1. Connect to MarkLogic with the [MarkLogic Java Client](https://developer.marklogic.com/products/java) by reusing
configuration you've already defined in your project
1. Clear your test database before a test run so it always runs in a known state
1. Easily read and make assertions on JSON and XML documents, including support for XPath-based assertions
1. And for Data Hub Framework users - run harmonize flows against your staging database and easily verify results in a test database that mirrors your final database

Here's a simple example of a JUnit test that runs a harmonize flow and then makes assertions on some of the documents that 
were generated:

    @Test
    public void verifyMyHarmonizeFlow() {
      runHarmonizeFlow("MyEntity", "My Harmonize Flow");
      
      assertCollectionSize("Expecting 100 new entities as a result of running the harmonize flow", "my-entities", 100);
      
      // Read and verify the contents of a JSON document using standard Jackson and JUnit APIs
      JsonNode json = readJsonDocument("/entity/1.json");
      assertEquals("SomeValue", json.get("someProperty").asText());
      
      // Or read and verify the contents of an XML document using XPath and JUnit      
      XmlNode xml = readXmlDocument("/entity/2.xml");
      xml.assertElementValue("/my:document/my:element", "some value");
      xml.assertElementCount("Should have gotten two of these", "/my:document/other:element", 2);
      
      // Can easily make assertions on permissions
      readDocumentPermissions("/entity/1.json")
        .assertReadPermissionExists("rest-reader")
        .assertUpdatePermissionExists("rest-writer");
      
      // And on properties too
      readDocumentProperties("/entity/1.json").assertElementExists("/prop:properties/prop:last-modified[. != '']");
    }

## Getting started on a Data Hub Framework project

If you're working on a Data Hub Framework (DHF) project and you're like to start writing JUnit tests to verify your
harmonize flows and other application features, then check out [the DHF sample project](https://github.com/rjrudin/marklogic-junit/tree/master/examples/simple-dhf) to
see a working example with instructions on how to get started.

## Getting started on an ml-gradle project

If you'd like to use marklogic-junit on a regular ml-gradle project (not a DHF project), then 
start with [the ml-gradle sample project](https://github.com/rjrudin/marklogic-junit/tree/master/examples/simple-ml-gradle) 
to see a working example with instructions on how to get started. 
