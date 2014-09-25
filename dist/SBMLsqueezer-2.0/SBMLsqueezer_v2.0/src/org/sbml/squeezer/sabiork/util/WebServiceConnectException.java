/*
 * $Id: WebServiceConnectException.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/util/WebServiceConnectException.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2014 by the University of Tuebingen, Germany.
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
package org.sbml.squeezer.sabiork.util;

/**
 * A class of exceptions produced by failed web service connections.
 * 
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class WebServiceConnectException extends Exception {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -2228255799581621829L;
  
  /**
   * 
   */
  public WebServiceConnectException() {
    super();
  }
  
  /**
   * 
   * @param message
   * @param cause
   */
  public WebServiceConnectException(String message, Throwable cause) {
    super(message, cause);
  }
  
  /**
   * 
   * @param message
   */
  public WebServiceConnectException(String message) {
    super(message);
  }
  
  /**
   * 
   * @param cause
   */
  public WebServiceConnectException(Throwable cause) {
    super(cause);
  }
  
}
