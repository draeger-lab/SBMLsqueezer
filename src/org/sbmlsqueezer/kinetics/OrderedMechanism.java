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
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Aug 1, 2007
 */
public class OrderedMechanism extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException 
	 */
	public OrderedMechanism(PluginReaction parentReaction, PluginModel model,
			boolean reversibility) throws RateLawNotApplicableException, IOException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 * @throws IOException 
	 */
	public OrderedMechanism(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	@Override
	public String getName() {
		// according to Cornish-Bowden: Fundamentals of Enzyme kinetics
		double stoichiometryRight = 0;
		for (int i = 0; i < getParentReaction().getNumProducts(); i++)
			stoichiometryRight += getParentReaction().getProduct(i)
					.getStoichiometry();
		String name = "compulsory-order ternary-complex mechanism";
		if ((getParentReaction().getNumProducts() == 2)
				|| (stoichiometryRight == 2))
			name += " with two products";
		else if ((getParentReaction().getNumProducts() == 1)
				|| (stoichiometryRight == 1))
			name += " with one product";
		if (getParentReaction().getReversible())
			return "reversible " + name;
		return "irreversible " + name;
	}

	@Override
	public String getSBO() {
		return "none";
	}

	@Override
	protected StringBuffer createKineticEquation(PluginModel model, int reactionNum,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		StringBuffer numerator = new StringBuffer();// I
		StringBuffer denominator = new StringBuffer(); // II
		StringBuffer inhib = new StringBuffer();
		StringBuffer acti = new StringBuffer();
		StringBuffer formelTxt = new StringBuffer();

		PluginReaction reaction = getParentReaction();
		PluginSpeciesReference specRefE1 = (PluginSpeciesReference) reaction
				.getListOfReactants().get(0), specRefE2 = null;
		PluginSpeciesReference specRefP1 = (PluginSpeciesReference) reaction
				.getListOfProducts().get(0), specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefE2 = (PluginSpeciesReference) reaction.getListOfReactants()
					.get(1);
		else if (specRefE1.getStoichiometry() == 2.0)
			specRefE2 = specRefE1;
		else
			throw new RateLawNotApplicableException(
					"Number of reactants must equal two to apply ordered "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		boolean exception = false, biuni = false;
		switch (reaction.getNumProducts()) {
		case 1:
			if (specRefP1.getStoichiometry() == 1d)
				biuni = true;
			else if (specRefP1.getStoichiometry() == 2d)
				specRefP2 = specRefP1;
			else
				exception = true;
			break;
		case 2:
			specRefP2 = (PluginSpeciesReference) reaction.getListOfProducts()
					.get(1);
			break;
		default:
			exception = true;
			break;
		}
		if (exception)
			throw new RateLawNotApplicableException(
					"Number of products must equal either one or two to apply ordered "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		int enzymeNum = 0;
		reactionNum++;
		do {
			/*
			 * Variables that are needed for the different combinations of
			 * educts and prodcuts.
			 */
			
			StringBuffer kcatp= new StringBuffer();
			StringBuffer kMr1 =concat("kM_",reactionNum);
			StringBuffer kMr2 = concat("kM_",reactionNum);			
			StringBuffer kIr1 = concat("ki_",reactionNum);
		
		
			// reverse reactions
			StringBuffer kcatn=new StringBuffer();
			StringBuffer kMp1 = concat("kM_", reactionNum);
			StringBuffer kMp2 = concat("kM_",reactionNum);
			StringBuffer kIp1 = concat("ki_", reactionNum);
			StringBuffer kIp2 = concat("ki_", reactionNum);
			StringBuffer kIr2 = concat("ki_", reactionNum);

			if (modE.size() == 0) {
				kcatp = concat("Vp_", reactionNum);
							// reverse reactions
				kcatn = concat( "Vn_", reactionNum);
						} else {
				kcatp = concat("kcatp_", reactionNum);
							//
				kcatn =  concat("kcatn_", reactionNum);
				if (modE.size() > 1) {
					
					 kcatp= concat(kcatp,"_" , modE.get(enzymeNum));
			          kMr1 = concat(kMr1,"_" , modE.get(enzymeNum));
			          kMr2 = concat(kMr2,"_" , modE.get(enzymeNum));
					
			          kIr1 = concat(kIr1,"_" , modE.get(enzymeNum));
					
			
					// reverse reactions
			          
			         StringBuffer modEnzymeNumber=new StringBuffer(modE.get(enzymeNum));
					    kcatn=concat(kcatn , '_' ,modEnzymeNumber);
			            kMp1 =concat(kMp1 , '_' ,modEnzymeNumber);
			            kMp2 =concat(kMp2 , '_' ,modEnzymeNumber);
			            kIp1 =concat(kIp1 , '_' ,modEnzymeNumber);
			            kIp2 =concat(kIp2 , '_' ,modEnzymeNumber);
			            kIr2 =concat(kIr2 , '_' ,modEnzymeNumber);
				
				}
			}
			
			   kMr2 =concat( kMr2, "_", specRefE2.getSpecies());
			   kMr1 =concat(kMr1, "_" , specRefE1.getSpecies());
			// reverse reactions
			if (specRefP2 != null)
				kMp2 =concat (kMp2, "_" , specRefP2.getSpecies());
			    kMp1 =concat(kMp1,"_" , specRefP1.getSpecies());

			if (specRefE2.equals(specRefE1)) {
			kMr1 =concat( "kMr1" , kMr1.substring(2));
			kMr2 =concat( "kMr2" , kMr2.substring(2));

			}
			// reverse reactions
			kIp1 =concat (kIp1, "_" , specRefP1.getSpecies());
			if (specRefP2 != null) {
				if (specRefP2.equals(specRefP1)) {
					kMp1 = concat( "kMp1", kMp1.substring(2));
					kMp2 = concat( "kMp2", kMp2.substring(2));
			
				}
				kIp2=concat(kIp2 , "_" , specRefP2.getSpecies());
			
			}
			kIr1 =concat(kIr1,"_" , specRefE1.getSpecies());
		

			// reverse reactions
			kIr2 =concat(kIr2, "_" , specRefE2.getSpecies());
		

			if (!listOfLocalParameters.contains(kcatp))
				listOfLocalParameters.add(kcatp);

			/*
			 * Irreversible reaction
			 */
			if (!reaction.getReversible()) {
				if (!listOfLocalParameters.contains(kMr2))
					listOfLocalParameters.add(kMr2);
				if (!listOfLocalParameters.contains(kMr1))
					listOfLocalParameters.add(kMr1);
				if (!listOfLocalParameters.contains(kIr1))
					listOfLocalParameters.add(kIr1);

				numerator = kcatp ;
				
				if (modE.size() > 0) {
					numerator =times(numerator, kcatp, new StringBuffer(modE.get(enzymeNum)));
				
				}
				numerator =times(numerator, kcatp, new StringBuffer( specRefE1.getSpecies()));
				
				denominator = times(kIr1 , kMr2);
				
				if (specRefE2.equals(specRefE1)) {
					StringBuffer numerator_s1s2=pow(new StringBuffer(specRefE1.getSpecies()), new StringBuffer('2'));
					numerator= times(numerator,numerator_s1s2);
				
					sum(denominator,times(sum(kMr2,kMr1 ),new StringBuffer(specRefE1.getSpecies())),pow(new StringBuffer(specRefE1.getSpecies()) , new StringBuffer('2')));
					
				} else {
					numerator= times(numerator, new StringBuffer(specRefE2.getSpecies()));
					
					denominator =sum(denominator, times(kMr2, new StringBuffer( specRefE1.getSpecies())),
							times( kMr1, new StringBuffer( specRefE2.getSpecies())),
							times(new StringBuffer(specRefE1.getSpecies()) ,new StringBuffer(specRefE2.getSpecies())));
				
				}

			} else if (!biuni) {
				/*
				 * Reversible Bi-Bi reaction.
				 */
				if (!listOfLocalParameters.contains(kIr2))
					listOfLocalParameters.add(kIr2);
				if (!listOfLocalParameters.contains(kcatn))
					listOfLocalParameters.add(kcatn);
				if (!listOfLocalParameters.contains(kMr1))
					listOfLocalParameters.add(kMr1);
				if (!listOfLocalParameters.contains(kMr2))
					listOfLocalParameters.add(kMr2);
				if (!listOfLocalParameters.contains(kMp1))
					listOfLocalParameters.add(kMp1);
				if (!listOfLocalParameters.contains(kMp2))
					listOfLocalParameters.add(kMp2);
				if (!listOfLocalParameters.contains(kIr1))
					listOfLocalParameters.add(kIr1);
				if (!listOfLocalParameters.contains(kIp1))
					listOfLocalParameters.add(kIp1);
				if (!listOfLocalParameters.contains(kIp2))
					listOfLocalParameters.add(kIp2);

				numerator = kcatp;
			
				if (modE.size() > 0) {
					numerator =times(numerator,new StringBuffer(modE.get(enzymeNum))),;
				
				}

				numerator += specRefE1.getSpecies();
			
				denominator = "1 + (" + specRefE1.getSpecies() + '/' + kIr1
						+ ") + ((" + kMr1 + " * " + specRefE2.getSpecies()
						+ ")/(" + kIr1 + " * " + kMr2 + "))" + " + ((" + kMp2
						+ " * " + specRefP1.getSpecies() + ")/(" + kIp2 + " * "
						+ kMp1 + ")) + (" + specRefP2.getSpecies() + '/' + kIp2
						+ ") + (";
				if (specRefE2.equals(specRefE1)) {
					numerator += "^2";
				
					denominator += specRefE1.getSpecies() + "^2";
				
				} else {
					numerator += " * " + specRefE2.getSpecies();
				
					denominator += '(' + specRefE1.getSpecies() + " * "
							+ specRefE2.getSpecies() + ')';
			
				}
				numerator += ")/(" + kIr1 + " * " + kMr2 + "))" + " - ((" + kcatn
						+ " * ";
			
				if (modE.size() > 0) {
					numerator += modE.get(enzymeNum) + " * ";
				
				}
				numerator += specRefP1.getSpecies();
				if (specRefP2.equals(specRefP1)) {
					numerator += "^2";
									} else {
					numerator += " * " + specRefP2.getSpecies();
									}
				numerator += ")/(" + kIp2 + " * " + kMp1 + "))";
				denominator += "/(" + kIr1 + " * " + kMr2 + ")) + ((" + kMp2
						+ " * " + specRefE1.getSpecies() + " * "
						+ specRefP1.getSpecies() + ")/(" + kIr1 + " * " + kMp1
						+ " * " + kIp2 + ")) + ((" + kMr1 + " * "
						+ specRefE2.getSpecies() + " * "
						+ specRefP2.getSpecies() + ")/(" + kIr1 + " * " + kMr2
						+ " * " + kIp2 + ")) + (";
			
				if (specRefP2.equals(specRefP1)) {
					denominator += specRefP1.getSpecies() + "^2";
					
				} else {
					denominator += '(' + specRefP1.getSpecies() + " * "
							+ specRefP2.getSpecies() + ')';
					
				}
				denominator += "/(" + kMp1 + " * " + kIp2 + ")) + (("
						+ specRefE1.getSpecies();
				
				if (specRefE2.equals(specRefE1)) {
					denominator += "^2";
					
				} else {
					denominator += " * " + specRefE2.getSpecies();
				
				}
				denominator += " * " + specRefP1.getSpecies() + ")/(" + kIr1
						+ " * " + kMr2 + " * " + kIp1 + ")) + (("
						+ specRefE2.getSpecies() + " * "
						+ specRefP1.getSpecies();
			
				if (specRefP2.equals(specRefP1)) {
					denominator += "^2";
					
				} else {
					denominator += " * " + specRefP2.getSpecies();
				
				}
				denominator += ")/(" + kIr2 + " * " + kMp1 + " * " + kIp2 + "))";
				

			} else {
				/*
				 * Reversible bi-uni reaction
				 */
				if (!listOfLocalParameters.contains(kcatn))
					listOfLocalParameters.add(kcatn);
				if (!listOfLocalParameters.contains(kMr1))
					listOfLocalParameters.add(kMr1);
				if (!listOfLocalParameters.contains(kMr2))
					listOfLocalParameters.add(kMr2);
				if (!listOfLocalParameters.contains(kMp1))
					listOfLocalParameters.add(kMp1);
				if (!listOfLocalParameters.contains(kIr1))
					listOfLocalParameters.add(kIr1);
				if (!listOfLocalParameters.contains(kIp1))
					listOfLocalParameters.add(kIp1);

				numerator = "((" + kcatp + " * ";
			
				if (modE.size() > 0) {
					numerator += modE.get(enzymeNum) + " * ";
				
				}
				numerator += specRefE1.getSpecies();
			
				denominator = "1 + (" + specRefE1.getSpecies() + '/' + kIr1
						+ ") + ((" + kMr1 + " * " + specRefE2.getSpecies()
						+ ")/(" + kIr1 + " * " + kMr2 + ")) + (";
			
				if (specRefE2.equals(specRefE1)) {
					numerator += "^2";
				
					denominator += specRefE1.getSpecies() + "^2";
		
				} else {
					numerator += " * " + specRefE2.getSpecies();

					denominator += '(' + specRefE1.getSpecies() + " * "
							+ specRefE2.getSpecies() + ')';

				}
				numerator += ")/(" + kIr1 + " * " + kMr2 + "))" + " - ((" + kcatn
						+ " * ";
		
				if (modE.size() > 0) {
					numerator += modE.get(enzymeNum) + " * ";
					
				}
				numerator += specRefP1.getSpecies() + ")/" + kMp1 + ')';
			

				denominator += "/(" + kIr1 + " * " + kMr2 + ")) + ((" + kMr1
						+ " * " + specRefE2.getSpecies() + " * "
						+ specRefP1.getSpecies() + ")/(" + kIr1 + " * " + kMr2
						+ " * " + kIp1 + ")) + (" + specRefP1.getSpecies() + '/'
						+ kMp1 + ')';
				
			}

			/*
			 * Construct formula
			 */
			formelTxt += "((" + numerator + ")/(" + denominator + "))";
			
			if (enzymeNum < modE.size() - 1) {
				formelTxt += " + ";
				
			}
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);

		/*
		 * Activation
		 */
		if (!modActi.isEmpty()) {
			for (int activatorNum = 0; activatorNum < modActi.size(); activatorNum++) {
				String kA = "kA_" + reactionNum;
				kA += "_" + modActi.get(activatorNum);
			

				if (!listOfLocalParameters.contains(kA))
					listOfLocalParameters.add(kA);
				acti += '(' + modActi.get(activatorNum) + "/(" + kA + " + "
						+ modActi.get(activatorNum) + ")) * ";
				
			}
		}

		/*
		 * Inhibition
		 */
		if (!modInhib.isEmpty()) {
			for (int inhibitorNum = 0; inhibitorNum < modInhib.size(); inhibitorNum++) {
				String kI = "kI_" + reactionNum;
				kI += "_" + modInhib.get(inhibitorNum);
				
				if (!listOfLocalParameters.contains(kI))
					listOfLocalParameters.add(kI);
				inhib += '(' + kI + "/(" + kI + " + "
						+ modInhib.get(inhibitorNum) + ")) * ";
				
			}
		}

		if ((acti.length() + inhib.length() > 0) && (modE.size() > 1)) {
			inhib += '(';
			formelTxt += ')';
		
		}
		formelTxt = acti + inhib + formelTxt;
		

		if (enzymeNum > 1)
			
		return formelTxt;
	}

}
