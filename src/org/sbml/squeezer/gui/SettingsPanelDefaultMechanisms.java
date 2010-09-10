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

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.util.StringTools;

import de.zbit.gui.LayoutHelper;

/**
 * A {@link JPanel} to let the user select all default instances of
 * {@link BasicKineticLaw} that implement the designated interfaces to be
 * available for certain reaction types.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2009-09-22
 * @since 1.3
 */
public class SettingsPanelDefaultMechanisms extends SettingsPanel {

	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 243553812503691739L;
	/**
	 * 
	 */
	private Properties settings, origSett;

	/**
	 * Reaction Mechanism Panel
	 * 
	 * @param properties
	 */
	public SettingsPanelDefaultMechanisms(Properties properties) {
		super(properties);
		settings = new Properties();
		String k;
		for (Object key : properties.keySet()) {
			k = key.toString();
			if (k.startsWith("KINETICS_")
					|| k.equals("OPT_TREAT_ALL_REACTIONS_REVERSIBLE")
					|| k.equals("TYPE_STANDARD_VERSION")) {
				settings.put(key, properties.get(key));
			}
		}
		origSett = properties;
		init(((Boolean) origSett
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue());
	}

	/**
	 * Creates a panel that contains radio buttons for the given class of
	 * kinetic equations.
	 * 
	 * @param classes
	 * @param key
	 * @return
	 */
	private JPanel createButtonGroupPanel(Set<String> classes, CfgKeys key) {
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
			jRButton[i].setSelected(settings.get(key).toString().equals(
					className));
			StringBuilder toolTip = new StringBuilder();
			String msg;
			switch (key) {
			case KINETICS_GENE_REGULATION:
				msg = "Check this box if you want %s  as reaction scheme for gene regulation (reactions involving genes, RNA and proteins)";
				toolTip.append(String.format(msg, jRButton[i].getText()));
				break;
			case KINETICS_UNI_UNI_TYPE:
				msg = "Check this box if %s is to be applied for uni-uni reactions (one reactant, one product).";
				toolTip.append(String.format(msg, jRButton[i].getText()));
				break;
			case KINETICS_BI_UNI_TYPE:
				msg = "Check this box if you want the %s scheme as reaction scheme for bi-uni reactions (two reactant, one product).";
				toolTip.append(String.format(msg, jRButton[i].getText()));
				break;
			case KINETICS_BI_BI_TYPE:
				msg = "Check this box if you want the %s to be applied to all bi-bi reactions (two reactants, two products).";
				toolTip.append(String.format(msg, jRButton[i].getText()));
				break;
			case KINETICS_OTHER_ENZYME_REACTIONS:
				msg = "Reactions, which are enzyme-catalyzed, and follow none of the schemes uni-uni, bi-uni and bi-bi can be modeled using %s.";
				toolTip.append(String.format(msg, jRButton[i].getText()));
				break;
			default:
				msg = "Reactions, which are %s, can be described using %s.";
				toolTip.append(String.format(msg, key.toString().toLowerCase()
						.replace('_', ' '), type));
				break;
			}
			jRButton[i].setToolTipText(GUITools.toHTML(toolTip.toString(), 40));
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
			if (radioButton.isSelected())
				oneIsSelected = true;
			if (origSett.get(key).toString().endsWith(radioButton.getText()))
				pos = i;
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
	 * @param key
	 * @return
	 */
	private String createTitleFor(CfgKeys key) {
		StringTokenizer st = new StringTokenizer(key.toString().substring(8)
				.toLowerCase().replace('_', ' '));
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
	public Properties getProperties() {
		Properties p = new Properties();
		Object value;
		for (Object key : settings.keySet()) {
			value = settings.get(key);
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
		return this.settings;
	}

	/**
	 * Initializes the selection of default mechanisms.
	 * 
	 * @param properties
	 */
	private void init(boolean treatReactionsReversible) {
		// setLayout(new GridLayout(1, 2));
		// JPanel leftMechanismPanel = new JPanel();
		// LayoutHelper lh = new LayoutHelper(leftMechanismPanel);
		// lh.add(createButtonGroupPanel(ReactionType
		// .getKineticsNonEnzyme(treatReactionsReversible),
		// CfgKeys.KINETICS_NONE_ENZYME_REACTIONS));
		// lh.add(createButtonGroupPanel(ReactionType
		// .getKineticsUniUni(treatReactionsReversible),
		// CfgKeys.KINETICS_UNI_UNI_TYPE));
		// lh.add(createButtonGroupPanel(ReactionType
		// .getKineticsBiUni(treatReactionsReversible),
		// CfgKeys.KINETICS_BI_UNI_TYPE));

		// JPanel rightMechanismPanel = new JPanel();
		// lh = new LayoutHelper(rightMechanismPanel);
		// lh.add(createButtonGroupPanel(ReactionType
		// .getKineticsBiBi(treatReactionsReversible),
		// CfgKeys.KINETICS_BI_BI_TYPE));
		// lh.add(createButtonGroupPanel(ReactionType
		// .getKineticsArbitraryEnzyme(treatReactionsReversible),
		// CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS));
		// lh.add(createButtonGroupPanel(ReactionType
		// .getKineticsGeneRegulation(treatReactionsReversible),
		// CfgKeys.KINETICS_GENE_REGULATION));

		// add(leftMechanismPanel);
		// add(rightMechanismPanel);
		// GUITools.setAllBackground(this, Color.WHITE);
		LayoutHelper lh = new LayoutHelper(this);
		lh.add(createJPanelSettingsReversibility(),
				createJPanelStandarVersions());
		tabs = createMechanismTabs(treatReactionsReversible);
		tabsPanel = new JPanel(new GridLayout(1, 1));
		tabsPanel.add(tabs);
		tabsPanel
				.setBorder(BorderFactory
						.createTitledBorder(" Select the default rate law for each mechanism "));
		lh.add(tabsPanel, 2);
	}

	/**
	 * 
	 * @param treatReactionsReversible
	 * @return
	 */
	private JTabbedPane createMechanismTabs(boolean treatReactionsReversible) {
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
		CfgKeys curr = CfgKeys.KINETICS_NONE_ENZYME_REACTIONS;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsNonEnzyme(treatReactionsReversible), curr));
		curr = CfgKeys.KINETICS_UNI_UNI_TYPE;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsUniUni(treatReactionsReversible), curr));
		curr = CfgKeys.KINETICS_BI_UNI_TYPE;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsBiUni(treatReactionsReversible), curr));
		curr = CfgKeys.KINETICS_BI_BI_TYPE;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsBiBi(treatReactionsReversible), curr));
		curr = CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsArbitraryEnzyme(treatReactionsReversible), curr));
		curr = CfgKeys.KINETICS_GENE_REGULATION;
		tabs.addTab(createTitleFor(curr), createButtonGroupPanel(ReactionType
				.getKineticsGeneRegulation(treatReactionsReversible), curr));
		return tabs;
	}

	/**
	 * 
	 */
	private JPanel tabsPanel;
	private JTabbedPane tabs;
	/**
	 * 
	 */
	private JComboBox jComboBoxTypeStandardVersion;

	/**
	 * 
	 * @return
	 */
	private JPanel createJPanelStandarVersions() {
		JPanel jPanelStandardVersions = new JPanel();
		jComboBoxTypeStandardVersion = new JComboBox(new String[] { "cat",
				"hal", "weg" });
		jComboBoxTypeStandardVersion.setSelectedIndex(((Integer) this.settings
				.get(CfgKeys.TYPE_STANDARD_VERSION)).intValue());
		jComboBoxTypeStandardVersion
				.setToolTipText(GUITools
						.toHTML(
								"Select the version of the modular rate laws. These options are described in the publications of Liebermeister et al. 2010. This option can only be accessed if all reactions are modeled reversibly.",
								40));
		LayoutHelper helper = new LayoutHelper(jPanelStandardVersions);
		helper.add(new JPanel(), 0, 0, 5, 1, 1, 1);
		helper.add(new JPanel(), 0, 1, 1, 1, 1, 1);
		helper.add(new JLabel(GUITools.toHTML(
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
	 * @return
	 */
	private JPanel createJPanelSettingsReversibility() {
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
	 */
	private JRadioButton jRadioButtonForceReacRev;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == null) {
			return;
		}
		if (e.getSource() instanceof JRadioButton) {
			JRadioButton rbutton = (JRadioButton) e.getSource();
			if (rbutton.equals(jRadioButtonForceReacRev)) {
				settings.put(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE,
						Boolean.valueOf(jRadioButtonForceReacRev.isSelected()));
				jComboBoxTypeStandardVersion
						.setEnabled(jRadioButtonForceReacRev.isSelected());
				int selected = tabs.getSelectedIndex();
				tabsPanel.removeAll();
				tabs = createMechanismTabs(jRadioButtonForceReacRev
						.isSelected());
				tabs.setSelectedIndex(selected);
				tabsPanel.add(tabs);
				// init(jRadioButtonForceReacRev.isSelected());
				validate();
			} else {
				settings
						.put(CfgKeys.valueOf(rbutton.getActionCommand()),
								SBMLsqueezer.KINETICS_PACKAGE + '.'
										+ rbutton.getText());
			}
		} else if (e.getSource().equals(jComboBoxTypeStandardVersion)) {
			settings.put(CfgKeys.TYPE_STANDARD_VERSION, Integer
					.valueOf(jComboBoxTypeStandardVersion.getSelectedIndex()));
		}
		super.itemStateChanged(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.gui.SettingsPanel#setProperties(java.util.Properties)
	 */
	public void setProperties(Properties settings) {
		removeAll();
		this.settings = settings;
		init(((Boolean) settings
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue());
	}
}
