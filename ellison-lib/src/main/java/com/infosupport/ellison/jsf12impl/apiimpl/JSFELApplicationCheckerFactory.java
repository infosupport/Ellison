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

import com.infosupport.ellison.core.api.ELApplicationCheckerFactory;
import com.infosupport.ellison.core.api.ELFinder;
import com.infosupport.ellison.core.api.PageFinder;
import com.infosupport.ellison.core.api.StaticELResolver;
import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.infosupport.ellison.jsf12impl.exceptions.ApplicationConfigurationException;
import com.infosupport.ellison.jsf12impl.resolvers.JSFELResolver;
import com.infosupport.ellison.jsf12impl.util.ServletContextFactory;

import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ELApplicationCheckerFactory} for the JSF 1.2 implementation.
 *
 * @author StefanZ
 */
public class JSFELApplicationCheckerFactory implements ELApplicationCheckerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSFELApplicationCheckerFactory.class);
    /**
     * Keeps track of the last checked application archive.
     * This field is used to make sure the contents of {@code lastServletContext} are related to this application
     * instance. If not, then a new {@link ServletContext} needs to be instantiated.
     */
    private ApplicationArchive lastCheckedApplicationArchive;
    /**
     * The servlet context to be used in combination with {@code lastCheckedApplicationArchive}.
     * This field is used to cache the configuration for the application archive, which takes a while to parse.
     */
    private ServletContext lastServletContext;

    /**
     * Create a {@link PageFinder}.
     *
     * @param applicationArchive
     *     the application archive to create a {@code PageFinder} for
     *
     * @return a {@link PageFinder} capable of finding JSF pages, or {@code null} if no {@link ServletContext} could
     *         be created. To make sure the latter doesn't happen, always call
     *         {@link #canHandleApplication(com.infosupport.ellison.core.archive.ApplicationArchive)}
     *         on the same {@code ApplicationArchive} first.
     */
    @Override
    public PageFinder createPageFinder(ApplicationArchive applicationArchive) {
        PageFinder pageFinder = null;

        try {
            pageFinder = new JSFPageFinder(getServletContext(applicationArchive));
        } catch (ApplicationConfigurationException e) {
            LOGGER.error(
                "Could not create a PageFinder. Did you check if this factory can handle the application archive?", e);
        }
        return pageFinder;
    }

    /**
     * Create an {@link ELFinder}.
     *
     * @param applicationArchive
     *     the application archive to create a {@code PageFinder} for
     *
     * @return an {@link ELFinder} capable of finding EL expressions within JSF pages
     */
    @Override
    public ELFinder createELFinder(ApplicationArchive applicationArchive) {
        return new JSFELFinder();
    }

    /**
     * Create the {@link StaticELResolver} tasked with resolving all found EL expressions within the application, and
     * report all found problems.
     *
     * @param applicationArchive
     *     the application archive to create a {@code PageFinder} for
     *
     * @return an EL resolver capable of resolving correct expressions in JSF applications, or {@code null} if no
     *         {@link ServletContext} could be created. To make sure the latter doesn't happen, always call
     *         {@link #canHandleApplication(com.infosupport.ellison.core.archive.ApplicationArchive)}
     *         on the same {@code ApplicationArchive} first.
     */
    @Override
    public StaticELResolver createELResolver(ApplicationArchive applicationArchive) {
        StaticELResolver elResolver = null;

        try {
            elResolver = new JSFELResolver(getServletContext(applicationArchive));
        } catch (ApplicationConfigurationException e) {
            LOGGER.error("Could not create a StaticELResolver. Did you check if this factory can handle the application"
                             + " archive?", e);
        }

        return elResolver;
    }

    /**
     * Indicates whether this implementation is capable of handling the supplied application.
     *
     * @param applicationArchive
     *     the archive of the application to check
     *
     * @return {@code true} if this implementation can handle the application, otherwise {@code false}
     */
    @Override
    public boolean canHandleApplication(ApplicationArchive applicationArchive) {
        boolean canHandleApplicationArchive = false;

        try {
            getServletContext(applicationArchive);
            canHandleApplicationArchive = true;
        } catch (ApplicationConfigurationException e) {
            LOGGER.info(String.format("Could not create a servlet context for application archive '%s'",
                                      applicationArchive.getApplicationFile().getName()), e);
        }

        return canHandleApplicationArchive;
    }

    protected ServletContext getServletContext(ApplicationArchive applicationArchive)
        throws ApplicationConfigurationException {
        ServletContext servletContext;

        if (applicationArchive.equals(lastCheckedApplicationArchive)) {
            servletContext = lastServletContext;
        } else {
            servletContext = ServletContextFactory.createServletContext(applicationArchive);
            lastCheckedApplicationArchive = applicationArchive;
            lastServletContext = servletContext;
        }

        return servletContext;
    }
}
