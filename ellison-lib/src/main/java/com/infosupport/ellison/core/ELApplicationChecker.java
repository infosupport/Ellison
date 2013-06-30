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
package com.infosupport.ellison.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.LogManager;

import com.infosupport.ellison.core.api.ELApplicationCheckerFactory;
import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.ELFinder;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.api.PageFinder;
import com.infosupport.ellison.core.api.StaticELResolver;
import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.infosupport.ellison.core.archive.ApplicationArchiveFactory;
import com.infosupport.ellison.core.exceptions.ApplicationNotSupportedException;
import com.infosupport.ellison.core.exceptions.ArchiveFormatUnsupportedException;
import com.infosupport.ellison.core.exceptions.ELCheckerException;
import com.infosupport.ellison.core.util.Pair;
import com.sun.faces.util.FacesLogger;

/**
 * Entry point for launching analysis of Expression Language expressions within Java applications.
 * See {@link #checkApplication(java.io.File)} for a somewhat detailed description of how the actual checking is
 * performed.
 *
 * @author StefanZ
 */
public class ELApplicationChecker {
    private final ClassLoader originalClassLoader;
    private List<ELApplicationCheckerFactory> supportedELCheckerApplicationFactories;

    /**
     * The following static initializer simply disables all logging done by the {@link FacesLogger}.
     */
    static {
        for (FacesLogger facesLogger : FacesLogger.values()) {
            facesLogger.getLogger().setUseParentHandlers(false);
        }
        LogManager.getLogManager().reset();
    }

