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
package org.sbmlsqueezer.kinetics;

/**
 * Exception for cases in which the SBML file may be corrupted
 *
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
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
