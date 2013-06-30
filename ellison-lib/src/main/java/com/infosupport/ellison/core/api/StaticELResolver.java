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

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * This class is used to resolve EL expressions statically.
 *
 * @author StefanZ
 */
public abstract class StaticELResolver extends ELResolver {
    /**
     * Resolves a base/property pair from an EL expression.
     *
     * @param context
     *     the context within which the expression is being resolved
     * @param base
     *     the base of the {@code property} (may be null if {@code property} points to a bean directly)
     * @param property
     *     the name of the property. This needs not be a String, but its {@link Object#toString} method should return a
     *     valid property name.
     *
     * @return the type of whatever value would normally have been resolved
     *
     * @throws com.infosupport.ellison.core.exceptions.ELCheckerException
     *     when this expression can or will lead to problems at runtime. This exception contains an instanceof {@link
     *     ELError} which indicates what kind of problem was found. All subclasses <strong>must</strong> catch any and
     *     all exceptions, and wrap any relevant ones into {@code ELCheckerException}s.
     */
    @Override
    public abstract Object getValue(ELContext context, Object base, Object property);

    @Override
    public final Class<?> getType(ELContext context, Object base, Object property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setValue(ELContext context, Object base, Object property, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final boolean isReadOnly(ELContext context, Object base, Object property) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final Class<?> getCommonPropertyType(ELContext context, Object base) {
        throw new UnsupportedOperationException();
    }
}
