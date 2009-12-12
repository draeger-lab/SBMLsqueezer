package org.sbmlsqueezer.kinetics;

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
public class PingPongMechanism extends BasicKineticLaw {

  /**
   * @param parentReaction
   * @param model
   * @param reversibility
   * @param listOfPossibleEnzymes
   * @throws RateLawNotApplicableException
   */
  public PingPongMechanism(PluginReaction parentReaction, PluginModel model,
      List<String> listOfPossibleEnzymes) throws RateLawNotApplicableException {
    super(parentReaction, model, listOfPossibleEnzymes);
  }

  /**
   * @param parentReaction
   * @param model
   * @param reversibility
   * @throws RateLawNotApplicableException
   */
  public PingPongMechanism(PluginReaction parentReaction, PluginModel model,
      boolean reversibility) throws RateLawNotApplicableException {
    super(parentReaction, model);
  }

  @Override
  protected String createKineticEquation(PluginModel model, int reactionNum,
      List<String> modE, List<String> modActi, List<String> modTActi,
      List<String> modInhib, List<String> modTInhib, List<String> modCat)
      throws RateLawNotApplicableException {
    String numerator = "", numeratorTeX = ""; // I
    String denominator = "", denominatorTeX = ""; // II
    String inhib = "";
    String inhibTeX = "";
    String acti = "";
    String actiTeX = "";
    String formelTxt = formelTeX = "";

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
      String kcatp, kcatpTeX;
      String kMr1 = "kM_" + reactionNum;
      String kMr2 = "kM_" + reactionNum;
      String kMr1TeX = "k^\\text{M}_{" + reactionNum;
      String kMr2TeX = "k^\\text{M}_{" + reactionNum;

      if (modE.size() == 0) {
        kcatp = "Vp_" + reactionNum;
        kcatpTeX = "V^\\text{m}_{+" + reactionNum;
      } else {
        kcatp = "kcatp_" + reactionNum;
        kcatpTeX = "k^\\text{cat}_{+" + reactionNum;
        if (modE.size() > 1) {
          kcatp += "_" + modE.get(enzymeNum);
          kMr1 += "_" + modE.get(enzymeNum);
          kMr2 += "_" + modE.get(enzymeNum);
          kcatpTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
          kMr1TeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
          kMr2TeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
        }
      }
      kMr2 += "_" + specRefE2.getSpecies();
      kMr1 += "_" + specRefE1.getSpecies();
      if (specRefE2.equals(specRefE1)) {
        kMr1 = "kMr1" + kMr1.substring(2);
        kMr2 = "kMr2" + kMr2.substring(2);
        kMr1TeX = "k^\\text{Mr1" + kMr1TeX.substring(9);
        kMr2TeX = "k^\\text{Mr2" + kMr2TeX.substring(9);
      }
      kcatpTeX += "}";
      kMr1TeX += ",{" + Species.idToTeX(specRefE1.getSpecies())
          + "}}";
      kMr2TeX += ",{" + Species.idToTeX(specRefE2.getSpecies())
          + "}}";

      if (!paraList.contains(kcatp)) paraList.add(kcatp);
      if (!paraList.contains(kMr2)) paraList.add(kMr2);
      if (!paraList.contains(kMr1)) paraList.add(kMr1);

      /*
       * Irreversible Reaction
       */
      if (!reaction.getReversible()) {
        numerator = kcatp + " * ";
        numeratorTeX = kcatpTeX;
        if (modE.size() > 0) {
          numerator += modE.get(enzymeNum) + " * ";
          numeratorTeX += Species.toTeX(modE.get(enzymeNum));
        }
        numerator += specRefE1.getSpecies();
        numeratorTeX += Species.toTeX(specRefE1.getSpecies());
        denominator = kMr2 + " * " + specRefE1.getSpecies()
            + " + " + kMr1 + " * " + specRefE2.getSpecies()
            + " + " + specRefE1.getSpecies();
        denominatorTeX = kMr2TeX
            + Species.toTeX(specRefE1.getSpecies()) + " + "
            + kMr1TeX + Species.toTeX(specRefE2.getSpecies())
            + " + " + Species.toTeX(specRefE1.getSpecies());
        if (specRefE2.equals(specRefE1)) {
          numerator += "^2";
          numeratorTeX += "^2";
          denominator += "^2";
          denominatorTeX += "^2";
        } else {
          numerator += " * " + specRefE2.getSpecies();
          numeratorTeX += Species.toTeX(specRefE2.getSpecies());
          denominator += " * " + specRefE2.getSpecies();
          denominatorTeX += Species.toTeX(specRefE2.getSpeciesInstance()
              .getId());
        }

        /*
         * Reversible Reaction
         */
      } else {
        String kcatn, kcatnTeX;
        String kMp1 = "kM_" + reactionNum;
        String kMp2 = "kM_" + reactionNum;
        String kIp1 = "ki_" + reactionNum;
        String kIp2 = "ki_" + reactionNum;
        String kIr1 = "ki_" + reactionNum;
        String kMp1TeX = "k^\\text{M}_{" + reactionNum;
        String kMp2TeX = "k^\\text{M}_{" + reactionNum;
        String kIp1TeX = "k^\\text{i}_{" + reactionNum;
        String kIp2TeX = "k^\\text{i}_{" + reactionNum;
        String kIr1TeX = "k^\\text{i}_{" + reactionNum;

        if (modE.size() == 0) {
          kcatn = "Vn_" + reactionNum;
          kcatnTeX = "V^\\text{m}_{-" + reactionNum;
        } else {
          kcatn = "kcatn_" + reactionNum;
          kcatnTeX = "k^\\text{cat}_{-" + reactionNum;
          if (modE.size() > 1) {
            kcatn += "_" + modE.get(enzymeNum);
            kMp1 += "_" + modE.get(enzymeNum);
            kMp2 += "_" + modE.get(enzymeNum);
            kIp1 += "_" + modE.get(enzymeNum);
            kIp2 += "_" + modE.get(enzymeNum);
            kIr1 += "_" + modE.get(enzymeNum);
            kcatnTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
            kMp1TeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
            kMp2TeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
            kIp1TeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
            kIp2TeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
            kIr1TeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
          }
        }
        kMp1 += "_" + specRefP1.getSpecies();
        kMp2 += "_" + specRefP2.getSpecies();
        kIp1 += "_" + specRefP1.getSpecies();
        kIp2 += "_" + specRefP2.getSpecies();
        kIr1 += "_" + specRefE1.getSpecies();
        if (specRefP2.equals(specRefP1)) {
          kMp1 = "kMp1" + kMp1.substring(2);
          kMp2 = "kMp2" + kMp2.substring(2);
          kIp1 = "kip1" + kIp1.substring(2);
          kIp2 = "kip2" + kIp2.substring(2);
          kMp1TeX = "k^\\text{Mp1" + kMp1TeX.substring(9);
          kMp2TeX = "k^\\text{Mp2" + kMp2TeX.substring(9);
          kIp1TeX = "k^\\text{ip1" + kIp1TeX.substring(9);
          kIp2TeX = "k^\\text{ip2" + kIp2TeX.substring(9);
        }
        kcatnTeX += "}";
        kMp1TeX += ",{"
            + Species.idToTeX(specRefP1.getSpecies()) + "}}";
        kMp2TeX += ",{"
            + Species.idToTeX(specRefP2.getSpecies()) + "}}";
        kIp1TeX += ",{"
            + Species.idToTeX(specRefP1.getSpecies()) + "}}";
        kIp2TeX += ",{"
            + Species.idToTeX(specRefP2.getSpecies()) + "}}";
        kIr1TeX += ",{"
            + Species.idToTeX(specRefE1.getSpecies()) + "}}";

        if (!paraList.contains(kcatn)) paraList.add(kcatn);
        if (!paraList.contains(kMp2)) paraList.add(kMp2);
        if (!paraList.contains(kMp1)) paraList.add(kMp1);
        if (!paraList.contains(kIp1)) paraList.add(kIp1);
        if (!paraList.contains(kIp2)) paraList.add(kIp2);
        if (!paraList.contains(kIr1)) paraList.add(kIr1);

        numerator = kcatp + "/(" + kIr1 + " * " + kMr2 + ") * ";
        numeratorTeX = "\\frac{" + kcatpTeX + "}{" + kIr1TeX + kMr2TeX + "}";
        if (modE.size() > 0) {
          numerator += modE.get(enzymeNum) + " * ";
          numeratorTeX += Species.toTeX(modE.get(enzymeNum));
        }
        numerator += specRefE1.getSpecies();
        numeratorTeX += Species.toTeX(specRefE1.getSpecies());
        denominator = specRefE1.getSpecies() + "/" + kIr1
            + " + (" + kMr1 + " * " + specRefE2.getSpecies()
            + ")/(" + kIr1 + " * " + kMr2 + ") + "
            + specRefP1.getSpecies() + "/" + kIp1 + " + ("
            + kMp1 + " * " + specRefP2.getSpecies() + ")/("
            + kIp1 + " * " + kMp2 + ") + ";
        denominatorTeX = "\\frac{"
            + Species.toTeX(specRefE1.getSpecies()) + "}{"
            + kIr1TeX + "}+\\frac{" + kMr1TeX
            + Species.toTeX(specRefE2.getSpecies()) + "}{"
            + kIr1TeX + kMr2TeX + "}+\\frac{"
            + Species.toTeX(specRefP1.getSpecies()) + "}{"
            + kIp1TeX + "}+\\frac{" + kMp1TeX
            + Species.toTeX(specRefP2.getSpecies()) + "}{"
            + kIp1TeX + kMp2TeX + "}+\\frac{"
            + Species.toTeX(specRefE1.getSpecies());
        if (specRefE2.equals(specRefE1)) {
          numerator += "^2";
          numeratorTeX += "^2";
          denominator += "^2";
          denominatorTeX += "^2";
        } else {
          numerator += " * " + specRefE2.getSpecies();
          numeratorTeX += Species.toTeX(specRefE2.getSpecies());
          denominator += "(" + specRefE1.getSpecies() + " * "
              + specRefE2.getSpecies() + ")";
          denominatorTeX += Species.toTeX(specRefE2.getSpeciesInstance()
              .getId());
        }
        numerator += " - " + kcatn + "/(" + kIp1 + " * " + kMp2 + ") * ";
        numeratorTeX += "-\\frac{" + kcatnTeX + "}{" + kIp1TeX + kMp2TeX + "}";
        denominator += "/(" + kIr1 + " * " + kMr2 + ") + ("
            + specRefE1.getSpecies() + " * "
            + specRefP1.getSpecies() + "/" + kIr1 + " * "
            + kIp1 + ") + (" + kMr1 + " * "
            + specRefE2.getSpecies() + " * "
            + specRefP2.getSpecies() + ")/(" + kIr1 + " * "
            + kMr2 + " * " + kIp2 + ") + ";
        denominatorTeX += "}{" + kIr1TeX + kMr2TeX + "}+\\frac{"
            + Species.toTeX(specRefE1.getSpecies())
            + Species.toTeX(specRefP1.getSpecies()) + "}{"
            + kIr1TeX + kIp1TeX + "}+\\frac{" + kMr1TeX
            + Species.toTeX(specRefE2.getSpecies())
            + Species.toTeX(specRefP2.getSpecies()) + "}{"
            + kIr1TeX + kMr2TeX + kIp2TeX + "}+\\frac{"
            + Species.toTeX(specRefP1.getSpecies());
        if (modE.size() > 0) {
          numerator += modE.get(enzymeNum) + " * ";
          numeratorTeX += Species.toTeX(modE.get(enzymeNum));
        }
        numerator += specRefP1.getSpecies();
        numeratorTeX += Species.toTeX(specRefP1.getSpecies());
        if (specRefP2.equals(specRefP1)) {
          numerator += "^2";
          numeratorTeX += "^2";
          denominator += specRefP1.getSpecies() + "^2";
          denominatorTeX += "^2";
        } else {
          numerator += " * " + specRefP2.getSpecies();
          numeratorTeX += Species.toTeX(specRefP2.getSpecies());
          denominator += "(" + specRefP1.getSpecies() + " * "
              + specRefP2.getSpecies() + ")";
          denominatorTeX += Species.toTeX(specRefP2.getSpeciesInstance()
              .getId());
        }
        denominator += "/(" + kIp1 + " * " + kMp2 + ")";
        denominatorTeX += "}{" + kIp1TeX + kMp2TeX + "}";
      }

      /*
       * Construct formula.
       */
      formelTxt += "(" + numerator + ")/(" + denominator + ")";
      formelTeX += "\\frac{" + numeratorTeX + "}{" + denominatorTeX + "}";
      if (enzymeNum < (modE.size() - 1)) {
        formelTxt += " + ";
        formelTeX += "\\\\+";
      }
      enzymeNum++;
    } while (enzymeNum <= modE.size() - 1);

