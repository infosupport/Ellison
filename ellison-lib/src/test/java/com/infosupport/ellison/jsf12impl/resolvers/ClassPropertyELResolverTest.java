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

import com.infosupport.ellison.jsf12impl.apiimpl.JSFELExpression;
import com.infosupport.ellison.jsf12impl.resolvers.ClassPropertyELResolver;

import javax.el.ELContext;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Tests the {@link ClassPropertyELResolver}.
 *
 * @author StefanZ
 */
public class ClassPropertyELResolverTest {
    ClassPropertyELResolver elResolver;
    ELContext elContext;

    @Before
    public void setup() {
        elResolver = new ClassPropertyELResolver();
        elContext = new JSFELExpression.JSFELContext(elResolver);
    }

    /**
     * Tests whether a normal declared (meaning it is declared directly in the class,
     * and is not inherited from one of the superclasses in its inheritance hierarchy) property of a class can be
     * resolved.
     */
    @Test
    public void testGetValue_DeclaredProperty() throws Exception {
        Object returnedClass = elResolver.getValue(elContext, TestClass.class, "stringProperty");

        assertThat(elContext.isPropertyResolved(), is(true));
        assertThat(returnedClass, is(sameInstance((Object) String.class)));
    }

    /**
     * Tests whether a boolean property can be resolved.
     * This test is necessary, because boolean property getters may be named {@code isProperty},
     * as opposed to other properties, which are supposed to be named {@code getProperty}.
     */
    @Test
    public void testGetValue_DeclaredBooleanProperty() throws Exception {
        Object returnedClass = elResolver.getValue(elContext, TestClass.class, "booleanProperty");

        assertThat(elContext.isPropertyResolved(), is(true));
        assertThat(returnedClass, is(sameInstance((Object) boolean.class)));
    }

    /**
     * Tests whether the resolver is capable of resolving properties from a class' inheritance hierarchy.
     */
    @Test
    public void testGetValue_SuperClassProperty() throws Exception {
        Object returnedClass = elResolver.getValue(elContext, TestClass.class, "superStringProperty");

        assertThat(elContext.isPropertyResolved(), is(true));
        assertThat(returnedClass, is(sameInstance((Object) int.class)));
    }

    /**
     * Tests whether the {@link ELContext} indeed reflects that the property has not been resolved if searching for a
     * property that is not part of the class or its inheritance hierarchy.
     */
    @Test
    public void testGetvalue_NoSuchProperty() throws Exception {
        Object returnedClass = elResolver.getValue(elContext, TestClass.class, "nosuchproperty");

        assertThat(elContext.isPropertyResolved(), is(false));
    }

    private static class TestSuperClass {
        private int superStringProperty;

        public int getSuperStringProperty() {
            return superStringProperty;
        }

        public void setSuperStringProperty(int superStringProperty) {
            this.superStringProperty = superStringProperty;
        }
    }

    private static class TestClass extends TestSuperClass {
        private boolean booleanProperty;
        private String stringProperty;

        public boolean isBooleanProperty() {
            return booleanProperty;
        }

        public void setBooleanProperty(boolean booleanProperty) {
            this.booleanProperty = booleanProperty;
        }

        public String getStringProperty() {
            return stringProperty;
        }

        public void setStringProperty(String stringProperty) {
            this.stringProperty = stringProperty;
        }
    }
}
