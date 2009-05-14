/**
 * Aug 7, 2007
 *
 * @since 2.0
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 */
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;

/**
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Jochen Supper
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.5.0
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
			throws RateLawNotApplicableException, IOException, IllegalFormatException {
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
			throws RateLawNotApplicableException, IOException, IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	@Override
	public String getName() {
		String name;
		if (getParentReaction().getNumModifiers() > 0)
			name = "Hill equation, microscopic form";
		else
			name = "zeroth order irreversible mass action kinetics, continuous scheme";
		return name;
	}

	@Override
	public String getSBO() {
		String name = getName().toLowerCase(), sbo = "none";
		if (name.equals("hill equation"))
			sbo = "0000192";
		else if (name.equals("hill equation, microscopic form"))
			sbo = "0000195";
		else if (name.equals("hill equation, reduced form"))
			sbo = "0000198";
		else if (name
				.equals("zeroth order irreversible mass action kinetics, continuous scheme"))
			sbo = "0000047";
		return sbo;
	}

	@Override
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
		// necessary due to the changes in CellDesigner from version 4.0 alpha to beta and 4.0.1
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

		StringBuffer acti = new StringBuffer();
		StringBuffer inhib = new StringBuffer();
		
		// KS: half saturation constant.
		for (int activatorNum = 0; activatorNum < modTActi.size(); activatorNum++) {
			StringBuffer kS = concat("kSp_" , reaction.getId() , "_" , modTActi.get(activatorNum));
			StringBuffer hillcoeff =concat( "np_"
					, reaction.getId() , "_" , modTActi.get(activatorNum));
			acti = times(acti, frac(pow(new StringBuffer(modTActi.get(activatorNum)) , hillcoeff), sum(pow(new StringBuffer(modTActi.get(activatorNum)),hillcoeff), pow(kS, hillcoeff)))
);
			
			if (!listOfLocalParameters.contains(hillcoeff))
				listOfLocalParameters.add(hillcoeff);
			if (!listOfLocalParameters.contains(kS))
				listOfLocalParameters.add(kS);

				}
		if (acti.length() > 2) {
			acti = new StringBuffer(acti.substring(3));
		}

		for (int inhibitorNum = 0; inhibitorNum < modTInhib.size(); inhibitorNum++) {
			StringBuffer kS = concat("kSm_" , reaction.getId() ,"_", modTInhib.get(inhibitorNum));
			StringBuffer hillcoeff = concat("nm_", reaction.getId(), "_", modTInhib.get(inhibitorNum));
			
			inhib = times(inhib, frac(diff(concat(1),pow(new StringBuffer(modTActi.get(inhibitorNum)) , hillcoeff)), sum(pow(new StringBuffer(modTActi.get(inhibitorNum)),hillcoeff), pow(kS, hillcoeff)))
			);
			if (!listOfLocalParameters.contains(hillcoeff))
				listOfLocalParameters.add(hillcoeff);
			if (!listOfLocalParameters.contains(kS))
				listOfLocalParameters.add(kS);

				}
		if (inhib.length() > 2) {
			// cut the multiplication symbol at the beginning.
			inhib = new StringBuffer(inhib.substring(3));
		}

		
		StringBuffer formelTxt = concat("kg_",reaction.getId());
		if (!listOfLocalParameters.contains(formelTxt))
			listOfLocalParameters.add(formelTxt);
		if ((acti.length() > 0) && (inhib.length() > 0)) {
			
			formelTxt =times(formelTxt, acti, inhib);
		} else if (acti.length() > 0) {
			formelTxt =times(formelTxt, acti);
		} else if (inhib.length() > 0) {
			
			formelTxt =times(formelTxt, inhib);
		}

		// Influence of the concentrations of the educts:
		for (int reactantNum = 0; reactantNum < reaction.getNumReactants(); reactantNum++) {
			PluginSpecies reactant = reaction.getReactant(reactantNum)
					.getSpeciesInstance();
			StringBuffer gene=new StringBuffer();
			if (!reactant.getSpeciesAlias(0).getType().toUpperCase().equals(
					"GENE")) {
				
				gene = new StringBuffer(reactant.getId());
				if (reaction.getReactant(reactantNum).getStoichiometry() != 1.0) {
					gene=pow(gene, concat(reaction.getReactant(reactantNum)
							.getStoichiometry()));
					
				}
										
			}
			formelTxt =times(formelTxt, gene);
		}
		return formelTxt;
	}

}
