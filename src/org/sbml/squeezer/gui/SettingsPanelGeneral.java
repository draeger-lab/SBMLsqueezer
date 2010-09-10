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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.sbml.squeezer.CfgKeys;

import de.zbit.gui.LayoutHelper;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-09-10
 */
public class SettingsPanelGeneral extends SettingsPanel implements
		ActionListener {

	public enum Command {
		/**
		 * 
		 */
		OPEN,
		/**
		 * 
		 */
		SAVE
	}

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -5344644880108822465L;

	/**
	 * 
	 */
	private JTextField tfOpenDir, tfSaveDir;

	/**
	 * 
	 */
	private JSpinner stepSize, maxSpinnerValue;

	/**
	 * Explaining text for the buttons.
	 */
	private static final String toolTipButtons = "Select the default directory to %s various kinds of files.";

	/**
	 * 
	 * @param properties
	 * @param defaults
	 */
	public SettingsPanelGeneral(Properties properties, Properties defaults) {
		super(properties, defaults);
		setProperties(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() != null) {
			switch (Command.valueOf(e.getActionCommand())) {
			case OPEN:
				chooseDirectory(tfOpenDir, Command.OPEN);
				break;
			case SAVE:
				chooseDirectory(tfSaveDir, Command.SAVE);
				break;
			default:
				break;
			}
		}
	}

	/**
	 * 
	 * @param tf
	 * @param com
	 */
	private void chooseDirectory(JTextField tf, Command com) {
		JFileChooser chooser = GUITools.createJFileChooser(tf.getText(), false,
				false, JFileChooser.DIRECTORIES_ONLY);
		int returnType;
		switch (com) {
		case OPEN:
			returnType = chooser.showOpenDialog(this);
			break;
		case SAVE:
			returnType = chooser.showSaveDialog(this);
			break;
		default:
			returnType = JFileChooser.CANCEL_OPTION;
			break;
		}
		if (returnType == JFileChooser.APPROVE_OPTION) {
			tf.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#getProperties()
	 */
	@Override
	public Properties getProperties() {
		File f = new File(tfOpenDir.getText());
		if (f.exists() && f.isDirectory()) {
			settings.put(CfgKeys.OPEN_DIR, tfOpenDir.getText());
		} else {
			JOptionPane.showMessageDialog(getTopLevelAncestor(), new JLabel(
					GUITools.toHTML("No such directory " + f.getPath() + '.',
							40)), "Warning", JOptionPane.WARNING_MESSAGE);
			tfOpenDir.setText(settings.get(CfgKeys.OPEN_DIR).toString());
		}
		f = new File(tfSaveDir.getText());
		if (f.exists() && f.isDirectory()) {
			settings.put(CfgKeys.SAVE_DIR, tfSaveDir.getText());
		} else {
			JOptionPane.showMessageDialog(getTopLevelAncestor(), new JLabel(
					GUITools.toHTML("No such directory " + f.getPath() + '.',
							40)), "Warning", JOptionPane.WARNING_MESSAGE);
			tfSaveDir.setText(settings.get(CfgKeys.SAVE_DIR).toString());
		}
		settings.put(CfgKeys.SPINNER_STEP_SIZE, ((Double) stepSize.getValue())
				.doubleValue());
		settings.put(CfgKeys.SPINNER_MAX_VALUE, ((Double) maxSpinnerValue
				.getValue()).doubleValue());
		return settings;
	}

	/**
	 * 
	 */
	private void init() {

		/*
		 * Default directories
		 */
		tfOpenDir = new JTextField();
		tfSaveDir = new JTextField();
		tfOpenDir.addKeyListener(this);
		tfSaveDir.addKeyListener(this);
		tfOpenDir.setText(this.settings.get(CfgKeys.OPEN_DIR).toString());
		tfSaveDir.setText(this.settings.get(CfgKeys.SAVE_DIR).toString());
		JButton openButton = GUITools.createButton(GUITools.ICON_OPEN, this,
				Command.OPEN, String.format(toolTipButtons, "open"));
		JButton saveButton = GUITools.createButton(GUITools.ICON_SAVE, this,
				Command.SAVE, String.format(toolTipButtons, "save"));
		JPanel dirPanel = new JPanel();
		LayoutHelper lh = new LayoutHelper(dirPanel);
		lh.add(new JLabel("Open directory:"), new JPanel(), tfOpenDir,
				new JPanel(), openButton);
		lh.add(new JLabel("Save directory:"), new JPanel(), tfSaveDir,
				new JPanel(), saveButton);
		dirPanel.setBorder(BorderFactory
				.createTitledBorder(" Default directories "));

		/*
		 * Default values for JSpinners:
		 */
		double theStepSize = Double.parseDouble(this.settings.get(
				CfgKeys.SPINNER_STEP_SIZE).toString());
		stepSize = new JSpinner(new SpinnerNumberModel(theStepSize, 1E-20,
				1E20, theStepSize));
		maxSpinnerValue = new JSpinner(new SpinnerNumberModel(Double
				.parseDouble(this.settings.get(CfgKeys.SPINNER_MAX_VALUE)
						.toString()), 1E-20, 1E20, theStepSize));
		JPanel spinnerPanel = new JPanel();
		lh = new LayoutHelper(spinnerPanel);
		lh.add(new JLabel("Maximal value:"), new JPanel(), maxSpinnerValue);
		lh.add(new JLabel("Step size:"), new JPanel(), stepSize);
		spinnerPanel.setBorder(BorderFactory
				.createTitledBorder(" Default values for spinners "));

		setLayout(new GridLayout(2, 1));
		add(dirPanel);
		add(spinnerPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.gui.SettingsPanel#setProperties(java.util.Properties)
	 */
	@Override
	public void setProperties(Properties properties) {
		this.settings = new Properties();
		String k;
		for (Object key : properties.keySet()) {
			k = key.toString();
			if (k.startsWith("SPINNER_")
					|| (k.equals("OPEN_DIR") || (k.equals("SAVE_DIR")))) {
				settings.put(key, properties.get(key));
			}
		}
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#getTitle()
	 */
	@Override
	public String getTitle() {
		return "Program settings";
	}

}
