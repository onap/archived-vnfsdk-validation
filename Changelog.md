# Changelog
All notable changes to this project will be documented in this file.


## [1.2.5]

### Fixed
- Fixed package integrity issue with non mano arifacts.
    - https://jira.onap.org/browse/VNFSDK-581
- Fixed VNF/PNF package integrity issue with CMS signature not containing certificate.
    - https://jira.onap.org/browse/VNFSDK-582
- Fixed bug that was showing errors during validation of CSAR,
    when any other non_mano_artifact_set than onap_pnf_sw_information was present in manifest file.
    - https://jira.onap.org/browse/VNFSDK-585
