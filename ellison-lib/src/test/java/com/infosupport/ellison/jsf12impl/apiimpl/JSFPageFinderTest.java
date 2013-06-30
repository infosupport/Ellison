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
package com.infosupport.ellison.jsf12impl.apiimpl;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.net.URI;
import java.util.Collection;

import javax.servlet.ServletContext;

import org.junit.BeforeClass;
import org.junit.Test;

import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.infosupport.ellison.core.archive.ApplicationArchiveFactory;
import com.infosupport.ellison.jsf12impl.apiimpl.JSFPageFinder;
import com.infosupport.ellison.jsf12impl.util.ServletContextFactory;

/**
 * Tests {@link JSFPageFinder}.
 *
 * @author StefanZ
 */
public class JSFPageFinderTest {
    private static ApplicationArchive applicationArchive;
    private static ApplicationArchive applicationArchiveNoWebInf;
    private static Collection<URI> actualPages;

    @BeforeClass
    public static void setupClass() throws Exception {
        ApplicationArchiveFactory applicationArchiveFactory = ApplicationArchiveFactory.getInstance();
        applicationArchive = applicationArchiveFactory.createApplicationArchive(
            new File(JSFPageFinder.class.getResource("/jsf12-helloworld-noclasses.war").toURI()));
        applicationArchiveNoWebInf = applicationArchiveFactory.createApplicationArchive(
            new File(JSFPageFinder.class.getResource("/jsf12-helloworld-nowebinf.war").toURI()));

        actualPages = applicationArchive.findFilesByGlobPattern(null, "*.xhtml");
    }

    @Test
    public void testFindPages_Success() throws Exception {
        Thread.currentThread().setContextClassLoader(applicationArchive.getClassLoader());
        ServletContext servletContext = ServletContextFactory.createServletContext(applicationArchive);
        JSFPageFinder jsfPageFinder = new JSFPageFinder(servletContext);
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        Collection<URI> foundPages = jsfPageFinder.findPages(applicationArchive);

        Thread.currentThread().setContextClassLoader(originalClassLoader);

        assertThat(actualPages.containsAll(foundPages), is(true));
        assertThat(foundPages.containsAll(actualPages), is(true));
    }

    /**
     * Tests {@link JSFPageFinder#findPages(com.infosupport.ellison.core.archive.ApplicationArchive)
     * }, pretending that an error has occurred while trying to parse the web deployment descriptor (web.xml).
     *
     * @throws Exception
     */
    @Test
    public void testFindPages_NoWebInf() throws Exception {
        JSFPageFinder jsfPageFinder = new JSFPageFinder(null);
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        Thread.currentThread().setContextClassLoader(applicationArchiveNoWebInf.getClassLoader());

        Collection<URI> foundPages = jsfPageFinder.findPages(applicationArchiveNoWebInf);

        Thread.currentThread().setContextClassLoader(originalClassLoader);

        assertThat(foundPages.size(), is(equalTo(0)));
    }
}
