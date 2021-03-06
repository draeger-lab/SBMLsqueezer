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
package org.sbml.squeezer.test.gui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;

import javax.swing.JDialog;

import org.junit.Ignore;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;

import de.zbit.io.OpenedFile;
import de.zbit.sbml.gui.SBMLModelSplitPane;

/**
 * A dialog showing the structure of a model. Just for testing purposes. (2010-04-09)
 * 
 * @author Andreas Dr&auml;ger
 * 
 * @since 2.0
 */
@Ignore
public class ModelDialog extends JDialog {
  
  /**
   * 
   */
  private static final long serialVersionUID = 414365858348606128L;
  
  /**
   * 
   * @param owner
   * @param model
   * @throws HeadlessException
   */
  public ModelDialog(Dialog owner, Model model)
      throws HeadlessException {
    super(owner);
    init(model);
    setVisible(true);
  }
  
  /**
   * 
   * @param owner
   * @param model
   * @throws HeadlessException
   */
  public ModelDialog(Frame owner, Model model)
      throws HeadlessException {
    super(owner);
    init(model);
    setVisible(true);
  }
  
  /**
   * 
   * @param model
   */
  public ModelDialog(Model model) {
    super();
    init(model);
    setVisible(true);
  }
  
  /**
   * 
   * @param model
   */
  public ModelDialog(String title, Model model) {
    super();
    init(model);
    setTitle(title);
    setVisible(true);
  }
  
  /**
   * 
   * @param model
   */
  private void init(Model model) {
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    setModal(true);
    setTitle("JSBML Model structure");
    try {
      getContentPane().add(new SBMLModelSplitPane(new OpenedFile<SBMLDocument>(model.getSBMLDocument()), true));
    } catch (Exception e) {
      e.printStackTrace();
    }
    pack();
    setLocationRelativeTo(getOwner());
  }
  
}
