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
 * An exception to be thrown if the selected kinetics is not applicable to the
 * given reaction.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Aug 14, 2007
 */
public class RateLawNotApplicableException extends Exception {

	/**
	 * ID
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
