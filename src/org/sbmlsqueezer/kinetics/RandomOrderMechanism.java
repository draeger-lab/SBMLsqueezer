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
public class RandomOrderMechanism extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException 
	 */
	public RandomOrderMechanism(PluginReaction parentReaction,
			PluginModel model, boolean reversibility)
			throws RateLawNotApplicableException, IOException {
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
	public RandomOrderMechanism(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public String getName() {
		// according to Cornish-Bowden: Fundamentals of Enzyme kinetics
		double stoichiometryRight = 0;
		for (int i = 0; i < getParentReaction().getListOfProducts()
				.getNumItems(); i++)
			stoichiometryRight += getParentReaction().getProduct(i)
					.getStoichiometry();
		String name = "rapid-equilibrium random order ternary-complex mechanism";
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
		boolean biuni = false;
		StringBuffer numerator = new StringBuffer();// I
		StringBuffer denominator = new StringBuffer(); // II
		StringBuffer inhib = new StringBuffer();
		StringBuffer acti = new StringBuffer();
		StringBuffer formelTxt = new StringBuffer();

		PluginReaction reaction = getParentReaction();
		PluginSpeciesReference specRefE1 = reaction.getReactant(0), specRefE2;
		PluginSpeciesReference specRefP1 = reaction.getProduct(0), specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefE2 = (PluginSpeciesReference) reaction.getListOfReactants()
					.get(1);
		else if (specRefE1.getStoichiometry() == 2.0)
			specRefE2 = specRefE1;
		else
			throw new RateLawNotApplicableException(
					"Number of reactants must equal two to apply random order "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		boolean exception = false;
		switch (reaction.getNumProducts()) {
		case 1:
			if (specRefP1.getStoichiometry() == 1.0)
				biuni = true;
			else if (specRefP1.getStoichiometry() == 2.0)
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
					"Number of products must equal either one or two to apply random order "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		/*
		 * If modE is empty there was no enzyme sined to the reaction. Thus we
		 * do not want anything in modE to occur in the kinetic equation.
		 */
		int enzymeNum = 0;
		reactionNum++;
		do {
			/*
			 * Irreversible reaction
			 */
			if (!reaction.getReversible()) {
				StringBuffer kcatp;
				StringBuffer kMr2 = new StringBuffer("kM_"); kMr2.append(reactionNum);
				StringBuffer kMr1 =new StringBuffer("kM_"); kMr1.append(reactionNum);
				StringBuffer kIr1 =new StringBuffer("ki_"); kIr1.append(reactionNum);
				
				if (modE.size() == 0) {
					kcatp = new StringBuffer("Vp_"); kcatp.append(reactionNum);
					} else {
					kcatp =new StringBuffer("kcatp_"); kcatp.append(reactionNum);
					if (modE.size() > 1) {
						String  modEnzymeNumber=modE.get(enzymeNum);
						kcatp=concat(kcatp,Character.valueOf('_'),modEnzymeNumber);
						kMr2=concat(kMr2,Character.valueOf('_'),modEnzymeNumber);
						kMr1=concat(kMr1,Character.valueOf('_'),modEnzymeNumber);
						kIr1=concat(kIr1,Character.valueOf('_'),modEnzymeNumber);
						
					}
				}
				
				String speciesE2=specRefE2.getSpecies();
				kMr2=concat(kMr2,Character.valueOf('_'),speciesE2);
				kMr1=concat(kMr1,Character.valueOf('_'),speciesE2);
						
				if (specRefE2.equals(specRefE1)) {
					
				kMr1=concat(kMr1,"kMr1",kMr1.substring(2));
				kMr2=concat(kMr2,"kMr2",kMr2.substring(2));
					
				}
				kIr1= concat(kIr1,Character.valueOf('_'),speciesE2);
				
				if (!listOfLocalParameters.contains(kcatp))
					listOfLocalParameters.add(kcatp);
				if (!listOfLocalParameters.contains(kMr2))
					listOfLocalParameters.add(kMr2);
				if (!listOfLocalParameters.contains(kMr1))
					listOfLocalParameters.add(kMr1);
				if (!listOfLocalParameters.contains(kIr1))
					listOfLocalParameters.add(kIr1);
                    numerator = kcatp;
               if (modE.size() > 0) {
				numerator= times(numerator, new StringBuffer(modE.get(enzymeNum)));
				}
                numerator= times(numerator, new StringBuffer(specRefE1.getSpecies()));
               	
				if (specRefE2.equals(specRefE1)) {
					numerator=pow(numerator, new StringBuffer('2'));
					denominator=sum(
							        times(kIr1, kMr2), 
							        times(sum(kMr2, kMr1),new StringBuffer(specRefE1.getSpecies())),
							        pow(new StringBuffer(specRefE1.getSpecies()), new StringBuffer('2'))
							        );
							
				} else {
					numerator=times(numerator, new StringBuffer(specRefE2.getSpecies()));
					
					denominator = sum(
							       times(kIr1, kMr2),
					               times(kMr2, new StringBuffer(specRefE1.getSpecies())), 
							       times(kMr1 , new StringBuffer(specRefE2.getSpecies())),
                                   times(new StringBuffer(specRefE1.getSpecies()), new StringBuffer(specRefE2.getSpecies()))
                                   );
					}

			} else {
				/*
				 * Reversible reaction: Bi-Bi
				 */
				if (!biuni) {
					
					StringBuffer kcatp=new StringBuffer();
					StringBuffer kcatn=new StringBuffer();
					
					StringBuffer kMr2 = new StringBuffer("kM_") ;  kMr2.append(reactionNum);
					StringBuffer kIr1 = new StringBuffer("ki_");  kIr1.append(reactionNum);
					StringBuffer kMp1 = new StringBuffer("kM_");  kMp1.append(reactionNum);
					StringBuffer kIp1 = new StringBuffer("ki_");  kIp1.append(reactionNum);
					StringBuffer kIp2 = new StringBuffer("ki_");  kIp2.append(reactionNum);
					StringBuffer kIr2 = new StringBuffer("ki_");  kIr2.append(reactionNum);
					
					
					if (modE.size() == 0) {
						kcatp.append("Vp_"); kcatp.append(reactionNum);
						kcatn.append("Vn_"); kcatn.append(reactionNum);
						} else {
						kcatp.append("kcatp_"); kcatp.append(reactionNum);
						kcatn.append("kcatn_"); kcatn.append(reactionNum);
							if (modE.size() > 1) {
								
								String modEnzymeNumber=modE.get(enzymeNum);
								kcatp.append(Character.valueOf('_'));  kcatp.append(modEnzymeNumber);
								kcatn.append(Character.valueOf('_'));  kcatn.append(modEnzymeNumber);
								kMr2.append(Character.valueOf('_'));  kMr2.append(modEnzymeNumber);
								kMp1.append(Character.valueOf('_'));  kMp1.append(modEnzymeNumber);
								kIp1.append(Character.valueOf('_'));  kIp1.append(modEnzymeNumber);
								kIp2.append(Character.valueOf('_'));  kIp2.append(modEnzymeNumber);
								kIr2.append(Character.valueOf('_'));  kIr2.append(modEnzymeNumber);
								kIr1.append(Character.valueOf('_')); kIr1.append(modEnzymeNumber);
								}
					}					
					
					String speciesE2= specRefE2.getSpecies();
					kMr2.append(Character.valueOf('_'));  kMr2.append(speciesE2);
					kIr1.append(Character.valueOf('_') );  kIr1.append(speciesE2);
					kIr2.append(Character.valueOf('_') );  kIr2.append(speciesE2);
					kIp1.append( Character.valueOf('_') );  kIp1.append(speciesE2);
					kIp2.append( Character.valueOf('_'));  kIp2.append(speciesE2);
					kMp1.append(Character.valueOf('_') );  kMp1.append(speciesE2);
					if (specRefE2.equals(specRefE1)) {
						kIr1.append("kir1"); kIr1.append(kIr1.substring(2));
						kIr2.append("kir2"); kIr2.append(kIr2.substring(2));
						}
					if (specRefP2.equals(specRefP1)) {
						kIp1.append("kip1"); kIp1.append(kIp1.substring(2));
						kIp2.append("kip2"); kIp2.append(kIp2.substring(2));
						}
					
					if (!listOfLocalParameters.contains(kcatp))
						listOfLocalParameters.add(kcatp);
					if (!listOfLocalParameters.contains(kMr2))
						listOfLocalParameters.add(kcatn);
					if (!listOfLocalParameters.contains(kMr2))
						listOfLocalParameters.add(kMr2);
					if (!listOfLocalParameters.contains(kMp1))
						listOfLocalParameters.add(kMp1);
					if (!listOfLocalParameters.contains(kIp1))
						listOfLocalParameters.add(kIp1);
					if (!listOfLocalParameters.contains(kIp2))
						listOfLocalParameters.add(kIp2);
					if (!listOfLocalParameters.contains(kIr2))
						listOfLocalParameters.add(kIr2);
					if (!listOfLocalParameters.contains(kIr1))
						listOfLocalParameters.add(kIr1);
					
					StringBuffer numeratorForward=new StringBuffer();					
					StringBuffer numeratorReverse=new StringBuffer();
					
					
					   numeratorForward=kcatp;
						if (modE.size() > 0) {
							numeratorForward=times(numeratorForward, new StringBuffer(modE.get(enzymeNum)));
						}

					denominator =sum(new StringBuffer('1'),
							        frac(new StringBuffer(specRefE1.getSpecies()), kIr1),
						 		    frac(new StringBuffer( specRefE2.getSpecies() ), kIr2),
							 		frac(new StringBuffer(specRefP1.getSpecies() ),kIp1),
									frac(new StringBuffer( specRefP2.getSpecies() ),kIp2)
									);
							       
				
					
					// happens if the product has a stoichiometry of two.
				
					StringBuffer p1p2=new StringBuffer();
					
					if (specRefP1.equals(specRefP2)) {
						p1p2= pow(new StringBuffer(specRefP1.getSpecies()), new StringBuffer('2'));
					} else {
						p1p2= times(new StringBuffer(specRefP1.getSpecies()),new StringBuffer(specRefP2.getSpecies()));
						}
					denominator=sum(denominator, frac(p1p2,times(kIp2,kMp1 )));
					
					// happens if the educt has a stoichiometry of two.
					if (specRefE1.equals(specRefE2)) {
						
						numeratorForward=times(numeratorForward,pow(new StringBuffer(specRefE1.getSpecies() ), new StringBuffer('2')));
						p1p2= pow(new StringBuffer(specRefE1.getSpecies() ), new StringBuffer('2'));
					} else {
						numeratorForward=times(numeratorForward,times(new StringBuffer(specRefE1.getSpecies()),new StringBuffer(specRefE2.getSpecies())));
						p1p2= times( new StringBuffer(specRefE1.getSpecies()),new StringBuffer(specRefE2.getSpecies()));
						}
									
					numeratorForward=frac(numeratorForward , times(kIr1 , kMr2));
					denominator=sum(denominator, frac(p1p2, times( kIr1 , kMr2)));
					
					
					numeratorReverse=kcatn;
					if (modE.size() != 0) {
						numeratorReverse=times(numeratorReverse,new StringBuffer( modE.get(enzymeNum)));
						}
					numeratorReverse=times(numeratorReverse,new StringBuffer(specRefP1.getSpecies()));
					if (specRefP1.equals(specRefP2)) {
						numeratorReverse=pow(numeratorReverse, new StringBuffer('2'));
					} else {
						numeratorReverse=times(numeratorReverse, new StringBuffer('2'));
					}
					numeratorReverse=frac(numeratorReverse, times(kIp2,kMp1));
					numerator=diff(numeratorForward,numeratorReverse);
				} else {
					/*
					 * Reversible reaction: Bi-Uni reaction
					 */
					StringBuffer kcatp=new StringBuffer();
					StringBuffer kcatn=new StringBuffer();
					
					StringBuffer reactionNumber=new StringBuffer(reactionNum);
					StringBuffer kMr2 = concat(new StringBuffer("kM_") , reactionNumber);
					StringBuffer kMp1 = concat(new StringBuffer("kM_"), reactionNumber);
					StringBuffer kIr2 = concat(new StringBuffer("ki_"), reactionNumber);
					StringBuffer kIr1 = concat(new StringBuffer("ki_"), reactionNumber);
					
					
					if (modE.size() == 0) {
						kcatp = concat(new StringBuffer("Vp_"), reactionNumber);
						kcatn = concat(new StringBuffer("Vn_"), reactionNumber);
						} else {
						kcatp = concat(new StringBuffer("kcatp_"), reactionNumber);
						kcatn = concat(new StringBuffer("kcatn_"), reactionNumber);
					
						if (modE.size() > 1) {
							StringBuffer modEnzymeNumber=new StringBuffer(modE.get(enzymeNum));
							kcatp=concat(kcatp , new StringBuffer( Character.valueOf('_')) ,modEnzymeNumber);
							kcatn=concat(kcatn , new StringBuffer(Character.valueOf('_')) ,modEnzymeNumber);
							kMr2 =concat(kMr2  , new StringBuffer(Character.valueOf('_')) ,modEnzymeNumber);
							kMp1 =concat(kMp1 , new StringBuffer(Character.valueOf('_')) ,modEnzymeNumber);
							kIr2 =concat(kIr2 , new StringBuffer( Character.valueOf('_')) ,modEnzymeNumber);
							kIr1 =concat(kIr1 , new StringBuffer(Character.valueOf('_')) ,modEnzymeNumber);
							
							}
					}
					
					StringBuffer speciesE2=new StringBuffer(specRefE2.getSpecies());
					kMr2=sum(kMr2 , new StringBuffer(Character.valueOf('_')) ,speciesE2);
					kIr1=concat(kIr1 , new StringBuffer(Character.valueOf('_') ) ,speciesE2);
					kIr2=concat(kIr2 , new StringBuffer(Character.valueOf('_') ) ,speciesE2);
					kMp1=concat(kMp1 , new StringBuffer(Character.valueOf('_') ) ,speciesE2);
					
				
					if (specRefE2.equals(specRefE1)) {
						kIr1=concat(kIr1 , new StringBuffer("kip1") , new StringBuffer( kIr1.substring(2)));
						kIr2=concat(kIr2, new StringBuffer("kip2") , new StringBuffer(kIr2.substring(2)));
					
						}
					
					if (!listOfLocalParameters.contains(kcatp))
						listOfLocalParameters.add(kcatp);
					if (!listOfLocalParameters.contains(kcatn))
						listOfLocalParameters.add(kcatn);
					if (!listOfLocalParameters.contains(kMr2))
						listOfLocalParameters.add(kMr2);
					if (!listOfLocalParameters.contains(kMp1))
						listOfLocalParameters.add(kMp1);
					if (!listOfLocalParameters.contains(kIr2))
						listOfLocalParameters.add(kIr2);
					if (!listOfLocalParameters.contains(kIr1))
						listOfLocalParameters.add(kIr1);
					StringBuffer numeratorForward=new StringBuffer();					
					StringBuffer numeratorReverse=new StringBuffer();
					StringBuffer p1p2=new StringBuffer();
					
					numeratorForward = kcatp;
					if (modE.size() != 0) {
						numeratorForward=times(numeratorForward, new StringBuffer(modE.get(enzymeNum)));
						}

					denominator = sum(new StringBuffer('1'),
							frac(new StringBuffer( specRefE1.getSpecies()), kIr1),
									frac(new StringBuffer( specRefE2.getSpecies()), kIr2));
					if (specRefE1.equals(specRefE2)) {
						numeratorForward=times(numeratorForward,pow(new StringBuffer(specRefE1.getSpecies() ), new StringBuffer('2')));
						
						p1p2= pow(new StringBuffer(specRefE1.getSpecies()), new StringBuffer('2'));
					} else {
						numeratorForward=times(numeratorForward,new StringBuffer(specRefE1.getSpecies()), new StringBuffer(specRefE2.getSpecies()));
						p1p2=times(new StringBuffer( specRefE1.getSpecies() ), new StringBuffer(specRefE2.getSpecies()));
						}
					numeratorForward=frac(numerator,times(kIr1, kMr2)); 
					
					numeratorReverse=kcatn;
					if (modE.size() != 0) {
						numeratorReverse=times(numeratorReverse,new StringBuffer(modE.get(enzymeNum)));
						}
					numeratorReverse=times(numeratorReverse,frac(new StringBuffer(specRefP1.getSpecies() ),kMp1 ));
					numerator=diff(numeratorForward,numeratorReverse);
					denominator=sum(denominator, frac(p1p2, sum(times( kIr1 , kMr2), frac(new StringBuffer(specRefP1.getSpecies()),kMp1))));
					
					}
			}

			/*
			 * Construct formula
			 */
			formelTxt=frac(numerator ,denominator);
			if (enzymeNum < modE.size() - 1) {
				; 
				}
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);

		/*
		 * Activation
		 */
		if (!modActi.isEmpty()) {
			StringBuffer kA= new StringBuffer();
			for (int activatorNum = 0; activatorNum < modActi.size(); activatorNum++) {
				kA = concat("kA_" ,reactionNum , Character.valueOf('_'),modActi.get(activatorNum));
				if (!listOfLocalParameters.contains(kA))
					listOfLocalParameters.add(kA);
				acti=concat(acti ,
						frac(new StringBuffer(modActi.get(activatorNum)),sum(kA,new StringBuffer(modActi.get(activatorNum)))));
				}
		}

		/*
		 * Inhibition
		 */
		if (!modInhib.isEmpty()) {
			StringBuffer kI= new StringBuffer();
			for (int inhibitorNum = 0; inhibitorNum < modInhib.size(); inhibitorNum++) {
				
				concat("kI_" , reactionNum ,Character.valueOf('_'),modInhib.get(inhibitorNum));
				if (!listOfLocalParameters.contains(kI))
					listOfLocalParameters.add(kI);
				
				inhib=concat(inhib,
						frac(kI, sum(kI,new StringBuffer(modInhib.get(inhibitorNum)))));
				}
		}
		
		formelTxt =times(acti, inhib, formelTxt);
		
		return formelTxt;
	}

}
