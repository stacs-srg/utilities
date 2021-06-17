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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import uk.ac.standrews.cs.utilities.FileManipulation;

import javax.crypto.*;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static uk.ac.standrews.cs.utilities.crypto.PemFile.stripKeyDelimiters;
import static uk.ac.standrews.cs.utilities.crypto.PemFile.writePemFile;
import static uk.ac.standrews.cs.utilities.crypto.Utils.extension;

/**
 * <p>A utility class that encrypts or decrypts data using RSA public key encryption.
 * The encrypted data is also Base64 MIME-encoded.</p>
 * <p>
 * <p>This code works with keys in PEM format, generated as follows:</p>
 * <p>
 * <pre>{@code
 * openssl genrsa -out private_key.pem 2048
 * chmod 600 private_key.pem
 * openssl rsa -in private_key.pem -pubout > public_key.pem
 * }</pre>
 * <p>
 * <p>Code derived from articles linked below.</p>
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 * @see <a href="http://www.codejava.net/coding/file-encryption-and-decryption-simple-example">http://www.codejava.net/coding/file-encryption-and-decryption-simple-example</a>
 * @see <a href="http://stackoverflow.com/questions/11787571/how-to-read-pem-file-to-get-private-and-public-key#19166352">http://stackoverflow.com/questions/11787571/how-to-read-pem-file-to-get-private-and-public-key#19166352</a>
 */
public class AsymmetricEncryption {

    /**
     * The name of the directory in this user's home directory in which private and public keys are stored.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String DEFAULT_KEY_DIR = ".ssh";

    /**
     * This is the file extension used for both the private and the public key file
     */
    @SuppressWarnings("WeakerAccess")
    public static final String KEY_EXTENSION = ".pem";

    /**
     * The name of the private key file.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String DEFAULT_PRIVATE_KEY_FILE = "private_key" + KEY_EXTENSION;

    /**
     * The name of the public key file.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String DEFAULT_PUBLIC_KEY_FILE = "public_key" + KEY_EXTENSION;

    /**
     * The delimiting header in the private key file.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String PRIVATE_KEY_HEADER = "RSA PRIVATE KEY";

    /**
     * The delimiting header in the public key file.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String PUBLIC_KEY_HEADER = "PUBLIC KEY";

    private static final String ENCRYPTED_KEY_END_DELIMITER = "==";

    private static final String TRANSFORMATION = "RSA";
    private static final String ALGORITHM = "RSA";

    private static final int DEFAULT_KEY_LENGTH_IN_BITS = 2048;
    private static final int MIN_KEY_LENGTH_IN_BITS = 512;
    private static final int MAX_KEY_LENGTH_IN_BITS = 4096;

    private static final Cipher CIPHER;
    private static final KeyFactory KEY_FACTORY;

    private static final String USER_HOME = System.getProperty("user.home");

    private static final Path USER_HOME_PATH = Paths.get(USER_HOME);
    private static final Path DEFAULT_KEY_PATH = USER_HOME_PATH.resolve(Paths.get(DEFAULT_KEY_DIR));
    private static final Path DEFAULT_PRIVATE_KEY_PATH = DEFAULT_KEY_PATH.resolve(Paths.get(DEFAULT_PRIVATE_KEY_FILE));
    private static final Path DEFAULT_PUBLIC_KEY_PATH = DEFAULT_KEY_PATH.resolve(Paths.get(DEFAULT_PUBLIC_KEY_FILE));

    static {
        try {
            // Code compiles without using Bouncy Castle library, but key loading doesn't work with default provider.
            Security.addProvider(new BouncyCastleProvider());

            CIPHER = Cipher.getInstance(TRANSFORMATION);
            KEY_FACTORY = KeyFactory.getInstance(ALGORITHM);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("error loading cipher " + TRANSFORMATION + " or algorithm " + ALGORITHM);
        }
    }

    /**
     * Generate a set of keys using RSA and with length 2048 bits
     *
     * @return the keys generated
     * @throws CryptoException if the keys cannot be generated
     */
    @SuppressWarnings("WeakerAccess")
    public static KeyPair generateKeys() throws CryptoException {

        return generateKeys(DEFAULT_KEY_LENGTH_IN_BITS);
    }

