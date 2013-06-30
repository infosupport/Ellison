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

import com.infosupport.ellison.core.util.ELResolverChain;

import javax.servlet.ServletContext;

/**
 * The {@link com.infosupport.ellison.core.api.StaticELResolver} to be used for resolving Expression
 * Language expressions within JSF 1.2 applications.
 *
 * @author StefanZ
 */
public class JSFELResolver extends ELResolverChain {

    /**
     * Constructor.
     *
     * @param servletContext
     *     the servlet context for which to (attempt to) resolve Expression Language expressions.
     */
    public JSFELResolver(ServletContext servletContext) {
        ManagedBeanELResolver managedBeanELResolver = new ManagedBeanELResolver(servletContext);
        ClassPropertyELResolver classPropertyELResolver = new ClassPropertyELResolver();
        ResourceBundleResolver resourceBundleResolver = new ResourceBundleResolver(servletContext);
        AnnotatedManagedBeanResolver annotatedManagedBeanResolver = new AnnotatedManagedBeanResolver();

        addResolver(managedBeanELResolver);
        addResolver(annotatedManagedBeanResolver);
        addResolver(classPropertyELResolver);
        addResolver(resourceBundleResolver);
    }
}
