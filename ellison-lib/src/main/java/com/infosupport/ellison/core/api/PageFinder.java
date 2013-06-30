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

import java.net.URI;
import java.util.Collection;

/**
 * Class used to find all pages in an application.
 *
 * @author StefanZ
 */
public interface PageFinder {
    /**
     * Finds all pages within the specified application archive.
     *
     * @param applicationArchive
     *     the archive of the application within which to find all pages
     *
     * @return a list of found pages
     */
    Collection<URI> findPages(ApplicationArchive applicationArchive);
}
