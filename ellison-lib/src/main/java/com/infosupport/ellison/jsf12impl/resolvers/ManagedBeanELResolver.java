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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.infosupport.ellison.core.api.StaticELResolver;
import com.infosupport.ellison.jsf12impl.exceptions.ManagedBeanNameCollisionException;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.mgbean.BeanBuilder;
import com.sun.faces.mgbean.BeanManager;
import com.sun.faces.mgbean.ManagedBeanPreProcessingException;

/**
 * {@link com.infosupport.ellison.core.api.StaticELResolver} implementation that is capable of
 * resolving references to managed beans contained in a {@link BeanManager}.
 * To illustrate the responsibility of this resolver, see the following example:
 * EL expression: {@code #{bean.property}}
 * The resolver will <strong>only</strong> resolve the {@code bean} part of this expression, and then again,
 * <strong>only</strong> if it refers to a registered managed bean (from one of the JSF configuration files).
 *
 * @author StefanZ
 */
public class ManagedBeanELResolver extends StaticELResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedBeanELResolver.class);
    private Map<String, Class<?>> managedBeans;
    
    /**
     * No-argument constructor.
     *
     * @param servletContext
     *     the servlet context for the application archive this resolver will be used for
     */
    public ManagedBeanELResolver(ServletContext servletContext) {
        managedBeans = new HashMap<>();

        ApplicationAssociate applicationAssociate = ApplicationAssociate.getInstance(servletContext);
        BeanManager beanManager = applicationAssociate.getBeanManager();

        addManagedBeansFromBeanManager(beanManager);
    }
    
    /**
     * Adds managed beans from a {@link BeanManager} to the list of beans that will be resolved by this resolver.
     * Typically, this {@link BeanManager} will have been constructed by parsing the {@code faces-config.xml} for the
     * application we're analyzing.
     *
     * @param beanManager
     *     The BeanManager containing the list of managed beans that have been configured for this application.
     *
     * @see BeanManager
     */
    public void addManagedBeansFromBeanManager(BeanManager beanManager) {
        for (Map.Entry<String, BeanBuilder> stringBeanBuilderEntry : beanManager.getRegisteredBeans().entrySet()) {
            Class beanClass = null;

            try {
                beanClass = stringBeanBuilderEntry.getValue().getBeanClass();
            } catch (ManagedBeanPreProcessingException e) {
                LOGGER
                    .warn("Bean '{}' ({}) does not exist.", stringBeanBuilderEntry.getKey(), e.getCause().getMessage());
                continue;
            }

            try {
                addManagedBean(stringBeanBuilderEntry.getKey(), beanClass);
            } catch (ManagedBeanNameCollisionException e) {
                LOGGER.warn(e.getMessage());
            }
        }
    }

    /**
     * Adds a managed bean to the list of beans this instance will be able to resolve. Postcondition: {@code
     * this.getValue(someELContext, null, beanELName) == beanClass}.
     *
     * @param beanELName
     *     The name by which the bean should be referred to.
     * @param beanClass
     *     The class of the managed bean.
     *
     * @throws ManagedBeanNameCollisionException
     *     when a particular name has been registered with this instance and you try to register another bean class
     *     under the same name.
     */
    public void addManagedBean(String beanELName, Class beanClass) throws ManagedBeanNameCollisionException {
        if (managedBeans.containsKey(beanELName)) {
            throw new ManagedBeanNameCollisionException(MessageFormat.format(
                "Managed bean name collision on name ''{0}''. The colliding classes are: {1} and {2}. Please check "
                    + "your configuration.", beanELName, managedBeans.get(beanELName).getCanonicalName(),
                beanClass.getCanonicalName()));
        } else {
            managedBeans.put(beanELName, beanClass);
        }
    }

	/**
	 * Resolves a {@code property} for a specific {@code base}. A ELCheckerException is thrown when this expression can or will lead
	 * to problems at runtime. This exception contains an instanceof
	 * {@link com.infosupport.ellison.core.api.ELError} which indicates what kind of problem was found.
	 * 
	 * @param context
	 *            The context an EL expression is being resolved in. This is used by all resolver implementations to indicate
	 *            whether or not they successfully resolved a property.
	 * @param base
	 *            The base class on which to find a property. In the case of this particular implementation, resolution will only
	 *            take place if {@code base == null}.
	 * @param property
	 *            The property to look for on base. In the case of this particular implementation, this will be a {@code String}
	 *            referring to the name of a managed bean.
	 * 
	 * @return If {@code base == null && property != null} and equals the name of a registered managed bean, then the class of the
	 *         managed bean, {@code null} otherwise.
	 */
    @Override
    public Object getValue(ELContext context, Object base, Object property)  {
        String propertyName = property.toString();

        if (base == null && managedBeans.containsKey(propertyName)) {
            context.setPropertyResolved(true);
            return managedBeans.get(propertyName);
        }

        return base;
    }
}
