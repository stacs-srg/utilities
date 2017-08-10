/*
 * Copyright 2017 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module utilities.
 *
 * utilities is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * utilities is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with utilities. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.utilities.crypto;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.utilities.FileManipulation;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class SymmetricEncryptionTest {

    private SecretKey valid_key;

    @Before
    public void setup() throws CryptoException {

        valid_key = SymmetricEncryption.generateRandomKey();
    }

    @Test(expected = CryptoException.class)
    public void encryptionWithShortKeyThrowsException() throws CryptoException {

        SymmetricEncryption.getKey("too short".getBytes());
    }

    @Test
    public void encryptedStringCanBeDecrypted() throws CryptoException {

        String plain_text = "the quick brown fox jumps over the lazy dog";

        assertEquals(plain_text, SymmetricEncryption.decrypt(valid_key, SymmetricEncryption.encrypt(valid_key, plain_text)));
    }

    @Test(expected = CryptoException.class)
    public void decryptionWithWrongKeyThrowsException() throws CryptoException {

        String plain_text = "the quick brown fox jumps over the lazy dog";
        SecretKey corrupted_key = corruptKey(valid_key);

        SymmetricEncryption.decrypt(corrupted_key, SymmetricEncryption.encrypt(valid_key, plain_text));
    }

    @Test
    public void encryptedFileCanBeDecrypted() throws CryptoException, IOException {

        Path plain_text_file_path = Files.createTempFile("plain_text_test", ".txt");
        Path encrypted_text_file_path = Files.createTempFile("encrypted_text_test", ".txt");
        Path decrypted_text_file_path = Files.createTempFile("decrypted_text_test", ".txt");

        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(plain_text_file_path))) {

            writer.println("the quick brown fox jumps over the lazy dog");
        }

        SymmetricEncryption.encrypt(valid_key, plain_text_file_path, encrypted_text_file_path);
        SymmetricEncryption.decrypt(valid_key, encrypted_text_file_path, decrypted_text_file_path);

        FileManipulation.assertThatFilesHaveSameContent(plain_text_file_path, decrypted_text_file_path);
    }

    @Test
    public void keyConversionToStringCanBeReversed() throws CryptoException {

        final SecretKey key = SymmetricEncryption.generateRandomKey();

        assertEquals(key, SymmetricEncryption.getKey(SymmetricEncryption.keyToString(key)));
    }

    @Test
    public void encryptionOverStreams() throws IOException, CryptoException {

        InputStream originalData = stringToInputStream("TEST");

        try (ByteArrayOutputStream outEncr = new ByteArrayOutputStream();
             ByteArrayOutputStream outDecr = new ByteArrayOutputStream()) {

            SecretKey key = SymmetricEncryption.generateRandomKey();

            SymmetricEncryption.encrypt(key, originalData, outEncr);
            InputStream encryptedStream = outputStreamToInputStream(outEncr);

            SymmetricEncryption.decrypt(key, encryptedStream, outDecr);
            InputStream decryptedStream = outputStreamToInputStream(outDecr);

            String decryptedData = inputStreamToString(decryptedStream);
            assertEquals(decryptedData, "TEST");
        }
    }

    private SecretKey corruptKey(SecretKey valid_key) throws CryptoException {

        final byte[] encoded = valid_key.getEncoded();
        encoded[0] = 37;
        return SymmetricEncryption.getKey(encoded);
    }

    public InputStream stringToInputStream(String input) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
    }

    public InputStream outputStreamToInputStream(ByteArrayOutputStream out) throws IOException {
        return new ByteArrayInputStream(out.toByteArray());
    }

    public String inputStreamToString(InputStream string) throws IOException {
        return IOUtils.toString(string, StandardCharsets.UTF_8);
    }

}
