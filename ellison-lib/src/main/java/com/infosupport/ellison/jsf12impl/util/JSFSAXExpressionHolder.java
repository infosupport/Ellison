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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.el.ELException;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.archive.ApplicationArchive;
import com.infosupport.ellison.core.util.Location;
import com.infosupport.ellison.core.util.LocationAwareELExpressionString;
import com.infosupport.ellison.core.util.Pair;
import com.sun.el.lang.ExpressionBuilder;
import com.sun.el.parser.Node;

/**
 * Class used in conjunction with {@link JSFSAXHandler}.
 * <p/>
 * It saves expressions as they are found in JSF pages.
 *
 * @author StefanZ
 */
public class JSFSAXExpressionHolder {
    private StringBuffer stringBuffer;
    private Collection<LocationAwareELExpression> elExpressions;
    private Collection<ELError> elErrors;
    private Collection<ELError> unmodifiableELErrors;
    private Collection<LocationAwareELExpression> unmodifiableELExpressions;
    private boolean inText = false;
    private Locator beginLocator = null;
    private ApplicationArchive applicationArchive;

    /**
     * Constructor.
     *
     * @param applicationArchive
     *     the application archive within which we are looking for expressions
     */
    public JSFSAXExpressionHolder(ApplicationArchive applicationArchive) {
        stringBuffer = new StringBuffer();
        elExpressions = new ArrayList<>();
        elErrors = new ArrayList<>();
        unmodifiableELErrors = Collections.unmodifiableCollection(elErrors);
        unmodifiableELExpressions = Collections.unmodifiableCollection(elExpressions);
        this.applicationArchive = applicationArchive;
    }

    /**
     * Adds read characters to an internal buffer.
     *
     * @param chars
     *     the read characters
     * @param start
     *     the index within {@code chars} at which to start reading characters
     * @param length
     *     the amount of characters to read in {@code chars}
     * @param locator
     *     the {@code Locator} that indicates what document is being read, and where in that document the parser is
     *     currently located
     *
     * @see Locator
     */
    public void writeCharacters(char[] chars, int start, int length, Locator locator) {
        if (!inText) {
            inText = true;
            beginLocator = new LocatorImpl(locator);
        }
        stringBuffer.append(chars, start, length);
    }

    /**
     * Ends whatever floating text was left (thus making it an expression), and adds the value of the attribute as an
     * expression itself.
     *
     * @param attributeValue
     *     the attribute value to handle as an expression
     * @param locator
     *     the current location within the document
     *
     * @see Locator
     */
    public void writeAttribute(String attributeValue, Locator locator) {
        endText();
        addExpression(attributeValue, locator);
    }

    /**
     * If the parser was previously reading normal "text" from a document, add whatever was in the buffer thus far as
     * an
     * expression, then clear the buffer.
     */
    public void endText() {
        if (inText) {
            addExpression(stringBuffer.toString(), beginLocator);
            stringBuffer.setLength(0);
            inText = false;
        }
    }

    /**
     * To be called whenever the parser reaches the end of a document. It also ends free-floating text, though it
     * should
     * be impossible to reach such a state in a valid XML document.
     */
    public void endDocument() {
        endText();
    }

    private void addExpression(String expressionString, Locator locator) {
        Node elNode = null;
        String trimmedExpressionString = expressionString.trim();
        Location location = null;

        if (trimmedExpressionString.length() > 0) {
            location = new Location(applicationArchive.relativizePath(URI.create(locator.getSystemId())),
                                    locator.getLineNumber(), locator.getColumnNumber());
            try {
                elNode = ExpressionBuilder.createNode(trimmedExpressionString);
            } catch (ELException e) {
                elErrors.add(new ELError(String.format("%s: %s", e.getMessage(), e.getCause().getMessage()),
                                         new LocationAwareELExpressionString(trimmedExpressionString, location),
                                         ELError.ELErrorSeverity.ERROR));
            }

            if (elNode != null) {
                Pair<List<LocationAwareELExpression>, List<ELError>> foundExpressionsAndErrors =
                    ELNodeUtil.extractCheckableExpressions(elNode, location);
                elExpressions.addAll(foundExpressionsAndErrors.first());
                elErrors.addAll(foundExpressionsAndErrors.second());
            }
        }
    }

    /**
     * Gets all the found expressions within the current document.
     *
     * @return a collection of all found expressions
     */
    public Collection<LocationAwareELExpression> getELExpressions() {
        return unmodifiableELExpressions;
    }

    /**
     * Gets all found errors within the current document.
     *
     * @return a collection of all found errors
     */
    public Collection<ELError> getElErrors() {
        return unmodifiableELErrors;
    }
}
