/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer;

/**
 * An exception to be thrown if the selected kinetics is not applicable to the
 * given reaction. (Aug 14, 2007)
 * 
 * @since 1.0
 *
 * @author Andreas Dr&auml;ger
 */
public class RateLawNotApplicableException extends Exception {
  
  /**
   * Gernerated serial version identifier.
   */
  private static final long serialVersionUID = -1467056182371100941L;
  
  /**
   * Construct a new exception for cases in which the selected kinetic
   * formalism is not applicable.
   */
  public RateLawNotApplicableException() {
    super();
  }
  
  /**
   * Construct a new exception for cases in which the selected kinetic
   * formalism is not applicable.
   * 
   * @param message
   *            Exception message
   */
  public RateLawNotApplicableException(String message) {
    super(message);
  }
  
  /**
   * Construct a new exception for cases in which the selected kinetic
   * formalism is not applicable.
   * 
   * @param message
   *            Exception message
   * @param cause
   *            Reason for the exception to be thrown.
   */
  public RateLawNotApplicableException(String message, Throwable cause) {
    super(message, cause);
  }
  
}
