package ode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jp.sbi.celldesigner.plugin.*;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class ExistKineticLaw 
{
	private PluginModel model;
	private SBMLsqueezerPlugin plugin;
	private HashMap<String,String> idAndName = new HashMap<String,String> ();
	private HashMap<Integer,String> numAndSpecie = new HashMap<Integer,String> ();
	private HashMap<String,Integer> speciesAndNum = new HashMap<String,Integer> ();
	private HashMap<String,String> specieAndODE = new HashMap<String,String> ();
	private HashMap<String,String> specieAndSimpleODE = new HashMap<String,String> ();
	private HashMap<String,String> specieAndSimpleODETex = new HashMap<String,String> ();
	private HashMap<String,String> specieAndODEtex = new HashMap<String,String> ();
	private HashMap<Integer,String> reactionNumAndKineticName = new HashMap<Integer,String> ();
	private HashMap<String,Integer> specieAndBoundaryC = new HashMap<String,Integer> ();
	private HashMap<Integer,String> reactionNumAndKinetic = new HashMap<Integer,String> ();
	private HashMap<Integer,String> reactionNumAndKinetictex = new HashMap<Integer,String> ();
	private HashMap<Integer,String> reactionNumAndKinetictexName = new HashMap<Integer,String> ();
	private HashMap<Integer,List<String>> reactionNumAndParameters = new HashMap<Integer,List<String>> ();
	private HashMap<Integer,List<String>> reactionNumAndReactants = new HashMap<Integer,List<String>> ();
	private HashMap<Integer,List<String>> reactionNumAndProducts = new HashMap<Integer,List<String>> ();
	private HashMap<Integer,String> reactionNumAndId = new HashMap<Integer,String> ();
	private boolean fullRank;
	private List<Integer> reactionNumOfNotExistKinetics = new ArrayList<Integer>();
	private HashMap<Integer,Integer> reactionNumAndNumOfReactants = new HashMap<Integer,Integer>();
	private List<Integer> reacNumofexistKinetics = new ArrayList<Integer>();
	private int level;
	private int whichkin = 1;
	private boolean biuni;
	private double[][] N;
	private List<String> modActi = new ArrayList<String>();
	private List<String> modCat = new ArrayList<String>();
	private List<String> modInhib = new ArrayList<String>();
	private List<String> modE = new ArrayList<String>();
	private String GENERIC = "";
	private String RECEPTOR = "";
	private String ION_CHANNEL = "";
	private String TRUNCATED = "";
	private String GENE = "";
	private String RNA = "";
	private String ANTISENSE_RNA = "";
	private String PHENOTYPE = "";
	private String ION = "";
	private String SIMPLE_MOLECULE = "";
	private String UNKNOWN = "";
	private String COMPLEX = "";
	private int uniUniType;
	private int biBiType; 
	private int biUniType;
	private boolean forceAllReactionsAsEnzymeReaction;
	private boolean processTypeConv;
	private boolean generateKineticForAllReaction;
	private boolean visitConvFirst = true;

	private boolean reversibility;
		
	/**
	 * Constructor
	 */
	//old Constructor
	public ExistKineticLaw(SBMLsqueezerPlugin plugin, int whichkin)
	{
		this.plugin  = plugin;
		model = plugin.getSelectedModel();
		this.whichkin = whichkin;
		identifyLevel();
		setBegin();
		findExictingLawsAndGenerteMissingLaws();	
		ODE ode = new ODE(plugin.getSelectedModel(), idAndName, numAndSpecie, specieAndODE, idAndName, specieAndBoundaryC, reactionNumOfNotExistKinetics, reacNumofexistKinetics, numAndSpecie);
		specieAndODE.clear();
		this.specieAndODE.clear();
		this.specieAndODE.putAll(ode.getAllODEs());
	}

	//new Constructor
	public ExistKineticLaw(SBMLsqueezerPlugin plugin, 
			boolean processTypeConv, boolean forceAllReactionsAsEnzymeReaction, int biBiType, int biUniType,
			int uniUniType, int maxSpecies, boolean maxSpeciesWarnings,
			boolean possibleEnzymeAllNotChecked, boolean possibleEnzymeAsRNA, 
			boolean possibleEnzymeSimpleMolecule, boolean possibleEnzymePhenotype, boolean possibleEnzymeReceptor, 
			boolean possibleEnzymeGene, boolean possibleEnzymeUnknown, boolean possibleEnzymeComplex, 
			boolean possibleEnzymeIonChannel, boolean possibleEnzymeIon, boolean possibleEnzymeTruncatedProtein, 
			boolean possibleEnzymeGenericProtein, boolean possibleEnzymeRNA, boolean generateKineticForAllReaction,
			boolean	reversibility) 
	{
        this.reversibility = reversibility;
        this.generateKineticForAllReaction=generateKineticForAllReaction;
		this.plugin  = plugin;
		this.uniUniType = uniUniType;
		this.biBiType = biBiType;
		this.biUniType = biUniType;
		this.forceAllReactionsAsEnzymeReaction = forceAllReactionsAsEnzymeReaction;
		this.processTypeConv = processTypeConv;
		//this.noReactionMAK = noReactionMAK;
		String a = "" + generateKineticForAllReaction;
		if(possibleEnzymeGenericProtein)
			GENERIC = "GENERIC";
		if(possibleEnzymeTruncatedProtein)
			TRUNCATED = "TRUNCATED";
		if(possibleEnzymeReceptor)
			RECEPTOR = "RECEPTOR";
		if(possibleEnzymeIonChannel)
			ION_CHANNEL = "ION_CHANNEL";
		if(possibleEnzymeGene)
			GENE = "GENE";
		if(possibleEnzymeRNA)
			RNA = "RNA";
		if(possibleEnzymeAsRNA)
			ANTISENSE_RNA = "ANTISENSE_RNA";
		if(possibleEnzymePhenotype)
			PHENOTYPE = "PHENOTYPE";
		if(possibleEnzymeIon)
			ION = "ION";
		if(possibleEnzymeSimpleMolecule)
			SIMPLE_MOLECULE = "SIMPLE_MOLECULE";
		if(possibleEnzymeUnknown)
			UNKNOWN = "UNKNOWN";
		if(possibleEnzymeComplex)
		 COMPLEX = "COMPLEX";
		model = plugin.getSelectedModel();
		
		setProcessTypeConv();
		identifyLevel();
		setBegin();
		findExictingLawsAndGenerteMissingLaws();	
	}
	
	
	/**
	 * ----------------------------------------------------------------------------------
	 * generate kinetics and paramters
	 * ----------------------------------------------------------------------------------
	 */
	//set all variabels for enzyme-kinetics of convenience
	public void setProcessTypeConv()
	{
		if(processTypeConv)
		{
			uniUniType = 2;
			biBiType = 2; 
			biUniType = 2;
		}
	}

	//set BoundaryConditions and initialize HashMaps for ODE-Generation
	public void setBegin()
	{	
		PluginListOf listOfSpecies = model.getListOfSpecies();
		for (int i = 0; i < model.getNumSpecies(); i++) 
		{
			PluginSpecies species = (PluginSpecies)listOfSpecies.get(i);
			specieAndODE.put( species.getName(), "");
			specieAndODEtex.put( species.getName(), "");

			if(level==1)
			{
				numAndSpecie.put(i, species.getName());
				speciesAndNum.put(species.getName(),i);
				idAndName.put(species.getName(), species.getName());
				
				if(species.getBoundaryCondition()==true)
					specieAndBoundaryC.put(species.getName(), 1);//1 für true
				else
					specieAndBoundaryC.put(species.getName(), 0);//0 für false
			}
			if(level==2)
			{
				numAndSpecie.put(i, species.getId());
				speciesAndNum.put(species.getId(),i);
				if (species.getName().length() < 0)
					idAndName.put(species.getId(), species.getName());
				else
					idAndName.put(species.getId(), species.getId());
				
				if(species.getBoundaryCondition()==true)
					specieAndBoundaryC.put(species.getId(), 1);//1 für true	
				else
					specieAndBoundaryC.put(species.getId(), 0);//0 für false
			}    
		}	
	}
	
	//find reactionen with missing KineticLaws and build them
	public void findExictingLawsAndGenerteMissingLaws()
	{	
		PluginListOf listOfReactions = model.getListOfReactions();
		for (int j = 0; j < model.getNumReactions(); j++) 
		{
			PluginReaction reaction = (PluginReaction)listOfReactions.get(j);
			if(generateKineticForAllReaction==true)
			{
				//PluginReaction reaction = (PluginReaction)listOfReactions.get(j);
				reactionNumAndId.put(j, reaction.getId());
				reactionNumOfNotExistKinetics.add(j);
				identifyModifer(j);
				identifyReactionTyp(j);
				createKinetic(j);	
			}
			else
			{
				reacNumofexistKinetics.add(j);
				reactionNumAndKineticName.put(j,"exist Kinetic");
				//PluginReaction reaction = (PluginReaction)listOfReactions.get(j);
				reactionNumAndId.put(j, reaction.getId());
				try
				{	
					reaction.getKineticLaw().getFormula();
					String formula = reaction.getKineticLaw().getFormula();
					if(formula.equals("")||formula.equals(" "))
					{
						ErrorField err = new ErrorField("Reaction " + reaction.getId() + " in the SBML file has an uncorrect format.");
					}
					//TODO
				}
				catch(NullPointerException e)
				{	  
					int c = reacNumofexistKinetics.indexOf(j);
					reacNumofexistKinetics.remove(c);
					reactionNumOfNotExistKinetics.add(j);
					identifyModifer(j);
					identifyReactionTyp(j);
					createKinetic(j);
				} 
			}
		}	
	}
	
	//Identify the level of the SBML-File
	public void identifyLevel()
	{
		PluginListOf listOfSpecies = model.getListOfSpecies();
		PluginSpecies species = (PluginSpecies)listOfSpecies.get(0);
		try
		{
			species.getId();
			level = 2;
		}
		catch(NullPointerException e)
		{
			level = 1;
		}
	}

	//identify which Modifer is used
	public void identifyModifer(int i)
	{
		PluginListOf listOfReactions = model.getListOfReactions();
		PluginReaction reaction = (PluginReaction)listOfReactions.get(i);
		for(int j = 0; j < reaction.getNumModifiers(); j++)
		{
			if(reaction.getModifier(j).getModificationType().equals("INHIBITION"))
			{
				modInhib.add(reaction.getModifier(j).getSpecies());
			}
			if(reaction.getModifier(j).getModificationType().equals("UNKNOWN_CATALYSIS"))
			{
				modActi.add(reaction.getModifier(j).getSpecies());
			}	
			if(reaction.getModifier(j).getModificationType().equals("CATALYSIS"))
			{
				for(int k = 0; i < idAndName.size(); k++)
				{
					if(numAndSpecie.get(k).equals(reaction.getModifier(j).getSpecies()))
					{
						PluginListOf listOfSpecies = model.getListOfSpecies();
						PluginSpecies species = (PluginSpecies)listOfSpecies.get(k);
						String speciesAliasType;
						if(species.getSpeciesAlias(0).getType().equals("PROTEIN"))
							speciesAliasType = "" + species.getSpeciesAlias(0).getProtein().getType();
						else
							speciesAliasType = "" + species.getSpeciesAlias(0).getType();
						if(speciesAliasType.equals(GENERIC)||
								speciesAliasType.equals(TRUNCATED)||
								speciesAliasType.equals(RECEPTOR)||
								speciesAliasType.equals(ION_CHANNEL)||
								speciesAliasType.equals(GENE)||
								speciesAliasType.equals(RNA)||
								speciesAliasType.equals(ANTISENSE_RNA)||
								speciesAliasType.equals(PHENOTYPE)||
								speciesAliasType.equals(ION)||
								speciesAliasType.equals(SIMPLE_MOLECULE)||
								speciesAliasType.equals(UNKNOWN)||
								speciesAliasType.equals(COMPLEX))
						{
							modE.add(reaction.getModifier(j).getSpecies());
						}
						else
						{
							modCat.add(reaction.getModifier(j).getSpecies());	
						}
						break;
					}
				}	
			}
		}	
	}
	
	public void stoechMatrix()
	{
		PluginListOf listOfReactions = model.getListOfReactions();
		for (int j = 0; j < model.getNumReactions(); j++) 
		{
			PluginReaction reaction = (PluginReaction)listOfReactions.get(j);
			PluginListOf listOfReactants = reaction.getListOfReactants();
			PluginListOf listOfProducts = reaction.getListOfProducts();
			for(int i = 0; i < reaction.getNumReactants(); i++)
			{
				PluginSpeciesReference specref = (PluginSpeciesReference)listOfReactants.get(i);
				N[speciesAndNum.get(specref.getSpecies())][j] = ((int)-specref.getStoichiometry());
			}
			for(int i = 0; i < reaction.getNumProducts(); i++)
			{
				PluginSpeciesReference specref = (PluginSpeciesReference)listOfProducts.get(i);
				N[speciesAndNum.get(specref.getSpecies())][j] = (int)specref.getStoichiometry();
			}
		}
	}
	
	//identify the reactionType for generting the kinetics
	public void  identifyReactionTyp(int reactionnum)
	{
		PluginListOf listOfReactions = model.getListOfReactions();
		PluginReaction reaction = (PluginReaction)listOfReactions.get(reactionnum);
		PluginListOf listOfReactants = reaction.getListOfReactants();
		PluginListOf listOfProducts = reaction.getListOfProducts();
		
		setReactionNumAndReactants(reactionnum, listOfReactants);
		setReactionNumAndProducts(reactionnum, listOfProducts);
	
		int reac = listOfReactants.getNumItems();
		int pro = listOfProducts.getNumItems();
		reactionNumAndNumOfReactants.put(reactionnum, reac);

		if(forceAllReactionsAsEnzymeReaction)
		{		
			//Enzym-Kinetics
			if(modE.isEmpty())
				modE.add("E");	
			switch ( reac) 
			  { 
				//Uni-Uni: MMK/Conv	(1E/1P)		
			    case 1: 
			    	if(pro==1)
			    	{
			    		whichkin = uniUniType;
			    	}
			    	else
			    		whichkin = 2;
			      break;
			      	//Bi-Uni: Conv/Ran/Ord/PP	(2E/1P)/(2E/2P)
			    case 2: 
			    	switch ( pro) 
					  { 
					    case 1: 
					    {
					    	whichkin = biUniType;
					    	biuni = true;
					    }
					    	break;
					    case 2: 
					    {
					    	whichkin = biBiType;
					    	biuni = false;
					    }
					    	break;
					    default:
					    {
					    	whichkin = 2;
					    }
					    	break;
					  }
			    	break;
				default: 
				{
					whichkin = 2;
				}
					break;
			  }		
		}
		else
		{
			// GMAK
			if(modE.isEmpty())
				{
					whichkin = 1;
				}
			//Enzym-Kinetics
			if(!modE.isEmpty())
			{		
				switch ( reac) 
				  { 
					//Uni-Uni: MMK/Conv	(1E/1P)		
				    case 1: 
				    	if(pro==1)
				    	{
				    		whichkin = uniUniType;
				    	}
				    	else
				    		whichkin = 2;
				      break;
				     //	Bi-Uni: Conv/Ran/Ord/PP	(2E/1P)/(2E/2P)
				    case 2: 
				    	switch ( pro) 
						  { 
						    case 1: 
						    {
						    	whichkin = biUniType;
						    	biuni = true;
						    }
						    	break;
						    case 2: 
						    {
						    	whichkin = biBiType;
						    	biuni = false;
						    }
						    	break;
						    default:
						    	whichkin = 2;
						    	break;
						  }
				    	break;
					default: 
						whichkin = 2;
						break;
				  }	
			}
		}
	}
	
	//generates the corresponding object for the kinetic-generation
	public void createKinetic(int i)
	{
		switch ( whichkin) 
		  { 
		    case 1: 
		    	reactionNumAndKineticName.put(i, "generalized M.A.K.");
		    	GMak gmak = new GMak(model,i,idAndName,modActi,modInhib,modCat,reversibility);				
				reactionNumAndKinetic.put(i, gmak.getKinetictxt());
				reactionNumAndKinetictex.put(i, gmak.getKinetictex());
				reactionNumAndParameters.put(i,  gmak.getParameters());	
		      break; 
		    case 2: 
		    	if(visitConvFirst)
		    	{
			    	if(model.getNumSpecies() >= model.getNumReactions())
					{
						this.N = new double[model.getNumSpecies()][model.getNumReactions()];
						stoechMatrix();
						GaussRang gR = new GaussRang(N, model.getNumSpecies(), model.getNumReactions());
			    		fullRank = gR.getVollerRang();
					}
					else
					{
						fullRank = false;
					}
			    	visitConvFirst=false;
		    	}
		    	if(fullRank==true)
		    	{
		    		reactionNumAndKineticName.put(i, "Convenience kinetics");
			    	Convenience conv = new Convenience(model,i,idAndName,modE,modActi,modInhib,reversibility);
			    	//Conv conv = new Conv(model,i,idAndName,modE,modActi,modInhib,reversibility);
					reactionNumAndKinetic.put(i, conv.getKinetictxt());
					reactionNumAndKinetictex.put(i, conv.getKinetictex());
					reactionNumAndParameters.put(i, conv.getParameters());
		    	}
		    	if(fullRank==false)
		    	{
		    		reactionNumAndKineticName.put(i, "Convenience kinetics");
			    	//Convenience conv = new Convenience(model,i,idAndName,modE,modActi,modInhib,reversibility);
			    	Conv conv = new Conv(model,i,idAndName,modE,modActi,modInhib,reversibility);
					reactionNumAndKinetic.put(i, conv.getKinetictxt());
					reactionNumAndKinetictex.put(i, conv.getKinetictex());
					reactionNumAndParameters.put(i, conv.getParameters());
		    	}		    					
		      break; 
		    case 3: 
		    	reactionNumAndKineticName.put(i, "Michaelis Menten kinetics");
		    	MMK mmk = new MMK(model,i,idAndName,modE,modActi,modInhib,reversibility);
		    	reactionNumAndKinetic.put(i, mmk.getKinetictxt());
				reactionNumAndKinetictex.put(i, mmk.getKinetictex());
				reactionNumAndParameters.put(i, mmk.getParameters());
		      break; 
		    case 4: 
		    	reactionNumAndKineticName.put(i, "Random");
		    	Random random = new Random(model,i,idAndName,modE,modActi,modInhib,biuni,reversibility);
				reactionNumAndKinetic.put(i, random.getKinetictxt());
				reactionNumAndKinetictex.put(i, random.getKinetictex());
				reactionNumAndParameters.put(i, random.getParameters());
		      break; 
		    case 5: 
		    	reactionNumAndKineticName.put(i, "Ping-Pong");
		    	PingPong pingpong = new PingPong(model,i,idAndName,modE,modActi,modInhib,reversibility);
				reactionNumAndKinetic.put(i, pingpong.getKinetictxt());
				reactionNumAndKinetictex.put(i, pingpong.getKinetictex());
				reactionNumAndParameters.put(i, pingpong.getParameters());		
		      break; 
		    case 6: 
		    	reactionNumAndKineticName.put(i, "Ordered");
		    	Ordered ordered = new Ordered(model,i,idAndName,modE,modActi,modInhib,biuni,reversibility);
				reactionNumAndKinetic.put(i, ordered.getKinetictxt());
				reactionNumAndKinetictex.put(i, ordered.getKinetictex());
				reactionNumAndParameters.put(i, ordered.getParameters());
		      break;    
		  } 
		modActi.clear();
		modE.clear();
		modInhib.clear();
		modCat.clear();
	}
	
	/**
	 * --------------------------------------------------------------------------------
	 * Store parameters and kinetics in SBML-File and generate the ODEs
	 *---------------------------------------------------------------------------------
	 */

	//set the generated kinetics and paramters in corresponding HashMaps
	public void storeKineticsAndParameters()
	{
		storeLaws();
		storeParameters();
		ODE ode = new ODE(plugin.getSelectedModel(), idAndName, numAndSpecie, specieAndODE,
				specieAndODEtex, specieAndBoundaryC, reactionNumOfNotExistKinetics, reacNumofexistKinetics, reactionNumAndKinetictex);
		specieAndODEtex.clear();
		specieAndODE.clear();
		this.specieAndODE.clear();
		this.specieAndODEtex.clear();
		this.reactionNumAndKinetictex.clear();
		this.specieAndODE.putAll(ode.getAllODEs());
		this.specieAndODEtex.putAll(ode.getAllODEtex());
		this.specieAndSimpleODE.putAll(ode.getSpecieAndSimpleODE());
		this.specieAndSimpleODETex.putAll(ode.getSpecieAndSimpleODETex());
		this.reactionNumAndKinetictex.putAll(ode.getReactionNumAndKinetictexId());
		this.reactionNumAndKinetictexName.putAll(ode.getReactionNumAndKinetictexName());
	}
	
	//store the generated Paramters in SBML-File
	public void storeParameters()
	{
		for(int j = 0; j < reactionNumOfNotExistKinetics.size(); j++)
		{
			for(int i = 0; i < reactionNumAndParameters.get(reactionNumOfNotExistKinetics.get(j)).size(); i++)
			{				
				PluginModel model = plugin.getSelectedModel();
				PluginSBase sbase = new PluginParameter(reactionNumAndParameters.get(reactionNumOfNotExistKinetics.get(j)).get(i),model);
				PluginParameter para = (PluginParameter)sbase;
				model.addParameter(para);
				plugin.notifySBaseAdded((PluginSBase)para);
			}
		}
	}
		
	//store the generated Kinetics in SBML-File
	public void storeLaws()
	{
		PluginModel model = plugin.getSelectedModel();
		for(int i = 0; i < reactionNumOfNotExistKinetics.size(); i++)
		{
			PluginReaction reaction = model.getReaction(reactionNumOfNotExistKinetics.get(i));
			//PluginSBase sbase = new PluginKineticLaw(reaction);
			PluginKineticLaw law = new PluginKineticLaw(reaction);
			law.setFormula(reactionNumAndKinetic.get(reactionNumOfNotExistKinetics.get(i)));
			plugin.notifySBaseAdded((PluginSBase)law);
		}
	}
		
	/**
	 * ---------------------------------------------------------------------------------
	 * Setter
	 * ---------------------------------------------------------------------------------
	 */	
	
	//set HashMap<Integer,List<String>> with reactionNumber and Reactans for the corresponding reaction
	public void setReactionNumAndReactants(int reactionnum, PluginListOf listOfReactants)
	{
		List<String> reacOfReaction = new ArrayList<String>();
		for(int i = 0 ; i < listOfReactants.getNumItems(); i++)
		{
			PluginSpeciesReference specref = (PluginSpeciesReference)listOfReactants.get(i);
			reacOfReaction.add(specref.getSpecies());
		}
		reactionNumAndReactants.put(reactionnum, reacOfReaction);	
	}

	//set HashMap<Integer,List<String>>  with reactionNumber and Products for the corresponding reaction
	public void setReactionNumAndProducts(int reactionnum,PluginListOf listOfProducts)
	{
		List<String> proOfReaction = new ArrayList<String>();
		for(int i = 0 ; i < listOfProducts.getNumItems(); i++)
		{
			PluginSpeciesReference specref = (PluginSpeciesReference)listOfProducts.get(i);
			proOfReaction.add(specref.getSpecies());
		}
		reactionNumAndProducts.put(reactionnum, proOfReaction);	
	}
	
	/**
	 * ---------------------------------------------------------------------------------
	 * Getter
	 * ---------------------------------------------------------------------------------
	 */
	
	//get HashMap<Integer,List<String>> with with reactionNumber and Reactans for the corresponding reaction
	public HashMap<Integer,List<String>> getReactionNumAndReactants()
	{
		return reactionNumAndReactants;
	}
	
	//get HashMap<Integer,List<String>>  with reactionNumber and Products for the corresponding reaction
	public HashMap<Integer,List<String>> getReactionNumAndProducts()
	{
		return reactionNumAndProducts;
	}
	
	
	//get HashMap<Integer,String>  with reactionNumber and Id for the corresponding reaction
	public HashMap<Integer,String> getReactionNumAndId()
	{
		return reactionNumAndId;
	}
	
	//get the the level for the SBML-File
	public int getlevel()
	{
		return level;
	}
	
		

	//get HashMap<Integer,String> with reactionNumber and generated Kinetic for the corresponding reaction
	
	
	public  HashMap<Integer,String> getReactionNumAndKinetic()
	{
		return reactionNumAndKinetic;
	}
	
	
	//get HashMap<Integer,String> with reactionNumber and generated kinetic in LaTex-Format for the corresponding reaction
	public  HashMap<Integer,String> getReactionNumAndKinetictex()
	{
		return reactionNumAndKinetictex;
	}
	
	
	
	//get HashMap<Integer,List<String>> with reactionNumber and generted parameters for the corresponding reaction
	public  HashMap<Integer,List<String>> getReactionNumAndParameters()
	{
		return reactionNumAndParameters;
	}
	
	
	
	//get HashMap<Integer,String>  with reactionNumber and generated kinetic in LaTex-Format with Specie_Name
	// instead of Species_Id for the corresponding reaction
	public  HashMap<Integer,String> getReactionNumAndKinetictexName()
	{
		return reactionNumAndKinetictexName;
	}
	
	//get HashMap<String,String>  generated ODE for the corresponding Species
	public HashMap<String,String> getAllODE()
	{
		return specieAndODE;
	}
	
	//get HashMap<String,String> generated ODE with a simple equation for the corresponding Species
	public HashMap<String,String> getSpecieAndSimpleODE()
	{
		return specieAndSimpleODE;
	}

	//get HashMap<String,String> generated ODE  in simple Equation and LaTex-Format for the corresponding Species
	public HashMap<String,String> getSpecieAndSimpleODETex()
	{
		return specieAndSimpleODETex;
	}
	
	//get HashMap<Integer,String> get all Species
	public HashMap<Integer,String> getAllSpecies()
	{
		return numAndSpecie;
	}
	
//	get HashMap<String,String> get all SpeciesName
	public HashMap<String,String> getAllSpeciesName()
	{
		return idAndName;
	}
	
	//get HashMap<Integer,String> with reactionNumber and KineticName for the corresponding reaction
	public HashMap<Integer,String> getReactionNumAndKineticName()
	{
		return reactionNumAndKineticName;
	}
	
	
	//get HashMap<Integer,Integer> with reactionNumber and number for Reactants for the corresponding reaction
	public HashMap<Integer,Integer> getReactionNumAndNumOfReactants()
	{
		return reactionNumAndNumOfReactants;
	}
	
	
	//get List<Integer> with reactionNumbers  for the Reactions without kinetics
	public List<Integer> getReactionNumOfNotExistKinetics()
	{
		return reactionNumOfNotExistKinetics;
	}
	
	
}
	