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
import com.infosupport.ellison.core.util.Pair;

import java.net.URI;
import java.util.Collection;

/**
 * Utility class to find all Expression Language expressions within an application.
 * <p/>
 * Subclass this to supply your own implementation.
 * <p/>
 *
 * @author StefanZ
 */
public interface ELFinder {
    /**
     * Finds all EL expressions within a list of pages.
     *
     * @param pages
     *     the pages to search for expressions in
     * @param applicationArchive
     *     the {@link com.infosupport.ellison.core.archive.ApplicationArchive} that is currently being
     *     processed
     *
     * @return a pair containing a list of all found syntactically valid EL expressions, and a collection of problems
     *         indicating which expressions weren't correct.
     */
    Pair<Collection<LocationAwareELExpression>, Collection<ELError>> findELExpressions(Collection<URI> pages,
                                                                                       ApplicationArchive
                                                                                           applicationArchive);
}
