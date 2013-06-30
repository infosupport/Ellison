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
package com.infosupport.ellison.core;

import com.infosupport.ellison.core.ELApplicationChecker;
import com.infosupport.ellison.core.api.*;
import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.infosupport.ellison.core.archive.ApplicationArchiveFactory;
import com.infosupport.ellison.core.exceptions.ApplicationNotSupportedException;
import com.infosupport.ellison.core.exceptions.ArchiveFormatUnsupportedException;
import com.infosupport.ellison.core.exceptions.ELCheckerException;
import com.infosupport.ellison.core.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link ELApplicationChecker}
 *
 * @author StefanZ
 * @see ELApplicationChecker
 */
@RunWith(MockitoJUnitRunner.class)
public class ELApplicationCheckerTest {
    ELApplicationChecker elApplicationChecker;
    Collection<ELError> emptyErrorCollection;
    Collection<LocationAwareELExpression> emptyELExpressionCollection;
    Collection<ELError> oneErrorCollection;
    Collection<ELError> twoErrorCollection;
    Collection<URI> emptyPagesCollection;
    @Mock File applicationFile;
    @Mock ApplicationArchiveFactory applicationArchiveFactory;
    @Mock ApplicationArchive applicationArchive;
    @Mock ELApplicationCheckerFactory elApplicationCheckerFactory;
    @Mock PageFinder pageFinder;
    @Mock ELFinder elFinder;
    @Mock StaticELResolver elResolver;
    @Mock ELError elError;

    @Before
    public void setup() {
        elApplicationChecker = spy(new ELApplicationChecker());
        emptyErrorCollection = Collections.emptyList(); // Have to do this in two steps... Generics hooray!
        emptyErrorCollection = Collections.unmodifiableCollection(emptyErrorCollection);
        emptyELExpressionCollection = Collections.emptyList();
        emptyELExpressionCollection = Collections.unmodifiableCollection(emptyELExpressionCollection);
        oneErrorCollection = new ArrayList<ELError>();
        oneErrorCollection.add(elError);
        twoErrorCollection = new ArrayList<ELError>(oneErrorCollection);
        twoErrorCollection.addAll(oneErrorCollection);
        emptyPagesCollection = Collections.emptyList();
    }

    /**
     * Tests {@link ELApplicationChecker#checkApplication(java.io.File)} with a file that does not exist.
     *
     * @throws Exception
     */
    @Test(expected = FileNotFoundException.class)
    public void testCheckApplication_FileNotFound() throws Exception {
        // Mock setup
        doReturn(applicationArchiveFactory).when(elApplicationChecker).getApplicationArchiveFactory();
        when(applicationArchiveFactory.createApplicationArchive(any(File.class))).thenThrow(
            FileNotFoundException.class);

        elApplicationChecker.checkApplication(applicationFile);
    }

    /**
     * Tests {@link ELApplicationChecker#checkApplication(java.io.File)} with an unsupported archive format.
     *
     * @throws Exception
     */
    @Test(expected = ArchiveFormatUnsupportedException.class)
    public void testCheckApplication_ArchiveFormatUnsupported() throws Exception {
        // Mock setup
        doReturn(applicationArchiveFactory).when(elApplicationChecker).getApplicationArchiveFactory();
        when(applicationArchiveFactory.createApplicationArchive(any(File.class))).thenThrow(
            ArchiveFormatUnsupportedException.class);

        elApplicationChecker.checkApplication(applicationFile);
    }

    /**
     * Tests {@link ELApplicationChecker#checkApplication(java.io.File)} with an archive that cannot be unpacked due
     * to some I/O error.
     *
     * @throws Exception
     */
    @Test(expected = IOException.class)
    public void testCheckApplication_IOException() throws Exception {
        doReturn(applicationArchiveFactory).when(elApplicationChecker).getApplicationArchiveFactory();
        when(applicationArchiveFactory.createApplicationArchive(any(File.class))).thenThrow(IOException.class);

        elApplicationChecker.checkApplication(applicationFile);
    }

    /**
     * Tests {@link ELApplicationChecker#checkApplication(java.io.File)} without registered checkers.
     *
     * @throws Exception
     */
    @Test
    public void testCheckApplication_WithoutRegisteredCheckers() throws Exception {
        Exception caughtException = null;

        // Mock setup
        doReturn(applicationArchiveFactory).when(elApplicationChecker).getApplicationArchiveFactory();
        when(applicationArchiveFactory.createApplicationArchive(any(File.class))).thenReturn(applicationArchive);
        when(applicationArchive.getApplicationFile()).thenReturn(applicationFile);
        when(applicationFile.getAbsolutePath()).thenReturn("somepath");

        // Run
        try {
            elApplicationChecker.checkApplication(applicationFile);
        } catch (ApplicationNotSupportedException e) {
            caughtException = e;
        }

        assertThat(caughtException, is(notNullValue()));
    }

