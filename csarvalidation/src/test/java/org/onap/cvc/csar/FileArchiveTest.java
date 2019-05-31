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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URISyntaxException;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.onap.cvc.csar.cc.sol004.IntegrationTestUtils.absoluteFilePath;

public class FileArchiveTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Test
    public void shouldUnpackCsarFile() throws URISyntaxException, IOException {
        // given
        String absolutePath = folder.getRoot().getAbsolutePath();

        // when
        FileArchive.Workspace workspace = new FileArchive(absolutePath).unpack(absoluteFilePath("pnf/r57019/allMandatoryEntriesDefinedInMetadataManifest.csar"));

        // then
        assertFalse(workspace.isZip());
        assertTrue(workspace.getRootFolder().isPresent());
        assertTrue(workspace.getPathToCsarFolder().isPresent());
        assertFalse(workspace.getPathToCertFile().isPresent());
        assertFalse(workspace.getPathToCmsFile().isPresent());
    }

    @Test
    public void shouldUnpackZipFile() throws URISyntaxException, IOException {
        // given
        String absolutePath = folder.getRoot().getAbsolutePath();

        // when
        FileArchive.Workspace workspace = new FileArchive(absolutePath).unpack(absoluteFilePath("pnf/signed-package-valid-signature.zip"));

        // then
        assertTrue(workspace.isZip());
        assertTrue(workspace.getRootFolder().isPresent());
        assertTrue(workspace.getPathToCsarFolder().isPresent());
        assertTrue(workspace.getPathToCertFile().isPresent());
        assertTrue(workspace.getPathToCmsFile().isPresent());
    }

}