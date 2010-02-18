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
public class Ordered extends KineticLaw {

	
	public Ordered(PluginModel mod, int reactionnum, HashMap<String, String> idAndName2, List<String> modE, List<String> modActi, List<String> modInhib, boolean biuni, boolean reversibility) 
	{
		this.reversibility = reversibility;
		idAndName.putAll(idAndName2);
		model=mod;
		this.biuni = biuni;
		this.modE.addAll(modE);
		this.modActi.addAll(modActi);
		this.modInhib.addAll(modInhib);
		this.reactionnum = reactionnum + 1;
		sMAK();
	}

	private boolean biuni;
	
	@Override
	public void sMAK() 
	{
		Species s = new Species();
		String zaehler = "", zaehlertex = "";	//	I
		String nenner = "", nennertex = "";	//	II
		String inhib = "";
		String inhibtex = "";
		String acti = "";
		String actitex = "";
		
		PluginListOf listOfReactions = model.getListOfReactions();
		PluginReaction reaction = (PluginReaction)listOfReactions.get(reactionnum-1);
		PluginListOf listOfReactants = reaction.getListOfReactants();
		PluginListOf listOfProducts = reaction.getListOfProducts();
		
		PluginSpeciesReference specrefe1 = (PluginSpeciesReference)listOfReactants.get(0);
		PluginSpeciesReference specrefp1 = (PluginSpeciesReference)listOfProducts.get(0);
		PluginSpeciesReference specrefe2 = (PluginSpeciesReference)listOfReactants.get(1);
		PluginSpeciesReference specrefp2 = (PluginSpeciesReference)listOfProducts.get(1);
		
		for(int i = 0; i < modE.size(); i++)
		{
			String kcatp = ("kcatp_" + reactionnum + "_" + i);
			String kMr1 = ("kMr1_" +reactionnum + "_" + i);
			String kMr2 = ("kMr2_" +reactionnum + "_" + i);
			String kIr1 = ("kIr1_" + reactionnum + "_" + i);
			String kcatptex = ("k^{\\text{cat}}_{\\text{+," + reactionnum + "," + i + "}}");
			String kMr1tex = ("k^{\\text{Mr1}}_{\\text{" +reactionnum + "," + i + "}}");
			String kMr2tex = ("k^{\\text{Mr2}}_{\\text{" +reactionnum + "," + i + "}}");
			String kIr1tex = ("k^{\\text{Ir1}}_{\\text{" + reactionnum + "," + i + "}}");
			//
			
			paraList.add(kcatp);
			paraList.add(kMr2);
			paraList.add(kMr1);
			paraList.add(kIr1);
			//
			
			zaehler = kcatp + "*"+ modE.get(i) + "*" + specrefe1.getSpecies()+ "*" + specrefe2.getSpecies();
			zaehlertex = kcatptex + "\\cdot "+ s.fromSpeciesToTex(modE.get(i)) + "\\cdot " + s.fromSpeciesToTex(specrefe1.getSpecies())+ "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies());;	
			nenner = kIr1 + "*" + kMr2 + "+ " + kMr2 + "*" + specrefe1.getSpecies() + "+" 
				+ kMr1 + "*" + specrefe2.getSpecies() + "+" + specrefe1.getSpecies() + "*" + specrefe2.getSpecies();
			nennertex = kIr1tex + "\\cdot " + kMr2tex + "+ " + kMr2tex + "\\cdot " + s.fromSpeciesToTex(specrefe1.getSpecies()) + "+" 
			+ kMr1tex + "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies()) + "+" + s.fromSpeciesToTex(specrefe1.getSpecies()) + "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies());;

			if(reaction.getReversible()==true||reversibility)
			{
				if( biuni==false)
				{
					String kcatn = ("kcatn_" + reactionnum + "_" + i);
					String kMp1 = ("kMp1_" +reactionnum + "_" + i);
					String kMp2 = ("kMp2_" +reactionnum + "_" + i);
					String kIp1 = ("kIp1_" + reactionnum + "_" + i);
					String kIp2 = ("kIp2_" + reactionnum + "_" + i);
					String kIr2 = ("kIr2_" + reactionnum + "_" + i);
					String kcatntex = ("k^{\\text{cat}}_{\\text{-," + reactionnum + "," + i + "}}");
					String kMp1tex = ("k^{\\text{Mp1}}_{\\text{" +reactionnum + "," + i + "}}");
					String kMp2tex = ("k^{\\text{Mp2}}_{\\text{" +reactionnum + "," + i + "}}");
					String kIp1tex = ("k^{\\text{Ip1}}_{\\text{" + reactionnum + "," + i + "}}");
					String kIp2tex = ("k^{\\text{Ip2}}_{\\text{" + reactionnum + "," + i + "}}");
					String kIr2tex = ("k^{\\text{Ir2}}_{\\text{" + reactionnum + "," + i + "}}");
					
					paraList.add(kIr2);
					paraList.add(kcatn);
					paraList.add(kMp2);
					paraList.add(kMp1);
					paraList.add(kIp1);
					paraList.add(kIp2);
					
					zaehler = "(" + kcatp + "*"+ modE.get(i) + "*" + specrefe1.getSpecies()+ "*" + specrefe2.getSpecies() 
						+ ")/("+ kIr1 + "*" + kMr2 + ")"+ " - (" + kcatn+ "*"+ modE.get(i) + "*" + specrefp1.getSpecies()
						+ "*" + specrefp2.getSpecies() + ")/(" + kIp2 + "*" + kMp1 + ")";	
					zaehlertex = "\\frac{" + kcatptex + "\\cdot "+ s.fromSpeciesToTex(modE.get(i)) + "\\cdot " + s.fromSpeciesToTex(specrefe1.getSpecies())+ "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies()) 
						+ "}{"+ kIr1tex + "\\cdot " + kMr2tex + "}"+ " - \\frac{" + kcatntex+ "\\cdot "+ s.fromSpeciesToTex(modE.get(i)) + "\\cdot " + s.fromSpeciesToTex(specrefp1.getSpecies())+ "\\cdot " 
						+ s.fromSpeciesToTex(specrefp2.getSpecies()) + "}{" + kIp2tex + "\\cdot " + kMp1tex + "}";
						
					nenner = "1 + " + "(" + specrefe1.getSpecies() + "/" + kIr1 + ")"
						+ " + (" + kMr1 + "*" + specrefe2.getSpecies() + "/" + kIr1 + "*" + kMr2 + ")"
						+ " + (" + kMp2 + "*" + specrefp1.getSpecies() + "/" + kIp2 + "*" + kMp1 + ")"
						+ " + (" + specrefp2.getSpecies() + "/" + kIp2 + ")"
						+ " + (" + specrefe1.getSpecies() + "*" + specrefe2.getSpecies() + "/" + kIr1 + "*" + kMr2 + ")"
						+ " + (" + kMp2 + "*" + specrefe1.getSpecies() + "*" + specrefp1.getSpecies() + "/" + kIr1 + "*" + kMp1 + "*" + kIp2 + ")"
						+ " + (" + kMr1 + "*" + specrefe2.getSpecies() + "*" + specrefp2.getSpecies() + "/" + kIr1 + "*" + kMr2 + "*" + kIp2 + ")"
						+ " + (" + specrefp1.getSpecies() + "*" + specrefp2.getSpecies() + "/" + kMp1 + "*" + kIp2+ ")"
						+ " + (" + specrefe1.getSpecies() + "*" + specrefe2.getSpecies() + "*" + specrefp1.getSpecies() + "/" + kIr1 + "*" + kMr2 + "*" + kIp1 + ")"
						+ " + (" + specrefe2.getSpecies() + "*" + specrefp1.getSpecies() + "*" + specrefp2.getSpecies() + "/" + kIr2 + "*" + kMp1 + "*" + kIp2 + ")"
						;	
					nennertex = "1 + " + "\\frac{" + s.fromSpeciesToTex(specrefe1.getSpecies()) + "}{" + kIr1tex + "}"
						+ " + \\frac{" + kMr1tex + "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies()) + "}{" + kIr1tex + "\\cdot " + kMr2tex + "}"
						+ " + \\frac{" + kMp2tex + "\\cdot " + s.fromSpeciesToTex(specrefp1.getSpecies()) + "}{" + kIp2tex + "\\cdot " + kMp1tex + "}"
						+ " + \\frac{" + s.fromSpeciesToTex(specrefp2.getSpecies()) + "}{" + kIp2tex + "}"
						+ " + \\frac{" + s.fromSpeciesToTex(specrefe1.getSpecies()) + "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies()) + "}{" + kIr1tex + "\\cdot " + kMr2tex + "}"
						+ " + \\frac{" + kMp2tex + "\\cdot " + s.fromSpeciesToTex(specrefe1.getSpecies()) + "\\cdot " + s.fromSpeciesToTex(specrefp1.getSpecies()) + "}{" + kIr1tex + "\\cdot " + kMp1tex + "\\cdot " + kIp2tex + "}"
						+ " + \\frac{" + kMr1tex + "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies()) + "\\cdot " + s.fromSpeciesToTex(specrefp2.getSpecies()) + "}{" + kIr1tex + "\\cdot " + kMr2tex + "\\cdot " + kIp2tex + "}"
						+ " + \\frac{" + s.fromSpeciesToTex(specrefp1.getSpecies()) + "\\cdot " + s.fromSpeciesToTex(specrefp2.getSpecies()) + "}{" + kMp1tex + "\\cdot " + kIp2tex+ "}"
						+ " + \\frac{" + s.fromSpeciesToTex(specrefe1.getSpecies()) + "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies()) + "\\cdot " + s.fromSpeciesToTex(specrefp1.getSpecies()) + "}{" + kIr1tex + "\\cdot " + kMr2tex + "\\cdot " + kIp1tex + "}"
						+ " + \\frac{" + s.fromSpeciesToTex(specrefe2.getSpecies()) + "\\cdot " + s.fromSpeciesToTex(specrefp1.getSpecies()) + "\\cdot " + s.fromSpeciesToTex(specrefp2.getSpecies()) + "}{" + kIr2tex + "\\cdot " + kMp1tex + "\\cdot " + kIp2tex + "}"
						;	
				}
	
				if( biuni==true)
				{
					String kcatn = ("kcatn_" + reactionnum + "_" + i);
					String kMp1 = ("kMp1_" +reactionnum + "_" + i);
					String kIp1 = ("kIp1_" + reactionnum + "_" + i);
					String kcatntex = ("k^{\\text{cat}}_{\\text{-," + reactionnum + "," + i + "}}");
					String kMp1tex = ("k^{\\text{Mp1}}_{\\text{" +reactionnum + "," + i + "}}");
					String kIp1tex = ("k^{\\text{Ip1}}_{\\text{" + reactionnum + "," + i + "}}");
					
					paraList.add(kcatn);
					paraList.add(kMp1);
					paraList.add(kIp1);
					
					zaehler = "(" + kcatp + "*"+ modE.get(i) + "*" + specrefe1.getSpecies()+ "*" + specrefe2.getSpecies() 
						+ ")/("+ kIr1 + "*" + kMr2 + ")"+ " - (" + kcatn+ "*"+ modE.get(i) + "*" + specrefp1.getSpecies()
						+ ")/(" + kMp1 + ")";	
					zaehlertex = "\\frac{" + kcatptex + "\\cdot "+ s.fromSpeciesToTex(modE.get(i)) + "\\cdot " + s.fromSpeciesToTex(specrefe1.getSpecies())+ "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies()) 
						+ "}{"+ kIr1tex + "\\cdot " + kMr2tex + "}"+ " - \\frac{" + kcatntex + "\\cdot "+ s.fromSpeciesToTex(modE.get(i)) + "\\cdot " + s.fromSpeciesToTex(specrefp1.getSpecies()) 
						+ "}{" + kMp1tex + "}";
						
					nenner = "1 + " + "(" + specrefe1.getSpecies() + "/" + kIr1 + ")"
						+ " + (" + kMr1 + "*" + specrefe2.getSpecies() + "/" + kIr1 + "*" + kMr2 + ")"
						+ " + (" + specrefe1.getSpecies() + "*" + specrefe2.getSpecies() + "/" + kIr1 + "*" + kMr2 + ")"
						+ " + (" + kMr1 + "*" + specrefe2.getSpecies() + "*" + specrefp1.getSpecies() + "/" + kIr1 + "*" + kMr2 + "*" + kIp1 + ")"
						+ " + (" + specrefp1.getSpecies() + "/" + kMp1 + ")"
						;	
					nennertex = "1 + " + "\\frac{" + s.fromSpeciesToTex(specrefe1.getSpecies()) + "}{" + kIr1tex + "}"
						+ " + \\frac{" + kMr1tex + "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies()) + "}{" + kIr1tex + "\\cdot " + kMr2tex + "}"
						+ " + \\frac{" + s.fromSpeciesToTex(specrefe1.getSpecies()) + "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies()) + "}{" + kIr1tex + "\\cdot " + kMr2tex + "}"
						+ " + \\frac{" + kMr1tex + "\\cdot " + s.fromSpeciesToTex(specrefe2.getSpecies()) + "\\cdot " + s.fromSpeciesToTex(specrefp1.getSpecies()) + "}{" + kIr1tex + "\\cdot " + kMr2tex + "\\cdot " + kIp1tex + "}"
						+ " + \\frac{" + s.fromSpeciesToTex(specrefp1.getSpecies()) + "}{" + kMp1tex + "}"
						;	
				}
			}
			
			if(modActi.isEmpty()==false)
			{
				for(int j = 0; j < modActi.size(); j++)
				{
					String kA = "kA_" + reactionnum + "_" + i + "_" + j ;
					String kAtex = "k^{\\text{A}}_{\\text{" + reactionnum + "," + i + "," + j + "}}";
					
					paraList.add("kA_" + reactionnum +"_"+ i+"_"+ j );
					acti = acti + "(" +modActi.get(j) + "/(" + kA + "+" + modActi.get(j) + "))*";
					actitex = actitex + "\\frac{" + s.fromSpeciesToTex(modActi.get(j)) + "}{" + kAtex + "+" + s.fromSpeciesToTex(modActi.get(j)) + "}\\cdot ";
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
		
//			Formel bilden
			if(i == (modE.size()-1))
			{
				formeltxt = formeltxt + acti + inhib + "(" + zaehler + ")/(" + nenner + ")" ;
				formeltex = formeltex + actitex + inhibtex + "\\frac{" + zaehlertex + "}{" + nennertex + "}" ;
			}
			else
			{
				formeltxt = formeltxt + acti + inhib + "(" + zaehler + ")/(" + nenner + ") + ";
				formeltex = formeltex + actitex + inhibtex + "\\frac{" + zaehlertex + "}{" + nennertex + "} + ";
			}
		}
		
		

	}

	@Override
	public void sMAK(int catNumber) {
		// TODO Auto-generated method stub
		
	}

}
