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

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;

/**
 * Utility methods to handle a Pem file
 *
 * @author Simone I. Conte "sic2@st-andrews.ac.uk"
 */
class PemFile {

    private PemObject pemObject;

    private PemFile(Key key, String description) {
        this.pemObject = new PemObject(description, key.getEncoded());
    }

    private void write(Path filename) throws IOException {

        try (PemWriter pemWriter = new PemWriter(new OutputStreamWriter(Files.newOutputStream(filename)))) {

            pemWriter.writeObject(this.pemObject);
        }
    }

    /**
     * Write the key to the specified path
     *
     * @param key to be written
     * @param header the header for the file
     * @param filename where to write the file
     * @throws IOException if the file could not be created
     */
    static void writePemFile(Key key, String header, Path filename) throws IOException {

        // Make sure that the path exists
        File file = filename.toFile();
        file.getParentFile().mkdirs();

        PemFile pemFile = new PemFile(key, header);
        pemFile.write(filename);
    }

    /**
     * Remove the headers from the key
     *
     * @param key_in_pem_format the key
     * @param header the header to be removed (e.g. for PRIVATE KEY, it will remove -----BEGIN PRIVATE----- and -----END PRIVATE-----)
     * @return the stripped key
     */
    static String stripKeyDelimiters(final String key_in_pem_format, String header) {

        String beginHeader = "-----BEGIN " + header + "-----";
        String endHeader = "-----END " + header + "-----";

        return key_in_pem_format.replace(beginHeader + "\n", "").replace(endHeader, "");
    }

    /**
     * Get the key stored at the given path
     *
     * @param key_path where the key is stored
     * @return the key in String format
     * @throws CryptoException if the key could not be loaded
     */
    static String getKey(final Path key_path) throws CryptoException {

        try {
            return new String(Files.readAllBytes(key_path));

        } catch (final IOException e) {
            throw new CryptoException("Can't access key file: " + key_path);
        }
    }
}
