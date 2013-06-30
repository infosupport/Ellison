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

import com.infosupport.ellison.core.api.ELError;
import com.infosupport.ellison.core.api.LocationAwareELExpression;
import com.infosupport.ellison.core.archive.ApplicationArchive;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.*;

/**
 * A {@link SAXParser} for JSF Facelet pages.
 * <p/>
 * This implementation simply delegates all work to a {@code SAXParser}, and uses a {@link
 * com.infosupport.ellison.jsf12impl.util.JSFSAXHandler} to keep track
 * of all found expressions.
 */
public class JSFPageParser extends SAXParser {
    private SAXParser delegate;
    private JSFSAXHandler jsfSaxHandler;

    /**
     * Constructor.
     *
     * @param applicationArchive
     *     the application archive within which we are going to parse pages
     *
     * @throws SAXException
     *     if the parser could not be configured
     * @throws ParserConfigurationException
     *     if the parser could not be configured
     * @see SAXParserFactory#setFeature(String, boolean)
     * @see XMLReader#setProperty(String, Object)
     */
    public JSFPageParser(ApplicationArchive applicationArchive) throws SAXException, ParserConfigurationException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setValidating(false);
        saxParserFactory.setNamespaceAware(true);
        saxParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        saxParserFactory.setFeature("http://xml.org/sax/features/validation", false);

        delegate = saxParserFactory.newSAXParser();
        XMLReader xmlReader = delegate.getXMLReader();
        jsfSaxHandler = new JSFSAXHandler(applicationArchive);
        xmlReader.setEntityResolver(jsfSaxHandler);
        xmlReader.setErrorHandler(jsfSaxHandler);
        xmlReader.setEntityResolver(jsfSaxHandler);
    }

    @Override
    public Parser getParser() throws SAXException {
        return delegate.getParser();
    }

    @Override
    public XMLReader getXMLReader() throws SAXException {
        return delegate.getXMLReader();
    }

    @Override
    public boolean isNamespaceAware() {
        return delegate.isNamespaceAware();
    }

    @Override
    public boolean isValidating() {
        return delegate.isValidating();
    }

    @Override
    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        delegate.setProperty(name, value);
    }

    @Override
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return delegate.getProperty(name);
    }

    /**
     * Parses file {@code f} using the default {@link JSFSAXHandler}.
     *
     * @param f
     *     the file to parse
     *
     * @throws SAXException
     *     if any SAX error occurs during processing
     * @throws IOException
     *     if any IO error occurs during processing
     */
    public void parse(File f) throws SAXException, IOException {
        delegate.parse(f, jsfSaxHandler);
    }

    /**
     * Gets all the found expressions within the current document.
     *
     * @return a collection of all found expressions
     */
    public Collection<LocationAwareELExpression> getExpressions() {
        return jsfSaxHandler.getELExpressions();
    }

    /**
     * Gets all found errors within the current document.
     *
     * @return a collection of all found errors
     */
    public Collection<ELError> getErrors() {
        return jsfSaxHandler.getErrors();
    }
}
