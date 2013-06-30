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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.infosupport.ellison.core.util.Location;
import com.infosupport.ellison.core.util.LocationAwareELExpressionString;

import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link LocationAwareELExpressionString} class, which is to be used only in {@link
 * com.infosupport.ellison.core.api.ELError}s.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationAwareELExpressionStringTest {
    LocationAwareELExpressionString elExpressionString;
    @Mock Location location;
    String expressionString = null;

    @Before
    public void setup() {
        elExpressionString = new LocationAwareELExpressionString(expressionString, location);
    }

    /**
     * Make sure the {@link LocationAwareELExpressionString#getValue(javax.el.ELResolver)} method throws an {@code
     * UnsupportedOperationException}.
     *
     * @throws Exception
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetValue() throws Exception {
        elExpressionString.getValue(mock(ELResolver.class));
    }
}