    /**
     * Generate a set of keys using RSA and with an arbitrary length
     * The key length should be within 512 and 4096 bits
     *
     * @param key_length the length of the keys in bits
     * @return the keys generated
     * @throws CryptoException if the keys cannot be generated
     */
    @SuppressWarnings("WeakerAccess")
    public static KeyPair generateKeys(final int key_length) throws CryptoException {

        // RSA keys must be at least 512 bits long
        // Keys longer than 4096 bits can take too long to generate
        if (key_length < MIN_KEY_LENGTH_IN_BITS || key_length > MAX_KEY_LENGTH_IN_BITS)
            throw new CryptoException("Length of key is invalid");

        try {
            final KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
            generator.initialize(key_length);
            return generator.generateKeyPair();

        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Encrypts the given plain text string using the given public key, and MIME-encodes the result.
     *
     * @param public_key the public key
     * @param plain_text the plain text
     * @return the encrypted and MIME-encoded text
     * @throws CryptoException if the text cannot be encrypted
     */
    @SuppressWarnings("WeakerAccess")
    public static String encrypt(final PublicKey public_key, final String plain_text) throws CryptoException {

        try (final InputStream input_stream = new ByteArrayInputStream(plain_text.getBytes());
             final ByteArrayOutputStream output_stream = new ByteArrayOutputStream()) {

            encrypt(public_key, input_stream, output_stream);

            return new String(output_stream.toByteArray());

        } catch (final IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Decrypts the given encrypted and MIME-encoded text string using the given private key.
     *
     * @param private_key the private key
     * @param cipher_text the encrypted and MIME-encoded text
     * @return the plain text
     * @throws CryptoException if the decryption cannot be completed
     */
    @SuppressWarnings("WeakerAccess")
    public static String decrypt(final PrivateKey private_key, final String cipher_text) throws CryptoException {

        try (final InputStream input_stream = new ByteArrayInputStream(cipher_text.getBytes());
             final ByteArrayOutputStream output_stream = new ByteArrayOutputStream()) {

            decrypt(private_key, input_stream, output_stream);

            return new String(output_stream.toByteArray());

        } catch (final IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Encrypts the given plain text file to another file, using the given public key, and MIME-encodes the result.
     *
     * @param public_key       the public key
     * @param plain_text_path  the path of the plain text file
     * @param cipher_text_path the path of the resulting encrypted file
     * @throws CryptoException if the encryption cannot be completed
     * @throws IOException     if a file cannot be accessed
     */
    @SuppressWarnings("WeakerAccess")
    public static void encrypt(final PublicKey public_key, final Path plain_text_path, final Path cipher_text_path) throws CryptoException, IOException {

        try (final InputStream input_stream = Files.newInputStream(plain_text_path);
             final OutputStream output_stream = Files.newOutputStream(cipher_text_path)) {

            encrypt(public_key, input_stream, output_stream);
        }
    }

    /**
     * Decrypts the given encrypted and MIME-encoded text file to another file, using the given private key.
     *
     * @param private_key      the private key
     * @param cipher_text_path the path of the encrypted file
     * @param plain_text_path  the path of the resulting plain text file
     * @throws CryptoException if the encryption cannot be completed
     * @throws IOException     if a file cannot be accessed
     */
    @SuppressWarnings("WeakerAccess")
    public static void decrypt(final PrivateKey private_key, final Path cipher_text_path, final Path plain_text_path) throws CryptoException, IOException {

        try (final InputStream input_stream = Files.newInputStream(cipher_text_path);
             final OutputStream output_stream = Files.newOutputStream(plain_text_path)) {

            decrypt(private_key, input_stream, output_stream);
        }
    }

    /**
     * Encrypts the given plain text file, using the given public key, MIME-encodes the result, and outputs it to the given stream.
     *
     * @param public_key      the public key
     * @param plain_text_path the path of the plain text file
     * @param output_stream   the output stream for the resulting encrypted data
     * @throws CryptoException if the encryption cannot be completed
     * @throws IOException     if the plain text file cannot be accessed
     */
    @SuppressWarnings("unused")
    public static void encrypt(final PublicKey public_key, final Path plain_text_path, final OutputStream output_stream) throws CryptoException, IOException {

        try (final InputStream input_stream = Files.newInputStream(plain_text_path)) {

            encrypt(public_key, input_stream, output_stream);
        }
    }

    /**
     * Decrypts the given encrypted and MIME-encoded text file, using the given private key, and outputs it to the given stream.
     *
     * @param private_key      the private key
     * @param cipher_text_path the path of the encrypted file
     * @param output_stream    the output stream for the resulting data
     * @throws CryptoException if the encryption cannot be completed
     * @throws IOException     if the encrypted file cannot be accessed
     */
    @SuppressWarnings("unused")
    public static void decrypt(final PrivateKey private_key, final Path cipher_text_path, final OutputStream output_stream) throws CryptoException, IOException {

        try (final InputStream input_stream = Files.newInputStream(cipher_text_path)) {

            decrypt(private_key, input_stream, output_stream);
        }
    }

    /**
     * Encrypts the plain text read from the given stream, using the given public key, MIME-encodes the result, and outputs it to another given stream.
     *
     * @param public_key    the public key
     * @param input_stream  the input stream for the plain text
     * @param output_stream the output stream for the resulting encrypted data
     * @throws CryptoException if the encryption cannot be completed
     */
    @SuppressWarnings("WeakerAccess")
    public static void encrypt(final PublicKey public_key, final InputStream input_stream, final OutputStream output_stream) throws CryptoException {

        try {
            CIPHER.init(Cipher.ENCRYPT_MODE, public_key);

            final byte[] plain_text = FileManipulation.readAllBytes(input_stream);
            final byte[] encrypted = CIPHER.doFinal(plain_text);
            final byte[] mime_encoded = Base64.getMimeEncoder().encode(encrypted);

            output_stream.write(mime_encoded);

        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException | IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Decrypts the encrypted and MIME-encoded data read from the given stream, using the given private key, and outputs it to another given stream.
     *
     * @param private_key   the private key
     * @param input_stream  the input stream for the encrypted file
     * @param output_stream the output stream for the resulting data
     * @throws CryptoException if the encryption cannot be completed
     */
    @SuppressWarnings("WeakerAccess")
    public static void decrypt(final PrivateKey private_key, final InputStream input_stream, final OutputStream output_stream) throws CryptoException {

        try {
            CIPHER.init(Cipher.DECRYPT_MODE, private_key);

            final byte[] mime_encoded = FileManipulation.readAllBytes(input_stream);
            final byte[] encrypted = Base64.getMimeDecoder().decode(mime_encoded);
            final byte[] plain_text = CIPHER.doFinal(encrypted);

            output_stream.write(plain_text);

        } catch (BadPaddingException | InvalidKeyException | IllegalBlockSizeException | IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Verifies that the pair of Public and Private keys is valid
     *
     * @param public_key  of the key pair
     * @param private_key of the key pair
     * @return true if the key pair is valid
     * @throws CryptoException if unable to verify the key pair
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean verifyKeyPair(final PublicKey public_key, final PrivateKey private_key) throws CryptoException {

        SecureRandom random = new SecureRandom();
        String randomChallenge = new BigInteger(130, random).toString(32);

        String encryptedChallenge = encrypt(public_key, randomChallenge);

        try {
            String decryptedChallenge = decrypt(private_key, encryptedChallenge);

            // Checking the result of the challenge is not strictly needed, as a not-valid key pair will result in a
            // CryptoException.
            // Checking the challenge however provides a more rigid and clean key-pair verification.
            return decryptedChallenge.equals(randomChallenge);

        } catch (CryptoException e) {
            return false;
        }
    }

    /**
     * Gets this user's private key.
     * The key is assumed to be stored in the file {@value #DEFAULT_PRIVATE_KEY_FILE} in the directory {@value #DEFAULT_KEY_DIR} in
     * this user's home directory.
     *
     * @return this user's private key
     * @throws CryptoException if the private key cannot be accessed
     */
    @SuppressWarnings("WeakerAccess")
    public static PrivateKey getPrivateKey() throws CryptoException {

        return getPrivateKey(DEFAULT_PRIVATE_KEY_PATH);
    }

    /**
     * Gets a private key from a given file.
     *
     * @param key_path the path of the private key file
     * @return the private key
     * @throws CryptoException if the private key cannot be accessed
     */
    @SuppressWarnings("WeakerAccess")
    public static PrivateKey getPrivateKey(final Path key_path) throws CryptoException {

        String key = getKey(key_path);
        return getPrivateKeyFromPEMString(key);
    }

    /**
     * Gets this user's public key.
     * The key is assumed to be stored in the file {@value #DEFAULT_PUBLIC_KEY_FILE} in the directory {@value #DEFAULT_KEY_DIR} in
     * this user's home directory.
     *
     * @return this user's public key
     * @throws CryptoException if the public key cannot be accessed
     */
    @SuppressWarnings("unused")
    public static PublicKey getPublicKey() throws CryptoException {

        return getPublicKey(DEFAULT_PUBLIC_KEY_PATH);
    }

    /**
     * Gets a public key from a given file.
     *
     * @param key_path the path of the public key file
     * @return the public key
     * @throws CryptoException if the public key cannot be accessed
     */
    @SuppressWarnings("WeakerAccess")
    public static PublicKey getPublicKey(final Path key_path) throws CryptoException {

        return getPublicKeyFromPEMString(getKey(key_path));
    }

    /**
     * Gets a private key from a string. The string is assumed to be in PEM format, with BEGIN and END delimiters {@value #PRIVATE_KEY_HEADER}
     *
     * @param key_in_pem_format the private key in PEM format
     * @return the private key
     * @throws CryptoException if the private key cannot be extracted
     */
    @SuppressWarnings("WeakerAccess")
    public static PrivateKey getPrivateKeyFromPEMString(final String key_in_pem_format) throws CryptoException {

        final String base64_encoded_private_key = stripKeyDelimiters(key_in_pem_format, PRIVATE_KEY_HEADER);
        return getPrivateKeyFromString(base64_encoded_private_key);
    }

    /**
     * Gets a private key from a string. The string is assumed to be in base64.
     *
     * @param key_base64 the private key in PEM format
     * @return the private key
     * @throws CryptoException if the private key cannot be extracted
     */
    @SuppressWarnings("WeakerAccess")
    public static PrivateKey getPrivateKeyFromString(final String key_base64) throws CryptoException {

        try {
            final byte[] private_key = Base64.getMimeDecoder().decode(key_base64);

            return KEY_FACTORY.generatePrivate(new PKCS8EncodedKeySpec(private_key));

        } catch (final InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Gets a public key from a string. The string is assumed to be in PEM format, with BEGIN and END delimiters {@value #PUBLIC_KEY_HEADER}
     *
     * @param key_in_pem_format the public key in PEM format
     * @return the public key
     * @throws CryptoException if the public key cannot be extracted
     */
    @SuppressWarnings("WeakerAccess")
    public static PublicKey getPublicKeyFromPEMString(final String key_in_pem_format) throws CryptoException {

        final String base64_encoded_public_key = stripKeyDelimiters(key_in_pem_format, PUBLIC_KEY_HEADER);
        return getPublicKeyFromString(base64_encoded_public_key);
    }

    /**
     * Gets a public key from a string. The string is assumed to be in base64.
     *
     * @param key_base64 the public key in base64
     * @return the public key
     * @throws CryptoException if the public key cannot be extracted
     */
    @SuppressWarnings("WeakerAccess")
    public static PublicKey getPublicKeyFromString(final String key_base64) throws CryptoException {

        try {
            final byte[] public_key = Base64.getMimeDecoder().decode(key_base64);

            return KEY_FACTORY.generatePublic(new X509EncodedKeySpec(public_key));

        } catch (final InvalidKeySpecException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Convert a given key to its string representation in base64
     *
     * @param key to be converted
     * @return the string version of the key in base64
     */
    @SuppressWarnings("WeakerAccess")
    public static String keyToBase64(Key key) {

        return Base64.getMimeEncoder().encodeToString(key.getEncoded())
                .replace("\n", "")
                .replace("\r", "");
    }

    /**
     * Loads a list of public keys from the given file containing keys in PEM format.
     *
     * @param path the file containing public keys
     * @return a list of keys in PEM format
     * @throws IOException if the file cannot be accessed
     */
    @SuppressWarnings("WeakerAccess")
    public static List<PublicKey> loadPublicKeys(final Path path) throws IOException, CryptoException {

        final List<PublicKey> key_list = new ArrayList<>();

        try (final BufferedReader reader = new BufferedReader(FileManipulation.getInputStreamReader(path))) {

            StringBuilder builder = null;

            String line;
            while ((line = reader.readLine()) != null) {

                if (line.contains(PUBLIC_KEY_HEADER)) {

                    if (builder != null) {

                        builder.append(line);
                        PublicKey public_key = getPublicKeyFromPEMString(builder.toString());
                        key_list.add(public_key);
                        builder = null;

                    } else {

                        builder = new StringBuilder();
                        builder.append(line);
                        builder.append("\n");
                    }

                } else {

                    if (builder != null) {
                        builder.append(line);
                        builder.append("\n");
                    }
                }
            }
        }

        return key_list;
    }

    /**
     * Persist a key pair to the specified paths for the private and the public key
     *
     * @param key_pair             to be persisted
     * @param private_key_filename the path for the private key
     * @param public_key_filename  the path for the public key
     * @throws CryptoException if the keys could not be persisted
     */
    @SuppressWarnings("WeakerAccess")
    public static void persist(final KeyPair key_pair, final Path private_key_filename, final Path public_key_filename) throws CryptoException {

        try {

            writePemFile(key_pair.getPrivate(), PRIVATE_KEY_HEADER, extension(private_key_filename, KEY_EXTENSION));
            writePemFile(key_pair.getPublic(), PUBLIC_KEY_HEADER, extension(public_key_filename, KEY_EXTENSION));

        } catch (final IOException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Attempts to extract an AES key from a file, in which
     * each line in the input stream is assumed to contain a MIME-encoded AES key, encrypted with a particular user's
     * RSA public key. This method attempts to decrypt each one with this user's RSA private key, and returns the first
     * one to be successfully decrypted.
     *
     * @param encrypted_keys the file containing encrypted keys
     * @return the decrypted AES key
     * @throws IOException     if the input stream cannot be read
     * @throws CryptoException if no key can be successfully decrypted
     */
    @SuppressWarnings("unused")
    public static SecretKey getAESKey(final Path encrypted_keys) throws IOException, CryptoException {

        try (final InputStream encrypted_key_stream = Files.newInputStream(encrypted_keys)) {
            return getAESKey(encrypted_key_stream);
        }
    }

    /**
     * Attempts to extract an AES key from an input stream, in which
     * each line in the input stream is assumed to contain a MIME-encoded AES key, encrypted with a particular user's
     * RSA public key. This method attempts to decrypt each one with this user's RSA private key, and returns the first
     * one to be successfully decrypted.
     *
     * @param encrypted_key_stream the input stream containing encrypted keys
     * @return the decrypted AES key
     * @throws IOException     if the input stream cannot be read
     * @throws CryptoException if no key can be successfully decrypted
     */
    @SuppressWarnings("WeakerAccess")
    public static SecretKey getAESKey(final InputStream encrypted_key_stream) throws IOException, CryptoException {

        // SecretKey represents a symmetric key, whereas PrivateKey represents a private asymmetric key.

        final PrivateKey private_key = getPrivateKey();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(encrypted_key_stream))) {

            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {

                builder.append(line);

                if (line.endsWith(ENCRYPTED_KEY_END_DELIMITER)) {

                    try {
                        return SymmetricEncryption.getKey(decrypt(private_key, builder.toString()));

                    } catch (final CryptoException e) {

                        // Couldn't decrypt, try the next one.
                        builder = new StringBuilder();
                    }
                }
            }

            throw new CryptoException("No valid encrypted key");
        }
    }

    /**
     * Encrypts the AES key with the given public key
     *
     * @param public_key the key used to perform the encryption
     * @param AES_key    the key to encrypt
     * @return the encrypted key as a String
     * @throws CryptoException if the AES key could not be encrypted
     */
    @SuppressWarnings("WeakerAccess")
    public static String encryptAESKey(final PublicKey public_key, final SecretKey AES_key) throws CryptoException {

        return encrypt(public_key, SymmetricEncryption.keyToString(AES_key)) + "\n";
    }

    @SuppressWarnings("unused")
    public static void encryptAESKey(final SecretKey AES_key, final Path authorized_keys_path, final Path destination_path) throws IOException, CryptoException {

        List<String> original_contents = FileManipulation.readAllLines(Files.newInputStream(authorized_keys_path));

        try (OutputStreamWriter writer = FileManipulation.getOutputStreamWriter(destination_path)) {

            try {
                final List<PublicKey> public_keys = loadPublicKeys(authorized_keys_path);

                for (final PublicKey public_key : public_keys) {
                    writeEncryptedAESKey(public_key, AES_key, writer);
                }

            } catch (IOException | CryptoException e) {

                for (String line : original_contents) {
                    writer.write(line + "\n");
                }
                throw e;
            } finally {
                writer.flush();
            }
        }
    }

    /**
     * Decrypts the AES key with the given private key
     *
     * @param private_key   used to decrypt the key
     * @param encrypted_key the AES key that was encrypted using the matching public key
     * @return the AES key
     * @throws CryptoException if the AES key could not be decrypted
     */
    @SuppressWarnings("WeakerAccess")
    public static SecretKey decryptAESKey(final PrivateKey private_key, final String encrypted_key) throws CryptoException {

        return SymmetricEncryption.getKey(decrypt(private_key, encrypted_key));
    }

    private static void writeEncryptedAESKey(final PublicKey public_key, final SecretKey AES_key, final OutputStreamWriter writer) throws IOException, CryptoException {

        writer.append(encryptAESKey(public_key, AES_key));
        writer.append("\n");
    }

    private static String getKey(final Path key_path) throws CryptoException {

        // This used to use Files.readAllBytes() which was simpler, but didn't work
        // on Windows with the different newline encoding. This way the key string
        // contains Java newlines whatever platform we're running on.

        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = Files.newBufferedReader(key_path)) {

            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }

            return builder.toString();

        } catch (final IOException e) {
            throw new CryptoException("Can't access key file: " + key_path);
        }
    }
}
