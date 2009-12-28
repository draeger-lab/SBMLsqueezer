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

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.SBMLsqueezer;

/**
 * This class is a panel, which contains all necessary options to be specified
 * for an appropriate creation of kinetic laws. It is also a data structure,
 * providing get and set methods to alter the current status of the user
 * settings.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Nov 15, 2007
 */
public class SettingsPanelKinetics extends JPanel implements ChangeListener,
		ItemListener {

	/**
	 * Generated serial version ID.
	 */
	private static final long serialVersionUID = 5242359204965070649L;

	private List<ChangeListener> changeListeners;

	private List<ItemListener> itemListeners;

	private JCheckBox jCheckBoxAddAllParametersGlobally;

	private JCheckBox jCheckBoxPossibleEnzymeAsRNA;

	private JCheckBox jCheckBoxPossibleEnzymeComplex;

	private JCheckBox jCheckBoxPossibleEnzymeGenericProtein;

	private JCheckBox jCheckBoxPossibleEnzymeReceptor;

	private JCheckBox jCheckBoxPossibleEnzymeRNA;

	private JCheckBox jCheckBoxPossibleEnzymeSimpleMolecule;

	private JCheckBox jCheckBoxPossibleEnzymeTruncatedProtein;

	private JCheckBox jCheckBoxPossibleEnzymeUnknown;

	private JCheckBox jCheckBoxTreatAllReactionsAsEnzyeReaction;

	private JRadioButton jRadioButtonTypeUnitConsistency;

	private JCheckBox jCheckBoxSetBoundaryCondition;

	private JCheckBox jCheckBoxRemoveUnnecessaryPandU;

	private JCheckBox jCheckBoxWarnings;

	private JComboBox jComboBoxTypeStandardVersion;

	private JRadioButton jRadioButtonForceReacRev;

	private JRadioButton jRadioButtonGenerateForAllReactions;

	private JSpinner jSpinnerMaxRealisticNumOfReactants;

	private Properties settings;

	/**
	 * 
	 */
	private JSpinner jSpinnerDefaultParamValue;
	/**
	 * 
	 */
	private JSpinner jSpinnerDefaultSpeciesValue;
	/**
	 * 
	 */
	private JSpinner jSpinnerDefaultCompartmentSize;

	/**
	 * 
	 * @param settings
	 */
	public SettingsPanelKinetics(Properties settings) {
		super(new GridBagLayout());
		this.settings = new Properties();
		for (Object key : settings.keySet()) {
			String k = key.toString();
			if (k.startsWith("OPT_") || k.startsWith("POSSIBLE_ENZYME_")
					|| k.startsWith("TYPE_"))
				this.settings.put(key, settings.get(key));
		}
		this.itemListeners = new LinkedList<ItemListener>();
		this.changeListeners = new LinkedList<ChangeListener>();
		init();
	}

	/**
	 * 
	 * @param l
	 */
	public void addChangeListener(ChangeListener l) {
		changeListeners.add(l);
	}

	/**
	 * 
	 * @param l
	 */
	public void addItemListener(ItemListener l) {
		itemListeners.add(l);
	}

	/**
	 * 
	 * @return
	 */
	public Properties getSettings() {
		return settings;
	}

	/**
	 * 
	 */
	private void init() {
		ButtonGroup buttonGroup;
		Font titleFont = new Font("Dialog", Font.BOLD, 12);
		Color borderColor = new Color(51, 51, 51);

		// Top Panel
		GridBagLayout layout = new GridBagLayout();
		JPanel jPanelGeneralOptions = new JPanel(layout);
		jPanelGeneralOptions.setBorder(BorderFactory.createTitledBorder(null,
				" General options ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		jCheckBoxSetBoundaryCondition = new JCheckBox(GUITools.toHTML(
				"Set boundary condition for gene coding species", 25),
				((Boolean) settings
						.get(CfgKeys.OPT_SET_BOUNDARY_CONDITION_FOR_GENES))
						.booleanValue());
		jCheckBoxSetBoundaryCondition
				.setToolTipText(GUITools
						.toHTML(
								"If selected, the boundary condition of all species that represent gene coding elements, such as genes or gene coding regions will be set to true.",
								40));
		jCheckBoxRemoveUnnecessaryPandU = new JCheckBox(
				GUITools.toHTML("Remove uneccessary parameters and units", 25),
				((Boolean) settings
						.get(CfgKeys.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS))
						.booleanValue());
		jCheckBoxRemoveUnnecessaryPandU
				.setToolTipText(GUITools
						.toHTML(
								"If selected parameters and unit definitions that are never referenced in the model are automatically deleted when creating new kinetic laws.",
								40));
		jCheckBoxTreatAllReactionsAsEnzyeReaction = new JCheckBox(GUITools
				.toHTML("Consider all reactions to be enzyme-catalyzed", 25),
				((Boolean) settings
						.get(CfgKeys.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED))
						.booleanValue());
		jCheckBoxTreatAllReactionsAsEnzyeReaction
				.setToolTipText(GUITools
						.toHTML(
								"If checked, all reactions are considered to be enzyme-catalyzed.",
								40));
		jCheckBoxAddAllParametersGlobally = new JCheckBox(GUITools.toHTML(
				"Add all new parameters globally", 25));
		jCheckBoxAddAllParametersGlobally
				.setToolTipText(GUITools
						.toHTML(
								"If selected, all newly created parameters are stored "
										+ "globally in the model. Otherwise SBMLsqueezer only stores most "
										+ "parameters locally in the respective rate law.",
								40));
		jCheckBoxAddAllParametersGlobally.setSelected(((Boolean) settings
				.get(CfgKeys.OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY))
				.booleanValue());
		jCheckBoxWarnings = new JCheckBox("Warnings for too many reactants:");
		jCheckBoxWarnings.setSelected(((Boolean) settings
				.get(CfgKeys.OPT_WARNINGS_FOR_TOO_MANY_REACTANTS))
				.booleanValue());
		jCheckBoxWarnings.setToolTipText(GUITools.toHTML(
				"If checked, warnings will be shown for reactions "
						+ "with more reactants than specified here.", 40));
		jSpinnerMaxRealisticNumOfReactants = new JSpinner(
				new SpinnerNumberModel(((Integer) settings
						.get(CfgKeys.OPT_MAX_NUMBER_OF_REACTANTS)).intValue(),
						2, 10, 1));
		jSpinnerMaxRealisticNumOfReactants
				.setToolTipText(GUITools
						.toHTML(
								"Specifiy how many reactants are at most likely to collide. This option is only available if warnings should be displayed at all.",
								40));
		jSpinnerDefaultCompartmentSize = new JSpinner(new SpinnerNumberModel(
				Double.parseDouble(settings.get(
						CfgKeys.OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE)
						.toString()), 0, 9999.9, .1));
		jSpinnerDefaultCompartmentSize
				.setToolTipText(GUITools
						.toHTML(
								"For compartments that are not yet initialized, SBMLsqueezer will use this value as the default initial size.",
								40));
		jSpinnerDefaultSpeciesValue = new JSpinner(new SpinnerNumberModel(
				Double.parseDouble(settings.get(
						CfgKeys.OPT_DEFAULT_SPECIES_INITIAL_VALUE).toString()),
				0, 9999.9, .1));
		jSpinnerDefaultSpeciesValue
				.setToolTipText(GUITools
						.toHTML(
								"If species are not yet initialized, SBMLsqueezer will use this value as initial amount or initial concentration of the species depending on its hasOnlySubstanceUnits value, i.e., for species that are interpreted in terms of concentration, an initial concentration will be set, whereas an initial amount will be set if the species is to be interpreted in terms of molecule counts.",
								40));
		jSpinnerDefaultParamValue = new JSpinner(new SpinnerNumberModel(
				Double
						.parseDouble(settings.get(
								CfgKeys.OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS)
								.toString()), 0, 9999.9, .1));
		jSpinnerDefaultParamValue
				.setToolTipText(GUITools
						.toHTML(
								"Specifiy the default value for newly created parameters.",
								40));
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
				GUITools.toHTML("Default initial size for compartments:", 30)),
				0, 4, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jSpinnerDefaultCompartmentSize, 1, 4, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout, new JPanel(),
				0, 5, 2, 1, 1, 0);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout, new JLabel(
				GUITools.toHTML(
						"Default initial amount or concentration for species:",
						30)), 0, 6, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jSpinnerDefaultSpeciesValue, 1, 6, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout, new JPanel(),
				0, 7, 2, 1, 1, 0);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout, new JLabel(
				GUITools.toHTML("Default value for new parameters:", 30)), 0,
				8, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGeneralOptions, layout,
				jSpinnerDefaultParamValue, 1, 8, 1, 1, 1, 1);

		// Second Panel
		JRadioButton jRadioButtonGenerateOnlyMissingKinetics = new JRadioButton(
				"Only when missing");
		jRadioButtonGenerateOnlyMissingKinetics
				.setToolTipText(GUITools
						.toHTML(
								"If checked, kinetics are only generated if missing in the SBML file.",
								40));
		jRadioButtonGenerateForAllReactions = new JRadioButton(
				"For all reactions");
		jRadioButtonGenerateForAllReactions
				.setToolTipText(GUITools
						.toHTML(
								"If checked, already existing kinetic laws will be overwritten.",
								40));
		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonGenerateForAllReactions);
		buttonGroup.add(jRadioButtonGenerateOnlyMissingKinetics);
		jRadioButtonGenerateForAllReactions.setSelected(((Boolean) settings
				.get(CfgKeys.OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION))
				.booleanValue());
		jRadioButtonGenerateOnlyMissingKinetics
				.setSelected(!jRadioButtonGenerateForAllReactions.isSelected());
		layout = new GridBagLayout();
		JPanel jPanelGenerateNewKinetics = new JPanel(layout);
		jPanelGenerateNewKinetics.setBorder(BorderFactory.createTitledBorder(
				null, " Generate new kinetics ",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		LayoutHelper.addComponent(jPanelGenerateNewKinetics, layout,
				jRadioButtonGenerateOnlyMissingKinetics, 0, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelGenerateNewKinetics, layout,
				jRadioButtonGenerateForAllReactions, 0, 1, 1, 1, 1, 1);

		// Third Panel
		jRadioButtonForceReacRev = new JRadioButton(
				"Model all reactions in a reversible manner");
		jRadioButtonForceReacRev.setSelected(((Boolean) settings
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue());
		jRadioButtonForceReacRev
				.setToolTipText(GUITools
						.toHTML(
								"If checked, all reactions will be set to reversible no matter what is given by the SBML file.",
								40));
		JRadioButton jRadioButtonSettingsFrameForceRevAsCD = new JRadioButton(
				"Use information from SBML");
		jRadioButtonSettingsFrameForceRevAsCD.setSelected(!((Boolean) settings
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue());
		jRadioButtonSettingsFrameForceRevAsCD
				.setToolTipText(GUITools
						.toHTML(
								"If checked, the information about reversiblity will be left unchanged.",
								40));
		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonSettingsFrameForceRevAsCD);
		buttonGroup.add(jRadioButtonForceReacRev);
		layout = new GridBagLayout();
		JPanel jPanelSettingsReversibility = new JPanel();
		jPanelSettingsReversibility.setLayout(layout);
		jPanelSettingsReversibility.setBorder(BorderFactory.createTitledBorder(
				null, " Reversibility ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		LayoutHelper.addComponent(jPanelSettingsReversibility, layout,
				jRadioButtonSettingsFrameForceRevAsCD, 0, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelSettingsReversibility, layout,
				jRadioButtonForceReacRev, 0, 1, 1, 1, 1, 1);

		// Fourth Panel
		jCheckBoxPossibleEnzymeGenericProtein = new JCheckBox("Generic protein");
		jCheckBoxPossibleEnzymeGenericProtein.setSelected(((Boolean) settings
				.get(CfgKeys.POSSIBLE_ENZYME_GENERIC)).booleanValue());
		jCheckBoxPossibleEnzymeGenericProtein.setToolTipText(GUITools.toHTML(
				"If checked, generic proteins are treated as enzymes. "
						+ "Otherwise, generic protein-catalyzed reactions are "
						+ "not considered to be enzyme reactions.", 40));
		jCheckBoxPossibleEnzymeRNA = new JCheckBox("RNA");
		jCheckBoxPossibleEnzymeRNA.setSelected(((Boolean) settings
				.get(CfgKeys.POSSIBLE_ENZYME_RNA)).booleanValue());
		jCheckBoxPossibleEnzymeRNA.setToolTipText(GUITools.toHTML(
				"If checked, RNA is treated as an enzyme. "
						+ "Otherwise RNA catalyzed reactions are not "
						+ "considered to be enzyme-catalyzed reactions.", 40));
		jCheckBoxPossibleEnzymeRNA.setBackground(Color.WHITE);
		jCheckBoxPossibleEnzymeComplex = new JCheckBox("Complex");
		jCheckBoxPossibleEnzymeComplex.setSelected(((Boolean) settings
				.get(CfgKeys.POSSIBLE_ENZYME_COMPLEX)).booleanValue());
		jCheckBoxPossibleEnzymeComplex.setToolTipText(GUITools.toHTML(
				"If checked, complex molecules are treated as enzymes. "
						+ "Otherwise, complex catalized reactions are not "
						+ "considered to be enzyme reactions.", 40));
		jCheckBoxPossibleEnzymeTruncatedProtein = new JCheckBox(
				"Truncated protein");
		jCheckBoxPossibleEnzymeTruncatedProtein.setSelected(((Boolean) settings
				.get(CfgKeys.POSSIBLE_ENZYME_TRUNCATED)).booleanValue());
		jCheckBoxPossibleEnzymeTruncatedProtein.setToolTipText(GUITools.toHTML(
				"If checked, truncated proteins are treated as enzymes. "
						+ "Otherwise, truncated protein catalized reactions "
						+ "are not considered to be enzyme reactions.", 40));
		jCheckBoxPossibleEnzymeReceptor = new JCheckBox("Receptor");
		jCheckBoxPossibleEnzymeReceptor.setSelected(((Boolean) settings
				.get(CfgKeys.POSSIBLE_ENZYME_RECEPTOR)).booleanValue());
		jCheckBoxPossibleEnzymeReceptor.setToolTipText(GUITools.toHTML(
				"If checked, receptors are treated as enzymes. "
						+ "Otherwise, receptor catalized reactions are not "
						+ "considered to be enzyme reactions.", 40));
		jCheckBoxPossibleEnzymeUnknown = new JCheckBox("Unknown");
		jCheckBoxPossibleEnzymeUnknown.setSelected(((Boolean) settings
				.get(CfgKeys.POSSIBLE_ENZYME_UNKNOWN)).booleanValue());
		jCheckBoxPossibleEnzymeUnknown
				.setToolTipText(GUITools
						.toHTML(
								"If checked, unknown molecules are treated as enzymes. "
										+ "Otherwise, unknown molecule catalized reactions are not "
										+ "considered to be enzyme reactions.",
								40));
		jCheckBoxPossibleEnzymeAsRNA = new JCheckBox("asRNA");
		jCheckBoxPossibleEnzymeAsRNA.setSelected(((Boolean) settings
				.get(CfgKeys.POSSIBLE_ENZYME_ANTISENSE_RNA)).booleanValue());
		jCheckBoxPossibleEnzymeAsRNA.setToolTipText(GUITools.toHTML(
				"If checked, asRNA is treated as an enzyme. "
						+ "Otherwise asRNA catalized reactions are not "
						+ "considered to be enzyme-catalyzed reactions.", 40));
		jCheckBoxPossibleEnzymeSimpleMolecule = new JCheckBox("Simple molecule");
		jCheckBoxPossibleEnzymeSimpleMolecule.setSelected(((Boolean) settings
				.get(CfgKeys.POSSIBLE_ENZYME_SIMPLE_MOLECULE)).booleanValue());
		jCheckBoxPossibleEnzymeSimpleMolecule
				.setToolTipText(GUITools
						.toHTML(
								"If checked, simple molecules are treated as enzymes. "
										+ "Otherwise, simple molecule catalized reactions are not "
										+ "considered to be enzyme reactions.",
								40));

		layout = new GridBagLayout();
		JPanel jPanelSettingsEnzymes = new JPanel();
		jPanelSettingsEnzymes.setLayout(layout);
		jPanelSettingsEnzymes.setBorder(BorderFactory.createTitledBorder(null,
				" Species to be treated as enzymes ",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
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
		jRadioButtonTypeUnitConsistency = new JRadioButton(GUITools.toHTML(
				"Bring species to substance units", 30),
				((Integer) this.settings.get(CfgKeys.TYPE_UNIT_CONSISTENCY))
						.intValue() == 0);
		jRadioButtonTypeUnitConsistency
				.setToolTipText(GUITools
						.toHTML(
								"If this option is selected, species occuring in kinetic equations are multiplyed with the size of the surrounding compartment if their hasOnlySubstanceUnits attribute is set to false. The units of parameters are set accordingly.",
								40));
		JRadioButton jRadioButtonTypeUnitsCompVol = new JRadioButton(GUITools
				.toHTML("Bring species to concentration units", 30),
				!jRadioButtonTypeUnitConsistency.isSelected());
		jRadioButtonTypeUnitsCompVol
				.setToolTipText(GUITools
						.toHTML(
								"If this option is selected, species are interpreted in terms of concentration and are therefore divided by their surrounding compartment size when these occur in kinetic equations and their hasOnlySubstanceUnits attribute is false. The units of parameters are set accordingly.",
								40));
		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonTypeUnitConsistency);
		buttonGroup.add(jRadioButtonTypeUnitsCompVol);
		unitConsistency.add(jRadioButtonTypeUnitConsistency);
		unitConsistency.add(jRadioButtonTypeUnitsCompVol);
		jPanelTypeUnitConsistency.setBorder(BorderFactory.createTitledBorder(
				null, " How to ensure unit consistency ",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		JPanel jPanelStandardVersions = new JPanel();
		jComboBoxTypeStandardVersion = new JComboBox(new String[] { "cat",
				"hal", "weg" });
		jComboBoxTypeStandardVersion.setSelectedIndex(((Integer) this.settings
				.get(CfgKeys.TYPE_STANDARD_VERSION)).intValue());
		jComboBoxTypeStandardVersion
				.setToolTipText(GUITools
						.toHTML(
								"Select the version of the standardised kinetic equations. These options are described in the publications of Liebermeister et al. This option can only be accessed if all reactions are modeled reversibly.",
								40));
		LayoutHelper helper = new LayoutHelper(jPanelStandardVersions);
		helper.add(new JPanel(), 0, 0, 5, 1, 1, 1);
		helper.add(new JPanel(), 0, 1, 1, 1, 1, 1);
		helper.add(new JLabel(GUITools.toHTML(
				"Choose the version of standardised kinetics:", 20)), 1, 1, 1,
				1, 0, 1);
		helper.add(new JPanel(), 2, 1, 1, 1, 1, 1);
		helper.add(jComboBoxTypeStandardVersion, 3, 1, 1, 1, 1, 0);
		helper.add(new JPanel(), 4, 1, 1, 1, 1, 1);
		helper.add(new JPanel(), 0, 2, 5, 1, 1, 1);
		jPanelStandardVersions.setBorder(BorderFactory.createTitledBorder(null,
				" Version of standard kinetics ",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		// Add all panels to this settings panel:
		layout = (GridBagLayout) this.getLayout();
		LayoutHelper.addComponent(this, layout, jPanelGeneralOptions, 0, 0, 2,
				1, 1, 1);
		LayoutHelper.addComponent(this, layout, jPanelGenerateNewKinetics, 0,
				1, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, layout, jPanelSettingsReversibility, 1,
				1, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, layout, jPanelTypeUnitConsistency, 0,
				2, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, layout, jPanelStandardVersions, 1, 2,
				1, 1, 1, 1);
		LayoutHelper.addComponent(this, layout, jPanelSettingsEnzymes, 0, 3, 2,
				1, 1, 1);

		GUITools.setAllBackground(this, Color.WHITE);
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
		jRadioButtonForceReacRev.addItemListener(this);
		jCheckBoxPossibleEnzymeGenericProtein.addItemListener(this);
		jCheckBoxPossibleEnzymeRNA.addItemListener(this);
		jCheckBoxPossibleEnzymeComplex.addItemListener(this);
		jCheckBoxPossibleEnzymeTruncatedProtein.addItemListener(this);
		jCheckBoxPossibleEnzymeReceptor.addItemListener(this);
		jCheckBoxPossibleEnzymeUnknown.addItemListener(this);
		jCheckBoxPossibleEnzymeAsRNA.addItemListener(this);
		jCheckBoxPossibleEnzymeSimpleMolecule.addItemListener(this);
		jRadioButtonTypeUnitConsistency.addItemListener(this);
		jComboBoxTypeStandardVersion.addItemListener(this);
		jCheckBoxSetBoundaryCondition.addItemListener(this);
		jCheckBoxRemoveUnnecessaryPandU.addItemListener(this);

		jSpinnerMaxRealisticNumOfReactants.setEnabled(jCheckBoxWarnings
				.isSelected());
		jComboBoxTypeStandardVersion.setEnabled(jRadioButtonForceReacRev
				.isSelected());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource().equals(jCheckBoxTreatAllReactionsAsEnzyeReaction)) {
			settings.put(CfgKeys.OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED,
					Boolean.valueOf(jCheckBoxTreatAllReactionsAsEnzyeReaction
							.isSelected()));
		} else if (e.getSource().equals(jCheckBoxAddAllParametersGlobally)) {
			settings.put(CfgKeys.OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY,
					Boolean.valueOf(jCheckBoxAddAllParametersGlobally
							.isSelected()));
		} else if (e.getSource().equals(jCheckBoxWarnings)) {
			settings.put(CfgKeys.OPT_WARNINGS_FOR_TOO_MANY_REACTANTS, Boolean
					.valueOf(jCheckBoxWarnings.isSelected()));
			jSpinnerMaxRealisticNumOfReactants.setEnabled(jCheckBoxWarnings
					.isSelected());
		} else if (e.getSource().equals(jRadioButtonGenerateForAllReactions)) {
			settings.put(CfgKeys.OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION,
					Boolean.valueOf(jRadioButtonGenerateForAllReactions
							.isSelected()));
		} else if (e.getSource().equals(jRadioButtonForceReacRev)) {
			settings.put(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE, Boolean
					.valueOf(jRadioButtonForceReacRev.isSelected()));
			jComboBoxTypeStandardVersion.setEnabled(jRadioButtonForceReacRev
					.isSelected());
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeRNA)) {
			settings.put(CfgKeys.POSSIBLE_ENZYME_RNA, Boolean
					.valueOf(jCheckBoxPossibleEnzymeRNA.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeAsRNA)) {
			settings.put(CfgKeys.POSSIBLE_ENZYME_ANTISENSE_RNA, Boolean
					.valueOf(jCheckBoxPossibleEnzymeAsRNA.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeGenericProtein)) {
			settings.put(CfgKeys.POSSIBLE_ENZYME_GENERIC,
					Boolean.valueOf(jCheckBoxPossibleEnzymeGenericProtein
							.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource()
				.equals(jCheckBoxPossibleEnzymeTruncatedProtein)) {
			settings.put(CfgKeys.POSSIBLE_ENZYME_TRUNCATED, Boolean
					.valueOf(jCheckBoxPossibleEnzymeTruncatedProtein
							.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeSimpleMolecule)) {
			settings.put(CfgKeys.POSSIBLE_ENZYME_SIMPLE_MOLECULE,
					Boolean.valueOf(jCheckBoxPossibleEnzymeSimpleMolecule
							.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeComplex)) {
			settings.put(CfgKeys.POSSIBLE_ENZYME_COMPLEX, Boolean
					.valueOf(jCheckBoxPossibleEnzymeComplex.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeReceptor)) {
			settings.put(CfgKeys.POSSIBLE_ENZYME_RECEPTOR, Boolean
					.valueOf(jCheckBoxPossibleEnzymeReceptor.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jCheckBoxPossibleEnzymeUnknown)) {
			settings.put(CfgKeys.POSSIBLE_ENZYME_UNKNOWN, Boolean
					.valueOf(jCheckBoxPossibleEnzymeUnknown.isSelected()));
			possibleEnzymeTestAllNotChecked();
		} else if (e.getSource().equals(jRadioButtonTypeUnitConsistency)) {
			settings.put(CfgKeys.TYPE_UNIT_CONSISTENCY, Integer
					.valueOf(jRadioButtonTypeUnitConsistency.isSelected() ? 0
							: 1));
		} else if (e.getSource().equals(jComboBoxTypeStandardVersion)) {
			settings.put(CfgKeys.TYPE_STANDARD_VERSION, Integer
					.valueOf(jComboBoxTypeStandardVersion.getSelectedIndex()));
		} else if (e.getSource().equals(jCheckBoxSetBoundaryCondition)) {
			settings.put(CfgKeys.OPT_SET_BOUNDARY_CONDITION_FOR_GENES, Boolean
					.valueOf(jCheckBoxSetBoundaryCondition.isSelected()));
		} else if (e.getSource().equals(jCheckBoxRemoveUnnecessaryPandU)) {
			settings.put(CfgKeys.OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS,
					Boolean.valueOf(jCheckBoxRemoveUnnecessaryPandU
							.isSelected()));
		}
		for (ItemListener i : itemListeners)
			i.itemStateChanged(e);
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
				&& !jCheckBoxTreatAllReactionsAsEnzyeReaction.isSelected())
			possibleEnzymeAllNotChecked = true;
		else
			possibleEnzymeAllNotChecked = false;
		return possibleEnzymeAllNotChecked;
	}

	/**
	 * 
	 */
	public void restoreDefaults() {
		String openDir = settings.get(CfgKeys.OPEN_DIR).toString();
		String saveDir = settings.get(CfgKeys.SAVE_DIR).toString();
		settings = SBMLsqueezer.getDefaultSettings();
		settings.put(CfgKeys.OPEN_DIR, openDir);
		settings.put(CfgKeys.SAVE_DIR, saveDir);
		init();
		validate();
		possibleEnzymeTestAllNotChecked();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(jSpinnerMaxRealisticNumOfReactants)) {
			settings.put(CfgKeys.OPT_MAX_NUMBER_OF_REACTANTS, Integer
					.parseInt(jSpinnerMaxRealisticNumOfReactants.getValue()
							.toString()));
		} else if (e.getSource().equals(jSpinnerDefaultCompartmentSize)) {
			settings.put(CfgKeys.OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE, Double
					.valueOf(jSpinnerDefaultCompartmentSize.getValue()
							.toString()));
		} else if (e.getSource().equals(jSpinnerDefaultSpeciesValue)) {
			settings
					.put(CfgKeys.OPT_DEFAULT_SPECIES_INITIAL_VALUE, Double
							.valueOf(jSpinnerDefaultSpeciesValue.getValue()
									.toString()));
		} else if (e.getSource().equals(jSpinnerDefaultParamValue)) {
			settings.put(CfgKeys.OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS, Double
					.valueOf(jSpinnerDefaultParamValue.getValue().toString()));
		}
		for (int i = 0; i < changeListeners.size(); i++)
			changeListeners.get(i).stateChanged(e);
	}
}
