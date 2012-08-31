/*
 * $Id$
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
package org.sbml.squeezer.sabiork.wizard.gui;

import javax.swing.JPanel;

import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.CardID;

import org.sbml.squeezer.sabiork.wizard.model.WizardModel;

/**
 * The abstract base class for all panels to be displayed in the SABIO-RK
 * wizard.
 * 
 * @author Matthias Rall
 * @version $Rev$
 */
public abstract class Card extends JPanel {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 7548783744523732450L;

	protected JDialogWizard dialog;
	protected WizardModel model;

	/**
	 * 
	 * @param dialog
	 * @param model
	 */
	public Card(JDialogWizard dialog, WizardModel model) {
		this.dialog = dialog;
		this.model = model;
	}

	/**
	 * Actions to take place before the actual {@link Card} is displayed.
	 */
	public void performBeforeShowing() {
	};

	/**
	 * Returns the id of the preceding {@link Card}.
	 * 
	 * @return the id of the preceding {@link Card}
	 */
	public abstract CardID getPreviousCardID();

	/**
	 * Returns the id of the succeeding {@link Card}.
	 * 
	 * @return the id of the succeeding {@link Card}
	 */
	public abstract CardID getNextCardID();

}
