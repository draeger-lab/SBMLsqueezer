/*
 * $Id: EquationRenderer.java 716 2011-12-06 14:15:30Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/EquationRenderer.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
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

import java.awt.Component;

import javax.swing.border.Border;

import de.zbit.sbml.gui.Renderer;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

import atp.sHotEqn;

import org.sbml.tolatex.LaTeXOptions;

/**
 * Provide all needed functions for the sHotEqn latex renderer.
 * 
 * @author Sebastian Nagel
 * @since 1.4
 * @version $Rev: 716 $
 */
public class EquationRenderer implements Renderer{
	public EquationRenderer() {
		
	}
	
	public Class<? extends KeyProvider> getLaTeXOptions() {
		return LaTeXOptions.class;
	}
	
	public Option<Boolean> printNamesIfAvailable() {
		return LaTeXOptions.PRINT_NAMES_IF_AVAILABLE;
	}
	
	public Component getEquation(String equation){
		return new sHotEqn(equation);
	}
	
	public Component setBorder(String equation, Border border) {
		sHotEqn eqn = new sHotEqn(equation);
		eqn.setBorder(border);
		return eqn;
	}
}
