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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.io.StringTools;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.kinetics.InterfaceArbitraryEnzymeKinetics;
import org.sbml.squeezer.kinetics.InterfaceBiBiKinetics;
import org.sbml.squeezer.kinetics.InterfaceBiUniKinetics;
import org.sbml.squeezer.kinetics.InterfaceGeneRegulatoryKinetics;
import org.sbml.squeezer.kinetics.InterfaceIrreversibleKinetics;
import org.sbml.squeezer.kinetics.InterfaceNonEnzymeKinetics;
import org.sbml.squeezer.kinetics.InterfaceReversibleKinetics;
import org.sbml.squeezer.kinetics.InterfaceUniUniKinetics;
import org.sbml.squeezer.rmi.Reflect;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-22
 */
public class SettingsPanelDefaultMechanisms extends JPanel implements
		ItemListener, ChangeListener {

	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 243553812503691739L;
	private static final Font titleFont = new Font("Dialog", Font.BOLD, 12);
	private static final Color borderColor = new Color(51, 51, 51);
	private static List<String> bib;
	private static List<String> bun;
	private static List<String> grn;
	private static List<String> non;
	private static List<String> arb;
	private static List<String> uni;
	private Properties settings;
	private List<ItemListener> itemListeners;

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
		init(((Boolean) properties
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue());
	}

	/**
	 * Initializes the selection of default mechanisms.
	 * 
	 * @param properties
	 */
	private void init(boolean treatReactionsReversible) {
		bib = new LinkedList<String>();
		bun = new LinkedList<String>();
		grn = new LinkedList<String>();
		non = new LinkedList<String>();
		arb = new LinkedList<String>();
		uni = new LinkedList<String>();
		Class<?> l[] = Reflect.getAllClassesInPackage(
				SBMLsqueezer.KINETICS_PACKAGE, false, true,
				BasicKineticLaw.class);
		for (Class<?> c : l) {
			if (!Modifier.isAbstract(c.getModifiers())) {
				Set<Class<?>> s = new HashSet<Class<?>>();
				for (Class<?> interf : c.getInterfaces())
					s.add(interf);
				if ((s.contains(InterfaceIrreversibleKinetics.class) || treatReactionsReversible)
						&& s.contains(InterfaceReversibleKinetics.class)) {
					if (s.contains(InterfaceUniUniKinetics.class))
						uni.add(c.getCanonicalName());
					if (s.contains(InterfaceBiUniKinetics.class))
						bun.add(c.getCanonicalName());
					if (s.contains(InterfaceBiBiKinetics.class))
						bib.add(c.getCanonicalName());
					if (s.contains(InterfaceArbitraryEnzymeKinetics.class))
						arb.add(c.getCanonicalName());
				}
				if (s.contains(InterfaceGeneRegulatoryKinetics.class))
					grn.add(c.getCanonicalName());
				if (s.contains(InterfaceNonEnzymeKinetics.class))
					non.add(c.getCanonicalName());
			}
		}
		setLayout(new GridLayout(1, 2));
		setBorder(BorderFactory.createTitledBorder(null,
				" Reaction Mechanisms ", TitledBorder.CENTER,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		setBackground(Color.WHITE);

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
			interfaceClass = InterfaceBiBiKinetics.class;
			break;
		case KINETICS_BI_UNI_TYPE:
			interfaceClass = InterfaceBiUniKinetics.class;
			break;
		case KINETICS_GENE_REGULATION:
			interfaceClass = InterfaceGeneRegulatoryKinetics.class;
			break;
		case KINETICS_NONE_ENZYME_REACTIONS:
			interfaceClass = InterfaceNonEnzymeKinetics.class;
			break;
		case KINETICS_OTHER_ENZYME_REACTIONS:
			interfaceClass = InterfaceArbitraryEnzymeKinetics.class;
			break;
		default: // case KINETICS_UNI_UNI_TYPE:
			interfaceClass = InterfaceUniUniKinetics.class;
			break;
		}
		JRadioButton jRButton[] = new JRadioButton[classes.size()];
		for (int i = 0; i < jRButton.length; i++) {
			String className = classes.get(i);
			String type = className.substring(className.lastIndexOf('.') + 1);
			jRButton[i] = new JRadioButton(className.substring(className
					.lastIndexOf('.') + 1));
			jRButton[i].setSelected(settings.get(key).toString().equals(className));
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
				toolTip.append(key.toString().toLowerCase().replace('_', ' '));
				toolTip.append(", can be described using ");
				toolTip.append(type);
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
			String className = SBMLsqueezer.KINETICS_PACKAGE + '.'
					+ rbutton.getText();
			String command = rbutton.getActionCommand();
			if (command.equals(InterfaceNonEnzymeKinetics.class
					.getCanonicalName()))
				settings.put(CfgKeys.KINETICS_NONE_ENZYME_REACTIONS, className);
			else if (command.equals(InterfaceGeneRegulatoryKinetics.class
					.getCanonicalName()))
				settings.put(CfgKeys.KINETICS_GENE_REGULATION, className);
			else if (command.equals(InterfaceUniUniKinetics.class
					.getCanonicalName()))
				settings.put(CfgKeys.KINETICS_UNI_UNI_TYPE, className);
			else if (command.equals(InterfaceBiUniKinetics.class
					.getCanonicalName()))
				settings.put(CfgKeys.KINETICS_BI_UNI_TYPE, className);
			else if (command.equals(InterfaceBiBiKinetics.class
					.getCanonicalName()))
				settings.put(CfgKeys.KINETICS_BI_BI_TYPE, className);
			for (ItemListener i : itemListeners)
				i.itemStateChanged(e);
		}
	}

	/**
	 * 
	 * @return
	 */
	public Properties getSettings() {
		return this.settings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof Boolean) {
			removeAll();
			init(((Boolean) e.getSource()).booleanValue());
			validate();
		}
	}
}
