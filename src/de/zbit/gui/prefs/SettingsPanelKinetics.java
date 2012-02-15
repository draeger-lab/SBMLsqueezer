/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
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
package de.zbit.gui.prefs;

import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;

import org.sbml.squeezer.SqueezerOptions;

import de.zbit.gui.LayoutHelper;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.SBPreferences;

/**
 * This class is a panel, which contains all necessary options to be specified
 * for an appropriate creation of kinetic laws. It is also a data structure,
 * providing get and set methods to alter the current status of the user
 * settings.
 * 
 * @since 1.0
 * @version $Rev$
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @author Sebastian Nagel
 * @date Nov 15, 2007
 */
public class SettingsPanelKinetics extends PreferencesPanel {

	/**
	 * Generated serial version ID.
	 */
	private static final long serialVersionUID = 5242359204965070649L;

	/**
	 * 
	 */
	private JCheckBox jCheckBoxAddAllParametersGlobally,
			jCheckBoxPossibleEnzymeAsRNA, jCheckBoxPossibleEnzymeComplex,
			jCheckBoxPossibleEnzymeGenericProtein,
			jCheckBoxPossibleEnzymeReceptor, jCheckBoxPossibleEnzymeRNA,
			jCheckBoxPossibleEnzymeSimpleMolecule,
			jCheckBoxPossibleEnzymeTruncatedProtein,
			jCheckBoxPossibleEnzymeUnknown,
			jCheckBoxTreatAllReactionsAsEnzyeReaction,
			jCheckBoxSetBoundaryCondition, jCheckBoxRemoveUnnecessaryPandU,
			jCheckBoxWarnings;
	/**
	 * 
	 */
	private JRadioButton jRadioButtonTypeUnitConsistency,
			jRadioButtonGenerateForAllReactions;
	/**
	 * 
	 */
	private JSpinner jSpinnerMaxRealisticNumOfReactants,
			jSpinnerDefaultParamValue, jSpinnerDefaultSpeciesValue,
			jSpinnerDefaultCompartmentSize;
	
	/**
	 * 
	 */
	private static final transient ResourceBundle messagesBundle = ResourceManager.getBundle("org.sbml.squeezer.gui.locales.Messages");

