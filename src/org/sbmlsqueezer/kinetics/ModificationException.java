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
 * TODO: comment missing
 *
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
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
