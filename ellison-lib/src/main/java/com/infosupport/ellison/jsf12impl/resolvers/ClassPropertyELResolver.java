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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import javax.el.ELContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosupport.ellison.core.api.StaticELResolver;

/**
 * {@link StaticELResolver} implementation that resolves properties of classes. For example, let's consider {@link
 * java.net.URL}, which has a property {@code protocol} (see {@link java.net.URL#getProtocol()}). Assuming there is a
 * {@code URL} class or subclass that is configured as a managed bean under the name {@code urlBean},
 * the following EL expression would refer to the {@code protocol} property: {@code #{urlBean.protocol}}.
 *
 * @author StefanZ
 */
public class ClassPropertyELResolver extends StaticELResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPropertyELResolver.class);

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (base instanceof Class) {
            String propertyName = property.toString();
            Class beanClass = (Class) base;
            BeanInfo beanInfo = null;

            try {
                beanInfo = Introspector.getBeanInfo(beanClass);
            } catch (IntrospectionException e) {
                LOGGER.debug(String.format("Caught an exception while trying to get bean information for class '%s'",
                                           beanClass.getCanonicalName()), e);
            }

            if (beanInfo != null) {
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    if (propertyDescriptor.getName().equals(propertyName)) {
                        context.setPropertyResolved(true);
                        return propertyDescriptor.getPropertyType();
                    }
                }
            }
        }

        return base;
    }
}

