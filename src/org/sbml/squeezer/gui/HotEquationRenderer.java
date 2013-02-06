/*
 * $Id: EquationRenderer.java 716 2011-12-06 14:15:30Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/EquationRenderer.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2013 by the University of Tuebingen, Germany.
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

package org.sbml.squeezer.gui;

import javax.swing.JComponent;

import atp.sHotEqn;
import de.zbit.sbml.gui.EquationRenderer;

/**
 * Provide all needed functions for the {@link sHotEqn} latex renderer.
 * 
 * @author Sebastian Nagel
 * @since 1.4
 * @version $Rev: 716 $
 */
public class HotEquationRenderer implements EquationRenderer {
	
	/**
	 * 
	 */
	public HotEquationRenderer() {
	  super();
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.Renderer#renderEquation(java.lang.String)
	 */
	public JComponent renderEquation(String equation) {
		return new sHotEqn(equation);
	}

	/* (non-Javadoc)
	 * @see de.zbit.sbml.gui.Renderer#printNamesIfAvailable()
	 */
	public boolean printNamesIfAvailable() {
		return true;
	}

}
