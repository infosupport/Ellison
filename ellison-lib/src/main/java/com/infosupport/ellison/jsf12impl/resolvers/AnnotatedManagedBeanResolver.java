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

import com.infosupport.ellison.core.api.StaticELResolver;

import java.beans.Introspector;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.ManagedBean;
import javax.el.ELContext;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * Finds all classes on the classpath annotated with the {@link ManagedBean} annotation,
 * and resolves EL expressions referring to these.
 *
 * @author StefanZ
 */
public class AnnotatedManagedBeanResolver extends StaticELResolver {
    private Map<String, Class<?>> annotatedBeans;

    /**
     * Constructor.
     * Classes on the classpath (as defined by {@code Thread.currentThread().getContextClassLoader()}) are scanned
     * for the presence of a {@code @ManagedBean} annotation. The found classes are then stored in a map,
     * with the key being the name of the bean ({@link javax.annotation.ManagedBean#value()}). If the name of the
     * bean is an empty string, then {@link Introspector#decapitalize(String)} is called on the simple name of the
     * bean class ({@link Class#getSimpleName()}).
     */
    public AnnotatedManagedBeanResolver() {
        annotatedBeans = new HashMap<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Reflections reflections = new Reflections(
            new ConfigurationBuilder().filterInputsBy(new FilterBuilder().include(".*\\.class"))
                .setUrls(ClasspathHelper.forClassLoader(classLoader)).setScanners(new TypeAnnotationsScanner()));
        Set<Class<?>> managedBeanClasses = reflections.getTypesAnnotatedWith(ManagedBean.class);

        for (Class<?> managedBeanClass : managedBeanClasses) {
            ManagedBean annotation = managedBeanClass.getAnnotation(ManagedBean.class);
            String elName = annotation.value();

            if (elName.length() == 0) {
                elName = Introspector.decapitalize(managedBeanClass.getSimpleName());
            }

            annotatedBeans.put(elName, managedBeanClass);
        }
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (base == null) {
            String propertyName = property.toString();

            if (annotatedBeans.containsKey(propertyName)) {
                context.setPropertyResolved(true);
                return annotatedBeans.get(propertyName);
            }
        }

        return base;
    }
}
