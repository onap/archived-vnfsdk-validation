PMDictionary Validation
=======================
This module can be used as a library to validate pmdictionary against the schema (schema is the first document in the file).

How to use PMDictionary validation library
------------------------------------------
VNF-SDK validation library (pmdictionaryvalidation) should be used to validate the PM_Dictionary file.
    Below dependency should be added to the required modules in your project.

        <dependency>
            <groupId>org.onap.vnfsdk.validation</groupId>
            <artifactId>validation-pmdictionary</artifactId>
            <version>version</version>
        </dependency>

How to validate PMDictionary
--------------------------
1.Validate PMDictionary from a path to the file.

    new YamlFileValidator().validateYamlFileWithSchema(pathToFile)

2.Validate PMDictionary file from the byte array.

    new YamlFileValidator().validateYamlFileWithSchema(fileContentAsByteArray)

Above methods return list of YamlDocumentValidationError(empty list for no errors) or throw YamlProcessingException/YAMLException when something goes wrong.
