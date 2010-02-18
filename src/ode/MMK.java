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
public class MMK extends KineticLaw{

	MMK(PluginModel mod, int reactionnum, HashMap<String, String> idAndName2, List<String> modE, List<String> modActi, List<String> modInhib, boolean reversibility) 
	{
		this.reversibility = reversibility;
		idAndName.putAll(idAndName2);
		model=mod;
		this.modE.addAll(modE);
		this.modActi.addAll(modActi);
		this.modInhib.addAll(modInhib);
		this.reactionnum = reactionnum + 1;
		sMAK();
	}

	public void sMAK() 
	{
		Species s = new Species();
		String minus = "";
		String zaehler = "";
		String zaehlertex = "";
		String nenner = "";	
		String nennertex = "";
		PluginListOf listOfReactions = model.getListOfReactions();
		PluginReaction reaction = (PluginReaction)listOfReactions.get(reactionnum-1);
		PluginListOf listOfReactants = reaction.getListOfReactants();
		PluginListOf listOfProducts = reaction.getListOfProducts();
		
		PluginSpeciesReference specrefe = (PluginSpeciesReference)listOfReactants.get(0);
		PluginSpeciesReference specrefp = (PluginSpeciesReference)listOfProducts.get(0);
		
		
		for(int i = 0; i < modE.size(); i++)
		{	
			String kAa = "",kAb = "",kIa = "",kIb = "",zkMs = "",nkMs = "",kMp = "";
			String kAatex = "", kAbtex = "", kIatex = "", kIbtex = "", zkMstex = "", kMptex = "", nkMstex = "";
			String kcatp = ("kcatp_" + reactionnum + "_" + i);
			String kcatn = ("kcatn_" + reactionnum + "_" + i); 
			String kcatptex = ("k^{\\text{cat}}_{\\text{+," + reactionnum + "," + i + "}}");
			String kcatntex = ("k^{\\text{cat}}_{\\text{-," + reactionnum + "," + i + "}}");
			String Vp = kcatp + "*" + modE.get(i) + "*" + specrefe.getSpecies() ;
			String Vptex = kcatptex + "\\cdot " + s.fromSpeciesToTex(modE.get(i)) + "\\cdot " + s.fromSpeciesToTex(specrefe.getSpecies()) ;
			String Vn = "", Vntex = "";
			String kMs = ("kMs_" +reactionnum + "_" + i);
			String kMstex = ("k^{\\text{Ms}}_{\\text{" +reactionnum + "," + i + "}}");
			
			
			paraList.add(kMs);
			paraList.add(kcatp);
			
			if(modActi.isEmpty()==false)
			{
				kAa = ("*(1+" + "kAa_" + reactionnum + "_" + i + "/" + modActi.get(0) + ")");
				kAb = ("*(1+" + "kAb_" + reactionnum + "_" + i + "/" + modActi.get(0) + ")");
				
				kAatex = ("\\cdot \\left(1+" + "\\frac{k^{\\text{Aa}}_{\\text{" + reactionnum + "," + i + "}}}{" + s.fromSpeciesToTex(modActi.get(0)) + "}\\right)");
				kAbtex = ("\\cdot \\left(1+" + "\\frac{k^{\\text{Ab}}_{\\text{" + reactionnum + "," + i + "}}}{" + s.fromSpeciesToTex(modActi.get(0)) + "}\\right)");
			
				paraList.add("kAa_" + reactionnum + "_" + i);
				paraList.add("kAb_" + reactionnum + "_" + i);
			}
			if(reaction.getReversible()==true||reversibility)
			{				
				minus = "-";
				Vn = kcatn + "*" + modE.get(i) + "*"+ specrefp.getSpecies();
				Vntex = kcatntex + "\\cdot " + s.fromSpeciesToTex(modE.get(i)) + "\\cdot "+ s.fromSpeciesToTex(specrefp.getSpecies());
				kMp = ("*kMp_" +reactionnum + "_" + i);
				kMptex = ("\\cdot k^{\\text{Mp}}_{\\text{" +reactionnum + "," + i + "}}");
				zkMs = "*" + kMs;
				zkMstex = "\\cdot " + kMstex;
				nkMs = ("+" + specrefp.getSpecies() + "*" + kMs);
				nkMstex = ("+" + s.fromSpeciesToTex(specrefp.getSpecies()) + "\\cdot " + kMstex);
				
				paraList.add(kcatn);
				paraList.add("kMp_" +reactionnum + "_" + i);
			}
			if(modInhib.isEmpty()==false)
			{
				paraList.add("kIa_" + reactionnum + "_" + i);
				paraList.add("kIb_" +reactionnum + "_" + i);
				
				kIa = ("*(1+" + modInhib.get(0) + "/kIa_" + reactionnum + "_" + i + ")");
				kIb = ("*(1+" + modInhib.get(0) + "/kIb_" + reactionnum + "_" + i + ")");
				kIatex = ("\\cdot \\left(1+ \\frac{" + s.fromSpeciesToTex(modInhib.get(0)) + "}{k^{\\text{Ia}}_{\\text{" + reactionnum + "," + i + "}}}\\right)");
				kIbtex = ("\\cdot \\left(1+ \\frac{" + s.fromSpeciesToTex(modInhib.get(0)) + "}{k^{\\text{Ib}}_{\\text{" + reactionnum + "," + i + "}}}\\right)");
				
				//Vn = "((" + kcatn + "*" + modE.get(i) + "*"  + specrefp.getSpecies();
				//Vp = "(" + kcatp + "*" + modE.get(i) +  "*"  + specrefe.getSpecies();	
				//Vntex = kcatntex + "\\cdot " + modE.get(i) + "\\cdot "+ specrefp.getSpecies();
				//Vptex = kcatptex + "\\cdot " + modE.get(i) + "\\cdot " + specrefe.getSpecies() ;
				
				
				if(reaction.getReversible()==true||reversibility)
				{
					zkMs = zkMs + ")";
					//zkMstex = zkMstex + "\\right)";
					zkMstex = zkMstex;
				}
			}
			if(reaction.getReversible()==false && reversibility==false)
			{
				Vn = "";
				Vntex = "";
			}

			zaehler = Vp + kMp + minus + Vn  + zkMs;
			zaehlertex = Vptex + kMptex + minus + Vntex  + zkMstex;

			nenner = kMs + kMp + kIa + kAa + "+(" + specrefe.getSpecies() + kMp + nkMs + ")" + kIb + kAb;
			nennertex = kMstex + kMptex + kIatex + kAatex + "+\\left(" + s.fromSpeciesToTex(specrefe.getSpecies()) + kMptex + nkMstex + "\\right)" + kIbtex + kAbtex;

			
			if(i == (modE.size()-1))
			{
				formeltxt = formeltxt + "(" + zaehler + ")/(" + nenner + ")" ;
				formeltex = formeltex + "\\frac{" + zaehlertex + "}{" + nennertex + "}" ;
			}
			else
			{
				formeltxt = formeltxt + "(" + zaehler + ")/(" + nenner + ") + ";
				formeltex = formeltex + "\\frac{" + zaehlertex + "}{" + nennertex + "} + ";
			}
		}
	}

	@Override
	public void sMAK(int catNumber) {
		// TODO Auto-generated method stub
		
	}	
	
}
