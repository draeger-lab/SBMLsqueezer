package org.sbmlsqueezer.kinetics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.sbmlsqueezer.io.String2TeX;

import jp.sbi.celldesigner.plugin.PluginListOf;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

/**
 * TODO: comment missing
 *
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Nadine Hassis <Nadine.hassis@gmail.com> Copyright (c) ZBiT,
 *         University of T&uuml;bingen, Germany Compiler: JDK 1.6.0
 * @date Aug 1, 2007
 */
public class ODE {

  private HashMap<Integer, String> numAndSpecies           = new HashMap<Integer, String>();

  private HashMap<String, String>  speciesAndODE           = new HashMap<String, String>();

  private HashMap<String, String>  specieAndODETeX         = new HashMap<String, String>();

  private HashMap<String, String>  specieAndODETeXId       = new HashMap<String, String>();

  private HashMap<String, String>  speciesAndSimpleODE     = new HashMap<String, String>();

  private HashMap<String, String>  speciesAndSimpleODETeX  = new HashMap<String, String>();

  private List<Integer>            reactionNumber          = new ArrayList<Integer>();

  private List<Integer>            reacNumOfXexistKinetics = new ArrayList<Integer>();

  private String[]                 reactionNumAndKineticTeX;

  private String[]                 reactionNumAndKineticName;

  private String[]                 reactionNumAndKineticTeXName;

  private PluginModel              model;

  /**
   * @param model
   * @param numAndSpecies
   * @param speciesAndODE
   * @param specieAndODEtex
   * @param reactionNumber
   * @param reacNumofexistKinetics
   * @param reactionNumAndKineticLaw
   */
  public ODE(PluginModel model, HashMap<Integer, String> numAndSpecies,
      HashMap<String, String> speciesAndODE,
      HashMap<String, String> specieAndODEtex, List<Integer> reactionNumber,
      List<Integer> reacNumofexistKinetics,
      HashMap<Integer, BasicKineticLaw> reactionNumAndKineticLaw) {
    this.model = model;
    this.numAndSpecies.putAll(numAndSpecies);
    this.speciesAndODE.putAll(speciesAndODE);
    this.specieAndODETeX.putAll(specieAndODEtex);
    this.reactionNumber.addAll(reactionNumber);
    this.reacNumOfXexistKinetics.addAll(reacNumofexistKinetics);
    this.specieAndODETeXId.putAll(specieAndODEtex);
    this.speciesAndSimpleODE.putAll(speciesAndODE);
    this.speciesAndSimpleODETeX.putAll(specieAndODEtex);
    this.reactionNumAndKineticTeX = new String[model.getNumReactions()];
    this.reactionNumAndKineticTeXName = new String[model.getNumReactions()];
    this.reactionNumAndKineticName = new String[model.getNumReactions()];
    Arrays.fill(reactionNumAndKineticName, "Existing Kinetic");
    for (Iterator<Integer> i = reactionNumAndKineticLaw.keySet().iterator(); i
        .hasNext();) {
      Integer key = i.next();
      BasicKineticLaw law = reactionNumAndKineticLaw.get(key);
      reactionNumAndKineticTeX[key.intValue()] = law.getKineticTeX();
      reactionNumAndKineticName[key.intValue()] = law.getName();
    }
    setTeX();
    idToName();
    simpleODE();
    correctness();
    // setODE();
  }

