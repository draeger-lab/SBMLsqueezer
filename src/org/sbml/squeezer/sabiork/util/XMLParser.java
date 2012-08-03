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
