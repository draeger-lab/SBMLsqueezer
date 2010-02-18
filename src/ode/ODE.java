package ode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.sbi.celldesigner.plugin.*;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class ODE {

	private HashMap<String,String> idAndName = new HashMap<String,String> ();
	private HashMap<Integer,String> numAndSpecie = new HashMap<Integer,String> ();
	private HashMap<String,String> specieAndODE = new HashMap<String,String> ();
	private HashMap<String,String> specieAndODEtex = new HashMap<String,String> ();
	private HashMap<String,String> specieAndODEtexId = new HashMap<String,String> ();
	private HashMap<String,String> specieAndSimpleODE = new HashMap<String,String> ();
	private HashMap<String,String> specieAndSimpleODETex = new HashMap<String,String> ();
	private HashMap<String,Integer> specieAndBoundaryC = new HashMap<String,Integer> ();
	private List<Integer> reactionNumber = new ArrayList<Integer>();
	private List<Integer> reacNumofexistKinetics = new ArrayList<Integer>();
	private HashMap<Integer,String> reactionNumAndKineticId = new HashMap<Integer,String> ();
	private HashMap<Integer,String> reactionNumAndKineticName = new HashMap<Integer,String> ();
	private HashMap<Integer,String> reactionNumAndKinetictexId = new HashMap<Integer,String> ();
	private HashMap<Integer,String> reactionNumAndKinetictexName = new HashMap<Integer,String> ();
	private PluginModel model;


	ODE(PluginModel model, HashMap<String,String> idAndName, HashMap<Integer,String> numAndSpecie,
		HashMap<String,String> specieAndODE, HashMap<String, String> specieAndODEtex, 
		HashMap<String,Integer> specieAndBoundaryC, List<Integer> reactionNumber,
		List<Integer> reacNumofexistKinetics,HashMap<Integer,String> reactionNumAndKinetictex)
	{
		this.model = model;
		this.idAndName.putAll(idAndName);
		this.numAndSpecie.putAll(numAndSpecie);
		this.specieAndODE.putAll(specieAndODE);
		this.specieAndODEtex.putAll(specieAndODEtex);
		this.specieAndBoundaryC.putAll(specieAndBoundaryC);
		this.reactionNumber.addAll(reactionNumber);
		this.reacNumofexistKinetics.addAll(reacNumofexistKinetics);
		this.reactionNumAndKinetictexId.putAll(reactionNumAndKinetictex);
		this.specieAndODEtexId.putAll(specieAndODEtex);
		this.specieAndSimpleODE.putAll(specieAndODE);
		this.specieAndSimpleODETex.putAll(specieAndODEtex);
		setTex();
		idToName();
		simpleODE();
		correctness();
		//setODE();
	}


	void setODEreac(PluginSpeciesReference specref, int reacnum)
	{	         		 
		String ode = specieAndODE.get(specref.getSpecies());
		String odeTex =  specieAndODEtex.get(specref.getSpecies());
		String odeTexId = specieAndODEtexId.get(specref.getSpecies());
		ode = ode + "(" + reactionNumAndKineticName.get(reacnum) + ") ";
		odeTex = odeTex + "(" + reactionNumAndKinetictexName.get(reacnum) + ") ";
		odeTexId = odeTexId + "(" + reactionNumAndKinetictexId.get(reacnum) + ") ";
		specieAndODE.put(specref.getSpecies(), ode);
		specieAndODEtex.put(specref.getSpecies(), odeTex);
		specieAndODEtexId.put(specref.getSpecies(), odeTexId);
	}
		
	void setODEpro(PluginSpeciesReference specref, int reacnum)
	{
		String ode = specieAndODE.get(specref.getSpecies());
 		String odeTex = specieAndODEtex.get(specref.getSpecies());
 		String odeTexId = specieAndODEtexId.get(specref.getSpecies());
 		ode = ode +  "(" + reactionNumAndKineticName.get(reacnum) + ") ";
 		odeTex = odeTex + "(" + reactionNumAndKinetictexName.get(reacnum) + ") ";
 		odeTexId = odeTexId + "(" + reactionNumAndKinetictexId.get(reacnum) + ") ";
 		specieAndODE.put(specref.getSpecies(), ode);
	    specieAndODEtex.put(specref.getSpecies(), odeTex);  
	    specieAndODEtexId.put(specref.getSpecies(), odeTexId);
	}

	public void idToName()
	{
		PluginListOf listOfReactions = model.getListOfReactions();
		for (int j = 0; j < model.getNumReactions(); j++) 
	    {
	    	PluginReaction reaction = (PluginReaction)listOfReactions.get(j);
	    	String kineticLawPartName = reaction.getKineticLaw().getFormula();
	    	String kineticLawPartId = reaction.getKineticLaw().getFormula();
	    	String kineticLawPartTexName = reactionNumAndKinetictexId.get(j);
	    	String kineticLawPartTexId = reactionNumAndKinetictexId.get(j);
//möglichkeit hier nur in den Reaktanten zu suchen	    
	    	for (int i = 0; i < model.getNumSpecies(); i++) 
	  	    {
	    		numAndSpecie.get(i);//name bzw. id suche das in kineticLaw und ersete es
	    		if(kineticLawPartName.contains(numAndSpecie.get(i)))
	    		{
	    			/*
    				kineticLawPartName = kineticLawPartName.replaceAll(numAndSpecie.get(i), "[" + idAndName.get(numAndSpecie.get(i)) + "]");
	    			reactionNumAndKineticName.put(j, kineticLawPartName);
	    			kineticLawPartId = kineticLawPartId.replaceAll(numAndSpecie.get(i), "[" + numAndSpecie.get(i) + "]");
	    			reactionNumAndKineticId.put(j, kineticLawPartId);
	    			kineticLawPartTexName = kineticLawPartTexName.replaceAll(numAndSpecie.get(i),"[" + idAndName.get(numAndSpecie.get(i)) + "]");
	    			reactionNumAndKinetictexName.put(j, kineticLawPartTexName);
	    			kineticLawPartTexId = kineticLawPartTexId.replaceAll(numAndSpecie.get(i),"[" + numAndSpecie.get(i) + "]");
	    			reactionNumAndKinetictexId.put(j, kineticLawPartTexId);
	    			*/
	    			kineticLawPartName = kineticLawPartName.replaceAll(numAndSpecie.get(i),  idAndName.get(numAndSpecie.get(i)) );
	    			reactionNumAndKineticName.put(j, kineticLawPartName);
	    			kineticLawPartTexName = kineticLawPartTexName.replaceAll(numAndSpecie.get(i), idAndName.get(numAndSpecie.get(i)));
	    			reactionNumAndKinetictexName.put(j, kineticLawPartTexName);

	    			
	    		}
	  	    }
	    }	
	}
	
	public void simpleODE()
	{	
		PluginListOf listOfReactions = model.getListOfReactions();
		for (int j = 0; j < model.getNumReactions(); j++) 
	    {
			PluginReaction reaction = (PluginReaction)listOfReactions.get(j);
			PluginListOf listOfReactants = reaction.getListOfReactants();
			PluginListOf listOfProducts = reaction.getListOfProducts();
	    
	        for (int i = 0; i < reaction.getNumReactants(); i++)
	        {
	        	PluginSpeciesReference specref = (PluginSpeciesReference)listOfReactants.get(i);
	        	if(specieAndBoundaryC.get(specref.getSpecies())==0)
	         	{
	        		String kinetic;
	        		kinetic = specieAndSimpleODE.get(specref.getSpecies());
	        		if(specieAndSimpleODE.get(specref.getSpecies())==null)
	        			kinetic = "";
	        		kinetic = kinetic + "-v" + j;
	        		specieAndSimpleODE.put(specref.getSpecies(), kinetic);
	        		String kinetictex;
	        		kinetictex = specieAndSimpleODETex.get(specref.getSpecies());
	        		if(specieAndSimpleODETex.get(specref.getSpecies())==null)
	        			kinetictex = "";
	        		kinetictex = kinetictex + "-v_{" + j + "}";
	        		specieAndSimpleODETex.put(specref.getSpecies(), kinetictex);
//	        		setODEreac(specref,j);
	         	}
	        	else
	        		specieAndSimpleODE.put(specref.getSpecies(), "0"); 
	  	    }
	        for (int i = 0; i < reaction.getNumProducts(); i++)
	        {
	         	PluginSpeciesReference specref = (PluginSpeciesReference)listOfProducts.get(i);
	         	if(specieAndBoundaryC.get(specref.getSpecies())==0)
	         	{
	         		String kinetic;
	        		kinetic = specieAndSimpleODE.get(specref.getSpecies());
	        		if(specieAndSimpleODE.get(specref.getSpecies())==null)
	        			kinetic = "";
	        		kinetic = kinetic + "+v" + j;
	         		specieAndSimpleODE.put(specref.getSpecies(), kinetic);
	         		String kinetictex;
	        		kinetictex = specieAndSimpleODETex.get(specref.getSpecies());
	        		if(specieAndSimpleODETex.get(specref.getSpecies())==null)
	        			kinetictex = "";
	        		kinetictex = kinetictex + "+v_{" + j + "}";
	        		specieAndSimpleODETex.put(specref.getSpecies(), kinetictex);
//	         		setODEpro(specref,j);
	         	}
	        	else
	        	{
	        		specieAndSimpleODE.put(specref.getSpecies(), "0");
	        		specieAndSimpleODETex.put(specref.getSpecies(), "0");
	        	}
	        }
	    }
	}
	
	public void correctness()
	{
		for (int i = 0; i < model.getNumSpecies(); i++) 
  	    {
			String kinetic;
			String kineticTex;
			kinetic = specieAndSimpleODE.get(numAndSpecie.get(i));
			kineticTex = specieAndSimpleODETex.get(numAndSpecie.get(i));
			try
			{	
				if(kinetic.charAt(0)== '+')
				{
					kinetic = kinetic.substring(1);
					specieAndSimpleODE.put(numAndSpecie.get(i),kinetic);
					kineticTex = kineticTex.substring(1);
					specieAndSimpleODETex.put(numAndSpecie.get(i),kineticTex);
				}
			}
			catch(java.lang.StringIndexOutOfBoundsException e)
			{	  
				specieAndSimpleODE.put(numAndSpecie.get(i),"0");
				specieAndSimpleODETex.put(numAndSpecie.get(i),"0");
			}
  	    }
	}
	
//erzeugt tex für Kinetiken die schon vorher in SBML standen	
	public void setTex()
	{
		if(!reacNumofexistKinetics.isEmpty())
		{
			for(int i = 0; i < reacNumofexistKinetics.size(); i++)
			{
				PluginListOf listOfReactions = model.getListOfReactions();
			    PluginReaction reaction = (PluginReaction)listOfReactions.get(reacNumofexistKinetics.get(i));
				String kinetic = reaction.getKineticLaw().getFormula();
				String2Tex stringToTex = new String2Tex();
				reactionNumAndKinetictexId.put(i, stringToTex.getEquation(kinetic));	
			}
		}     
	}
	
// get ODE
	public HashMap<String,String> getAllODEs()
	{
		for(int i = 0; i < numAndSpecie.size(); i++)
		{
			System.out.println(specieAndODE.get(numAndSpecie.get(i)));
		}
		return specieAndODE;
	}

	public HashMap<String,String> getAllODEtex()
	{
		for(int i = 0; i < numAndSpecie.size(); i++)
		{
			System.out.println(specieAndODEtex.get(numAndSpecie.get(i)));
		}
		return specieAndODEtex;
	}

	public HashMap<String,String> getSpecieAndSimpleODE()
	{
		return specieAndSimpleODE;
	}

	public HashMap<String,String> getSpecieAndSimpleODETex()
	{
		return specieAndSimpleODETex;
	}
	
	public HashMap<Integer,String> getReactionNumAndKinetictexId()
	{
		return reactionNumAndKinetictexId;
	}
	
	public HashMap<Integer,String>  getReactionNumAndKinetictexName()
	{
		return reactionNumAndKinetictexName;
	}

	
}
