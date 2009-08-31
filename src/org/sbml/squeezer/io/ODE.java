/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.sbml.ListOf;
import org.sbml.Model;
import org.sbml.Reaction;
import org.sbml.Species;
import org.sbml.SpeciesReference;

/**
 * TODO: comment missing
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis </a>
 * @date Aug 1, 2007
 */
public class ODE {

	private HashMap<Integer, String> numAndSpecies = new HashMap<Integer, String>();

	private HashMap<String, String> speciesAndODE = new HashMap<String, String>();

	private HashMap<String, String> specieAndODETeX = new HashMap<String, String>();

	private HashMap<String, String> specieAndODETeXId = new HashMap<String, String>();

	private HashMap<String, String> speciesAndSimpleODE = new HashMap<String, String>();

	private HashMap<String, String> speciesAndSimpleODETeX = new HashMap<String, String>();

	private List<Integer> reactionNumber = new ArrayList<Integer>();

	private List<Integer> reacNumOfXexistKinetics = new ArrayList<Integer>();

	private String[] reactionNumAndKineticTeX;

	private String[] reactionNumAndKineticName;

	private String[] reactionNumAndKineticTeXName;

	private Model model;

	/**
	 * @param model
	 * @param reactionNumber
	 * @param reacNumofexistKinetics
	 */
	public ODE(Model model, List<Integer> reactionNumber,
			List<Integer> reacNumofexistKinetics) {
		this.model = model;
		this.numAndSpecies.putAll(numAndSpecies);
		this.speciesAndODE.putAll(speciesAndODE);
		// this.specieAndODETeX.putAll(specieAndODEtex);
		this.reactionNumber.addAll(reactionNumber);
		this.reacNumOfXexistKinetics.addAll(reacNumofexistKinetics);
		// this.specieAndODETeXId.putAll(specieAndODEtex);
		this.speciesAndSimpleODE.putAll(speciesAndODE);
		// this.speciesAndSimpleODETeX.putAll(specieAndODEtex);
		this.reactionNumAndKineticTeX = new String[model.getNumReactions()];
		this.reactionNumAndKineticTeXName = new String[model.getNumReactions()];
		this.reactionNumAndKineticName = new String[model.getNumReactions()];
		Arrays.fill(reactionNumAndKineticName, "Existing Kinetic");
//		for (Iterator<Integer> i = reactionNumAndKineticLaw.keySet().iterator(); i
//				.hasNext();) {
//			Integer key = i.next();
//			BasicKineticLaw law = reactionNumAndKineticLaw.get(key);
//			reactionNumAndKineticTeX[key.intValue()] = law.getMath().toLaTeX()
//					.toString();
//			reactionNumAndKineticName[key.intValue()] = law.getName();
//		}
		setTeX();
		idToName();
		simpleODE();
		correctness();
		// setODE();
	}

	public void correctness() {
		for (int speciesNum = 0; speciesNum < model.getNumSpecies(); speciesNum++) {
			String kinetic;
			String kineticTeX;
			kinetic = speciesAndSimpleODE.get(numAndSpecies.get(speciesNum));
			kineticTeX = speciesAndSimpleODETeX.get(numAndSpecies
					.get(speciesNum));
			if ((kinetic == null) || (kineticTeX == null)) {
				kinetic = "0";
				kineticTeX = "0";
				speciesAndSimpleODE.put(numAndSpecies.get(speciesNum), kinetic);
				speciesAndSimpleODETeX.put(numAndSpecies.get(speciesNum),
						kineticTeX);
			} else if ((kinetic.length() == 0) || (kineticTeX.length() == 0)) {
				kinetic = "0";
				kineticTeX = "0";
				speciesAndSimpleODE.put(numAndSpecies.get(speciesNum), kinetic);
				speciesAndSimpleODETeX.put(numAndSpecies.get(speciesNum),
						kineticTeX);
			} else if (kinetic.charAt(0) == '+') {
				speciesAndSimpleODE.put(numAndSpecies.get(speciesNum), kinetic
						.substring(1));
				speciesAndSimpleODETeX.put(numAndSpecies.get(speciesNum),
						kineticTeX.substring(1));
			}
		}
	}

