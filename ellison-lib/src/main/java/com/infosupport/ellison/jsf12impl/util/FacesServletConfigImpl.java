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

import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.infosupport.ellison.jsf12impl.exceptions.ApplicationConfigurationException;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFileFactory;
import com.sun.enterprise.deployment.util.XModuleType;
import com.sun.enterprise.deployment.web.InitializationParameter;
import java.io.IOException;
import java.util.*;
import javax.faces.webapp.FacesServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import org.xml.sax.SAXParseException;

/**
 * Implementation of {@link ServletConfig}, used to extract configuration parameters specifically for an implementation
 * of {@link FacesServlet}.
 *
 * @author StefanZ
 */
public class FacesServletConfigImpl implements ServletConfig {
    private String servletName;
    private ServletContext servletContext;
    private Map<String, String> initParameters;

    /**
     * Constructor.
     *
     * @param applicationArchive
     *     the application archive to extract configuration parameters from
     *
     * @throws com.infosupport.ellison.jsf12impl.exceptions.ApplicationConfigurationException
     *     if no {@link FacesServlet} is configured for {@code applicationArchive}. This can mean that no {@code
     *     FacesServlet} is declared in the configuration, or that it is not mapped to any URL pattern.
     */
    public FacesServletConfigImpl(ApplicationArchive applicationArchive) throws ApplicationConfigurationException {
        WebBundleDescriptor webBundleDescriptor = getWebBundleDescriptor(applicationArchive);
        WebComponentDescriptor facesComponentDescriptor = getFacesServletComponentDescriptor(webBundleDescriptor);

        if (facesComponentDescriptor == null) {
            throw new ApplicationConfigurationException(String.format(
                "No FacesServlet configured in application archive '%s'",
                applicationArchive.getApplicationFile().getAbsolutePath()));
        }

        initParameters = new HashMap<String, String>();
        servletName = facesComponentDescriptor.getName();
        Enumeration<InitializationParameter> initParams = facesComponentDescriptor.getInitializationParameters();
        while (initParams.hasMoreElements()) {
            InitializationParameter initializationParameter = initParams.nextElement();
            initParameters.put(initializationParameter.getName(), initializationParameter.getValue());
        }
        servletContext = new ServletContextImpl(facesComponentDescriptor, applicationArchive.getClassLoader());
    }

    /**
     * Finds and parses the web bundle descriptor (web.xml) in an application archive.
     *
     * @param applicationArchive
     *     the application archive in which to find the web.xml
     *
     * @return a {@link com.sun.enterprise.deployment.WebBundleDescriptor} reflecting the contents of the web.xml in
     *         the
     *         application archive
     *
     * @throws ApplicationConfigurationException
     *     if an error occurred trying to read the application configuration
     */
    public static WebBundleDescriptor getWebBundleDescriptor(ApplicationArchive applicationArchive)
        throws ApplicationConfigurationException {
        //noinspection unchecked
        DeploymentDescriptorFile<WebBundleDescriptor> deploymentDescriptorFile =
            DeploymentDescriptorFileFactory.getDDFileFor(XModuleType.WAR);
        deploymentDescriptorFile.setXMLValidation(false);

        try {
            return deploymentDescriptorFile
                .read(null, applicationArchive.getFile(deploymentDescriptorFile.getDeploymentDescriptorPath()));
        } catch (SAXParseException | IOException e) {
            throw new ApplicationConfigurationException(e);
        }
    }

    /**
     * Finds the configuration for the {@link FacesServlet} within a {@link WebBundleDescriptor}.
     *
     * @param webBundleDescriptor
     *     the {@link WebBundleDescriptor} to search in
     *
     * @return if there was a {@code <servlet>} section in the {@link WebBundleDescriptor} with a {@code
     *         javax.faces.webapp .FacesServlet} {@code <servlet-class>}, <strong>and</strong> that same servlet
     *         actually has a {@code <servlet-mapping>}, then return a {@code WebComponentDescriptor} describing all
     *         this information. Otherwise, return null.
     */
    public static WebComponentDescriptor getFacesServletComponentDescriptor(WebBundleDescriptor webBundleDescriptor) {
        WebComponentDescriptor[] facesServletComponentDescriptors =
            webBundleDescriptor.getWebComponentByImplName(FacesServlet.class.getCanonicalName());
        for (WebComponentDescriptor facesServletComponentDescriptor : facesServletComponentDescriptors) {
            Set<String> prefixMapping = facesServletComponentDescriptor.getUrlPatternsSet();

            if (prefixMapping != null && prefixMapping.size() > 0) {

                return facesServletComponentDescriptor;
            }
        }

        return null;
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        return initParameters.get(name);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }
}
