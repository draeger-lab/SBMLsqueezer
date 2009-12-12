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
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;

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
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public HillEquation(PluginReaction parentReaction, PluginModel model)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model);
	}

	/**
	 * TODO Add comment
	 * 
	 * @param mod
	 * @param reactionNum
	 * @param modActi
	 * @param modInhib
	 * @param reversibility
	 * @param modTActi
	 * @param modTInhib
	 * @throws IOException
	 * @throws IllegalFormatException
	 * @throws ModificationException
	 */
	public HillEquation(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO: add Hillequation if reactiontype is translation or
		// transcription
		if (reaction.getReactionType().equals("TRANSLATION")
				|| reaction.getReactionType().equals("TRANSCRIPTION"))
			return true;
		return false;
	}

	public String getName() {
		String name;
		if (getParentReaction().getNumModifiers() > 0)
			name = "Hill equation, microscopic form";
		else
			name = "mass action rate law for zeroth order irreversible reactions, continuous scheme";
		return name;
	}

	// @Override
	public String getSBO() {
		String name = getName().toLowerCase(), sbo = "none";
		if (name.equals("hill equation"))
			sbo = "0000192";
		else if (name.equals("hill equation, microscopic form"))
			sbo = "0000195";
		else if (name.equals("hill equation, reduced form"))
			sbo = "0000198";
		else if (name
				.equals("mass action rate law for zeroth order irreversible reactions, continuous scheme"))
			sbo = "0000047";
		return sbo;
	}

	protected StringBuffer createKineticEquation(PluginModel model,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
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

		PluginReaction reaction = getParentReaction();
		for (int modifier = 0; modifier < reaction.getNumModifiers(); modifier++) {
			String name[] = reaction.getModifier(modifier)
					.getModificationType().split("_");
			if (1 < name.length) {
				String modificationType = name[1].toLowerCase();
				if (reaction.getReactant(0).getSpeciesInstance()
						.getSpeciesAlias(0).getType().toUpperCase().equals(
								"GENE")
						&& reaction.getModifier(modifier).getModificationType()
								.toUpperCase().startsWith("TRANSLATIONAL"))
					throw new ModificationException(
							"Wrong activation in reaction " + reaction.getId()
									+ ". Only transcriptional "
									+ modificationType + "is allowed here.");
				else if (reaction.getReactant(0).getSpeciesInstance()
						.getSpeciesAlias(0).getType().toUpperCase().contains(
								"RNA")
						&& reaction.getModifier(modifier).getModificationType()
								.toUpperCase().startsWith("TRANSCRIPTIONAL"))
					throw new ModificationException(
							"Wrong activation in reaction " + reaction.getId()
									+ ". Only translational "
									+ modificationType + " is allowed here.");
			}
		}

		if (reaction.getReactionType().equals("TRANSLATION")
				|| reaction.getReactionType().equals("TRANSCRIPTION"))
			for (int i = 0; i < reaction.getNumReactants(); i++)
				modTActi.add(reaction.getReactant(i).getSpecies());
		StringBuffer formula = createHillEquation(modTActi, modTInhib);
		// Influence of the concentrations of the reactants:
		for (int reactantNum = 0; reactantNum < reaction.getNumReactants(); reactantNum++) {
			PluginSpecies reactant = reaction.getReactant(reactantNum)
					.getSpeciesInstance();
			StringBuffer gene = new StringBuffer();
			if (!reactant.getSpeciesAlias(0).getType().toUpperCase().equals(
					"GENE")) {
				gene = new StringBuffer(reactant.getId());
				if (reaction.getReactant(reactantNum).getStoichiometry() != 1f) {
					gene = pow(gene, concat(reaction.getReactant(reactantNum)
							.getStoichiometry()));
					formula = times(formula, gene);
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
	private StringBuffer createHillEquation(List<String> modTActi,
			List<String> modTInhib) {
		StringBuffer acti = new StringBuffer();
		StringBuffer inhib = new StringBuffer();
		String rId = getParentReaction().getId();

		// KS: half saturation constant.
		int i;
		for (i = 0; i < modTActi.size(); i++)
		/*
		 * if (!model.getSpecies(modTActi.get(i)).getSpeciesAlias(0).getType()
		 * .toUpperCase().equals("GENE"))
		 */{
			StringBuffer kS = concat("kSp_", rId, underscore, modTActi.get(i));
			StringBuffer hillcoeff = concat("np_", rId, underscore, modTActi
					.get(i));
			acti = times(acti, frac(pow(modTActi.get(i), hillcoeff), sum(pow(
					modTActi.get(i), hillcoeff), pow(kS, hillcoeff))));
			addLocalParameter(hillcoeff);
			addLocalParameter(kS);
		}
		for (i = 0; i < modTInhib.size(); i++)
		/*
		 * if (!model.getSpecies(modTInhib.get(i)).getSpeciesAlias(0)
		 * .getType().toUpperCase().equals("GENE"))
		 */{
			StringBuffer kS = concat("kSm_", rId, underscore, modTInhib.get(i));
			StringBuffer hillcoeff = concat("nm_", rId, underscore, modTInhib
					.get(i));
			inhib = times(inhib, diff(Integer.toString(1), frac(pow(
					new StringBuffer(modTInhib.get(i)), hillcoeff), sum(pow(
					new StringBuffer(modTInhib.get(i)), hillcoeff), pow(kS,
					hillcoeff)))));
			addLocalParameter(hillcoeff);
			addLocalParameter(kS);
		}
		StringBuffer kg = concat("kg_", rId);
		addLocalParameter(kg);

		StringBuffer formelTxt = new StringBuffer(kg);
		if ((acti.length() > 0) && (inhib.length() > 0))
			formelTxt = times(formelTxt, acti, inhib);
		else if (acti.length() > 0)
			formelTxt = times(formelTxt, acti);
		else if (inhib.length() > 0)
			formelTxt = times(formelTxt, inhib);
		return formelTxt;
	}
}
