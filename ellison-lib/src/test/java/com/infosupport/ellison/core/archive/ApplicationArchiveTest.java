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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.io.Files;
import com.infosupport.ellison.core.archive.ApplicationArchive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.annotation.Nullable;
import junitx.util.PrivateAccessor;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.CALLS_REAL_METHODS;
import static org.mockito.Mockito.*;


/**
 * Tests the {@link com.infosupport.ellison.core.archive.ApplicationArchive} class.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationArchiveTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationArchiveTest.class);
    static File inputJar = null;
    static Collection<String> inputJarContents = null;
    @Mock(answer = CALLS_REAL_METHODS) ApplicationArchive applicationArchive;

    @BeforeClass
    public static void setupClass() throws Exception {
        inputJar = new File(ApplicationArchiveTest.class.getResource("/example.jar").toURI());
        inputJarContents = new ArrayList<>();
        File contentsFile = new File(ApplicationArchiveTest.class.getResource("/example.jar.contents").toURI());
        inputJarContents = Files.readLines(contentsFile, Charset.defaultCharset());
    }

    @Before
    public void setupTest() {
        doReturn(inputJar).when(applicationArchive).getApplicationFile();
    }

    /**
     * Tests whether {@link com.infosupport.ellison.core.archive.ApplicationArchive#unpackJar()}
     * actually unpacks an archive (and only once). This is done by asserting that files within the archive exist in
     * the directory the archive was unpacked to.
     */
    @Test
    public void testUnpackJar_Success() throws Exception {
        doReturn(inputJar).when(applicationArchive).getApplicationFile();

        applicationArchive.unpackJar();
        applicationArchive.unpackJar();

        // The JAR should only be unpacked once!
        verify(applicationArchive, times(1)).unpackJarHelper(inputJar);

        for (String inputJarContent : inputJarContents) {
            File content = applicationArchive.getFile(inputJarContent);

            assertThat(content, is(notNullValue()));
            assertThat(content.isDirectory(), is(equalTo(inputJarContent.endsWith("/"))));
        }
    }

    @Test(expected = IOException.class)
    public void testUnpackJar_IOException() throws Exception {
        doReturn(inputJar).when(applicationArchive).getApplicationFile();
        doThrow(IOException.class).when(applicationArchive)
            .unpackJarEntry(Matchers.any(File.class), Matchers.any(JarFile.class), Matchers.any(JarEntry.class));

        applicationArchive.unpackJar();
    }

    @Test(expected = SecurityException.class)
    public void testUnpackJar_SecurityException() throws Exception {
        doReturn(inputJar).when(applicationArchive).getApplicationFile();
        doThrow(SecurityException.class).when(applicationArchive)
            .unpackJarEntry(Matchers.any(File.class), Matchers.any(JarFile.class), Matchers.any(JarEntry.class));

        applicationArchive.unpackJar();
    }

    @Test(expected = FileNotFoundException.class)
    public void testUnpackJar_FileNotFoundException() throws Exception {
        doReturn(inputJar).when(applicationArchive).getApplicationFile();
        doThrow(FileNotFoundException.class).when(applicationArchive)
            .unpackJarEntry(Matchers.any(File.class), Matchers.any(JarFile.class), Matchers.any(JarEntry.class));

        applicationArchive.unpackJar();
    }

    @Test(expected = FileNotFoundException.class)
    public void testGetFile_FileNotFound() throws Exception {
        String randomFileName = null;
        do {
            randomFileName = RandomStringUtils.randomAlphanumeric(10);
        } while (inputJarContents.contains(randomFileName));

        applicationArchive.getFile(randomFileName);
    }

    @Test(expected = SecurityException.class)
    public void testGetFile_NotRelativeFilePathUnix() throws Exception {
        applicationArchive.getFile("/");
    }

    @Test(expected = SecurityException.class)
    public void testGetFile_NotRelativeFilePathWindowsCurrentDrive() throws Exception {
        applicationArchive.getFile("\\");
    }

    @Test(expected = SecurityException.class)
    public void testGetFile_NotRelativeFilePathWindowsDrivePrefix() throws Exception {
        applicationArchive.getFile("C:\\");
    }

    @Test(expected = SecurityException.class)
    public void testGetFile_NotRelativeFilePathWindowsNetwork() throws Exception {
        applicationArchive.getFile("\\\\bla");
    }

    @Test
    public void testFindFilesByGlobPattern_AllClasses() throws Exception {
        applicationArchive.unpackJar();
        final URI unpackedPathURI = ((File) PrivateAccessor.getField(applicationArchive, "unpackedPath")).toURI();
        List<String> expectedClassFileNames =
            new ArrayList<>(Collections2.filter(inputJarContents, new Predicate<String>() {
                @Override
                public boolean apply(@Nullable String input) {
                    return input.endsWith(".class");
                }
            }));
        Collection<URI> actualClassFiles = applicationArchive.findFilesByGlobPattern("com", "*.class");
        List<String> actualClassFileNames =
            new ArrayList<>(Collections2.transform(actualClassFiles, new Function<URI, String>() {
                @Override
                public String apply(@Nullable URI input) {
                    return unpackedPathURI.relativize(input).toString();
                }
            }));

        Collections.sort(expectedClassFileNames);
        Collections.sort(actualClassFileNames);

        assertThat(actualClassFileNames, is(equalTo(expectedClassFileNames)));
    }

    @Test
    public void testFindFilesByGlobPattern_ClassInRoot() throws Exception {
        applicationArchive.unpackJar();

        Collection<URI> foundClassFiles = applicationArchive.findFilesByGlobPattern(null, "*.class", false);

        assertThat(foundClassFiles.size(), is(equalTo(0)));
    }

    @Test(expected = FileNotFoundException.class)
    public void testFindFilesByGlobPattern_NoSuchFile() throws Exception {
        applicationArchive.unpackJar();

        String noSuchFile = null;

        do {
            noSuchFile = RandomStringUtils.random(12);
        } while (inputJarContents.contains(noSuchFile + "/"));

        applicationArchive.findFilesByGlobPattern(noSuchFile, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindFilesByGlobPattern_NotADirectory() throws Exception {
        applicationArchive.unpackJar();

        URI unpackedPathURI = ((File) PrivateAccessor.getField(applicationArchive, "unpackedPath")).toURI();
        String existingFile =
            unpackedPathURI.relativize(applicationArchive.findFilesByGlobPattern(null, "*").iterator().next())
                .toString();

        applicationArchive.findFilesByGlobPattern(existingFile, "");
    }

    @Test(expected = SecurityException.class)
    public void testFindFilesByGlobPattern_InsecureBasePathDoubleDot() throws Exception {
        applicationArchive.findFilesByGlobPattern("..", "");
    }

    @Test(expected = SecurityException.class)
    public void testFindFilesByGlobPattern_InsecureBasePathDoubleDotAfterDirectory() throws Exception {
        applicationArchive.findFilesByGlobPattern("bla/..", "");
    }

    @Test
    public void testRelativizePath() throws Exception {
        applicationArchive.unpackJar();
        URI relativePath = URI.create("somefile");
        URI absolutePath = applicationArchive.getUnpackedPath().toURI().resolve(relativePath);

        URI returnedPath = applicationArchive.relativizePath(absolutePath);

        assertThat(returnedPath, is(equalTo(relativePath)));
    }
}
