/**
 * 
 */
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;


/**
 * @author andreas
 * 
 */
public class IrrevCompetNonCooperativeEnzymes extends BasicKineticLaw {

	private int numInhib;
	private int numOfEnzymes;

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException 
	 * @throws IllegalFormatException 
	 */
	public IrrevCompetNonCooperativeEnzymes(PluginReaction parentReaction,
			PluginModel model) throws RateLawNotApplicableException, IOException, IllegalFormatException {
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
	public IrrevCompetNonCooperativeEnzymes(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
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
		switch (numInhib) {
		case 0:
			if (numOfEnzymes == 0)
				return "normalised kinetics of unireactant enzymes";
			return "Henri-Michaelis Menten equation";
		case 1:
			return "competitive inhibition of irreversible unireactant enzymes by one inhibitor";
		default:
			return "competitive inhibition of irreversible unireactant enzymes by non-exclusive non-cooperative inhibitors";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#getSBO()
	 */
	@Override
	public String getSBO() {
		String name = getName().toLowerCase();
		if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by non-exclusive non-cooperative inhibitors"))
			return "0000273";
		if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by one inhibitor"))
			return "0000267";
		if (name.equals("henri-michaelis menten equation"))
			return "0000029";
		return "none";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#createKineticEquation(jp.sbi.celldesigner.plugin.PluginModel,
	 *      int, java.util.List, java.util.List, java.util.List, java.util.List,
	 *      java.util.List, java.util.List)
	 */
	@Override
	protected StringBuffer createKineticEquation(PluginModel model, 
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		if (((modTActi.size() > 0) && (modActi.size() > 0))
				|| ((modInhib.size() > 0) && (modTInhib.size() > 0)))
			throw new RateLawNotApplicableException(
					"Mixture of translational/transcriptional and regular activation/inhibition is not allowed.");
		if ((modCat.size() > 0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to enzyme-catalyzed reactions.");
		PluginReaction reaction = getParentReaction();
		if ((reaction.getNumReactants() > 1)
				|| (reaction.getReactant(0).getStoichiometry() != 1.0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to reactions with exactly one substrate.");
		if (modTActi.size() > 0)
			modActi = modTActi;
		if (modTInhib.size() > 0)
			modInhib = modTInhib;
		if (reaction.getReversible())
			reaction.setReversible(false);
		numInhib = modInhib.size();

		numOfEnzymes = modE.size();		
		StringBuffer formula = new StringBuffer();

		int enzymeNum = 0;
		do {
			StringBuffer kcat=new StringBuffer();
			if (numOfEnzymes == 0)
				kcat = concat("V_", reaction.getId());
			else {
				kcat = concat("kcat_" , reaction.getId());
				if (modE.size() > 1)
					kcat = concat(kcat , Character.valueOf('_') , modE.get(enzymeNum));
			}
			addLocalParameter(kcat);
			addLocalParameter(kcat);
			StringBuffer currEnzyme=new StringBuffer();
			StringBuffer numerator = new StringBuffer();
			

			if (numOfEnzymes <= 1)
				formula = currEnzyme;
			else {
				
				formula=sum(formula,currEnzyme);
				numerator=times(numerator,new StringBuffer(modE.get(enzymeNum)));
			}
			
			numerator=times(numerator,kcat);			
			numerator=times(numerator,new StringBuffer(reaction.getReactant(0).getSpecies()));

			StringBuffer denominator = new StringBuffer();
			StringBuffer kM = new StringBuffer(concat("kM_", reaction.getId()));
			
			if (numOfEnzymes > 1)
				
			kM = concat(kM ,Character.valueOf('_') , modE.get(enzymeNum));
			kM = concat(kM ,Character.valueOf('_') , reaction.getReactant(0).getSpecies());
			addLocalParameter(kM);
			
			if (modInhib.size() == 0)
				denominator=sum(denominator,kM);
			else {
				StringBuffer factor = new StringBuffer();
				factor =times(factor,kM);
				for (int i = 0; i < modInhib.size(); i++) {
										
					StringBuffer kIi = new StringBuffer(concat("Ki_" ,reaction.getId()));
					StringBuffer exponent =new StringBuffer(concat("m_", reaction.getId()));
					if (numOfEnzymes > 1) {
						kIi =concat(Character.valueOf('_') ,modE.get(enzymeNum));
						exponent = concat(Character.valueOf('_') , modE.get(enzymeNum));
					}
					kIi =concat(kIi, Character.valueOf('_') , modInhib.get(i));
					exponent =concat(exponent, Character.valueOf('_') , modInhib.get(i));
					addLocalParameter(kIi);
					addLocalParameter(exponent);
				
					factor =times(factor,pow(sum(new StringBuffer("1"),frac(new StringBuffer(modInhib.get(i)),kIi)), exponent));
				}
				denominator=sum(denominator,factor);
				
			}
			denominator=sum(denominator,new StringBuffer(reaction.getReactant(0).getSpecies()));		
			currEnzyme=frac(numerator,denominator);
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);

		/*
		 * Activation
		 */
		if (modActi.size() > 0) {
			int actiNum = 0;
			StringBuffer activation = new StringBuffer();
			
			do {
			    StringBuffer kAi = new StringBuffer(concat( "KA_" , reaction.getId() , Character.valueOf('_') , modActi.get(actiNum)));
				
				addLocalParameter(kAi);
							
				if (modActi.size() > 1) 					
						activation = times(activation, frac(new StringBuffer(modActi.get(actiNum)), sum(kAi,new StringBuffer(modActi.get(actiNum++)))));
				 else
					activation = frac(new StringBuffer(modActi.get(actiNum)), sum(kAi,new StringBuffer(modActi.get(actiNum++))));
			
			} while (actiNum <= modActi.size() - 1);
						
			formula =times(activation,formula);
		}
		return formula;
	}

}
