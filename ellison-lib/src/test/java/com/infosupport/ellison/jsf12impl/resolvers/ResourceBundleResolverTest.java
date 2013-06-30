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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.servlet.ServletContext;

import junitx.util.PrivateAccessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.exceptions.ELCheckerException;
import com.infosupport.ellison.jsf12impl.apiimpl.JSFELExpression;
import com.infosupport.ellison.jsf12impl.resolvers.ResourceBundleResolver;
import com.infosupport.ellison.jsf12impl.resolvers.ResourceBundleResolver.LocaleResourceBundlesMap;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationResourceBundle;

/**
 * Tests for the {@link ResourceBundleResolver} class.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceBundleResolverTest {
    static final String BUNDLE_NAME = "bundleName";
    static final String PROPERTY_NAME = "propertyName";
    static final String PROPERTY_VALUE = "propertyValue";
    static final String MISSING_PROPERTY_NAME = "missingPropertyName";
    static final String MISSING_PROPERTY_VALUE = "missingPropertyValue";
    static final String BUNDLE_BASENAME = "com.infosupport.ellison.jsf12impl.resolvers.ResourceBundle";
    @Mock ServletContext servletContext;
    @Mock ApplicationAssociate applicationAssociate;
    @Mock ApplicationFactory applicationFactory;
    @Mock Application application;
    @Mock ELContext elContext;
    @Mock JSFELExpression elExpression;
    ResourceBundleResolver resourceBundleResolver;

    @Before
    public void setup() throws Exception {
        when(servletContext.getAttribute((String) PrivateAccessor.getField(applicationAssociate, "ASSOCIATE_KEY")))
            .thenReturn(applicationAssociate);
        when(elContext.getContext(JSFELExpression.class)).thenReturn(elExpression);
    }

    @Test
    public void getValue_BundleDoesNotExist_NoRegisteredBundles() throws Exception {
        Map<String, ApplicationResourceBundle> resourceBundleMap = new HashMap<>();
        when(application.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        when(application.getSupportedLocales()).thenReturn(Collections.<Locale>emptyIterator());
        when(applicationAssociate.getResourceBundles()).thenReturn(resourceBundleMap);

        resourceBundleResolver = new ResourceBundleResolver_MockedApplication(servletContext);

        resourceBundleResolver.getValue(elContext, null, BUNDLE_NAME);

        verify(elContext, never()).setPropertyResolved(true);
    }

    @Test
    public void getValue_BundleExistsInOneLocale() throws Exception {
        Map<String, ApplicationResourceBundle> resourceBundleMap = new HashMap<>();
        ApplicationResourceBundle applicationResourceBundle = mock(ApplicationResourceBundle.class);
        ResourceBundle englishResourceBundle = new ResourceBundle_en();
        when(applicationResourceBundle.getResourceBundle(Locale.ENGLISH)).thenReturn(englishResourceBundle);
        resourceBundleMap.put(BUNDLE_NAME, applicationResourceBundle);
        List<Locale> supportedLocales = new ArrayList<>();
        supportedLocales.add(Locale.ENGLISH);
        when(application.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        when(application.getSupportedLocales()).thenReturn(supportedLocales.iterator());
        when(applicationAssociate.getResourceBundles()).thenReturn(resourceBundleMap);

        resourceBundleResolver = new ResourceBundleResolver_MockedApplication(servletContext);
        Object resolvedValue = resourceBundleResolver.getValue(elContext, null, BUNDLE_NAME);

        verify(elContext).setPropertyResolved(true);
        assertThat(resolvedValue, is(instanceOf(LocaleResourceBundlesMap.class)));
        assertThat(((LocaleResourceBundlesMap) resolvedValue).size(), is(1));
        assertThat(((LocaleResourceBundlesMap) resolvedValue).getBundleName(), is(equalTo(BUNDLE_NAME)));
        assertThat(((LocaleResourceBundlesMap) resolvedValue).get(Locale.ENGLISH),
                   is(sameInstance(englishResourceBundle)));
    }

    @Test
    public void getValue_BundleExistsInMoreThanOneLocale() throws Exception {
        Map<String, ApplicationResourceBundle> resourceBundleMap = new HashMap<>();
        ApplicationResourceBundle applicationResourceBundle = mock(ApplicationResourceBundle.class);
        ResourceBundle englishResourceBundle = new ResourceBundle_en();
        ResourceBundle chineseResourceBundle = new ResourceBundle_zh();
        when(applicationResourceBundle.getResourceBundle(Locale.ENGLISH)).thenReturn(englishResourceBundle);
        when(applicationResourceBundle.getResourceBundle(Locale.CHINESE)).thenReturn(chineseResourceBundle);
        resourceBundleMap.put(BUNDLE_NAME, applicationResourceBundle);
        List<Locale> supportedLocales = new ArrayList<>();
        supportedLocales.add(Locale.ENGLISH);
        supportedLocales.add(Locale.CHINESE);
        when(application.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        when(application.getSupportedLocales()).thenReturn(supportedLocales.iterator());
        when(applicationAssociate.getResourceBundles()).thenReturn(resourceBundleMap);

        resourceBundleResolver = new ResourceBundleResolver_MockedApplication(servletContext);
        Object resolvedValue = resourceBundleResolver.getValue(elContext, null, BUNDLE_NAME);

        verify(elContext).setPropertyResolved(true);
        assertThat(resolvedValue, is(instanceOf(LocaleResourceBundlesMap.class)));
        assertThat(((LocaleResourceBundlesMap) resolvedValue).size(), is(2));
        assertThat(((LocaleResourceBundlesMap) resolvedValue).getBundleName(), is(equalTo(BUNDLE_NAME)));
        assertThat(((LocaleResourceBundlesMap) resolvedValue).get(Locale.ENGLISH),
                   is(sameInstance(englishResourceBundle)));
        assertThat(((LocaleResourceBundlesMap) resolvedValue).get(Locale.CHINESE),
                   is(sameInstance(chineseResourceBundle)));
    }

    @Test
    public void getValue_PropertyDoesNotExistInAnyLocale() throws Exception {
        LocaleResourceBundlesMap resourceBundlesMap = new LocaleResourceBundlesMap(BUNDLE_NAME);
        ResourceBundle resourceBundle = mock(ResourceBundle.class);
        resourceBundlesMap.put(Locale.ENGLISH, resourceBundle);
        when(resourceBundle.getLocale()).thenReturn(Locale.ENGLISH);
        when(resourceBundle.containsKey(Mockito.any(String.class))).thenReturn(false);
        when(applicationAssociate.getResourceBundles())
            .thenReturn(Collections.<String, ApplicationResourceBundle>emptyMap());

        resourceBundleResolver = new ResourceBundleResolver_MockedApplication(servletContext);
        ELCheckerException caughtException = null;

        try {
            resourceBundleResolver.getValue(elContext, resourceBundlesMap, PROPERTY_NAME);
            fail();
        } catch (ELCheckerException e) {
            caughtException = e;
        }

        assertThat(caughtException.getError().getElExpression(),
                   is(sameInstance((LocationAwareELExpression) elExpression)));
        assertThat(caughtException.getError().getType(), is(ELError.ELErrorSeverity.ERROR));
    }

    @Test
    public void getValue_PropertyDoesNotExistInEveryLocale() throws Exception {
        LocaleResourceBundlesMap resourceBundlesMap = new LocaleResourceBundlesMap(BUNDLE_NAME);
        Object propertyValue = new Object();
        ResourceBundle englishResourceBundle = ResourceBundle.getBundle(BUNDLE_BASENAME, Locale.ENGLISH);
        ResourceBundle chineseResourceBundle = ResourceBundle.getBundle(BUNDLE_BASENAME, Locale.CHINESE);
        ResourceBundle dutchResourceBundle = ResourceBundle.getBundle(BUNDLE_BASENAME, Locale.forLanguageTag("nl"));
        resourceBundlesMap.put(Locale.ENGLISH, englishResourceBundle);
        resourceBundlesMap.put(Locale.CHINESE, chineseResourceBundle);
        resourceBundlesMap.put(Locale.forLanguageTag("nl"), dutchResourceBundle);
        when(applicationAssociate.getResourceBundles())
            .thenReturn(Collections.<String, ApplicationResourceBundle>emptyMap());

        resourceBundleResolver = new ResourceBundleResolver_MockedApplication(servletContext);
        ELCheckerException caughtException = null;

        try {
            resourceBundleResolver.getValue(elContext, resourceBundlesMap, MISSING_PROPERTY_NAME);
            fail();
        } catch (ELCheckerException e) {
            caughtException = e;
        }

        assertThat(caughtException.getError().getElExpression(),
                   is(sameInstance((LocationAwareELExpression) elExpression)));
        assertThat(caughtException.getError().getType(), is(ELError.ELErrorSeverity.WARNING));
    }

    private class ResourceBundleResolver_MockedApplication extends ResourceBundleResolver {
        public ResourceBundleResolver_MockedApplication(ServletContext servletContext) {
            super(servletContext);
        }

        @Override
        public Application getApplication() {
            return application;
        }
    }
}
