/*
 * Copyright 2021 Systems Research Group, University of St Andrews:
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

import uk.ac.standrews.cs.utilities.FileManipulation;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

/**
 * <p>A utility class that encrypts or decrypts a finite-length stream using AES.
 * The encrypted data is also Base64 MIME-encoded.</p>
 * <p>
 * <p>Code derived from articles linked below.</p>
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @see <a href="http://www.codejava.net/coding/file-encryption-and-decryption-simple-example">http://www.codejava.net/coding/file-encryption-and-decryption-simple-example</a>
 */
public class SymmetricEncryption {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";

    private static final int KEY_LENGTH_IN_BYTES = 16; // Only 128bit keys are supported with the default Java. AES should support 192 and 256 bit keys too.
    private static final String CIPHER_TEXT_HEADER = "uk.ac.standrews.cs.util.dataset.encrypted\n";

    private static final Cipher CIPHER;

    private static final Random RANDOM = new SecureRandom();

    static {
        try {
            CIPHER = Cipher.getInstance(TRANSFORMATION);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("error loading cipher " + TRANSFORMATION);
        }
    }

    /**
     * Encrypts the given plain text string using the given AES key, and MIME-encodes the result.
     *
     * @param key        the AES key
     * @param plain_text the plain text
     * @return the encrypted and MIME-encoded text
     * @throws CryptoException if the text cannot be encrypted
     */
    @SuppressWarnings("WeakerAccess")
    public static String encrypt(final SecretKey key, final String plain_text) throws CryptoException {

        try (final InputStream input_stream = new ByteArrayInputStream(plain_text.getBytes());
             final ByteArrayOutputStream output_stream = new ByteArrayOutputStream()) {

            encrypt(key, input_stream, output_stream);

            return new String(output_stream.toByteArray());

        } catch (IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Decrypts the given encrypted and MIME-encoded text string using the given AES key.
     *
     * @param key         the AES key
     * @param cipher_text the encrypted and MIME-encoded text
     * @return the plain text
     * @throws CryptoException if the decryption cannot be completed
     */
    @SuppressWarnings("WeakerAccess")
    public static String decrypt(final SecretKey key, final String cipher_text) throws CryptoException {

        try (final InputStream input_stream = new ByteArrayInputStream(cipher_text.getBytes());
             final ByteArrayOutputStream output_stream = new ByteArrayOutputStream()) {

            decrypt(key, input_stream, output_stream);

            return new String(output_stream.toByteArray());

        } catch (IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Encrypts the given plain text file to another file, using the given AES key, and MIME-encodes the result.
     *
     * @param key              the AES key
     * @param plain_text_path  the path of the plain text file
     * @param cipher_text_path the path of the resulting encrypted file
     * @throws CryptoException if the encryption cannot be completed
     * @throws IOException     if a file cannot be accessed
     */
    @SuppressWarnings("WeakerAccess")
    public static void encrypt(final SecretKey key, final Path plain_text_path, final Path cipher_text_path) throws CryptoException, IOException {

        try (final InputStream input_stream = Files.newInputStream(plain_text_path);
             final OutputStream output_stream = Files.newOutputStream(cipher_text_path)) {

            encrypt(key, input_stream, output_stream);
        }
    }

    /**
     * Decrypts the given encrypted and MIME-encoded text file to another file, using the given AES key.
     *
     * @param key              the AES key
     * @param cipher_text_path the path of the encrypted file
     * @param plain_text_path  the path of the resulting plain text file
     * @throws CryptoException if the encryption cannot be completed
     * @throws IOException     if a file cannot be accessed
     */
    @SuppressWarnings("WeakerAccess")
    public static void decrypt(final SecretKey key, final Path cipher_text_path, final Path plain_text_path) throws CryptoException, IOException {

        try (final InputStream input_stream = Files.newInputStream(cipher_text_path);
             final OutputStream output_stream = Files.newOutputStream(plain_text_path)) {

            decrypt(key, input_stream, output_stream);
        }
    }

    /**
     * Encrypts the given plain text file to another file, using the given AES key, and MIME-encodes the result.
     *
     * @param key             the AES key
     * @param plain_text_path the path of the plain text file
     * @param output_stream   the output stream for the resulting encrypted data
     * @throws CryptoException if the encryption cannot be completed
     * @throws IOException     if a file cannot be accessed
     */
    @SuppressWarnings("unused")
    public static void encrypt(final SecretKey key, final Path plain_text_path, final OutputStream output_stream) throws CryptoException, IOException {

        try (final InputStream input_stream = Files.newInputStream(plain_text_path)) {

            encrypt(key, input_stream, output_stream);
        }
    }

    /**
     * Decrypts the given encrypted and MIME-encoded text file to another file, using the given AES key.
     *
     * @param key              the AES key
     * @param cipher_text_path the path of the encrypted file
     * @param output_stream    the output stream for the resulting data
     * @throws CryptoException if the encryption cannot be completed
     * @throws IOException     if a file cannot be accessed
     */
    @SuppressWarnings("unused")
    public static void decrypt(final SecretKey key, final Path cipher_text_path, final OutputStream output_stream) throws CryptoException, IOException {

        try (final InputStream input_stream = Files.newInputStream(cipher_text_path)) {

            decrypt(key, input_stream, output_stream);
        }
    }

    /**
     * Encrypts the given plain text file to another file, using the given AES key, and MIME-encodes the result.
     *
     * @param key           the AES key
     * @param input_stream  the input stream for the plain text
     * @param output_stream the output stream for the resulting encrypted data
     * @throws CryptoException if the encryption cannot be completed
     */
    @SuppressWarnings("WeakerAccess")
    public static void encrypt(final SecretKey key, InputStream input_stream, OutputStream output_stream) throws CryptoException {

        try {
            CIPHER.init(Cipher.ENCRYPT_MODE, key);

            final byte[] plain_text = FileManipulation.readAllBytes(input_stream);
            final byte[] plain_text_with_header = prependHeader(plain_text);
            final byte[] encrypted = CIPHER.doFinal(plain_text_with_header);
            final byte[] mime_encoded = MIMEEncode(encrypted);

            output_stream.write(mime_encoded);
            output_stream.flush();

        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException | IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Decrypts the given encrypted and MIME-encoded text file to another file, using the given AES key.
     *
     * @param key           the AES key
     * @param input_stream  the input stream for the encrypted file
     * @param output_stream the output stream for the resulting data
     * @throws CryptoException if the encryption cannot be completed
     */
    @SuppressWarnings("WeakerAccess")
    public static void decrypt(final SecretKey key, final InputStream input_stream, final OutputStream output_stream) throws CryptoException {

        try {
            CIPHER.init(Cipher.DECRYPT_MODE, key);

            final byte[] mime_encoded = FileManipulation.readAllBytes(input_stream);
            final byte[] encrypted = MIMEDecode(mime_encoded);
            final byte[] plain_text_with_header = CIPHER.doFinal(encrypted);

            checkForValidHeader(plain_text_with_header);

            final byte[] plain_text = stripHeader(plain_text_with_header);

            output_stream.write(plain_text);

        } catch (BadPaddingException e) {
            throw new CryptoException("Incorrect key");
        } catch (InvalidKeyException | IllegalBlockSizeException | IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Extracts an AES key from the given MIME-encoded string.
     *
     * @param mime_encoded_AES_key the MIME-encoded key
     * @return the extracted key
     * @throws CryptoException if a valid key cannot be extracted
     */
    @SuppressWarnings("WeakerAccess")
    public static SecretKey getKey(final String mime_encoded_AES_key) throws CryptoException {

        try {
            final byte[] key_bytes = MIMEDecode(mime_encoded_AES_key.getBytes());

            if (key_bytes.length != KEY_LENGTH_IN_BYTES) {
                throw new CryptoException("Key length must be " + KEY_LENGTH_IN_BYTES);
            }
            return getKey(key_bytes);

        } catch (IllegalArgumentException e) {
            throw new CryptoException("Invalid MIME-encoded key: " + mime_encoded_AES_key);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static String keyToString(final SecretKey AES_key) {

        return new String(MIMEEncode(AES_key.getEncoded()));
    }

    /**
     * Generates a random AES key.
     *
     * @return a random key
     * @throws CryptoException if the key cannot be generated
     */
    @SuppressWarnings("WeakerAccess")
    public static SecretKey generateRandomKey() throws CryptoException {

        final byte[] key_bytes = new byte[KEY_LENGTH_IN_BYTES];

        RANDOM.nextBytes(key_bytes);

        return getKey(key_bytes);
    }

    @SuppressWarnings("WeakerAccess")
    protected static SecretKey getKey(final byte[] key_bytes) throws CryptoException {

        try {
            return new SecretKeySpec(key_bytes, 0, KEY_LENGTH_IN_BYTES, ALGORITHM);

        } catch (IllegalArgumentException e) {
            throw new CryptoException(e);
        }
    }

    private static byte[] MIMEEncode(final byte[] bytes) {

        return Base64.getMimeEncoder().encode(bytes);
    }

    private static byte[] MIMEDecode(final byte[] bytes) {

        return Base64.getMimeDecoder().decode(bytes);
    }

    private static byte[] stripHeader(final byte[] input_bytes_with_header) {

        return Arrays.copyOfRange(input_bytes_with_header, CIPHER_TEXT_HEADER.length(), input_bytes_with_header.length);
    }

    private static void checkForValidHeader(final byte[] input_bytes_with_header) throws InvalidKeyException {

        if (!new String(input_bytes_with_header, 0, CIPHER_TEXT_HEADER.length()).equals(CIPHER_TEXT_HEADER)) {
            throw new InvalidKeyException();
        }
    }

    private static byte[] prependHeader(final byte[] output_bytes) {

        final byte[] temp = new byte[output_bytes.length + CIPHER_TEXT_HEADER.length()];

        System.arraycopy(CIPHER_TEXT_HEADER.getBytes(), 0, temp, 0, CIPHER_TEXT_HEADER.length());
        System.arraycopy(output_bytes, 0, temp, CIPHER_TEXT_HEADER.length(), output_bytes.length);

        return temp;
    }
}
