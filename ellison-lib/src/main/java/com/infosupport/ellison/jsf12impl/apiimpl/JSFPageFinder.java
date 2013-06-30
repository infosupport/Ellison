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

import com.infosupport.ellison.core.api.PageFinder;
import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.sun.faces.config.WebConfiguration;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @inheritDoc
 */
public class JSFPageFinder implements PageFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger(JSFPageFinder.class);
    private ServletContext servletContext = null;

    /**
     * Constructor.
     *
     * @param servletContext
     *     the servlet context for the application to find pages in
     */
    public JSFPageFinder(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public Collection<URI> findPages(ApplicationArchive applicationArchive) {
        Collection<URI> pageFiles = new ArrayList<>();

        if (servletContext != null) {
            String defaultExtension = WebConfiguration.getInstance(servletContext)
                .getOptionValue(WebConfiguration.WebContextInitParameter.JspDefaultSuffix);

            try {
                pageFiles = applicationArchive.findFilesByGlobPattern(null, "*" + defaultExtension);
            } catch (FileNotFoundException e) {
                LOGGER.warn(String.format("Could not find pages in application archive '%s'",
                                          applicationArchive.getApplicationFile().getName()), e);
            }
        }

        return pageFiles;
    }
}
