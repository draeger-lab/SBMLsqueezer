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

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.ButtonState;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.CardID;

import org.sbml.squeezer.sabiork.wizard.model.KineticLawImporter;
import org.sbml.squeezer.sabiork.wizard.model.WizardModel;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that provides a short summary of the changes made by the SABIO-RK
 * wizard.
 * 
 * @author Matthias Rall
 * 
 * @since 2.0
 */
public class CardSummaryM extends Card {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 5350885163785722780L;
  private JScrollPane textAreaSummaryScrollPane;
  private JTextArea textAreaSummary;
  
  /**
   * 
   * @param dialog
   * @param model
   */
  public CardSummaryM(JDialogWizard dialog, WizardModel model) {
    super(dialog, model);
    initialize();
  }
  
  /**
   * 
   */
  private void initialize() {
    textAreaSummary = new JTextArea();
    textAreaSummary.setEnabled(false);
    textAreaSummaryScrollPane = new JScrollPane(textAreaSummary);
    
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(),
      WizardProperties.getText("CARD_SUMMARY_M_TEXT_SUMMARY")));
    add(textAreaSummaryScrollPane, BorderLayout.CENTER);
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performBeforeShowing()
   */
  @Override
  public void performBeforeShowing() {
    if (model.hasSelectedKineticLawImporter()) {
      KineticLawImporter selectedKineticLawImporter = model
          .getSelectedKineticLawImporter();
      if (selectedKineticLawImporter.isImportableKineticLaw()) {
        selectedKineticLawImporter.importKineticLaw();
        textAreaSummary.setText(selectedKineticLawImporter.getReport());
      }
    }
    dialog.setButtonState(ButtonState.FINISH);
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getPreviousCardID()
   */
  @Override
  public CardID getPreviousCardID() {
    return CardID.MATCHING;
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getNextCardID()
   */
  @Override
  public CardID getNextCardID() {
    return CardID.CONFIRM_DIALOG;
  }
  
}