	/**
	 * @throws IOException
	 * @throws InvalidPropertiesFormatException
	 * 
	 */
	public SettingsPanelKinetics() throws InvalidPropertiesFormatException,
			IOException {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.SettingsPanel#accepts(java.lang.Object)
	 */
	public boolean accepts(Object key) {
		String k = key.toString();
		return !k.equals("OPT_TREAT_ALL_REACTIONS_REVERSIBLE")
				&& (k.startsWith("OPT_") || k.startsWith("POSSIBLE_ENZYME_") || k
						.equals("TYPE_UNIT_CONSISTENCY"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#getTitle()
	 */
	public String getTitle() {
		return "Kinetics settings";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#init()
	 */
	public void init() {
		ButtonGroup buttonGroup;

		// Top Panel
		GridBagLayout layout = new GridBagLayout();
		JPanel jPanelGeneralOptions = new JPanel(layout);
		jPanelGeneralOptions.setBorder(BorderFactory
				.createTitledBorder(" "+messagesBundle.getString("GENERAL_OPTIONS")+" "));
		jCheckBoxSetBoundaryCondition = new JCheckBox(StringUtil.toHTML(
				SqueezerOptions.OPT_SET_BOUNDARY_CONDITION_FOR_GENES.getDisplayName(), 25),
				properties
						.getBooleanProperty(SqueezerOptions.OPT_SET_BOUNDARY_CONDITION_FOR_GENES));
		jCheckBoxSetBoundaryCondition
				.setToolTipText(StringUtil.toHTML(
								SqueezerOptions.OPT_SET_BOUNDARY_CONDITION_FOR_GENES.getDescription(),
								StringUtil.TOOLTIP_LINE_LENGTH));
		jCheckBoxRemoveUnnecessaryPandU = new JCheckBox(
				StringUtil.toHTML(SqueezerOptions.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS.getDisplayName(), 25),
				properties.getBooleanProperty(SqueezerOptions.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS));
		jCheckBoxRemoveUnnecessaryPandU
				.setToolTipText(StringUtil.toHTML(
								SqueezerOptions.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS.getDescription(),
								StringUtil.TOOLTIP_LINE_LENGTH));
		jCheckBoxTreatAllReactionsAsEnzyeReaction = new JCheckBox(StringUtil.toHTML(
				SqueezerOptions.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED.getDisplayName(), 25),
				properties
						.getBooleanProperty(SqueezerOptions.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED));
		jCheckBoxTreatAllReactionsAsEnzyeReaction
				.setToolTipText(StringUtil.toHTML(
								SqueezerOptions.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED.getDescription(),
								StringUtil.TOOLTIP_LINE_LENGTH));
		jCheckBoxAddAllParametersGlobally = new JCheckBox(StringUtil.toHTML(
				SqueezerOptions.OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY.getDisplayName(), 25));
		jCheckBoxAddAllParametersGlobally
				.setToolTipText(StringUtil.toHTML(
								SqueezerOptions.OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY.getDescription(),
								StringUtil.TOOLTIP_LINE_LENGTH));
		jCheckBoxAddAllParametersGlobally.setSelected(properties
				.getBooleanProperty(SqueezerOptions.OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY));
		jCheckBoxWarnings = new JCheckBox(SqueezerOptions.OPT_WARNINGS_FOR_TOO_MANY_REACTANTS.getDisplayName());
		jCheckBoxWarnings.setSelected(properties
				.getBooleanProperty(SqueezerOptions.OPT_WARNINGS_FOR_TOO_MANY_REACTANTS));
		jCheckBoxWarnings.setToolTipText(StringUtil.toHTML(
				SqueezerOptions.OPT_WARNINGS_FOR_TOO_MANY_REACTANTS.getDescription(), StringUtil.TOOLTIP_LINE_LENGTH));
		jSpinnerMaxRealisticNumOfReactants = new JSpinner(
				new SpinnerNumberModel(properties
						.getIntProperty(SqueezerOptions.OPT_MAX_NUMBER_OF_REACTANTS),
						2, 10, 1));
		jSpinnerMaxRealisticNumOfReactants
				.setToolTipText(StringUtil.toHTML(
								SqueezerOptions.OPT_MAX_NUMBER_OF_REACTANTS.getDescription(),
								StringUtil.TOOLTIP_LINE_LENGTH));
		jSpinnerDefaultCompartmentSize = new JSpinner(new SpinnerNumberModel(
				properties.getDoubleProperty(
						SqueezerOptions.OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE), 0, 9999.9, .1));
		jSpinnerDefaultCompartmentSize
				.setToolTipText(StringUtil.toHTML(
								SqueezerOptions.OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE.getDescription(),
								StringUtil.TOOLTIP_LINE_LENGTH));
		jSpinnerDefaultSpeciesValue = new JSpinner(new SpinnerNumberModel(
				properties.getDoubleProperty(
						SqueezerOptions.OPT_DEFAULT_SPECIES_INITIAL_VALUE),
				0, 9999.9, .1));
		jSpinnerDefaultSpeciesValue
				.setToolTipText(StringUtil.toHTML(
								SqueezerOptions.OPT_DEFAULT_SPECIES_INITIAL_VALUE.getDescription(),
								StringUtil.TOOLTIP_LINE_LENGTH));
		jSpinnerDefaultParamValue = new JSpinner(new SpinnerNumberModel(properties.getDoubleProperty(
								SqueezerOptions.OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS), 0, 9999.9, .1));
		jSpinnerDefaultParamValue
				.setToolTipText(StringUtil.toHTML(
								SqueezerOptions.OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS.getDescription(),
								StringUtil.TOOLTIP_LINE_LENGTH));
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jCheckBoxSetBoundaryCondition, 0, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jCheckBoxRemoveUnnecessaryPandU, 1, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jCheckBoxTreatAllReactionsAsEnzyeReaction, 0, 1, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jCheckBoxAddAllParametersGlobally, 1, 1, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jCheckBoxWarnings, 0, 2, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jSpinnerMaxRealisticNumOfReactants, 1, 2, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout, new JPanel(),
				0, 3, 2, 1, 1, 0);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout, new JLabel(
				StringUtil.toHTML(messagesBundle.getString("COMPARTMENTS_DEFAULT_INITIAL_SIZE"), 30)),
				0, 4, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jSpinnerDefaultCompartmentSize, 1, 4, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout, new JPanel(),
				0, 5, 2, 1, 1, 0);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout, new JLabel(
				StringUtil.toHTML(
						messagesBundle.getString("SPECIES_DEFAULT_INITIAL_AMOUNT_OR_CONCENTRATION"),
						30)), 0, 6, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jSpinnerDefaultSpeciesValue, 1, 6, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout, new JPanel(),
				0, 7, 2, 1, 1, 0);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout, new JLabel(
				StringUtil.toHTML(messagesBundle.getString("NEW_PARAMETER_DEFAULT_VALUE"), 30)), 0,
				8, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jSpinnerDefaultParamValue, 1, 8, 1, 1, 1, 1);

		// Second Panel
		JRadioButton jRadioButtonGenerateOnlyMissingKinetics = new JRadioButton(
				messagesBundle.getString("ONLY_WHEN_MISSING"));
		jRadioButtonGenerateOnlyMissingKinetics
				.setToolTipText(StringUtil.toHTML(
								messagesBundle.getString("ONLY_WHEN_MISSING_TOOLTIP"),
								StringUtil.TOOLTIP_LINE_LENGTH));
		jRadioButtonGenerateForAllReactions = new JRadioButton(
				SqueezerOptions.OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION.getDisplayName());
		jRadioButtonGenerateForAllReactions
				.setToolTipText(StringUtil.toHTML(
								SqueezerOptions.OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION.getDescription(),
								StringUtil.TOOLTIP_LINE_LENGTH));
		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonGenerateForAllReactions);
		buttonGroup.add(jRadioButtonGenerateOnlyMissingKinetics);
		jRadioButtonGenerateForAllReactions.setSelected(properties
				.getBooleanProperty(SqueezerOptions.OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION));
		jRadioButtonGenerateOnlyMissingKinetics
				.setSelected(!jRadioButtonGenerateForAllReactions.isSelected());
		layout = new GridBagLayout();
		JPanel jPanelGenerateNewKinetics = new JPanel(layout);
		jPanelGenerateNewKinetics.setBorder(BorderFactory
				.createTitledBorder(" "+messagesBundle.getString("GENERATE_NEW_KINETICS")+" "));
		LayoutHelper.addComponent(jPanelGenerateNewKinetics, layout,
				jRadioButtonGenerateOnlyMissingKinetics, 0, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGenerateNewKinetics, layout,
				jRadioButtonGenerateForAllReactions, 0, 1, 1, 1, 1, 1);

		// Third Panel

		// Fourth Panel
		jCheckBoxPossibleEnzymeGenericProtein = new JCheckBox(SqueezerOptions.POSSIBLE_ENZYME_GENERIC.getDisplayName());
		jCheckBoxPossibleEnzymeGenericProtein.setSelected(properties
				.getBoolean(SqueezerOptions.POSSIBLE_ENZYME_GENERIC));
		jCheckBoxPossibleEnzymeGenericProtein.setToolTipText(StringUtil.toHTML(
				SqueezerOptions.POSSIBLE_ENZYME_GENERIC.getDescription(), 
				StringUtil.TOOLTIP_LINE_LENGTH));
		jCheckBoxPossibleEnzymeRNA = new JCheckBox(SqueezerOptions.POSSIBLE_ENZYME_RNA.getDisplayName());
		jCheckBoxPossibleEnzymeRNA.setSelected(properties
				.getBooleanProperty(SqueezerOptions.POSSIBLE_ENZYME_RNA));
		jCheckBoxPossibleEnzymeRNA.setToolTipText(StringUtil.toHTML(
				SqueezerOptions.POSSIBLE_ENZYME_RNA.getDescription(),
				StringUtil.TOOLTIP_LINE_LENGTH));
		jCheckBoxPossibleEnzymeComplex = new JCheckBox(SqueezerOptions.POSSIBLE_ENZYME_COMPLEX.getDisplayName());
		jCheckBoxPossibleEnzymeComplex.setSelected(properties
				.getBooleanProperty(SqueezerOptions.POSSIBLE_ENZYME_COMPLEX));
		jCheckBoxPossibleEnzymeComplex.setToolTipText(StringUtil.toHTML(
				SqueezerOptions.POSSIBLE_ENZYME_COMPLEX.getDescription(), StringUtil.TOOLTIP_LINE_LENGTH));
		jCheckBoxPossibleEnzymeTruncatedProtein = new JCheckBox(SqueezerOptions.POSSIBLE_ENZYME_TRUNCATED.getDisplayName());
		jCheckBoxPossibleEnzymeTruncatedProtein
				.setSelected(properties
						.getBooleanProperty(SqueezerOptions.POSSIBLE_ENZYME_TRUNCATED));
		jCheckBoxPossibleEnzymeTruncatedProtein.setToolTipText(StringUtil.toHTML(
				SqueezerOptions.POSSIBLE_ENZYME_TRUNCATED.getDescription(),
				StringUtil.TOOLTIP_LINE_LENGTH));
		jCheckBoxPossibleEnzymeReceptor = new JCheckBox(SqueezerOptions.POSSIBLE_ENZYME_RECEPTOR.getDisplayName());
		jCheckBoxPossibleEnzymeReceptor.setSelected(properties
				.getBooleanProperty(SqueezerOptions.POSSIBLE_ENZYME_RECEPTOR));
		jCheckBoxPossibleEnzymeReceptor.setToolTipText(StringUtil.toHTML(
				SqueezerOptions.POSSIBLE_ENZYME_RECEPTOR.getDescription(),
				StringUtil.TOOLTIP_LINE_LENGTH));
		jCheckBoxPossibleEnzymeUnknown = new JCheckBox(SqueezerOptions.POSSIBLE_ENZYME_UNKNOWN.getDisplayName());
		jCheckBoxPossibleEnzymeUnknown.setSelected(properties
				.getBooleanProperty(SqueezerOptions.POSSIBLE_ENZYME_UNKNOWN));
		jCheckBoxPossibleEnzymeUnknown.setToolTipText(StringUtil.toHTML(
				SqueezerOptions.POSSIBLE_ENZYME_UNKNOWN.getDescription(),
				StringUtil.TOOLTIP_LINE_LENGTH));
		jCheckBoxPossibleEnzymeAsRNA = new JCheckBox(SqueezerOptions.POSSIBLE_ENZYME_ANTISENSE_RNA.getDisplayName());
		jCheckBoxPossibleEnzymeAsRNA.setSelected(properties
				.getBooleanProperty(SqueezerOptions.POSSIBLE_ENZYME_ANTISENSE_RNA));
		jCheckBoxPossibleEnzymeAsRNA.setToolTipText(StringUtil.toHTML(
				SqueezerOptions.POSSIBLE_ENZYME_ANTISENSE_RNA.getDescription(),
				StringUtil.TOOLTIP_LINE_LENGTH));
		jCheckBoxPossibleEnzymeSimpleMolecule = new JCheckBox(SqueezerOptions.POSSIBLE_ENZYME_SIMPLE_MOLECULE.getDisplayName());
		jCheckBoxPossibleEnzymeSimpleMolecule.setSelected(properties
				.getBooleanProperty(SqueezerOptions.POSSIBLE_ENZYME_SIMPLE_MOLECULE));
		jCheckBoxPossibleEnzymeSimpleMolecule.setToolTipText(StringUtil.toHTML(
				SqueezerOptions.POSSIBLE_ENZYME_SIMPLE_MOLECULE.getDescription(),
				StringUtil.TOOLTIP_LINE_LENGTH));

		layout = new GridBagLayout();
		JPanel jPanelSettingsEnzymes = new JPanel();
		jPanelSettingsEnzymes.setLayout(layout);
		jPanelSettingsEnzymes.setBorder(BorderFactory
				.createTitledBorder(" "+messagesBundle.getString("SPECIES_TO_BE_TREATED_AS_ENZYMS")+" "));
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeGenericProtein, 0, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeRNA, 1, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeComplex, 2, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeTruncatedProtein, 3, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeReceptor, 0, 1, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeAsRNA, 1, 1, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeUnknown, 2, 1, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelSettingsEnzymes, layout,
				jCheckBoxPossibleEnzymeSimpleMolecule, 3, 1, 1, 1, 1, 1);

