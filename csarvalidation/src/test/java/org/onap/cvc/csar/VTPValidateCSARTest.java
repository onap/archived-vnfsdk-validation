/*
 * Copyright 2019 Nokia
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.onap.cvc.csar;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class VTPValidateCSARTest {

    @Test
    public void shouldConfigureCsarValidatorBasedOnCsarArchive(){
        // given
        final CSARArchive csarArchive = givenCsarArchive();

        // when
        final VTPValidateCSAR.CSARValidation csarValidation = VTPValidateCSAR.createCsarValidationFor(csarArchive);

        // then
        final VTPValidateCSAR.CSARValidation.VNF vnf = csarValidation.getVnf();
        assertThat(vnf.getName()).isEqualTo("productName");
        assertThat(vnf.getVendor()).isEqualTo("vendorName");
        assertThat(vnf.getVersion()).isEqualTo("version");
        assertThat(vnf.getType()).isEqualTo("TOSCA");
        assertThat(vnf.getMode()).isEqualTo(CSARArchive.Mode.WITH_TOSCA_META_DIR.toString());
    }

    private CSARArchive givenCsarArchive() {
        final CSARArchive csarArchive = new CSARArchive();
        csarArchive.getToscaMeta().setMode(CSARArchive.Mode.WITH_TOSCA_META_DIR);
        csarArchive.getToscaMeta().setCompanyName("vendorName");

        final CSARArchive.Manifest.Metadata metadata = new CSARArchive.Manifest.Metadata();
        metadata.setProductName("productName");
        metadata.setPackageVersion("version");
        csarArchive.getManifest().setMetadata(metadata);
        return csarArchive;
    }

}
