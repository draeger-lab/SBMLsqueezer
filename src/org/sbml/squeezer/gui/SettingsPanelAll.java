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
import java.awt.event.ItemListener;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

import org.sbml.squeezer.SBMLsqueezer;

/**
 * A {@link JPanel} containing a {@link JTabbedPane} with several options for
 * the configuration of {@link SBMLsqueezer} in a GUI.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.3
 * @date 2009-09-22
 */
public class SettingsPanelAll extends SettingsPanel {

	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 3189416350182046246L;

	/**
	 * 
	 */
	private SettingsPanelKinetics panelKinSettings;

	/**
	 * 
	 */
	private SettingsPanelLaTeX panelLatexSettings;
	/**
	 * 
	 */
	private SettingsPanelGeneral panelGeneralSettings;

	/**
	 * 
	 */
	private SettingsPanelDefaultMechanisms panelDefaultMechanisms;

	// TODO: Not in this version
	// private SettingsPanelStability panelStabilitySettings;
	// TODO: Not in this version
	// private SettingsPanelSimulation panelSimulationSettings;

	/**
	 * 
	 */
	private JTabbedPane tab;

	/**
	 * 
	 */
	private Properties defaultSettings;

	/**
	 * 
	 * @param properties
	 * @param defaultProperties
	 */
	public SettingsPanelAll(Properties properties, Properties defaultProperties) {
		super(properties);
		this.defaultSettings = defaultProperties;
		setLayout(new GridLayout(1, 1));
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.gui.SettingsPanel#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties settings) {
		int tab = getSelectedIndex();
		removeAll();
		this.settings = settings;
		init();
		setSelectedIndex(tab);
	}

	private void init() {
		tab = new JTabbedPane();
		/*
		 * Kinetics settings
		 */
		panelKinSettings = new SettingsPanelKinetics(this.settings);
		tab.addTab("Kinetics settings", panelKinSettings);

		/*
		 * Reaction mechanism settings
		 */
		panelDefaultMechanisms = new SettingsPanelDefaultMechanisms(
				this.settings);
		tab.addTab("Reaction mechanisms", new JScrollPane(
				panelDefaultMechanisms,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		/*
		 * Program settings
		 */
		panelGeneralSettings = new SettingsPanelGeneral(settings);
		tab.addTab("Program settings", new JScrollPane(panelGeneralSettings,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		/*
		 * LaTeX Settings
		 */
		this.panelLatexSettings = new SettingsPanelLaTeX(settings, false);
		tab.addTab("LaTeX output settings", new JScrollPane(panelLatexSettings,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		// this.add(tab);
		// addItemListener(this);

		/*
		 * Stability Settings
		 */
		// TODO: Not in this version
		// panelStabilitySettings = new SettingsPanelStability(this.settings);
		// tab.addTab("Stability settings", new JScrollPane(
		// panelStabilitySettings,
		// JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		// JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

		/*
		 * Simulation Settings
		 */
		// TODO: Not in this version
		// try {
		// panelSimulationSettings = new SettingsPanelSimulation(settings);
		// tab.addTab("Simulation settings", new JScrollPane(
		// panelSimulationSettings,
		// JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		// JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		// } catch (Exception exc) {
		// exc.printStackTrace();
		// JOptionPane.showMessageDialog(this, exc.getMessage(), exc
		// .getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		// }

		this.add(tab);
		addItemListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.gui.SettingsPanel#addChangeListener(javax.swing.event
	 * .ChangeListener)
	 */
	public void addChangeListener(ChangeListener listener) {
		if (panelKinSettings != null) {
			panelKinSettings.addChangeListener(listener);
		}
		if (panelLatexSettings != null) {
			panelLatexSettings.addChangeListener(listener);
		}
		// TODO: Not in this version
		// if (panelSimulationSettings != null) {
		// panelSimulationSettings.addChangeListener(listener);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.squeezer.gui.SettingsPanel#addItemListener(java.awt.event.
	 * ItemListener)
	 */
	public void addItemListener(ItemListener listener) {
		if (panelKinSettings != null) {
			panelKinSettings.addItemListener(listener);
		}
		if (panelLatexSettings != null) {
			panelDefaultMechanisms.addItemListener(listener);
		}
		// TODO: Not in this version
		// if (panelSimulationSettings != null) {
		// panelSimulationSettings.addItemListener(listener);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#getProperties()
	 */
	public Properties getProperties() {
		this.settings.putAll(panelGeneralSettings.getProperties());
		this.settings.putAll(panelKinSettings.getProperties());
		this.settings.putAll(panelDefaultMechanisms.getProperties());
		this.settings.putAll(panelLatexSettings.getProperties());
		// TODO: Not in this version
		// Properties props = panelSimulationSettings.getProperties();
		// for (Object key : props.keySet()) {
		// this.settings.put(key, props.get(key));
		// }
		return settings;
	}

	/**
	 * 
	 * @return
	 */
	public int getSelectedIndex() {
		return tab.getSelectedIndex();
	}

	/**
	 * 
	 * @param tab
	 */
	public void setSelectedIndex(int tab) {
		this.tab.setSelectedIndex(tab);
	}

	/**
	 * 
	 */
	public void restoreDefaults() {
		this.settings = (Properties) defaultSettings.clone();
		removeAll();
		init();
	}
}
