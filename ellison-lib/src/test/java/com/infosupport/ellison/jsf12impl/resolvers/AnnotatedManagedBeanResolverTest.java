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

import java.net.URL;
import java.net.URLClassLoader;
import javax.annotation.ManagedBean;
import javax.el.ELContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.infosupport.ellison.jsf12impl.resolvers.AnnotatedManagedBeanResolver;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests for the {@link AnnotatedManagedBeanResolver} class.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class AnnotatedManagedBeanResolverTest {
    @Mock ELContext elContext;
    ClassLoader originalClassLoader;

    @Before
    public void setup() {
        originalClassLoader = Thread.currentThread().getContextClassLoader();
    }

    @After
    public void tearDown() {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    @Test
    public void getValue_NoAnnotatedClasses() throws Exception {
        ClassLoader newClassLoader = this.getClass().getClassLoader();

        /*
         * The following loop is supposed to find the first classloader that isn't capable of finding the
         * AnnotatedBeanWithImplicitName class.
         */
        do {
            newClassLoader = newClassLoader.getParent();
            try {
                newClassLoader.loadClass(AnnotatedBeanWithImplicitName.class.getCanonicalName());
            } catch (ClassNotFoundException e) {
                break;
            }
        } while (true);

        Thread.currentThread().setContextClassLoader(newClassLoader);
        AnnotatedManagedBeanResolver resolver = new AnnotatedManagedBeanResolver();

        Object resolvedValue = resolver.getValue(elContext, null, "annotatedBeanWithImplicitName");

        verify(elContext, never()).setPropertyResolved(true);
    }

    @Test
    public void getValue_WithAnnotatedClass_ImplicitNaming() throws Exception {
        URL annotatedClassURL = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{annotatedClassURL});
        Thread.currentThread().setContextClassLoader(classLoader);
        AnnotatedManagedBeanResolver resolver = new AnnotatedManagedBeanResolver();

        Object resolvedValue = resolver.getValue(elContext, null, "annotatedBeanWithImplicitName");

        verify(elContext).setPropertyResolved(true);
        assertThat(resolvedValue, is(instanceOf(Class.class)));
        assertThat((Class) resolvedValue, is(sameInstance((Class) AnnotatedBeanWithImplicitName.class)));
    }

    @Test
    public void getValue_WithAnnotatedClass_ExplicitNaming() throws Exception {
        URL annotatedClassURL = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{annotatedClassURL});
        Thread.currentThread().setContextClassLoader(classLoader);
        AnnotatedManagedBeanResolver resolver = new AnnotatedManagedBeanResolver();

        Object resolvedValue = resolver.getValue(elContext, null, "someName");

        verify(elContext).setPropertyResolved(true);
        assertThat(resolvedValue, is(instanceOf(Class.class)));
        assertThat((Class) resolvedValue, is(sameInstance((Class) AnnotatedBeanWithExplicitName.class)));
    }

    @Test
    public void getValue_NotResolvable_BaseNotNull() throws Exception {
        AnnotatedManagedBeanResolver resolver = new AnnotatedManagedBeanResolver();

        Object resolvedValue = resolver.getValue(elContext, this, "someName");

        verify(elContext, never()).setPropertyResolved(false);
        assertThat(resolvedValue, is(sameInstance((Object) this)));
    }

    @Test
    public void getValue_NotResolvable_NotAnAnnotatedManagedBean() throws Exception {
        AnnotatedManagedBeanResolver resolver = new AnnotatedManagedBeanResolver();

        Object resolvedValue = resolver.getValue(elContext, null, "someName");

        verify(elContext, never()).setPropertyResolved(false);
    }

    @ManagedBean
    public static class AnnotatedBeanWithImplicitName {
    }

    @ManagedBean("someName")
    public static class AnnotatedBeanWithExplicitName {
    }
}
