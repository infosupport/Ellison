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

import java.net.URI;

/**
 * Represents a location within a uri.
 *
 * @author StefanZ
 */
public class Location {
    private URI uri;
    private int line;
    private int column;

    /**
     * Constructor.
     *
     * @param uri
     *     the uri this location points to
     * @param line
     *     the line of the location in the uri
     * @param column
     *     the column of the location in the uri
     */
    public Location(URI uri, int line, int column) {
        this.column = column;
        this.uri = uri;
        this.line = line;
    }

    /**
     * Get the column index within the file this location points to.
     *
     * @return the column index within the file this location points to
     */
    public int getColumn() {
        return column;
    }

    /**
     * Get the uri this location points to.
     *
     * @return the uri this location points to.
     */
    public URI getUri() {
        return uri;
    }

    /**
     * Get the line index within the file this location points to.
     *
     * @return the line index within the file this location points to
     */
    public int getLine() {
        return line;
    }
}
