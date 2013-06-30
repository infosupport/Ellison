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

import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.infosupport.ellison.core.util.Location;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * "Tests" for {@link Location}.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationTest {
    Location location;
    URI file;
    int line = 1;
    int column = 2;

    @Before
    public void setup() {
        file = URI.create("./");
        location = new Location(file, line, column);
    }

    @Test
    public void testGetFile() throws Exception {
        assertThat(location.getUri(), is(equalTo(file)));
    }

    @Test
    public void testGetColumn() throws Exception {
        assertThat(location.getColumn(), is(equalTo(column)));
    }

    @Test
    public void testGetLine() throws Exception {
        assertThat(location.getLine(), is(equalTo(line)));
    }
}
