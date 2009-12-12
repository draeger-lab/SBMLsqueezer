/**
 * Aug 3, 2007
 *
 * @since 2.0
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 **/
package org.sbmlsqueezer.kinetics;

/**
 * Exception for cases in which the SBML file may be corrupted
 *
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 * @date Aug 3, 2007
 */
public class IllegalFormatException extends Exception {

  /**
   * ID
   */
  private static final long serialVersionUID = 5281372527605594662L;

  /**
   * Construct a new exception for cases in which the SBML file may be corrupted.
   */
  public IllegalFormatException() {
    super();
  }
  
  /**
   * Construct a new exception for cases in which the SBML file may be corrupted.
   * @param message Exception message
   */
  public IllegalFormatException(String message) {
    super(message);
  }
  
  /**
   * Construct a new exception for cases in which the SBML file may be corrupted.
   * @param message Exception message
   * @param cause Reason for the exception to be thrown.
   */
  public IllegalFormatException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
