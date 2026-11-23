/*
 * Copyright (c) 2019 - 2026 Seth Berrier, Kaelie Tetzlaff, Jacob Buelow, Luke Denney, Ian Anderson, Zoe Cuthrell, Blane Suess, Isaac Tesch,
 *                           Nathaniel Willius, Atlas Collins, Simon Cao, Joe Luther, Jakob Schmucki, Nathan Sunday, Joseph O'Connor
 * Copyright (c) 2019 The Regents of the University of Minnesota
 *
 * Licensed under GPLv3
 * ( http://www.gnu.org/licenses/gpl-3.0.html )
 *
 * This code is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 */

package kintsugi3d.builder.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public final class XMLHelper
{
    private static final Logger LOG = LoggerFactory.getLogger(XMLHelper.class);

    private static final String WARNING_MESSAGE = "Warning building XML document.";
    private static final String ERROR_MESSAGE = "Error building XML document.";
    private static final String FATAL_ERROR_MESSAGE = "Fatal error building XML document.";

    private XMLHelper()
    {
    }

    public static DocumentBuilder getDocumentBuilder(ErrorHandler errorHandler) throws ParserConfigurationException
    {
        // Taken from https://www.digitalocean.com/community/tutorials/java-convert-string-to-xml-document-and-xml-document-to-string
        // TODO: MAY BE PRONE TO XXE ATTACKS?
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        builder.setErrorHandler(errorHandler);
        return builder;
    }

    public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
    {
        return getDocumentBuilder(new ErrorLogger());
    }

    public static final class ErrorLogger implements ErrorHandler
    {
        @Override
        public void warning(SAXParseException exception)
        {
            LOG.warn(WARNING_MESSAGE, exception);
        }

        @Override
        public void error(SAXParseException exception) throws SAXParseException
        {
            LOG.error(ERROR_MESSAGE, exception);
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXParseException
        {
            LOG.error(FATAL_ERROR_MESSAGE, exception);
            throw exception;
        }
    }

    public static final class ErrorsAsWarningsLogger implements ErrorHandler
    {
        @Override
        public void warning(SAXParseException exception)
        {
            LOG.warn(WARNING_MESSAGE, exception);
        }

        @Override
        public void error(SAXParseException exception) throws SAXParseException
        {
            LOG.warn(ERROR_MESSAGE, exception);
            throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXParseException
        {
            LOG.warn(FATAL_ERROR_MESSAGE, exception);
            throw exception;
        }
    }
}
