/*
 * $Id: SABIORKTest.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/test/org/sbml/squeezer/test/sabiork/SABIORKTest.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2018 by the University of Tuebingen, Germany.
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
package org.sbml.squeezer.test.sabiork;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.util.ValuePair;
import org.sbml.squeezer.sabiork.util.WebServiceConnectException;
import org.sbml.squeezer.sabiork.util.WebServiceResponseException;
import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.SABIORK.QueryField;

/**
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
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
    String expected = "°C";
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
    List<ValuePair<SABIORK.QueryField, String>> searchTerms = new ArrayList<ValuePair<SABIORK.QueryField, String>>();
    searchTerms.add(new ValuePair<SABIORK.QueryField, String>(
        SABIORK.QueryField.ORGANISM, "  homo sapiens   "));
    searchTerms.add(new ValuePair<SABIORK.QueryField, String>(
        SABIORK.QueryField.ENZYMENAME, "           "));
    searchTerms.add(new ValuePair<SABIORK.QueryField, String>(
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
