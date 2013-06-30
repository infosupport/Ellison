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
package com.infosupport.ellison.jsf12impl.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.infosupport.ellison.core.archive.ApplicationArchiveFactory;
import com.infosupport.ellison.jsf12impl.util.FacesServletConfigImpl;

/**
 * Tests {@link FacesServletConfigImpl}.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class FacesServletConfigImplTest {
    private static ApplicationArchive applicationArchive;
    private static ApplicationArchive applicationArchiveNoFacesServlet;

    @BeforeClass
    public static void setupClass() throws Exception {
        ApplicationArchiveFactory applicationArchiveFactory = ApplicationArchiveFactory.getInstance();
        applicationArchive = applicationArchiveFactory.createApplicationArchive(
            new File(FacesServletConfigImplTest.class.getResource("/jsf12-helloworld.war").toURI()));
        applicationArchiveNoFacesServlet = applicationArchiveFactory.createApplicationArchive(
            new File(FacesServletConfigImplTest.class.getResource("/jsf12-helloworld-nofacesservlet.war").toURI()));
    }

    @Test
    public void testConstructor_Success() throws Exception {
        new FacesServletConfigImpl(applicationArchive);
    }

    @Test
    public void testConstructor_NoFacesServlet() throws Exception {
        Exception caughtException = null;

        try {
            new FacesServletConfigImpl(applicationArchiveNoFacesServlet);
        } catch (Exception e) {
            caughtException = e;
        }

        assertThat(caughtException, is(not(nullValue())));
        assertThat(caughtException.getCause(), is(nullValue()));
    }
}
