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
- Fixed VNFSDK doesn't check if all files in package are listed in manifest file
    - https://jira.onap.org/browse/VNFSDK-583
- Fixed package security SOL004 Option 1 make rule less restrictive as this rule is not implemented in SDC Onboarding
    - https://jira.onap.org/browse/VNFSDK-595
    
## [1.2.8]