    /**
     * Tests {@link ELApplicationChecker#checkApplication(java.io.File)} one registered (mocked) checker that reports
     * not being able to check the application.
     *
     * @throws Exception
     */
    @Test
    public void testCheckApplication_WithOneRegisteredIncapableChecker() throws Exception {
        Exception caughtException = null;

        // Mock setup
        doReturn(emptyErrorCollection).when(elApplicationChecker)
            .checkExpressions(anyCollectionOf(LocationAwareELExpression.class), any(StaticELResolver.class));
        doReturn(applicationArchiveFactory).when(elApplicationChecker).getApplicationArchiveFactory();
        when(applicationArchiveFactory.createApplicationArchive(any(File.class))).thenReturn(applicationArchive);
        when(applicationArchive.getApplicationFile()).thenReturn(applicationFile);
        when(applicationFile.getAbsolutePath()).thenReturn("somepath");
        when(elApplicationCheckerFactory.canHandleApplication(applicationArchive)).thenReturn(false);

        // Run
        elApplicationChecker.addSupportedELCheckerApplicationFactory(elApplicationCheckerFactory);
        try {
            elApplicationChecker.checkApplication(applicationFile);
        } catch (ApplicationNotSupportedException e) {
            caughtException = e;
        }

        assertThat(caughtException, is(notNullValue()));
    }

    /**
     * Tests {@link ELApplicationChecker#checkApplication(java.io.File)} one registered (mocked) checker that reports
     * being capable of handling the application.
     *
     * @throws Exception
     */
    @Test
    public void testCheckApplication_WithOneRegisteredCapableChecker() throws Exception {
        // Mock setup
        doReturn(emptyErrorCollection).when(elApplicationChecker)
            .checkExpressions(anyCollectionOf(LocationAwareELExpression.class), any(StaticELResolver.class));
        doReturn(applicationArchiveFactory).when(elApplicationChecker).getApplicationArchiveFactory();
        when(applicationArchiveFactory.createApplicationArchive(any(File.class))).thenReturn(applicationArchive);
        when(elApplicationCheckerFactory.canHandleApplication(applicationArchive)).thenReturn(true);
        when(elApplicationCheckerFactory.createPageFinder(applicationArchive)).thenReturn(pageFinder);
        when(elApplicationCheckerFactory.createELFinder(applicationArchive)).thenReturn(elFinder);
        when(elApplicationCheckerFactory.createELResolver(applicationArchive)).thenReturn(elResolver);
        when(pageFinder.findPages(any(ApplicationArchive.class))).thenReturn(emptyPagesCollection);
        when(elFinder.findELExpressions(anyCollectionOf(URI.class), any(ApplicationArchive.class))).thenReturn(
            new Pair<Collection<LocationAwareELExpression>, Collection<ELError>>(emptyELExpressionCollection,
                                                                                 oneErrorCollection));
        when(elApplicationChecker.checkExpressions(anyCollectionOf(LocationAwareELExpression.class),
                                                   any(StaticELResolver.class))).thenReturn(oneErrorCollection);

        // Run
        elApplicationChecker.addSupportedELCheckerApplicationFactory(elApplicationCheckerFactory);
        Collection<ELError> result = elApplicationChecker.checkApplication(applicationFile);

        verify(elApplicationChecker).checkExpressions(emptyELExpressionCollection, elResolver);
        assertThat(result, is(equalTo(twoErrorCollection)));
    }


    /**
     * Tests that {@link ELApplicationChecker#checkExpressions(java.util.Collection, StaticELResolver)} does not report
     * any
     * errors when there are no expressions to check.
     */
    @Test
    public void testFindErrors() {
        LocationAwareELExpression locationAwareELExpression = mock(LocationAwareELExpression.class);
        ELError elError1 = new ELError("error message", locationAwareELExpression, ELError.ELErrorSeverity.WARNING);
        ELCheckerException elCheckerException = new ELCheckerException(elError1);
        Collection<LocationAwareELExpression> elExpressions = new ArrayList<>();
        elExpressions.add(locationAwareELExpression);
        elExpressions.add(locationAwareELExpression);

        when(locationAwareELExpression.getValue(elResolver)).thenThrow(elCheckerException);

        Collection<ELError> foundErrors = elApplicationChecker.checkExpressions(elExpressions, elResolver);

        assertThat(foundErrors.size(), is(equalTo(2)));
        for (ELError foundError : foundErrors) {
            assertThat(foundError, is(theInstance(elError1)));
        }
    }
}
