package org.onap.cvc.csar.parser;

import org.junit.Test;
import org.onap.cvc.csar.PnfCSARError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SourcesParserTest {

    final private String TEST_FILE_NAME = "test_file.mf";

    @Test
    public void shouldReturnEmptyListOfSourcesFromEmptyListOfLines() {
        // given
        final var parser = new SourcesParser(TEST_FILE_NAME);
        final List<String> manifestLines = List.of();

        // when
        final var parsingResult = parser.parse(manifestLines);

        // then
        assertThat(parsingResult.getRight()).isEmpty();
        assertThat(parsingResult.getLeft()).isEmpty();
    }

    @Test
    public void shouldCreateListOfSourcesFromListOfLinesContainingTwoSources() {
        // given
        final var parser = new SourcesParser(TEST_FILE_NAME);
        final var manifestLines = List.of(
            "Source: pnf_main_descriptor.mf",
            "Source: Definitions/pnf_main_descriptor.yaml"
        );

        // when
        final var parsingResult = parser.parse(manifestLines);

        // then
        var errors = parsingResult.getRight();
        assertThat(errors).isEmpty();
        var sources = parsingResult.getLeft();
        assertThat(sources).containsExactly(
            new SourcesParser.Source("pnf_main_descriptor.mf"),
            new SourcesParser.Source("Definitions/pnf_main_descriptor.yaml")
        );
    }

    @Test
    public void shouldCreateListOfSourcesFromListOfLinesContainingTwoSourcesWithHashesAndAlgorithms() {
        // given
        final var parser = new SourcesParser(TEST_FILE_NAME);
        final var manifestLines = List.of(
            "Source: Definitions/pnf_main_descriptor.yaml",
            "Algorithm: SHA-256",
            "Hash: 8a041578eefd22c10418600e4c3cb8c5d1ff5703ae2785ed53540263f4030703",
            "Source: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
            "Algorithm: SHA-256",
            "Hash: 9c82363306531b5f087f11058a7b18021e4597a75ca7c5a72d0893893646bcb0"
        );

        // when
        final var parsingResult = parser.parse(manifestLines);

        // then
        var errors = parsingResult.getRight();
        assertThat(errors).isEmpty();
        var sources = parsingResult.getLeft();
        assertThat(sources).containsExactly(
            new SourcesParser.Source(
                "Definitions/pnf_main_descriptor.yaml",
                "SHA-256",
                "8a041578eefd22c10418600e4c3cb8c5d1ff5703ae2785ed53540263f4030703"),
            new SourcesParser.Source(
                "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
                "SHA-256",
                "9c82363306531b5f087f11058a7b18021e4597a75ca7c5a72d0893893646bcb0")
        );
    }

    @Test
    public void shouldCreateListOfSourcesFromListOfLinesContainingTwoSourcesWithHashesAndAlgorithmsAndTwoSourcesWithOnlyValue() {
        // given
        final var parser = new SourcesParser(TEST_FILE_NAME);
        final var manifestLines = List.of(
            "Source: Definitions/pnf_main_descriptor.yaml",
            "Algorithm: SHA-256",
            "Hash: 8a041578eefd22c10418600e4c3cb8c5d1ff5703ae2785ed53540263f4030703",
            "Source: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
            "Algorithm: SHA-256",
            "Hash: 9c82363306531b5f087f11058a7b18021e4597a75ca7c5a72d0893893646bcb0",
            "Source: pnf_main_descriptor.mf",
            "Source: TOSCA-Metadata/TOSCA.meta"
        );

        // when
        final var parsingResult = parser.parse(manifestLines);

        // then
        var errors = parsingResult.getRight();
        assertThat(errors).isEmpty();
        var sources = parsingResult.getLeft();
        assertThat(sources).containsExactly(
            new SourcesParser.Source(
                "Definitions/pnf_main_descriptor.yaml",
                "SHA-256",
                "8a041578eefd22c10418600e4c3cb8c5d1ff5703ae2785ed53540263f4030703"),
            new SourcesParser.Source(
                "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
                "SHA-256",
                "9c82363306531b5f087f11058a7b18021e4597a75ca7c5a72d0893893646bcb0"),
            new SourcesParser.Source(
                "pnf_main_descriptor.mf"),
            new SourcesParser.Source(
                "TOSCA-Metadata/TOSCA.meta")
        );
    }

    @Test
    public void shouldCreateListOfSourcesFromListOfLinesContainingTwoSourcesWithHashesAlgorithmsSignatureAndCertificate() {
        // given
        final var parser = new SourcesParser(TEST_FILE_NAME);
        final var manifestLines = List.of(
            "Source: Definitions/pnf_main_descriptor.yaml",
            "Algorithm: SHA-256",
            "Hash: 8a041578eefd22c10418600e4c3cb8c5d1ff5703ae2785ed53540263f4030703",
            "Signature: Definitions/pnf_main_descriptor.sig.cms",
            "Certificate: Definitions/pnf_main_descriptor.cert",
            "Source: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
            "Algorithm: SHA-256",
            "Hash: 9c82363306531b5f087f11058a7b18021e4597a75ca7c5a72d0893893646bcb0",
            "Signature: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.sig.cms",
            "Certificate: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.cert"
        );

        // when
        final var parsingResult = parser.parse(manifestLines);

        // then
        var errors = parsingResult.getRight();
        assertThat(errors).isEmpty();
        var sources = parsingResult.getLeft();
        assertThat(sources).containsExactly(
            new SourcesParser.Source(
                "Definitions/pnf_main_descriptor.yaml",
                "SHA-256",
                "8a041578eefd22c10418600e4c3cb8c5d1ff5703ae2785ed53540263f4030703",
                "Definitions/pnf_main_descriptor.sig.cms",
                "Definitions/pnf_main_descriptor.cert"),
            new SourcesParser.Source(
                "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
                "SHA-256",
                "9c82363306531b5f087f11058a7b18021e4597a75ca7c5a72d0893893646bcb0",
                "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.sig.cms",
                "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.cert")
        );
    }


    @Test
    public void shouldCreateListOfSourcesFromListOfLinesContainingSourcesWithVariousDataProvided() {
        // given
        final var parser = new SourcesParser(TEST_FILE_NAME);
        final var manifestLines = List.of(
            "Source: pnf_main_descriptor.mf",
            "Source: Definitions/pnf_main_descriptor.yaml",
            "Algorithm: SHA-256",
            "Hash: 8a041578eefd22c10418600e4c3cb8c5d1ff5703ae2785ed53540263f4030703",
            "Source: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
            "Algorithm: SHA-256",
            "Hash: 9c82363306531b5f087f11058a7b18021e4597a75ca7c5a72d0893893646bcb0",
            "Signature: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.sig.cms",
            "Certificate: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.cert"
        );

        // when
        final var parsingResult = parser.parse(manifestLines);

        // then
        var errors = parsingResult.getRight();
        assertThat(errors).isEmpty();
        var sources = parsingResult.getLeft();
        assertThat(sources).containsExactly(
            new SourcesParser.Source(
                "pnf_main_descriptor.mf"),
            new SourcesParser.Source(
                "Definitions/pnf_main_descriptor.yaml",
                "SHA-256",
                "8a041578eefd22c10418600e4c3cb8c5d1ff5703ae2785ed53540263f4030703"),
            new SourcesParser.Source(
                "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
                "SHA-256",
                "9c82363306531b5f087f11058a7b18021e4597a75ca7c5a72d0893893646bcb0",
                "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.sig.cms",
                "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.cert")
        );
    }

    @Test
    public void shouldStopParsingWhenLineWithSpecialTagIsReached() {
        // given
        final var parser = new SourcesParser(TEST_FILE_NAME);
        final var manifestLines = List.of(
            "Source: pnf_main_descriptor.mf",
            "Source: Definitions/pnf_main_descriptor.yaml",
            "Algorithm: SHA-256",
            "Hash: 8a041578eefd22c10418600e4c3cb8c5d1ff5703ae2785ed53540263f4030703",
            "Metadata: test special metadata",
            "Source: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
            "Algorithm: SHA-256",
            "Hash: 9c82363306531b5f087f11058a7b18021e4597a75ca7c5a72d0893893646bcb0",
            "Signature: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.sig.cms",
            "Certificate: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.cert"
        );

        // when
        final var parsingResult = parser.parse(manifestLines);

        // then
        var errors = parsingResult.getRight();
        assertThat(errors).isEmpty();
        var sources = parsingResult.getLeft();
        assertThat(sources).containsExactly(
            new SourcesParser.Source(
                "pnf_main_descriptor.mf"),
            new SourcesParser.Source(
                "Definitions/pnf_main_descriptor.yaml",
                "SHA-256",
                "8a041578eefd22c10418600e4c3cb8c5d1ff5703ae2785ed53540263f4030703")
        );
    }

    @Test
    public void shouldCreateListContainingErrorWhenListOfSourcesContainIncorrectLine() {
        // given
        final var parser = new SourcesParser(TEST_FILE_NAME);
        final var manifestLines = List.of(
            "Source pnf_main_descriptor.mf"
        );

        // when
        final var parsingResult = parser.parse(manifestLines);

        // then
        var errors = parsingResult.getRight();
        assertThat(errors).usingFieldByFieldElementComparator().containsExactly(
            new PnfCSARError.PnfCSARErrorWarning("Source pnf_main_descriptor.mf","test_file.mf",0)
        );
        var sources = parsingResult.getLeft();
        assertThat(sources).isEmpty();
    }


    @Test
    public void shouldCreateListContainingOneSourceAndOneErrorWhenListOfSourcesContainOneCorrectLineAndOneIncorrectLine() {
        // given
        final var parser = new SourcesParser(TEST_FILE_NAME);
        final var manifestLines = List.of(
            "Source: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
            "Algorithm: SHA-256",
            "Hash: 9c82363306531b5f087f11058a7b18021e4597a75ca7c5a72d0893893646bcb0",
            "Signature: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.sig.cms",
            "Certificate: Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.cert",
            "Source pnf_main_descriptor.mf"
        );

        // when
        final var parsingResult = parser.parse(manifestLines);

        // then
        var errors = parsingResult.getRight();
        assertThat(errors).usingFieldByFieldElementComparator().containsExactly(
            new PnfCSARError.PnfCSARErrorWarning("Source pnf_main_descriptor.mf","test_file.mf",5)
        );
        var sources = parsingResult.getLeft();
        assertThat(sources).containsExactly(
            new SourcesParser.Source(
                "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.yaml",
                "SHA-256",
                "9c82363306531b5f087f11058a7b18021e4597a75ca7c5a72d0893893646bcb0",
                "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.sig.cms",
                "Definitions/etsi_nfv_sol001_pnfd_2_5_1_types.cert")
        );
    }
}
