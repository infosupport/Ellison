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

import com.infosupport.ellison.core.api.ELFinder;
import com.infosupport.ellison.core.api.PageFinder;
import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.infosupport.ellison.core.archive.WARApplicationArchive;
import com.infosupport.ellison.jsf12impl.apiimpl.JSFELApplicationCheckerFactory;
import com.infosupport.ellison.jsf12impl.exceptions.ApplicationConfigurationException;
import com.infosupport.ellison.jsf12impl.resolvers.JSFELResolver;

import java.io.File;
import javax.el.ELResolver;
import javax.servlet.ServletContext;
import junitx.util.PrivateAccessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JSFELApplicationCheckerFactory} implementation.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class JSFELApplicationCheckerFactoryTest {
    @Mock ApplicationArchive applicationArchive;
    @Mock File applicationFile;
    @Mock ServletContext servletContext;
    JSFELApplicationCheckerFactory applicationCheckerFactory;
    ApplicationArchive jsf12ApplicationArchive;
    ClassLoader originalClassLoader;

    @Before
    public void setup() throws Exception {
        applicationCheckerFactory = spy(new JSFELApplicationCheckerFactory());
        File jsf12ArchiveFile = new File(getClass().getResource("/jsf12-helloworld.war").toURI());
        jsf12ApplicationArchive = new WARApplicationArchive(jsf12ArchiveFile);

        originalClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(jsf12ApplicationArchive.getClassLoader());
    }

    @After
    public void teardown() throws Exception {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    @Test
    public void testCreatePageFinder_Success() throws Exception {
        doReturn(servletContext).when(applicationCheckerFactory).getServletContext(applicationArchive);

        PageFinder pageFinder = applicationCheckerFactory.createPageFinder(applicationArchive);

        assertThat(pageFinder, is(notNullValue()));
    }

    @Test
    public void testCreatePageFinder_ApplicationConfigurationException() throws Exception {
        doThrow(ApplicationConfigurationException.class).when(applicationCheckerFactory)
            .getServletContext(applicationArchive);

        PageFinder pageFinder = applicationCheckerFactory.createPageFinder(applicationArchive);

        assertThat(pageFinder, is(nullValue()));
    }

    @Test
    public void testCreateELFinder() throws Exception {
        doReturn(servletContext).when(applicationCheckerFactory).getServletContext(applicationArchive);

        ELFinder elFinder = applicationCheckerFactory.createELFinder(applicationArchive);

        assertThat(elFinder, is(notNullValue()));
    }

    /**
     * Tests whether creating an ELResolver succeeds. We use an actual WAR archive here,
     * because mocking the entire path to the BeanManager holding information about registered managed beans is
     * cumbersome (for {@link com.infosupport.ellison.jsf12impl.resolvers.ManagedBeanELResolver}),
     * plus, it's possible more resolvers will be added to the chain that is {@link JSFELResolver},
     * for which we don't know what the dependencies are.
     */
    @Test
    public void testCreateELResolver_Success() throws Exception {
        ELResolver elResolver = applicationCheckerFactory.createELResolver(jsf12ApplicationArchive);

        assertThat(elResolver, is(notNullValue()));
    }

    @Test
    public void testCreateELResolver_ApplicationConfigurationException() throws Exception {
        doThrow(ApplicationConfigurationException.class).when(applicationCheckerFactory)
            .getServletContext(applicationArchive);

        ELResolver elResolver = applicationCheckerFactory.createELResolver(applicationArchive);

        assertThat(elResolver, is(nullValue()));
    }

    /**
     * Tests whether {@code canHandleFactory()} handles thrown {@link ApplicationConfigurationException} exceptions
     * gracefully.
     */
    @Test
    public void testCanHandleApplication_ApplicationConfigurationException() throws Exception {
        doThrow(ApplicationConfigurationException.class).when(applicationCheckerFactory)
            .getServletContext(applicationArchive);
        when(applicationArchive.getApplicationFile()).thenReturn(applicationFile);
        when(applicationFile.getName()).thenReturn("bladibla");

        boolean canHandle = applicationCheckerFactory.canHandleApplication(applicationArchive);

        assertThat(canHandle, is(false));
    }

    /**
     * Tests the success scenario (successfully create a servlet context for the archive).
     */
    @Test
    public void testCanHandleApplication_Success() throws Exception {
        doReturn(servletContext).when(applicationCheckerFactory).getServletContext(applicationArchive);

        boolean canHandle = applicationCheckerFactory.canHandleApplication(applicationArchive);

        assertThat(canHandle, is(true));
    }

    /**
     * Make sure that created servlet contexts are cached, as instantiating one is quite costly.
     */
    @Test
    public void testGetServletContext_GetCache() throws Exception {
        PrivateAccessor.setField(applicationCheckerFactory, "lastCheckedApplicationArchive", applicationArchive);
        PrivateAccessor.setField(applicationCheckerFactory, "lastServletContext", servletContext);

        ServletContext returnedServletContext = applicationCheckerFactory.getServletContext(applicationArchive);

        assertThat(returnedServletContext, is(sameInstance(servletContext)));
    }

    /**
     * Make sure that a created servlet context gets saved in the instance's cache.
     */
    @Test
    public void testGetServletContext_SaveCache() throws Exception {


        ServletContext returnedServletContext = applicationCheckerFactory.getServletContext(jsf12ApplicationArchive);
        ServletContext savedServletContext =
            (ServletContext) PrivateAccessor.getField(applicationCheckerFactory, "lastServletContext");
        ApplicationArchive savedApplicationArchive =
            (ApplicationArchive) PrivateAccessor.getField(applicationCheckerFactory, "lastCheckedApplicationArchive");

        assertThat(savedServletContext, is(sameInstance(returnedServletContext)));
        assertThat(savedApplicationArchive, is(sameInstance(jsf12ApplicationArchive)));
    }
}
