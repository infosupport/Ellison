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

import javax.el.ELContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.infosupport.ellison.core.api.StaticELResolver;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Tests {@link com.infosupport.ellison.core.api.StaticELResolver}.
 *
 * @author StefanZ
 */

@RunWith(MockitoJUnitRunner.class)
public class StaticELResolverTest {
    StaticELResolver staticELResolver;

    @Before
    public void setup() {
        staticELResolver = mock(StaticELResolver.class, CALLS_REAL_METHODS);
    }

    /**
     * Makes sure that when calling {@link StaticELResolver#getValue(javax.el.ELContext, Object, Object)}
     */
    @Test
    public void testGetValue_CallsAbstract() {
        ELContext elContext = mock(ELContext.class);
        Object baseClass = getClass();
        String property = "property";
        Class expectedResultObject = getClass();

        doReturn(expectedResultObject).when(staticELResolver).getValue(elContext, baseClass, property);

        Object actualResultObject = staticELResolver.getValue(elContext, baseClass, property);

        assertThat(actualResultObject, is(instanceOf(Class.class)));
        assertThat((Class) actualResultObject, is(sameInstance(expectedResultObject)));
        verify(staticELResolver).getValue(eq(elContext), eq((Class) baseClass), eq(property));
    }

    @Test
    public void testGetCommonPropertyTypeUnsupportedOperations() {
        try {
            staticELResolver.getCommonPropertyType(null, null);
        } catch (UnsupportedOperationException e) {
            return; // SUCCESS!
        }

        fail(); // No UnsupportedOperationException was thrown.
    }

    @Test
    public void testGetFeatureDescriptors() {
        try {
            staticELResolver.getFeatureDescriptors(null, null);
        } catch (UnsupportedOperationException e) {
            return; // SUCCESS!
        }

        fail(); // No UnsupportedOperationException was thrown.
    }

    @Test
    public void testGetType() {
        try {
            staticELResolver.getType(null, null, null);
        } catch (UnsupportedOperationException e) {
            return; // SUCCESS!
        }

        fail(); // No UnsupportedOperationException was thrown.
    }

    @Test
    public void testIsReadOnly() {
        try {
            staticELResolver.isReadOnly(null, null, null);
        } catch (UnsupportedOperationException e) {
            return; // SUCCESS!
        }

        fail(); // No UnsupportedOperationException was thrown.
    }

    @Test
    public void testSetValue() {
        try {
            staticELResolver.setValue(null, null, null, null);
        } catch (UnsupportedOperationException e) {
            return; // SUCCESS!
        }

        fail(); // No UnsupportedOperationException was thrown.
    }
}
