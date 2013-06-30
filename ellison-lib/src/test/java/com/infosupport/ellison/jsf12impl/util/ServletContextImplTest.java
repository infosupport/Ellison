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

import com.infosupport.ellison.jsf12impl.util.FacesServletConfigImpl;
import com.infosupport.ellison.jsf12impl.util.ServletContextImpl;
import com.infosupport.ellison.testutil.Enumerations;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.ContextParameter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import junitx.util.PrivateAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ServletContextImpl} class.
 *
 * @author StefanZ
 */
@RunWith(MockitoJUnitRunner.class)
public class ServletContextImplTest {
    static final String INIT_PARAM_NAME = "initparamname";
    static final String INIT_PARAM_VALUE = "initparamvalue";
    static final String ATTRIBUTE_NAME = "attributename";
    static final String ATTRIBUTE_VALUE = "attributevalue";
    ServletContextImpl servletContext;
    Set<ContextParameter> contextParameterSet;
    @Mock WebComponentDescriptor webComponentDescriptor;
    @Mock FacesServletConfigImpl facesServletConfig;
    @Mock ClassLoader classLoader;
    @Mock WebBundleDescriptor webBundleDescriptor;
    @Mock ContextParameter contextParameter;

    @Before
    public void setup() throws Exception {
        contextParameterSet = new HashSet<>();
        when(contextParameter.getName()).thenReturn(INIT_PARAM_NAME);
        when(contextParameter.getValue()).thenReturn(INIT_PARAM_VALUE);
        when(webComponentDescriptor.getWebBundleDescriptor()).thenReturn(webBundleDescriptor);
        when(webBundleDescriptor.getContextParametersSet()).thenReturn(contextParameterSet);
        servletContext = new ServletContextImpl(webComponentDescriptor, classLoader);
    }

    @Test
    public void testGetContextPath() throws Exception {
        servletContext.getContextPath();

        verify(webBundleDescriptor).getContextRoot();
    }

    @Test
    public void testGetContext() throws Exception {
        assertThat(servletContext.getContext(null), is(sameInstance((ServletContext) servletContext)));
        assertThat(servletContext.getContext("someuri"), is(sameInstance((ServletContext) servletContext)));
    }

    @Test
    public void testGetMajorVersion() throws Exception {
        assertThat(servletContext.getMajorVersion(), is(equalTo(2)));
    }

