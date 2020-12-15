package org.onap.cvc.csar.cc.sol004.r130206;

import org.onap.cvc.csar.security.ShaHashCodeGenerator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class FileHashValidator {

    private static final String SHA_256 = "SHA-256";
    private static final String SHA_512 = "SHA-512";

    private final ShaHashCodeGenerator shaHashCodeGenerator = new ShaHashCodeGenerator();

    public boolean isValid(String expectedHash, Path fileToCheck, String algorithm)
        throws NoSuchAlgorithmException, IOException {
        String hashCode = generateHashCode(fileToCheck, algorithm);
        return hashCode.equals(expectedHash);
    }

    private String generateHashCode(Path fileToCheck,  String algorithm)
        throws NoSuchAlgorithmException, IOException {
        final byte[] sourceData = Files.readAllBytes(fileToCheck);

        if (algorithm.equalsIgnoreCase(SHA_256)) {
            return this.shaHashCodeGenerator.generateSha256(sourceData);
        } else if (algorithm.equalsIgnoreCase(SHA_512)) {
            return this.shaHashCodeGenerator.generateSha512(sourceData);
        }

        throw new UnsupportedOperationException(String.format("Algorithm '%s' is not supported!", algorithm));
    }

}