    /**
     * No argument constructor.
     */
    public ELApplicationChecker() {
        supportedELCheckerApplicationFactories = new ArrayList<ELApplicationCheckerFactory>();
        originalClassLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * Checks the given application archive for Expression Language errors and problems.
     * <p/>
     * The workflow of this method is as follows: <ol>
     * <li>Create an {@link com.infosupport.ellison.core.archive.ApplicationArchive} for {@code
     * application}
     * (using {@link com.infosupport.ellison.core.archive.ApplicationArchiveFactory})</li>
     * <li>Set the current thread's context classloader (see {@link Thread#setContextClassLoader(ClassLoader)}) to
     * whatever {@link com.infosupport.ellison.core.archive.ApplicationArchive#getClassLoader()}
     * returns</li>
     * <li>Ask every {@link ELApplicationCheckerFactory} currently in the list of supported
     * {@code ELApplicationCheckerFactory} if it supports the given {@code application}. If none do,
     * restore the original classloader, and throw an {@link
     * com.infosupport.ellison.core.exceptions.ApplicationNotSupportedException}.
     * Otherwise,
     * continue to the next step</li>
     * <li>Ask the {@link ELApplicationCheckerFactory} for an instance of {@link
     * com.infosupport.ellison.core.api.PageFinder},
     * and use it to get a list of pages within the {@code application}</li>
     * <li>Ask the {@link ELApplicationCheckerFactory} for an instance of {@link
     * com.infosupport.ellison.core.api.ELFinder},
     * and use it to get a list of EL expressions for each page, as found in the previous step. {@link
     * com.infosupport.ellison.core.api.ELFinder#findELExpressions(java.util.Collection,
     * com.infosupport.ellison.core.archive.ApplicationArchive)} is used for this,
     * which returns a list of correct EL expressions, as well as a list of errors it might have found. Save
     * the list of errors for later reporting</li>
     * <li>Ask the {@link ELApplicationCheckerFactory} for an instance of {@link StaticELResolver},
     * and use it to resolve each and every EL expression in the list found in the previous step. This is done
     * using {@link #checkExpressions(java.util.Collection, StaticELResolver)}</li>
     * <li>Restore the original classloader, and return the list of all found errors</li>
     * </ol>
     * <p/>
     * Note that this method changes the context classloader of the caller's thread during its execution. However, upon
     * returning, this method must have restored the classloader that was used during construction of this instance
     * (even in the event of an exception being thrown).
     *
     * @param application
     *     the application archive to analyse
     *
     * @return a list of found errors, or {@code null} if the application archive does not exist.
     *
     * @throws com.infosupport.ellison.core.exceptions.ArchiveFormatUnsupportedException
     *     when the archive is not yet supported by this class
     * @throws com.infosupport.ellison.core.exceptions.ApplicationNotSupportedException
     *     when the configuration for the application indicates different components/frameworks than this application
     *     supports
     * @throws IOException
     *     if some I/O error occurred while trying to unpack the application archive
     */
    public Collection<ELError> checkApplication(File application)
        throws ArchiveFormatUnsupportedException, ApplicationNotSupportedException, IOException {
        ApplicationArchive applicationArchive = null;
        ELApplicationCheckerFactory elApplicationCheckerFactory = null;

        applicationArchive = getApplicationArchiveFactory().createApplicationArchive(application);

        Thread.currentThread().setContextClassLoader(applicationArchive.getClassLoader());

        elApplicationCheckerFactory = findCheckerForApplication(applicationArchive);

        PageFinder pageFinder = elApplicationCheckerFactory.createPageFinder(applicationArchive);
        ELFinder elFinder = elApplicationCheckerFactory.createELFinder(applicationArchive);
        StaticELResolver elResolver = elApplicationCheckerFactory.createELResolver(applicationArchive);

        Collection<URI> pages = pageFinder.findPages(applicationArchive);
        Pair<Collection<LocationAwareELExpression>, Collection<ELError>> elExpressionsAndErrors =
            elFinder.findELExpressions(pages, applicationArchive);
        Collection<ELError> foundErrors = checkExpressions(elExpressionsAndErrors.first(), elResolver);

        foundErrors.addAll(elExpressionsAndErrors.second());

        restoreClassLoader();

        return foundErrors;
    }

    /**
     * Find a {@link ELApplicationCheckerFactory} (from the list of supported ones, see
     * {@link #addSupportedELCheckerApplicationFactory(ELApplicationCheckerFactory)}) that supports {@code
     * applicationArchive}.
     *
     * @param applicationArchive
     *     the {@link ApplicationArchive} to find an {@link ELApplicationCheckerFactory} for
     *
     * @return an instance of {@link ELApplicationCheckerFactory} that can process {@code applicationArchive}. This
     *         means that {@link ELApplicationCheckerFactory#canHandleApplication(ApplicationArchive)} must return
     *         {@code true}.
     *
     * @throws ApplicationNotSupportedException
     *     if no {@link ELApplicationCheckerFactory can be found}
     */
    private ELApplicationCheckerFactory findCheckerForApplication(ApplicationArchive applicationArchive)
        throws ApplicationNotSupportedException {
        boolean applicationSupported = false;
        ELApplicationCheckerFactory elApplicationCheckerFactory = null;

        for (ELApplicationCheckerFactory factory : supportedELCheckerApplicationFactories) {
            if (factory.canHandleApplication(applicationArchive)) {
                applicationSupported = true;
                elApplicationCheckerFactory = factory;
                break;
            }
        }

        if (!applicationSupported) {
            restoreClassLoader();
            throw new ApplicationNotSupportedException(
                String.format("No plugins are available to check the application '%s'",
                              applicationArchive.getApplicationFile().getAbsolutePath()));
        }

        return elApplicationCheckerFactory;
    }

    /**
     * Restores what was this thread's context classloader at the time of the construction of this instance.
     */
    private void restoreClassLoader() {
        Thread.currentThread().setContextClassLoader(originalClassLoader);
    }

    /**
     * Use the resolver to check every expression in {@code elExpressions}, and return every found error.
     *
     * @param elExpressions
     *     the list of Expression Language expressions to check
     * @param elResolver
     *     the resolver to check the expressions with
     *
     * @return a list of found errors (can be empty)
     */
    public Collection<ELError> checkExpressions(Collection<LocationAwareELExpression> elExpressions,
                                                StaticELResolver elResolver) {
        Collection<ELError> foundErrors = new ArrayList<>();
        for (LocationAwareELExpression elExpression : elExpressions) {
            try {
                elExpression.getValue(elResolver);
            } catch (ELCheckerException e) {
                foundErrors.add(e.getError());
            }
        }

        return foundErrors;
    }

    /**
     * Add an {@code ELApplicationCheckerFactory} that can be used to check an application for faulty EL expressions.
     *
     * @param elApplicationCheckerFactory
     *     the {@code ELApplicationCheckerFactory} to add to the list of supported Expression Language checkers
     */
    public void addSupportedELCheckerApplicationFactory(ELApplicationCheckerFactory elApplicationCheckerFactory) {
        supportedELCheckerApplicationFactories.add(elApplicationCheckerFactory);
    }

    /**
     * Method for better testability.
     *
     * @return an instance of the ApplicationArchiveFactory
     */
    protected ApplicationArchiveFactory getApplicationArchiveFactory() {
        return ApplicationArchiveFactory.getInstance();
    }
}
