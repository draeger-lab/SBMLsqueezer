/**
 * Aug 13, 2007
 *
 * @since 2.0
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 **/
package org.sbmlsqueezer.kinetics;

/**
 * TODO: comment missing
 *
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Aug 13, 2007
 */
public class ModificationException extends RateLawNotApplicableException {

  /**
   * ID
   */
  private static final long serialVersionUID = 495775752324636450L;

  /**
   * Construct a new exception for cases in which transcriptional activation or
   * translational activation or inhibition, respectively, was used where
   * regular activation or inhibition had to be applied or the other way
   * arround.
   */
  public ModificationException() {
    super();
  }

  /**
   * Construct a new exception for cases in which transcriptional activation or
   * translational activation or inhibition, respectively, was used where
   * regular activation or inhibition had to be applied or the other way
   * arround.
   *
   * @param message
   *          Exception message
   */
  public ModificationException(String message) {
    super(message);
  }

  /**
   * Construct a new exception for cases in which transcriptional activation or
   * translational activation or inhibition, respectively, was used where
   * regular activation or inhibition had to be applied or the other way
   * arround.
   *
   * @param message
   *          Exception message
   * @param cause
   *          Reason for the exception to be thrown.
   */
  public ModificationException(String message, Throwable cause) {
    super(message, cause);
  }

}
