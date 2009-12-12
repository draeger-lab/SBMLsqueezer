package org.sbmlsqueezer.kinetics;

import jp.sbi.celldesigner.plugin.PluginSpecies;

/**
 *
 * TODO: comment missing
 *
 * @since 2.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 * @date Aug 1, 2007
 *
 */
public class Species extends PluginSpecies {

  /**
   *
   * TODO: comment missing
   *
   * @param species
   * @return
   */
  public static String toTeX(String species) {
    int index = species.length();
    for (int i = 0; i < 10; i++) {
      String j = "" + i;
      if (index > species.indexOf(j) && species.indexOf(j) > 0) {
        index = species.indexOf(j);
      }
    }
    String num = species.substring(index);
    String speciesTex = "[\\text{" + species.substring(0, index) + "}";
    speciesTex += num.length() > 0 ? "_{" + num + "}]" : "]";

    return speciesTex;
  }


  /**
   *
   * TODO: comment missing
   *
   * @param species
   * @return
   */
  public static String idToTeX(String species) {
    String idTeX = toTeX(species);
    return idTeX.substring(1, idTeX.length() - 1);
  }

}