		JPanel jPanelTypeUnitConsistency = new JPanel();
		LayoutHelper unitConsistency = new LayoutHelper(
				jPanelTypeUnitConsistency);
		jRadioButtonTypeUnitConsistency = new JRadioButton(StringUtil.toHTML(
				SqueezerOptions.TYPE_UNIT_CONSISTENCY.getDisplayName(), 30),
				SqueezerOptions.TYPE_UNIT_CONSISTENCY.getValue(properties).ordinal() == 0);
		jRadioButtonTypeUnitConsistency.setToolTipText(StringUtil.toHTML(
				SqueezerOptions.TYPE_UNIT_CONSISTENCY.getDescription(),
				StringUtil.TOOLTIP_LINE_LENGTH));
		JRadioButton jRadioButtonTypeUnitsCompVol = new JRadioButton(StringUtil.toHTML(
				messagesBundle.getString("TYPE_UNIT_COMPARTMENT"), 30),
				!jRadioButtonTypeUnitConsistency.isSelected());
		jRadioButtonTypeUnitsCompVol.setToolTipText(StringUtil.toHTML(
				messagesBundle.getString("TYPE_UNIT_COMPARTMENT_TOOLTIP"),
				StringUtil.TOOLTIP_LINE_LENGTH));
		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonTypeUnitConsistency);
		buttonGroup.add(jRadioButtonTypeUnitsCompVol);
		unitConsistency.add(jRadioButtonTypeUnitConsistency);
		unitConsistency.add(jRadioButtonTypeUnitsCompVol);
		jPanelTypeUnitConsistency.setBorder(BorderFactory
				.createTitledBorder(" How to ensure unit consistency "));

		// Add all panels to this settings panel:
		LayoutHelper lh = new LayoutHelper(this);
		lh.add(jPanelGeneralOptions, 0, 0, 2, 1, 1, 1);
		lh.add(jPanelGenerateNewKinetics, 0, 1, 1, 1, 1, 1);
		lh.add(jPanelTypeUnitConsistency, 1, 1, 1, 1, 1, 1);
		lh.add(jPanelSettingsEnzymes, 0, 2, 2, 1, 1, 1);

		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		// Add change listener
		jCheckBoxTreatAllReactionsAsEnzyeReaction.addItemListener(this);
		jCheckBoxAddAllParametersGlobally.addItemListener(this);
		jCheckBoxWarnings.addItemListener(this);
		jSpinnerMaxRealisticNumOfReactants.addChangeListener(this);
		jSpinnerDefaultCompartmentSize.addChangeListener(this);
		jSpinnerDefaultSpeciesValue.addChangeListener(this);
		jSpinnerDefaultParamValue.addChangeListener(this);
		jRadioButtonGenerateForAllReactions.addItemListener(this);
		jCheckBoxPossibleEnzymeGenericProtein.addItemListener(this);
		jCheckBoxPossibleEnzymeRNA.addItemListener(this);
		jCheckBoxPossibleEnzymeComplex.addItemListener(this);
		jCheckBoxPossibleEnzymeTruncatedProtein.addItemListener(this);
		jCheckBoxPossibleEnzymeReceptor.addItemListener(this);
		jCheckBoxPossibleEnzymeUnknown.addItemListener(this);
		jCheckBoxPossibleEnzymeAsRNA.addItemListener(this);
		jCheckBoxPossibleEnzymeSimpleMolecule.addItemListener(this);
		jRadioButtonTypeUnitConsistency.addItemListener(this);
		jCheckBoxSetBoundaryCondition.addItemListener(this);
		jCheckBoxRemoveUnnecessaryPandU.addItemListener(this);

		jSpinnerMaxRealisticNumOfReactants.setEnabled(jCheckBoxWarnings
				.isSelected());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.gui.SettingsPanel#itemStateChanged(java.awt.event.ItemEvent
	 * )
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource().equals(jCheckBoxTreatAllReactionsAsEnzyeReaction)) {
			properties.put(SqueezerOptions.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED,
					Boolean.valueOf(jCheckBoxTreatAllReactionsAsEnzyeReaction
							.isSelected()));
		} else if (e.getSource().equals(jCheckBoxAddAllParametersGlobally)) {
			properties.put(SqueezerOptions.OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY,
					Boolean.valueOf(jCheckBoxAddAllParametersGlobally
							.isSelected()));
		} else if (e.getSource().equals(jCheckBoxWarnings)) {
			properties.put(SqueezerOptions.OPT_WARNINGS_FOR_TOO_MANY_REACTANTS, Boolean
					.valueOf(jCheckBoxWarnings.isSelected()));
			jSpinnerMaxRealisticNumOfReactants.setEnabled(jCheckBoxWarnings
					.isSelected());
		} else if (e.getSource().equals(jRadioButtonGenerateForAllReactions)) {
			properties.put(SqueezerOptions.OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION,
					Boolean.valueOf(jRadioButtonGenerateForAllReactions
							.isSelected()));
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeRNA)) {
			properties.put(SqueezerOptions.POSSIBLE_ENZYME_RNA, Boolean
					.valueOf(jCheckBoxPossibleEnzymeRNA.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeAsRNA)) {
			properties.put(SqueezerOptions.POSSIBLE_ENZYME_ANTISENSE_RNA, Boolean
					.valueOf(jCheckBoxPossibleEnzymeAsRNA.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeGenericProtein)) {
			properties.put(SqueezerOptions.POSSIBLE_ENZYME_GENERIC,
					Boolean.valueOf(jCheckBoxPossibleEnzymeGenericProtein
							.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource()
				.equals(jCheckBoxPossibleEnzymeTruncatedProtein)) {
			properties.put(SqueezerOptions.POSSIBLE_ENZYME_TRUNCATED, Boolean
					.valueOf(jCheckBoxPossibleEnzymeTruncatedProtein
							.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeSimpleMolecule)) {
			properties.put(SqueezerOptions.POSSIBLE_ENZYME_SIMPLE_MOLECULE,
					Boolean.valueOf(jCheckBoxPossibleEnzymeSimpleMolecule
							.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeComplex)) {
			properties.put(SqueezerOptions.POSSIBLE_ENZYME_COMPLEX, Boolean
					.valueOf(jCheckBoxPossibleEnzymeComplex.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeReceptor)) {
			properties.put(SqueezerOptions.POSSIBLE_ENZYME_RECEPTOR, Boolean
					.valueOf(jCheckBoxPossibleEnzymeReceptor.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeUnknown)) {
			properties.put(SqueezerOptions.POSSIBLE_ENZYME_UNKNOWN, Boolean
					.valueOf(jCheckBoxPossibleEnzymeUnknown.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jRadioButtonTypeUnitConsistency)) {
      properties.put(SqueezerOptions.TYPE_UNIT_CONSISTENCY,
            jRadioButtonTypeUnitConsistency.isSelected() ? SqueezerOptions.TypeUnitConsistency.amount
                : SqueezerOptions.TypeUnitConsistency.concentration);
		} else if (e.getSource().equals(jCheckBoxSetBoundaryCondition)) {
			properties
					.put(SqueezerOptions.OPT_SET_BOUNDARY_CONDITION_FOR_GENES,
							Boolean.valueOf(jCheckBoxSetBoundaryCondition
									.isSelected()));
		} else if (e.getSource().equals(jCheckBoxRemoveUnnecessaryPandU)) {
			properties.put(SqueezerOptions.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS,
					Boolean.valueOf(jCheckBoxRemoveUnnecessaryPandU
							.isSelected()));
		}
		super.itemStateChanged(e);
	}

	/**
	 * This method checks, if there is any possible Enzyme checked or not
	 * 
	 * @return: void
	 */
	public boolean possibleEnzymeTestAllNotChecked() {
		boolean possibleEnzymeAllNotChecked;
		if (!jCheckBoxPossibleEnzymeRNA.isSelected()
				&& !jCheckBoxPossibleEnzymeAsRNA.isSelected()
				&& !jCheckBoxPossibleEnzymeGenericProtein.isSelected()
				&& !jCheckBoxPossibleEnzymeTruncatedProtein.isSelected()
				&& !jCheckBoxPossibleEnzymeSimpleMolecule.isSelected()
				&& !jCheckBoxPossibleEnzymeComplex.isSelected()
				&& !jCheckBoxPossibleEnzymeReceptor.isSelected()
				&& !jCheckBoxPossibleEnzymeUnknown.isSelected()
				&& !jCheckBoxTreatAllReactionsAsEnzyeReaction.isSelected()) {
			possibleEnzymeAllNotChecked = true;
		} else {
			possibleEnzymeAllNotChecked = false;
		}
		return possibleEnzymeAllNotChecked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#restoreDefaults()
	 */
	@Override
	public void restoreDefaults() {
		super.restoreDefaults();
		possibleEnzymeTestAllNotChecked();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(jSpinnerMaxRealisticNumOfReactants)) {
			properties.put(SqueezerOptions.OPT_MAX_NUMBER_OF_REACTANTS, Integer
					.parseInt(jSpinnerMaxRealisticNumOfReactants.getValue()
							.toString()));
		} else if (e.getSource().equals(jSpinnerDefaultCompartmentSize)) {
			properties.put(SqueezerOptions.OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE, Double
					.valueOf(jSpinnerDefaultCompartmentSize.getValue()
							.toString()));
		} else if (e.getSource().equals(jSpinnerDefaultSpeciesValue)) {
			properties
					.put(SqueezerOptions.OPT_DEFAULT_SPECIES_INITIAL_VALUE, Double
							.valueOf(jSpinnerDefaultSpeciesValue.getValue()
									.toString()));
		} else if (e.getSource().equals(jSpinnerDefaultParamValue)) {
			properties.put(SqueezerOptions.OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS, Double
					.valueOf(jSpinnerDefaultParamValue.getValue().toString()));
		}
		super.stateChanged(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.SettingsPanel#loadPreferences()
	 */
	protected SBPreferences loadPreferences()
			throws InvalidPropertiesFormatException, IOException {
		return SBPreferences.getPreferencesFor(SqueezerOptions.class);
	}
}
