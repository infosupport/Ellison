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

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author StefanZ
 */
public class JSFSAXHandler extends DefaultHandler {
    private Locator locator;
    private JSFSAXExpressionHolder jsfSaxExpressionHolder;

    /**
     * Constructor.
     *
     * @param applicationArchive
     *     the application archive within which the page we are going to parse can be found
     */
    public JSFSAXHandler(ApplicationArchive applicationArchive) {
        super();
        jsfSaxExpressionHolder = new JSFSAXExpressionHolder(applicationArchive);
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        return new InputSource(new StringReader(""));
    }

    @Override
    public void notationDecl(String name, String publicId, String systemId) throws SAXException {
        super.notationDecl(name, publicId, systemId);
        jsfSaxExpressionHolder.endText();
    }

    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName)
        throws SAXException {
        super.unparsedEntityDecl(name, publicId, systemId, notationName);
        jsfSaxExpressionHolder.endText();
    }

    @Override
    public void setDocumentLocator(Locator locatorToSet) {
        this.locator = locatorToSet;
    }

    @Override
    public void endDocument() throws SAXException {
        jsfSaxExpressionHolder.endDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        super.startPrefixMapping(prefix, uri);
        jsfSaxExpressionHolder.endText();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        for (int i = 0; i < attributes.getLength(); i++) {
            jsfSaxExpressionHolder.writeAttribute(attributes.getValue(i), locator);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        jsfSaxExpressionHolder.writeCharacters(ch, start, length, locator);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        super.processingInstruction(target, data);
        jsfSaxExpressionHolder.endText();
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        super.skippedEntity(name);
        jsfSaxExpressionHolder.endText();
    }

    /**
     * Gets all the found expressions within the current document.
     *
     * @return a collection of all found expressions
     */
    public Collection<LocationAwareELExpression> getELExpressions() {
        return jsfSaxExpressionHolder.getELExpressions();
    }

    /**
     * Gets all found errors within the current document.
     *
     * @return a collection of all found errors
     */
    public Collection<ELError> getErrors() {
        return jsfSaxExpressionHolder.getElErrors();
    }
}
