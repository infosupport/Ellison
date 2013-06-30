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
package com.infosupport.ellison.jsf12impl.apiimpl;

import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.ELFinder;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.infosupport.ellison.core.util.Location;
import com.infosupport.ellison.core.util.LocationAwareELExpressionString;
import com.infosupport.ellison.core.util.Pair;
import com.infosupport.ellison.jsf12impl.util.JSFPageParser;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Use this class to find Expression Language expressions within Facelets pages.
 * <p/>
 * The actual finding of expressions is done as follows:<ul>
 * <li>Upon reading any text that is not a tag name, attribute name or attribute value (see {@link
 * org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)}, parse that as if it were one long expression using
 * {@link com.sun.el.lang.ExpressionBuilder#createNode(String)}</li>
 * <li>Parse any attribute values as EL expressions.</li>
 * </ul>
 * All parsed expressions are then "filtered", by extracting anything that is not a {@link javax.el.ValueExpression}
 * or {@link com.sun.el.parser.AstIdentifier}. See {@link
 * com.infosupport.ellison.jsf12impl.util.ELNodeUtil#extractCheckableExpressions(com.sun.el.parser.Node,
 * com.infosupport.ellison.core.util.Location)} to see how that works.
 * <p/>
 * Note that any expression that cannot be parsed will be wrapped in an {@link ELError}.
 *
 * @author StefanZ
 */
public class JSFELFinder implements ELFinder {
    @Override
    public Pair<Collection<LocationAwareELExpression>, Collection<ELError>> findELExpressions(Collection<URI> pages,
                                                                                              ApplicationArchive
                                                                                                  applicationArchive) {
        Collection<LocationAwareELExpression> expressions = new ArrayList<>();
        Collection<ELError> errors = new ArrayList<>();

        for (URI pageURI : pages) {
            File pageFile = null;
            try {
                pageFile = applicationArchive.getFile(pageURI.toString());
            } catch (IOException e) {
                errors.add(new ELError(String.format("Could not find page '%s'", pageURI.toString()),
                                       new LocationAwareELExpressionString("", new Location(pageURI, 0, 0)),
                                       ELError.ELErrorSeverity.ERROR));
            }
            JSFPageParser pageParser = null;
            boolean pageParsingFailed = false;

            if (pageFile != null) {
                try {
                    pageParser = new JSFPageParser(applicationArchive);
                } catch (SAXException | ParserConfigurationException e) {
                    throw new RuntimeException("Could not setup an XML parser to handle JSF pages.", e);
                }


                try {
                    pageParser.parse(pageFile);
                } catch (SAXException | IOException e) {
                    pageParsingFailed = true;
                    errors.add(new ELError(e.getMessage(),
                                           new LocationAwareELExpressionString(null, new Location(pageURI, 0, 0)),
                                           ELError.ELErrorSeverity.ERROR));
                }

                if (!pageParsingFailed) {
                    errors.addAll(pageParser.getErrors());
                    expressions.addAll(pageParser.getExpressions());
                }
            }
        }

        return new Pair<>(expressions, errors);
    }
}
