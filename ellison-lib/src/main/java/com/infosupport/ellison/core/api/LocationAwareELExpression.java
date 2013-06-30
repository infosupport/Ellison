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

import javax.el.ELResolver;

import com.infosupport.ellison.core.util.Location;

/**
 * Wrapper around Expression Language expressions that gives an indication about where the expression came from.
 *
 * @author StefanZ
 */
public abstract class LocationAwareELExpression {
    private Location location;

    /**
     * Constructor.
     *
     * @param location
     *     the location the EL expression came from
     */
    public LocationAwareELExpression(Location location) {
        this.location = location;
    }

	/**
	 * Resolve the value the expression refers to. Can throw a runtime ELCheckerException when this expression can or will lead to
	 * problems at runtime. This exception contains an instance of {@link ELError} which indicates what kind of problem was found.
	 * All subclasses <strong>must</strong> catch any and all exceptions, and wrap any relevant ones into {@code ELCheckerException}
	 * s.
	 * 
	 * @param resolver
	 *            the resolver to use to resolve the value of the expression
	 * 
	 * @return the value the expression points to
	 * 
	 */
	public abstract Object getValue(ELResolver resolver);

    /**
     * Get the string representation of this EL expression (i.e. the original expression).
     *
     * @return the string representation of this expression
     */
    public abstract String getExpressionString();

    /**
     * Get the location of the expression.
     *
     * @return the location of the expression
     */
    public Location getLocation() {
        return location;
    }
}
