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
package com.infosupport.ellison.jsf12impl.resolvers;

import java.util.ListResourceBundle;

/**
 * This is supposed to be used as a Chinese (zh) resource bundle in tests.
 *
 * @author StefanZ
 * @see ResourceBundleResolverTest
 */
public class ResourceBundle_zh extends ListResourceBundle {
    private Object[][] contents =
        {{ResourceBundleResolverTest.PROPERTY_NAME, ResourceBundleResolverTest.PROPERTY_VALUE}};

    @Override
    protected Object[][] getContents() {
        return contents;
    }
}
