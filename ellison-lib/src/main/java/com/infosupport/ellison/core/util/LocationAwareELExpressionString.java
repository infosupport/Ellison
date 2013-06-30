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

import javax.el.ELResolver;

import com.infosupport.ellison.core.api.LocationAwareELExpression;

/**
 * {@link com.infosupport.ellison.core.api.LocationAwareELExpression} used to hold only the string
 * representation of an expression, to be used in {@link
 * com.infosupport.ellison.core.api.ELError}s for found expressions that could not be parsed.
 *
 * @author StefanZ
 */
public class LocationAwareELExpressionString extends LocationAwareELExpression {
    private String expressionString;

    /**
     * Constructor.
     *
     * @param expressionString
     *     the expression string that caused this error
     * @param location
     *     the location the EL expression came from
     */
    public LocationAwareELExpressionString(String expressionString, Location location) {
        super(location);

        this.expressionString = expressionString;
    }

    /**
     * DO NOT CALL! This is an invalid operation on this implementation of {@code LocationAwareELExpression}.
     *
     * @param resolver
     *     the resolver to use to resolve the value of the expression
     *
     * @return this method implementation never returns, but throws an {@code UnsupportedOperationException}.
     *
     */
    @Override
    public Object getValue(ELResolver resolver) {
        throw new UnsupportedOperationException("LocationAwareELExpressionString instances cannot be resolved!");
    }

    @Override
    public String getExpressionString() {
        return expressionString;
    }
}
