package ode;


import java.util.HashMap;
import java.util.List;
import jp.sbi.celldesigner.plugin.*;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class Convenience extends KineticLaw {

	Convenience(PluginModel mod, int reactionnum, HashMap<String, String> idAndName2, List<String> modE, List<String> modActi, List<String> modInhib, boolean reversibility) 
	{
		this.reversibility = reversibility;
		idAndName.putAll(idAndName2);
		this.modE.addAll(modE);
		this.modActi.addAll(modActi);
		this.modInhib.addAll(modInhib);
		model=mod;
		this.reactionnum = reactionnum + 1;
		sMAK();
	}

	public void sMAK() 
	{	
		Species s = new Species();
		String nenner = "";
		String nennertex ="";
		String zaehler = "";
		String zaehlertex = "";
		String inhib = "";
		String inhibtex = "";
		String acti = "";
		String actitex = "";
		
		PluginListOf listOfReactions = model.getListOfReactions();
		PluginReaction reaction = (PluginReaction)listOfReactions.get(reactionnum-1);
		PluginListOf listOfReactants = reaction.getListOfReactants();
		PluginListOf listOfProducts = reaction.getListOfProducts();
    
		for(int i = 0; i < modE.size(); i++)
		{
			// sums for each educt
			zaehler = "kcatp_" + reactionnum + "_" + i;
			zaehlertex = "k^{\\text{cat}}_{\\text{+," + reactionnum +","+ i + "}}";
			nenner = "";
			nennertex = "";
			paraList.add("kcatp_" + reactionnum +"_"+ i);
			for (int j = 0; j < reaction.getNumReactants(); j++)
			{	
				paraList.add("kMa_" + reactionnum + "_" + j + "_" + i);
				String exp = "";
				PluginSpeciesReference specref = (PluginSpeciesReference)listOfReactants.get(j);
				// build denumerator
				String kMa = "kMa_" + reactionnum + "_" + j + "_" + i;
				String kMatex = "k^{\\text{Ma}}_{\\text{" + reactionnum + "," + i + ","+ i + "}}";
				nenner = nenner + " 1 + " + specref.getSpecies() + "/" + kMa ;	
				nennertex = nennertex + "\\left( 1 + \\frac{" + s.fromSpeciesToTex(specref.getSpecies()) + "}{" + kMatex  + "}";
				for(int k = 1; k < (int)specref.getStoichiometry(); k++) 
				{
					exp = "^" +(k+1);
					nenner = nenner + " + (" + specref.getSpecies()+"/" + kMa + ")" + exp;
					nennertex = nennertex + " + \\left(\\frac{" + s.fromSpeciesToTex(specref.getSpecies())+"}{" + kMatex + "}\\right)" + exp;
				}
				nennertex = nennertex + "\\right)";
				if((j+1) < reaction.getNumReactants())
				{
					nenner = nenner +"*";
					nennertex = nennertex + "\\cdot ";
				}
				
				// build numerator		
				zaehler = zaehler +  "*(" + specref.getSpecies()+"/" + kMa + ")" + exp;
				zaehlertex = zaehlertex +  "\\cdot \\left(\\frac{" + s.fromSpeciesToTex(specref.getSpecies())+"}{" + kMatex + "}\\right)" + exp;
			}
			//only if reaction is reversible
			if(reaction.getReversible()==true||reversibility){
				nenner = nenner + " + ";
				nennertex = nennertex + " + ";
				zaehler = zaehler + " - kcatn_" + reactionnum +"_"+ i;
				zaehlertex = zaehlertex + " - k^{\\text{cat}}_{\\text{-," + reactionnum +","+ i + "}}";
				paraList.add("kcatn_" + reactionnum +"_"+ i);
				// Sums for each product	
				for (int j = 0; j < reaction.getNumProducts(); j++)
				{
					String kMb = "kMb_" + reactionnum + "_" + j + "_"+ i;
					String kMbtex = "k^{\\text{Mb}}_{\\text{" + reactionnum + "," + j + "," + i + "}}";
					paraList.add("kMb_" + reactionnum + "_" + j +"_"+ i);
					String exp = "";
					PluginSpeciesReference specref = (PluginSpeciesReference)listOfProducts.get(j);
					nenner = nenner + " 1 + " + specref.getSpecies() + "/" + kMb;
					nennertex = nennertex + "\\left( 1 + \\frac{" + s.fromSpeciesToTex(specref.getSpecies()) + "}{" + kMbtex + "}";
					for(int k = 1; k < (int)specref.getStoichiometry(); k++) 
					{
						exp = "^" +(k+1);
						nenner = nenner + " + (" + specref.getSpecies()+"/" + kMb + ")" + exp;
						nennertex = nennertex + " + \\left(\\frac{" + s.fromSpeciesToTex(specref.getSpecies())+"}{" + kMbtex + "}\\right)" + exp;
					}
					nennertex = nennertex + "\\right)";
					if((j+1) < reaction.getNumProducts())
					{
						nenner = nenner + "*";
						nennertex = nennertex + "\\cdot ";
					}
					zaehler = zaehler + "*(" + specref.getSpecies()+"/" + kMb + ")" + exp;
					zaehlertex = zaehlertex + "\\cdot \\left(\\frac{" + s.fromSpeciesToTex(specref.getSpecies())+"}{" + kMbtex + "}\\right)" + exp;
				}
			nenner = nenner + " -1";
			nennertex = nennertex + " -1";
			}
			if(modActi.isEmpty()==false)
			{
				for(int j = 0; j < modActi.size(); j++)
				{
					String kA = "kA_" + reactionnum + "_" + i + "_" + j ;
					String kAtex = "k^{\\text{A}}_{\\text{" + reactionnum + "," + i + "," + j + "}}";
					
					paraList.add("kA_" + reactionnum +"_"+ i+"_"+ j );
					acti = acti + "(" +modActi.get(j) + "/(" + kA + "+" + modActi.get(j) + "))*";
					actitex = actitex + "\\frac{" + s.fromSpeciesToTex(modActi.get(j)) + "}{" + kAtex + "+" + s.fromSpeciesToTex(modActi.get(j)) + "}\\cdot";
				}
			}
			if(modInhib.isEmpty()==false)
			{
				for(int j = 0; j < modInhib.size(); j++)
				{
					String kI = "kI_" + reactionnum +"_"+ i+"_"+ j;
					String kItex = "k^{\\text{I}}_{\\text{" + reactionnum +","+ i+","+ j + "}}";
					
					paraList.add("kI_" + reactionnum +"_"+ i+"_"+ j );
					inhib = inhib + "(" + kI + "/(" + kI + "+" + modInhib.get(j) + "))*";
					inhibtex = inhibtex + "\\frac{" +kItex + "}{" + kItex + "+" + s.fromSpeciesToTex(modInhib.get(j)) + "}\\cdot ";
				}		
			}
			if(i == (modE.size()-1))
			{
				formeltxt =  formeltxt + acti + inhib + modE.get(i) + "*(" + zaehler + ")" + "/(" + nenner + ")"; 
				formeltex  = formeltex + actitex + inhibtex + s.fromSpeciesToTex(modE.get(i)) + "\\cdot \\frac{" + zaehlertex + "}{" + nennertex + "}"; 
			}
			else
			{
				formeltxt =  formeltxt + acti + inhib + modE.get(i) + "*(" + zaehler + ")" + "/(" + nenner + ") + ";
				formeltex  = formeltex + actitex + inhibtex + s.fromSpeciesToTex(modE.get(i)) + "\\cdot \\frac{" + zaehlertex + "}{" + nennertex + "} + ";
			}
		}
	}

	public void sMAK(int catNumber) {
		// TODO Auto-generated method stub
	}
}
