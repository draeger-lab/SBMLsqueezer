/*
 * Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
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
import java.util.Properties;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.LawListener;
import org.sbml.squeezer.io.SBMLio;

/**
 * This class allows SBMLsqueezer to create a kinetic law interactively for just
 * one reaction.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-10-22
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
	private Properties settings;
	private int value;
	private LawListener lawListener;

	/**
	 * 
	 * @param kineticLawSelectionDialog
	 * @param settings
	 * @param sbmlIO
	 * @param reactionID
	 * @throws Throwable
	 */
	public KineticLawWindowAdapter(JDialog dialog, Properties settings,
			SBMLio sbmlIO, String reactionID, LawListener l) throws Throwable {
		super();
		this.value = JOptionPane.CLOSED_OPTION;
		this.dialog = dialog;
		this.sbmlio = sbmlIO;
		this.gotFocus = false;
		this.KineticsAndParametersStoredInSBML = false;
		this.settings = settings;
		this.lawListener = l;

		Model model = sbmlIO.getSelectedModel();
		reaction = model.getReaction(reactionID);
		klg = new KineticLawGenerator(model, reactionID, settings);
		messagePanel = new KineticLawSelectionPanel(klg, reaction);

		pane = new JOptionPane(messagePanel, JOptionPane.QUESTION_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION, GUITools.ICON_LEMON_SMALL, null,
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

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentHidden(java.awt.event.
	 * ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent
	 * )
	 */
	public void componentMoved(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.awt.event.ComponentListener#componentResized(java.awt.event.
	 * ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent
	 * )
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
		if (value == JOptionPane.OK_OPTION
				&& !messagePanel.getExistingRateLawSelected()) {
			String equationType = messagePanel.getSelectedKinetic();
			reaction.setReversible(messagePanel.getReversible());
			sbmlio.stateChanged(reaction);
			klg.getSettings().put(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE,
					Boolean.valueOf(messagePanel.getReversible()));
			try {
				klg.storeKineticLaw(klg.createKineticLaw(reaction,
						equationType, messagePanel.getReversible()),
						lawListener);
				sbmlio.saveChanges(reaction);
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
			SBMLsqueezerUI.checkForSBMLErrors(dialog,
					sbmlio.getSelectedModel(), sbmlio.getWriteWarnings(),
					((Boolean) settings.get(CfgKeys.SHOW_SBML_WARNINGS))
							.booleanValue());
			KineticsAndParametersStoredInSBML = true;
		} else
			KineticsAndParametersStoredInSBML = false;
		return KineticsAndParametersStoredInSBML;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosed(WindowEvent we) {
		// setParentEnabled(we, true);
		super.windowClosed(we);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent we) {
		pane.setValue(null);
		// setParentEnabled(we, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowAdapter#windowGainedFocus(java.awt.event.WindowEvent
	 * )
	 */
	public void windowGainedFocus(WindowEvent we) {
		// Once window gets focus, set initial focus
		if (!gotFocus) {
			pane.selectInitialValue();
			gotFocus = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seejava.beans.PropertyChangeListener#propertyChange(java.beans.
	 * PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		// Let the defaultCloseOperation handle the closing
		// if the user closed the window without selecting a button
		// (newValue = null in that case). Otherwise, close the
		// dialog.
		if (dialog.isVisible() && event.getSource() == pane
				&& (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY))
				&& event.getNewValue() != null
				&& event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
			Object selectedValue = pane.getValue();
			value = JOptionPane.CLOSED_OPTION;
			if (pane.getOptions() == null && selectedValue instanceof Integer)
				value = ((Integer) selectedValue).intValue();
			dialog.setVisible(false);
		}
	}
}
