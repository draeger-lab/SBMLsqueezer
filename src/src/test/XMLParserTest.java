package test;

import static org.junit.Assert.*;
import java.io.UnsupportedEncodingException;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;
import sabiork.util.XMLParser;

public class XMLParserTest {

	@Test
	public void testGetMultipleXMLElementTextContent()
			throws UnsupportedEncodingException, XMLStreamException {
		String[] expecteds = new String[] { "glycolysis classical",
				"glycerolipid metabolism", "glycolysis/gluconeogenesis",
				"glycosphingolipid metabolism",
				"glycerophospholipid metabolism",
				"glycine, serine and threonine metabolism" };
		String xml = "<Pathways><Pathway>glycolysis classical</Pathway><Pathway>glycerolipid metabolism</Pathway><Pathway>glycolysis/gluconeogenesis</Pathway><Pathway>glycosphingolipid metabolism</Pathway><Pathway>glycerophospholipid metabolism</Pathway><Pathway>glycine, serine and threonine metabolism</Pathway></Pathways>";
		List<String> actuals = XMLParser.getMultipleXMLElementTextContent(xml,
				"Pathway", "");
		assertArrayEquals(expecteds, actuals.toArray());
	}

	@Test
	public void testGetXMLElementTextContent()
			throws UnsupportedEncodingException, XMLStreamException {
		String expected = "glycolysis classical";
		String xml = "<Pathways><Pathway>glycolysis classical</Pathway><Pathway>glycerolipid metabolism</Pathway><Pathway>glycolysis/gluconeogenesis</Pathway><Pathway>glycosphingolipid metabolism</Pathway><Pathway>glycerophospholipid metabolism</Pathway><Pathway>glycine, serine and threonine metabolism</Pathway></Pathways>";
		String actual = XMLParser.getXMLElementTextContent(xml, "Pathway", "");
		assertTrue(actual.equals(expected));
	}

}
