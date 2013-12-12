package org.sbml.squeezer.test;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;

public class MiniSBML {
	
	/**
	 * @param args
	 * @throws XMLStreamException 
	 * @throws SBMLException 
	 */
	public static void main(String[] args) throws SBMLException, XMLStreamException {
		SBMLDocument doc = new SBMLDocument(2, 3);
		Model model = doc.createModel("m1");
		Compartment c = model.createCompartment("c1");
		Species s1 = model.createSpecies("s1", c);
		Species s2 = model.createSpecies("s2", c);
		Reaction r = model.createReaction("r1");
		r.createReactant("s1ref", s1);
		r.createProduct("s2Ref", s2);
		SBMLWriter.write(doc, System.out, ' ', (short) 2);
	}
	
}
