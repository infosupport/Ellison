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
import com.infosupport.ellison.core.archive.WARApplicationArchive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import javax.annotation.Nullable;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.infosupport.ellison.core.archive.WARApplicationArchive}.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class WARApplicationArchiveTest {
    static File inputApplicationArchive;
    static Collection<String> inputApplicationArchiveContents;
    static File noLibsInputApplicationArchive;
    static Collection<String> noLibsInputApplicationArchiveContents;
    static File noClassesInputApplicationArchive;
    static Collection<String> noClassesInputApplicationArchiveContents;
    static File noWebInfInputApplicationArchive;
    static Collection<String> noWebInfInputApplicationArchiveContents;
    WARApplicationArchive warApplicationArchive;

    @BeforeClass
    public static void setup() throws Exception {
        File contentsFile = new File(WARApplicationArchive.class.getResource("/jsf12-helloworld.war.contents").toURI());
        File noLibsContentsFile =
            new File(WARApplicationArchive.class.getResource("/jsf12-helloworld-nolibs.war.contents").toURI());
        File noClassesContentsFile =
            new File(WARApplicationArchive.class.getResource("/jsf12-helloworld-noclasses.war.contents").toURI());
        File noWebInfContentsFile =
            new File(WARApplicationArchive.class.getResource("/jsf12-helloworld-nowebinf.war.contents").toURI());

        inputApplicationArchiveContents = Files.readLines(contentsFile, Charset.defaultCharset());
        noLibsInputApplicationArchiveContents = Files.readLines(noLibsContentsFile, Charset.defaultCharset());
        noClassesInputApplicationArchiveContents = Files.readLines(noClassesContentsFile, Charset.defaultCharset());
        noWebInfInputApplicationArchiveContents = Files.readLines(noWebInfContentsFile, Charset.defaultCharset());

        inputApplicationArchive = new File(WARApplicationArchive.class.getResource("/jsf12-helloworld.war").toURI());
        noLibsInputApplicationArchive =
            new File(WARApplicationArchive.class.getResource("/jsf12-helloworld-nolibs.war").toURI());
        noClassesInputApplicationArchive =
            new File(WARApplicationArchive.class.getResource("/jsf12-helloworld-noclasses.war").toURI());
        noWebInfInputApplicationArchive =
            new File(WARApplicationArchive.class.getResource("/jsf12-helloworld-nowebinf.war").toURI());
    }

    /**
     * Tests whether {@link com.infosupport.ellison.core.archive.WARApplicationArchive#getClassLoader()}
     * returns
     * a classloader that can resolve all classes in a WAR, including classes within libraries.
     *
     * @throws Exception
     */
    @Test
    public void testGetClassLoader() throws Exception {
        warApplicationArchive = new WARApplicationArchive(inputApplicationArchive);
        ClassLoader classLoader = warApplicationArchive.getClassLoader();
        Collection<String> classFileNames =
            Collections2.filter(inputApplicationArchiveContents, new Predicate<String>() {
                @Override
                public boolean apply(@Nullable String input) {
                    return input.endsWith(".class");
                }
            });
        Collection<String> classNames =
            Collections2.transform(classFileNames, new ClassPathToFullyQualifiedClassName("WEB-INF/classes/"));


        tryGettingAllFiles(warApplicationArchive, inputApplicationArchiveContents);
        for (String className : classNames) {
            // The following line throws a ClassNotFoundException if the ClassLoader doesn't work as expected.
            Class.forName(className, true, classLoader);
        }

        // FileItemFactory is a class that is included in one of the libraries in WEB-INF/lib
        Class.forName("org.apache.commons.fileupload.FileItemFactory", true, classLoader);

        try {
            // Try with the default classloader, and make sure it throws an exception
            Class.forName("org.apache.commons.fileupload.FileItemFactory");
            fail();
        } catch (ClassNotFoundException e) {
            // Success!
        }
    }

    /**
     * Tests whether {@link com.infosupport.ellison.core.archive.WARApplicationArchive#getClassLoader()}
     * returns
     * a classloader that can load all classes, even when the WAR does not contain a WEB-INF/lib directory.
     *
     * @throws Exception
     */
    @Test
    public void testGetClassLoader_NoLibsInWAR() throws Exception {
        warApplicationArchive = new WARApplicationArchive(noLibsInputApplicationArchive);
        ClassLoader classLoader = warApplicationArchive.getClassLoader();
        Collection<String> classFileNames =
            Collections2.filter(noLibsInputApplicationArchiveContents, new Predicate<String>() {
                @Override
                public boolean apply(@Nullable String input) {
                    return input.endsWith(".class");
                }
            });
        Collection<String> classNames =
            Collections2.transform(classFileNames, new ClassPathToFullyQualifiedClassName("WEB-INF/classes/"));

        tryGettingAllFiles(warApplicationArchive, noLibsInputApplicationArchiveContents);
        for (String className : classNames) {
            // The following line throws a ClassNotFoundException if the ClassLoader doesn't work as expected.
            Class.forName(className, true, classLoader);
        }
    }

    /**
     * Tests whether {@link com.infosupport.ellison.core.archive.WARApplicationArchive#getClassLoader()}
     * returns
     * a classloader that can load library classes, even when the WAR does not contain a WEB-INF/classes directory.
     */
    @Test
    public void testGetClassLoader_NoClassesInWAR() throws Exception {
        warApplicationArchive = new WARApplicationArchive(noClassesInputApplicationArchive);
        ClassLoader classLoader = warApplicationArchive.getClassLoader();

        tryGettingAllFiles(warApplicationArchive, noClassesInputApplicationArchiveContents);

        // FileItemFactory is a class that is included in one of the libraries in WEB-INF/lib
        Class.forName("org.apache.commons.fileupload.FileItemFactory", true, classLoader);

        try {
            // Try getting the WEB-INF/classes
            warApplicationArchive.getFile("WEB-INF/classes");
            fail();
        } catch (FileNotFoundException e) {
            // Success!
        }

        try {
            // Try with the default classloader, and make sure it throws an exception
            Class.forName("org.apache.commons.fileupload.FileItemFactory");
            fail();
        } catch (ClassNotFoundException e) {
            // Success!
        }
    }

    /**
     * Tests whether {@link com.infosupport.ellison.core.archive.WARApplicationArchive#getClassLoader()}
     * returns
     * a classloader, even when there is no WEB-INF directory in the application archive.
     *
     * @throws Exception
     */
    @Test
    public void testGetClassLoader_NoWebInfInWAR() throws Exception {
        warApplicationArchive = new WARApplicationArchive(noWebInfInputApplicationArchive);
        ClassLoader classLoader = warApplicationArchive.getClassLoader();

        tryGettingAllFiles(warApplicationArchive, noWebInfInputApplicationArchiveContents);
    }

    /**
     * Makes sure that if the application archive could not be unpacked, a {@code null} classloader is returned when
     * calling {@link com.infosupport.ellison.core.archive.WARApplicationArchive#getClassLoader()}.
     */
    @Test(expected = IOException.class)
    public void testGetClassLoader_CouldNotUnpackArchive() throws Exception {
        File inputFile = new File(getClass().getResource("/example.jar.contents").toURI());

        warApplicationArchive = new WARApplicationArchive(inputFile);
    }

    /**
     * Make a {@link java.net.MalformedURLException} get thrown within {@link
     * com.infosupport.ellison.core.archive.WARApplicationArchive.JarURIToURLTransform#
     * apply(java.net.URI)}
     */
    @Test
    public void testJarURIToURLTransform_apply_IOException() {
        warApplicationArchive = mock(WARApplicationArchive.class);
        WARApplicationArchive.JarURIToURLTransform transform = warApplicationArchive.new JarURIToURLTransform();
        URI uriToTransform = URI.create("file:/");

        when(warApplicationArchive.getApplicationFile()).thenReturn(new File(uriToTransform));
        when(warApplicationArchive.resolvePath(any(URI.class))).thenThrow((Class) IOException.class);

        assertThat(transform.apply(uriToTransform), is(nullValue()));
    }

    /**
     * Assert that creating a classloader only happens once, and that any subsequent calls to {@link
     * com.infosupport.ellison.core.archive.WARApplicationArchive#getClassLoader()} return the same
     * instance.
     */
    @Test
    public void testGetClassLoader_VerifyCaching() throws Exception {
        ClassLoader classLoader = null;
        warApplicationArchive = new WARApplicationArchive(inputApplicationArchive);

        classLoader = warApplicationArchive.getClassLoader();

        assertThat(classLoader, is(not(nullValue())));
        assertThat(warApplicationArchive.getClassLoader(), is(sameInstance(classLoader)));
    }

    /**
     * This method calls {@link com.infosupport.ellison.core.archive.ApplicationArchive#getFile(String)}
     * on every item in {@code filenames}. If one of the
     * files cannot be found, then an exception will be thrown.
     *
     * @param applicationArchive
     *     the archive to find the files in
     * @param filenames
     *     the names of all the files that should be found in the archive
     *
     * @throws Exception
     *     if a file could not be found, or if the archive hadn't yet been unpacked and an error occurred trying to
     *     unpack it.
     */
    private void tryGettingAllFiles(WARApplicationArchive applicationArchive, Collection<String> filenames)
        throws Exception {
        for (String filename : filenames) {
            applicationArchive.getFile(filename);
        }
    }

    /**
     * Transforms a pathname of the form "someprefix/com/example/ClassName.class" to "com.example.ClassName". The
     * resulting string should then be a fully qualified class name.
     */
    private static class ClassPathToFullyQualifiedClassName implements Function<String, String> {
        private final String prefix;

        public ClassPathToFullyQualifiedClassName(String prefix) {
            this.prefix = prefix;
        }

        @Nullable
        @Override
        public String apply(@Nullable String input) {
            String fullyQualifiedClassName = input.replaceFirst(prefix, "");
            int lastPeriod = fullyQualifiedClassName.lastIndexOf('.');
            fullyQualifiedClassName = fullyQualifiedClassName.substring(0, lastPeriod);
            fullyQualifiedClassName = fullyQualifiedClassName.replaceAll("/", ".");

            return fullyQualifiedClassName;
        }
    }
}
