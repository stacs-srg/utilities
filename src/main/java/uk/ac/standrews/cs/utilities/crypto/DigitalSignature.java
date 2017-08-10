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

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static uk.ac.standrews.cs.utilities.crypto.PemFile.*;
import static uk.ac.standrews.cs.utilities.crypto.Utils.extension;

/**
 * This utility class can be used to digitally sign text/data and to verify the signature.
 * The digital signature is generated using the RSA algorithm.
 *
 * To perform digital signature operations you need a PRIVATE_KEY and a CERTIFICATE (public), which they can be generated
 * via the {@method #generateKeys()}, {@method #generateKeys(int length)} methods.
 *
 *
 * Here is an example of using the digital signature algorithm via openssl:
 *
 * <pre>{@code
 * openssl req -nodes -x509 -sha1 -newkey rsa:4096 -keyout "KEY.key" -out "CERTIFICATE.crt"
 * echo "Hello, World" > sign.txt
 * openssl dgst -sha1 -sign "KEY.key" -out signed sign.txt
 * openssl dgst -sha1 -verify  <(openssl x509 -in "CERTIFICATE.crt"  -pubkey -noout) -signature signed sign.txt
 * }</pre>
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
public class DigitalSignature {

    private static final String RSA_ALGORITHM = "RSA";
    private static final String PROVIDER = "BC";
    private static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
    private static final int DEFAULT_KEY_LENGTH_IN_BITS = 512;
    private static final int MIN_KEY_LENGTH_IN_BITS = 512;
    private static final int MAX_KEY_LENGTH_IN_BITS = 4096;

    private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
    private static final String SECURE_RANDOM_PROVIDER = "SUN";

    // http://www.zytrax.com/tech/survival/ssl.html#pem-ids

    /**
     * The delimiting header in the private key file.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String PRIVATE_KEY_HEADER = "RSA PRIVATE KEY";

    /**
     * The delimiting header in the certificate file.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String CERTIFICATE_HEADER = "CERTIFICATE";

    /**
     * The extension for the private key
     */
    @SuppressWarnings("WeakerAccess")
    public static final String PRIVATE_KEY_EXTENSION = ".key";

    /**
     * The extension for the certificate of the signature
     */
    @SuppressWarnings("WeakerAccess")
    public static final String CERTIFICATE_EXTENSION = ".crt";

    static {
        // Code compiles without using Bouncy Castle library, but key loading doesn't work with default provider.
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Generates a pair of keys with the default key length
     *
     * @return a pair of keys
     * @throws CryptoException if the keys could not be generated
     */
    @SuppressWarnings("WeakerAccess")
    public static KeyPair generateKeys() throws CryptoException {

        return generateKeys(DEFAULT_KEY_LENGTH_IN_BITS);
    }

    /**
     * Generates a pair of keys with the specified key length
     *
     * @return a pair of keys
     * @throws CryptoException if the keys could not be generated
     */
    @SuppressWarnings("WeakerAccess")
    public static KeyPair generateKeys(final int key_length) throws CryptoException {

        if (key_length < MIN_KEY_LENGTH_IN_BITS || key_length > MAX_KEY_LENGTH_IN_BITS) throw new CryptoException("Length of key is invalid");

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(RSA_ALGORITHM, PROVIDER);
            SecureRandom random = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM, SECURE_RANDOM_PROVIDER);

            keyGen.initialize(key_length, random);
            return keyGen.generateKeyPair();

        } catch (final NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Sign some text using the given key
     *
     * @param private_key to be used to sign the text
     * @param plain_text in plain to be signed.
     * @return the signed text
     * @throws CryptoException if the text could not be signed
     */
    @SuppressWarnings("WeakerAccess")
    public static byte[] sign(final PrivateKey private_key, final String plain_text) throws CryptoException {

        byte[] retval;
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);

            signature.initSign(private_key);
            signature.update(plain_text.getBytes());
            retval = signature.sign();

        } catch (final NoSuchAlgorithmException | NoSuchProviderException | SignatureException | InvalidKeyException e) {
            throw new CryptoException(e);
        }
        return retval;
    }

    /**
     * Sign some text using the given key. The returned signature will be in base64
     *
     * @param private_key to be used to sign the text
     * @param plain_text in plain to be signed.
     * @return the signed text in base64
     * @throws CryptoException if the text could not be signed
     */
    @SuppressWarnings("WeakerAccess")
    public static String sign64(final PrivateKey private_key, final String plain_text) throws CryptoException {

        byte[] signatureBytes = sign(private_key, plain_text);
        byte[] encodedBytes = Base64.encodeBase64(signatureBytes);
        return new String(encodedBytes);
    }

    /**
     * Verify the signature against the text with the given public key
     *
     * @param public_key used to verify the signed text
     * @param plain_text that was signed
     * @param signature_to_verify signature to be verified
     * @return true if the text was verified
     * @throws CryptoException if the signature could not be verified
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean verify(final PublicKey public_key, final String plain_text, final byte[] signature_to_verify) throws CryptoException {

        boolean isValid;
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
            signature.initVerify(public_key);
            signature.update(plain_text.getBytes());
            isValid = signature.verify(signature_to_verify);

        } catch (final NoSuchAlgorithmException | NoSuchProviderException | SignatureException | InvalidKeyException e) {
            throw new CryptoException(e);
        }

        return isValid;
    }

    /**
     * Verify the signature in base64 against the text with the given public key
     *
     * @param public_key used to verify the signed text
     * @param plain_text that was signed
     * @param signature64_to_verify signature to be verified. This must be in base64
     * @return true if the text was verified
     * @throws CryptoException if the signature could not be verified
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean verify64(final PublicKey public_key, final String plain_text, final String signature64_to_verify) throws CryptoException {

        byte[] decodedBytes = Base64.decodeBase64(signature64_to_verify);
        return verify(public_key, plain_text, decodedBytes);
    }

    /**
     * Verifies that the pair of Public and Private keys is valid
     *
     * @param public_key of the key pair
     * @param private_key of the key pair
     * @return true if the key pair is valid
     * @throws CryptoException if unable to verify the key pair
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean verifyKeyPair(final PublicKey public_key, final PrivateKey private_key) throws CryptoException {

        SecureRandom random = new SecureRandom();
        String randomChallenge = new BigInteger(130, random).toString(32);

        byte[] signatue = sign(private_key, randomChallenge);
        return verify(public_key, randomChallenge, signatue);

    }

    /**
     * Get the private key stored at the given path
     *
     * @param key_path where the private key is stored
     * @return the private key
     * @throws CryptoException if the private key could not be loaded
     */
    @SuppressWarnings("WeakerAccess")
    public static PrivateKey getPrivateKey(final Path key_path) throws CryptoException {

        try {
            String inString = stripKeyDelimiters(getKey(key_path), PRIVATE_KEY_HEADER);
            byte[] data = Base64.decodeBase64(inString.getBytes());

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(data);
            KeyFactory kf = KeyFactory.getInstance(RSA_ALGORITHM);
            return kf.generatePrivate(keySpec);

        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CryptoException(e);
        }

    }

    /**
     * Get the certificate stored at the given path
     *
     * @param key_path where the certificate is stored
     * @return the certificate (public)
     * @throws CryptoException if the certificate could not be loaded
     */
    @SuppressWarnings("WeakerAccess")
    public static PublicKey getCertificate(final Path key_path) throws CryptoException {

        try {
            String inString = stripKeyDelimiters(getKey(key_path), CERTIFICATE_HEADER);
            byte[] data = Base64.decodeBase64(inString.getBytes());

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(data);
            KeyFactory kf = KeyFactory.getInstance(RSA_ALGORITHM);
            return  kf.generatePublic(keySpec);

        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CryptoException(e);
        }

    }

    /**
     * Get a certificate given its string representation.
     * The string representation has not headers.
     *
     * @param certificate to be cast in base64
     * @return the certificate as a key
     * @throws CryptoException if the certificate cannot be cast
     */
    @SuppressWarnings("WeakerAccess")
    public static PublicKey getCertificate(final String certificate) throws CryptoException {

        try {
            byte[] data = Base64.decodeBase64(certificate.getBytes());

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(data);
            KeyFactory kf = KeyFactory.getInstance(RSA_ALGORITHM);
            return  kf.generatePublic(keySpec);

        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CryptoException(e);
        }

    }

    /**
     * Get the string representation, in base64, of a given certificate
     *
     * @param certificate to be cast
     * @return the certificate as a string in base64
     * @throws CryptoException if the certificate cannot be cast to a string
     */
    @SuppressWarnings("WeakerAccess")
    public static String getCertificateString(final PublicKey certificate) throws CryptoException {

        byte[] encodedBytes = Base64.encodeBase64(certificate.getEncoded());
        return new String(encodedBytes);
    }

    /**
     * Persist a key pair to the specified paths for the private and the public key
     *
     * @param key_pair to be persisted
     * @param private_key_filename the path for the private key
     * @param certificate_filename the path for the certificate
     * @throws CryptoException if the keys could not be persisted
     */
    @SuppressWarnings("WeakerAccess")
    public static void persist(final KeyPair key_pair, final Path private_key_filename, final Path certificate_filename) throws CryptoException {

        try {

            writePemFile(key_pair.getPrivate(), PRIVATE_KEY_HEADER, extension(private_key_filename, PRIVATE_KEY_EXTENSION));
            writePemFile(key_pair.getPublic(), CERTIFICATE_HEADER, extension(certificate_filename, CERTIFICATE_EXTENSION));

        } catch (final IOException e) {
            throw new CryptoException(e);
        }
    }
}