    @Test
    public void testGetMinorVersion() throws Exception {
        assertThat(servletContext.getMinorVersion(), is(equalTo(5)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMimeType_NullFile() throws Exception {
        servletContext.getMimeType(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetMimeType_SomeFile() throws Exception {
        servletContext.getMimeType("somefile");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetResourcePaths() throws Exception {
        servletContext.getResourcePaths("path");
    }

    @Test
    public void testGetResource_RelativePath() throws Exception {
        servletContext.getResource("path");

        verify(classLoader).getResource("path");
    }

    @Test
    public void testGetResource_AbsolutePath() throws Exception {
        servletContext.getResource("/path");

        verify(classLoader).getResource("path");
    }

    @Test
    public void testGetResourceAsStream_RelativePath() throws Exception {
        servletContext.getResourceAsStream("path");

        verify(classLoader).getResourceAsStream("path");
    }

    @Test
    public void testGetResourceAsStream_AbsolutePath() throws Exception {
        servletContext.getResourceAsStream("/path");

        verify(classLoader).getResourceAsStream("path");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetRequestDispatcher() throws Exception {
        servletContext.getRequestDispatcher("string");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetNamedDispatcher() throws Exception {
        servletContext.getNamedDispatcher("dispatcherName");
    }

    @Test
    public void testGetServlet() throws Exception {
        Servlet servlet = servletContext.getServlet("servletname");

        assertThat(servlet, is(nullValue()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetServlets() throws Exception {
        Enumeration servlets = servletContext.getServlets();

        assertThat(servlets, is(equalTo((Enumeration) Collections.emptyEnumeration())));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetServletNames() throws Exception {
        Enumeration servletNames = servletContext.getServletNames();

        assertThat(servletNames, is(equalTo((Enumeration) Collections.emptyEnumeration())));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLog_ExceptionString() throws Exception {
        servletContext.log(new Exception(), "message");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLog_String() throws Exception {
        servletContext.log("message");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testLog_StringThrowable() throws Exception {
        servletContext.log("message", new Throwable());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetRealPath_SomePath() throws Exception {
        servletContext.getRealPath("path");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetRealPath_NullPath() throws Exception {
        servletContext.getRealPath(null);
    }

    @Test
    public void testGetServerInfo() throws Exception {
        String serverInfo = (String) PrivateAccessor.getField(ServletContextImpl.class, "SERVER_INFO");
        String returnedServerInfo = servletContext.getServerInfo();

        assertThat(returnedServerInfo, is(equalTo(serverInfo)));
    }

    @Test
    public void testGetInitParameter_EmptyParams() throws Exception {
        assertThat(servletContext.getInitParameter(INIT_PARAM_NAME), is(nullValue()));
    }

    @Test
    public void testGetInitParameter_WithParams() throws Throwable {
        contextParameterSet.add(contextParameter);

        PrivateAccessor.invoke(servletContext, "loadInitParameters", new Class[0], new Object[0]);

        assertThat(servletContext.getInitParameter(INIT_PARAM_NAME), is(equalTo(INIT_PARAM_VALUE)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetInitParameterNames_EmptyParams() throws Exception {
        assertThat(Enumerations.enumerationSize(servletContext.getInitParameterNames()), is(equalTo(0)));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetInitParameterNames_WithParams() throws Throwable {
        contextParameterSet.add(contextParameter);

        PrivateAccessor.invoke(servletContext, "loadInitParameters", new Class[0], new Object[0]);

        assertThat(Enumerations.enumerationSize(servletContext.getInitParameterNames()), is(equalTo(1)));
        assertThat(servletContext.getInitParameterNames().nextElement(), is(equalTo((Object) INIT_PARAM_NAME)));
    }

    @Test
    public void testGetAttribute_NoAttributesSet() throws Exception {
        assertThat(servletContext.getAttribute(ATTRIBUTE_NAME), is(nullValue()));
    }

    @Test
    public void testGetAttribute_AttributesSet() throws Exception {
        servletContext.setAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

        assertThat(servletContext.getAttribute(ATTRIBUTE_NAME), is(equalTo((Object) ATTRIBUTE_VALUE)));
    }

    @Test
    public void testGetAttributeNames_NoAttributeSet() throws Exception {
        assertThat(Enumerations.enumerationSize(servletContext.getAttributeNames()), is(equalTo(0)));
    }

    @Test
    public void testGetAttributeNames_AttributeSet() throws Exception {
        servletContext.setAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE);

        assertThat(Enumerations.enumerationSize(servletContext.getAttributeNames()), is(equalTo(1)));
        assertThat(servletContext.getAttributeNames().nextElement(), is(equalTo((Object) ATTRIBUTE_NAME)));
    }

    /**
     * Make sure no exceptions are thrown, even when the attribute wasn't in the context.
     *
     * @throws Exception
     *     shouldn't happen!
     */
    @Test
    public void testRemoveAttribute_NoAttributeSet() throws Exception {
        servletContext.removeAttribute(ATTRIBUTE_NAME);
    }

    @Test
    public void testRemoveAttribute_AttributeSet() throws Exception {
        servletContext.setAttribute(ATTRIBUTE_NAME, ATTRIBUTE_VALUE);
        servletContext.removeAttribute(ATTRIBUTE_NAME);

        assertThat(servletContext.getAttribute(ATTRIBUTE_NAME), is(nullValue()));
    }

    @Test
    public void testGetServletContextName() throws Exception {
        String servletContextName = (String) PrivateAccessor.getField(ServletContextImpl.class, "CONTEXT_NAME");
        String returnedValue = servletContext.getServletContextName();

        assertThat(returnedValue, is(equalTo(servletContextName)));
    }
}
