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
package uk.ac.standrews.cs.utilities.network;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

public class UriUtilTest {

    @Test
    public void testUriEncode() throws UnsupportedEncodingException {

        assertEquals(UriUtil.uriEncode("my files"), "my%20files");
        assertEquals(UriUtil.uriEncode("a/b/c"), "a/b/c");
        assertEquals(UriUtil.uriEncode("a b/c d/e f"), "a%20b/c%20d/e%20f");
        assertEquals(UriUtil.uriEncode("/P2P Services.Data/rdb copy/"), "/P2P%20Services.Data/rdb%20copy/");
    }

    @Test
    public void testChildUri() throws URISyntaxException, UnsupportedEncodingException {

        assertEquals(UriUtil.childUri(new URI(""), "abc", false), new URI("/abc"));
        assertEquals(UriUtil.childUri(new URI(""), "abc", true), new URI("/abc/"));
        assertEquals(UriUtil.childUri(new URI("/"), "abc", false), new URI("/abc"));
        assertEquals(UriUtil.childUri(new URI("/"), "abc", true), new URI("/abc/"));
        assertEquals(UriUtil.childUri(new URI("/x"), "abc", false), new URI("/x/abc"));
        assertEquals(UriUtil.childUri(new URI("/x"), "abc", true), new URI("/x/abc/"));
        assertEquals(UriUtil.childUri(new URI("/x/"), "abc", false), new URI("/x/abc"));
        assertEquals(UriUtil.childUri(new URI("/x/"), "abc", true), new URI("/x/abc/"));
        assertEquals(UriUtil.childUri(new URI("/x/"), "my files", true), new URI("/x/my%20files/"));
        assertEquals(UriUtil.childUri(new URI("/my%20files/"), "my file", true), new URI("/my%20files/my%20file/"));
    }

    @Test
    public void testParentUri() throws URISyntaxException, UnsupportedEncodingException {

        assertEquals(UriUtil.parentUri(new URI("")), new URI("/"));
        assertEquals(UriUtil.parentUri(new URI("/")), new URI("/"));
        assertEquals(UriUtil.parentUri(new URI("/abc")), new URI("/"));
        assertEquals(UriUtil.parentUri(new URI("/abc/")), new URI("/"));
        assertEquals(UriUtil.parentUri(new URI("/abc/def")), new URI("/abc"));
        assertEquals(UriUtil.parentUri(new URI("/abc/def%20ghi/j%20kl%23")), new URI("/abc/def%20ghi"));
    }

    @Test
    public void testBaseName() throws URISyntaxException {

        assertEquals(UriUtil.baseName(new URI("")), "");
        assertEquals(UriUtil.baseName(new URI("/")), "");
        assertEquals(UriUtil.baseName(new URI("/abc")), "abc");
        assertEquals(UriUtil.baseName(new URI("/abc/")), "abc");
        assertEquals(UriUtil.baseName(new URI("/abc/def")), "def");
        assertEquals(UriUtil.baseName(new URI("/abc/def%20ghi/j%20kl%23")), "j kl#");
    }

    @Test
    public void testPathElementIterator() throws URISyntaxException {

        Iterator iterator = UriUtil.pathElementIterator(new URI(""));
        assertFalse(iterator.hasNext());

        iterator = UriUtil.pathElementIterator(new URI("/"));
        assertFalse(iterator.hasNext());

        iterator = UriUtil.pathElementIterator(new URI("/abc"));
        assertEquals(iterator.next(), "abc");
        assertFalse(iterator.hasNext());

        iterator = UriUtil.pathElementIterator(new URI("/abc/"));
        assertEquals(iterator.next(), "abc");
        assertFalse(iterator.hasNext());

        iterator = UriUtil.pathElementIterator(new URI("/abc/def%20ghi/j%20kl%23"));
        assertEquals(iterator.next(), "abc");
        assertEquals(iterator.next(), "def ghi");
        assertEquals(iterator.next(), "j kl#");
        assertFalse(iterator.hasNext());
    }
}
