/*
 * $Id: CardMethod.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/gui/CardMethod.java $
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
package org.sbml.squeezer.sabiork.wizard.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.ButtonState;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.CardID;

import org.sbml.squeezer.sabiork.wizard.model.WizardModel;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that allows to choose between the two different methods in the
 * SABIO-RK wizard.
 * 
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class CardMethod extends Card {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 8380771924006772969L;
  
  private ButtonGroup buttonGroup;
  private JLabel labelWelcome;
  private JLabel labelIntroduction;
  private JPanel panelTexts;
  private JPanel panelRadioButtons;
  private JPanel panelMethod;
  private JPanel panelWelcome;
  private JRadioButton radioButtonAutomatic;
  private JRadioButton radioButtonManual;
  
  /**
   * 
   * @param dialog
   * @param model
   */
  public CardMethod(JDialogWizard dialog, WizardModel model) {
    super(dialog, model);
    initialize();
  }
  
  /**
   * 
   */
  private void initialize() {
    labelWelcome = new JLabel(
      WizardProperties.getText("CARD_METHOD_TEXT_WELCOME"));
    labelWelcome.setFont(new Font(getFont().getName(), Font.BOLD, getFont()
      .getSize()));
    
    labelIntroduction = new JLabel(
      WizardProperties.getText("CARD_METHOD_TEXT_INTRODUCTION"));
    
    radioButtonAutomatic = new JRadioButton(
      WizardProperties.getText("CARD_METHOD_TEXT_AUTOMATIC"));
    radioButtonAutomatic.setSelected(true);
    
    radioButtonManual = new JRadioButton(
      WizardProperties.getText("CARD_METHOD_TEXT_MANUAL"));
    
    buttonGroup = new ButtonGroup();
    buttonGroup.add(radioButtonAutomatic);
    buttonGroup.add(radioButtonManual);
    
    panelTexts = new JPanel(new GridLayout(2, 1));
    panelTexts.add(labelWelcome);
    panelTexts.add(labelIntroduction);
    
    panelRadioButtons = new JPanel(new GridBagLayout());
    panelRadioButtons.add(radioButtonAutomatic, new GridBagConstraints(0,
      0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
      GridBagConstraints.NONE, new Insets(10, 10, 5, 0), 0, 0));
    panelRadioButtons.add(radioButtonManual, new GridBagConstraints(0, 1,
      1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
      GridBagConstraints.NONE, new Insets(5, 10, 10, 0), 0, 0));
    
    panelMethod = new JPanel(new BorderLayout());
    panelMethod.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(),
      WizardProperties.getText("CARD_METHOD_TEXT_METHOD")));
    panelMethod.add(panelRadioButtons, BorderLayout.WEST);
    
    panelWelcome = new JPanel(new BorderLayout());
    panelWelcome.setBorder(new EmptyBorder(new Insets(15, 15, 15, 15)));
    panelWelcome.add(panelTexts, BorderLayout.NORTH);
    panelWelcome.add(Box.createRigidArea(new Dimension(0, 25)));
    panelWelcome.add(panelMethod, BorderLayout.SOUTH);
    
    setLayout(new BorderLayout());
    add(panelWelcome, BorderLayout.NORTH);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performBeforeShowing()
   */
  @Override
  public void performBeforeShowing() {
    dialog.setButtonState(ButtonState.START);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getPreviousCardID()
   */
  @Override
  public CardID getPreviousCardID() {
    return CardID.NOT_AVAILABLE;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getNextCardID()
   */
  @Override
  public CardID getNextCardID() {
    if (radioButtonAutomatic.isSelected()) {
      return CardID.REACTIONS_A;
    } else {
      return CardID.REACTIONS_M;
    }
  }
  
}
