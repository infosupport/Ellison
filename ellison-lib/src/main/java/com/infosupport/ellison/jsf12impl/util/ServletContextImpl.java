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

import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.ContextParameter;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * An implementation of {@link ServletContext} that only worries about presenting the configuration data from an {@link
 * com.infosupport.ellison.core.archive.ApplicationArchive}.
 *
 * @author StefanZ
 */
public class ServletContextImpl implements ServletContext {
    private static final String CONTEXT_NAME = "Expression Language static checker";
    private static final String SERVER_INFO = "Info Support EL static checker/1.0";
    private static final int SUPPORTED_SERVLET_MINOR_VERSION = 5;
    private static final int SUPPORTED_SERVLET_MAJOR_VERSION = 2;
    private static final String PATH_ROOT_CHARACTER = "/";
    private WebComponentDescriptor config;
    private Map<String, Object> attributes;
    private Map<String, String> contextParameters;
    private ClassLoader classLoader;

    /**
     * Constructor.
     *
     * @param facesServletDescriptor
     *     a {@link WebComponentDescriptor} instance describing the configuration of an implementation of {@link
     *     javax.faces.webapp.FacesServlet}. Typically, this will be extracted from a {@link
     *     com.sun.enterprise.deployment.WebBundleDescriptor}.
     * @param facesServletConfig
     *     an instance of {@link FacesServletConfigImpl}, which is a {@link javax.servlet.ServletConfig}. Its main job
     *     is to hold configuration data specific to a {@link javax.faces.webapp.FacesServlet}
     * @param classLoader
     *     a classloader capable of loading any needed resource, as defined by the standard for the type of application
     *     archive
     */
    public ServletContextImpl(WebComponentDescriptor facesServletDescriptor, ClassLoader classLoader) {
        config = facesServletDescriptor;
        attributes = new HashMap<String, Object>();
        this.classLoader = classLoader;
        loadInitParameters();
    }

    /**
     * Remove the leading slash ('/') from a path.
     *
     * @param path
     *     the path to remove the leading slash from
     *
     * @return if {@code path.startsWith("/")}, then return {@code path} with the first character removed, otherwise
     *         return {@code path} as-is.
     */
    private static String removeRootFromPath(String path) {
        String pathWithoutRoot = path;

        if (path.startsWith(PATH_ROOT_CHARACTER)) {
            pathWithoutRoot = path.substring(1);
        }

        return pathWithoutRoot;
    }

    @Override
    public String getContextPath() {
        return config.getWebBundleDescriptor().getContextRoot();
    }

    @Override
    public ServletContext getContext(String uripath) {
        return this;
    }

    @Override
    public int getMajorVersion() {
        return SUPPORTED_SERVLET_MAJOR_VERSION;
    }

    @Override
    public int getMinorVersion() {
        return SUPPORTED_SERVLET_MINOR_VERSION;
    }

    @Override
    public String getMimeType(String file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set getResourcePaths(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return classLoader.getResource(removeRootFromPath(path));
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return classLoader.getResourceAsStream(removeRootFromPath(path));
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Servlet getServlet(String name) throws ServletException {
        return null;
    }

    @Override
    public Enumeration getServlets() {
        return Collections.emptyEnumeration();
    }

    @Override
    public Enumeration getServletNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public void log(String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(Exception exception, String msg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void log(String message, Throwable throwable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRealPath(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServerInfo() {
        return SERVER_INFO;
    }

    @Override
    public String getInitParameter(String name) {
        return contextParameters.get(name);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return Collections.enumeration(contextParameters.keySet());
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object object) {
        attributes.put(name, object);
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public String getServletContextName() {
        return CONTEXT_NAME;
    }

    private void loadInitParameters() {
        contextParameters = new HashMap<String, String>();
        for (ContextParameter contextParameter : config.getWebBundleDescriptor().getContextParametersSet()) {
            contextParameters.put(contextParameter.getName(), contextParameter.getValue());
        }
    }
}
