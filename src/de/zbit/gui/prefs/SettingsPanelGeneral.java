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
package de.zbit.gui.prefs;

import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

import de.zbit.gui.LayoutHelper;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-09-10
 */
public class SettingsPanelGeneral extends PreferencesPanel {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -5344644880108822465L;

	/**
	 * 
	 */
	private JSpinner minSpinnerValue, maxSpinnerValue, stepSize;

	/**
	 * 
	 */
	private DirectoryChooser chooser;

	/**
	 * 
	 * @param properties
	 */
	public SettingsPanelGeneral(SBProperties properties) {
		super(properties);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.SettingsPanel#accepts(java.lang.Object)
	 */
	@Override
	public boolean accepts(Object key) {
		String k = key.toString();
		return k.startsWith("SPINNER_")
				|| (k.equals("OPEN_DIR") || (k.equals("SAVE_DIR")));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#getProperties()
	 */
	@Override
	public SBProperties getProperties() {
		if (chooser.checkOpenDir()) {
			properties.put(CfgKeys.SqueezerOptions, chooser.getOpenDir());
		}
		if (chooser.checkSaveDir()) {
			properties.put(CfgKeys.SqueezerOptions, chooser.getSaveDir());
		}
		properties.put(CfgKeys.SqueezerOptions,
				((Double) stepSize.getValue()).doubleValue());
		properties.put(CfgKeys.SqueezerOptions, ((Double) maxSpinnerValue
				.getValue()).doubleValue());
		return properties;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#init()
	 */
	@Override
	public void init() {
		chooser = new DirectoryChooser(properties.get(CfgKeys.SqueezerOptions)
				.toString(), properties.get(CfgKeys.SqueezerOptions).toString(), true);
		chooser.setBorder(BorderFactory
				.createTitledBorder(" Default directories "));
		/*
		 * Default values for JSpinners:
		 */
		double theStepSize = Double.parseDouble(this.properties.get(
				CfgKeys.SqueezerOptions).toString());
		stepSize = new JSpinner(new SpinnerNumberModel(theStepSize, 1E-20,
				1E20, theStepSize));
		stepSize.addChangeListener(this);
		minSpinnerValue = new JSpinner(new SpinnerNumberModel(Double
				.parseDouble(this.properties.get(CfgKeys.SqueezerOptions)
						.toString()), -1E20, 1E20, theStepSize));
		minSpinnerValue.addChangeListener(this);
		maxSpinnerValue = new JSpinner(new SpinnerNumberModel(Double
				.parseDouble(this.properties.get(CfgKeys.SqueezerOptions)
						.toString()), 1E-20, 1E20, theStepSize));
		maxSpinnerValue.addChangeListener(this);
		JPanel spinnerPanel = new JPanel();
		
		LayoutHelper lh = new LayoutHelper(spinnerPanel);
		lh.add(new JLabel("Minimal value:"), 0, 0, 1, 1, 0, 0);
		lh.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		lh.add(minSpinnerValue, 2, 0, 1, 1, 1, 0);
		lh.add(new JPanel(), 0, 1, 3, 1, 1, 0);
		lh.add(new JLabel("Maximal value:"), 0, 2, 1, 1, 0, 0);
		lh.add(maxSpinnerValue, 2, 2, 1, 1, 1, 0);
		lh.add(new JPanel(), 0, 3, 3, 1, 1, 0);
		lh.add(new JLabel("Step size:"), 0, 4, 1, 1, 0, 0);
		lh.add(stepSize, 2, 4, 1, 1, 1, 0);
		spinnerPanel.setBorder(BorderFactory
				.createTitledBorder(" Default values for spinners "));

		lh = new LayoutHelper(this);
		lh.add(chooser, 0, 0, 1, 1, 0, 0);
		lh.add(spinnerPanel, 0, 1, 1, 1, 0, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.zbit.gui.cfg.SettingsPanel#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(minSpinnerValue)) {
			properties.put(CfgKeys.SqueezerOptions, (Double) minSpinnerValue
					.getValue());
		} else if (e.getSource().equals(maxSpinnerValue)) {
			properties.put(CfgKeys.SqueezerOptions, (Double) maxSpinnerValue
					.getValue());
		} else if (e.getSource().equals(stepSize)) {
			properties.put(CfgKeys.SqueezerOptions, (Double) stepSize
					.getValue());
		}
		super.stateChanged(e);
	}

	@Override
	public List<String> checkPreferences() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected SBPreferences loadPreferences() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
