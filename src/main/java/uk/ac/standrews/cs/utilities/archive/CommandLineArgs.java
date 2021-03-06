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
package uk.ac.standrews.cs.utilities.archive;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility for manipulating command line arguments.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
@SuppressWarnings("unused")
public final class CommandLineArgs {

    /**
     * Parses the given command line arguments into a map from flags to values.
     * Each argument is assumed to be of the form
     * <p>
     * -xvalue
     * <p>
     * which would result in a map entry from "-x" to "value"
     *
     * @param args the command line arguments
     * @return a map from flags to values
     */
    @SuppressWarnings("WeakerAccess")
    public static Map<String, String> parseCommandLineArgs(final String[] args) {

        final Map<String, String> map = new HashMap<>();

        for (final String element : args) {
            if (element.length() > 1) {

                final String flag = element.substring(0, 2);
                final String value = element.substring(2);
                map.put(flag, value);
            }
        }

        return map;
    }

    /**
     * Extracts the argument value for the given flag from the given command line arguments.
     *
     * @param args the command line arguments
     * @param flag the required flag
     * @return the value for the given flag, or null if not found
     */
    @SuppressWarnings("WeakerAccess")
    public static String getArg(final String[] args, final String flag) {

        return parseCommandLineArgs(args).get(flag);
    }

    /**
     * Extracts the argument value for the given flag from the given command line arguments.
     *
     * @param args          the command line arguments
     * @param flag          the required flag
     * @param default_value the value to be returned if the flag is not found
     * @return the value for the given flag, or null if not found
     */
    public static String getArg(final String[] args, final String flag, final String default_value) {

        String value = parseCommandLineArgs(args).get(flag);
        return value != null ? value : default_value;
    }

    /**
     * Checks for the presence of the given flag in the command line arguments.
     *
     * @param args the command line arguments
     * @param flag the required flag
     * @return true if the given flag is found
     */
    public static boolean containsArg(final String[] args, final String flag) {

        return getArg(args, flag) != null;
    }

    /**
     * Extracts an integer value for the given flag from the given command line arguments.
     *
     * @param args          the command line arguments
     * @param flag          the required flag
     * @param default_value the value to be returned if the flag is not found
     * @return the resulting value
     */
    public static int extractIntFromCommandLineArgs(final String[] args, final String flag, final int default_value) {

        final String value_string = getArg(args, flag);

        try {
            if (value_string != null) {
                return Integer.parseInt(value_string);
            }
            return default_value;
        } catch (final NumberFormatException e) {
            return default_value;
        }
    }
}
