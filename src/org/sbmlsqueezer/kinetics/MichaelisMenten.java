/*
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler: JDK 1.6.0
 */
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

/**
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @date Aug 1, 2007
 */
public class MichaelisMenten extends BasicKineticLaw {

	private int numOfInhibitors;

	private int numOfActivators;

	private int numOfEnzymes;

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException 
	 * @throws IllegalFormatException 
	 */
	public MichaelisMenten(PluginReaction parentReaction, PluginModel model)
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
	public MichaelisMenten(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException, IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

//	@Override
	@Override
	public String getName() {
		switch (numOfEnzymes) {
		case 0: // no enzyme, irreversible
			if (!getParentReaction().getReversible() && (numOfActivators == 0)
					&& (numOfInhibitors == 0))
				return "normalised kinetics of unireactant enzymes"; // 0000199
			else if ((numOfActivators == 0) && (numOfInhibitors == 0))
				return "kinetics of non-modulated unireactant enzymes"; // 0000326
			break;
		case 1: // one enzyme
			if (getParentReaction().getReversible()) {
				if ((numOfActivators == 0) && (numOfInhibitors == 0))
					return "kinetics of non-modulated unireactant enzymes"; // 0000199
			} else if ((numOfActivators == 0) && (numOfInhibitors == 0)) // irreversible
				// equivalents: Briggs-Haldane equation or Van
				// Slyke-Cullen
				// equation
				return "Henri-Michaelis Menten equation"; // 0000029
			break;
		}
		if (!getParentReaction().getReversible())
			switch (numOfInhibitors) {
			case 1:
				return "simple mixed-type inhibition of irreversible unireactant enzymes"; // 0000265
			case 2:
				return "mixed-type inhibition of irreversible unireactant enzymes by two inhibitors"; // 0000276
			default:
				return "mixed-type inhibition of irreversible enzymes by mutually exclusive inhibitors"; // 0000275
			}
		return "kinetics of unireactant enzymes"; // 0000269
	}

//	@Override
	@Override
	public String getSBO() {
		String name = getName(), sbo = "none";
		if (name.equalsIgnoreCase("normalised kinetics of unireactant enzymes"))
			sbo = "0000199";
		else if (name
				.equalsIgnoreCase("kinetics of non-modulated unireactant enzymes"))
			sbo = "0000326";
		else if (name.equalsIgnoreCase("Briggs-Haldane equation"))
			sbo = "0000031";
		else if (name
				.equalsIgnoreCase("kinetics of irreversible non-modulated unireactant enzymes"))
			sbo = "0000028";
		else if (name
				.equalsIgnoreCase("simple mixed-type inhibition of irreversible unireactant enzymes"))
			sbo = "0000265";
		else if (name.equalsIgnoreCase("kinetics of unireactant enzymes"))
			sbo = "0000269";
		else if (name.equalsIgnoreCase("Henri-Michaelis Menten equation"))
			sbo = "0000029";
		else if (name.equalsIgnoreCase("Van Slyke-Cullen equation"))
			sbo = "0000030";
		else if (name
				.equalsIgnoreCase("simple uncompetitive inhibition of irreversible unireactant enzymes"))
			sbo = "0000262";
		else if (name
				.equalsIgnoreCase("mixed-type inhibition of irreversible enzymes by mutually exclusive inhibitors"))
			sbo = "0000275";
		else if (name
				.equalsIgnoreCase("simple non-competitive inhibition of unireactant enzymes"))
			sbo = "0000266";
		else if (name
				.equalsIgnoreCase("mixed-type inhibition of irreversible unireactant enzymes by two inhibitors"))
			sbo = "0000276";
		else if (name
				.equalsIgnoreCase("mixed-type inhibition of unireactactant enzymes by two inhibitors"))
			sbo = "0000277";
		else if (name
				.equalsIgnoreCase("simple competitive inhibition of irreversible unireactant enzymes by two non-exclusive inhibitors"))
			sbo = "0000274";
		else if (name
				.equalsIgnoreCase("competitive inhibition of irreversible unireactant enzymes by two exclusive inhibitors"))
			sbo = "0000271";
		else if (name
				.equalsIgnoreCase("competitive inhibition of irreversible unireactant enzymes by exclusive inhibitors"))
			sbo = "0000270";
		else if (name
				.equalsIgnoreCase("simple competitive inhibition of irreversible unireactant enzymes by one inhibitor"))
			sbo = "0000260";

		return sbo;
	}

	@Override
	protected StringBuffer createKineticEquation(PluginModel model, 
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		StringBuffer numerator = new StringBuffer();// I
		StringBuffer denominator = new StringBuffer(); // II
		StringBuffer formelTxt = new StringBuffer();
		numOfActivators = modActi.size();
		numOfEnzymes = modE.size();
		numOfInhibitors = modInhib.size();
		
		PluginReaction reaction = getParentReaction();
		if ((reaction.getNumReactants() > 1)
				|| (reaction.getReactant(0).getStoichiometry() != 1.0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to reactions with exactly one reactant.");
		if (((reaction.getNumProducts() > 1) || (reaction.getProduct(0)
				.getStoichiometry() != 1.0))
				&& reaction.getReversible())
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to reactions with exactly one product.");

		PluginSpeciesReference specRefR = reaction.getReactant(0);
		PluginSpeciesReference specRefP = reaction.getProduct(0);
		int enzymeNum = 0;
		do {
			StringBuffer kcatp= new StringBuffer();
			StringBuffer kcatn=new StringBuffer();
			StringBuffer kMe = concat("kM_" , reaction.getId()),
			kMp = kMe;
			
			if (modE.size() == 0) {
				kcatp = concat("Vp_" ,reaction.getId());
				kcatn = concat("Vn_" , reaction.getId());
				} else {
				kcatp =  concat("kcatp_" , reaction.getId());
				kcatn =  concat("kcatn_" , reaction.getId());
				if (modE.size() > 1) {
					kcatp= concat(kcatp ,'_' , modE.get(enzymeNum));
					kcatn= concat(kcatn ,'_' , modE.get(enzymeNum));
					kMe = concat(kMe ,'_' , modE.get(enzymeNum));
					kMp= concat(kMp ,'_' , modE.get(enzymeNum));
					}
				
			}
			kMe=concat( kMe,'_' , specRefR.getSpecies());
			
			if (!listOfLocalParameters.contains(kcatp))
				listOfLocalParameters.add(new StringBuffer(kcatp));
			if (!listOfLocalParameters.contains(kMe))
				listOfLocalParameters.add(new StringBuffer(kMe));
			StringBuffer currEnzyme = new StringBuffer();
			StringBuffer kMeN = new StringBuffer();
			kMeN=kMe;
			/*
			 * Irreversible Reaction
			 */
			if (!reaction.getReversible()) {
				numerator = times(kcatp , new StringBuffer(specRefR.getSpecies()));
				
				denominator = new StringBuffer(specRefR.getSpecies());
				
				/*
				 * Reversible Reaction
				 */
			} else {
			
				numerator = times(frac (kcatp , kMe), new StringBuffer(specRefR.getSpecies()));
				
				denominator = frac( new StringBuffer(specRefR.getSpecies()) , kMe);
				
				kMp = concat(kMp,Character.valueOf('_'), specRefP.getSpecies());
				
				if (!listOfLocalParameters.contains(kcatn))
					listOfLocalParameters.add(kcatn);
				if (!listOfLocalParameters.contains(kMp))
					listOfLocalParameters.add(kMp);
		 
				numerator =diff(numerator, times(frac( kcatn,kMp), new StringBuffer(specRefP.getSpecies())));
				denominator =sum(denominator, frac(new StringBuffer(specRefP.getSpecies()), kMp));
				}

			/*
			 * Inhibition
			 */
			if (modInhib.size() == 1) {
				StringBuffer kIa, kIb;

				kIa = concat("KIa_", reaction.getId());
				kIb = concat("KIb_" , reaction.getId());
				if (!listOfLocalParameters.contains(kIa))
					listOfLocalParameters.add(kIa);
				if (!listOfLocalParameters.contains(kIb))
					listOfLocalParameters.add(kIb);
				StringBuffer inh = new StringBuffer();
		
				inh = sum(new StringBuffer(1), frac(new StringBuffer(modInhib.get(0)), kIb ));
				kIb = sum(new StringBuffer(1), frac(new StringBuffer(modInhib.get(0)), kIb ));
				if (reaction.getReversible()) {
					
					denominator= sum(frac(new StringBuffer(modInhib.get(0)), kIa), times(denominator, inh));
					//denominator=frac(new StringBuffer(modInhib.get(0)),sum(kIa, times(denominator, kIb)));
					} else {
				
					denominator=sum(frac(times(kMe,new StringBuffer(modInhib.get(0))),kIa),times(denominator, inh));
					//denominator = frac(times(kMe, new StringBuffer(modInhib.get(0))) ,sum(kIa ,times(denominator, kIb)));
					}

			} else if ((modInhib.size() > 1)
					&& !getParentReaction().getReversible()) {
				// mixed-type inihibition of irreversible enzymes by mutually
				// exclusive
				// inhibitors.

				StringBuffer denominator_mod= new StringBuffer(1);
				
			
				// kMe += " * (1 + "; // Km
				for (int i = 0; i < modInhib.size(); i++) {
					StringBuffer kIai = concat((i + 1), Character.valueOf('_'), reaction.getId());
						StringBuffer inhib = new StringBuffer(modInhib.get(i));
					if (modE.size() > 1) {
						kIai = concat(kIai, Character.valueOf('_'), modE.get(enzymeNum));
						}
						StringBuffer kIbi = concat("kIb", kIai);
					kIai = concat("kIa" , kIai);
					if (!listOfLocalParameters.contains(kIai))
						listOfLocalParameters.add(new StringBuffer(kIai));
					if (!listOfLocalParameters.contains(kIbi))
						listOfLocalParameters.add(new StringBuffer(kIbi));

					denominator = sum(new StringBuffer(1), frac(inhib, kIai));
					denominator_mod = sum(denominator_mod, frac(new StringBuffer(inhib), new StringBuffer(kIai)));
					
					
					kMeN=sum(new StringBuffer(1),frac(inhib,kIbi));
					// kMe += inhib + '/' + kIbi; // Km
				}
				
				denominator=times(denominator, denominator_mod); // substrate
			
				
				kMeN=times(kMe,kMeN);

			} else if (modInhib.size() > 1) {
				// the formalism from the convenience kinetics as a default.
				StringBuffer inhib = new StringBuffer();
				StringBuffer inh =  new StringBuffer();
				for (int inhibitorNum = 0; inhibitorNum < modInhib.size(); inhibitorNum++) {
					StringBuffer kI = concat("kI_" , reaction.getId());
					kI = concat(kI, Character.valueOf('_') , modInhib.get(inhibitorNum));
					if (!listOfLocalParameters.contains(kI))
					listOfLocalParameters.add(new StringBuffer(kI));
					inh=frac(kI,sum(kI, new StringBuffer(modInhib.get(inhibitorNum))));
					
					inhib = frac(new StringBuffer(kI),sum(new StringBuffer(kI), new StringBuffer(modInhib.get(inhibitorNum))));
						}
				    currEnzyme=inh;
				    formelTxt =inhib;
				}

			if (reaction.getReversible()) {
				
				denominator = sum(new StringBuffer(1) ,denominator);
				} else {
			
				denominator = sum(kMeN ,denominator);
			}

			// construct formula
			currEnzyme = frac(numerator, denominator);

			formelTxt= times(formelTxt, frac(numerator, denominator));
			if (modE.size() > 0) {
				// TODO - ERROR
				currEnzyme=times(new StringBuffer(modE.get(enzymeNum)), currEnzyme);
				formelTxt = sum(new StringBuffer(modE.get(enzymeNum)),formelTxt);
					
			}
			
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);
		/*
		 * Activation
		 */
	
		for (int i = 0; i < modActi.size(); i++) {

			StringBuffer kAa; 

			kAa = concat("kA_",reaction.getId());
			
			if (!listOfLocalParameters.contains(kAa))
				listOfLocalParameters.add(kAa);

			formelTxt =times(frac(new StringBuffer(modActi.get(i)),sum( kAa, new StringBuffer(modActi.get(i)))), formelTxt);
			}
	

		
		return formelTxt;
	}
}
