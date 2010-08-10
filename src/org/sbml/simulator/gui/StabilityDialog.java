/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.simulator.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.sbml.squeezer.gui.StabilityPanel;
import org.sbml.squeezer.io.SBMLio;

/**
 * A dialog window to start a stability analysis on the currently selected
 * model.
 * 
 * @author Alexander D&ouml;rr
 * @date 2009-12-18
 * @since 1.3
 */
public class StabilityDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	public static final boolean APPROVE_OPTION = true;
	/**
	 * 
	 */
	public static final boolean CANCEL_OPTION = false;
	/**
	 * 
	 */
	private static final String APPLY = "Apply";
	/**
	 * 
	 */
	private static final String CANCEL = "Cancel";
	/**
	 * 
	 */
	private JButton apply;
	/**
	 * 
	 */
	private JButton cancel;
	/**
	 * 
	 */
	private StabilityPanel stabilityPanel;
	/**
	 * 
	 */
	private Properties settings;
	/**
	 * 
	 */
	private SBMLio sbmlIO;
	/**
	 * 
	 */
	private boolean exitStatus;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param owner
	 */
	public StabilityDialog(Frame owner) {
		super(owner, "Stability");
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(CANCEL)) {
			dispose();

		} else if (e.getActionCommand().equals(APPLY)) {
			sbmlIO.getSelectedModel();
		}

	}

	/**
	 * Initializes this dialog.
	 */
	private void init() {
		stabilityPanel = new StabilityPanel(this.settings);
		getContentPane().add(stabilityPanel, BorderLayout.CENTER);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel p = new JPanel();

		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		cancel.setActionCommand(CANCEL);
		cancel.setSize(cancel.getSize());
		apply = new JButton("Apply");
		apply.setSize(cancel.getSize());
		apply.addActionListener(this);
		apply.setActionCommand(APPLY);
		apply.setEnabled(true);
		p.add(cancel);
		p.add(apply);
		add(p, BorderLayout.SOUTH);
	}

	/**
	 * 
	 * @return
	 */
	public boolean showStabilityDialog(Properties settings, SBMLio sbmlIO) {
		this.settings = settings;
		this.sbmlIO = sbmlIO;
		this.exitStatus = CANCEL_OPTION;
		init();
		pack();
		setLocationRelativeTo(getOwner());
		setResizable(false);
		setModal(true);
		setVisible(true);
		return exitStatus;
	}

}
