package org.sbmlsqueezer.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 * TODO: comment missing
 *
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 * @date Aug 1, 2007
 *
 */
public class SettingsWriter {

  BufferedWriter out;

  /**
   * @param file
   * @param maxEducts
   * @param uniUniType
   * @param biUniType
   * @param biBiType
   * @param maxSpeciesWarnings
   * @param noReactionMAK
   * @param possibleEnzymeRNA
   * @param generateKineticForAllReaction
   * @param reversibility
   * @param possibleEnzymeGenericProtein
   * @param possibleEnzymeTruncatedProtein
   * @param possibleEnzymeComplex
   * @param possibleEnzymeUnknown
   * @param possibleEnzymeReceptor
   * @param possibleEnzymeSimpleMolecule
   * @param possibleEnzymeAsRNA
   * @param possibleEnzymeAllNotChecked
   * @param forceAllReactionsAsEnzymeReaction
   */
  public SettingsWriter(File file, int maxEducts, short uniUniType,
      short biUniType, short biBiType, boolean maxSpeciesWarnings,
      boolean noReactionMAK, boolean possibleEnzymeRNA,
      boolean generateKineticForAllReaction, boolean reversibility,
      boolean possibleEnzymeGenericProtein,
      boolean possibleEnzymeTruncatedProtein, boolean possibleEnzymeComplex,
      boolean possibleEnzymeUnknown, boolean possibleEnzymeReceptor,
      boolean possibleEnzymeSimpleMolecule, boolean possibleEnzymeAsRNA,
      boolean possibleEnzymeAllNotChecked,
      boolean forceAllReactionsAsEnzymeReaction) {
    try {
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file
          .getPath())));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    write("SBMLsqeezer Settings-file");
    append("version:1.00");
    append("START");
    append("maxSpecies:" + maxEducts);
    append("uniUniType:" + uniUniType);
    append("biUniType:" + biUniType);
    append("biBiType:" + biBiType);
    append("warnings:" + maxSpeciesWarnings);
    append("noReactionMAK:" + noReactionMAK);
    append("possibleEnzymeRNA:" + possibleEnzymeRNA);
    append("GenKinForAllReac:" + generateKineticForAllReaction);
    append("reversibility:" + reversibility);
    append("possibleEnzymeGenericProtein:" + possibleEnzymeGenericProtein);
    append("possibleEnzymeTruncatedProtein:" + possibleEnzymeTruncatedProtein);
    append("possibleEnzymeComplex:" + possibleEnzymeComplex);
    append("possibleEnzymeUnknown:" + possibleEnzymeUnknown);
    append("possibleEnzymeReceptor:" + possibleEnzymeReceptor);
    append("possibleEnzymeSimpleMolecule:" + possibleEnzymeSimpleMolecule);
    append("possibleEnzymeAsRNA:" + possibleEnzymeAsRNA);
    append("possibleEnzymeAllNotChecked:" + possibleEnzymeAllNotChecked);
    append("forceAllReactionsAsEnzymeReaction:"
        + forceAllReactionsAsEnzymeReaction);
    append("END");
    close();
  }

  private void write(String str) {
    try {
      out.write(str);
      out.newLine();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private void append(String str) {
    try {
      out.write(str);
      out.newLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void close() {
    try {
      out.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

}
