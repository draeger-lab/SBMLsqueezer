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
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.Kinetics;
import org.sbml.squeezer.io.StringTools;
import org.sbml.squeezer.kinetics.ArbitraryEnzymeKinetics;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.kinetics.BiBiKinetics;
import org.sbml.squeezer.kinetics.BiUniKinetics;
import org.sbml.squeezer.kinetics.GeneRegulatoryKinetics;
import org.sbml.squeezer.kinetics.IrreversibleKinetics;
import org.sbml.squeezer.kinetics.NonEnzymeKinetics;
import org.sbml.squeezer.kinetics.ReversibleKinetics;
import org.sbml.squeezer.kinetics.UniUniKinetics;
import org.sbml.squeezer.rmi.Reflect;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-22
 */
public class SettingsPanelDefaultMechanisms extends JPanel implements
		ItemListener {

	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 243553812503691739L;
	private Properties settings;
	private List<ItemListener> itemListeners;
	private static final String pckg = "org.sbml.squeezer.kinetics";
	private static final Font titleFont = new Font("Dialog", Font.BOLD, 12);
	private static final Color borderColor = new Color(51, 51, 51);

	/**
	 * Reaction Mechanism Panel
	 * 
	 * @param properties
	 */
	public SettingsPanelDefaultMechanisms(Properties properties) {
		settings = new Properties();
		for (Object key : properties.keySet()) {
			if (key.toString().startsWith("KINETICS_"))
				settings.put(key, properties.get(key));
		}
		itemListeners = new LinkedList<ItemListener>();

		setLayout(new GridLayout(1, 2));
		setBorder(BorderFactory.createTitledBorder(null,
				" Reaction Mechanisms ", TitledBorder.CENTER,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		setBackground(Color.WHITE);

		List<String> bib = new LinkedList<String>();
		List<String> bun = new LinkedList<String>();
		List<String> grn = new LinkedList<String>();
		List<String> non = new LinkedList<String>();
		List<String> arb = new LinkedList<String>();
		List<String> uni = new LinkedList<String>();
		Class<?> l[] = Reflect.getAllClassesInPackage(pckg, false, true,
				BasicKineticLaw.class);
		for (Class<?> c : l) {
			if (!Modifier.isAbstract(c.getModifiers())) {
				Set<Class<?>> s = new HashSet<Class<?>>();
				for (Class<?> interf : c.getInterfaces())
					s.add(interf);
				if (s.contains(IrreversibleKinetics.class)
						&& s.contains(ReversibleKinetics.class)) {
					if (s.contains(UniUniKinetics.class))
						uni.add(c.getCanonicalName());
					if (s.contains(BiUniKinetics.class))
						bun.add(c.getCanonicalName());
					if (s.contains(BiBiKinetics.class))
						bib.add(c.getCanonicalName());
					if (s.contains(ArbitraryEnzymeKinetics.class))
						arb.add(c.getCanonicalName());
				}
				if (s.contains(GeneRegulatoryKinetics.class))
					grn.add(c.getCanonicalName());
				if (s.contains(NonEnzymeKinetics.class))
					non.add(c.getCanonicalName());
			}
		}

		// Add all sub-panels to the reaction mechanism panel:
		// Sub-panels for the reaction mechanism panel
		JPanel leftMechanismPanel = new JPanel();
		leftMechanismPanel.setBackground(Color.WHITE);
		LayoutHelper lh = new LayoutHelper(leftMechanismPanel);
		lh.add(createButtonGroupPanel(non,
				CfgKeys.KINETICS_NONE_ENZYME_REACTIONS));
		lh.add(createButtonGroupPanel(uni, CfgKeys.KINETICS_UNI_UNI_TYPE));
		lh.add(createButtonGroupPanel(bun, CfgKeys.KINETICS_BI_UNI_TYPE));

		JPanel rightMechanismPanel = new JPanel();
		rightMechanismPanel.setBackground(Color.WHITE);
		lh = new LayoutHelper(rightMechanismPanel);
		lh.add(createButtonGroupPanel(bib, CfgKeys.KINETICS_BI_BI_TYPE));
		lh.add(createButtonGroupPanel(arb,
				CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS));
		lh.add(createButtonGroupPanel(grn, CfgKeys.KINETICS_GENE_REGULATION));

		add(leftMechanismPanel);
		add(rightMechanismPanel);
	}

	/**
	 * Creates a panel that contains radio buttons for the given class of
	 * kinetic equations.
	 * 
	 * @param classes
	 * @param key
	 * @param interfaceClass
	 * @return
	 */
	private JPanel createButtonGroupPanel(List<String> classes, CfgKeys key) {
		ButtonGroup buttonGroup = new ButtonGroup();
		JPanel p = new JPanel();
		LayoutHelper helper = new LayoutHelper(p);
		StringTokenizer st = new StringTokenizer(key.toString().substring(8)
				.toLowerCase().replace('_', ' '));
		StringBuilder title = new StringBuilder();
		while (st.hasMoreElements()) {
			title.append(' ');
			title.append(StringTools.firstLetterUpperCase(st.nextElement()
					.toString()));
		}
		title.append(' ');
		p.setBorder(BorderFactory.createTitledBorder(null, title.toString(),
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		Class<?> interfaceClass;
		switch (key) {
		case KINETICS_BI_BI_TYPE:
			interfaceClass = BiBiKinetics.class;
			break;
		case KINETICS_BI_UNI_TYPE:
			interfaceClass = BiUniKinetics.class;
			break;
		case KINETICS_GENE_REGULATION:
			interfaceClass = GeneRegulatoryKinetics.class;
			break;
		case KINETICS_NONE_ENZYME_REACTIONS:
			interfaceClass = NonEnzymeKinetics.class;
			break;
		case KINETICS_OTHER_ENZYME_REACTIONS:
			interfaceClass = ArbitraryEnzymeKinetics.class;
			break;
		default: // case KINETICS_UNI_UNI_TYPE:
			interfaceClass = UniUniKinetics.class;
			break;
		}
		JRadioButton jRButton[] = new JRadioButton[classes.size()];
		for (int i = 0; i < jRButton.length; i++) {
			String className = classes.get(i);
			Kinetics type = Kinetics.getTypeForName(className);
			jRButton[i] = new JRadioButton(className.substring(className
					.lastIndexOf('.') + 1));
			jRButton[i].setSelected(Kinetics.valueOf(settings.get(key)
					.toString()) == type);
			StringBuilder toolTip = new StringBuilder();
			switch (key) {
			case KINETICS_GENE_REGULATION:
				toolTip.append("Check this box if you want ");
				toolTip.append(jRButton[i].getText());
				toolTip.append(" as reaction scheme for gene regulation ");
				toolTip.append("(reactions involving genes, RNA and proteins)");
				break;
			case KINETICS_UNI_UNI_TYPE:
				toolTip.append("Check this box if ");
				toolTip.append(jRButton[i].getText());
				toolTip.append(" is to be applied for uni-uni reactions");
				toolTip.append(" (one reactant, one product).");
				break;
			case KINETICS_BI_UNI_TYPE:
				toolTip.append("Check this box if you want the ");
				toolTip.append(jRButton[i].getText());
				toolTip.append(" scheme as reaction scheme for bi-uni ");
				toolTip.append("reactions (two reactant, one product).");
				break;
			case KINETICS_BI_BI_TYPE:
				toolTip.append("Check this box if you want the ");
				toolTip.append(jRButton[i].getText());
				toolTip.append(" to be applied to all ");
				toolTip
						.append("bi-bi reactions (two reactants, two products).");
				break;
			case KINETICS_OTHER_ENZYME_REACTIONS:
				toolTip
						.append("Reactions, which are enzyme-catalyzed, and follow");
				toolTip
						.append("none of the schemes uni-uni, bi-uni and bi-bi ");
				toolTip.append("can be modeled using ");
				toolTip.append(jRButton[i].getText());
				toolTip.append('.');
				break;
			default:
				toolTip.append("Reactions, which are ");
				toolTip.append(type.toString().toLowerCase().replace('_', ' '));
				toolTip.append(", can be described using ");
				toolTip.append(type.getEquationName());
				toolTip.append('.');
				break;
			}
			jRButton[i].setToolTipText(GUITools.toHTML(toolTip.toString(), 40));
			jRButton[i].setEnabled(true);
			jRButton[i].setBackground(Color.WHITE);
			jRButton[i].addItemListener(this);
			jRButton[i].setActionCommand(interfaceClass.getCanonicalName());
			buttonGroup.add(jRButton[i]);
			helper.add(jRButton[i]);
		}
		p.setBackground(Color.WHITE);
		return p;
	}

	/**
	 * 
	 * @param l
	 */
	public void addItemListener(ItemListener l) {
		itemListeners.add(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() instanceof JRadioButton) {
			JRadioButton rbutton = (JRadioButton) e.getSource();
			Kinetics type = Kinetics.getTypeForName(pckg + '.'
					+ rbutton.getText());
			String command = rbutton.getActionCommand();
			if (command.equals(NonEnzymeKinetics.class.getCanonicalName()))
				settings.put(CfgKeys.KINETICS_NONE_ENZYME_REACTIONS, type);
			else if (command.equals(GeneRegulatoryKinetics.class
					.getCanonicalName()))
				settings.put(CfgKeys.KINETICS_GENE_REGULATION, type);
			else if (command.equals(UniUniKinetics.class.getCanonicalName()))
				settings.put(CfgKeys.KINETICS_UNI_UNI_TYPE, type);
			else if (command.equals(BiUniKinetics.class.getCanonicalName()))
				settings.put(CfgKeys.KINETICS_BI_UNI_TYPE, type);
			else if (command.equals(BiBiKinetics.class.getCanonicalName()))
				settings.put(CfgKeys.KINETICS_BI_BI_TYPE, type);
		}
		for (ItemListener i : itemListeners)
			i.itemStateChanged(e);
	}

	/**
	 * 
	 * @return
	 */
	public Properties getSettings() {
		return this.settings;
	}
}
