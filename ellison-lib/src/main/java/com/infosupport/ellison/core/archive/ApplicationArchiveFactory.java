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

import javax.enterprise.deploy.shared.ModuleType;

import com.infosupport.ellison.core.exceptions.ArchiveFormatUnsupportedException;

/**
 * {@code ApplicationArchive} factory.
 *
 * @author StefanZ
 */
public class ApplicationArchiveFactory {
    private static final String EXCEPTION_FORMAT = "Cannot handle application archive '%s': %s";
    private static ApplicationArchiveFactory instance = new ApplicationArchiveFactory();

    protected ApplicationArchiveFactory() {
    }

    /**
     * Get the singleton instance.
     *
     * @return the only instance of this class
     */
    public static ApplicationArchiveFactory getInstance() {
        return instance;
    }

    /**
     * Get the extension for a filename, <strong>without</strong> the period.
     *
     * @param fileName
     *     the filename to get the extension for
     *
     * @return The extension (the part after the last period) if the filename contains a period, {@code null} if no
     *         period was found.
     */
    public static String getFileNameExtensionWithoutPeriod(String fileName) {
        String extension = getFileNameExtensionWithPeriod(fileName);

        if (extension != null && extension.length() != 0) {
            extension = extension.substring(1);
        }

        return extension;
    }

    /**
     * Get the extension for a filename, <strong>with</strong> the period.
     *
     * @param fileName
     *     the filename to get the extension for
     *
     * @return The extension (the part after the last period) if the filename contains a period, {@code null} if no
     *         period was found.
     */
    public static String getFileNameExtensionWithPeriod(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        String extension = null;

        if (lastDotIndex != -1) {
            extension = fileName.substring(lastDotIndex);
        }

        return extension;
    }

    /**
     * Depending on the extension of the supplied file, returns a specific {@code ApplicationArchive} implementation
     * instance.
     *
     * @param application
     *     the application archive file to handle
     *
     * @return an instance of {@code ApplicationArchive} capable of handling the given {@code application}
     *
     * @throws com.infosupport.ellison.core.exceptions.ArchiveFormatUnsupportedException
     *     if the application archive has no extension, or the extension is unknown.
     * @throws IOException
     *     if there was an error unpacking {@code application}. Specifically, a {@link java.io.FileNotFoundException}
     *     will be thrown if {@code application} refers to a non-existent file
     * @see EARApplicationArchive
     * @see WARApplicationArchive
     */
    public ApplicationArchive createApplicationArchive(File application)
        throws ArchiveFormatUnsupportedException, IOException {
        String fileExtension = getFileNameExtensionWithPeriod(application.getName());

        if (fileExtension == null) {
            throw new ArchiveFormatUnsupportedException(
                String.format(EXCEPTION_FORMAT, application.getAbsolutePath(), "no extension"));
        } else if (fileExtension.equalsIgnoreCase(ModuleType.WAR.getModuleExtension())) {
            // This is Sparta!
            return new WARApplicationArchive(application);
        } else if (fileExtension.equalsIgnoreCase(ModuleType.EAR.getModuleExtension())) {
            return new EARApplicationArchive(application);
        } else {
            throw new ArchiveFormatUnsupportedException(String.format(EXCEPTION_FORMAT, application.getAbsolutePath(),
                                                                      "unknown extension '" + fileExtension + "'"));
        }
    }
}
