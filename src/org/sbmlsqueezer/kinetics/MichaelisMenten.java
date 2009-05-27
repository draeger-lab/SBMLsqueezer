package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;

import org.sbml.libsbml.ASTNode;
import org.sbmlsqueezer.io.LaTeXExport;
import org.sbmlsqueezer.io.TextExport;

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
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Aug 1, 2007
 */
public class MichaelisMenten extends BasicKineticLaw {

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
			throws RateLawNotApplicableException, IOException, IllegalFormatException {
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
			throws RateLawNotApplicableException, IOException, IllegalFormatException {
		super(parentReaction, model);
	}

	@Override
	protected StringBuffer createKineticEquation(PluginModel model, 
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		StringBuffer numerator =new StringBuffer(); StringBuffer denominator = new StringBuffer();
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
		
		
		StringBuffer formula = new StringBuffer();
		int enzymeNum = 0;
		do {
			StringBuffer kcatp, kcatn =new StringBuffer();
			StringBuffer kMe = new StringBuffer(concat("kM_", reaction.getId()));
			StringBuffer kMp = new StringBuffer();
			kMp = kMe;
			
			if (modE.size() == 0) {
				kcatp = concat("Vp_" , reaction.getId());
				kcatn = concat("Vn_", reaction.getId());
				} else {
				kcatp = concat("kcatp_" , reaction.getId());
				kcatn = concat("kcatn_" ,reaction.getId());
					if (modE.size() > 1) {
					kcatp = concat(kcatp,Character.valueOf('_') , modE.get(enzymeNum));
					kcatn =concat(kcatn, Character.valueOf('_') , modE.get(enzymeNum));
					kMe= concat(kMe,Character.valueOf('_') , modE.get(enzymeNum));
					kMp =concat(kMp,Character.valueOf('_') , modE.get(enzymeNum));
					}
				}
			kMe =concat(kMe, Character.valueOf('_') + specRefR.getSpecies());
			
			if (!listOfLocalParameters.contains(kcatp))
				listOfLocalParameters.add(kcatp);
			if (!listOfLocalParameters.contains(kMe))
				listOfLocalParameters.add(kMe);
			
			StringBuffer currEnzyme =new StringBuffer();
			
			/*
			 * Irreversible Reaction
			 */
			if (!reaction.getReversible()) {
		
				numerator = times(kcatp,  new StringBuffer(specRefR.getSpecies()));
				denominator = new StringBuffer(specRefR.getSpecies());
				/*
				 * Reversible Reaction
				 */
			}else{
				numerator = times(frac(kcatp, kMe ), new StringBuffer(specRefR.getSpecies()));
				denominator = frac(new StringBuffer(specRefR.getSpecies()), kMe);
				kMp =concat(kMp ,Character.valueOf('_') , specRefP.getSpecies());
				
				if (!listOfLocalParameters.contains(kcatn))
					listOfLocalParameters.add(kcatn);
				if (!listOfLocalParameters.contains(kMp))
					listOfLocalParameters.add(kMp);

				
				numerator=diff(numerator, times(frac(kcatn, kMp), new StringBuffer(specRefP.getSpecies())));
		
				denominator=sum(denominator, frac(new StringBuffer(specRefP.getSpecies()), kMp));
				}

			/*
			 * Inhibition
			 */
			if (modInhib.size() == 1) {
				StringBuffer kIa, kIb =new StringBuffer();

				kIa = concat("KIa_", reaction.getId());
				kIb = concat("KIb_", reaction.getId());
					
				if (modE.size() > 1) {
					kIa = concat(kIa, Character.valueOf('_') + modE.get(enzymeNum));
					kIb = concat(kIb, Character.valueOf('_') + modE.get(enzymeNum));
					}

				if (!listOfLocalParameters.contains(kIa))
					listOfLocalParameters.add(kIa);
				if (!listOfLocalParameters.contains(kIb))
					listOfLocalParameters.add(kIb);
				StringBuffer inh = new StringBuffer();
			
				inh=sum(new StringBuffer('1'), frac(new StringBuffer(modInhib.get(0)) , kIb));
				
				if (reaction.getReversible()) {
					denominator=sum(frac(new StringBuffer(modInhib.get(0)), kIa), times(denominator,inh));
					} else {
				
					denominator=sum(frac(times(kMe, new StringBuffer(modInhib.get(0))), kIa), times(denominator, inh));
					}

			} else if ((modInhib.size() > 1)
					&& !getParentReaction().getReversible()) {
				// mixed-type inihibition of irreversible enzymes by mutually
				// exclusive
				// inhibitors.

				denominator=times(denominator);

				//denominator += " * (1 + "; // substrate
				
				StringBuffer inh=new StringBuffer();
				// kMe += " * (1 + "; // Km
				for (int i = 0; i < modInhib.size(); i++) {
					StringBuffer kIai = new StringBuffer(concat((i + 1) , Character.valueOf('_') , reaction.getId()));
					StringBuffer inhib = new StringBuffer(modInhib.get(i));
					if (modE.size() > 1) {
						kIai =concat(kIai ,Character.valueOf('_'),modE.get(enzymeNum));
						}
					StringBuffer kIbi = new StringBuffer(concat("kIb", kIai));
					kIai = concat("kIa" , kIai);
					if(!listOfLocalParameters.contains(kIai))
						listOfLocalParameters.add(kIai);
					if (!listOfLocalParameters.contains(kIbi))
						listOfLocalParameters.add(kIbi);
					
				inh =frac(inhib, kIbi);
				denominator=times(denominator, sum(new StringBuffer('1'),frac(inhib,kIai)));

				}
			} else if (modInhib.size() > 1) {
				// the formalism from the convenience kinetics as a default.
				StringBuffer inhib = new StringBuffer();
		        StringBuffer inh = new StringBuffer();
				
				for (int inhibitorNum = 0; inhibitorNum < modInhib.size(); inhibitorNum++) {
					StringBuffer kI = new StringBuffer(concat("kI_" , reaction.getId())) ;
					kI =concat(kI , Character.valueOf('_'), modInhib.get(inhibitorNum));
						if (!listOfLocalParameters.contains(kI))
						listOfLocalParameters.add(kI);

					inhib =times(inhib , frac(kI ,sum(kI , new StringBuffer(modInhib.get(inhibitorNum)))));
					}
				currEnzyme=times(currEnzyme, inh);
						
			}

			if (reaction.getReversible()) {
			
				denominator = sum(new StringBuffer('1') ,denominator);
				
			} else {
				
				denominator = sum(kMe , denominator);
				
			}

			// construct formula
			currEnzyme = frac(numerator, denominator);
			
			if (modE.size() > 0) {
			
				currEnzyme =times(new StringBuffer(modE.get(enzymeNum)), currEnzyme);
				
			}
		
			if (numOfEnzymes <= 1)
				formula = currEnzyme;
			else 
				formula =times(formula, currEnzyme);	
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);
		/*
		 * Activation
		 */
	
		StringBuffer act = new StringBuffer();
		for (int i = 0; i < modActi.size(); i++) {

			StringBuffer kAa;

			kAa =concat("kA_" , reaction.getId());
			
			/* "\\cdot\\left(1+\\frac{" */
			
			if (!listOfLocalParameters.contains(kAa))
				listOfLocalParameters.add(kAa);

			act=times(act, frac(new StringBuffer(modActi.get(i)), sum(kAa,new StringBuffer(modActi.get(i)))));
			
				}
			
			formula=times(act, formula);
				
		
		return formula;
	}

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

	@Override
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