	// get ODE
	public HashMap<String, String> getAllODEs() {
		return speciesAndODE;
	}

	public HashMap<String, String> getAllODETeX() {
		return specieAndODETeX;
	}

	public String[] getKineticLawNames() {
		return reactionNumAndKineticName;
	}

	public String[] getReactionNumAndKinetictexId() {
		return reactionNumAndKineticTeX;
	}

	public HashMap<String, String> getSpecieAndSimpleODE() {
		return speciesAndSimpleODE;
	}

	public HashMap<String, String> getSpeciesAndSimpleODETeX() {
		return speciesAndSimpleODETeX;
	}

	public void idToName() {
		for (int reactionNum = 0; reactionNum < model.getNumReactions(); reactionNum++) {
			Reaction reaction = model.getReaction(reactionNum);
			if (reaction.getKineticLaw() != null) {
				String kineticLawPartName = reaction.getKineticLaw()
						.getFormula();
				String kineticLawPartTeXName = reactionNumAndKineticTeX[reactionNum]
						.toString();
				// Moeglichkeit hier nur in den Reaktanten zu suchen
				for (int speciesNum = 0; speciesNum < model.getNumSpecies(); speciesNum++) {
					numAndSpecies.get(speciesNum);// name bzw. id suche das in
					// kineticLaw
					// und ersete es
					if (kineticLawPartName.contains(numAndSpecies
							.get(speciesNum))) {
						kineticLawPartName = kineticLawPartName.replaceAll(
								numAndSpecies.get(speciesNum), model
										.getSpecies(speciesNum).getName());
						// reactionNumAndKineticName[reactionNum] =
						// kineticLawPartName;
						kineticLawPartTeXName = kineticLawPartTeXName
								.replaceAll(numAndSpecies.get(speciesNum),
										model.getSpecies(speciesNum).getName());
						reactionNumAndKineticTeXName[reactionNum] = kineticLawPartTeXName;
					}
				}
			}
		}
	}

	/**
	 * Erzeugt TeX f&uuml;r Kinetiken die schon vorher in SBML standen
	 */
	public void setTeX() {
		if (!reacNumOfXexistKinetics.isEmpty()) {
			for (int i = 0; i < reacNumOfXexistKinetics.size(); i++) {
				ListOf<Reaction> listOfReactions = model.getListOfReactions();
				Reaction reaction = listOfReactions.get(reacNumOfXexistKinetics
						.get(i));
				String kinetic = reaction.getKineticLaw().getFormula();
				String2TeX stringToTex = new String2TeX();
				// TODO
				reactionNumAndKineticTeX[i] = stringToTex.getEquation(kinetic)
						.toString();
			}
		}
	}

	/**
	 * TODO: comment missing
	 * 
	 */
	public void simpleODE() {
		ListOf<Reaction> listOfReactions = model.getListOfReactions();
		for (int j = 0; j < model.getNumReactions(); j++) {
			Reaction reaction = listOfReactions.get(j);
			ListOf<SpeciesReference> listOfReactants = reaction
					.getListOfReactants();
			ListOf<SpeciesReference> listOfProducts = reaction
					.getListOfProducts();

			for (int reactantNum = 0; reactantNum < reaction.getNumReactants(); reactantNum++) {
				SpeciesReference specref = (SpeciesReference) listOfReactants
						.get(reactantNum);
				if (!specref.getSpeciesInstance().getBoundaryCondition()) {
					String kinetic;
					kinetic = speciesAndSimpleODE.get(specref.getSpecies());
					if (speciesAndSimpleODE.get(specref.getSpecies()) == null)
						kinetic = "";
					kinetic = kinetic + "-v" + (j + 1);
					speciesAndSimpleODE.put(specref.getSpecies(), kinetic);
					String kineticTeX;
					kineticTeX = speciesAndSimpleODETeX.get(specref
							.getSpecies());
					if (speciesAndSimpleODETeX.get(specref.getSpecies()) == null)
						kineticTeX = "";
					kineticTeX += "-v_{" + (j + 1) + "}";
					speciesAndSimpleODETeX
							.put(specref.getSpecies(), kineticTeX);
					// setODEreac(specref,j);
				} else
					speciesAndSimpleODE.put(specref.getSpecies(), "0");
			}
			for (int productNum = 0; productNum < reaction.getNumProducts(); productNum++) {
				SpeciesReference specref = (SpeciesReference) listOfProducts
						.get(productNum);
				if (!specref.getSpeciesInstance().getBoundaryCondition()) {
					String kinetic;
					kinetic = speciesAndSimpleODE.get(specref.getSpecies());
					if (speciesAndSimpleODE.get(specref.getSpecies()) == null)
						kinetic = "";
					kinetic = kinetic + "+v" + (j + 1);
					speciesAndSimpleODE.put(specref.getSpecies(), kinetic);
					String kineticTeX;
					kineticTeX = speciesAndSimpleODETeX.get(specref
							.getSpecies());
					if (speciesAndSimpleODETeX.get(specref.getSpecies()) == null)
						kineticTeX = "";
					kineticTeX = kineticTeX + "+v_{" + (j + 1) + "}";
					speciesAndSimpleODETeX
							.put(specref.getSpecies(), kineticTeX);
					// setODEpro(specref,j);
				} else {
					speciesAndSimpleODE.put(specref.getSpecies(), "0");
					speciesAndSimpleODETeX.put(specref.getSpecies(), "0");
				}
			}
		}
	}

