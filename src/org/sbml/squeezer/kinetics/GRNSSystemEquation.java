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
 * 
 * This class creates a S-System based equation as defined in the papers
 * "A model-based optimization framework for the inference 
 * on gene regulatory networks from DNA array data" and 
 * "A model-based optimization framework for the inference 
 * of regulatory interactions using time-course DNA microarray expression data" 
 * of Thomas et al. 2004 and 2007
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 * 
 */
public class GRNSSystemEquation extends BasicKineticLaw {

	/**
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public GRNSSystemEquation(Reaction parentReaction)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction);
		// TODO Automatisch erstellter Konstruktoren-Stub
	}

	public static boolean isApplicable(Reaction reaction) {
		System.out.println("isApplicable: "+reaction.isSetSBOTerm()+"SBOTerm reaction: "+ reaction.getSBOTerm());
		if (SBO.isTranslation(reaction.getSBOTerm())
				|| SBO.isTranscription(reaction.getSBOTerm()))
			return true;
		return false;
	}

	/*
	 * (Kein Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List, java.util.List)
	 */
	@Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {

		if (!modActi.isEmpty())
			modTActi.addAll(modActi);
		if (!modInhib.isEmpty())
			modTInhib.addAll(modInhib);
		if (!modE.isEmpty())
			modTActi.addAll(modE);
		if (!modCat.isEmpty())
			modTActi.addAll(modCat);
		Reaction r = getParentSBMLObject();

		// Exceptions
		for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
			if (SBO.isGene(r.getReactant(0).getSpeciesInstance().getSBOTerm())
					&& (SBO.isTranslationalActivation(modifier.getSBOTerm()) || SBO
							.isTranslationalInhibitor(modifier.getSBOTerm())))
				throw new ModificationException(
						"Wrong activation in reaction "
								+ r.getId()
								+ ". Only transcriptional modification is allowed here.");
			else if ((SBO.isMessengerRNA(r.getReactant(0).getSpeciesInstance()
					.getSBOTerm()) || SBO.isRNA(r.getReactant(0)
					.getSpeciesInstance().getSBOTerm()))
					&& (SBO.isTranscriptionalActivation(modifier.getSBOTerm()) || SBO
							.isTranscriptionalInhibitor(modifier.getSBOTerm())))
				throw new ModificationException("Wrong activation in reaction "
						+ r.getId()
						+ ". Only translational modification is allowed here.");
		}

		ASTNode kineticLaw = new ASTNode(this);
		ASTNode kineticLawPart = new ASTNode(this);
		String rId = getParentSBMLObject().getId();

		Parameter a = createOrGetParameter(concat("a_", rId, underscore)
				.toString());
		Parameter b = createOrGetParameter(concat("b_", rId, underscore)
				.toString());
		Parameter c = createOrGetParameter(concat("c_", rId, underscore)
				.toString());
		Parameter d = createOrGetParameter(concat("d_", rId, underscore)
				.toString());

		ASTNode nodea = new ASTNode(a, this);
		ASTNode nodeb = new ASTNode(b, this);
		ASTNode nodec = new ASTNode(c, this);
		ASTNode noded = new ASTNode(d, this);

		Species reactant = r.getReactant(0).getSpeciesInstance();
		Species product = r.getProduct(0).getSpeciesInstance();
		ASTNode productnode = new ASTNode(product, this);

		// Transkription		
		if (SBO.isTranscription(r.getSBOTerm())){
			// kann eine Transkription noch anders definiert sein (siehe CellDesigner)?
			// if (SBO.isEmptySet(reactant.getSBOTerm()) && SBO.isRNA(product.getSBOTerm()))...

			System.out.println("Das ist eine Transkription! SBOTerm: "+r.getSBOTerm());
			for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
				Species modifier = r.getModifier(modifierNum).getSpeciesInstance();
				
				if(SBO.isProtein(modifier.getSBOTerm())){
					System.out.println("Modifier " + modifier + " ist ein Protein! SBOTerm: "+modifier.getSBOTerm() );

					Parameter e = createOrGetParameter("e_", modifierNum, rId);
					System.out.println("Parameter erstellt: " + e.toString());
					
					//TODO: ist ein modifier ein aktivator oder inhibitor (sboterm)?
					
					/*if (SBO.isTranscriptionalActivation(modifier.getSBOTerm())) {
						System.out.println("Modifier " + modifier
								+ " ist Aktivator! set value 1");
						e.setValue(1);
						e.appendNotes("pos");
					}
					if (SBO.isTranscriptionalInhibitor(modifier.getSBOTerm())) {
						System.out.println("Modifier " + modifier
								+ " ist Inhibitor! set value -1");
						e.setValue(-1);
						e.appendNotes("neg");
					}*/

					ASTNode modnode = new ASTNode(modifier, this);
					ASTNode enode = new ASTNode(e, this);
					if (kineticLawPart.isUnknown())
						kineticLawPart = ASTNode.pow(modnode, enode);
					else
						kineticLawPart = ASTNode.times(kineticLawPart, ASTNode.pow(
								modnode, enode));

					kineticLaw = ASTNode.diff(ASTNode.times(nodea, kineticLawPart),
							ASTNode.times(nodeb, productnode));
				}
			}
		}
			
		// Translation
		if (SBO.isTranslation(r.getSBOTerm())){
			// kann eine Translation noch anders definiert sein (siehe CellDesigner)?
			// if (SBO.isEmptySet(reactant.getSBOTerm()) && SBO.isProtein(product.getSBOTerm()))...

			System.out.println("Das ist eine Translation! SBOTerm: "+r.getSBOTerm());
			for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
				Species modifier = r.getModifier(modifierNum).getSpeciesInstance();
				if(SBO.isRNA(modifier.getSBOTerm())){
				//TODO: rna des proteins?
				System.out.println("Modifier " + modifier + " ist eine RNA! SBOTerm: "+modifier.getSBOTerm());

				ASTNode modnode = new ASTNode(modifier, this);

				kineticLaw = ASTNode.diff(ASTNode.times(nodec, modnode), ASTNode
						.times(noded, productnode));
				}
			}
		}

		System.out.println(kineticLaw.toLaTeX());
		return kineticLaw;
	}
}
