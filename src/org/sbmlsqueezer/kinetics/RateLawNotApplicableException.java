/**
 * Aug 14, 2007
 *
 * @since 2.0
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 */
package org.sbmlsqueezer.kinetics;

/**
 * An exception to be thrown if the selected kinetics is not applicable to the
 * given reaction.
 *
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Aug 14, 2007
 */
public class RateLawNotApplicableException extends Exception {

  /**
   * ID
   */
  private static final long serialVersionUID = -1467056182371100941L;

  /**
   * Construct a new exception for cases in which the selected kinetic formalism
   * is not applicable.
   */
  public RateLawNotApplicableException() {
    super();
  }

  /**
   * Construct a new exception for cases in which the selected kinetic formalism
   * is not applicable.
   *
   * @param message
   *          Exception message
   */
  public RateLawNotApplicableException(String message) {
    super(message);
  }

  /**
   * Construct a new exception for cases in which the selected kinetic formalism
   * is not applicable.
   *
   * @param message
   *          Exception message
   * @param cause
   *          Reason for the exception to be thrown.
   */
  public RateLawNotApplicableException(String message, Throwable cause) {
    super(message, cause);
  }

}
