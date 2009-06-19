package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;

/**
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Aug 1, 2007
 */
public class MichaelisMenten extends GeneralizedMassAction {

	private int numOfInhibitors;

	private int numOfActivators;

	private int numOfEnzymes;

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 * @throws IOException
	 */
	public MichaelisMenten(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 * @throws IOException
	 */
	public MichaelisMenten(PluginReaction parentReaction, PluginModel model)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model);
	}

	protected StringBuffer createKineticEquation(PluginModel model,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		
//		PluginReaction reaction = getParentReaction();
//				
//		StringBuffer acti = createActivationFactor(modActi);
//		StringBuffer inhib= createInhibitionFactor(modInhib);
//		
//		StringBuffer numerator = new StringBuffer();
//		StringBuffer denominator = new StringBuffer();
//		
//		StringBuffer kcatp= new StringBuffer("kcatp_"+reaction.getId());
//		StringBuffer kcatn= new StringBuffer("kcatn"+reaction.getId());
//		StringBuffer kMr = concat("kM_", reaction.getId());
//		StringBuffer kMp = concat("kM_", reaction.getId());
//		
//		//reversible
//		if(reaction.getReversible()){
//			if ((reaction.getNumReactants() > 1)
//					|| (reaction.getReactant(0).getStoichiometry() != 1.0))
//				throw new RateLawNotApplicableException(
//						"This rate law can only be applied to reactions with exactly one reactant.");
//			if (((reaction.getNumProducts() > 1) || (reaction.getProduct(0)
//					.getStoichiometry() != 1.0))
//					&& reaction.getReversible())
//				throw new RateLawNotApplicableException(
//						"This rate law can only be applied to reactions with exactly one product.");
//			
//			
//			
//			
//			if (modE.size()+modCat.size()<1){
//				
//			numerator = diff(times(frac(kcatp,concat(kMr,'_',getSpecies(reaction.getReactant(0)))),
//					new StringBuffer(getSpecies(reaction.getReactant(0)))),19292);
//				
//				
//			}else
//			{
//				
//			}
//			
//			
//		}
//		
//		//irreversible
//		else{
//			if ((reaction.getNumReactants() > 1)
//					|| (reaction.getReactant(0).getStoichiometry() != 1.0))
//				throw new RateLawNotApplicableException(
//						"This rate law can only be applied to reactions with exactly one reactant.");
//			
//			
//		}
//		return null;
		return alt(model, modE, modActi, modTActi, modInhib, modTInhib, modCat);
	}

		
	public StringBuffer alt(PluginModel model,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		
		PluginReaction reaction = getParentReaction();
				
		StringBuffer numerator = new StringBuffer();
		StringBuffer denominator = new StringBuffer();
	
		numOfActivators = modActi.size();
		numOfEnzymes = modE.size();
		numOfInhibitors = modInhib.size();

		if ((reaction.getNumReactants() > 1)
				|| (reaction.getReactant(0).getStoichiometry() != 1d))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to reactions with exactly one reactant.");
		if (((reaction.getNumProducts() > 1) || (reaction.getProduct(0)
				.getStoichiometry() != 1.0))
				&& reaction.getReversible())
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to reactions with exactly one product.");

		StringBuffer specRefR = new StringBuffer(reaction.getReactant(0)
				.getSpecies());
		StringBuffer specRefP = new StringBuffer(reaction.getProduct(0)
				.getSpecies());

		StringBuffer formula = new StringBuffer();
		int enzymeNum = 0;
		do {
			StringBuffer kcatp, kcatn;
			StringBuffer kMr = concat("kM_", reaction.getId());
			StringBuffer kMp = concat("kM_", reaction.getId());

			if (modE.size() == 0) {
				kcatp = concat("Vp_", reaction.getId());
				kcatn = concat("Vn_", reaction.getId());
			} else {
				kcatp = concat("kcatp_", reaction.getId());
				kcatn = concat("kcatn_", reaction.getId());
				if (modE.size() > 1) {
					kcatp = concat(kcatp, underscore, modE.get(enzymeNum));
					kcatn = concat(kcatn, underscore, modE.get(enzymeNum));
					kMr = concat(kMr, underscore, modE.get(enzymeNum));
					kMp = concat(kMp, underscore, modE.get(enzymeNum));
				}
			}
			kMr = concat(kMr, underscore, specRefR);

			addLocalParameter(kcatp);
			addLocalParameter(kMr);

			StringBuffer currEnzymeKin = new StringBuffer();

			if (!reaction.getReversible()) {
				/*
				 * Irreversible Reaction
				 */
				numerator = times(kcatp, specRefR);
				denominator = new StringBuffer(specRefR);
			} else {
				/*
				 * Reversible Reaction
				 */
				numerator = times(frac(kcatp, kMr), specRefR);
				denominator = frac(specRefR, kMr);
				kMp = concat(kMp, underscore, specRefP);

				addLocalParameter(kcatn);
				addLocalParameter(kMp);

				numerator = diff(numerator, times(frac(kcatn, kMp), specRefP));
				denominator = sum(denominator, frac(specRefP, kMp));
			}
			createInihibitionTerms(modInhib, reaction, modE, denominator, kMr,
					currEnzymeKin, enzymeNum);

			if (reaction.getReversible())
				denominator = sum(new StringBuffer("1"), denominator);
			else if ((modInhib.size() <= 1)
					|| getParentReaction().getReversible())
				denominator = sum(kMr, denominator);

			// construct formula
			currEnzymeKin = frac(numerator, denominator);
			if (modE.size() > 0)
				currEnzymeKin = times(new StringBuffer(modE.get(enzymeNum)),
						currEnzymeKin);
			formula = sum(formula, currEnzymeKin);
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);

		// the formalism from the convenience kinetics as a default.
		if ((modInhib.size() > 1) && (reaction.getReversible()))
			formula = times(createInhibitionFactor(modInhib), formula);
		// Activation
		if (modActi.size() > 0)
			formula = times(createActivationFactor(modActi), formula);
		return formula;
	}

	/**
	 * Inhibition
	 * 
	 * @param modInhib
	 * @param reaction
	 * @param modE
	 * @param denominator
	 * @param kMr
	 * @param currEnzymeKin
	 * @param enzymeNum
	 */
	private void createInihibitionTerms(List<String> modInhib,
			PluginReaction reaction, List<String> modE,
			StringBuffer denominator, StringBuffer kMr,
			StringBuffer currEnzymeKin, int enzymeNum) {
		if (modInhib.size() == 1) {
			StringBuffer kIa = concat("KIa_", reaction.getId()), kIb = concat(
					"KIb_", reaction.getId());

			if (modE.size() > 1) {
				kIa = concat(kIa, underscore + modE.get(enzymeNum));
				kIb = concat(kIb, underscore + modE.get(enzymeNum));
			}

			addLocalParameter(kIa);
			addLocalParameter(kIb);
			StringBuffer specRefI = new StringBuffer(modInhib.get(0));
			if (reaction.getReversible())
				denominator = sum(frac(specRefI, kIa), times(denominator, sum(
						new StringBuffer("1"), frac(specRefI, kIb))));
			else
				denominator = sum(times(frac(kMr, kIa), specRefI), denominator,
						times(frac(kMr, kIb), specRefI));

		} else if ((modInhib.size() > 1)
				&& !getParentReaction().getReversible()) {
			/*
			 * mixed-type inihibition of irreversible enzymes by mutually
			 * exclusive inhibitors.
			 */
			StringBuffer sumIa = new StringBuffer("1");
			StringBuffer sumIb = new StringBuffer("1");
			for (int i = 0; i < modInhib.size(); i++) {
				StringBuffer kIai = concat(Integer.valueOf(i + 1), underscore,
						reaction.getId());
				if (modE.size() > 1)
					kIai = concat(kIai, underscore, modE.get(enzymeNum));
				StringBuffer kIbi = concat("kIb_", kIai);
				kIai = concat("kIa_", kIai);
				addLocalParameter(kIai);
				addLocalParameter(kIbi);
				StringBuffer specRefI = new StringBuffer(modInhib.get(i));
				sumIa = sum(sumIa, frac(specRefI, kIai));
				sumIb = sum(sumIb, frac(specRefI, kIbi));
			}
			denominator = sum(times(denominator, sumIa), times(kMr, sumIb));
		}
	}

	public String getName() {
		switch (numOfEnzymes) {
		case 0: // no enzyme, irreversible
			if (!getParentReaction().getReversible() && (numOfActivators == 0)
					&& (numOfInhibitors == 0))
				return "normalised kinetics of unireactant enzymes"; // 0000199
			else if ((numOfActivators == 0) && (numOfInhibitors == 0))
				return "kinetics of non-modulated unireactant enzymes"; // 0000326
			break;
		case 1: // one enzmye
			if (getParentReaction().getReversible()) {
				if ((numOfActivators == 0) && (numOfInhibitors == 0))
					return "kinetics of non-modulated unireactant enzymes";
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

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	public String getSBO() {
		String name = getName().toLowerCase(), sbo = "none";
		if (name.equals("normalised kinetics of unireactant enzymes"))
			sbo = "0000199";
		else if (name.equals("kinetics of non-modulated unireactant enzymes"))
			sbo = "0000326";
		else if (name.equals("Briggs-Haldane equation"))
			sbo = "0000031";
		else if (name
				.equals("kinetics of irreversible non-modulated unireactant enzymes"))
			sbo = "0000028";
		else if (name
				.equals("simple mixed-type inhibition of irreversible unireactant enzymes"))
			sbo = "0000265";
		else if (name.equals("kinetics of unireactant enzymes"))
			sbo = "0000269";
		else if (name.equals("Henri-Michaelis Menten equation"))
			sbo = "0000029";
		else if (name.equals("Van Slyke-Cullen equation"))
			sbo = "0000030";
		else if (name
				.equals("simple uncompetitive inhibition of irreversible unireactant enzymes"))
			sbo = "0000262";
		else if (name
				.equals("mixed-type inhibition of irreversible enzymes by mutually exclusive inhibitors"))
			sbo = "0000275";
		else if (name
				.equals("simple non-competitive inhibition of unireactant enzymes"))
			sbo = "0000266";
		else if (name
				.equals("mixed-type inhibition of irreversible unireactant enzymes by two inhibitors"))
			sbo = "0000276";
		else if (name
				.equals("mixed-type inhibition of unireactactant enzymes by two inhibitors"))
			sbo = "0000277";
		else if (name
				.equals("simple competitive inhibition of irreversible unireactant enzymes by two non-exclusive inhibitors"))
			sbo = "0000274";
		else if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by two exclusive inhibitors"))
			sbo = "0000271";
		else if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by exclusive inhibitors"))
			sbo = "0000270";
		else if (name
				.equals("simple competitive inhibition of irreversible unireactant enzymes by one inhibitor"))
			sbo = "0000260";

		return sbo;
	}
}
