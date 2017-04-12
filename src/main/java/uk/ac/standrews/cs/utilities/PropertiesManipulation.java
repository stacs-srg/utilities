/***************************************************************************
 *                                                                         *
 * nds Library                                                             *
 * Copyright (C) 2005-2010 Distributed Systems Architecture Research Group *
 * University of St Andrews, Scotland                                      *
 * http://www-systems.cs.st-andrews.ac.uk/                                 *
 *                                                                         *
 * This file is part of nds, a package of utility classes.                 *
 *                                                                         *
 * nds is free software: you can redistribute it and/or modify             *
 * it under the terms of the GNU General Public License as published by    *
 * the Free Software Foundation, either version 3 of the License, or       *
 * (at your option) any later version.                                     *
 *                                                                         *
 * nds is distributed in the hope that it will be useful,                  *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of          *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
 * GNU General Public License for more details.                            *
 *                                                                         *
 * You should have received a copy of the GNU General Public License       *
 * along with nds.  If not, see <http://www.gnu.org/licenses/>.            *
 *                                                                         *
 ***************************************************************************/
package uk.ac.standrews.cs.utilities;

import java.io.*;
import java.util.Properties;
import java.util.Set;

/**
 * Wrapper for java properties class, which make it easier to create, load, and save properties. A simplified version
 * of H2OPropertiesWrapper, without the need to explicitly load or save.
 *
 * @author Angus Macdonald (angus AT cs.st-andrews.ac.uk)
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class PropertiesManipulation {

    private final Properties properties;
    private final File properties_file;

    private FileOutputStream output_stream = null;
    private FileInputStream input_stream = null;

    /**
     * Creates a properties wrapper for the given file path.
     *
     * @param properties_file the file to contain the properties
     * @throws IOException if the file could not be created or the properties could not be loaded from it
     */
    public PropertiesManipulation(final File properties_file) throws IOException {

        this.properties_file = properties_file;
        properties = new Properties();

        createFileIfNecessary();
        loadProperties();
    }

    /**
     * Gets the property associated with a given key.
     *
     * @param key a key
     * @return the corresponding property
     */
    public String getProperty(final String key) {

        return properties.getProperty(key);
    }

    /**
     * Sets the property for a given key, and saves to the backing file.
     *
     * @param key   a key
     * @param value the new value to be associated with the key
     * @throws IOException if the file cannot be saved
     */
    public void setProperty(final String key, final String value) throws IOException {

        properties.setProperty(key, value);
        save();
    }

    /**
     * Returns the set of the keys in this properties file.
     *
     * @return the set of the keys in this properties file
     */
    public Set<Object> getKeys() {

        return properties.keySet();
    }

    public static synchronized Properties getProperties(final String properties_path_string) throws IOException {

        try (InputStream stream = PropertiesManipulation.class.getClassLoader().getResourceAsStream(properties_path_string)) {

            Properties properties = new Properties();
            properties.load(stream);
            return properties;
        }
    }

    // -------------------------------------------------------------------------------------------------------

    private void createFileIfNecessary() throws IOException {

        if (!properties_file.exists()) {

            // Create any directories specified in the path, if necessary.
            if (properties_file.getParentFile() != null) {
                if (!properties_file.getParentFile().mkdirs()) {
                    throw new IOException();
                }
            }

            // Create the properties file.
            if (!properties_file.createNewFile()) {
                throw new IOException();
            }
        }
    }

    private void loadProperties() throws IOException {

        input_stream = new FileInputStream(properties_file);

        properties.load(input_stream);
    }

    private void save() throws IOException {

        if (output_stream == null) {
            output_stream = new FileOutputStream(properties_file);
        }

        properties.store(output_stream, "Properties File");
        close();
    }

    private void close() throws IOException {

        if (output_stream != null) {
            output_stream.close();
        }
        if (input_stream != null) {
            input_stream.close();
        }

        output_stream = null;
        input_stream = null;
    }
}
