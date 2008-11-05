/**
 * Aug 7, 2007
 *
 * @since 2.0
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 */
package org.sbmlsqueezer.kinetics;

import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;

/**
 * TODO: comment missing
 *
 * @since 2.0
 * @version
 * @author Jochen Supper
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.5.0
 * @date Aug 7, 2007
 */
public class HillEquation extends BasicKineticLaw {

  /**
   * TODO Add comment
   *
   * @param mod
   * @param reactionNum
   * @param modActi
   * @param modInhib
   * @param reversibility
   * @param modTActi
   * @param modTInhib
   * @throws ModificationException
   */
  public HillEquation(PluginReaction parentReaction, PluginModel model,
      List<String> listOfPossibleEnzymes) throws RateLawNotApplicableException {
    super(parentReaction, model, listOfPossibleEnzymes);
  }

  /**
   * @param parentReaction
   * @param model
   * @param reversibility
   * @throws RateLawNotApplicableException
   */
  public HillEquation(PluginReaction parentReaction, PluginModel model)
      throws RateLawNotApplicableException {
    super(parentReaction, model);
  }

  @Override
  protected String createKineticEquation(PluginModel model, int reactionNum,
      List<String> modE, List<String> modActi, List<String> modTActi,
      List<String> modInhib, List<String> modTInhib, List<String> modCat)
      throws RateLawNotApplicableException {
    if (!modActi.isEmpty())
    	throw new ModificationException("Wrong activation in reaction "
          + model.getReaction(reactionNum).getId()
          + ". Only transcriptional or translational activation, "
          + "respectively, is allowed here.");
    if (!modInhib.isEmpty())
      throw new ModificationException("Wrong inhibition in reaction "
          + reactionNum
          + ". Only transcriptional or translational inhibition, "
          + "respectively, is allowed here.");

    PluginReaction reaction = getParentReaction();
    for (int modifier = 0; modifier < reaction.getNumModifiers(); modifier++) {
      String modificationType = reaction.getModifier(modifier)
          .getModificationType().split("_")[1].toLowerCase();
      if (reaction.getReactant(0).getSpeciesInstance().getSpeciesAlias(0)
          .getType().toUpperCase().equals("GENE")
          && reaction.getModifier(modifier).getModificationType().toUpperCase()
              .startsWith("TRANSLATIONAL")) 
        throw new ModificationException("Wrong activation in reaction "
            + reactionNum + ". Only transcriptional " + modificationType
            + "is allowed here.");
      else if (reaction.getReactant(0).getSpeciesInstance().getSpeciesAlias(0)
          .getType().toUpperCase().contains("RNA")
          && reaction.getModifier(modifier).getModificationType().toUpperCase()
              .startsWith("TRANSCRIPTIONAL"))
        throw new ModificationException("Wrong activation in reaction "
            + reactionNum + ". Only translational " + modificationType
            + " is allowed here.");
    }

    String acti = "";
    String inhib = "";
    String actiTeX = "";
    String inhibTeX = "";
    reactionNum++;

    // KS: half saturation constant.
    for (int activatorNum = 0; activatorNum < modTActi.size(); activatorNum++) {
      String kS = "kSp_" + reactionNum + "_" + modTActi.get(activatorNum), hillcoeff = "np_"
          + reactionNum + "_" + modTActi.get(activatorNum);
      acti += " * " + modTActi.get(activatorNum) + "^" + hillcoeff + "/("
          + modTActi.get(activatorNum) + "^" + hillcoeff + " + " + kS + "^"
          + hillcoeff + ")";

      if (!paraList.contains(hillcoeff)) paraList.add(hillcoeff);
      if (!paraList.contains(kS)) paraList.add(kS);

      kS = "k^\\text{S}_{+" + reactionNum + ",{"
          + Species.idToTeX(modTActi.get(activatorNum)) + "}}";
      hillcoeff = "n_{+" + reactionNum + ",{"
          + Species.idToTeX(modTActi.get(activatorNum)) + "}}";
      actiTeX += "\\cdot\\frac{" + Species.toTeX(modTActi.get(activatorNum))
          + "^{" + hillcoeff + "}}{" + Species.toTeX(modTActi.get(activatorNum))
          + "^{" + hillcoeff + "} + \\left(" + kS + "\\right)^{" + hillcoeff
          + "}}";
    }
    if (acti.length() > 2) {
      acti = acti.substring(3);
      actiTeX = actiTeX.substring(5);
    }

    for (int inhibitorNum = 0; inhibitorNum < modTInhib.size(); inhibitorNum++) {
      String kS = "kSm_" + reactionNum + "_" + modTInhib.get(inhibitorNum), hillcoeff = "nm_"
          + reactionNum + "_" + modTInhib.get(inhibitorNum);
      inhib += " * (1 - " + modTInhib.get(inhibitorNum) + "^" + hillcoeff + "/("
          + modTInhib.get(inhibitorNum) + "^" + hillcoeff + " + " + kS + "^"
          + hillcoeff + "))";

      if (!paraList.contains(hillcoeff)) paraList.add(hillcoeff);
      if (!paraList.contains(kS)) paraList.add(kS);

      kS = "k^\\text{S}_{-" + reactionNum + ",{"
          + Species.idToTeX(modTInhib.get(inhibitorNum)) + "}}";
      hillcoeff = "n_{-" + reactionNum + ",{"
          + Species.idToTeX(modTInhib.get(inhibitorNum)) + "}}";
      inhibTeX += "\\cdot\\left(1 - \\frac{"
          + Species.toTeX(modTInhib.get(inhibitorNum)) + "^{" + hillcoeff
          + "}}{" + Species.toTeX(modTInhib.get(inhibitorNum)) + "^{"
          + hillcoeff + "} + \\left(" + kS + "\\right)^{" + hillcoeff
          + "}}\\right)";
    }
    if (inhib.length() > 2) {
      // cut the multiplication symbol at the beginning.
      inhib = inhib.substring(3);
      inhibTeX = inhibTeX.substring(5);
    }

    formelTeX = "k^\\text{g}_" + reactionNum;
    String formelTxt = "kg_" + reactionNum;
    if (!paraList.contains(formelTxt)) paraList.add(formelTxt);
    if ((actiTeX.length() > 0) && (inhibTeX.length() > 0)) {
      formelTeX += "\\cdot " + actiTeX + "\\cdot " + inhibTeX;
      formelTxt += " * " + acti + " * " + inhib;
    } else if (actiTeX.length() > 0) {
      formelTeX += "\\cdot " + actiTeX;
      formelTxt += " * " + acti;
    } else if (inhibTeX.length() > 0) {
      formelTeX += "\\cdot " + inhibTeX;
      formelTxt += " * " + inhib;
    }

    // Influence of the concentrations of the educts:
    for (int reactantNum = 0; reactantNum < reaction.getNumReactants(); reactantNum++) {
      PluginSpecies reactant = reaction.getReactant(reactantNum)
          .getSpeciesInstance();
      if (!reactant.getSpeciesAlias(0).getType().toUpperCase().equals("GENE")) {
        formelTxt += " * " + reactant.getId();
        if (reaction.getReactant(reactantNum).getStoichiometry() != 1.0) {
          formelTxt += "^"
              + reaction.getReactant(reactantNum).getStoichiometry();
        }
        formelTeX += "\\cdot " + Species.toTeX(reactant.getId()) + "^{";
        if (reaction.getReactant(reactantNum).getStoichiometry() != 1.0)
          if (reaction.getReactant(reactantNum).getStoichiometry()
              - ((int) reaction.getReactant(reactantNum).getStoichiometry()) == 0)
            formelTeX += Integer.toString((int) reaction.getReactant(
                reactantNum).getStoichiometry());
          else formelTeX += reaction.getReactant(reactantNum)
              .getStoichiometry();
        formelTeX += "}";
      }
    }
    return formelTxt;
  }

  @Override
  public String getName() {
  	String name;
  	if (getParentReaction().getNumModifiers() > 0)
  		name = "Hill equation, microscopic form";
  	else
  		name = "zeroth order irreversible mass action kinetics, continuous scheme";
    return name;
  }

  public static boolean isApplicable(PluginReaction reaction) {
    // TODO
    return true;
  }

	@Override
  public String getSBO() {
		String name = getName().toLowerCase(), sbo = "none";
		if (name.equals("Hill equation"))
			sbo = "0000192";
		else if (name.equals("hill equation, microscopic form"))
			sbo = "0000195";
		else if (name.equals("hill equation, reduced form"))
			sbo = "0000198";
		else if (name.equals("zeroth order irreversible mass action kinetics, continuous scheme"))
			sbo = "0000047";
	  return sbo;
  }

}
