/**
 * Copyright 2017 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.cvc.csar;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;

public class CSARArchiveTest {

    @Test
    public void testAll() throws IOException, InterruptedException {
        System.out.println(CSARArchive.SOL0004_2_4_1);

        for (String csarFileName: Arrays.asList(new String[] {"enterprise2DC", "VoLTE", "vEPC_NS", "vIMS_NS", "sample2", "vUSN", "vPCRF"})) {
            try {
                CSARArchive csar = new CSARArchive();
                System.out.println(csarFileName);
                csar.init("./src/test/resources/" + csarFileName + ".csar");
                csar.parse();
                csar.cleanup();
                System.out.println(csar.getErrors());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
