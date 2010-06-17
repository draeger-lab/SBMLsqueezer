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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.math;

/**
 * This class represents an exception that is thrown when the model to be simulated
 * is overdetermined
 * 
 * @author Alexander D&ouml;rr
 * @since 1.4
 */
public class ModelOverdeterminedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5288546434951201722L;

	/**
	 * 
	 */
	public ModelOverdeterminedException() {
		super();
	}

	/**
	 * @param message
	 */
	public ModelOverdeterminedException(String message) {
		super(message);
		
	}

	/**
	 * @param cause
	 */
	public ModelOverdeterminedException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ModelOverdeterminedException(String message, Throwable cause) {
		super(message, cause);
	
	}

}
