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

import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.infosupport.ellison.jsf12impl.exceptions.ApplicationConfigurationException;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.ConfigureListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Utility class to create a {@link javax.servlet.ServletContext} instance (specifically, a {@link
 * ServletContextImpl}).
 */
public final class ServletContextFactory {
    private ServletContextFactory() {
    }

    /**
     * Creates a {@link javax.servlet.ServletContext}.
     *
     * @param applicationArchive
     *     the application archive to create the servlet context for
     *
     * @return an instance of {@link ServletContextImpl}, with all the configuration read from the application archive
     *
     * @throws com.infosupport.ellison.jsf12impl.exceptions.ApplicationConfigurationException
     *     when an error occurs while trying to extract configuration information from the application
     */
    public static ServletContext createServletContext(ApplicationArchive applicationArchive)
        throws ApplicationConfigurationException {
        ServletConfig servletConfig = new FacesServletConfigImpl(applicationArchive);
        ServletContextImpl servletContext = (ServletContextImpl) servletConfig.getServletContext();
        ServletContextEvent scEvent = new ServletContextEvent(servletContext);
        ServletContextListener scListener = new ConfigureListener();
        scListener.contextInitialized(scEvent);

        ApplicationAssociate applicationAssociate = ApplicationAssociate.getInstance(servletContext);
        ApplicationAssociate.setCurrentInstance(applicationAssociate);

        return servletContext;
    }
}
