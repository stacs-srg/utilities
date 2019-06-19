/*
 * Copyright 2019 Systems Research Group, University of St Andrews:
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
package uk.ac.standrews.cs.utilities.network;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class UriUtil {

    /**
     * Replaces illegal URI characters with 3 chart UTF-8 encoding.
     *
     * @param s the string to be encoded
     * @return the corresponding legal URI string
     */
    public static String uriEncode(String s) throws UnsupportedEncodingException {

        String result = URLEncoder.encode(s, "UTF-8");

        // Above replaces spaces with + characters, and escapes /.

        // Replace + with %20.
        result = result.replaceAll("\\+", "%20");

        // Replace %2F with /.
        result = result.replaceAll("%2F", "/");

        return result;
    }

    /**
     * Creates a local path URI from a given parent and base name. This assumes an absolute path and ignores
     * scheme, host, port etc.
     *
     * @param parent_uri     the parent URI
     * @param base_name      the base name
     * @param trailing_slash true if the new URI should include a trailing slash
     * @return the new URI
     */
    public static URI childUri(URI parent_uri, String base_name, boolean trailing_slash) throws UnsupportedEncodingException, URISyntaxException {

        String uri_path = parent_uri.toString();

        if (!uri_path.endsWith("/")) uri_path += "/";

        uri_path += uriEncode(base_name);

        if (trailing_slash) uri_path += "/";

        return stringToUri(uri_path);
    }

    /**
     * Creates a  local path URI corresponding to the parent of a given URI. This assumes an absolute path and ignores
     * scheme, host, port etc.
     *
     * @param uri the child URI
     * @return the parent URI
     */
    public static URI parentUri(URI uri) throws UnsupportedEncodingException, URISyntaxException {

        StringBuilder parent_path = new StringBuilder("/");

        Iterator element_iterator = pathElementIterator(uri);

        String previous;
        String current = "";

        while (element_iterator.hasNext()) {
            previous = current;
            current = (String) element_iterator.next();
            if (!parent_path.toString().equals("/")) parent_path.append("/");
            parent_path.append(UriUtil.uriEncode(previous));
        }

        return stringToUri(parent_path.toString());
    }

    /**
     * Returns the base name of the path of the given URI.
     *
     * @param uri the URI
     * @return the base name of the URI
     */
    public static String baseName(URI uri) {

        String base_name = "";
        Iterator element_iterator = pathElementIterator(uri);

        while (element_iterator.hasNext()) base_name = (String) element_iterator.next();

        return base_name;
    }

    /**
     * Returns an iterator over the names in the path of the given URI.
     *
     * @param uri the URI
     * @return an iterator over the names in the path of the URI
     */
    public static Iterator pathElementIterator(URI uri) {

        String path = uri.normalize().getPath();

        if (path.startsWith("/")) path = path.substring(1);

        String[] path_elements = path.split("/");

        List path_as_list = Arrays.asList(path_elements);

        while (path_as_list.size() > 0 && path_as_list.get(0).equals(""))
            path_as_list = path_as_list.subList(1, path_as_list.size());

        return path_as_list.iterator();
    }

    private static URI stringToUri(String path) throws URISyntaxException {

        return new URI(path);
    }
}
