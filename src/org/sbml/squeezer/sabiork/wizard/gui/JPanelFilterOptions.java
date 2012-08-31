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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that provides the possibility to set the different filter options.
 * 
 * @author Matthias Rall
 * @version $Rev$
 */
public class JPanelFilterOptions extends JPanel implements ActionListener {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 5448185584732193553L;
	private final PropertyChangeSupport propertyChangeSupport;
	private boolean isWildtype;
	private boolean isMutant;
	private boolean isRecombinant;
	private boolean hasKineticData;
	private boolean isDirectSubmission;
	private boolean isJournal;
	private boolean isEntriesInsertedSince;
	private double lowerpHValue;
	private double upperpHValue;
	private double lowerTemperature;
	private double upperTemperature;
	private Date dateSubmitted;
	private JButton buttonCalendar;
	private JCheckBox checkBoxWildtype;
	private JCheckBox checkBoxMutant;
	private JCheckBox checkBoxRecombinant;
	private JCheckBox checkBoxKineticData;
	private JCheckBox checkBoxDirectSubmission;
	private JCheckBox checkBoxJournal;
	private JCheckBox checkBoxEntriesInsertedSince;
	private JComboBoxInterval comboBoxLowerpHValue;
	private JComboBoxInterval comboBoxUpperpHValue;
	private JComboBoxInterval comboBoxLowerTemperature;
	private JComboBoxInterval comboBoxUpperTemperature;
	private JDialogCalendar dialogCalendar;
	private JLabel labelEnzyme;
	private JLabel labelKineticData;
	private JLabel labelEnvironmentalConditions;
	private JLabel labelpH;
	private JLabel labelpHHyphen;
	private JLabel labelTemperature;
	private JLabel labelTemperatureHyphen;
	private JLabel labelSource;
	private JPanel panelCalendar;
	private JTextField textFieldCalendar;
	private SimpleDateFormat dateFormat;

