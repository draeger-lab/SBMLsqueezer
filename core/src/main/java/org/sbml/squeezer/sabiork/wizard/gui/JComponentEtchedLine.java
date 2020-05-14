/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016 by the University of Tuebingen, Germany.
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
package org.sbml.squeezer.sabiork.wizard.gui;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.border.EtchedBorder;

/**
 * A class for drawing an etched horizontal line.
 * 
 * @author Matthias Rall
 *
 * @since 2.0
 */
public class JComponentEtchedLine extends JComponent {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 2648773760851854370L;
  
  /**
   * 
   */
  public JComponentEtchedLine() {
    setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    setPreferredSize(new Dimension(1, 2));
    setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
  }
  
}