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
package com.infosupport.ellison.jsf12impl.util;

import javax.xml.parsers.SAXParser;
import junitx.util.PrivateAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.infosupport.ellison.jsf12impl.util.JSFPageParser;

import static org.mockito.Mockito.verify;

/**
 * Tests whether {@link JSFPageParser} calls identical methods on its delegate {@link javax.xml.parsers.SAXParser}.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class JSFPageParserTest {
    static final String PROPERTY_NAME = "propName";
    static final String PROPERTY_VALUE = "propValue";
    @Mock(answer = Answers.CALLS_REAL_METHODS) JSFPageParser jsfPageParser;
    @Mock SAXParser saxParserDelegate;

    @Before
    public void setup() throws Exception {
        PrivateAccessor.setField(jsfPageParser, "delegate", saxParserDelegate);
    }

    @Test
    public void testGetParser() throws Exception {
        jsfPageParser.getParser();

        verify(saxParserDelegate).getParser();
    }

    @Test
    public void testGetXMLReader() throws Exception {
        jsfPageParser.getXMLReader();

        verify(saxParserDelegate).getXMLReader();
    }

    @Test
    public void testIsNamespaceAware() throws Exception {
        jsfPageParser.isNamespaceAware();

        verify(saxParserDelegate).isNamespaceAware();
    }

    @Test
    public void testIsValidating() throws Exception {
        jsfPageParser.isValidating();

        verify(saxParserDelegate).isValidating();
    }

    @Test
    public void testSetProperty() throws Exception {
        jsfPageParser.setProperty(PROPERTY_NAME, PROPERTY_VALUE);

        verify(saxParserDelegate).setProperty(PROPERTY_NAME, PROPERTY_VALUE);
    }

    @Test
    public void testGetProperty() throws Exception {
        jsfPageParser.getProperty(PROPERTY_NAME);

        verify(saxParserDelegate).getProperty(PROPERTY_NAME);
    }
}
