/*
  Copyright 2020 Nokia
  <p>
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */

package org.onap.cvc.csar.cc.sol004.r130206;

import org.onap.cvc.csar.CSARArchive;

public class Error {

    private static final String EMPTY_STRING = "";

    public static class CSARErrorUnableToFindCertificate extends CSARArchive.CSARError {

        CSARErrorUnableToFindCertificate() {
            super("0x4001");
            this.message = "Unable to find cert file!";
        }
    }

    public static class CSARErrorUnableToFindCms extends CSARArchive.CSARError {

        CSARErrorUnableToFindCms() {
            super("0x4002");
            this.message = "Unable to find cms signature!";
        }
    }

    public static class CSARErrorUnableToLoadCms extends CSARArchive.CSARError {
        CSARErrorUnableToLoadCms() {
            super("0x4002");
            this.message = "Unable to load cms signature!";
        }
    }

    public static class CSARErrorUnableToFindCsarContent extends CSARArchive.CSARError {

        public CSARErrorUnableToFindCsarContent() {
            super("0x4003");
            this.message = "Unable to find csar content!";
        }
    }

    public static class CSARErrorWrongHashCode extends CSARArchive.CSARError {

        CSARErrorWrongHashCode(String path) {
            super("0x4004");
            this.message = String.format("Source '%s' has wrong hash!", path);
        }
    }

    public static class CSARErrorUnableToFindAlgorithm extends CSARArchive.CSARError {

        CSARErrorUnableToFindAlgorithm(String path) {
            super("0x4005");
            this.message = String.format("Source '%s' has hash, but unable to find algorithm tag!", path);
        }
    }

    public static class CSARErrorUnableToFindSource extends CSARArchive.CSARError {

        CSARErrorUnableToFindSource(String path) {
            super("0x4006");
            this.message = String.format("Unable to calculate digest - file missing: %s", path);
        }
    }

    public static class CSARErrorInvalidSignature extends CSARArchive.CSARError {

        CSARErrorInvalidSignature() {
            super("0x4007");
            this.message = "Manifest file has invalid signature!";
            this.file = "";
        }
    }

    public static class CSARErrorContentMismatch extends CSARArchive.CSARError {

        CSARErrorContentMismatch() {
            super("0x4008");
            this.message = "Mismatch between contents of non-mano-artifact-sets and source files of the package";
        }
    }

    public static class CSARErrorUnableToFindEntryCertificate extends CSARArchive.CSARError {

        CSARErrorUnableToFindEntryCertificate() {
            super("0x4009");
            this.message = "Unable to find cert file defined by ETSI-Entry-Certificate!";
        }
    }

    public static class CSARErrorEntryCertificateIsDefinedDespiteTheCms extends CSARArchive.CSARError {

        CSARErrorEntryCertificateIsDefinedDespiteTheCms() {
            super("0x4011");
            this.message = "ETSI-Entry-Certificate entry in Tosca.meta is defined despite the certificate is included in the signature container";
        }
    }

    public static class CSARErrorEntryCertificateIsPresentDespiteTheCms extends CSARArchive.CSARError {

        CSARErrorEntryCertificateIsPresentDespiteTheCms() {
            super("0x4012");
            this.message = "ETSI-Entry-Certificate certificate present despite the certificate is included in the signature container";
        }
    }

    public static class CSARErrorRootCertificateIsPresentDespiteTheCms extends CSARArchive.CSARError {

        CSARErrorRootCertificateIsPresentDespiteTheCms() {
            super("0x4013");
            this.message = "Certificate present in root catalog despite the certificate is included in the signature container";
        }
    }

    public static class CSARErrorRootCertificateIsPresentDespiteTheEtsiEntryCertificate extends CSARArchive.CSARError {

        CSARErrorRootCertificateIsPresentDespiteTheEtsiEntryCertificate() {
            super("0x4013");
            this.message = "Certificate present in root catalog despite the TOSCA.meta file";
        }
    }

    public static class CSARErrorUnableToFindCertificateEntryInTosca extends CSARArchive.CSARError {

        CSARErrorUnableToFindCertificateEntryInTosca() {
            super("0x4014");
            this.message = "Unable to find ETSI-Entry-Certificate in Tosca file";
        }
    }

    public static class CSARErrorUnableToFindArtifactCertificateTag extends CSARArchive.CSARError {

        CSARErrorUnableToFindArtifactCertificateTag(String path) {
            super("0x4015");
            this.message = String.format("Source '%s' has signature tag, but unable to find certificate tag!", path);
        }
    }

    public static class CSARErrorUnableToFindArtifactSignatureTag extends CSARArchive.CSARError {

        CSARErrorUnableToFindArtifactSignatureTag(String path) {
            super("0x4017");
            this.message = String.format("Source '%s' has certificate tag, but unable to find signature tag!", path);
        }
    }

    public static class CSARErrorUnableToFindArtifactSecurityFile extends CSARArchive.CSARError {
        CSARErrorUnableToFindArtifactSecurityFile(String path, String signatureFile, String type) {
            super("0x4018");
            this.message = String.format(
                "Source '%s' has '%s' tag, pointing to non existing file!. Pointed file '%s'",
                path, type, signatureFile);
        }

    }

    public static class CSARErrorFailToLoadArtifactSignature extends CSARArchive.CSARError {

        CSARErrorFailToLoadArtifactSignature(String path, String cms) {
            super("0x4019");
            this.message = String.format("Fail to load signature '%s', for source '%s'!", path, cms);
        }
    }

    public static class CSARErrorIncorrectArtifactSignature extends CSARArchive.CSARError {

        CSARErrorIncorrectArtifactSignature(String path) {
            super("0x4020");
            this.message = String.format("Source '%s' has incorrect signature!", path);
        }
    }

    public static class CSARErrorWrongSecurityFileExtension extends CSARArchive.CSARError {

        CSARErrorWrongSecurityFileExtension(String path, String[] expectedExtension, String type) {
            super("0x4021");
            this.message = String.format(
                "Source '%s' has '%s' file with wrong extension, expected extension: '%s'!",
                path, type, String.join(", ", expectedExtension)
            );
        }
    }

    public static class CSARErrorWrongSecurityFileLocation extends CSARArchive.CSARError {

        CSARErrorWrongSecurityFileLocation(String path, String filePath, String type) {
            super("0x4022");
            this.message = String.format(
                "Source '%s' has '%s' file located in wrong directory, directory: '%s'." +
                    "Signature should be in same directory as source file!",
                path, type, filePath);
        }
    }

    public static class CSARErrorWrongSecurityFileName extends CSARArchive.CSARError {

        CSARErrorWrongSecurityFileName(String path, String fileName, String type) {
            super("0x4023");
            this.message = String.format(
                "Source '%s' has '%s' file with wrong name, signature name: '%s'." +
                    "Signature should have same name as source file!",
                path, type, fileName);
        }
    }

    public static class CSARWarningNoSecurity extends CSARArchive.CSARErrorWarning {
        CSARWarningNoSecurity() {
            super(EMPTY_STRING, EMPTY_STRING, -1, EMPTY_STRING);
            this.message = "Warning. Consider adding package integrity and authenticity assurance according to ETSI NFV-SOL 004 Security Option 1";
        }
    }
}
