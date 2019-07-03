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

package org.onap.cvc.csar.parser;


final class ManifestConsts {

    private ManifestConsts(){}

    static final String METADATA_SECTION_TAG_SECTION = "metadata";
    static final String SOURCE_TAG_SECTION = "source";
    static final String ALGORITHM = "algorithm";
    static final String HASH = "hash";
    static final String NON_MANO_ARTIFACT_SETS_TAG_SECTION = "non_mano_artifact_sets";
    static final String PRODUCT_NAME = "pnfd_name";
    static final String PROVIDER_ID = "pnfd_provider";
    static final String VERSION = "pnfd_archive_version";
    static final String RELEASE_DATE_TIME = "pnfd_release_date_time";
    static final String CMS = "CMS";
    static final String BEGIN_CMS_SECTION = "BEGIN CMS";
    static final String END_CMS_SECTION = "END CMS";

}
