package ode;

import java.util.HashMap;
import java.util.List;
import jp.sbi.celldesigner.plugin.PluginListOf;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class GMak extends KineticLaw{

	GMak(PluginModel mod, int reactionnum, HashMap<String, String> idAndName2, List<String> modActi, List<String> modInhib, List<String> modCat, boolean reversibility) 
	{
		this.reversibility = reversibility;
		idAndName.putAll(idAndName2);
		this.modActi.addAll(modActi);
		this.modCat.addAll(modCat);
		this.modInhib.addAll(modInhib);
		model=mod;
		this.reactionnum = reactionnum + 1;
		if(modCat.isEmpty())
			sMAK(-1);
		else
		{
		  for(int i = 0; i < modCat.size(); i++)
		    {
			  sMAK(i);
			  if((i+1)< modCat.size())
			  {
				  formeltex = formeltex + "+";
				  formeltxt = formeltxt + "+";
			  }
		    }
		}
	}
	
	public void sMAK(int catNumber) 
	{
		Species s = new Species();
		String c1 ="";
		String c2 ="";
		String kass;
		String kdiss;
		String inhib = "";
		String inhibtex = "";
		String acti = "";
		String actitex = "";
		String exp = "";
		PluginListOf listOfReactions = model.getListOfReactions();
		PluginReaction reaction = (PluginReaction)listOfReactions.get(reactionnum-1);
	    PluginListOf listOfReactants = reaction.getListOfReactants();
	    PluginListOf listOfProducts = reaction.getListOfProducts();
    
    	kass ="kass_" + reactionnum;
	    paraList.add(kass);
	    formeltxt = kass;
	    formeltex = "k_{\\text{+," + reactionnum + "}}";
		for (int j = 0; j < reaction.getNumReactants(); j++)
	    {
			exp = "";
			PluginSpeciesReference specref = (PluginSpeciesReference)listOfReactants.get(j);
			if(1 < (int)specref.getStoichiometry())
			{
				exp = "^" + (int)specref.getStoichiometry();
			}
	        formeltxt = formeltxt + " * " + idAndName.get(specref.getSpecies()) + exp;
	        formeltex = formeltex + "\\cdot " + s.fromSpeciesToTex(specref.getSpecies()) + exp;
	    }
		if((-1) < catNumber )
		{
			formeltxt = formeltxt + " * " + modCat.get(catNumber);
			formeltex = formeltex + "\\cdot " + s.fromSpeciesToTex(modCat.get(catNumber));
		}
		
//		nur das wird hinzugefügt, wenn Reaction reversibel	
		if(reaction.getReversible()==true||reversibility)
		{
			kdiss = "kdiss_" + reactionnum;
			paraList.add(kdiss);
			formeltxt =  formeltxt + " - "+ kdiss;
			formeltex =  formeltex + " - k_{\\text{-," + reactionnum + "}}";
			for (int j = 0; j < reaction.getNumProducts(); j++)
			{
				exp = "";
				PluginSpeciesReference specref = (PluginSpeciesReference)listOfProducts.get(j);
				if(1 < (int)specref.getStoichiometry())
				{
					exp = "^" + (int)specref.getStoichiometry();
				}
				formeltxt = formeltxt  + "*" + idAndName.get(specref.getSpecies()) + exp ;
				formeltex = formeltex  + "\\cdot " + s.fromSpeciesToTex(specref.getSpecies()) + exp ;
			}
			if((-1) < catNumber )
			{
				formeltxt = formeltxt + "*" + modCat.get(catNumber);
				formeltex = formeltex + "\\cdot " + s.fromSpeciesToTex(modCat.get(catNumber));
			}
		}
		if(modActi.isEmpty()==false)
		{
			c2 = ")";
			c1 = "(";
			for(int j = 0; j < modActi.size(); j++)
			{
				String kA = "kA_" + reactionnum + "_" + j;
				String kAtex = "k^{\\text{A}}_{\\text{" + reactionnum + "," + j + "}}";
				
				paraList.add("kA_" + reactionnum + "_" + j );
				acti = acti + "(" +modActi.get(j) + "/(" + kA + "+" + modActi.get(j) + "))*";
				actitex = actitex + "\\frac{" + s.fromSpeciesToTex(modActi.get(j)) + "}{" + kAtex + "+" + s.fromSpeciesToTex(modActi.get(j)) + "}\\cdot ";
			}
		}
		
		if(modInhib.isEmpty()==false)
		{
			c2 = ")";
			c1 = "(";
			for(int j = 0; j < modInhib.size(); j++)
			{
				String kI = "kI_" + reactionnum + "_" + j ;
				String kItex = "k^{\\text{I}}_{\\text{" + reactionnum + "," + j + "}}";
				
				paraList.add("kI_" + reactionnum + "_" + j );
				inhib = inhib + "(" + kI + "/(" + kI + "+" + modInhib.get(j) + "))*";
				inhibtex = inhibtex + "\\frac{" + kItex + "}{" + kItex + "+" + s.fromSpeciesToTex(modInhib.get(j)) + "}\\cdot ";
			}
			
		}
		formeltxt = acti + inhib + c1 + formeltxt + c2;
		formeltex = actitex + inhibtex + c1 +  formeltex + c2;
	}

	@Override
	public void sMAK() {
		// TODO Auto-generated method stub
		
	}
	
}
