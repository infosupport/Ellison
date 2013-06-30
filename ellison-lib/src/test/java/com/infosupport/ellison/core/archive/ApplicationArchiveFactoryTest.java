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

import com.infosupport.ellison.core.archive.ApplicationArchiveFactory;
import com.infosupport.ellison.core.archive.EARApplicationArchive;
import com.infosupport.ellison.core.archive.WARApplicationArchive;
import com.infosupport.ellison.core.exceptions.ArchiveFormatUnsupportedException;
import com.infosupport.ellison.testutil.StackTraceUtil;

import java.io.File;
import java.io.FileNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.googlecode.catchexception.CatchException.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test ApplicationArchiveFactory.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationArchiveFactoryTest {
    static final String WAR_FILENAME = "somefile.war";
    static final String EAR_FILENAME = "somefile.ear";
    static final String UNSUPPORTED_FILENAME =
        "somefile.really_this_should_not_be_a_supported_filename_extension_or_should_it";
    ApplicationArchiveFactory applicationArchiveFactory;

    @Before
    public void setup() {
        applicationArchiveFactory = ApplicationArchiveFactory.getInstance();
    }

    @Test
    public void testGetFileNameExtension_LastCharIsPeriod() {
        String extensionWithPeriod = null;
        String extensionWithoutPeriod = null;

        try {
            extensionWithPeriod = ApplicationArchiveFactory.getFileNameExtensionWithPeriod("bla.");
            extensionWithoutPeriod = ApplicationArchiveFactory.getFileNameExtensionWithoutPeriod("bla.");
        } catch (IndexOutOfBoundsException e) {
            fail(); // The method should never throw an exception.
        }

        assertThat(extensionWithPeriod, is(equalTo(".")));
        assertThat(extensionWithoutPeriod, is(equalTo("")));
    }

    @Test
    public void testGetFileNameExtension_NoPeriod() {
        String extensionWithPeriod = null;
        String extensionWithoutPeriod = null;

        try {
            extensionWithPeriod = ApplicationArchiveFactory.getFileNameExtensionWithPeriod("bla");
            extensionWithoutPeriod = ApplicationArchiveFactory.getFileNameExtensionWithoutPeriod("bla");
        } catch (IndexOutOfBoundsException e) {
            fail(); // The method should never throw an exception.
        }

        assertThat(extensionWithPeriod, is(nullValue()));
        assertThat(extensionWithoutPeriod, is(nullValue()));
    }

    @Test
    public void testGetFileNameExtension_MultiplePeriods() {
        String extensionWithPeriod = null;
        String extensionWithoutPeriod = null;
        String multiPeriodString = "one.two.three";

        try {
            extensionWithPeriod = ApplicationArchiveFactory.getFileNameExtensionWithPeriod(multiPeriodString);
            extensionWithoutPeriod = ApplicationArchiveFactory.getFileNameExtensionWithoutPeriod(multiPeriodString);
        } catch (IndexOutOfBoundsException e) {
            fail(); // The method should never throw an exception
        }

        assertThat(extensionWithPeriod, is(equalTo(".three")));
        assertThat(extensionWithoutPeriod, is(equalTo("three")));
    }

    @Test
    public void testCreateApplicationArchive_WARExtension() throws Exception {
        File file = mock(File.class);
        when(file.exists()).thenReturn(false);
        when(file.getName()).thenReturn(WAR_FILENAME);

        verifyException(applicationArchiveFactory, FileNotFoundException.class).createApplicationArchive(file);
        assertThat(StackTraceUtil.hasCaller(caughtException().getStackTrace(), WARApplicationArchive.class), is(true));
        assertThat(StackTraceUtil.hasCaller(caughtException().getStackTrace(), EARApplicationArchive.class), is(false));
    }

    @Test
    public void testCreateApplicationArchive_EARExtension() throws Exception {
        File file = mock(File.class);
        when(file.exists()).thenReturn(false);
        when(file.getName()).thenReturn(EAR_FILENAME);

        verifyException(applicationArchiveFactory, FileNotFoundException.class).createApplicationArchive(file);
        assertThat(StackTraceUtil.hasCaller(caughtException().getStackTrace(), EARApplicationArchive.class), is(true));
        assertThat(StackTraceUtil.hasCaller(caughtException().getStackTrace(), WARApplicationArchive.class), is(false));
    }

    @Test
    public void testCreateApplicationArchive_ExistingWAR() throws Exception {
        File file = mock(File.class);
        when(file.exists()).thenReturn(true);
        when(file.getName()).thenReturn(WAR_FILENAME);
        when(file.getPath()).thenReturn(WAR_FILENAME);

        catchException(applicationArchiveFactory, FileNotFoundException.class).createApplicationArchive(file);
        // Succeed (No exception should be thrown)
    }

    @Test
    public void testCreateApplicationArchive_Unsupported() throws Exception {
        File file = mock(File.class);
        when(file.getName()).thenReturn(UNSUPPORTED_FILENAME);

        verifyException(applicationArchiveFactory, ArchiveFormatUnsupportedException.class)
            .createApplicationArchive(file);
    }

    @Test
    public void testCreateApplicationArchive_NoExtension() throws Exception {
        File file = mock(File.class);

        when(file.getName()).thenReturn("noextension");

        verifyException(applicationArchiveFactory, ArchiveFormatUnsupportedException.class)
            .createApplicationArchive(file);
    }
}
