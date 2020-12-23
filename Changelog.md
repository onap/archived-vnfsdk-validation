# Changelog
All notable changes to this project will be documented in this file.


## [1.2.5]

### Added
- Added rule R972082 to enabled PNF requirements.
    - https://jira.onap.org/browse/VNFSDK-585

### Fixed
- Fixed package integrity issue with non mano arifacts.
    - https://jira.onap.org/browse/VNFSDK-581
- Fixed VNF/PNF package integrity issue with CMS signature not containing certificate.
    - https://jira.onap.org/browse/VNFSDK-582
- Fixed bug that was showing errors during validation of CSAR,
    when any other non_mano_artifact_set than onap_pnf_sw_information was present in manifest file.
    - https://jira.onap.org/browse/VNFSDK-585

## [1.2.6]

### Added
- Added file extension validation of file related in onap_pnf_sw_information artifact set.
    - https://jira.onap.org/browse/VNFSDK-585

### Fixed
- Fixed bug that was generating invalid report when user run validation with all rules and single validation fails.
    - https://jira.onap.org/browse/VNFSDK-586


## [1.2.7]

### Fixed
- Fixed bug that was causing problem with loading rules properties.
    - https://jira.onap.org/browse/VNFSDK-587
- Fixed package security SOL004 Option 1 make rule less restrictive as this rule is not implemented in SDC Onboarding
    - https://jira.onap.org/browse/VNFSDK-595
    
## [1.2.8]

## Fixed
- Fixed VNFSDK doesn't check if all files in package are listed in manifest file
    - https://jira.onap.org/browse/VNFSDK-583

## [1.2.9]

### Added
- Added rule R972082 to validate PM_Dictionary using schema.
    - https://jira.onap.org/browse/VNFSDK-594
    
## Fixed
- Fixed rule R01123 that was reporting all files in ZIP as not present in manifest
    - https://jira.onap.org/browse/VNFSDK-583
    
    
## [1.2.10]

### Added
- Added parameters list validation to PM Dictionary .
    - https://jira.onap.org/browse/VNFSDK-594


## [1.2.11]

## Fixed
- Fixed rule R816745 that wasn't sending all exceptions connected with YAML parsing as validation error
    - https://jira.onap.org/browse/VNFSDK-644

## [1.2.12]

## Fixed
- Fixed rule R816745 that was searching for the path to PM_Dictionary in manifest file under name source,
  instead of Source (starting with a capital letter). 
  Now  both versions (source and Source) are accepted by this rule.
    - https://jira.onap.org/browse/VNFSDK-645  
- Fixed commons-codec vulnerability
    - https://jira.onap.org/browse/VNFSDK-584

## Added
- Added non-vulnerable log4j version
    - https://jira.onap.org/browse/VNFSDK-553

## Upgrade
- Upgraded from java 8 to java 11
    - https://jira.onap.org/browse/VNFSDK-631

## [1.2.13]

## Fixed
- Fixed rule R130206 CMS and certificate searching and validation mechanism 
    - https://jira.onap.org/browse/VNFSDK-595 
    
## Added
- Add new field called "warnings" to oclip json response. All ignored errors are now reported as warnings.
    - https://jira.onap.org/browse/VNFSDK-596

## [1.2.14]

## Fixed
- Fixed rule R130206 handling of CSARs with no TOSCA meta and no Certificate in root directory
    - https://jira.onap.org/browse/VNFSDK-481
- Fixed rule R816745 that was not reporting error when CMS and TOSCA meta file were present, 
  however TOSCA did not contain ETSI-Entry-Certificate
    - https://jira.onap.org/browse/VNFSDK-660

## [1.2.15]

## Move
- Extract pm-dictionary validation to separate module
    - https://jira.onap.org/browse/VNFSDK-713
-  Added possibility to validation pm-dictionary from byte array
    - https://jira.onap.org/browse/VNFSDK-713   
    
## [1.2.16]    
    
## Added
- Possibility to add certificate and signature per artifact in manifest file. 
    - https://jira.onap.org/browse/VNFSDK-714
- Possibility to validate PM_Dictionary using CLI operation
    - https://jira.onap.org/browse/VNFSDK-715

## [1.2.17]

## Added
- Possibility to use a common certificate for individual signature per artifact in manifest file. 
    - https://jira.onap.org/browse/VNFSDK-714
- Add oclip command to trigger pm_dictionary validation
    - https://jira.onap.org/browse/VNFSDK-721
