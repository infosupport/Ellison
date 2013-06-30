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

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.CatchException.verifyException;
import static junitx.util.PrivateAccessor.getField;
import static junitx.util.PrivateAccessor.setField;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.el.ELContext;
import javax.servlet.ServletContext;

import junitx.util.PrivateAccessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.infosupport.ellison.core.exceptions.ELCheckerException;
import com.infosupport.ellison.core.util.Location;
import com.infosupport.ellison.jsf12impl.apiimpl.JSFELExpression;
import com.infosupport.ellison.jsf12impl.exceptions.ManagedBeanNameCollisionException;
import com.infosupport.ellison.jsf12impl.resolvers.ManagedBeanELResolver;
import com.sun.el.lang.ExpressionBuilder;
import com.sun.el.parser.Node;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.mgbean.BeanBuilder;
import com.sun.faces.mgbean.BeanManager;

/**
 * Tests {@link ManagedBeanELResolver}.
 *
 * @author StefanZ
 */

@RunWith(MockitoJUnitRunner.class)
public class ManagedBeanELResolverTest {
    public static final String BEAN_EL_NAME = "beanELName";

    ManagedBeanELResolver managedBeanELResolver;
    @Mock BeanManager beanManager;
    @Mock BeanBuilder beanBuilder;
    @Mock ELContext elContext;
    @Mock Location location;


    @Before
    public void setup() throws NoSuchFieldException {
        ServletContext servletContext = mock(ServletContext.class);
        BeanManager setupBeanManager = mock(BeanManager.class);
        ApplicationAssociate applicationAssociate = mock(ApplicationAssociate.class);
        String associateKey = (String) PrivateAccessor.getField(ApplicationAssociate.class, "ASSOCIATE_KEY");

        when(servletContext.getAttribute(associateKey)).thenReturn(applicationAssociate);
        when(applicationAssociate.getBeanManager()).thenReturn(setupBeanManager);
        when(setupBeanManager.getRegisteredBeans()).thenReturn(Collections.<String, BeanBuilder>emptyMap());

        managedBeanELResolver = new ManagedBeanELResolver(servletContext);
    }

    @Test
    public void testGetAddedValue() throws NoSuchFieldException {
        Map<String, Class> stringClassMap = new HashMap<>();
        Object returnValue;
        stringClassMap.put(BEAN_EL_NAME, this.getClass());

        setField(managedBeanELResolver, "managedBeans", stringClassMap);

        returnValue = managedBeanELResolver.getValue(elContext, null, BEAN_EL_NAME);

        verify(elContext).setPropertyResolved(true);
        assertThat(returnValue, is(equalTo((Object) this.getClass())));
    }

    @Test
    public void testGetNotAddedValue() throws NoSuchFieldException {
        managedBeanELResolver.getValue(elContext, null, this);

        verify(elContext, never()).setPropertyResolved(true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddManagedBeansFromBeanManager() throws NoSuchFieldException {
        Map<String, BeanBuilder> stringBeanBuilderMap = new HashMap<>();
        Map<String, Class<?>> resolverInternalBeanMap;
        Class<?> thisClass = this.getClass();
        stringBeanBuilderMap.put(BEAN_EL_NAME, beanBuilder);

        when(beanManager.getRegisteredBeans()).thenReturn(stringBeanBuilderMap);
        when(beanBuilder.getBeanClass()).thenReturn((Class) thisClass);

        managedBeanELResolver.addManagedBeansFromBeanManager(beanManager);

        resolverInternalBeanMap = (Map<String, Class<?>>) getField(managedBeanELResolver, "managedBeans");
        assertThat(resolverInternalBeanMap.containsKey(BEAN_EL_NAME), is(true));
        assertThat(resolverInternalBeanMap.get(BEAN_EL_NAME), is(equalTo((Class) thisClass)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAddManagedBean() throws ManagedBeanNameCollisionException, NoSuchFieldException {
        Map<String, Class<?>> resolverInternalBeanMap = mock(Map.class);
        setField(managedBeanELResolver, "managedBeans", resolverInternalBeanMap);

        managedBeanELResolver.addManagedBean(BEAN_EL_NAME, this.getClass());

        verify(resolverInternalBeanMap).put(BEAN_EL_NAME, this.getClass());
    }

    @Test
    public void testAddManagedBean_DuplicateNames() throws ManagedBeanNameCollisionException {
        catchException(managedBeanELResolver, ManagedBeanNameCollisionException.class)
            .addManagedBean(BEAN_EL_NAME, this.getClass());
        assertThat(caughtException(), is(equalTo(null)));
        verifyException(managedBeanELResolver, ManagedBeanNameCollisionException.class)
            .addManagedBean(BEAN_EL_NAME, this.getClass());
    }

    @Test(expected = ELCheckerException.class)
    public void testGetValue_NoRegisteredBeans() throws Exception {
        Node expressionNode = ExpressionBuilder.createNode("bean");
        JSFELExpression jsfelExpression = new JSFELExpression(expressionNode, location);

        jsfelExpression.getValue(managedBeanELResolver);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetValue_WithRegisteredBean() throws Exception {
        Object thisClass = this.getClass();
        Node expressionNode = ExpressionBuilder.createNode("#{bean}");
        JSFELExpression jsfelExpression = new JSFELExpression(expressionNode, location);
        managedBeanELResolver.addManagedBean("bean", this.getClass());

        Object expressionResult = jsfelExpression.getValue(managedBeanELResolver);

        assertThat(expressionResult, is(notNullValue()));
        assertThat(expressionResult, is(instanceOf(Class.class)));
        assertThat(expressionResult, is(sameInstance(thisClass)));
    }
}
