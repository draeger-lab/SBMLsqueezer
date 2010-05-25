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
package org.sbml.squeezer.util;

/**
 * A pair of two values.
 * 
 * @author Andreas Dr&auml;ger
 * @date 16:18:26
 */
public class ValuePair<S, T> {

	/**
	 * First value
	 */
	private S s;
	/**
	 * Second value
	 */
	private T t;

	public ValuePair() {
		this(null, null);
	}
	
	public ValuePair(S s, T t) {
		this.setFirstValue(s);
		this.setSecondValue(t);
	}

	/**
	 * @return the s
	 */
	public S getFirstValue() {
		return s;
	}

	/**
	 * @return the t
	 */
	public T getSecondValue() {
		return t;
	}

	/**
	 * @param s the s to set
	 */
	public void setFirstValue(S s) {
		this.s = s;
	}

	/**
	 * @param t the t to set
	 */
	public void setSecondValue(T t) {
		this.t = t;
	}

}
