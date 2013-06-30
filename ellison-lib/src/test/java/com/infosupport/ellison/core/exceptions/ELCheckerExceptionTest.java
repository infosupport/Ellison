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
import com.infosupport.ellison.core.exceptions.ELCheckerException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link com.infosupport.ellison.core.exceptions.ELCheckerException}.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class ELCheckerExceptionTest {
    @Mock ELError elError;

    @Test
    public void elCheckerExceptionConstructor_ELError() {
        ELCheckerException elCheckerException = new ELCheckerException(elError);

        assertThat(elCheckerException.getError(), is(sameInstance(elError)));
    }

    @Test
    public void elCheckerExceptionConstructor_ELErrorProperties() {
        ELCheckerException elCheckerException =
            new ELCheckerException(elError.getMessage(), elError.getElExpression(), elError.getType());

        assertThat(elCheckerException.getError().getType(), is(sameInstance(elError.getType())));
        assertThat(elCheckerException.getError().getMessage(), is(sameInstance(elError.getMessage())));
        assertThat(elCheckerException.getError().getElExpression(), is(sameInstance(elError.getElExpression())));
    }
}