    /*
     * Activation
     */
    if (!modActi.isEmpty()) {
      for (int activatorNum = 0; activatorNum < modActi.size(); activatorNum++) {
        String kA = "kA_" + reactionNum + "_" + modActi.get(activatorNum), kATeX = "k^\\text{A}_{"
            + reactionNum
            + ",{"
            + Species.idToTeX(modActi.get(activatorNum))
            + "}}";
        if (!paraList.contains(kA)) paraList.add(kA);
        acti += "(" + modActi.get(activatorNum) + "/(" + kA + " + "
            + modActi.get(activatorNum) + ")) * ";
        actiTeX += "\\frac{" + Species.toTeX(modActi.get(activatorNum)) + "}{"
            + kATeX + " + " + Species.toTeX(modActi.get(activatorNum))
            + "}\\cdot ";
      }
    }
    /*
     * Inhibition
     */
    if (!modInhib.isEmpty()) {
      for (int inhibitorNum = 0; inhibitorNum < modInhib.size(); inhibitorNum++) {
        String kI = "kI_" + reactionNum + "_" + modInhib.get(inhibitorNum);
        String kItex = "k^\\text{I}_{" + reactionNum + ",{"
            + Species.idToTeX(modInhib.get(inhibitorNum)) + "}}";
        paraList.add(kI);
        inhib += "(" + kI + "/(" + kI + " + " + modInhib.get(inhibitorNum)
            + ")) * ";
        inhibTeX += "\\frac{" + kItex + "}{" + kItex + " + "
            + Species.toTeX(modInhib.get(inhibitorNum)) + "}\\cdot ";
      }
    }
    if ((acti.length() + inhib.length() > 0) && (modE.size() > 1)) {
      inhib += "(";
      formelTxt += ")";
      inhibTeX += inhibTeX.substring(0, inhibTeX.length() - 6)
          + "\\\\\\cdot\\left(";
      formelTeX = formelTeX.replaceAll("\\\\\\+", "\\right.\\\\\\\\+\\\\left.")
          + "\\right)";
    }
    formelTxt = acti + inhib + formelTxt;
    formelTeX = actiTeX + inhibTeX + formelTeX;

    if (enzymeNum > 1) formelTeX += "\\end{multline}";
    return formelTxt;
  }

  @Override
  public String getName() {
    // according to Cornish-Bowden: Fundamentals of Enzyme kinetics
    String name = "substituted-enzyme mechanism (Ping-Pong)";
    if (getParentReaction().getReversible()) return "reversible " + name;
    return "irreversible " + name;
  }

  public static boolean isApplicable(PluginReaction reaction) {
    // TODO
    return true;
  }

	@Override
  public String getSBO() {
	  return "none";
  }

}
