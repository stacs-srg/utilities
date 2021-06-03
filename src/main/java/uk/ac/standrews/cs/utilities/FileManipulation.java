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
package uk.ac.standrews.cs.utilities;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Various file manipulation methods.
 *
 * @author Graham Kirby (graham.kirby@st-andrews.ac.uk)
 */
public class FileManipulation {

    @SuppressWarnings("WeakerAccess")
    public static final Charset FILE_CHARSET = StandardCharsets.UTF_8;
    private static final String URL_ENCODING = FILE_CHARSET.name();

    private static final String JAR_PREFIX = "jar";
    private static final String FILE_PREFIX = "file";
    private static final int LENGTH_OF_FILE_PREFIX = (FILE_PREFIX + ":").length();

    private static final int INPUT_BUFFER_SIZE_IN_BYTES = 512;

    /**
     * Creates an input stream reader for a given path.
     *
     * @param path the path
     * @return the input stream reader
     * @throws IOException if the file cannot be read
     */
    public static InputStreamReader getInputStreamReader(final Path path) throws IOException {

        return new InputStreamReader(getInputStream(path), FILE_CHARSET);
    }

    public static InputStream getInputStream(final Path path) throws IOException {

        return Files.newInputStream(path);
    }

    /**
     * Gets the number of lines in a given file.
     *
     * @param path the path of the file
     * @return the number of lines
     * @throws IOException if the file cannot be read
     */
    @SuppressWarnings("unused")
    public static int countLines(final Path path) throws IOException {

        return countLines(getInputStream(path));
    }

    @SuppressWarnings("WeakerAccess")
    public static int countLines(final InputStream input_stream) throws IOException {

        return readAllLines(input_stream).size();
    }

    /**
     * Gets all the bytes available from a given input stream
     *
     * @param inputStream the input stream
     * @return an array containing all the available bytes
     * @throws IOException if the stream cannot be read
     */
    public static byte[] readAllBytes(final InputStream inputStream) throws IOException {

        try (ByteArrayOutputStream temporary_byte_array_stream = new ByteArrayOutputStream()) {

            final byte[] buffer = new byte[INPUT_BUFFER_SIZE_IN_BYTES];

            int i;
            while ((i = inputStream.read(buffer)) != -1) {
                temporary_byte_array_stream.write(buffer, 0, i);
            }
            return temporary_byte_array_stream.toByteArray();
        }
    }

