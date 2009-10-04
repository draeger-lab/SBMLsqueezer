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
package org.sbml.squeezer;

public interface LawListener {

	/**
	 * Allows you to tell this listener which number (in a list or in an array
	 * or what ever) you are currently working with.
	 * 
	 * @param num
	 *            The current element.
	 */
	public void currentNumber(int num);

	/**
	 * Allows you to tell this listener the total number of elements to work
	 * with.
	 * 
	 * @param i
	 *            Number of elements.
	 */
	public void totalNumber(int i);

}
