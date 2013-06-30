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

/**
 * Represents a single error in/problem with an Expression Language expression.
 *
 * @author StefanZ
 */
public class ELError {
    private final LocationAwareELExpression elExpression;
    private final String message;
    private final ELErrorSeverity type;

    /**
     * Constructs.
     *
     * @param message
     *     A message to indicate what the error is.
     * @param elExpression
     *     The expression containing the error
     * @param type
     *     the type of error
     */
    public ELError(String message, LocationAwareELExpression elExpression, ELErrorSeverity type) {
        this.elExpression = elExpression;
        this.type = type;
        this.message = message;
    }

    /**
     * Get the expression that caused this error.
     *
     * @return the expression that caused this error.
     */
    public LocationAwareELExpression getElExpression() {
        return elExpression;
    }

    /**
     * Get the type of this error.
     *
     * @return the type of this error
     */
    public ELErrorSeverity getType() {
        return type;
    }

    /**
     * Get the message describing the error.
     *
     * @return the message describing the error
     */
    public String getMessage() {
        return message;
    }

    /**
     * Enumeration containing the severities of errors one can expect in Expression Language.
     *
     * @author StefanZ
     */
    public static enum ELErrorSeverity {
        /**
         * Gives information about some expression.
         */
        INFO,
        /**
         * Warning about an expression.
         */
        WARNING,
        /**
         * The expression will cause an error, or is otherwise buggy.
         */
        ERROR,
    }
}
