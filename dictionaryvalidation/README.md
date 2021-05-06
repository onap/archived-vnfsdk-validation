Dictionary Validation
=======================
This module can be used as a library to validate dictionary against the schema (schema is the first document in the file).

How to use Dictionary validation library
------------------------------------------
VNF-SDK validation library (dictionaryvalidation) should be used to validate the Dictionary file.
    Below dependency should be added to the required modules in your project.

        <dependency>
            <groupId>org.onap.vnfsdk.validation</groupId>
            <artifactId>validation-dictionary</artifactId>
            <version>version</version>
        </dependency>

How to validate Dictionary
--------------------------
1.Validate Dictionary from a path to the file.

    new YamlContentValidator().validate(pathToFile)

2.Validate Dictionary file from the byte array.

    new YamlContentValidator().validate(fileContentAsByteArray)

Above methods return list of YamlDocumentValidationError(empty list for no errors) or throw YamlProcessingException/YAMLException when something goes wrong.