	void setODEpro(SpeciesReference specref, int reacnum) {
		String ode = speciesAndODE.get(specref.getSpecies());
		String odeTex = specieAndODETeX.get(specref.getSpecies());
		String odeTexId = specieAndODETeXId.get(specref.getSpecies());
		ode = ode + "(" + reactionNumAndKineticName[reacnum] + ") ";
		odeTex = odeTex + "(" + reactionNumAndKineticTeXName[reacnum] + ") ";
		odeTexId = odeTexId + "(" + reactionNumAndKineticTeX[reacnum] + ") ";
		speciesAndODE.put(specref.getSpecies(), ode);
		specieAndODETeX.put(specref.getSpecies(), odeTex);
		specieAndODETeXId.put(specref.getSpecies(), odeTexId);
	}

	void setODEreac(SpeciesReference specref, int reacnum) {
		String ode = speciesAndODE.get(specref.getSpecies());
		String odeTex = specieAndODETeX.get(specref.getSpecies());
		String odeTexId = specieAndODETeXId.get(specref.getSpecies());
		ode = ode + "(" + reactionNumAndKineticName[reacnum] + ") ";
		odeTex = odeTex + "(" + reactionNumAndKineticTeXName[reacnum] + ") ";
		odeTexId = odeTexId + "(" + reactionNumAndKineticTeX[reacnum] + ") ";
		speciesAndODE.put(specref.getSpecies(), ode);
		specieAndODETeX.put(specref.getSpecies(), odeTex);
		specieAndODETeXId.put(specref.getSpecies(), odeTexId);
	}

	/**
	 * 
	 */
	private void initializeSpeciesNumAndIDs() {
		short level = identifyLevel(model);
		for (int i = 0; i < model.getNumSpecies(); i++) {
			Species species = model.getSpecies(i);
			speciesAndODE.put(species.getName(), "");
			// speciesAndODETeX.put(species.getName(), "");
			switch (level) {
			case 1:
				// numAndSpeciesID.put(i, species.getName());
				// speciesIDandNum.put(species.getName(), i);
				break;
			case 2:
				// numAndSpeciesID.put(i, species.getId());
				// speciesIDandNum.put(species.getId(), i);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * get the the level for the SBML-File
	 * 
	 * @return
	 */
	public int getLevel() {
		return identifyLevel(model);
	}

	/**
	 * Identify the level of the SBML-File
	 */
	public short identifyLevel(Model model) {
		if (model.getNumSpecies() > 0) {
			Species species = model.getSpecies(0);
			if (species.getId() != null)
				return 2;
		}
		return 1;
	}

	/**
	 * get HashMap<Integer,String> get all Species
	 * 
	 * @return
	 */
	public HashMap<Integer, String> getAllSpeciesNumAndIDs() {
		return numAndSpecies;
	}
}
