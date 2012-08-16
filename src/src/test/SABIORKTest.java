package test;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;
import org.sbml.jsbml.KineticLaw;
import sabiork.SABIORK;
import sabiork.SABIORK.QueryField;
import sabiork.util.Pair;
import sabiork.util.WebServiceConnectException;
import sabiork.util.WebServiceResponseException;

public class SABIORKTest {

	@Test
	public void testGetIDs() throws WebServiceConnectException,
			WebServiceResponseException, IOException, XMLStreamException {
		Integer[] expecteds = new Integer[] { 23934, 23936, 23937, 23938,
				23939, 23940, 23941 };
		List<Integer> actuals = SABIORK.getIDs(SABIORK.QueryField.ORGANISM
				+ ":\"homo sapiens\""
				+ SABIORK.getFilterOptionsQuery(true, false, true, true, 10.0,
						14.0, -10.0, 115.0, true, true, false, null));
		assertArrayEquals(expecteds, actuals.toArray());
	}

	@Test
	public void testGetKineticLaw() throws UnsupportedEncodingException,
			XMLStreamException, WebServiceConnectException,
			WebServiceResponseException, IOException {
		String expected = String.valueOf(1);
		String actual = SABIORK.getKineticLawID(SABIORK.getKineticLaw(1));
		assertTrue(actual.equals(expected));
	}

	@Test
	public void testGetKineticLawsListOfInteger()
			throws WebServiceConnectException, WebServiceResponseException,
			IOException, XMLStreamException {
		List<Integer> expecteds = new ArrayList<Integer>();
		expecteds.add(1);
		expecteds.add(100);
		expecteds.add(1000);
		List<Integer> actuals = new ArrayList<Integer>();
		List<KineticLaw> kineticLaws = SABIORK.getKineticLaws(expecteds);
		for (KineticLaw kineticLaw : kineticLaws) {
			actuals.add(Integer.valueOf(SABIORK.getKineticLawID(kineticLaw)));
		}
		assertArrayEquals(expecteds.toArray(), actuals.toArray());
	}

	@Test
	public void testGetKineticLawsString() throws WebServiceConnectException,
			WebServiceResponseException, IOException, XMLStreamException {
		Integer[] expecteds = new Integer[] { 23934, 23936, 23937, 23938,
				23939, 23940, 23941 };
		List<KineticLaw> kineticLaws = SABIORK
				.getKineticLaws(SABIORK.QueryField.ORGANISM
						+ ":\"homo sapiens\""
						+ SABIORK.getFilterOptionsQuery(true, false, true,
								true, 10.0, 14.0, -10.0, 115.0, true, true,
								false, null));
		List<Integer> actuals = new ArrayList<Integer>();
		for (KineticLaw kineticLaw : kineticLaws) {
			actuals.add(Integer.valueOf(SABIORK.getKineticLawID(kineticLaw)));
		}
		assertArrayEquals(expecteds, actuals.toArray());
	}

	@Test
	public void testGetSuggestions() throws WebServiceConnectException,
			WebServiceResponseException, IOException, XMLStreamException {
		String[] expecteds = new String[] { "lung", "lung cancer cell",
				"lung mucoepidermoid carcinoma" };
		List<String> actuals = SABIORK.getSuggestions(
				SABIORK.QueryField.TISSUE, "lu");
		assertArrayEquals(expecteds, actuals.toArray());
	}

	@Test
	public void testGetKineticLawID() throws WebServiceConnectException,
			WebServiceResponseException, IOException, XMLStreamException {
		String expected = "1";
		String actual = SABIORK.getKineticLawID(SABIORK.getKineticLaw(1));
		assertTrue(actual.equals(expected));
	}

	@Test
	public void testGetStartValuepH() throws UnsupportedEncodingException,
			XMLStreamException, WebServiceConnectException,
			WebServiceResponseException, IOException {
		String expected = "7.5";
		String actual = SABIORK.getStartValuepH(SABIORK.getKineticLaw(1));
		assertTrue(actual.equals(expected));
	}

	@Test
	public void testGetStartValueTemperature()
			throws UnsupportedEncodingException, XMLStreamException,
			WebServiceConnectException, WebServiceResponseException,
			IOException {
		String expected = "25.0";
		String actual = SABIORK.getStartValueTemperature(SABIORK
				.getKineticLaw(1));
		assertTrue(actual.equals(expected));
	}

	@Test
	public void testGetTemperatureUnit() throws UnsupportedEncodingException,
			XMLStreamException, WebServiceConnectException,
			WebServiceResponseException, IOException {
		String expected = "¡C";
		String actual = SABIORK.getTemperatureUnit(SABIORK.getKineticLaw(1));
		assertTrue(actual.equals(expected));
	}

	@Test
	public void testGetBuffer() throws UnsupportedEncodingException,
			XMLStreamException, WebServiceConnectException,
			WebServiceResponseException, IOException {
		String expected = "50 mM potassium phosphate, 4 % DMSO";
		String actual = SABIORK.getBuffer(SABIORK.getKineticLaw(1));
		assertTrue(actual.equals(expected));
	}

	@Test
	public void testGetSearchTermsQuery() {
		String expected = SABIORK.QueryField.ORGANISM + ":\"homo sapiens\""
				+ " AND " + SABIORK.QueryField.TISSUE + ":liver";
		List<Pair<SABIORK.QueryField, String>> searchTerms = new ArrayList<Pair<SABIORK.QueryField, String>>();
		searchTerms.add(new Pair<SABIORK.QueryField, String>(
				SABIORK.QueryField.ORGANISM, "  homo sapiens   "));
		searchTerms.add(new Pair<SABIORK.QueryField, String>(
				SABIORK.QueryField.ENZYMENAME, "           "));
		searchTerms.add(new Pair<SABIORK.QueryField, String>(
				SABIORK.QueryField.TISSUE, "   liver   "));
		String actual = SABIORK.getSearchTermsQuery(searchTerms);
		assertTrue(actual.equals(expected));
	}

	@Test
	public void testGetFilterOptionsQuery() {
		String expected = " AND " + QueryField.HAS_KINETIC_DATA + ":true"
				+ " AND " + QueryField.PH_VALUE_RANGE + ":[" + 0.0 + " TO "
				+ 14.0 + "]" + " AND " + QueryField.TEMPERATURE_RANGE + ":["
				+ -10.0 + " TO " + 115.0 + "]";
		String actual = SABIORK.getFilterOptionsQuery(null, null, null, null,
				null, null, null, null, null, null, null, null);
		assertTrue(actual.equals(expected));
	}

}
