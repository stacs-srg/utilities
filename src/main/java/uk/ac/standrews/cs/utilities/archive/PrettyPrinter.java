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
package uk.ac.standrews.cs.utilities.archive;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Contains various utility methods that make it easier to print the contents of data structures such as collections, arrays, and maps.
 *
 * @author Angus Macdonald (angus.macdonald@st-andrews.ac.uk)
 */
@SuppressWarnings("unused")
public final class PrettyPrinter {

    private static final String DEFAULT_SEPARATOR_CHAR = ", ";

    /**
     * Concatenates representations of the full contents of a collection. Calls {@link #toString()} on each element.
     *
     * @param collection a collection
     * @return a concatenated representation of the collection elements
     */
    public static String toString(final Collection<?> collection) {

        return toString(collection, DEFAULT_SEPARATOR_CHAR);
    }

    /**
     * Concatenates representations of the full contents of a collection. Calls {@link #toString()} on each element.
     *
     * @param collection    a collection
     * @param separatorChar The character to be used to separate entries.
     * @return a concatenated representation of the collection elements
     */
    public static String toString(final Collection<?> collection, final String separatorChar) {

        return toString(collection, separatorChar, true);
    }

    /**
     * Concatenates representations of the full contents of a collection. Calls {@link #toString()} on each element.
     *
     * @param collection           a collection
     * @param separatorChar        The character to be used to separate entries.
     * @param surroundWithBrackets If true, surround the collection with "Collection [ ... ]"
     * @return a concatenated representation of the collection elements
     */
    public static String toString(final Collection<?> collection, final String separatorChar, final boolean surroundWithBrackets) {

        if (collection == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        if (surroundWithBrackets) {
            sb.append("Collection [");
        }

        boolean first = true;
        for (final Object element : collection) {
            if (!first) {
                sb.append(separatorChar);
            }
            sb.append(toString(element));
            first = false;
        }

        if (surroundWithBrackets) {
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * Concatenates {@link #toString()} representations of the full contents of an array. Calls {@link #toString()} on each element.
     * <p>
     * <p>Uses the ({@value #DEFAULT_SEPARATOR_CHAR}) and surrounds the result with brackets. For more customization use
     * {@link #toString(Object[], String, boolean)}.
     *
     * @param array an array
     * @return a concatenated representation of the array elements
     */
    public static String toString(final Object[] array) {

        return toString(array, DEFAULT_SEPARATOR_CHAR, true);
    }

    /**
     * Concatenates {@link #toString()} representations of the full contents of an array. Calls {@link #toString()} on each element. Allows you to specify
     * the character which separates each entry.
     * <p>
     * <p>Uses surrounds the result with brackets. For more customization use
     * {@link #toString(Object[], String, boolean)}.
     *
     * @param array         an array
     * @param separatorChar The character used to separate array object.
     * @return a concatenated representation of the array elements
     */
    public static String toString(final Object[] array, final String separatorChar) {

        return toString(array, separatorChar, true);
    }

    /**
     * Concatenates {@link #toString()} representations of the full contents of an array. Calls {@link #toString()} on each element. Allows you
     * to specify the separator character to be used, and whether results are surrounded by brackets.
     *
     * @param array                an array
     * @param separatorChar        The character used to separate array object.
     * @param surroundWithBrackets Whether the contents of the array should be surrounded with 'Array [ ... ]'.
     * @return a concatenated representation of the array elements
     */
    public static String toString(final Object[] array, final String separatorChar, final boolean surroundWithBrackets) {

        if (array == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();

        if (surroundWithBrackets) {
            sb.append("Array [");
        }

        boolean first = true;
        for (final Object element : array) {
            if (!first) {
                sb.append(separatorChar);
            }
            sb.append(element);
            first = false;
        }

        if (surroundWithBrackets) {
            sb.append("]");
        }

        return sb.toString();
    }

    /**
     * Concatenates representations of the full contents of a map. Calls {@link #toString()} on each element.
     *
     * @param map a map
     * @return a concatenated representation of the map keys and values
     */
    public static String toString(final Map<?, ?> map) {

        if (map == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("Map [");

        boolean first = true;
        for (final Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append("\n");
            }
            sb.append("Key: ").append(entry.getKey()).append("\t\tValue").append(entry.getValue());
            first = false;
        }

        sb.append("]");

        return sb.toString();
    }

    /**
     * Returns the result of {@link #toString()} on the parameter. This method is able to print out the contents of arrays that are typed as objects. If the object isn't an array it just calls {@link #toString()}.
     *
     * @param object an object
     * @return the result of the {@link #toString()} method.
     */
    public static String toString(final Object object) {

        if (object == null) {
            return null;
        } else if (object.getClass().isArray()) {
            return toStringArrayAsObject(object);
        } else {
            return object.toString();
        }
    }

    /**
     * Get the contents of an array if it is typed as an object.
     *
     * @param array An array.
     * @return The 'toString' representation of the array. NULL if it isn't an array.
     */
    private static String toStringArrayAsObject(final Object array) {

        assert array.getClass().isArray() : "This method assumes that the object being passed in is an array.";

        final StringBuilder sb = new StringBuilder();

        sb.append("Array [");

        boolean first = true;
        for (int i = 0; i < Array.getLength(array); i++) {
            if (!first) {
                sb.append(DEFAULT_SEPARATOR_CHAR);
            }
            sb.append(toString(Array.get(array, i)));
            first = false;
        }

        sb.append("]");

        return sb.toString();
    }
}
