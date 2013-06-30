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
package com.infosupport.ellison.core.util;

/**
 * Class containing all relevant constants for this application.
 *
 * @author StefanZ
 */
public final class Constants {
    /**
     * The prefix for temporary directories created by this application.
     */
    public static final String TEMP_DIR_PREFIX = "el-static-checker_";

    /**
     * The size of the buffer used to unpack files in JARs.
     */
    public static final int UNPACK_BUFFER_SIZE = 4096;

    /**
     * Constructor. This is private, as this is a utility class.
     */
    private Constants() {
    }
}
