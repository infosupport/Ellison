/*
 * Ellison: EL checker
 * Copyright (C) 2013 Info Support
 * dev@sonar.codehaus.org
 * 
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.infosupport.ellison.core.archive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.infosupport.ellison.core.util.Constants;

/**
 * Represents a Java application archive.
 *
 * @author StefanZ
 */
public abstract class ApplicationArchive {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationArchive.class);
    private File applicationFile;
    private File unpackedPath;

    /**
     * Constructor.
     * <p/>
     * Please note that this constructor immediately tries to unpack the supplied archive, and throws an exception when
     * it doesn't succeed.
     *
     * @param applicationFile
     *     the application archive file to use
     *
     * @throws IOException
     *     when there was an error unpacking the application archive. Specifically, a {@link FileNotFoundException} is
     *     thrown if {@code applicationFile} does not point to an existing file.
     */
    public ApplicationArchive(File applicationFile) throws IOException {
        if (!applicationFile.exists()) {
            throw new FileNotFoundException(
                String.format("File does not exist: %s", applicationFile.getAbsolutePath()));
        }
        this.applicationFile = applicationFile;
        unpackJar();
    }

    /**
     * Checks whether the path is relative.
     * <p/>
     * {@link java.io.File#isAbsolute()} is not very testable, as its behavior is dependent upon the environment in
     * which the application is run.
     *
     * @param path
     *     the path to check
     *
     * @return false if:<ul> <li>the path starts with a slash ('/')</li> <li>the path starts with one backslash
     *         ('\')</li> <li>the path contains a directory component consisting of only two dots ('/../')</li> <li>the
     *         path starts with a windows drive letter ('C:\\')</li> </ul>
     */
    private static boolean isPathRelative(String path) {
        boolean isNotRelative = false;
        File pathFile = new File(path);
        String absolutePath = pathFile.getAbsolutePath();
        String canonicalPath = null;
        try {
            canonicalPath = pathFile.getCanonicalPath();
        } catch (IOException e) {
            LOGGER.info(String.format("Error canonicalizing path '%s'", path), e);
            // Assume the path parameter is not canonical, meaning it might be unsafe.
            return false;
        }
        isNotRelative = path.startsWith("/");
        isNotRelative = isNotRelative || path.startsWith("\\");
        isNotRelative = isNotRelative || !canonicalPath.equals(absolutePath);
        isNotRelative =
            isNotRelative || (CharUtils.isAsciiAlpha(path.charAt(0)) && path.substring(1).startsWith(":\\"));

        return !isNotRelative;
    }

    /**
     * Gets a file from inside the application archive.
     * <p/>
     * Note that this method calls {@link #unpackJar()}, so if the archive hasn't yet been unpacked, this method may
     * throw an {@code IOException}.
     *
     * @param relativePath
     *     the path of the file to find. This must be a relative path.
     *
     * @return a pointer to the queried file.
     *
     * @throws IOException
     *     if an error occurred while trying to unpack the application archive, or if the supplied path does not point
     *     to a file within the application archive (in which case the exception will be an instance of {@link
     *     FileNotFoundException}.
     */
    public File getFile(String relativePath) throws IOException {
        File foundFile = null;

        if (!isPathRelative(relativePath)) {
            throw new SecurityException("Supplied paths must be relative!");
        }

        foundFile = new File(unpackedPath, relativePath);

        if (!foundFile.exists()) {
            throw new FileNotFoundException(String.format("No file '%s' in application archive", relativePath));
        }

        return foundFile;
    }

    /**
     * Finds (recursively) all files in a directory matching a specific pattern.
     *
     * @param basePath
     *     the name of the base directory to search in. May be null if looking for files from the root directory.
     * @param wildcardPattern
     *     files matching this pattern will be in the returned collection. See {@link WildcardFileFilter} for the
     *     pattern format
     *
     * @return a list of all files matching {@code wildcardPattern} in directory {@code basePath} within this
     *         application archive.
     *
     * @throws FileNotFoundException
     *     if {@code basePath != null} and {@code basePath} does not point to an existing file in the application
     *     archive
     * @see WildcardFileFilter
     *      <p/>
     */
    public Collection<URI> findFilesByGlobPattern(String basePath, String wildcardPattern)
        throws FileNotFoundException {

        return findFilesByGlobPattern(basePath, wildcardPattern, true);
    }

    /**
     * Finds all files in a directory matching a specific pattern.
     * <p/>
     * For a definition of what kinds of patterns are supported, see {@link WildcardFileFilter}.
     *
     * @param basePath
     *     the name of the base directory to search in. May be null if looking for files from the root directory.
     * @param wildcardPattern
     *     files matching this pattern will be in the returned collection. See {@link WildcardFileFilter} for the
     *     pattern format
     * @param doRecursively
     *     whether to search for files matching {@code wildcardPattern} recursively or not.
     *
     * @return a list of all files matching {@code wildcardPattern} in directory {@code basePath} within this
     *         application archive.
     *
     * @throws FileNotFoundException
     *     if {@code basePath != null} and {@code basePath} does not point to an existing file in the application
     *     archive
     */
    public Collection<URI> findFilesByGlobPattern(String basePath, String wildcardPattern, boolean doRecursively)
        throws FileNotFoundException {
        IOFileFilter fileFilter = new WildcardFileFilter(wildcardPattern);
        File baseDir = null;
        IOFileFilter dirFilter = null;

        if (basePath == null) {
            baseDir = unpackedPath;
        } else {
            if (!isPathRelative(basePath)) {
                throw new SecurityException(
                    "It is not permitted to supply base paths containing references to parent directories ('..').");
            }
            baseDir = new File(unpackedPath, basePath);
        }

        if (doRecursively) {
            dirFilter = TrueFileFilter.INSTANCE;
        } else {
            dirFilter = FalseFileFilter.INSTANCE;
        }

        if (!baseDir.exists()) {
            throw new FileNotFoundException(String.format("Basepath '%s' does not exist within archive '%s'", basePath,
                                                          getApplicationFile().getAbsolutePath()));
        }

        if (!baseDir.isDirectory()) {
            throw new IllegalArgumentException(
                String.format("Basepath '%s' is not a directory within the archive", basePath));
        }

        return Collections2.transform(FileUtils.listFiles(baseDir, fileFilter, dirFilter), new Function<File, URI>() {
            @Override
            public URI apply(File input) {
                return relativizePath(input.toURI());
            }
        });
    }

    /**
     * Get a classloader capable of loading classes and libraries from the application.
     *
     * @return a classloader capable of loading classes and libraries from the application.
     */
    public abstract ClassLoader getClassLoader();

    /**
     * Unpacks the application archive file (must be a JAR or subtype thereof) into a temporary directory.
     * <p/>
     * This method makes sure to mark any and all extracted files for deletion upon process termination.
     * <p/>
     * Also note that the same archive will not be unpacked twice
     *
     * @return a {@code File} pointing to the directory where the JAR was extracted to
     *
     * @throws java.io.IOException
     *     <ul> <li>when a temporary directory could not be created</li> <li>if access to the {@code jarFile} was
     *     denied
     *     by the {@code SecurityManager}</li> <li>if an error occurred while extracting the {@code jarFile}</li> </ul>
     * @see java.util.jar.JarFile#JarFile(java.io.File)
     */
    protected File unpackJar() throws IOException {
        if (unpackedPath == null) {
            unpackedPath = unpackJarHelper(getApplicationFile());
        }

        return unpackedPath;
    }

    /**
     * Helper function for {@link #unpackJar()}. This does all the actual work, without caching anything.
     *
     * @param jarFile
     *     the file to unpack
     *
     * @return a {@code File} pointing to the temporary directory the JAR was extracted to
     *
     * @throws IOException
     *     when: <ul><li>the temporary directory to unpack the JAR into could not be created</li> <li>the application
     *     archive could not be read as a JAR</li> <li>a file in the archive could not be read</li> <li>a file in the
     *     archive could not be unpacked (write error)</li></ul>
     */
    protected File unpackJarHelper(File jarFile) throws IOException {
        File destination = Files.createTempDirectory(Constants.TEMP_DIR_PREFIX + jarFile.getName()).toFile();
        destination.deleteOnExit();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> jarEntries = jar.entries();

            while (jarEntries.hasMoreElements()) {
                unpackJarEntry(destination, jar, jarEntries.nextElement());
            }
        }

        return destination;
    }

    /**
     * Extracts a single {@link JarEntry} into the {@code destination} directory.
     * <p/>
     * If {@code jarEntry} represents a directory, then a new directory is created in {@code destination}. If it is a
     * file, its {@link InputStream} is opened, and its contents are then written in {@code destination}.
     *
     * @param destination
     *     the destination directory to unpack {@code jarEntry} into. This is the "root" directory where the entire
     *     archive is unpacked into, as each {@code JarEntry}'s name points to whatever directory it should in fact be
     *     in. See {@link File#File(java.io.File, String)} to see how this is achieved.
     * @param jar
     *     the archive from which the {@code jarEntry} originates
     * @param jarEntry
     *     the {@code JarEntry} to unpack
     *
     * @throws IOException
     *     if any IO error occurred while trying to unpack a file
     */
    protected void unpackJarEntry(File destination, JarFile jar, JarEntry jarEntry) throws IOException {
        byte[] ioBuffer = new byte[Constants.UNPACK_BUFFER_SIZE];
        if (jarEntry.isDirectory()) {
            File newDir = new File(destination, jarEntry.getName());
            boolean dirCreated = newDir.mkdir();
            if (dirCreated) {
                newDir.deleteOnExit();
            }
        } else {
            File outFile = new File(destination, jarEntry.getName());
            outFile.deleteOnExit();

            try (InputStream jarEntryInputStream = jar.getInputStream(jarEntry);
                 OutputStream jarEntryOutputStream = new FileOutputStream(outFile)) {
                for (int readBytes = jarEntryInputStream.read(ioBuffer); readBytes != -1;
                    readBytes = jarEntryInputStream.read(ioBuffer)) {
                    jarEntryOutputStream.write(ioBuffer, 0, readBytes);
                }
            }
        }
    }

    /**
     * Get a {@link File} pointing to the application archive file itself.
     *
     * @return the application archive file itself
     */
    public File getApplicationFile() {
        return applicationFile;
    }

    /**
     * Turns a path into one relative to the location where the archive was unpacked.
     * <p/>
     * Example: archive was unpacked into C:\\unpackedpath, and we pass this method the path C:\\unpackedpath\somefile
     * .txt. The returned value will be the relative path "somefile".
     *
     * @param path
     *     the path to relativize
     *
     * @return the relativized path
     */
    public URI relativizePath(URI path) {
        return unpackedPath.toURI().relativize(path);
    }

    /**
     * Does the inverse of {@link #relativizePath(java.net.URI)}: given a relative URI,
     * return the absolute path within this archive.
     *
     * @param uri
     *     the URI to make absolute
     *
     * @return an absolute URI representation of {@code uri}
     *
     * @see URI#resolve(java.net.URI)
     */
    public URI resolvePath(URI uri) {
        return getUnpackedPath().toURI().resolve(uri);
    }

    /**
     * Get the path where the archive has been unpacked.
     *
     * @return a {@link File} representing the temporary directory this application archive has been unpacked to.
     */
    public File getUnpackedPath() {
        return unpackedPath;
    }
}
