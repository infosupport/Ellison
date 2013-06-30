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
import java.io.IOException;

/**
 * Implements {@link ApplicationArchive} for EAR (Enterprise Archive).
 *
 * @author StefanZ
 */
public class EARApplicationArchive extends ApplicationArchive {
    /**
     * Constructor.
     *
     * @param applicationFile
     *     the application archive file to use
     *
     * @throws IOException
     *     when there was an error unpacking the application archive. Specifically, a {@link
     *     java.io.FileNotFoundException} is thrown if {@code applicationFile} does not point to an existing file.
     * @see ApplicationArchive#ApplicationArchive(java.io.File)
     */
    public EARApplicationArchive(File applicationFile) throws IOException {
        super(applicationFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClassLoader getClassLoader() {
        return null;
    }
}
