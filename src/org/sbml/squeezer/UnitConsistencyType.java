/*
 * $Id: UnitConsistencyType.java 19.03.2012 17:52:21 draeger$
 * $URL$
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
package org.sbml.squeezer;

/**
 * This is an enumeration of the two possible ways of how to ensure consistent
 * units within an SBML model; reacting species might either be given in
 * molecule counts ({@link #amount}) or their {@link #concentration}.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sebastian Nagel
 * @date 2010-10-29
 * @version $Rev$
 * @since 1.4
 */
public enum UnitConsistencyType {
	/**
	 * 
	 */
	amount,
	/**
	 * 
	 */
	concentration;
}
