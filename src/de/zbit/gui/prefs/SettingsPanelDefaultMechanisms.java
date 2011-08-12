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
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import org.sbml.jsbml.util.StringTools;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.gui.GUITools;
import org.sbml.squeezer.kinetics.BasicKineticLaw;

import de.zbit.gui.LayoutHelper;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * A {@link JPanel} to let the user select all default instances of
 * {@link BasicKineticLaw} that implement the designated interfaces to be
 * available for certain reaction types.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date 2009-09-22
 * @version $Rev$
 * @since 1.3
 */
public class SettingsPanelDefaultMechanisms extends PreferencesPanel {

	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 243553812503691739L;

	/**
	 * 
	 */
	private JComboBox jComboBoxTypeStandardVersion;

	/**
	 * 
	 */
	private JRadioButton jRadioButtonForceReacRev;

	/**
	 * 
	 */
	private JTabbedPane tabs;

	/**
	 * 
	 */
	private JPanel tabsPanel;

	/**
	 * Reaction Mechanism Panel
	 * 
	 * @param properties
	 * @throws IOException
	 */
	public SettingsPanelDefaultMechanisms() throws IOException {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.cfg.SettingsPanel#accepts(java.lang.Object)
	 */
	@Override
	public boolean accepts(Object key) {
		String k = key.toString();
		return k.startsWith("KINETICS_")
				|| k.equals("OPT_TREAT_ALL_REACTIONS_REVERSIBLE")
				|| k.equals("TYPE_STANDARD_VERSION");
	}

	/**
	 * Creates a panel that contains radio buttons for the given class of
	 * kinetic equations.
	 * 
	 * @param classes
	 * @param key
	 * @return
	 */
	private JPanel createButtonGroupPanel(Set<String> classes, Option<?> key) {
		ButtonGroup buttonGroup = new ButtonGroup();
		JPanel p = new JPanel();
		LayoutHelper helper = new LayoutHelper(p);
		// p.setBorder(BorderFactory.createTitledBorder(createTitleFor(key)));
		JRadioButton jRButton[] = new JRadioButton[classes.size()];
		String cl[] = classes.toArray(new String[] {});
		Arrays.sort(cl);
		int i;
		for (i = 0; i < jRButton.length; i++) {
			String className = cl[i];
			String type = className.substring(className.lastIndexOf('.') + 1);
			jRButton[i] = new JRadioButton(className.substring(className
					.lastIndexOf('.') + 1));
			jRButton[i].setSelected(properties.get(key).toString().equals(
					className));
			StringBuilder toolTip = new StringBuilder();
			String msg;
			if (key.equals(SqueezerOptions.KINETICS_GENE_REGULATION)) {
				msg = "Check this box if you want %s  as reaction scheme for gene regulation (reactions involving genes, RNA and proteins)";
				toolTip.append(String.format(msg, jRButton[i].getText()));
			} else if (key.equals(SqueezerOptions.KINETICS_UNI_UNI_TYPE)) {
				msg = "Check this box if %s is to be applied for uni-uni reactions (one reactant, one product).";
				toolTip.append(String.format(msg, jRButton[i].getText()));
			} else if (key.equals(SqueezerOptions.KINETICS_BI_UNI_TYPE)) {
				msg = "Check this box if you want the %s scheme as reaction scheme for bi-uni reactions (two reactant, one product).";
				toolTip.append(String.format(msg, jRButton[i].getText()));
			} else if (key.equals(SqueezerOptions.KINETICS_BI_BI_TYPE)) {
				msg = "Check this box if you want the %s to be applied to all bi-bi reactions (two reactants, two products).";
				toolTip.append(String.format(msg, jRButton[i].getText()));
			} else if (key.equals(SqueezerOptions.KINETICS_ARBITRARY_ENZYME_REACTIONS)) {
				msg = "Reactions, which are enzyme-catalyzed, and follow none of the schemes uni-uni, bi-uni and bi-bi can be modeled using %s.";
				toolTip.append(String.format(msg, jRButton[i].getText()));
			} else {
				msg = "Reactions, which are %s, can be described using %s.";
				toolTip.append(String.format(msg, key.toString().toLowerCase()
						.replace('_', ' '), type));
			}
			jRButton[i].setToolTipText(StringUtil.toHTML(toolTip.toString(), 40));
			jRButton[i].setEnabled(true);
			// jRButton[i].setBackground(Color.WHITE);
			jRButton[i].addItemListener(this);
			jRButton[i].setActionCommand(key.toString());
			buttonGroup.add(jRButton[i]);
			helper.add(jRButton[i]);
		}
		i = 0;
		int pos = 0;
		boolean oneIsSelected = false;
		for (JRadioButton radioButton : jRButton) {
			if (radioButton.isSelected()) {
				oneIsSelected = true;
			}
			if (properties.getDefaults().get(key).toString().endsWith(
					radioButton.getText())) {
				pos = i;
			}
			i++;
		}
		if (!oneIsSelected) {// no one is selected
			jRButton[pos].setSelected(true);
		}
		// p.setBackground(Color.WHITE);
		return p;
	}

	/**
	 * 
	 * @return
	 */
	private JPanel createJPanelSettingsReversibility() {
		jRadioButtonForceReacRev = new JRadioButton(
				"Model all reactions in a reversible manner");
		jRadioButtonForceReacRev.setSelected(properties
				.getBooleanProperty(SqueezerOptions.OPT_TREAT_ALL_REACTIONS_REVERSIBLE));
		jRadioButtonForceReacRev
				.setToolTipText(StringUtil.toHTML(
								"If checked, all reactions will be set to reversible no matter what is given by the SBML file.",
								GUITools.TOOLTIP_LINE_LENGTH));
		JRadioButton jRadioButtonSettingsFrameForceRevAsCD = new JRadioButton(
				"Use information from SBML");
		jRadioButtonSettingsFrameForceRevAsCD
				.setSelected(!(properties
						.getBooleanProperty(SqueezerOptions.OPT_TREAT_ALL_REACTIONS_REVERSIBLE)));
		jRadioButtonSettingsFrameForceRevAsCD
				.setToolTipText(StringUtil.toHTML(
								"If checked, the information about reversiblity will be left unchanged.",
								GUITools.TOOLTIP_LINE_LENGTH));
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonSettingsFrameForceRevAsCD);
		buttonGroup.add(jRadioButtonForceReacRev);
		GridBagLayout layout = new GridBagLayout();
		JPanel jPanelSettingsReversibility = new JPanel();
		jPanelSettingsReversibility.setLayout(layout);
		jPanelSettingsReversibility.setBorder(BorderFactory
				.createTitledBorder(" Reversibility "));
		LayoutHelper.addComponent(jPanelSettingsReversibility, layout,
				jRadioButtonSettingsFrameForceRevAsCD, 0, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(jPanelSettingsReversibility, layout,
				jRadioButtonForceReacRev, 0, 1, 1, 1, 1, 1);
		jRadioButtonForceReacRev.addItemListener(this);

		return jPanelSettingsReversibility;
	}

	/**
	 * 
	 * @return
	 */
	private JPanel createJPanelStandarVersions() {
		JPanel jPanelStandardVersions = new JPanel();
		jComboBoxTypeStandardVersion = new JComboBox(new String[] { "cat",
				"hal", "weg" });
		jComboBoxTypeStandardVersion.setSelectedIndex(properties
						.getIntProperty(SqueezerOptions.TYPE_STANDARD_VERSION));
		jComboBoxTypeStandardVersion.setToolTipText(StringUtil.toHTML(
								"Select the version of the modular rate laws. These options are described in the publications of Liebermeister et al. 2010. This option can only be accessed if all reactions are modeled reversibly.",
								GUITools.TOOLTIP_LINE_LENGTH));
		LayoutHelper helper = new LayoutHelper(jPanelStandardVersions);
		helper.add(new JPanel(), 0, 0, 5, 1, 1, 1);
		helper.add(new JPanel(), 0, 1, 1, 1, 1, 1);
		helper.add(new JLabel(StringUtil.toHTML(
				"Choose the version of modular rate laws:", 20)), 1, 1, 1, 1,
				0, 1);
		helper.add(new JPanel(), 2, 1, 1, 1, 1, 1);
		helper.add(jComboBoxTypeStandardVersion, 3, 1, 1, 1, 1, 0);
		helper.add(new JPanel(), 4, 1, 1, 1, 1, 1);
		helper.add(new JPanel(), 0, 2, 5, 1, 1, 1);
		jPanelStandardVersions.setBorder(BorderFactory
				.createTitledBorder(" Version of modular rate laws "));

		jComboBoxTypeStandardVersion.addItemListener(this);
		jComboBoxTypeStandardVersion.setEnabled(jRadioButtonForceReacRev
				.isSelected());

		return jPanelStandardVersions;
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	private JTabbedPane createMechanismTabs() {
		boolean treatReactionsReversible = properties
				.getBooleanProperty(SqueezerOptions.OPT_TREAT_ALL_REACTIONS_REVERSIBLE);
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
		Option<?> curr = SqueezerOptions.KINETICS_NONE_ENZYME_REACTIONS;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsNonEnzyme(treatReactionsReversible), curr));
		curr = SqueezerOptions.KINETICS_UNI_UNI_TYPE;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsUniUni(treatReactionsReversible), curr));
		curr = SqueezerOptions.KINETICS_BI_UNI_TYPE;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsBiUni(treatReactionsReversible), curr));
		curr = SqueezerOptions.KINETICS_BI_BI_TYPE;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsBiBi(treatReactionsReversible), curr));
		curr = SqueezerOptions.KINETICS_ARBITRARY_ENZYME_REACTIONS;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsArbitraryEnzyme(treatReactionsReversible), curr));
		curr = SqueezerOptions.KINETICS_GENE_REGULATION;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsGeneRegulation(treatReactionsReversible), curr));
		return tabs;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	private String createTitleFor(Option<?> key) {
		StringTokenizer st = new StringTokenizer(key.formatOptionName());
		StringBuilder title = new StringBuilder();
		while (st.hasMoreElements()) {
			title.append(' ');
			title.append(StringTools.firstLetterUpperCase(st.nextElement()
					.toString()));
		}
		title.append(' ');
		return title.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#getProperties()
	 */
	@Override
	public SBProperties getProperties() {
		SBProperties p = new SBProperties();
		Object value;
		for (Object key : properties.keySet()) {
			value = properties.get(key);
			try {
				Class<?> cl = Class.forName(value.toString());
				Set<Class<?>> interfaces = new HashSet<Class<?>>();
				for (Class<?> c : cl.getInterfaces()) {
					interfaces.add(c);
				}
				p.put(key, cl.getCanonicalName());
			} catch (ClassNotFoundException e) {
				p.put(key, value);
			}
		}
		return this.properties;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#getTitle()
	 */
	@Override
	public String getTitle() {
		return "Reaction mechanisms";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.gui.SettingsPanel#init()
	 */
	@Override
	public void init() {
		LayoutHelper lh = new LayoutHelper(this);
		lh.add(createJPanelSettingsReversibility(),
				createJPanelStandarVersions());
		tabs = createMechanismTabs();
		tabsPanel = new JPanel(new GridLayout(1, 1));
		tabsPanel.add(tabs);
		String msg = " Select the default rate law for each mechanism ";
		tabsPanel.setBorder(BorderFactory.createTitledBorder(msg));
		lh.add(tabsPanel, 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == null) {
			return;
		}
		if (e.getSource() instanceof JRadioButton) {
			JRadioButton rbutton = (JRadioButton) e.getSource();
			if (rbutton.equals(jRadioButtonForceReacRev)) {
				properties.put(SqueezerOptions.OPT_TREAT_ALL_REACTIONS_REVERSIBLE,
						Boolean.valueOf(jRadioButtonForceReacRev.isSelected()));
				jComboBoxTypeStandardVersion
						.setEnabled(jRadioButtonForceReacRev.isSelected());
				int selected = tabs.getSelectedIndex();
				tabsPanel.removeAll();
				properties.put(SqueezerOptions.OPT_TREAT_ALL_REACTIONS_REVERSIBLE,
						Boolean.valueOf(jRadioButtonForceReacRev.isSelected()));
				tabs = createMechanismTabs();
				tabs.setSelectedIndex(selected);
				tabsPanel.add(tabs);
				// init(jRadioButtonForceReacRev.isSelected());
				validate();
			} else {
				properties
						.put(rbutton.getActionCommand(),
								SBMLsqueezer.KINETICS_PACKAGE + '.'
										+ rbutton.getText());
			}
		} else if (e.getSource().equals(jComboBoxTypeStandardVersion)) {
			properties.put(SqueezerOptions.TYPE_STANDARD_VERSION, Integer
					.valueOf(jComboBoxTypeStandardVersion.getSelectedIndex()));
		}
		super.itemStateChanged(e);
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.prefs.PreferencesPanel#loadPreferences()
	 */
	protected SBPreferences loadPreferences() throws IOException {
		return SBPreferences.getPreferencesFor(SqueezerOptions.class);
	}
}
