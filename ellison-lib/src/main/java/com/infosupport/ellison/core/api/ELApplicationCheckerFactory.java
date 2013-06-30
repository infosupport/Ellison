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
package com.infosupport.ellison.core.api;

import com.infosupport.ellison.core.archive.ApplicationArchive;

/**
 * Service Provider facade for static Expression Language checkers. This is the class used to access implementations
 * for
 * {@link ELFinder}, {@link PageFinder} and {@link com.infosupport.ellison.core.util.ELResolverChain}.
 * Furthermore, this class is tasked with indicating
 * whether it can handle an application or not.
 * <p/>
 * Implementations of this interface will be loaded using {@link java.util.ServiceLoader}, so as to keep a as low a
 * coupling as possible.
 *
 * @author StefanZ
 * @see java.util.ServiceLoader
 */
public interface ELApplicationCheckerFactory {
    /**
     * Create a {@link PageFinder}.
     *
     * @param applicationArchive
     *     the application archive to create a {@code PageFinder} for
     *
     * @return An instance of {@link PageFinder} that can find "pages". For a JSF application, this would be a list of
     *         files having whatever extension was configured in the servlet configuration.
     */
    PageFinder createPageFinder(ApplicationArchive applicationArchive);

    /**
     * Create an {@link ELFinder}.
     *
     * @param applicationArchive
     *     the application archive to create an {@code ELFinder} for
     *
     * @return An instance of {@link ELFinder} that can find EL Expressions according to the supported underlying
     *         framework implemented by this Service Provider.
     *
     * @see ELFinder
     */
    ELFinder createELFinder(ApplicationArchive applicationArchive);

    /**
     * Create the {@link StaticELResolver} tasked with resolving all found EL expressions within the application, and
     * report all found problems.
     *
     * @param applicationArchive
     *     the application archive to create an {@code StaticELResolver} for
     *
     * @return An instance of {@link StaticELResolver}
     */
    StaticELResolver createELResolver(ApplicationArchive applicationArchive);

    /**
     * Indicates whether this implementation is capable of handling the supplied application.
     *
     * @param applicationArchive
     *     the archive of the application to check
     *
     * @return {@code true} if this implementation can handle the application, otherwise {@code false}
     */
    boolean canHandleApplication(ApplicationArchive applicationArchive);
}