	public JPanelFilterOptions() {
		this.propertyChangeSupport = new PropertyChangeSupport(this);
		this.isWildtype = true;
		this.isMutant = true;
		this.isRecombinant = false;
		this.hasKineticData = true;
		this.isDirectSubmission = true;
		this.isJournal = true;
		this.isEntriesInsertedSince = false;
		this.lowerpHValue = 0.0;
		this.upperpHValue = 14.0;
		this.lowerTemperature = -10.0;
		this.upperTemperature = 115.0;
		this.dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		try {
			this.dateSubmitted = dateFormat.parse("15/10/2008");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		initialize();
	}

	private void initialize() {
		labelEnzyme = new JLabel(
				WizardProperties.getText("JPANEL_FILTER_OPTIONS_TEXT_ENZYME"));

		checkBoxWildtype = new JCheckBox(
				WizardProperties.getText("JPANEL_FILTER_OPTIONS_TEXT_WILDTYPE"));
		checkBoxWildtype.setSelected(isWildtype);
		checkBoxWildtype.addActionListener(this);

		checkBoxMutant = new JCheckBox(
				WizardProperties.getText("JPANEL_FILTER_OPTIONS_TEXT_MUTANT"));
		checkBoxMutant.setSelected(isMutant);
		checkBoxMutant.addActionListener(this);

		checkBoxRecombinant = new JCheckBox(
				WizardProperties
						.getText("JPANEL_FILTER_OPTIONS_TEXT_RECOMBINANT"));
		checkBoxRecombinant.setSelected(isRecombinant);
		checkBoxRecombinant.addActionListener(this);

		labelKineticData = new JLabel(
				WizardProperties
						.getText("JPANEL_FILTER_OPTIONS_TEXT_KINETIC_DATA"));

		checkBoxKineticData = new JCheckBox(
				WizardProperties
						.getText("JPANEL_FILTER_OPTIONS_TEXT_RATE_EQUATION"));
		checkBoxKineticData.setSelected(hasKineticData);
		checkBoxKineticData.addActionListener(this);

		labelEnvironmentalConditions = new JLabel(
				WizardProperties
						.getText("JPANEL_FILTER_OPTIONS_TEXT_ENVIRONMENTAL_CONDITIONS"));

		labelpH = new JLabel(
				WizardProperties.getText("JPANEL_FILTER_OPTIONS_TEXT_PH"));

		comboBoxLowerpHValue = new JComboBoxInterval(lowerpHValue, upperpHValue);
		comboBoxLowerpHValue.setSelectedItem(lowerpHValue);
		comboBoxLowerpHValue.addActionListener(this);

		labelpHHyphen = new JLabel(
				WizardProperties.getText("JPANEL_FILTER_OPTIONS_TEXT_HYPHEN"));

		comboBoxUpperpHValue = new JComboBoxInterval(lowerpHValue, upperpHValue);
		comboBoxUpperpHValue.setSelectedItem(upperpHValue);
		comboBoxUpperpHValue.addActionListener(this);

		labelTemperature = new JLabel(
				WizardProperties
						.getText("JPANEL_FILTER_OPTIONS_TEXT_TEMPERATURE_IN_CELCIUS"));

		comboBoxLowerTemperature = new JComboBoxInterval(lowerTemperature,
				upperTemperature);
		comboBoxLowerTemperature.setSelectedItem(lowerTemperature);
		comboBoxLowerTemperature.addActionListener(this);

		labelTemperatureHyphen = new JLabel(
				WizardProperties.getText("JPANEL_FILTER_OPTIONS_TEXT_HYPHEN"));

		comboBoxUpperTemperature = new JComboBoxInterval(lowerTemperature,
				upperTemperature);
		comboBoxUpperTemperature.setSelectedItem(upperTemperature);
		comboBoxUpperTemperature.addActionListener(this);

		labelSource = new JLabel(
				WizardProperties.getText("JPANEL_FILTER_OPTIONS_TEXT_SOURCE"));

		checkBoxDirectSubmission = new JCheckBox(
				WizardProperties
						.getText("JPANEL_FILTER_OPTIONS_TEXT_DIRECT_SUBMISSION"));
		checkBoxDirectSubmission.setSelected(isDirectSubmission);
		checkBoxDirectSubmission.addActionListener(this);

		checkBoxJournal = new JCheckBox(
				WizardProperties.getText("JPANEL_FILTER_OPTIONS_TEXT_JOURNAL"));
		checkBoxJournal.setSelected(isJournal);
		checkBoxJournal.addActionListener(this);

		checkBoxEntriesInsertedSince = new JCheckBox(
				WizardProperties
						.getText("JPANEL_FILTER_OPTIONS_TEXT_ENTRIES_INSERTED_SINCE"));
		checkBoxEntriesInsertedSince.setSelected(isEntriesInsertedSince);
		checkBoxEntriesInsertedSince.addActionListener(this);

		textFieldCalendar = new JTextField();
		textFieldCalendar.setText(dateFormat.format(dateSubmitted));
		textFieldCalendar.setEnabled(false);

		buttonCalendar = new JButton(new ImageIcon(this.getClass().getResource(
				WizardProperties
						.getText("JPANEL_FILTER_OPTIONS_IMAGE_CALENDAR"))));
		buttonCalendar.addActionListener(this);

		panelCalendar = new JPanel();
		panelCalendar.setLayout(new BoxLayout(panelCalendar, BoxLayout.X_AXIS));
		panelCalendar.add(textFieldCalendar);
		panelCalendar.add(buttonCalendar);

		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), WizardProperties
				.getText("JPANEL_FILTER_OPTIONS_TEXT_FILTER_OPTIONS")));
		add(labelEnzyme, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 5, 5, 0), 0, 0));
		add(checkBoxWildtype, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(checkBoxMutant, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(checkBoxRecombinant, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(labelKineticData, new GridBagConstraints(0, 3, 4, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(10, 5, 5, 0), 0, 0));
		add(checkBoxKineticData, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(labelEnvironmentalConditions, new GridBagConstraints(0, 5, 4, 1,
				0.0, 0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(10, 5, 5, 0), 0, 0));
		add(labelpH, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 20, 0, 0), 0, 0));
		add(comboBoxLowerpHValue, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		add(labelpHHyphen, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(comboBoxUpperpHValue, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		add(labelTemperature, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 20, 0, 0), 0, 0));
		add(comboBoxLowerTemperature, new GridBagConstraints(1, 7, 1, 1, 0.0,
				0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(labelTemperatureHyphen, new GridBagConstraints(2, 7, 1, 1, 0.0,
				0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(comboBoxUpperTemperature, new GridBagConstraints(3, 7, 1, 1, 0.0,
				0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(labelSource, new GridBagConstraints(0, 8, 4, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(10, 5, 5, 0), 0, 0));
		add(checkBoxDirectSubmission, new GridBagConstraints(0, 9, 1, 1, 0.0,
				0.0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(checkBoxJournal, new GridBagConstraints(1, 9, 2, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		add(checkBoxEntriesInsertedSince, new GridBagConstraints(0, 10, 1, 1,
				0.0, 0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		add(panelCalendar, new GridBagConstraints(1, 10, 3, 1, 0.0, 0.0,
				GridBagConstraints.LINE_START, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * Returns a SABIO-RK query for the currently selected filter options.
	 * 
	 * @return
	 */
	public String getFilterOptionsQuery() {
		return SABIORK.getFilterOptionsQuery(isWildtype, isMutant,
				isRecombinant, hasKineticData, lowerpHValue, upperpHValue,
				lowerTemperature, upperTemperature, isDirectSubmission,
				isJournal, isEntriesInsertedSince, dateSubmitted);
	}

	/**
	 * Adjusts the values of the both {@link JComboBoxInterval}.
	 * 
	 * @param comboBoxLowerInterval
	 * @param comboBoxUpperInterval
	 */
	private void setAppropriateIntervals(
			JComboBoxInterval comboBoxLowerInterval,
			JComboBoxInterval comboBoxUpperInterval) {
		double selectedLowerValue = comboBoxLowerInterval.getSelectedValue();
		double selectedUpperValue = comboBoxUpperInterval.getSelectedValue();
		if (selectedUpperValue < selectedLowerValue) {
			comboBoxUpperInterval.setInterval(selectedLowerValue,
					comboBoxUpperInterval.getMaximum());
			comboBoxUpperInterval.setSelectedItem(selectedLowerValue);
		} else {
			comboBoxUpperInterval.setInterval(selectedLowerValue,
					comboBoxUpperInterval.getMaximum());
			comboBoxUpperInterval.setSelectedItem(selectedUpperValue);
		}
	}

	public boolean isWildtype() {
		return isWildtype;
	}

	public boolean isMutant() {
		return isMutant;
	}

	public boolean isRecombinant() {
		return isRecombinant;
	}

	public boolean hasKineticData() {
		return hasKineticData;
	}

	public boolean isDirectSubmission() {
		return isDirectSubmission;
	}

	public boolean isJournal() {
		return isJournal;
	}

	public boolean isEntriesInsertedSince() {
		return isEntriesInsertedSince;
	}

	public double getLowerpHValue() {
		return lowerpHValue;
	}

	public double getUpperpHValue() {
		return upperpHValue;
	}

	public double getLowerTemperature() {
		return lowerTemperature;
	}

	public double getUpperTemperature() {
		return upperTemperature;
	}

	public Date getDateSubmitted() {
		return dateSubmitted;
	}

	public void setWildtype(boolean isWildtype) {
		boolean oldValue = this.isWildtype;
		boolean newValue = isWildtype;
		this.isWildtype = newValue;
		propertyChangeSupport.firePropertyChange("isWildtype", oldValue,
				newValue);
	}

	public void setMutant(boolean isMutant) {
		boolean oldValue = this.isMutant;
		boolean newValue = isMutant;
		this.isMutant = newValue;
		propertyChangeSupport
				.firePropertyChange("isMutant", oldValue, newValue);
	}

	public void setRecombinant(boolean isRecombinant) {
		boolean oldValue = this.isRecombinant;
		boolean newValue = isRecombinant;
		this.isRecombinant = newValue;
		propertyChangeSupport.firePropertyChange("isRecombinant", oldValue,
				newValue);
	}

	public void setHasKineticData(boolean hasKineticData) {
		boolean oldValue = this.hasKineticData;
		boolean newValue = hasKineticData;
		this.hasKineticData = newValue;
		propertyChangeSupport.firePropertyChange("hasKineticData", oldValue,
				newValue);
	}

	public void setDirectSubmission(boolean isDirectSubmission) {
		boolean oldValue = this.isDirectSubmission;
		boolean newValue = isDirectSubmission;
		this.isDirectSubmission = newValue;
		propertyChangeSupport.firePropertyChange("isDirectSubmission",
				oldValue, newValue);
	}

	public void setJournal(boolean isJournal) {
		boolean oldValue = this.isJournal;
		boolean newValue = isJournal;
		this.isJournal = newValue;
		propertyChangeSupport.firePropertyChange("isJournal", oldValue,
				newValue);
	}

	public void setEntriesInsertedSince(boolean isEntriesInsertedSince) {
		boolean oldValue = this.isEntriesInsertedSince;
		boolean newValue = isEntriesInsertedSince;
		this.isEntriesInsertedSince = newValue;
		propertyChangeSupport.firePropertyChange("isEntriesInsertedSince",
				oldValue, newValue);
	}

	public void setLowerpHValue(double lowerpHValue) {
		double oldValue = this.lowerpHValue;
		double newValue = lowerpHValue;
		this.lowerpHValue = newValue;
		propertyChangeSupport.firePropertyChange("lowerpHValue", oldValue,
				newValue);
	}

	public void setUpperpHValue(double upperpHValue) {
		double oldValue = this.upperpHValue;
		double newValue = upperpHValue;
		this.upperpHValue = newValue;
		propertyChangeSupport.firePropertyChange("upperpHValue", oldValue,
				newValue);
	}

	public void setLowerTemperature(double lowerTemperature) {
		double oldValue = this.lowerTemperature;
		double newValue = lowerTemperature;
		this.lowerTemperature = newValue;
		propertyChangeSupport.firePropertyChange("lowerTemperature", oldValue,
				newValue);
	}

	public void setUpperTemperature(double upperTemperature) {
		double oldValue = this.upperTemperature;
		double newValue = upperTemperature;
		this.upperTemperature = newValue;
		propertyChangeSupport.firePropertyChange("upperTemperature", oldValue,
				newValue);
	}

	public void setDateSubmitted(Date dateSubmitted) {
		Date oldValue = this.dateSubmitted;
		Date newValue = dateSubmitted;
		this.dateSubmitted = newValue;
		propertyChangeSupport.firePropertyChange("dateSubmitted", oldValue,
				newValue);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(checkBoxWildtype)) {
			setWildtype(checkBoxWildtype.isSelected());
		}
		if (e.getSource().equals(checkBoxMutant)) {
			setMutant(checkBoxMutant.isSelected());
		}
		if (e.getSource().equals(checkBoxRecombinant)) {
			setRecombinant(checkBoxRecombinant.isSelected());
		}
		if (e.getSource().equals(checkBoxKineticData)) {
			setHasKineticData(checkBoxKineticData.isSelected());
		}
		if (e.getSource().equals(comboBoxLowerpHValue)) {
			setAppropriateIntervals(comboBoxLowerpHValue, comboBoxUpperpHValue);
			setLowerpHValue(comboBoxLowerpHValue.getSelectedValue());
			setUpperpHValue(comboBoxUpperpHValue.getSelectedValue());
		}
		if (e.getSource().equals(comboBoxUpperpHValue)) {
			setUpperpHValue(comboBoxUpperpHValue.getSelectedValue());
		}
		if (e.getSource().equals(comboBoxLowerTemperature)) {
			setAppropriateIntervals(comboBoxLowerTemperature,
					comboBoxUpperTemperature);
			setLowerTemperature(comboBoxLowerTemperature.getSelectedValue());
			setUpperTemperature(comboBoxUpperTemperature.getSelectedValue());
		}
		if (e.getSource().equals(comboBoxUpperTemperature)) {
			setUpperTemperature(comboBoxUpperTemperature.getSelectedValue());
		}
		if (e.getSource().equals(checkBoxDirectSubmission)) {
			setDirectSubmission(checkBoxDirectSubmission.isSelected());
		}
		if (e.getSource().equals(checkBoxJournal)) {
			setJournal(checkBoxJournal.isSelected());
		}
		if (e.getSource().equals(checkBoxEntriesInsertedSince)) {
			setEntriesInsertedSince(checkBoxEntriesInsertedSince.isSelected());
		}
		if (e.getSource().equals(buttonCalendar)) {
			dialogCalendar = new JDialogCalendar(
					SwingUtilities.getWindowAncestor(this),
					ModalityType.APPLICATION_MODAL, dateSubmitted);
			textFieldCalendar.setText(dateFormat.format(dialogCalendar
					.getSelectedDate()));
			setDateSubmitted(dialogCalendar.getSelectedDate());
		}
	}

}
