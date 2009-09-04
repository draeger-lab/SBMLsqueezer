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

import org.sbml.jlibsbml.ASTNode;
import org.sbml.jlibsbml.ModifierSpeciesReference;
import org.sbml.jlibsbml.Parameter;
import org.sbml.jlibsbml.Reaction;
import org.sbml.jlibsbml.SBO;
import org.sbml.jlibsbml.Species;
import org.sbml.squeezer.ModificationException;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates a Hill equation as defined in the paper"Hill Kinetics
 * meets P Systems: A Case Study on Gene Regulatory Networks as Computing Agents
 * in silico and in vivo" of Hinze et al.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:supper@genomatix.de">Jochen Supper</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Aug 7, 2007
 */
public class HillEquation extends BasicKineticLaw {

	/**
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public HillEquation(Reaction parentReaction)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
	}

	public static boolean isApplicable(Reaction reaction) {
		// TODO: add Hillequation if reactiontype is translation or
		// transcription
		if (SBO.isTranslation(reaction.getSBOTerm())
				|| SBO.isTranscription(reaction.getSBOTerm()))
			return true;
		return false;
	}

	public String getName() {
		String name;
		if (getParentSBMLObject().getNumModifiers() > 0)
			name = "Hill equation, microscopic form";
		else
			name = "mass action rate law for zeroth order irreversible reactions, continuous scheme";
		return name;
	}

	// // @Override
	// public String getSBO() {
	// String name = getName().toLowerCase(), sbo = "none";
	// if (name.equals("hill equation"))
	// sbo = "0000192";
	// else if (name.equals("hill equation, microscopic form"))
	// sbo = "0000195";
	// else if (name.equals("hill equation, reduced form"))
	// sbo = "0000198";
	// else if (name
	// .equals(
	// "mass action rate law for zeroth order irreversible reactions, continuous scheme"
	// ))
	// sbo = "0000047";
	// return sbo;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List, java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		if (!modActi.isEmpty())
			modTActi.addAll(modActi);
		/*
		 * throw new ModificationException("Wrong activation in reaction " +
		 * model.getReaction(reactionNum).getId() +
		 * ". Only transcriptional or translational activation, " +
		 * "respectively, is allowed here.");
		 */
		if (!modInhib.isEmpty())
			modTInhib.addAll(modInhib);
		/*
		 * throw new ModificationException("Wrong inhibition in reaction " +
		 * reactionNum + ". Only transcriptional or translational inhibition, "
		 * + "respectively, is allowed here.");
		 */
		// necessary due to the changes in CellDesigner from version 4.0 alpha
		// to beta and 4.0.1
		if (!modE.isEmpty())
			modTActi.addAll(modE);
		if (!modCat.isEmpty())
			modTActi.addAll(modCat);

