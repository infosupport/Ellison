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
package com.infosupport.ellison.jsf12impl.apiimpl;

import com.infosupport.ellison.core.exceptions.ELCheckerException;
import com.infosupport.ellison.core.util.Location;
import com.infosupport.ellison.jsf12impl.apiimpl.JSFELExpression;
import com.sun.el.lang.ExpressionBuilder;
import com.sun.el.parser.Node;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link JSFELExpression}.
 *
 * @author StefanZ
 */
public class JSFELExpressionTest {
    /**
     * Make {@link JSFELExpression#getValue(javax.el.ELResolver)} catch an ELException.
     *
     * @throws Exception
     */
    @Test
    public void testGetValue_ResolverThrowsELException() throws Exception {
        ELResolver elResolver = mock(ELResolver.class);
        Node node = ExpressionBuilder.createNode("#{something}");
        JSFELExpression jsfelExpression = new JSFELExpression(node, mock(Location.class));
        ELException elException = new ELException("message");
        Exception caughtException = null;

        when(elResolver.getValue(any(ELContext.class), any(Object.class), any(Object.class))).thenThrow(elException);

        try {
            jsfelExpression.getValue(elResolver);
        } catch (ELCheckerException e) {
            caughtException = e;
        }

        assertThat(caughtException, is(not(nullValue())));
        assertThat(caughtException.getMessage(), is(not(nullValue())));
        assertThat(caughtException.getMessage(), is(equalTo(elException.getMessage())));
    }

    @Test(expected = ELCheckerException.class)
    public void testGetValue_ResolverDoesNotResolve() throws Exception {
        ELResolver elResolver = mock(ELResolver.class);
        Node node = ExpressionBuilder.createNode("#{something}");
        JSFELExpression jsfelExpression = new JSFELExpression(node, mock(Location.class));

        when(elResolver.getValue(any(ELContext.class), any(Object.class), any(Object.class))).thenReturn(null);

        jsfelExpression.getValue(elResolver);
    }
}
