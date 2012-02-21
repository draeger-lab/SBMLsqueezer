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
package org.sbml.squeezer.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.io.SBMLio;

import de.zbit.gui.GUITools;
import de.zbit.util.AbstractProgressBar;
import de.zbit.util.prefs.SBPreferences;

/**
 * This class allows SBMLsqueezer to create a kinetic law interactively for just
 * one reaction.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2009-10-22
 * @since 1.3
 * @version $Rev$
 */
public class KineticLawWindowAdapter extends WindowAdapter implements
		ComponentListener, PropertyChangeListener {

	private boolean gotFocus;
	private boolean KineticsAndParametersStoredInSBML;
	private JOptionPane pane;
	private JDialog dialog;
	private SBMLio sbmlio;
	private KineticLawGenerator klg;
	private KineticLawSelectionPanel messagePanel;
	private Reaction reaction;
	private SBPreferences prefs;
	private int value;

	/**
	 * 
	 * @param kineticLawSelectionDialog
	 * @param settings
	 * @param sbmlIO
	 * @param reactionID
	 * @param progressListener 
	 * @throws Throwable
	 */
	public KineticLawWindowAdapter(JDialog dialog, SBMLio sbmlIO, String reactionID) throws Throwable {
		super();
		this.prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
		this.value = JOptionPane.CLOSED_OPTION;
		this.dialog = dialog;
		this.sbmlio = sbmlIO;
		this.gotFocus = false;
		this.KineticsAndParametersStoredInSBML = false;

		Model model = sbmlIO.getSelectedModel();
		reaction = model.getReaction(reactionID);
		klg = new KineticLawGenerator(model, reactionID);
		messagePanel = new KineticLawSelectionPanel(klg, reaction);

		pane = new JOptionPane(messagePanel, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, UIManager.getIcon("ICON_LEMON_SMALL"), null,
				null);
		pane.setInitialValue(null);
		Window owner = dialog.getOwner();
		pane.setComponentOrientation(((owner == null) ? JOptionPane
				.getRootFrame() : owner).getComponentOrientation());
		Container contentPane = this.dialog.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(pane, BorderLayout.CENTER);

		this.dialog.setComponentOrientation(pane.getComponentOrientation());
		this.dialog.addWindowListener(this);
		this.dialog.addWindowListener(this);
		this.dialog.addWindowFocusListener(this);
		this.dialog.addComponentListener(this);
		pane.addPropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @seejava.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e) {
	}

	/* (non-Javadoc)
	 * @seejava.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {
		// reset value to ensure closing works properly
		pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isKineticsAndParametersStoredInSBML() {
		if ((value == JOptionPane.OK_OPTION)
				&& !messagePanel.getExistingRateLawSelected()) {
			String equationType = messagePanel.getSelectedKinetic();
			reaction.addTreeNodeChangeListener(sbmlio);
			reaction.setReversible(messagePanel.getReversible());
			klg.getPreferences().put(SqueezerOptions.OPT_TREAT_ALL_REACTIONS_REVERSIBLE,
					Boolean.valueOf(messagePanel.getReversible()));
			try {
				// TODO: Do this in background using SwingWorker!
				KineticLaw kineticLaw = klg.createKineticLaw(reaction,
					equationType, messagePanel.getReversible());
				klg.storeKineticLaw(kineticLaw);
				sbmlio.saveChanges(reaction);
			} catch (Throwable exc) {
				GUITools.showErrorMessage(dialog, exc);
			}
			SBMLsqueezerUI.checkForSBMLErrors(dialog,
					sbmlio.getSelectedModel(), sbmlio.getWriteWarnings(), prefs
							.getBoolean(SqueezerOptions.SHOW_SBML_WARNINGS));
			KineticsAndParametersStoredInSBML = true;
		} else {
			KineticsAndParametersStoredInSBML = false;
		}
		return KineticsAndParametersStoredInSBML;
	}

	public void showProgress(AbstractProgressBar progressBar){
		klg.setProgressBar(progressBar);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent we) {
		// setParentEnabled(we, true);
		super.windowClosed(we);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent we) {
		pane.setValue(null);
		// setParentEnabled(we, true);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowAdapter#windowGainedFocus(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowGainedFocus(WindowEvent we) {
		// Once window gets focus, set initial focus
		if (!gotFocus) {
			pane.selectInitialValue();
			gotFocus = true;
		}
	}

	/* (non-Javadoc)
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		// Let the defaultCloseOperation handle the closing
		// if the user closed the window without selecting a button
		// (newValue = null in that case). Otherwise, close the
		// dialog.
		if (dialog.isVisible() && (event.getSource() == pane)
				&& event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)
				&& (event.getNewValue() != null)
				&& (event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE)) {
			Object selectedValue = pane.getValue();
			value = JOptionPane.CLOSED_OPTION;
			if (pane.getOptions() == null && selectedValue instanceof Integer)
				value = ((Integer) selectedValue).intValue();
			dialog.setVisible(false);
		}
	}
}
