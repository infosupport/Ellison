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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Implements {@link ApplicationArchive} for EAR (Enterprise Archive).
 *
 * @author StefanZ
 */
public class WARApplicationArchive extends ApplicationArchive {
    private static final Logger LOGGER = LoggerFactory.getLogger(WARApplicationArchive.class);
    private ClassLoader cachedClassLoader = null;

    /**
     * Constructor.
     *
     * @param applicationFile
     *     the application archive file to use
     *
     * @throws IOException
     *     when there was an error unpacking the application archive. Specifically, a {@link FileNotFoundException} is
     *     thrown if {@code applicationFile} does not point to an existing file.
     * @see ApplicationArchive#ApplicationArchive(java.io.File)
     */
    public WARApplicationArchive(File applicationFile) throws IOException {
        super(applicationFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader() {
        ClassLoader warClassLoader = null;
        Collection<URI> libFiles = null;

        if (cachedClassLoader != null) {
            return cachedClassLoader;
        }

        Collection<URL> urlList = new ArrayList<>();
        URL mainURL = null;
        URL classesURL = null;

        try {
            mainURL = getUnpackedPath().toURI().toURL();
            classesURL = new URL(mainURL, "WEB-INF/classes/");
        } catch (MalformedURLException e) {
            // This should never happen, as long as unpackJar() returns a valid path
            LOGGER.warn("Somehow, a malformed URL got into WARApplicationArchive.getClassLoader().", e);
        }

        if (mainURL != null && classesURL != null) {
            urlList.add(mainURL);
            urlList.add(classesURL);
            try {
                libFiles = findFilesByGlobPattern("WEB-INF/lib", "*.jar", false);
            } catch (FileNotFoundException e) {
                LOGGER.info("No libraries in this application archive.");
            }

            if (libFiles != null) {
                Collection<URL> jarURLS = Collections2.transform(libFiles, new JarURIToURLTransform());
                urlList.addAll(jarURLS);
            }
            
            warClassLoader =
                new URLClassLoader(urlList.toArray(new URL[urlList.size()]), this.getClass().getClassLoader());
        }

        cachedClassLoader = warClassLoader;

        return warClassLoader;
    }

    /**
     * {@link Function} that transforms a file to a URL for which connections may <strong>not</strong> be cached.
     *
     * @see URLConnection#setUseCaches(boolean)
     * @see URLConnection#setDefaultUseCaches(boolean)
     */
    class JarURIToURLTransform implements Function<URI, URL> {
        @Nullable
        @Override
        public URL apply(@Nullable URI input) {
            URL jarURL = null;
            try {
                jarURL = new URL("jar:" + resolvePath(input).toString() + "!/");
            } catch (IOException e) {
                LOGGER.warn(String.format("Could not add JAR '%s' to classloader for application archive '%s'",
                                          input.toString(), getApplicationFile().getAbsolutePath()), e);
            }


            /* Connection caching basically prevents deleting the
               backing files (I dare you, remove the next block,
               and watch your temp directory filling up).
               Turn off connection caching for these libraries,
               as we might want to delete them when they're no
               longer being used. */
            if (jarURL != null) {
                URLConnection c = null;
                try {
                    c = jarURL.openConnection();
                    c.setDefaultUseCaches(false);
                    c.setUseCaches(false);
                } catch (IOException e) {
                    LOGGER.error("Caught an exception trying to open a connection to what should be an existing "
                                     + "file. This should NEVER happen.", e);
                }
            }

            return jarURL;
        }
    }
}
