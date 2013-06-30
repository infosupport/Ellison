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
package com.infosupport.ellison.jsf12impl.exceptions;

/**
 * Exception used to indicate an error in the configuration for an application.
 *
 * @author StefanZ
 */
public class ApplicationConfigurationException extends Exception {
    /**
     * Constructor.
     *
     * @param cause
     *     the exception that caused this one to be thrown
     */
    public ApplicationConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     *
     * @param message
     *     the message to associate with this exception
     * @param cause
     *     the exception that caused this one to be thrown
     */
    public ApplicationConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     *
     * @param message
     *     the message to associate with this exception
     */
    public ApplicationConfigurationException(String message) {
        super(message);
    }
}
