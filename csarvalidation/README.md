CSAR Validation
===============
Validates CSAR based on
* ETSI SOL004 & SOL001 specification
* ONAP VNFREQS & PNFREQS

Every validation aspect is modeled as one test cases using Open Command Specification (OCS) 1.o
and by using Open CLI Platform (OCLIP), those test cases are executed similar to running commands

SOL004 specification is implemented in org.onap.cvc.csar.CSARArchive class by supporting both the
options one with TOSCA-meta and another one without TOSCA-meta. This class could be used as SDK for
parsing the given CSAR.

Every VNFREQS & PNFREQS is implemented as independent test case and following guidelines provide
required steps to add test cases.

How to add new test cases?
--------------------------
Assume that we want to address the VNFREQS  R02454 as one test case.

1. Create new OCS yaml vtp-validate-csar-r02454.yaml under src/main/resources/open-cli-schema folder

NOTE:
Name of the file should be always in the form of vtp-validate-csar-<VNFREWQS-Number>.yaml
Inside this YAML, add name as csar-validate-<VNFREWQS-Number>.
Remaining section would be same as other existing OCS YAML.

2. Add corresponding implementation class under src/main/java/org/onap/cvc/csar/cc/VTPValidateCSARR02454.java

NOTE:
Add @OnapCommandSchema(schema = "vtp-validate-csar-r02454.yaml") annotation to the class, where the schema will
have OCS YAML file name

3. Add required CSARError inside this class and set unique error code using CSARError::setCode() method

4. Implement the run() method in this class by using the below code snippet

    protected void run() throws OnapCommandException {
        //Read the input arguments
        String path = (String) getParametersMap().get("csar").getValue();
        List<CSARError> errors = new ArrayList<>();
        //execute
        try {
            CSARArchive csar = new CSARArchive();
            csar.init(path);
            csar.parse();

           // *********** ADD REQUIRED VALIDATION ************

            csar.cleanup();
        } catch (Exception e) {
            LOG.error("R-40293: ", e);
            throw new OnapCommandExecutionFailed(e.getMessage());
        }

        this.getResult().setOutput(errors);

        //set the result
        for (CSARError e: errors) {
            this.getResult().getRecordsMap().get("code").getValues().add(e.getCode());
            this.getResult().getRecordsMap().get("message").getValues().add(e.getMessage());
            this.getResult().getRecordsMap().get("file").getValues().add(e.getFile());
            this.getResult().getRecordsMap().get("line-no").getValues().add(Integer.toString(e.getLineNumber()));
        }
   }

5. Add the new class into src/main/resources/META-INF/services/org.onap.cli.fw.cmd.OnapCommand file

6. Run the test cases at src/test/java/org/onap/cvc/csar/CsarValidatorTest and verify that it picked up the new test cases.

How to configure vnfreqs.properties
-----------------------------------

1. To enable the given vnfreqs, edit vnfreqs.enabled with required VNFREQS number

2. To ignore certian errors, use errors.ignored.

How to run CSAR validation
--------------------------
Follow the setups given below to run as csar-validate command.

<u><b>Warning !!!</b>
Be default, during project building, documentation generation is being performed 
(see point "Documentation generation" for more details).</u>
In order to disable this process, add parameter `-DskipDocsGeneration` to mvn command, example:
`mvn clean package -DskipDocsGeneration`
  

1. Install OCLIP (`wget -O - https://raw.githubusercontent.com/onap/cli/master/deployment/zip/installer/install-latest.sh | sh`)

2. Run `mvn clean install` on this project, and copy the target/validation-csar-x.y.z.jar in to $OPEN_CLI_HOME/lib

3. Run `oclip --product onap-vtp csar-validate --csar <CSAR path>`

Documentation generation
--------------------------
During project build, automated generation of tables, containing supported rules, is being performed.
Created tables are being saved in target directory, `target/generated-docs/{current_release}`.
This generation is being performed by `exec-maven-plugin`, defined in pom.xml.     
In order to generate tables python3 and pip3 are required.

Contact
-------
Kanagaraj.Manickam@huawei.com
