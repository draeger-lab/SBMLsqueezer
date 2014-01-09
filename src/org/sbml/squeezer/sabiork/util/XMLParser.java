/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2014 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.sabiork.util;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * A class for simple XML parsing.
 * 
 * @author Matthias Rall
 * @version $Rev$
 */
public class XMLParser {

	/**
	 * Returns all text contents of a given XML element.
	 * 
	 * @param xml
	 *            the XML document
	 * @param xmlElementQNameLocalPart
	 *            the qualified name of the XML element
	 * @param xmlElementQNameNamespaceURI
	 *            the namespace of the XML element
	 * @return a list of all text contents of the given XML element
	 * @throws XMLStreamException
	 * @throws UnsupportedEncodingException
	 */
	public static List<String> getMultipleXMLElementTextContent(String xml,
			String xmlElementQNameLocalPart, String xmlElementQNameNamespaceURI)
			throws XMLStreamException, UnsupportedEncodingException {
		List<String> multipleXMLElementTextContent = new ArrayList<String>();
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLEventReader xmlEventReader = xmlInputFactory
				.createXMLEventReader(new ByteArrayInputStream(xml
						.getBytes("UTF-8")));
		while (xmlEventReader.hasNext()) {
			XMLEvent xmlEvent = xmlEventReader.nextEvent();
			if (xmlEvent.isStartElement()) {
				StartElement startElement = xmlEvent.asStartElement();
				String startElementQNameLocalPart = startElement.getName()
						.getLocalPart();
				String startElementQNameNamespaceURI = startElement.getName()
						.getNamespaceURI();
				if (startElementQNameLocalPart.equals(xmlElementQNameLocalPart)
						&& startElementQNameNamespaceURI
								.equals(xmlElementQNameNamespaceURI)) {
					xmlEvent = xmlEventReader.nextEvent();
					if (xmlEvent.isCharacters()) {
						Characters characters = xmlEvent.asCharacters();
						if (!characters.isWhiteSpace()) {
							multipleXMLElementTextContent.add(characters
									.getData().trim());
						}
					}
				}
			}
		}
		return multipleXMLElementTextContent;
	}

	/**
	 * Returns the text content of a given XML element.
	 * 
	 * @param xml
	 *            the XML document
	 * @param xmlElementQNameLocalPart
	 *            the qualified name of the XML element
	 * @param xmlElementQNameNamespaceURI
	 *            the namespace of the XML element
	 * @return the text content of the given XML element
	 * @throws XMLStreamException
	 * @throws UnsupportedEncodingException
	 */
	public static String getXMLElementTextContent(String xml,
			String xmlElementQNameLocalPart, String xmlElementQNameNamespaceURI)
			throws XMLStreamException, UnsupportedEncodingException {
		String xmlElementTextContent = "";
		List<String> multipleXMLElementTextContent = getMultipleXMLElementTextContent(
				xml, xmlElementQNameLocalPart, xmlElementQNameNamespaceURI);
		if (!multipleXMLElementTextContent.isEmpty()) {
			xmlElementTextContent = multipleXMLElementTextContent.get(0);
		}
		return xmlElementTextContent;
	}

}
