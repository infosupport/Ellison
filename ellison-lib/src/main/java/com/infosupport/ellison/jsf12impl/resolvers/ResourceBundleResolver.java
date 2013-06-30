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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.servlet.ServletContext;

import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.api.StaticELResolver;
import com.infosupport.ellison.core.exceptions.ELCheckerException;
import com.infosupport.ellison.jsf12impl.apiimpl.JSFELExpression;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationResourceBundle;

/**
 * This implementation is to be used to resolve references to resource bundles.
 *
 * @author StefanZ
 */
public class ResourceBundleResolver extends StaticELResolver {
    private Map<String, LocaleResourceBundlesMap> resourceBundleLocaleCollectionMap;

    /**
     * Constructor.
     * The configuration pertaining to resource bundles and supported locales will be extracted from the {@code
     * servletContext}, and stored in a local field for faster resolving.
     *
     * @param servletContext
     *     the servlet context in which to find the necessary configuration
     *
     * @see ApplicationAssociate#getInstance(javax.servlet.ServletContext)
     * @see com.sun.faces.application.ApplicationAssociate#getResourceBundles()
     * @see javax.faces.application.Application#getDefaultLocale()
     * @see javax.faces.application.Application#getSupportedLocales()
     */
    public ResourceBundleResolver(ServletContext servletContext) {
        resourceBundleLocaleCollectionMap = new HashMap<>();
        ApplicationAssociate applicationAssociate = ApplicationAssociate.getInstance(servletContext);
        Application application = getApplication();

        for (Map.Entry<String, ApplicationResourceBundle> applicationResourceBundleEntry : applicationAssociate
            .getResourceBundles().entrySet()) {
            LocaleResourceBundlesMap localeResourceBundlesMap =
                new LocaleResourceBundlesMap(applicationResourceBundleEntry.getKey());
            localeResourceBundlesMap.put(application.getDefaultLocale(), applicationResourceBundleEntry.getValue()
                .getResourceBundle(application.getDefaultLocale()));
            Iterator<Locale> supportedLocales = application.getSupportedLocales();

            while (supportedLocales.hasNext()) {
                Locale supportedLocale = supportedLocales.next();
                localeResourceBundlesMap
                    .put(supportedLocale, applicationResourceBundleEntry.getValue().getResourceBundle(supportedLocale));
            }

            resourceBundleLocaleCollectionMap.put(applicationResourceBundleEntry.getKey(), localeResourceBundlesMap);
        }
    }

    /**
     * Get the value for a resource bundle or its properties.
     * 
     * Can throw runtime ELCheckerException when the {@code base} was a {@code LocaleResourceBundlesMap} and the {@code property}
     * wasn't found in every bundle.
     * @see com.infosupport.ellison.jsf12impl.resolvers.ResourceBundleResolver.LocaleResourceBundlesMap
     * 
     * @param context
     *            the evaluation context to use
     * @param base
     *            the base object; may be null (in this case, the resource bundle itself will be returned in the form of a
     *            {@link com.infosupport.ellison.jsf12impl.resolvers.ResourceBundleResolver .LocaleResourceBundlesMap}
     *            ), or a
     *            {@link com.infosupport.ellison.jsf12impl.resolvers.ResourceBundleResolver .LocaleResourceBundlesMap}
     *            itself, in which case the resolver will try to get an actual object from the bundle.
     * @param property
     *            the property; must not be null
     * 
     * @return If base was null, and a the named resource bundle was found, then return a LocaleResourceBundlesMap. If the base was
     *         null, and no resource bundle was found, then return {@code base}. If the base was a {@code LocaleResourceBundlesMap},
     *         and the named property was found within each supported locale, then this returns the actual object from the default
     *         locale. If the base was a {@code LocaleResourceBundlesMap}, and the named property wasn't found within each supported
     *         locale, then this resolver throws an {@link ELCheckerException} explaining this fact.
     */
    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        String propertyName = property.toString();
        Object returnValue = base;

        if (base == null) {
            if (resourceBundleLocaleCollectionMap.containsKey(propertyName)) {
                context.setPropertyResolved(true);
                returnValue = resourceBundleLocaleCollectionMap.get(propertyName);
            }
        } else if (base instanceof LocaleResourceBundlesMap) {
            LocaleResourceBundlesMap localeResourceBundlesMap = (LocaleResourceBundlesMap) base;
            List<ResourceBundle> resourceBundlesWithoutProperty = new ArrayList<>();

            for (Map.Entry<Locale, ResourceBundle> localeResourceBundleEntry : localeResourceBundlesMap.entrySet()) {
                ResourceBundle resourceBundle = localeResourceBundleEntry.getValue();

                if (resourceBundle.containsKey(propertyName)) {
                    context.setPropertyResolved(true);
                    returnValue = resourceBundle.getObject(propertyName);
                } else {
                    resourceBundlesWithoutProperty.add(resourceBundle);
                }
            }

            if (resourceBundlesWithoutProperty.size() == localeResourceBundlesMap.size()) {
                throw new ELCheckerException(String.format(
                    "None of the locales for resource bundle '%s' contain a " + "property named '%s'",
                    localeResourceBundlesMap.getBundleName(), propertyName),
                                             (LocationAwareELExpression) context.getContext(JSFELExpression.class),
                                             ELError.ELErrorSeverity.ERROR);
            } else if (resourceBundlesWithoutProperty.size() > 0) {
                throwMissingFromSomeLocalesException(context, propertyName, localeResourceBundlesMap,
                                                     resourceBundlesWithoutProperty);
            }
        }

        return returnValue;
    }

    private void throwMissingFromSomeLocalesException(ELContext context, String propertyName,
                                                      LocaleResourceBundlesMap localeResourceBundlesMap,
                                                      List<ResourceBundle> resourceBundlesWithoutProperty) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Resource bundle '%s' does not contain property '%s' for the following locales: ",
                                localeResourceBundlesMap.getBundleName(), propertyName));

        for (int i = 0; i < resourceBundlesWithoutProperty.size(); i++) {
            Locale locale = resourceBundlesWithoutProperty.get(i).getLocale();

            if (i > 0) {
                sb.append(", ");
            }

            sb.append('\'');
            sb.append(locale.toLanguageTag());
            sb.append('\'');
        }

        throw new ELCheckerException(sb.toString(), (JSFELExpression) context.getContext(JSFELExpression.class),
                                     ELError.ELErrorSeverity.WARNING);
    }

    public Application getApplication() {
        return ((ApplicationFactory) FactoryFinder.getFactory(FactoryFinder.APPLICATION_FACTORY)).getApplication();
    }

    /**
     * This map is used to group together locale-specific instances of the "same" resource bundle together.
     */
    public static class LocaleResourceBundlesMap extends HashMap<Locale, ResourceBundle> {
        private static final long serialVersionUID = -2606631418043897571L;
        private String bundleName = null;

        /**
         * Constructor.
         *
         * @param bundleName
         *     the name by which the resource bundles contained in this instance will be referred to.
         */
        public LocaleResourceBundlesMap(String bundleName) {
            super();
            this.bundleName = bundleName;
        }

        public String getBundleName() {
            return bundleName;
        }
    }
}