    public static List<String> readAllLines(final InputStream input_stream) throws IOException {

        final List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input_stream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines;
        }
    }

    /**
     * Creates an output stream writer for a given path.
     *
     * @param path the path
     * @return the output stream writer
     * @throws IOException if the file cannot be written
     */
    public static OutputStreamWriter getOutputStreamWriter(final Path path) throws IOException {

        return new OutputStreamWriter(Files.newOutputStream(path), FILE_CHARSET);
    }

    /**
     * Gets the path of a given resource loaded via the given class.
     *
     * @param the_class     the class
     * @param resource_name the name of the resource
     * @return the path of the resource
     */
    @SuppressWarnings("unused")
    public static Path getResourcePath(final Class the_class, final String resource_name) {

        final URL resource = getResource(the_class, resource_name);
        try {
            return Paths.get(resource.toURI());
        } catch (final URISyntaxException e) {
            throw new RuntimeException("invalid URI for resource path: " + e.getMessage());
        }
    }

    /**
     * Gets an input stream reader for a given resource loaded via the given class.
     *
     * @param the_class     the class
     * @param resource_name the name of the resource
     * @return the input stream reader
     */
    @SuppressWarnings("unused")
    public static InputStreamReader getInputStreamReaderForResource(final Class the_class, final String resource_name) {

        return new InputStreamReader(getInputStreamForResource(the_class, resource_name));
    }

    /**
     * Gets an input stream for a given resource loaded via the given class.
     *
     * @param the_class     the class
     * @param resource_name the name of the resource
     * @return the input stream
     */
    @SuppressWarnings("WeakerAccess")
    public static InputStream getInputStreamForResource(final Class the_class, final String resource_name) {

        // First try to get resource from the real file system.
        final InputStream stream_from_file_system = the_class.getResourceAsStream(resource_name);

        // If that doesn't work, try to get resource from jar file, in which case resource needs to be prepended with full class name.
        return stream_from_file_system != null ? stream_from_file_system : the_class.getResourceAsStream(getResourceNamePrefixedWithClass(the_class, resource_name));
    }

    /**
     * Deletes a directory and all its contents.
     *
     * @param directory_path the path of the directory
     * @throws IOException if the directory cannot be deleted
     */
    @SuppressWarnings("WeakerAccess")
    public static void deleteDirectory(final String directory_path) throws IOException {

        deleteDirectory(Paths.get(directory_path));
    }

    /**
     * Deletes a directory and all its contents.
     *
     * @param directory_path the path of the directory
     * @throws IOException if the directory cannot be deleted
     */
    @SuppressWarnings("WeakerAccess")
    public static void deleteDirectory(final Path directory_path) throws IOException {

        Files.walkFileTree(directory_path, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {

                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {

                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Creates a file at the given path if it does not already exist.
     *
     * @param path the path of the file
     * @throws IOException if the file cannot be created
     */
    @SuppressWarnings("unused")
    public static void createFileIfDoesNotExist(final Path path) throws IOException {

        if (!Files.exists(path)) {

            createParentDirectoryIfDoesNotExist(path);
            Files.createFile(path);
        }
    }

    /**
     * @param directory of the directory to be created
     * @throws IOException if one is thrown by Java IO layer
     */
    @SuppressWarnings("unused")
    public static void createDirectoryIfDoesNotExist(final File directory) throws IOException {

        createDirectoryIfDoesNotExist(Paths.get(directory.getAbsolutePath()));
    }

    /**
     * @param path of the directory to be created
     * @throws IOException if one is thrown by Java IO layer
     */
    @SuppressWarnings("WeakerAccess")
    public static void createDirectoryIfDoesNotExist(final Path path) throws IOException {

        Files.createDirectories(path);
    }

    /**
     * Creates the parent directory for the file at the given path if the parent does not already exist.
     *
     * @param path the path of the file
     * @throws IOException if the parent directory cannot be created
     */
    @SuppressWarnings("WeakerAccess")
    public static void createParentDirectoryIfDoesNotExist(final Path path) throws IOException {

        final Path parent_dir = path.getParent();
        if (parent_dir != null) {

            Files.createDirectories(parent_dir);
        }
    }

    /**
     * Gets the paths of the entries in the given directory.
     *
     * @param directory the path of the directory
     * @return a list of entry paths
     * @throws IOException if the directory cannot be accessed
     */
    @SuppressWarnings("unused")
    public static List<Path> getDirectoryEntries(final Path directory) throws IOException {

        final List<Path> result = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (final Path entry : stream) {
                result.add(entry);
            }
        } catch (final DirectoryIteratorException e) {
            throw e.getCause();
        }
        return result;
    }

    /**
     * For use in JUnit tests: asserts that two files have the same content.
     *
     * @param path1 the path of the first file
     * @param path2 the path of the second file
     * @throws IOException if one of the files cannot be read
     */
    public static void assertThatFilesHaveSameContent(final Path path1, final Path path2) throws IOException {

        try (BufferedReader reader1 = Files.newBufferedReader(path1, FILE_CHARSET); BufferedReader reader2 = Files.newBufferedReader(path2, FILE_CHARSET)) {

            String line1;

            while ((line1 = reader1.readLine()) != null) {
                assertEquals(line1, reader2.readLine());
            }
            assertNull(reader2.readLine());
        }
    }

    /**
     * Returns the top-level entries in the given resource directory.
     *
     * @param resource_directory_path the absolute path to a directory, with initial slash mapping to the root of the resource directory
     * @param class_loader            the class loader
     * @return a list of entries
     * @throws IOException if the directory cannot be read
     */
    @SuppressWarnings("WeakerAccess")
    public static List<String> getResourceDirectoryEntries(final String resource_directory_path, final ClassLoader class_loader) throws IOException {

        final String relative_path = getRelativePath(resource_directory_path);
        final URL path_url = class_loader.getResource(relative_path);

        if (path_url != null) {

            final String path_url_protocol = path_url.getProtocol();

            if (path_url_protocol.equals(FILE_PREFIX)) {
                return getResourceDirectoryEntriesFromFileSystem(path_url);
            }

            if (path_url_protocol.equals(JAR_PREFIX)) {
                return getResourceDirectoryEntriesFromJar(relative_path + "/", path_url);
            }
        }

        throw new IOException("can't access resource directory: " + resource_directory_path);
    }

    /**
     * Returns the top-level entries in the given resource directory.
     *
     * @param resource_directory_path the absolute path to a directory, with initial slash mapping to the root of the resource directory
     * @param class_loader            the class loader
     * @return a list of entries
     * @throws IOException if the directory cannot be read
     */
    @SuppressWarnings("unused")
    public static List<String> getResourceDirectoryEntries(final Path resource_directory_path, final ClassLoader class_loader) throws IOException {

        return getResourceDirectoryEntries(resource_directory_path.toString(), class_loader);
    }

    public static void concatenateFiles(File[] files, File outFile) throws IOException {

        try (OutputStream out = new FileOutputStream(outFile)) {

            byte[] buf = new byte[INPUT_BUFFER_SIZE_IN_BYTES];
            for (File file : files) {
                InputStream in = new FileInputStream(file);
                int b;
                while ((b = in.read(buf)) >= 0) {
                    out.write(buf, 0, b);
                    out.flush();
                }
            }
        }
    }

    /**
     * Returns the top-level entries in the given resource directory.
     *
     * @param relative_directory_path the path to a directory relative to the root of the resource directory e.g. "directory/path/"
     * @param resource_directory_url  the URL for the directory, in the form "file:/absolute/path/of/jar!/directory/path/"
     * @return a list of entries
     * @throws IOException if the directory cannot be read
     */
    private static List<String> getResourceDirectoryEntriesFromJar(final String relative_directory_path, final URL resource_directory_url) throws IOException {

        try (JarFile jar_file = getJarFile(resource_directory_url)) {

            // Gets all entries in the jar file, including sub-directories and files.
            final Enumeration<JarEntry> jar_entries = jar_file.entries();

            final Set<String> entries = new HashSet<>();

            while (jar_entries.hasMoreElements()) {

                // The path of this entry relative to the resource root.
                final String entry_path = jar_entries.nextElement().getName();

                // Check whether this entry is a child of the specified resource directory.
                if (entry_path.startsWith(relative_directory_path) && !entry_path.equals(relative_directory_path)) {

                    entries.add(getChildName(entry_path, relative_directory_path));
                }
            }

            return setToList(entries);
        }
    }

    private static List<String> setToList(final Set<String> entries) {

        return Arrays.asList(entries.toArray(new String[entries.size()]));
    }

    /**
     * @param entry_path              e.g.              "a/b/c/d/e"
     * @param relative_directory_path e.g. "a/b/c"
     * @return the name of the element that is a child of the relative directory e.g. "d"
     */
    private static String getChildName(final String entry_path, final String relative_directory_path) {

        return firstPartOfPath(remainingPathAfter(entry_path, relative_directory_path.length()));
    }

    private static String firstPartOfPath(final String path) {

        final int i = path.indexOf("/");

        return i == -1 ? path : path.substring(0, i);
    }

    /**
     * @param resource_directory_url the URL for the directory, in the form "file:/absolute/path/of/jar!/directory/path/"
     */
    private static JarFile getJarFile(final URL resource_directory_url) throws IOException {

        // String representation of the full path including absolute path of jar file and path to directory relative to resource root within jar.
        final String path = resource_directory_url.getPath();

        // Discard "file:" prefix and resource directory to give jar file path.
        final String absolute_path_of_jar_file = path.substring(LENGTH_OF_FILE_PREFIX, path.indexOf("!"));

        return new JarFile(URLDecoder.decode(absolute_path_of_jar_file, URL_ENCODING));
    }

    private static List<String> getResourceDirectoryEntriesFromFileSystem(final URL path_url) throws IOException {

        try {
            final String[] directory_entries = new File(path_url.toURI()).list();
            if (directory_entries == null) {
                return new ArrayList<>();
            }
            return Arrays.asList(directory_entries);
        } catch (final URISyntaxException e) {
            throw new IOException("can't access resource URL: " + path_url + " - " + e.getMessage());
        }
    }

    private static String getRelativePath(final String resource_directory_path) {

        return resource_directory_path.startsWith("/") ? remainingPathAfter(resource_directory_path, 1) : resource_directory_path;
    }

    private static String remainingPathAfter(final String resource_directory_path, final int start_index) {

        return resource_directory_path.substring(start_index);
    }

    private static URL getResource(final Class the_class, final String resource_name) {

        return the_class.getResource(getResourceNamePrefixedWithClass(the_class, resource_name));
    }

    private static String getResourceNamePrefixedWithClass(final Class the_class, final String resource_name) {

        return the_class.getSimpleName() + "/" + resource_name;
    }
}