		Reaction reaction = getParentSBMLObject();
		for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
			if (SBO.isGene(reaction.getReactant(0).getSpeciesInstance()
					.getSBOTerm())
					&& (SBO.isTranslationalActivation(modifier.getSBOTerm()) || SBO
							.isTranslationalInhibitor(modifier.getSBOTerm())))
				throw new ModificationException(
						"Wrong activation in reaction "
								+ reaction.getId()
								+ ". Only transcriptional modification is allowed here.");
			else if ((SBO.isMessengerRNA(reaction.getReactant(0)
					.getSpeciesInstance().getSBOTerm())
					|| SBO.isRNA(reaction.getReactant(0).getSpeciesInstance()
							.getSBOTerm()))
					&& (SBO.isTranscriptionalActivation(modifier.getSBOTerm()) || SBO
							.isTranscriptionalInhibitor(modifier.getSBOTerm())))
				throw new ModificationException("Wrong activation in reaction "
						+ reaction.getId()
						+ ". Only translational modification is allowed here.");
		}

		if (SBO.isTranslation(reaction.getSBOTerm())
				|| SBO.isTranscription(reaction.getSBOTerm()))
			for (int i = 0; i < reaction.getNumReactants(); i++)
				modTActi.add(reaction.getReactant(i).getSpecies());
		ASTNode formula = createHillEquation(modTActi, modTInhib);
		// Influence of the concentrations of the reactants:
		for (int reactantNum = 0; reactantNum < reaction.getNumReactants(); reactantNum++) {
			Species reactant = reaction.getReactant(reactantNum)
					.getSpeciesInstance();
			ASTNode gene;
			if (!SBO.isGene(reactant.getSBOTerm())) {
				gene = new ASTNode(reactant, this);
				if (reaction.getReactant(reactantNum).getStoichiometry() != 1f) {
					gene.raiseByThePowerOf(reaction.getReactant(reactantNum)
							.getStoichiometry());
					formula.multiplyWith(gene);
				}
			}
		}
		return formula;
	}

	/**
	 * This method actually creates the Hill equation.
	 * 
	 * @param reaction
	 * @param modTActi
	 * @param modTInhib
	 * @return
	 */
	private ASTNode createHillEquation(List<String> modTActi,
			List<String> modTInhib) {
		ASTNode acti[] = new ASTNode[modTActi.size()];
		ASTNode inhib[] = new ASTNode[modTInhib.size()];
		String rId = getParentSBMLObject().getId();

		// KS: half saturation constant.
		int i;
		for (i = 0; i < modTActi.size(); i++)
		/*
		 * if (!model.getSpecies(modTActi.get(i)).getSpeciesAlias(0).getType()
		 * .toUpperCase().equals("GENE"))
		 */{
			Parameter p_hillcoeff = new Parameter(concat("np_", rId,
					underscore, modTActi.get(i)).toString(), getLevel(), getVersion());
			Parameter p_kS = new Parameter(concat("kSp_", rId, underscore,
					modTActi.get(i)).toString(), getLevel(), getVersion());
			addLocalParameters(p_hillcoeff, p_kS);
			acti[i] = ASTNode.times(ASTNode.frac(ASTNode.pow(new ASTNode(
					modTActi.get(i), this), new ASTNode(p_hillcoeff, this)),
					ASTNode.sum(ASTNode.pow(new ASTNode(modTActi.get(i), this),
							new ASTNode(p_hillcoeff, this)), ASTNode.pow(
							new ASTNode(p_kS, this), new ASTNode(p_hillcoeff,
									this)))));
		}
		for (i = 0; i < modTInhib.size(); i++)
		/*
		 * if (!model.getSpecies(modTInhib.get(i)).getSpeciesAlias(0)
		 * .getType().toUpperCase().equals("GENE"))
		 */{
			Parameter p_hillcoeff = new Parameter(concat("nm_", rId,
					underscore, modTInhib.get(i)).toString(), getLevel(), getVersion());
			Parameter p_kS = new Parameter(concat("kSm_", rId, underscore,
					modTInhib.get(i)).toString(), getLevel(), getVersion());
			inhib[i] = ASTNode.times(ASTNode.diff(new ASTNode(1, this), ASTNode
					.frac(ASTNode.pow(new ASTNode(modTInhib.get(i), this),
							new ASTNode(p_hillcoeff, this)), ASTNode.sum(
							ASTNode.pow(new ASTNode(modTInhib.get(i), this),
									new ASTNode(p_hillcoeff, this)), ASTNode
									.pow(new ASTNode(p_kS, this), new ASTNode(
											p_hillcoeff, this))))));
		}
		Parameter p_kg = new Parameter(concat("kg_", rId).toString(), getLevel(), getVersion());
		addParameter(p_kg);

		ASTNode formelTxt = new ASTNode(p_kg, this);
		if (modTActi.size() > 0)
			formelTxt.multiplyWith(acti);
		if (modTInhib.size() > 0)
			formelTxt.multiplyWith(inhib);
		return formelTxt;
	}
}
