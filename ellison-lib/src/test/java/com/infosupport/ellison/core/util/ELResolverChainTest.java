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

import com.infosupport.ellison.core.api.StaticELResolver;
import com.infosupport.ellison.core.util.ELResolverChain;

import javax.el.ELContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class ELResolverChainTest {
    @Mock ELContext elContext;
    StaticELResolver[] staticELResolvers;
    ELResolverChain elResolverChain;

    @Before
    public void setup() {
        staticELResolvers = new StaticELResolver[]{mock(StaticELResolver.class), mock(StaticELResolver.class)};

        elResolverChain = new ELResolverChain();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetValue_OneResolverSuccess() {
        when(staticELResolvers[0].getValue(eq(elContext), any(Class.class), anyString())).thenReturn(this.getClass());
        when(elContext.isPropertyResolved()).thenReturn(true);
        elResolverChain.addResolver(staticELResolvers[0]);

        assertThat(elResolverChain.getValue(elContext, null, "bla"), is(equalTo((Object) this.getClass())));
    }

    @Test
    public void testGetValue_OneResolverFailure() {
        when(elContext.isPropertyResolved()).thenReturn(false);

        elResolverChain.addResolver(staticELResolvers[0]);

        assertThat(elResolverChain.getValue(elContext, this.getClass(), "bla"), is(equalTo((Object) this.getClass())));
        verify(staticELResolvers[0]).getValue(eq(elContext), eq(this.getClass()), anyString());
    }

    /**
     * Test {@code ELResolverChain.getValue} with two resolvers in the chain, the <strong>second</strong> of which
     * successfully resolves the query.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetValue_TwoResolverSecondSuccess() {
        when(staticELResolvers[0].getValue(eq(elContext), any(Class.class), anyString())).thenReturn(Class.class);
        when(staticELResolvers[1].getValue(eq(elContext), any(Class.class), anyString())).thenReturn(this.getClass());
        when(elContext.isPropertyResolved()).thenReturn(false, true);

        elResolverChain.addResolver(staticELResolvers[0]);
        elResolverChain.addResolver(staticELResolvers[1]);

        assertThat(elResolverChain.getValue(elContext, null, "bla"), is(equalTo((Object) this.getClass())));
        verify(staticELResolvers[0], times(1)).getValue(eq(elContext), any(Class.class), anyString());
    }

    /**
     * Test {@code ELResolverChain.getValue} with two resolvers in the chain, the <strong>first</strong> of which
     * successfully resolves the query.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetValue_TwoResolverFirstSuccess() {
        when(staticELResolvers[0].getValue(eq(elContext), any(Class.class), anyString())).thenReturn(Class.class);
        when(staticELResolvers[1].getValue(eq(elContext), any(Class.class), anyString())).thenReturn(this.getClass());
        when(elContext.isPropertyResolved()).thenReturn(true);

        elResolverChain.addResolver(staticELResolvers[0]);
        elResolverChain.addResolver(staticELResolvers[1]);

        assertThat(elResolverChain.getValue(elContext, null, "bla"), is(equalTo((Object) Class.class)));
        verify(staticELResolvers[1], times(0)).getValue(eq(elContext), any(Class.class), anyString());
    }
}
