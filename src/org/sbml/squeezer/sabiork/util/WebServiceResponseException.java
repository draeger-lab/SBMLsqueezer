/*
 * $$Id${file_name} ${time} ${user} $$
 * $$URL${file_name} $$
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

/**
 * A class of exceptions that occurred during the processing of HTTP requests.
 * 
 * @author Matthias Rall
 * @version $$Rev$$
 * ${tags}
 */
@SuppressWarnings("serial")
public class WebServiceResponseException extends Exception {

	int responseCode;

	public WebServiceResponseException(int responseCode) {
		super();
		this.responseCode = responseCode;
	}

	public WebServiceResponseException(String message, Throwable cause,
			int responseCode) {
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
