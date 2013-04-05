/*
 * $Id: ConsoleHandler.java 02.04.2013 15:14:49 draeger$
 * $URL: ConsoleHandler.java $
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
package org.sbml.squeezer.celldesigner;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.4
 */
public class ConsoleHandler extends java.util.logging.ConsoleHandler {
	
	/**
	 * 
	 */
	public ConsoleHandler() {
		super();
		setOutputStream(System.out);
	}
	
}
