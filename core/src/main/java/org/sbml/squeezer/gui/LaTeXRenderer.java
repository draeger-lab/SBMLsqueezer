/*
 * $Id: EquationRenderer.java 716 2011-12-06 14:15:30Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/EquationRenderer.java $
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

package org.sbml.squeezer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import de.zbit.sbml.gui.EquationRenderer;
import de.zbit.util.ResourceManager;

/**
 * Provide all needed functions for the latex renderer.
 * 
 * @author Sebastian Nagel
 * @since 2.0
 * 
 */
public class LaTeXRenderer implements EquationRenderer {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(LaTeXRenderer.class.getName());
  
  /**
   * 
   */
  private JButton buttonZoomIn, buttonZoomOut;
  
  /**
   * 
   */
  private Dimension dimension;
  
  /**
   * 
   */
  private float fontSize;
  
  /**
   * 
   */
  private JPanel panel;
  
  /**
   * 
   */
  private JScrollPane scroll;
  
  /**
   * 
   */
  private String texCode;
  
  /**
   * 
   */
  public LaTeXRenderer() {
    this(450, 80);
  }
  
  /**
   * 
   * @param width
   * @param height
   */
  public LaTeXRenderer(int width, int height) {
    super();
    dimension = new Dimension(width, height);
    fontSize = 14;
    panel = new JPanel(new BorderLayout());
    panel.setOpaque(true);
    buttonZoomIn = new JButton(UIManager.getIcon("ICON_ZOOM_IN_16"));
    buttonZoomOut = new JButton(UIManager.getIcon("ICON_ZOOM_OUT_16"));
    ResourceBundle bundle = ResourceManager.getBundle("org.sbml.squeezer.gui.locales.Messages");
    
    buttonZoomIn.setToolTipText(bundle.getString("ZOOM_IN_TOOLTIP"));
    buttonZoomIn.setBorderPainted(false);
    buttonZoomIn.setOpaque(true);
    buttonZoomIn.addActionListener(EventHandler.create(ActionListener.class, this, "zoomIn"));
    
    buttonZoomOut.setToolTipText(bundle.getString("ZOOM_OUT_TOOLTIP"));
    buttonZoomOut.setBorderPainted(false);
    buttonZoomOut.setOpaque(true);
    buttonZoomOut.addActionListener(EventHandler.create(ActionListener.class, this, "zoomOut"));
    
    JToolBar controlPanel = new JToolBar();
    controlPanel.add(buttonZoomIn);
    controlPanel.add(buttonZoomOut);
    controlPanel.setOpaque(true);
    controlPanel.setOrientation(SwingConstants.VERTICAL);
    panel.add(controlPanel, BorderLayout.WEST);
  }
  
  /**
   * @return the dimension
   */
  public Dimension getDimension() {
    return dimension;
  }
  
  /**
   * @return the fontSize
   */
  public float getFontSize() {
    return fontSize;
  }
  
  /**
   * @return the texCode
   */
  public String getTexCode() {
    return texCode;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.gui.Renderer#printNamesIfAvailable()
   */
  @Override
  public boolean printNamesIfAvailable() {
    return true;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.sbml.gui.Renderer#renderEquation(java.lang.String)
   */
  @Override
  public JComponent renderEquation(String equation) {
    //return new sHotEqn(equation);
    
    if (scroll != null) {
      panel.remove(scroll);
    }
    
    texCode = equation.replace("dmath", "align");
    
    logger.fine(MessageFormat.format("Font size: {0,number,integer}", fontSize));
    logger.fine(texCode);
    
    TeXFormula formula = new TeXFormula(texCode);
    TeXIcon texIcon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, fontSize, TeXFormula.SANSSERIF);
    JLabel teXDisplay = new JLabel(texIcon);
    teXDisplay.setBackground(Color.WHITE);
    
    scroll = new JScrollPane(teXDisplay);
    scroll.setBorder(BorderFactory.createLoweredBevelBorder());
    scroll.setBackground(Color.WHITE);
    scroll.setPreferredSize(dimension);
    
    panel.add(scroll, BorderLayout.CENTER);
    
    return panel;
  }
  
  /**
   * @param dimension the dimension to set
   */
  public void setDimension(Dimension dimension) {
    this.dimension = dimension;
    panel.validate();
  }
  
  /**
   * @param fontSize the fontSize to set
   */
  public void setFontSize(float fontSize) {
    this.fontSize = fontSize;
  }
  
  /**
   * @param equation the texCode to set
   */
  public void setTexCode(String equation) {
    renderEquation(equation);
    panel.validate();
  }
  
  /**
   * 
   */
  public void zoomIn() {
    if (texCode != null) {
      fontSize++;
      if (!buttonZoomOut.isEnabled()) {
        buttonZoomOut.setEnabled(true);
      }
      renderEquation(texCode);
      panel.validate();
    }
  }
  
  /**
   * 
   */
  public void zoomOut() {
    if (texCode != null) {
      if (fontSize > 1) {
        fontSize--;
        renderEquation(texCode);
      } else {
        buttonZoomOut.setEnabled(false);
      }
      panel.validate();
    }
  }
  
}
