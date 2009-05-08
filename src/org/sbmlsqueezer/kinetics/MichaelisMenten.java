/*
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler: JDK 1.6.0
 */
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;

import org.sbml.libsbml.ASTNode;
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
		ASTNode ast = null;
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
			ASTNode numerator_n;
			ASTNode denominator_n;
			ASTNode temp;
			ASTNode temp2;
			ASTNode currEnzyme = new ASTNode(AST_TIMES);
			ASTNode kMeN = new ASTNode(AST_NAME);
			kMeN.setName(kMe.toString());
			/*
			 * Irreversible Reaction
			 */
			if (!reaction.getReversible()) {
				numerator = times(kcatp , new StringBuffer(specRefR.getSpecies()));
				numerator_n = new ASTNode(AST_TIMES);
				temp = new ASTNode(AST_NAME);
				temp.setName(kcatp.toString());
				numerator_n.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(specRefR.getSpecies());
				numerator_n.addChild(temp);
				denominator = new StringBuffer(specRefR.getSpecies());
				denominator_n = new ASTNode(AST_NAME);
				denominator_n.setName(specRefR.getSpecies());
				
				/*
				 * Reversible Reaction
				 */
			} else {
				temp2 = new ASTNode(AST_DIVIDE);
				numerator_n = new ASTNode(AST_TIMES);
				temp = new ASTNode(AST_NAME);
				temp.setName(kcatp.toString());
				temp2.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(kMe.toString());
				temp2.addChild(temp);
				numerator_n.addChild(temp2);
				temp = new ASTNode(AST_NAME);
				temp.setName(specRefR.getSpecies());
				numerator_n.addChild(temp);

				numerator = times(frac (kcatp , kMe), new StringBuffer(specRefR.getSpecies()));
				denominator_n = new ASTNode(AST_DIVIDE);
				temp = new ASTNode(AST_NAME);
				temp.setName(specRefR.getSpecies());
				denominator_n.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(kMe.toString());
				denominator_n.addChild(temp);
				denominator = frac( new StringBuffer(specRefR.getSpecies()) , kMe);
				
				kMp = concat(kMp,'_', specRefP.getSpecies());
				
				if (!listOfLocalParameters.contains(kcatn))
					listOfLocalParameters.add(kcatn);
				if (!listOfLocalParameters.contains(kMp))
					listOfLocalParameters.add(kMp);

				temp2 = numerator_n;
				numerator_n = new ASTNode(AST_MINUS);
				numerator_n.addChild(temp2);
				temp = new ASTNode(AST_NAME);
				temp.setName(kcatn.toString());
				temp2 = new ASTNode(AST_DIVIDE);
				temp2.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(kMp.toString());
				temp2.addChild(temp);
				temp = new ASTNode(AST_TIMES);
				temp.addChild(temp2);
				temp2 = new ASTNode(AST_NAME);
				temp2.setName(specRefP.getSpecies());
				temp.addChild(temp2);
				numerator_n.addChild(temp);

				numerator =diff(numerator, times(frac( kcatn,kMp), new StringBuffer(specRefP.getSpecies())));
				
				temp = denominator_n;
				denominator_n = new ASTNode(AST_PLUS);
				denominator_n.addChild(temp);
				temp2 = new ASTNode(AST_DIVIDE);
				temp = new ASTNode(AST_NAME);
				temp.setName(specRefP.getSpecies());
				temp2.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(kMp.toString());
				temp2.addChild(temp);
				denominator_n.addChild(temp2);

				denominator =sum(frac( new StringBuffer(specRefP.getSpecies()), kMp));
				}

			/*
			 * Inhibition
			 */
			if (modInhib.size() == 1) {
				StringBuffer kIa, kIb;

				kIa = concat("KIa_", reaction.getId());
				kIb = concat("KIb_" , reaction.getId());
				
				if (modE.size() > 1) {
					kIa = concat(kIa, Character.valueOf('_'), modE.get(enzymeNum));
					kIb = concat(kIb, Character.valueOf('_'), modE.get(enzymeNum));
					}

				if (!listOfLocalParameters.contains(kIa))
					listOfLocalParameters.add(kIa);
				if (!listOfLocalParameters.contains(kIb))
					listOfLocalParameters.add(kIb);
				ASTNode inh = new ASTNode(AST_PLUS);
				temp = new ASTNode(AST_INTEGER);
				temp.setValue(1);
				inh.addChild(temp);
				temp2 = new ASTNode(AST_DIVIDE);
				temp = new ASTNode(AST_NAME);
				temp.setName(modInhib.get(0));
				temp2.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(kIb.toString());
				temp2.addChild(temp);
				inh.addChild(temp2);
				kIb = sum(new StringBuffer(1), frac(new StringBuffer(modInhib.get(0)), kIb ));
				if (reaction.getReversible()) {
					
					temp2 = denominator_n;
					denominator_n = new ASTNode(AST_PLUS);
					ASTNode faktor = new ASTNode(AST_DIVIDE);
					temp = new ASTNode(AST_NAME);
					temp.setName(modInhib.get(0));
					faktor.addChild(temp);
					temp = new ASTNode(AST_NAME);
					temp.setName(kIa.toString());
					faktor.addChild(temp);
					denominator_n.addChild(faktor);
					temp = new ASTNode(AST_TIMES);
					temp.addChild(temp2);
					temp.addChild(inh);
					denominator_n.addChild(temp);

					denominator = frac(new StringBuffer(modInhib.get(0)),sum(kIa, times(denominator, kIb)));
					} else {
						temp2 = new ASTNode(AST_TIMES);
					temp = new ASTNode(AST_NAME);
					temp.setName(kMe.toString());
					temp2.addChild(temp);
					temp = new ASTNode(AST_NAME);
					temp.setName(modInhib.get(0));
					temp2.addChild(temp);
					temp = new ASTNode(AST_DIVIDE);
					temp.addChild(temp2);
					temp2 = new ASTNode(AST_NAME);
					temp2.setName(kIa.toString());
					temp.addChild(temp2);

					temp2 = denominator_n;
					denominator_n = new ASTNode(AST_PLUS);
					denominator_n.addChild(temp);
					temp = new ASTNode(AST_TIMES);
					temp.addChild(temp2);
					temp.addChild(inh);
					denominator_n.addChild(temp);

					denominator = frac(times(kMe, new StringBuffer(modInhib.get(0))) ,sum(kIa ,times(denominator, kIb)));
					}

			} else if ((modInhib.size() > 1)
					&& !getParentReaction().getReversible()) {
				// mixed-type inihibition of irreversible enzymes by mutually
				// exclusive
				// inhibitors.

				temp2 = denominator_n;
				System.out.println("MEHRERE INIBITOREN");
				denominator_n = new ASTNode(AST_TIMES);
				denominator_n.addChild(temp2);
				temp = new ASTNode(AST_INTEGER);
				temp.setValue(1);
				temp2 = new ASTNode(AST_PLUS);
				temp2.addChild(temp);

				StringBuffer denominator_mod= new StringBuffer(1);
				
				kMeN = new ASTNode(AST_PLUS);
				temp = new ASTNode(AST_INTEGER);
				temp.setValue(1);
				kMeN.addChild(temp);
				ASTNode inh;
				// kMe += " * (1 + "; // Km
				for (int i = 0; i < modInhib.size(); i++) {
					String kIai = (i + 1) + '_' + reaction.getId();
						String inhib = modInhib.get(i);
					if (modE.size() > 1) {
						kIai += '_' + modE.get(enzymeNum);
						}
						String kIbi = "kIb" + kIai;
					kIai = "kIa" + kIai;
					if (!listOfLocalParameters.contains(kIai))
						listOfLocalParameters.add(new StringBuffer(kIai));
					if (!listOfLocalParameters.contains(kIbi))
						listOfLocalParameters.add(new StringBuffer(kIbi));

					inh = new ASTNode(AST_DIVIDE);
					temp = new ASTNode(AST_NAME);
					temp.setName(inhib);
					inh.addChild(temp);
					temp = new ASTNode(AST_NAME);
					temp.setName(kIai);
					inh.addChild(temp);
					temp2.addChild(inh);
					denominator_mod = sum(denominator_mod, frac(new StringBuffer(inhib), new StringBuffer(kIai)));
					
					inh = new ASTNode(AST_DIVIDE);
					temp = new ASTNode(AST_NAME);
					temp.setName(inhib);
					inh.addChild(temp);
					temp = new ASTNode(AST_NAME);
					temp.setName(kIbi);
					inh.addChild(temp);
					kMeN.addChild(inh);
					// kMe += inhib + '/' + kIbi; // Km
				}
				
				denominator=times(denominator, denominator_mod); // substrate
				// kMe += ')';
				temp = kMeN;
				kMeN = new ASTNode(AST_TIMES);
				inh = new ASTNode(AST_NAME);
				inh.setName(kMe.toString());
				kMeN.addChild(inh);
				kMeN.addChild(temp);
				denominator_n.addChild(temp2);

			} else if (modInhib.size() > 1) {
				// the formalism from the convenience kinetics as a default.
				StringBuffer inhib = new StringBuffer();
				ASTNode inh = new ASTNode(AST_TIMES);
				ASTNode faktor;
				for (int inhibitorNum = 0; inhibitorNum < modInhib.size(); inhibitorNum++) {
					StringBuffer kI = concat("kI_" , reaction.getId());
					kI = concat(kI, Character.valueOf('_') , modInhib.get(inhibitorNum));
						if (!listOfLocalParameters.contains(kI))
						listOfLocalParameters.add(new StringBuffer(kI));

					temp = new ASTNode(AST_PLUS);
					temp2 = new ASTNode(AST_NAME);
					temp2.setName(kI.toString());
					temp.addChild(temp2);
					temp2 = new ASTNode(AST_NAME);
					temp2.setName(modInhib.get(inhibitorNum));
					temp.addChild(temp2);
					faktor = new ASTNode(AST_DIVIDE);
					temp2 = new ASTNode(AST_NAME);
					temp2.setName(kI.toString());
					faktor.addChild(temp2);
					faktor.addChild(temp);
					inh.addChild(faktor);

					inhib = frac(new StringBuffer(kI),sum(new StringBuffer(kI), new StringBuffer(modInhib.get(inhibitorNum))));
						}
				currEnzyme.addChild(inh);
				formelTxt =inhib;
				}

			if (reaction.getReversible()) {
				temp2 = denominator_n;
				denominator_n = new ASTNode(AST_PLUS);
				temp = new ASTNode(AST_INTEGER);
				temp.setValue(1);
				denominator_n.addChild(temp);
				denominator_n.addChild(temp2);

				denominator = sum(new StringBuffer(1) ,denominator);
				} else {
				temp2 = denominator_n;
				denominator_n = new ASTNode(AST_PLUS);
				denominator_n.addChild(kMeN);
				denominator_n.addChild(temp2);

				denominator = sum(kMe ,denominator);
			}

			// construct formula
			temp = new ASTNode(AST_DIVIDE);
			temp.addChild(numerator_n);
			temp.addChild(denominator_n);
			if (currEnzyme.getNumChildren() <= 1)
				currEnzyme = temp;

			formelTxt= times(formelTxt, frac(numerator, denominator));
			if (modE.size() > 0) {
				// TODO - ERROR
				temp = currEnzyme;
				currEnzyme = new ASTNode(AST_TIMES);
				temp2 = new ASTNode(AST_NAME);
				temp2.setName(modE.get(enzymeNum));
				currEnzyme.addChild(temp2);
				currEnzyme.addChild(temp);

				formelTxt = sum(new StringBuffer(modE.get(enzymeNum)),formelTxt);
					
			}
			if (currEnzyme.getNumChildren() == 1) {
				currEnzyme = currEnzyme.getLeftChild();
			}
			if (numOfEnzymes <= 1)
				ast = currEnzyme;
			else {
				if (ast == null)
					ast = new ASTNode(AST_PLUS);
				ast.addChild(currEnzyme);
			}
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);
		/*
		 * Activation
		 */
		ASTNode temp, temp2;
		ASTNode act = new ASTNode(AST_TIMES);
		for (int i = 0; i < modActi.size(); i++) {

			StringBuffer kAa; // , kAbTeX;

			kAa = concat("kA_",reaction.getId());
			// ????
			/* "\\cdot\\left(1+\\frac{" */
		    // kAbTeX = "\\cdot\\left(1+\\frac{k^\\text{Ab}_{" + reaction.getId();

			if (!listOfLocalParameters.contains(kAa))
				listOfLocalParameters.add(kAa);

			temp = new ASTNode(AST_NAME);
			temp.setName(kAa.toString());
			temp2 = new ASTNode(AST_PLUS);
			temp2.addChild(temp);
			temp = new ASTNode(AST_NAME);
			temp.setName(modActi.get(i));
			temp2.addChild(temp);
			temp = new ASTNode(AST_DIVIDE);
			temp.addChild(new ASTNode(AST_NAME));
			temp.getLeftChild().setName(modActi.get(i));
			temp.addChild(temp2);
			act.addChild(temp);

			formelTxt =times(frac(new StringBuffer(modActi.get(i)),sum( kAa, new StringBuffer(modActi.get(i)))), formelTxt);
			
			// ????
			/*
			 * kAa = " * (1 + " + kAa + '/' + modActi.get(0) + ')'; kAb =
			 * " * (1 + " + kAb + '/' + modActi.get(0) + ')'; kAaTeX += "}}{" +
			 * Species.toTeX(modActi.get(0)) + "}\\right)"; kAbTeX += "}}{" +
			 * Species.toTeX(modActi.get(0)) + "}\\right)";
			 */
		}
		if (act.getNumChildren() > 0) {
			temp = ast;
			ast = new ASTNode(AST_TIMES);
			if (act.getNumChildren() == 1) {
				act = act.getLeftChild();
			}
			ast.addChild(act);
			ast.addChild(temp);
		}

		
		return formelTxt;
	}
}
