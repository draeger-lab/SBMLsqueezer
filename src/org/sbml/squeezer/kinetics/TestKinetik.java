/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.kinetics;

import java.io.IOException;
import java.util.IllegalFormatException;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.ModificationException;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 *
 */
public class TestKinetik extends BasicKineticLaw {

	/**
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public TestKinetik(Reaction parentReaction)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
		// TODO Automatisch erstellter Konstruktoren-Stub
	}
	
	public static boolean isApplicable(Reaction reaction) {
		return true;
	}
	
	/* (Kein Javadoc)
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	@Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		
		if (!modActi.isEmpty()) modTActi.addAll(modActi);
		if (!modInhib.isEmpty()) modTInhib.addAll(modInhib);
		if (!modE.isEmpty()) modTActi.addAll(modE);
		if (!modCat.isEmpty()) modTActi.addAll(modCat);
		Reaction r = getParentSBMLObject();
		
		// Exceptions
		for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
			if (SBO.isGene(r.getReactant(0).getSpeciesInstance().getSBOTerm())
					&& (SBO.isTranslationalActivation(modifier.getSBOTerm()) || SBO.isTranslationalInhibitor(modifier.getSBOTerm())))
				throw new ModificationException("Wrong activation in reaction "+ r.getId()+ ". Only transcriptional modification is allowed here.");
			else if ((SBO.isMessengerRNA(r.getReactant(0).getSpeciesInstance().getSBOTerm())
					|| SBO.isRNA(r.getReactant(0).getSpeciesInstance().getSBOTerm()))
					&& (SBO.isTranscriptionalActivation(modifier.getSBOTerm()) || SBO.isTranscriptionalInhibitor(modifier.getSBOTerm())))
				throw new ModificationException("Wrong activation in reaction "+ r.getId()+ ". Only translational modification is allowed here.");
		}
		
		ASTNode kineticLaw = new ASTNode(this);	
		ASTNode kineticLawPart = new ASTNode(this);	
		String rId = getParentSBMLObject().getId();
		
		Parameter a = createOrGetParameter(concat("a_", rId, underscore).toString());
		Parameter b = createOrGetParameter(concat("b_", rId, underscore).toString());
		Parameter c = createOrGetParameter(concat("c_", rId, underscore).toString());
		Parameter d = createOrGetParameter(concat("d_", rId, underscore).toString());
		
		ASTNode nodea = new ASTNode(a, this);
		ASTNode nodeb = new ASTNode(b, this);
		//ASTNode nodec = new ASTNode(c, this);
		//ASTNode noded = new ASTNode(d, this);
		
		// Species reactant = r.getReactant(0).getSpeciesInstance();
		Species product = r.getProduct(0).getSpeciesInstance();
		ASTNode productnode = new ASTNode(product, this);
		
		// Transkription
		// if (SBO.isEmptySet(reactant.getSBOTerm()) && SBO.isRNA(product.getSBOTerm())) {
		for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
			Species modifier = r.getModifier(modifierNum).getSpeciesInstance();
			System.out.println("for_testtesttest" + modifier.getSBOTerm());
			// if(SBO.isProtein(modifier.getSBOTerm())){

			Parameter e = createOrGetParameter(concat("e_", modifierNum, rId,
					underscore).toString());
			System.out.println("Mein Parameters: " + e.toString());

			ASTNode modnode = new ASTNode(modifier, this);
			ASTNode enode = new ASTNode(e, this);
			if (kineticLawPart.isUnknown())
				kineticLawPart = ASTNode.pow(modnode, enode);
			else
				kineticLawPart = ASTNode.times(kineticLawPart, ASTNode.pow(modnode, enode));
			//}
		}

		kineticLaw = ASTNode.diff(ASTNode.times(nodea, kineticLawPart), ASTNode
				.times(nodeb, productnode));
		// }
		
		// Translation
		/*
		 * if (SBO.isEmptySet(reactant.getSBOTerm()) && SBO.isProtein(product.getSBOTerm())) { 
		 * 	for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
		 * 	TODO: tranlslation
		 * 	}
		 * }
		 */
		
		return kineticLaw;
	}		
	
	
	/*
	 * (Kein Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getName()
	 */
	@Override
	public String getName() {
		// TODO Automatisch erstellter Methoden-Stub
		// return null;
		String name;
		name = "Kinetik zum Testen";
		return name;
	}

}
