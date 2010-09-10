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
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.squeezer.SBMLsqueezer;

/**
 * A specialized {@link JDialog} that shows several configuration options in a
 * {@link JTabbedPane}, provides a button for applying the choosen selection and
 * also to restore the default settings. All settings are synchronized with the
 * central configuration of {@link SBMLsqueezer}.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.3
 * @date 2009-09-06
 * @version
 */
public class SettingsDialog extends JDialog implements ActionListener,
		ItemListener, ChangeListener, KeyListener {

	public static final boolean APPROVE_OPTION = true;
	public static final boolean CANCEL_OPTION = false;
	private static final String APPLY = "Apply";
	private static final String CANCEL = "Cancel";
	private static final String DEFAULTS = "Defaults";
	private static final String OK = "OK";
	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -8842237776948135071L;
	private JButton apply;
	private JButton defaults;
	/**
	 * This will tell us later what the user selected here.
	 */
	private boolean exitStatus;
	private JButton ok;
	private SettingsPanel panelAllSettings;
	private Properties settings;
	private Properties defaultSettings;

	/**
	 * 
	 * @param owner
	 * @param settings
	 */
	public SettingsDialog(Frame owner, Properties defaultSettings) {
		super(owner, "Preferences");
		this.defaultSettings = defaultSettings;
	}

	/**
	 * 
	 */
	public SettingsDialog(String title, Properties defaultSettings) {
		super();
		setTitle(title);
		this.defaultSettings = defaultSettings;
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
			Properties p = (Properties) settings.clone();
			settings = defaultSettings;
			panelAllSettings.setProperties(settings);
			settings = p;
			apply.setEnabled(true);
			ok.setEnabled(true);
			defaults.setEnabled(false);
			panelAllSettings.addChangeListener(this);
			panelAllSettings.addItemListener(this);
			panelAllSettings.addKeyListener(this);
			validate();
		} else if (ae.getActionCommand().equals(APPLY)
				|| ae.getActionCommand().equals(OK)) {
			settings.putAll(panelAllSettings.getProperties());
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
	private void init(SettingsPanel panel) {
		panelAllSettings = panel;
		getContentPane().add(panelAllSettings, BorderLayout.CENTER);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel p = new JPanel();
		defaults = new JButton("Defaults");
		defaults.addActionListener(this);
		defaults.setActionCommand(DEFAULTS);
		defaults.setEnabled(!settings.equals(defaultSettings));
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
		
		cancel.setPreferredSize(defaults.getPreferredSize());
		apply.setPreferredSize(defaults.getPreferredSize());
		ok.setPreferredSize(defaults.getPreferredSize());
		
		p.add(cancel);
		p.add(defaults);
		p.add(apply);
		p.add(ok);
		add(p, BorderLayout.SOUTH);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		apply.setEnabled(true);
		defaults.setEnabled(true);
		ok.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		apply.setEnabled(true);
		defaults.setEnabled(true);
		ok.setEnabled(true);
	}

	/**
	 * 
	 * @return
	 */
	public boolean showSettingsDialog(Properties settings) {
		return showSettingsDialog(settings, new SettingsPanelAll(settings));
	}

	/**
	 * 
	 * @return
	 */
	public boolean showSettingsDialog(Properties settings, SettingsPanel panel) {
		this.settings = settings;
		this.exitStatus = CANCEL_OPTION;
		init(panel);
		pack();
		setLocationRelativeTo(getOwner());
		setResizable(false);
		setModal(true);
		panelAllSettings.addItemListener(this);
		panelAllSettings.addChangeListener(this);
		setVisible(true);
		return exitStatus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		apply.setEnabled(true);
		defaults.setEnabled(true);
		ok.setEnabled(true);
	}
}