  void setODEreac(PluginSpeciesReference specref, int reacnum) {
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

  void setODEpro(PluginSpeciesReference specref, int reacnum) {
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

  public void idToName() {
    for (int reactionNum = 0; reactionNum < model.getNumReactions(); reactionNum++) {
      PluginReaction reaction = model.getReaction(reactionNum);
      if (reaction.getKineticLaw() != null) {
        String kineticLawPartName = reaction.getKineticLaw().getFormula();
        String kineticLawPartTeXName = reactionNumAndKineticTeX[reactionNum];
        // Moeglichkeit hier nur in den Reaktanten zu suchen
        for (int speciesNum = 0; speciesNum < model.getNumSpecies(); speciesNum++) {
          numAndSpecies.get(speciesNum);// name bzw. id suche das in kineticLaw
          // und ersete es
          if (kineticLawPartName.contains(numAndSpecies.get(speciesNum))) {
            kineticLawPartName = kineticLawPartName.replaceAll(numAndSpecies
                .get(speciesNum), model.getSpecies(speciesNum).getName());
            // reactionNumAndKineticName[reactionNum] = kineticLawPartName;
            kineticLawPartTeXName = kineticLawPartTeXName.replaceAll(
                numAndSpecies.get(speciesNum), model.getSpecies(speciesNum)
                    .getName());
            reactionNumAndKineticTeXName[reactionNum] = kineticLawPartTeXName;
          }
        }
      }
    }
  }

  /**
   * TODO: comment missing
   *
   */
  public void simpleODE() {
    PluginListOf listOfReactions = model.getListOfReactions();
    for (int j = 0; j < model.getNumReactions(); j++) {
      PluginReaction reaction = (PluginReaction) listOfReactions.get(j);
      PluginListOf listOfReactants = reaction.getListOfReactants();
      PluginListOf listOfProducts = reaction.getListOfProducts();

      for (int reactantNum = 0; reactantNum < reaction.getNumReactants(); reactantNum++) {
        PluginSpeciesReference specref = (PluginSpeciesReference) listOfReactants
            .get(reactantNum);
        if (!specref.getSpeciesInstance().getBoundaryCondition()) {
          String kinetic;
          kinetic = speciesAndSimpleODE.get(specref.getSpecies());
          if (speciesAndSimpleODE.get(specref.getSpecies()) == null)
            kinetic = "";
          kinetic = kinetic + "-v" + (j+1);
          speciesAndSimpleODE.put(specref.getSpecies(), kinetic);
          String kineticTeX;
          kineticTeX = speciesAndSimpleODETeX.get(specref.getSpecies());
          if (speciesAndSimpleODETeX.get(specref.getSpecies()) == null)
            kineticTeX = "";
          kineticTeX += "-v_{" + (j+1) + "}";
          speciesAndSimpleODETeX.put(specref.getSpecies(), kineticTeX);
          // setODEreac(specref,j);
        } else speciesAndSimpleODE.put(specref.getSpecies(), "0");
      }
      for (int productNum = 0; productNum < reaction.getNumProducts(); productNum++) {
        PluginSpeciesReference specref = (PluginSpeciesReference) listOfProducts
            .get(productNum);
        if (!specref.getSpeciesInstance().getBoundaryCondition()) {
          String kinetic;
          kinetic = speciesAndSimpleODE.get(specref.getSpecies());
          if (speciesAndSimpleODE.get(specref.getSpecies()) == null)
            kinetic = "";
          kinetic = kinetic + "+v" + (j+1);
          speciesAndSimpleODE.put(specref.getSpecies(), kinetic);
          String kineticTeX;
          kineticTeX = speciesAndSimpleODETeX.get(specref.getSpecies());
          if (speciesAndSimpleODETeX.get(specref.getSpecies()) == null)
            kineticTeX = "";
          kineticTeX = kineticTeX + "+v_{" + (j+1) + "}";
          speciesAndSimpleODETeX.put(specref.getSpecies(), kineticTeX);
          // setODEpro(specref,j);
        } else {
          speciesAndSimpleODE.put(specref.getSpecies(), "0");
          speciesAndSimpleODETeX.put(specref.getSpecies(), "0");
        }
      }
    }
  }

  public void correctness() {
    for (int speciesNum = 0; speciesNum < model.getNumSpecies(); speciesNum++) {
      String kinetic;
      String kineticTeX;
      kinetic = speciesAndSimpleODE.get(numAndSpecies.get(speciesNum));
      kineticTeX = speciesAndSimpleODETeX.get(numAndSpecies.get(speciesNum));
      if ((kinetic == null) || (kineticTeX == null)) {
        kinetic = "0";
        kineticTeX = "0";
        speciesAndSimpleODE.put(numAndSpecies.get(speciesNum), kinetic);
        speciesAndSimpleODETeX.put(numAndSpecies.get(speciesNum), kineticTeX);
      } else if ((kinetic.length() == 0) || (kineticTeX.length() == 0)) {
        kinetic = "0";
        kineticTeX = "0";
        speciesAndSimpleODE.put(numAndSpecies.get(speciesNum), kinetic);
        speciesAndSimpleODETeX.put(numAndSpecies.get(speciesNum), kineticTeX);
      } else if (kinetic.charAt(0) == '+') {
        speciesAndSimpleODE.put(numAndSpecies.get(speciesNum), kinetic
            .substring(1));
        speciesAndSimpleODETeX.put(numAndSpecies.get(speciesNum), kineticTeX
            .substring(1));
      }
    }
  }

  /**
   * Erzeugt TeX f&uuml;r Kinetiken die schon vorher in SBML standen
   */
  public void setTeX() {
    if (!reacNumOfXexistKinetics.isEmpty()) {
      for (int i = 0; i < reacNumOfXexistKinetics.size(); i++) {
        PluginListOf listOfReactions = model.getListOfReactions();
        PluginReaction reaction = (PluginReaction) listOfReactions
            .get(reacNumOfXexistKinetics.get(i));
        String kinetic = reaction.getKineticLaw().getFormula();
        String2TeX stringToTex = new String2TeX();
        // TODO
        reactionNumAndKineticTeX[i] = stringToTex.getEquation(kinetic);
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

  public HashMap<String, String> getSpecieAndSimpleODE() {
    return speciesAndSimpleODE;
  }

  public HashMap<String, String> getSpeciesAndSimpleODETeX() {
    return speciesAndSimpleODETeX;
  }

  public String[] getReactionNumAndKinetictexId() {
    return reactionNumAndKineticTeX;
  }

  public String[] getKineticLawNames() {
    return reactionNumAndKineticName;
  }

}
