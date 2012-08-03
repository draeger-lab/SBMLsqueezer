/*
 * $Id: WebServiceResponseException.java 958 2012-08-03 13:28:57Z keller$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/util/WebServiceResponseException.java$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
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
@SuppressWarnings("serial")

/**
 * @author Matthias Rall
 * @version $Rev$
 * ${tags}
 */
public class WebServiceResponseException extends Exception {

	int responseCode;

	public WebServiceResponseException(int responseCode) {
		super();
		this.responseCode = responseCode;
	}

	public WebServiceResponseException(String message, Throwable cause, int responseCode) {
		super(message, cause);
		this.responseCode = responseCode;
	}

	public WebServiceResponseException(String message, int responseCode) {
		super(message);
		this.responseCode = responseCode;
	}

	public WebServiceResponseException(Throwable cause, int responseCode) {
		super(cause);
		this.responseCode = responseCode;
	}

	public int getResponseCode() {
		return responseCode;
	}

}
