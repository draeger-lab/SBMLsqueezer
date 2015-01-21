/*
 * $Id: WebServiceResponseException.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/util/WebServiceResponseException.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2015 by the University of Tuebingen, Germany.
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
 * A class of exceptions that occurred during the processing of HTTP requests.
 * 
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class WebServiceResponseException extends Exception {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -6398077215479639974L;
  
  int responseCode;
  
  /**
   * 
   * @param responseCode
   */
  public WebServiceResponseException(int responseCode) {
    super();
    this.responseCode = responseCode;
  }
  
  /**
   * 
   * @param message
   * @param cause
   * @param responseCode
   */
  public WebServiceResponseException(String message, Throwable cause,
    int responseCode) {
    super(message, cause);
    this.responseCode = responseCode;
  }
  
  /**
   * 
   * @param message
   * @param responseCode
   */
  public WebServiceResponseException(String message, int responseCode) {
    super(message);
    this.responseCode = responseCode;
  }
  
  /**
   * 
   * @param cause
   * @param responseCode
   */
  public WebServiceResponseException(Throwable cause, int responseCode) {
    super(cause);
    this.responseCode = responseCode;
  }
  
  /**
   * 
   * @return responseCode the response code of the exception
   */
  public int getResponseCode() {
    return responseCode;
  }
  
}
