/*
 * $Id:  OptionPanelTest.java 12:42:09 draeger$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2014 by the University of Tuebingen, Germany.
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

package org.sbml.squeezer.test;

import java.awt.HeadlessException;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.sbml.squeezer.OptionsGeneral;

import de.zbit.gui.prefs.PreferencesPanelForKeyProvider;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.4
 */
public class OptionPanelTest {
  
  /**
   * @param args
   * @throws IOException 
   * @throws HeadlessException 
   * @throws UnsupportedLookAndFeelException 
   * @throws IllegalAccessException 
   * @throws InstantiationException 
   * @throws ClassNotFoundException 
   */
  public static void main(String[] args) throws HeadlessException, IOException,
    ClassNotFoundException, InstantiationException, IllegalAccessException,
    UnsupportedLookAndFeelException {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    JOptionPane.showMessageDialog(null, new PreferencesPanelForKeyProvider(
      OptionsGeneral.class));
  }
  
}
