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

import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates a Hill equation as defined in the paper &ldquo;Hill
 * Kinetics meets P Systems: A Case Study on Gene Regulatory Networks as
 * Computing Agents in silico and in vivo&rdquo; of Hinze, T.; Hayat, S.;
 * Lenser, T.; Matsumaru, N., and Dittrich, P., 2007.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:supper@genomatix.de">Jochen Supper</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Aug 7, 2007
 */
public class HillEquation extends BasicKineticLaw implements
		InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
		InterfaceIrreversibleKinetics, InterfaceReversibleKinetics {

	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public HillEquation(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

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
			throws RateLawNotApplicableException {
		if (!modActi.isEmpty())
			modTActi.addAll(modActi);
		/*
		 * throw new ModificationException("Wrong activation in reaction " +
		 * model.getReaction(reactionNum).getId() + ". Only transcriptional or
		 * translational activation, " + "respectively, is allowed here.");
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

		// for (ModifierSpeciesReference modifier :
		// reaction.getListOfModifiers()) {
		// if (SBO.isGeneOrGeneCodingRegion(reaction.getReactant(0)
		// .getSpeciesInstance().getSBOTerm())
		// && (SBO.isTranslationalActivation(modifier.getSBOTerm()) || SBO
		// .isTranslationalInhibitor(modifier.getSBOTerm())))
		// throw new ModificationException(
		// "Wrong activation in reaction "
		// + reaction.getId()
		// + ". Only transcriptional modification is allowed here.");
		// else if (SBO.isRNAOrMessengerRNA(reaction.getReactant(0)
		// .getSpeciesInstance().getSBOTerm())
		// && (SBO.isTranscriptionalActivation(modifier.getSBOTerm()) || SBO
		// .isTranscriptionalInhibitor(modifier.getSBOTerm())))
		// throw new ModificationException("Wrong activation in reaction "
		// + reaction.getId()
		// + ". Only translational modification is allowed here.");
		// }

		if (reaction.getNumModifiers() == 0)
			setSBOTerm(47);
		else {
			setSBOTerm(192);
			if (modTActi.size() == 1)
				setSBOTerm(195);
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
			if (!SBO.isGeneOrGeneCodingRegion(reactant.getSBOTerm())) {
				gene = speciesTerm(reactant);
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

		// KS: half saturation constant.
		int i;
		for (i = 0; i < modTActi.size(); i++)
		/*
		 * if (!model.getSpecies(modTActi.get(i)).getSpeciesAlias(0).getType()
		 * .toUpperCase().equals("GENE"))
		 */{
			Parameter p_hillcoeff = parameterHillCoefficient(modTActi.get(i));
			Parameter p_kS = parameterKS(
					getModel().getSpecies(modTActi.get(i)), modTActi.get(i));
			acti[i] = ASTNode.times(ASTNode.frac(ASTNode.pow(
					speciesTerm(modTActi.get(i)),
					new ASTNode(p_hillcoeff, this)), ASTNode.sum(ASTNode.pow(
					speciesTerm(modTActi.get(i)),
					new ASTNode(p_hillcoeff, this)), ASTNode.pow(new ASTNode(
					p_kS, this), new ASTNode(p_hillcoeff, this)))));
		}
		for (i = 0; i < modTInhib.size(); i++)
		/*
		 * if (!model.getSpecies(modTInhib.get(i)).getSpeciesAlias(0)
		 * .getType().toUpperCase().equals("GENE"))
		 */{
			Parameter p_hillcoeff = parameterHillCoefficient(modTInhib.get(i));
			Parameter p_kS = parameterKS(getModel()
					.getSpecies(modTInhib.get(i)), modTInhib.get(i));
			inhib[i] = ASTNode.frac(ASTNode.pow(speciesTerm(modTInhib.get(i)),
					new ASTNode(p_hillcoeff, this)), ASTNode.sum(ASTNode.pow(
					speciesTerm(modTInhib.get(i)), new ASTNode(p_hillcoeff,
							this)), ASTNode.pow(this, p_kS, p_hillcoeff)));
		}
		Parameter p_kg = parameterVmax(true);

		ASTNode formelTxt = new ASTNode(p_kg, this);
		if (modTActi.size() > 0)
			formelTxt.multiplyWith(acti);
		if (modTInhib.size() > 0)
			formelTxt.multiplyWith(ASTNode.diff(new ASTNode(1, this), ASTNode
					.times(inhib)));
		return formelTxt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
	 */
	// @Override
	public String getSimpleName() {
		return "Hill equation";
	}
}
