/*
 * $$Id${file_name} ${time} ${user} $$
 * $$URL${file_name} $$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
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
import java.io.InputStream;
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
 * @author Matthias Rall
 * @version $$Rev$$
 * ${tags}
 */
public class XMLParser {

	public static List<String> getMultipleElementTextContent(String xml, String elementQNameLocalPart, String elementQNameNamespaceURI) throws XMLStreamException, UnsupportedEncodingException {
		List<String> multipleElementTextContent = new ArrayList<String>();
		InputStream inputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(inputStream);
		while (xmlEventReader.hasNext()) {
			XMLEvent xmlEvent = xmlEventReader.nextEvent();
			if (xmlEvent.isStartElement()) {
				StartElement startElement = xmlEvent.asStartElement();
				String startElementQNameLocalPart = startElement.getName().getLocalPart();
				String startElementQNameNamespaceURI = startElement.getName().getNamespaceURI();
				if (startElementQNameLocalPart.equals(elementQNameLocalPart) && startElementQNameNamespaceURI.equals(elementQNameNamespaceURI)) {
					xmlEvent = xmlEventReader.nextEvent();
					if (xmlEvent.isCharacters()) {
						Characters characters = xmlEvent.asCharacters();
						if (!characters.isWhiteSpace()) {
							multipleElementTextContent.add(characters.getData().trim());
						}
					}
				}
			}
		}
		return multipleElementTextContent;
	}

	public static String getFirstElementTextContent(String xml, String elementQNameLocalPart, String elementQNameNamespaceURI) throws XMLStreamException, UnsupportedEncodingException {
		String firstElementTextContent = "";
		List<String> multipleElementTextContent = getMultipleElementTextContent(xml, elementQNameLocalPart, elementQNameNamespaceURI);
		if (!multipleElementTextContent.isEmpty()) {
			firstElementTextContent = multipleElementTextContent.get(0);
		}
		return firstElementTextContent;
	}

}
