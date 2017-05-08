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

import org.junit.Before;
import org.junit.Test;
import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AsymmetricEncryptionTest {

    private KeyPairGenerator generator;
    private PrivateKey private_key;
    private PublicKey public_key;

    @Before
    public void setup() throws NoSuchAlgorithmException {

        generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        final KeyPair key_pair = generator.generateKeyPair();

        private_key = key_pair.getPrivate();
        public_key = key_pair.getPublic();
    }

    @Test
    public void encryptedStringCanBeDecrypted() throws CryptoException, IOException {

        String plain_text = "the quick brown fox jumps over the lazy dog";

        assertEquals(plain_text, AsymmetricEncryption.decrypt(private_key, AsymmetricEncryption.encrypt(public_key, plain_text)));
    }

    @Test(expected = CryptoException.class)
    public void decryptionWithWrongKeyThrowsException() throws CryptoException, IOException {

        String plain_text = "the quick brown fox jumps over the lazy dog";
        PrivateKey wrong_key = generator.generateKeyPair().getPrivate();

        AsymmetricEncryption.decrypt(wrong_key, AsymmetricEncryption.encrypt(public_key, plain_text));
    }

    @Test
    public void encryptedFileCanBeDecrypted() throws CryptoException, IOException {

        Path plain_text_file_path = Files.createTempFile("plain_text_test", ".txt");
        Path encrypted_text_file_path = Files.createTempFile("encrypted_text_test", ".txt");
        Path decrypted_text_file_path = Files.createTempFile("decrypted_text_test", ".txt");

        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(plain_text_file_path))) {

            writer.println("the quick brown fox jumps over the lazy dog");
        }

        AsymmetricEncryption.encrypt(public_key, plain_text_file_path, encrypted_text_file_path);
        AsymmetricEncryption.decrypt(private_key, encrypted_text_file_path, decrypted_text_file_path);

        FileManipulation.assertThatFilesHaveSameContent(plain_text_file_path, decrypted_text_file_path);
    }

    @Test
    public void generateDefaultKeys() throws CryptoException {

        assertNotNull(AsymmetricEncryption.generateKeys());
    }

    @Test
    public void generateKeys() throws CryptoException {

        assertNotNull(AsymmetricEncryption.generateKeys(512));
        assertNotNull(AsymmetricEncryption.generateKeys(1024));
        assertNotNull(AsymmetricEncryption.generateKeys(2048));
        assertNotNull(AsymmetricEncryption.generateKeys(4096));
    }

    @Test (expected = CryptoException.class)
    public void generateKeysFailShort() throws CryptoException {

        assertNotNull(AsymmetricEncryption.generateKeys(511));
    }

    @Test (expected = CryptoException.class)
    public void generateKeysFailLong() throws CryptoException {

        assertNotNull(AsymmetricEncryption.generateKeys(4097));
    }
}
