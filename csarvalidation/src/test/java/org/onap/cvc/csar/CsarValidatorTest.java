/*
 * Copyright 2021 Nokia
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
 */
package org.onap.cvc.csar;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.cli.fw.error.OnapCommandException;
import org.onap.cvc.csar.oclip.Command;
import org.onap.cvc.csar.oclip.CommandFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CsarValidatorTest {

    private static final String PATH_TO_CSAR_FILE = "pathToCsarFile";
    private static final String PRODUCT = "onap-vtp";
    private static final boolean IS_PNF = true;
    private static final String AMSTERDAM_RULE = "rAmsterdam";
    private static final String CASABLANCA_RULE = "rCasablanca";
    private static final String DUBLIN_RULE = "rDublin";
    private static final String FRANKFURT_RULE = "rFrankfurt";
    private static final String GUILIN_RULE = "rGuilin";
    private static final String HONOLULU_RULE = "rHonolulu";
    private static final String AMSTERDAM_RELEASE = "amsterdam";
    private static final String CASABLANCA_RELEASE = "casablanca";
    private static final String DUBLIN_RELEASE = "dublin";
    private static final String FRANKFURT_RELEASE = "frankfurt";
    private static final String GUILIN_RELEASE = "guilin";
    private static final String HONOLULU_RELEASE = "honolulu";
    @Mock
    private CommandFactory commandFactory;
    private final ReleasesResolver releasesResolver = ReleasesResolver.create("amsterdam,casablanca,dublin,frankfurt,guilin,honolulu");
    private final RulesToValidate rulesToValidate = new RulesToValidate("");
    private final List<String> activeRules = List.of(AMSTERDAM_RULE, CASABLANCA_RULE, DUBLIN_RULE, FRANKFURT_RULE, GUILIN_RULE, HONOLULU_RULE);

    private final List<String> ignoreCodes = List.of();

    private CsarValidator csarValidator;


    @Before
    public void setUp() {
        this.csarValidator = new CsarValidator(commandFactory, ignoreCodes, activeRules, releasesResolver, rulesToValidate);
    }

    @Test
    public void shouldReportAnErrorWhenReleaseIsNotRecognized() throws OnapCommandException {

        // given
        Command cmdAmsterdam = givenCmdFor(AMSTERDAM_RULE, AMSTERDAM_RELEASE);
        Command cmdCasablanca = givenCmdFor(CASABLANCA_RULE, CASABLANCA_RELEASE);
        Command cmdDublin = givenCmdFor(DUBLIN_RULE, DUBLIN_RELEASE);
        Command cmdFrankfurt = givenCmdFor(FRANKFURT_RULE, FRANKFURT_RELEASE);
        Command cmdGuilin = givenCmdFor(GUILIN_RULE, GUILIN_RELEASE);
        Command cmdHonolulu = givenCmdFor(HONOLULU_RULE, HONOLULU_RELEASE);

        // when
        final Pair<Boolean, List<VTPValidateCSAR.CSARValidation.Result>> validationResult = csarValidator.validate(new CsarValidator.ValidationContext(PATH_TO_CSAR_FILE, PRODUCT, "validationResult", IS_PNF));

        // then
        assertThat(validationResult.getLeft()).isFalse();
        verify(cmdAmsterdam, never()).run();
        verify(cmdCasablanca, never()).run();
        verify(cmdDublin, never()).run();
        verify(cmdFrankfurt, never()).run();
        verify(cmdGuilin, never()).run();
        verify(cmdHonolulu, never()).run();
    }

    @Test
    public void shouldValidateCsarUsingAllRulesWhenReleaseIsLatest() throws OnapCommandException {

        // given
        Command cmdAmsterdam = givenCmdFor(AMSTERDAM_RULE, AMSTERDAM_RELEASE);
        Command cmdCasablanca = givenCmdFor(CASABLANCA_RULE, CASABLANCA_RELEASE);
        Command cmdDublin = givenCmdFor(DUBLIN_RULE, DUBLIN_RELEASE);
        Command cmdFrankfurt = givenCmdFor(FRANKFURT_RULE, FRANKFURT_RELEASE);
        Command cmdGuilin = givenCmdFor(GUILIN_RULE, GUILIN_RELEASE);
        Command cmdHonolulu = givenCmdFor(HONOLULU_RULE, HONOLULU_RELEASE);

        // when
        csarValidator.validate(new CsarValidator.ValidationContext(PATH_TO_CSAR_FILE, PRODUCT, "latest", IS_PNF));

        // then
        verify(cmdAmsterdam).run();
        verify(cmdCasablanca).run();
        verify(cmdDublin).run();
        verify(cmdFrankfurt).run();
        verify(cmdGuilin).run();
        verify(cmdHonolulu).run();
    }

    @Test
    public void shouldValidateCsarUsingOnlyAmsterdamRulesWhenReleaseIsAmsterdam() throws OnapCommandException {
        // given
        Command cmdAmsterdam = givenCmdFor(AMSTERDAM_RULE, AMSTERDAM_RELEASE);
        Command cmdCasablanca = givenCmdFor(CASABLANCA_RULE, CASABLANCA_RELEASE);
        Command cmdDublin = givenCmdFor(DUBLIN_RULE, DUBLIN_RELEASE);
        Command cmdFrankfurt = givenCmdFor(FRANKFURT_RULE, FRANKFURT_RELEASE);
        Command cmdGuilin = givenCmdFor(GUILIN_RULE, GUILIN_RELEASE);
        Command cmdHonolulu = givenCmdFor(HONOLULU_RULE, HONOLULU_RELEASE);

        // when
        csarValidator.validate(new CsarValidator.ValidationContext(PATH_TO_CSAR_FILE, PRODUCT, AMSTERDAM_RELEASE, IS_PNF));

        // then
        verify(cmdAmsterdam).run();
        verify(cmdCasablanca, never()).run();
        verify(cmdDublin, never()).run();
        verify(cmdFrankfurt, never()).run();
        verify(cmdGuilin, never()).run();
        verify(cmdHonolulu, never()).run();
    }

    @Test
    public void shouldValidateCsarUsingCasablancaWithAncestorRulesWhenReleaseIsCasablanca() throws OnapCommandException {
        // given
        Command cmdAmsterdam = givenCmdFor(AMSTERDAM_RULE, AMSTERDAM_RELEASE);
        Command cmdCasablanca = givenCmdFor(CASABLANCA_RULE, CASABLANCA_RELEASE);
        Command cmdDublin = givenCmdFor(DUBLIN_RULE, DUBLIN_RELEASE);
        Command cmdFrankfurt = givenCmdFor(FRANKFURT_RULE, FRANKFURT_RELEASE);
        Command cmdGuilin = givenCmdFor(GUILIN_RULE, GUILIN_RELEASE);
        Command cmdHonolulu = givenCmdFor(HONOLULU_RULE, HONOLULU_RELEASE);

        // when
        csarValidator.validate(new CsarValidator.ValidationContext(PATH_TO_CSAR_FILE, PRODUCT, CASABLANCA_RELEASE, IS_PNF));

        // then
        verify(cmdAmsterdam).run();
        verify(cmdCasablanca).run();
        verify(cmdDublin, never()).run();
        verify(cmdFrankfurt, never()).run();
        verify(cmdGuilin, never()).run();
        verify(cmdHonolulu, never()).run();
    }

    @Test
    public void shouldValidateCsarUsingDublinWithAncestorRulesWhenReleaseIsDublin() throws OnapCommandException {
        // given
        Command cmdAmsterdam = givenCmdFor(AMSTERDAM_RULE, AMSTERDAM_RELEASE);
        Command cmdCasablanca = givenCmdFor(CASABLANCA_RULE, CASABLANCA_RELEASE);
        Command cmdDublin = givenCmdFor(DUBLIN_RULE, DUBLIN_RELEASE);
        Command cmdFrankfurt = givenCmdFor(FRANKFURT_RULE, FRANKFURT_RELEASE);
        Command cmdGuilin = givenCmdFor(GUILIN_RULE, GUILIN_RELEASE);
        Command cmdHonolulu = givenCmdFor(HONOLULU_RULE, HONOLULU_RELEASE);

        // when
        csarValidator.validate(new CsarValidator.ValidationContext(PATH_TO_CSAR_FILE, PRODUCT, DUBLIN_RELEASE, IS_PNF));

        // then
        verify(cmdAmsterdam).run();
        verify(cmdCasablanca).run();
        verify(cmdDublin).run();
        verify(cmdFrankfurt, never()).run();
        verify(cmdGuilin, never()).run();
        verify(cmdHonolulu, never()).run();
    }

    @Test
    public void shouldValidateCsarUsingHonoluluWithAncestorRulesWhenReleaseIsHonolulu() throws OnapCommandException {
        // given
        Command cmdAmsterdam = givenCmdFor(AMSTERDAM_RULE, AMSTERDAM_RELEASE);
        Command cmdCasablanca = givenCmdFor(CASABLANCA_RULE, CASABLANCA_RELEASE);
        Command cmdDublin = givenCmdFor(DUBLIN_RULE, DUBLIN_RELEASE);
        Command cmdFrankfurt = givenCmdFor(FRANKFURT_RULE, FRANKFURT_RELEASE);
        Command cmdGuilin = givenCmdFor(GUILIN_RULE, GUILIN_RELEASE);
        Command cmdHonolulu = givenCmdFor(HONOLULU_RULE, HONOLULU_RELEASE);

        // when
        csarValidator.validate(new CsarValidator.ValidationContext(PATH_TO_CSAR_FILE, PRODUCT, HONOLULU_RELEASE, IS_PNF));

        // then
        verify(cmdAmsterdam).run();
        verify(cmdCasablanca).run();
        verify(cmdDublin).run();
        verify(cmdFrankfurt).run();
        verify(cmdGuilin).run();
        verify(cmdHonolulu).run();
    }

    private Command givenCmdFor(String rule, String release) throws OnapCommandException {
        Command cmd = Mockito.mock(Command.class);
        Mockito.when(commandFactory.createForPnf(rule, PATH_TO_CSAR_FILE, PRODUCT)).thenReturn(cmd);
        Mockito.when(cmd.getRelease()).thenReturn(release);
        Mockito.when(cmd.run()).thenReturn(List.of());

        return cmd;
    }
}
