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

import org.apache.commons.lang3.tuple.Pair;
import org.onap.cvc.csar.CSARArchive;
import org.onap.cvc.csar.PnfCSARError;

import java.util.ArrayList;
import java.util.List;

import static org.onap.cvc.csar.parser.ManifestConsts.*;


public class CmsParser {


    private final String fileName;

    public CmsParser(String fileName) {
        this.fileName = fileName;
    }

    public Pair<String, List<CSARArchive.CSARError>>  parse(List<String> lines){
        StringBuilder buf = new StringBuilder();
        List<CSARArchive.CSARError> errors = new ArrayList<>();

        boolean isSpecialTagReached = false;
        boolean cmsSectionParsing = false;
        boolean endCmsMarkerReached = false;
        boolean atEndFile = true;


        for (String line : lines) {
            ManifestLine manifestLine = ManifestLine.of(line);
            if (cmsSectionParsing && (manifestLine.startsWith(METADATA_SECTION_TAG_SECTION)
                    || manifestLine.startsWith(NON_MANO_ARTIFACT_SETS_TAG_SECTION)
                    || manifestLine.startsWith(SOURCE_TAG_SECTION))) {
                isSpecialTagReached = true;
            } else if (!isSpecialTagReached && line.contains(BEGIN_CMS_SECTION)) {
                cmsSectionParsing = true;
            } else if (!isSpecialTagReached && line.contains(END_CMS_SECTION)) {
                if(!cmsSectionParsing){
                    errors.add(new PnfCSARError.PnfCSARErrorInvalidEntry("Unable to find BEGIN CMS marker!", this.fileName, -1));
                    break;
                }
                cmsSectionParsing = false;
                endCmsMarkerReached = true;
            } else if (cmsSectionParsing){
                buf.append(line);
            } else if(endCmsMarkerReached) {
                atEndFile = false;
            }
        }

        if(!atEndFile){
            errors.add(new PnfCSARError.PnfCSARErrorInvalidEntry("CMS section is not at the end of file!", this.fileName, -1));
        }

        return constructResponse(buf, errors, cmsSectionParsing, endCmsMarkerReached);
    }

    private Pair<String, List<CSARArchive.CSARError>> constructResponse(StringBuilder buf, List<CSARArchive.CSARError> errors, boolean cmsSectionParsing, boolean endCmsMarkerReached) {
        if(endCmsMarkerReached) {
            return Pair.of(buf.toString(), errors);
        } else if(cmsSectionParsing) {
            errors.add(new PnfCSARError.PnfCSARErrorInvalidEntry("Unable to find END CMS marker!", this.fileName, -1));
            return Pair.of("",errors);
        } else {
            return Pair.of("",errors);
        }
    }

}
