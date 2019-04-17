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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ZipFileContentValidatorTest {

    @Mock
    Path rootFolder;
    @Mock
    Path pathToCsarWorkspace;
    @Mock
    Path certFile;
    @Mock
    Path csarFile;
    @Mock
    Path cmsFile;

    private ZipFileContentValidator zipFileContentValidator;


    @Before
    public void setUp(){
         zipFileContentValidator = new ZipFileContentValidator();
    }

    @Test
    public void shouldReportThatCertFileAndCmsFileIsNotAvailable() {
        // given
        FileArchive.Workspace workspace = FileArchive.Workspace.forZip(
                rootFolder,
                pathToCsarWorkspace,
                null,
                null,
                null
        );

        // when
        List<CSARArchive.CSARError> errors = zipFileContentValidator.validate(workspace);

        // then
        assertThat(errors.size()).isEqualTo(2);
        assertThat(errors.stream().map(CSARArchive.CSARError::getMessage).collect(Collectors.toList())).contains(
                "Missing. Cert file is not available!", "Missing. CMS file is not available!"
        );
    }

    @Test
    public void shouldReportThatCertFileIsNotAvailable() {
        // given
        FileArchive.Workspace workspace = FileArchive.Workspace.forZip(
                rootFolder,
                pathToCsarWorkspace,
                null,
                cmsFile,
                csarFile
        );

        // when
        List<CSARArchive.CSARError> errors = zipFileContentValidator.validate(workspace);

        // then
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.stream().map(CSARArchive.CSARError::getMessage).collect(Collectors.toList())).contains(
                "Missing. Cert file is not available!"
        );
    }

    @Test
    public void shouldReportThatCmsFileIsNotAvailable() {
        // given
        FileArchive.Workspace workspace = FileArchive.Workspace.forZip(
                rootFolder,
                pathToCsarWorkspace,
                certFile,
                null,
                csarFile
        );

        // when
        List<CSARArchive.CSARError> errors = zipFileContentValidator.validate(workspace);

        // then
        assertThat(errors.size()).isEqualTo(1);
        assertThat(errors.stream().map(CSARArchive.CSARError::getMessage).collect(Collectors.toList())).contains(
                "Missing. CMS file is not available!"
        );
    }

    @Test
    public void shouldNotReportAnyErrorWhenAllFilesAreAvailable() {
        // given
        FileArchive.Workspace workspace = FileArchive.Workspace.forZip(
                rootFolder,
                pathToCsarWorkspace,
                certFile,
                cmsFile,
                csarFile
        );

        // when
        List<CSARArchive.CSARError> errors = zipFileContentValidator.validate(workspace);

        // then
        assertThat(errors.size()).isEqualTo(0);
    }
}