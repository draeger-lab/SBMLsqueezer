package sabiork.util;

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
