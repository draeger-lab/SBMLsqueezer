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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.SBMLsqueezer;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @since 1.3
 * @date 2009-09-06
 * @version
 */
public class SettingsDialog extends JDialog implements ActionListener,
		ItemListener, ChangeListener, KeyListener {

	/**
	 * This will tell us later what the user selected here.
	 */
	private boolean exitStatus;
	public static final boolean CANCEL_OPTION = false;
	public static final boolean APPROVE_OPTION = true;
	private Properties settings;
	private JTextField tfOpenDir;
	private JTextField tfSaveDir;
	private KineticsSettingsPanel kinSettingsPanel;
	private JButton apply;
	private JButton defaults;
	private JButton ok;
	private static final String APPLY = "Apply";
	private static final String CANCEL = "Cancel";
	private static final String DEFAULTS = "Defaults";
	private static final String OK = "OK";

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -8842237776948135071L;

	/**
	 * 
	 * @param owner
	 * @param settings
	 */
	public SettingsDialog(Frame owner) {
		super(owner, "Preferences");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals(CANCEL)) {
			dispose();
		} else if (ae.getActionCommand().equals(DEFAULTS)) {
			Properties p = (Properties) this.settings.clone();
			this.settings = SBMLsqueezer.getDefaultSettings();
			getContentPane().removeAll();
			init();
			this.settings = p;
			apply.setEnabled(true);
			ok.setEnabled(true);
			defaults.setEnabled(false);
			kinSettingsPanel.addChangeListener(this);
			kinSettingsPanel.addItemListener(this);
			validate();
		} else if (ae.getActionCommand().equals(APPLY)
				|| ae.getActionCommand().equals(OK)) {
			this.settings = kinSettingsPanel.getSettings();
			File f = new File(tfOpenDir.getText());
			if (f.exists() && f.isDirectory())
				settings.put(CfgKeys.OPEN_DIR, tfOpenDir.getText());
			else {
				JOptionPane.showMessageDialog(getOwner(), new JLabel(GUITools
						.toHTML("No such directory " + f.getPath() + '.', 40)),
						"Warning", JOptionPane.WARNING_MESSAGE);
				tfOpenDir.setText(settings.get(CfgKeys.OPEN_DIR).toString());
			}
			f = new File(tfSaveDir.getText());
			if (f.exists() && f.isDirectory())
				settings.put(CfgKeys.SAVE_DIR, tfSaveDir.getText());
			else {
				JOptionPane.showMessageDialog(getOwner(), new JLabel(GUITools
						.toHTML("No such directory " + f.getPath() + '.', 40)),
						"Warning", JOptionPane.WARNING_MESSAGE);
				tfSaveDir.setText(settings.get(CfgKeys.SAVE_DIR).toString());
			}
			apply.setEnabled(false);
			exitStatus = APPROVE_OPTION;
			if (ae.getActionCommand().equals(OK))
				dispose();
		}
	}

	/**
	 * 
	 * @return
	 */
	public Properties getSettings() {
		return settings;
	}

	/**
	 * Initializes this dialog.
	 */
	private void init() {
		JTabbedPane tabs = new JTabbedPane();

		/*
		 * Kinetics Settings
		 */
		kinSettingsPanel = new KineticsSettingsPanel(this.settings);
		tabs.addTab("Kinetics settings", kinSettingsPanel);

		/*
		 * Program settings
		 */
		tfOpenDir = new JTextField();
		tfSaveDir = new JTextField();
		tfOpenDir.addKeyListener(this);
		tfSaveDir.addKeyListener(this);
		LayoutHelper lh = new LayoutHelper(new JPanel());
		lh.add(new JLabel("Open directory:"), 0, 0, 1, 1, 1, 0);
		lh.add(new JPanel(), 0, 1, 1, 1, 0, 0);
		lh.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		tfOpenDir.setText(this.settings.get(CfgKeys.OPEN_DIR).toString());
		lh.add(tfOpenDir, 2, 0, 1, 1, 1, 0);
		lh.add(new JLabel("Save directory:"), 0, 2, 1, 1, 1, 0);
		tfSaveDir.setText(this.settings.get(CfgKeys.SAVE_DIR).toString());
		lh.add(tfSaveDir, 2, 2, 1, 1, 1, 0);
		tabs.addTab("Program settings", lh.getContainer());
		setLayout(new BorderLayout());

		/*
		 * LaTeX Settings
		 */
		// TODO!
		tabs.addTab("LaTeX output settings", new JPanel());

		getContentPane().add(tabs, BorderLayout.CENTER);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel p = new JPanel();
		defaults = new JButton("Defaults");
		defaults.addActionListener(this);
		defaults.setActionCommand(DEFAULTS);
		defaults.setEnabled(!this.settings.equals(SBMLsqueezer
				.getDefaultSettings()));
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		cancel.setActionCommand(CANCEL);
		cancel.setSize(defaults.getSize());
		apply = new JButton("Apply");
		apply.setSize(defaults.getSize());
		apply.addActionListener(this);
		apply.setActionCommand(APPLY);
		apply.setEnabled(false);
		ok = new JButton("OK");
		ok.addActionListener(this);
		ok.setActionCommand(OK);
		ok.setSize(defaults.getSize());
		ok.setEnabled(false);
		p.add(cancel);
		p.add(defaults);
		p.add(apply);
		p.add(ok);
		add(p, BorderLayout.SOUTH);
	}

	/**
	 * 
	 * @return
	 */
	public boolean showSettingsDialog(Properties settings) {
		this.settings = settings;
		this.exitStatus = CANCEL_OPTION;
		init();
		pack();
		setLocationRelativeTo(getOwner());
		setResizable(false);
		setModal(true);
		kinSettingsPanel.addItemListener(this);
		kinSettingsPanel.addChangeListener(this);
		setVisible(true);
		return exitStatus;
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		apply.setEnabled(true);
		defaults.setEnabled(true);
		ok.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		apply.setEnabled(true);
		defaults.setEnabled(true);
		ok.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		apply.setEnabled(true);
		defaults.setEnabled(true);
		ok.setEnabled(true);
	}

}
