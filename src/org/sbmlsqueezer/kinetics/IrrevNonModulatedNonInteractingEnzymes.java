/*
 * Feb 6, 2008 Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 */
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;


import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

/**
 * This class implements SBO:0000150 and all of its special cases. It is an
 * kinetic law of an irreversible enzyme reaction, which is not modulated, and
 * in which all reacting species do not interact: Kinetics of enzymes that react
 * with one or several substances, their substrates, that bind independently.
 * The enzymes do not catalyse the reactions in both directions.
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @date Feb 6, 2008
 */
public class IrrevNonModulatedNonInteractingEnzymes extends BasicKineticLaw {

	private int numOfEnzymes;

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException 
	 * @throws IllegalFormatException 
	 */
	public IrrevNonModulatedNonInteractingEnzymes(
			PluginReaction parentReaction, PluginModel model)
			throws RateLawNotApplicableException, IOException, IllegalFormatException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 * @throws IOException 
	 * @throws IllegalFormatException 
	 */
	public IrrevNonModulatedNonInteractingEnzymes(
			PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException, IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#getName()
	 */
	@Override
	public String getName() {
		double stoichiometry = 0;
		for (int i = 0; i < getParentReaction().getNumReactants(); i++)
			stoichiometry += getParentReaction().getReactant(i)
					.getStoichiometry();
		switch ((int) Math.round(stoichiometry)) {
		case 1:
			if (numOfEnzymes == 0)
				return "normalised kinetics of unireactant enzymes";
			return "Henri-Michaelis Menten equation";
		case 2:
			return "kinetics of irreversible non-modulated non-interacting bireactant enzymes";
		case 3:
			return "kinetics of irreversible non-modulated non-interacting trireactant enzymes";
		default:
			return "kinetics of irreversible non-modulated non-interacting reactant enzymes";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#getSBO()
	 */
	@Override
	public String getSBO() {
		String name = getName().toLowerCase(), sbo = "none";
		if (name
				.equals("kinetics of irreversible non-modulated non-interacting reactant enzymes"))
			sbo = "0000150";
		else if (name
				.equals("kinetics of irreversible non-modulated non-interacting bireactant enzymes"))
			sbo = "0000151";
		else if (name
				.equals("kinetics of irreversible non-modulated non-interacting trireactant enzymes"))
			sbo = "0000152";
		else if (name.equals("normalised kinetics of unireactant enzymes"))
			sbo = "0000199";
		else if (name.equals("henri-michaelis menten equation"))
			sbo = "0000029";
		return sbo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbmlsqueezer.kinetics.BasicKineticLaw#createKineticEquation(jp.sbi
	 * .celldesigner.plugin.PluginModel, int, java.util.List, java.util.List,
	 * java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	@Override
	protected StringBuffer createKineticEquation(PluginModel model, 
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		if ((modActi.size() > 0) || (modInhib.size() > 0)
				|| (modTActi.size() > 0) || (modTInhib.size() > 0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to non-modulated reactions.");
		if ((modCat.size() > 0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to enzyme-catalyzed reactions.");
		if (getParentReaction().getReversible())
			getParentReaction().setReversible(false);

		numOfEnzymes = modE.size();
	
		PluginReaction reaction = getParentReaction();
			
		int enzymeNum = 0;
		StringBuffer numerator=new StringBuffer();
		StringBuffer denominator=new StringBuffer();
		
		do {
			StringBuffer kcat=new StringBuffer();
			if (modE.size() == 0) {
				kcat = concat("V_" ,reaction.getId());
				} else {
					kcat = concat("kcat_", reaction.getId());
				if (modE.size() > 1) {
					kcat = concat(kcat, Character.valueOf('_') , modE.get(enzymeNum));
					}
			}
			if (!listOfLocalParameters.contains(kcat))
				listOfLocalParameters.add(new StringBuffer(kcat));

			if (modE.size() >= 1) {
			
				numerator=times(numerator,new StringBuffer(modE.get(enzymeNum)));
			}
		
			numerator=times(kcat, numerator);
			//numerator=times(numerator,currEnzyme);
			
			for (int i = 0; i < reaction.getNumReactants(); i++) {
				PluginSpeciesReference si = reaction.getReactant(i);
				if (((int) si.getStoichiometry()) - si.getStoichiometry() != 0)
					throw new RateLawNotApplicableException(
							"This rate law can only be applied if all reactants have integer stoichiometries.");
				StringBuffer kM = new StringBuffer();
					
				kM=concat("kM_" , reaction.getId());
				
				if (modE.size() > 1) {
					kM=concat(kM , Character.valueOf('_') , modE.get(enzymeNum));
					}
				kM=concat(kM , Character.valueOf('_') , si.getSpecies());
				if (!listOfLocalParameters.contains(kM))
					listOfLocalParameters.add(new StringBuffer(kM));

				StringBuffer frac= new StringBuffer();
				if (si.getStoichiometry() != 1) {
				
					frac=pow(frac(new StringBuffer(si.getSpecies()),kM), new StringBuffer((int) si.getStoichiometry()));
					
					numerator=times(numerator, pow(frac(new StringBuffer(si.getSpecies()),kM), new StringBuffer((int) si.getStoichiometry())));
				} else{
					
				frac=frac(new StringBuffer(si.getSpecies()),kM);
					
				numerator=times(numerator, frac(new StringBuffer(si.getSpecies()),kM));
				}
				if (reaction.getNumReactants() > 1) {				
					if (si.getStoichiometry() != 1) 					
						denominator=times(denominator,pow(sum(new StringBuffer('1'), frac),new StringBuffer((int) si.getStoichiometry())));
					else						
						denominator=times(denominator,sum(new StringBuffer('1'), frac));
					
				} else {
					if (si.getStoichiometry() != 1) 						
					 denominator=times(denominator,pow(sum(new StringBuffer('1'), frac),new StringBuffer((int) si.getStoichiometry())));
					 else 						
						denominator=times(denominator,sum(new StringBuffer('1'), frac));
				}
			}
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);
		return frac(numerator, denominator);
	}

}
