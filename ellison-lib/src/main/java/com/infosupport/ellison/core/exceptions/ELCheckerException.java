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
package com.infosupport.ellison.core.exceptions;

import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.LocationAwareELExpression;

/**
 * Exception used to indicate that an error was found in an EL expression.
 * <p/>
 * This exception extends {@code RuntimeException} because it must be thrown from {@link
 * com.infosupport.ellison.core.api.StaticELResolver#getValue(javax.el.ELContext, Object, Object)},
 * which
 * overrides {@link
 * javax.el.ELResolver#getValue(javax.el.ELContext, Object, Object)}. As the latter does not have any exceptions in its
 * signature, neither can the overriding method.
 *
 * @author StefanZ
 * @see RuntimeException
 */
public class ELCheckerException extends RuntimeException {
    private final ELError error;

    /**
     * Constructor.
     *
     * @param error
     *     The actual error that must be reported to callers.
     */
    public ELCheckerException(ELError error) {
        super(error.getMessage());
        this.error = error;
    }

    /**
     * Constructor.
     * <p/>
     * This is a convenience constructor which calls {@link ELCheckerException#ELCheckerException(ELError)} with a
     * newly
     * created {@code ELError} using the given parameters
     *
     * @param message
     *     the message associated with the error
     * @param elExpression
     *     the expression associated with the error
     * @param severity
     *     the severity of the error
     *
     * @see ELError .ELErrorSeverity)
     */
    public ELCheckerException(String message, LocationAwareELExpression elExpression,
                              ELError.ELErrorSeverity severity) {
        this(new ELError(message, elExpression, severity));
    }

    /**
     * @return the error associated with this exception
     */
    public ELError getError() {
        return error;
    }
}
