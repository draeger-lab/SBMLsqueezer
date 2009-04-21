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
public class PingPongMechanism extends GeneralizedMassAction {

  /**
   * @param parentReaction
   * @param model
   * @param reversibility
   * @throws RateLawNotApplicableException
 * @throws IOException 
   */
  public PingPongMechanism(PluginReaction parentReaction, PluginModel model,
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
  public PingPongMechanism(PluginReaction parentReaction, PluginModel model,
      List<String> listOfPossibleEnzymes) throws RateLawNotApplicableException, IOException {
    super(parentReaction, model, listOfPossibleEnzymes);
  }

  public static boolean isApplicable(PluginReaction reaction) {
    // TODO
    return true;
  }

  @Override
  public String getName() {
    // according to Cornish-Bowden: Fundamentals of Enzyme kinetics
    String name = "substituted-enzyme mechanism (Ping-Pong)";
    if (getParentReaction().getReversible()) return "reversible " + name;
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
	        .getListOfReactants().get(0);
	    PluginSpeciesReference specRefP1 = (PluginSpeciesReference) reaction
	        .getListOfProducts().get(0);
	    PluginSpeciesReference specRefE2 = null, specRefP2 = null;
	
	    if (reaction.getNumReactants() == 2)
	      specRefE2 = (PluginSpeciesReference) reaction.getListOfReactants().get(1);
	    else if (specRefE1.getStoichiometry() == 2.0)
	      specRefE2 = specRefE1;
	    else throw new RateLawNotApplicableException(
	        "Number of reactants must equal two to apply ping-pong "
	            + "Michaelis-Menten kinetics to reaction " + reaction.getId());
	
	    boolean exception = false;
	    switch (reaction.getNumProducts()) {
	    case 1:
	      if (specRefP1.getStoichiometry() == 2.0)
	        specRefP2 = specRefP1;
	      else exception = true;
	      break;
	    case 2:
	      specRefP2 = (PluginSpeciesReference) reaction.getListOfProducts().get(1);
	      break;
	    default:
	      exception = true;
	      break;
	    }
	    if (exception)
	      throw new RateLawNotApplicableException(
	          "Number of products must equal two to apply ping-pong"
	              + "Michaelis-Menten kinetics to reaction " + reaction.getId());
	
	    int enzymeNum = 0;
	    reactionNum++;
	    do {
	      StringBuffer kcatp=new StringBuffer() ;
	      StringBuffer kMr1 = concat("kM_" , reactionNum);
	      StringBuffer kMr2 = concat("kM_" , reactionNum);
	     
	      if (modE.size() == 0) {
	        kcatp = concat("Vp_", reactionNum);
	        
	      } else {
	        kcatp = concat("kcatp_" , reactionNum);
	       
	        if (modE.size() > 1) {
	          kcatp= concat(kcatp,"_" , modE.get(enzymeNum));
	          kMr1 = concat(kMr1,"_" , modE.get(enzymeNum));
	          kMr2 = concat(kMr2,"_" , modE.get(enzymeNum));
	         
	        }
	      }
	      kMr2 =concat(kMr2,"_", specRefE2.getSpecies());
	      kMr1 =concat(kMr1,"_", specRefE2.getSpecies());
	      if (specRefE2.equals(specRefE1)) {
	        kMr1 = concat( "kMr1" ,kMr1.substring(2));
	        kMr2 = concat( "kMr2" , kMr2.substring(2));
	       
	      }
	     
	
	      if (!listOfLocalParameters.contains(kcatp)) listOfLocalParameters.add(kcatp);
	      if (!listOfLocalParameters.contains(kMr2)) listOfLocalParameters.add(kMr2);
	      if (!listOfLocalParameters.contains(kMr1)) listOfLocalParameters.add(kMr1);
	
	      /*
	       * Irreversible Reaction
	       */
	      if (!reaction.getReversible()) {
	        numerator = kcatp ;
	       
	        if (modE.size() > 0) {
	          numerator= times (numerator, new StringBuffer(modE.get(enzymeNum)));
	          
	        }
	        numerator= times (numerator, new StringBuffer(specRefE1.getSpecies()));
	     
	        denominator = times(kMr2,sum(new StringBuffer( specRefE1.getSpecies()), kMr1),sum(new StringBuffer( specRefE2.getSpecies()),
           new StringBuffer(specRefE1.getSpecies())));
	        
	        if (specRefE2.equals(specRefE1)) {
	        	 numerator= pow (numerator, new StringBuffer('2'));
	        	 denominator= pow (denominator, new StringBuffer('2'));
	           
	          
	        } else {
	        	 numerator= times (numerator, new StringBuffer(specRefE2.getSpecies()));
	        	 denominator= times (denominator, new StringBuffer(specRefE2.getSpecies()));
		    
	         
	        }
	
	        /*
	         * Reversible Reaction
	         */
	      } else {
	    	  

			  StringBuffer numeratorForward=new StringBuffer();					
			  StringBuffer numeratorReverse=new StringBuffer();
			  
				
	    	  StringBuffer kcatn;
	    	  StringBuffer kMp1 =new StringBuffer("kM_" + reactionNum);
	    	  StringBuffer kMp2 =new StringBuffer( "kM_" + reactionNum);
	    	  StringBuffer kIp1 =new StringBuffer( "ki_" + reactionNum);
	    	  StringBuffer kIp2 =new StringBuffer( "ki_" + reactionNum);
	    	  StringBuffer kIr1 = new StringBuffer("ki_" + reactionNum);
	       
	
	        if (modE.size() == 0) {
	          kcatn = new StringBuffer("Vn_" + reactionNum);
	          
	        } else {
	          kcatn = new StringBuffer("kcatn_" + reactionNum);	         
	          if (modE.size() > 1) {
	            StringBuffer modEnzymeNumber=new StringBuffer(modE.get(enzymeNum));
			    kcatn=concat(kcatn , '_' ,modEnzymeNumber);
	            kMp1 =concat(kMp1 , '_' ,modEnzymeNumber);
	            kMp2 =concat(kMp2 , '_' ,modEnzymeNumber);
	            kIp1 =concat(kIp1 , '_' ,modEnzymeNumber);
	            kIp2 =concat(kIp2 , '_' ,modEnzymeNumber);
	            kIr1 =concat(kIr1 , '_' ,modEnzymeNumber);
	            }
	        }
	        kMp1 =concat(kMp1 , '_',specRefP1.getSpecies());
	       
	        kMp2=concat(kMp2,"_" + specRefP2.getSpecies());
	        kIp1 =concat(kIp1,"_" + specRefP1.getSpecies());
	        kIp2=concat(kIp2,"_" + specRefP2.getSpecies());
	        kIr1 =concat(kIr1 , "_" + specRefE1.getSpecies());
	        if (specRefP2.equals(specRefP1)) {
	          kMp1 =concat( "kMp1" , kMp1.substring(2));
	          kMp2 =concat( "kMp2" , kMp2.substring(2));
	          kIp1 =concat( "kip1" , kIp1.substring(2));
	          kIp2 =concat("kip2" , kIp2.substring(2));
	          
	        }
	       
	
	        if (!listOfLocalParameters.contains(kcatn)) listOfLocalParameters.add(kcatn);
	        if (!listOfLocalParameters.contains(kMp2)) listOfLocalParameters.add(kMp2);
	        if (!listOfLocalParameters.contains(kMp1)) listOfLocalParameters.add(kMp1);
	        if (!listOfLocalParameters.contains(kIp1)) listOfLocalParameters.add(kIp1);
	        if (!listOfLocalParameters.contains(kIp2)) listOfLocalParameters.add(kIp2);
	        if (!listOfLocalParameters.contains(kIr1)) listOfLocalParameters.add(kIr1);
	
	        numeratorForward = frac( kcatp , times( kIr1 ,  kMr2 ));
	          if (modE.size() > 0) {
	        	  numeratorForward =times (numeratorForward, new StringBuffer(modE.get(enzymeNum)));
	          }
	           StringBuffer numerator_s1s2=new StringBuffer(specRefE1.getSpecies());
	           StringBuffer denominator_s1s2=new StringBuffer(specRefE1.getSpecies());
	           denominator = sum( frac(new StringBuffer(specRefE1.getSpecies()), kIr1)
	             , frac(times(kMr1 ,new StringBuffer(specRefE2.getSpecies())),
	            times( kIr1,kMr2 )) ,
	            frac( new StringBuffer(specRefP1.getSpecies()) , kIp1),
	            frac(times( kMp1 , new StringBuffer(specRefP2.getSpecies())),times( kIp1, kMp2) ));
	        if (specRefE2.equals(specRefE1)) {
	        	numerator_s1s2 =pow (numerator_s1s2, new StringBuffer('2')); 
	        	numeratorForward =times (numeratorForward, numerator_s1s2); 
	        	denominator_s1s2=pow(new StringBuffer(specRefE1.getSpecies()), new StringBuffer('2')); 
	        	
	        } else {
	        	numeratorForward =times (numeratorForward,numerator_s1s2, new StringBuffer(specRefE2.getSpecies())); 
	        	denominator_s1s2=times(new StringBuffer(specRefE1.getSpecies()),new StringBuffer(specRefE2.getSpecies()));
	         
	            }
	        numeratorReverse = frac(kcatn ,times(kIp1, kMp2));
	        denominator =sum(denominator, frac(denominator_s1s2,times(kIr1, kMr2)));
	        denominator =sum(denominator, frac(times(new StringBuffer(specRefE1.getSpecies()),new StringBuffer(specRefP1.getSpecies())),times(kIr1, kIp1)),
	        		frac(times(kMr1,new StringBuffer(specRefE2.getSpecies()),new StringBuffer(specRefP2.getSpecies())), times(kIr1,kMr2,kIp2)));
	            
	             if (modE.size() > 0) {
	            	 numeratorReverse = times(numeratorReverse, new StringBuffer(modE.get(enzymeNum)));
	         }
	           
	             StringBuffer numerator_p1p2=new StringBuffer(specRefP1.getSpecies());
	             StringBuffer denominator_p1p2=new StringBuffer(specRefE1.getSpecies());
	         if (specRefP2.equals(specRefP1)) {
	        	 numerator_p1p2=pow(numerator_p1p2,new StringBuffer('2'));
	        	 numeratorReverse = times(numeratorReverse,numerator_p1p2);
	        	 denominator_p1p2=pow(new StringBuffer(specRefP1.getSpecies()),new StringBuffer('2'));
	        	 
	          } else {
	        	  numeratorReverse = times(numeratorReverse,new StringBuffer(specRefP2.getSpecies()));
	             
	        	  denominator_p1p2=times(new StringBuffer(specRefP1.getSpecies()),new StringBuffer(specRefP2.getSpecies()));

	          }
	         numerator= diff(numeratorForward,numeratorReverse);
	         denominator_p1p2=frac(denominator_p1p2,times(kIp1,kMp2));
	        denominator=sum(denominator, denominator_p1p2);
	       }
	
	      /*
	       * Construct formula.
	       */
	      formelTxt= frac(numerator,denominator);
	        if (enzymeNum < (modE.size() - 1)) {
	       ;
	        }
	      enzymeNum++;
	    } while (enzymeNum <= modE.size() - 1);
	
	    /*
	     * Activation
	     */
	    if (!modActi.isEmpty()) {
	      for (int activatorNum = 0; activatorNum < modActi.size(); activatorNum++) {
	        StringBuffer kA = concat("kA_", reactionNum , '_' , modActi.get(activatorNum));
	        if (!listOfLocalParameters.contains(kA)) listOfLocalParameters.add(kA);
	          
	        acti=concat(acti ,
					frac(new StringBuffer(modActi.get(activatorNum)),sum(kA,new StringBuffer(modActi.get(activatorNum)))));
			
	         }
	    }
	    /*
	     * Inhibition
	     */
	    if (!modInhib.isEmpty()) {
	      for (int inhibitorNum = 0; inhibitorNum < modInhib.size(); inhibitorNum++) {
	    	  StringBuffer kI = concat("kI_",reactionNum ,'_', modInhib.get(inhibitorNum));
	       
	        listOfLocalParameters.add(kI);
	        inhib=concat(inhib,
					frac(kI, sum(kI,new StringBuffer(modInhib.get(inhibitorNum)))));
			
	        }
	    }
	    if ((acti.length() + inhib.length() > 0) && (modE.size() > 1)) {
	    	formelTxt = concat(acti,'(',inhib, formelTxt,')');
	      }else{
	    	  formelTxt = concat(acti,inhib, formelTxt);
	      }
	   
		return formelTxt;
	  }

}
