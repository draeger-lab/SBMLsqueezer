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
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.Kinetics;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
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
	private JRadioButton jRadioButtonGMAK;
	private JRadioButton hillKineticsRadioButton;
	private JRadioButton jRadioButtonUniUniMMK;
	private JRadioButton jRadioButtonUniUniCONV;
	private JRadioButton jRadioButtonBiUniORD;
	private JRadioButton jRadioButtonBiUniCONV;
	private JRadioButton jRadioButtonBiUniRND;
	private JRadioButton jRadioButtonBiBiRND;
	private JRadioButton jRadioButtonBiBiCONV;
	private JRadioButton jRadioButtonBiBiORD;
	private JRadioButton jRadioButtonBiBiPP;
	private JRadioButton jRadioButtonOtherEnzymCONV;
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
		Font titleFont = new Font("Dialog", Font.BOLD, 12);
		Color borderColor = new Color(51, 51, 51);

		setLayout(new GridLayout(1, 2));
		setBorder(BorderFactory.createTitledBorder(null,
				" Reaction Mechanisms ", TitledBorder.CENTER,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		setBackground(Color.WHITE);

		// Sub-panels for the reaction mechanism panel
		JPanel leftMechanismPanel = new JPanel(new GridBagLayout());
		leftMechanismPanel.setBackground(Color.WHITE);

		JPanel rightMechanismPanel = new JPanel(new GridBagLayout());
		rightMechanismPanel.setBackground(Color.WHITE);

		// Non-Enzyme Reactions
		JPanel nonEnzyme = new JPanel(new GridLayout(1, 1));
		nonEnzyme.setBorder(BorderFactory.createTitledBorder(null,
				" Non-Enzyme Reaction ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		jRadioButtonGMAK = new JRadioButton(
				"Genereralized mass-action kinetics");
		jRadioButtonGMAK
				.setSelected(Kinetics.valueOf(settings.get(
						CfgKeys.KINETICS_NONE_ENZYME_REACTIONS).toString()) == Kinetics.GENERALIZED_MASS_ACTION);
		jRadioButtonGMAK
				.setToolTipText("<html>Reactions, which are not enzyme-catalyzed,<br>"
						+ "are described using generalized mass-action kinetics.</html>");
		jRadioButtonGMAK.setEnabled(false);
		jRadioButtonGMAK.setBackground(Color.WHITE);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonGMAK);

		nonEnzyme.add(jRadioButtonGMAK);
		nonEnzyme.setBackground(Color.WHITE);

		// Hill equation
		JPanel geneRegulation = new JPanel(new GridLayout(1, 1));
		geneRegulation.setBorder(BorderFactory.createTitledBorder(null,
				" Gene Expression Regulation ",
				TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));
		hillKineticsRadioButton = new JRadioButton("Hill equation");
		hillKineticsRadioButton
				.setToolTipText("<html>Check this box if you want the Hill equation as reaction scheme<br>"
						+ "for gene regulation (reactions involving genes, RNA and proteins).</html>");
		hillKineticsRadioButton.setBackground(Color.WHITE);
		hillKineticsRadioButton
				.setSelected(Kinetics.valueOf(settings.get(
						CfgKeys.KINETICS_GENE_REGULATION).toString()) == Kinetics.HILL_EQUATION);
		hillKineticsRadioButton.setEnabled(false);
		buttonGroup = new ButtonGroup();
		buttonGroup.add(hillKineticsRadioButton);
		geneRegulation.add(hillKineticsRadioButton);
		geneRegulation.setBackground(Color.WHITE);

		// Uni-Uni Reactions
		JPanel uniUniReactions = new JPanel(new GridLayout(2, 1));
		uniUniReactions.setBorder(BorderFactory.createTitledBorder(null,
				" Uni-Uni Reaction ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		jRadioButtonUniUniMMK = new JRadioButton();
		jRadioButtonUniUniMMK
				.setSelected(Kinetics.valueOf(settings.get(
						CfgKeys.KINETICS_UNI_UNI_TYPE).toString()) == Kinetics.MICHAELIS_MENTEN);
		jRadioButtonUniUniMMK
				.setToolTipText("<html>Check this box if Michaelis-Menten kinetics is to be<br>"
						+ "applied for uni-uni reactions (one reactant, one product).</html>");
		jRadioButtonUniUniMMK.setText("Michaelis-Menten kinetics");
		// uni-uni-type = 3
		jRadioButtonUniUniMMK.setBackground(Color.WHITE);

		jRadioButtonUniUniCONV = new JRadioButton("Convenience kinetics");
		// uni-uni-type = 2
		jRadioButtonUniUniCONV
				.setSelected(settings.get(CfgKeys.KINETICS_UNI_UNI_TYPE) == Kinetics.CONVENIENCE_KINETICS);
		jRadioButtonUniUniCONV
				.setToolTipText("<html>Check this box if convenience kinetics is to be<br>"
						+ "applied for uni-uni reactions (one reactant, one product).</html>");
		jRadioButtonUniUniCONV.setBackground(Color.WHITE);

		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonUniUniMMK);
		buttonGroup.add(jRadioButtonUniUniCONV);

		uniUniReactions.add(jRadioButtonUniUniMMK);
		uniUniReactions.add(jRadioButtonUniUniCONV);
		uniUniReactions.setBackground(Color.WHITE);

		// Bi-Uni Reactions
		JPanel biUniReactions = new JPanel(new GridLayout(3, 1));
		biUniReactions.setBorder(BorderFactory.createTitledBorder(null,
				" Bi-Uni Reaction ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		this.jRadioButtonBiUniORD = new JRadioButton();
		jRadioButtonBiUniORD
				.setSelected(Kinetics.valueOf(settings.get(
						CfgKeys.KINETICS_BI_UNI_TYPE).toString()) == Kinetics.ORDERED_MECHANISM);
		jRadioButtonBiUniORD
				.setToolTipText("<html>Check this box if you want the ordered mechanism scheme as<br>"
						+ "reaction scheme for bi-uni reactions (two reactant, one product).</html>");
		jRadioButtonBiUniORD.setText("Ordered"); // biUiType = 6
		jRadioButtonBiUniORD.setBackground(Color.WHITE);

		jRadioButtonBiUniCONV = new JRadioButton("Convenience kinetics"); // biUniType
		// = 2
		jRadioButtonBiUniCONV
				.setSelected(settings.get(CfgKeys.KINETICS_BI_UNI_TYPE) == Kinetics.CONVENIENCE_KINETICS);
		jRadioButtonBiUniCONV
				.setToolTipText("<html>Check this box if you want convenience kinetics as<br>"
						+ "reaction scheme for bi-uni reactions (two reactant, one product).</html>");
		jRadioButtonBiUniCONV.setBackground(Color.WHITE);

		jRadioButtonBiUniRND = new JRadioButton("Random"); // biUniType = 4
		jRadioButtonBiUniRND
				.setSelected(Kinetics.valueOf(settings.get(
						CfgKeys.KINETICS_BI_UNI_TYPE).toString()) == Kinetics.RANDOM_ORDER_MECHANISM);
		jRadioButtonBiUniRND
				.setToolTipText("<html>Check this box if you want the random mechanism scheme<br>"
						+ "to be applied for bi-uni reactions (two reactants, one product).</html>");
		jRadioButtonBiUniRND.setBackground(Color.WHITE);

		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonBiUniORD);
		buttonGroup.add(jRadioButtonBiUniCONV);
		buttonGroup.add(jRadioButtonBiUniRND);

		biUniReactions.add(jRadioButtonBiUniORD);
		biUniReactions.add(jRadioButtonBiUniCONV);
		biUniReactions.add(jRadioButtonBiUniRND);
		biUniReactions.setBackground(Color.WHITE);

		// right panel
		// Bi-Bi Reactions
		JPanel biBiReactions = new JPanel(new GridLayout(4, 1));
		biBiReactions.setBorder(BorderFactory.createTitledBorder(null,
				" Bi-Bi Reaction ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		jRadioButtonBiBiRND = new JRadioButton("Random"); // biBiType = 4
		jRadioButtonBiBiRND
				.setSelected(Kinetics.valueOf(settings.get(
						CfgKeys.KINETICS_BI_BI_TYPE).toString()) == Kinetics.RANDOM_ORDER_MECHANISM);
		jRadioButtonBiBiRND
				.setToolTipText("<html>Check this box if you want the random mechanism to be applied<br>"
						+ "to bi-bi reactions (two reactants, two products).</html>");
		jRadioButtonBiBiRND.setBackground(Color.WHITE);

		jRadioButtonBiBiCONV = new JRadioButton("Convenience kinetics"); // biBiType
		// = 2
		jRadioButtonBiBiCONV
				.setSelected(settings.get(CfgKeys.KINETICS_BI_BI_TYPE) == Kinetics.CONVENIENCE_KINETICS);
		jRadioButtonBiBiCONV
				.setToolTipText("<html>Check this box if you want convenience kinetics to be applied<br>"
						+ "for bi-bi reactions (two reactants, two products).</html>");
		jRadioButtonBiBiCONV.setBackground(Color.WHITE);

		jRadioButtonBiBiORD = new JRadioButton("Ordered"); // biBiType = 6
		jRadioButtonBiBiORD
				.setSelected(settings.get(CfgKeys.KINETICS_BI_BI_TYPE) == Kinetics.ORDERED_MECHANISM);
		jRadioButtonBiBiORD
				.setToolTipText("<html>Check this box if you want the ordered mechanism as reaction scheme<br>"
						+ "for bi-bi reactions (two reactants, two products).</html>");
		jRadioButtonBiBiORD.setBackground(Color.WHITE);

		jRadioButtonBiBiPP = new JRadioButton("Ping-pong"); // biBiType = 5
		jRadioButtonBiBiPP
				.setSelected(settings.get(CfgKeys.KINETICS_BI_BI_TYPE) == Kinetics.PING_PONG_MECAHNISM);
		jRadioButtonBiBiPP
				.setToolTipText("<html>Check this box if you want the ping-pong mechanism as reaction scheme<br>"
						+ "for bi-bi reactions (two reactants, two products).</html>");
		jRadioButtonBiBiPP.setBackground(Color.WHITE);

		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonBiBiRND);
		buttonGroup.add(jRadioButtonBiBiCONV);
		buttonGroup.add(jRadioButtonBiBiORD);
		buttonGroup.add(jRadioButtonBiBiPP);

		biBiReactions.add(jRadioButtonBiBiRND);
		biBiReactions.add(jRadioButtonBiBiCONV);
		biBiReactions.add(jRadioButtonBiBiORD);
		biBiReactions.add(jRadioButtonBiBiPP);
		biBiReactions.setBackground(Color.WHITE);

		// Other Enzyme Reactions
		JPanel otherEnzymeReactions = new JPanel(new GridLayout(1, 1));
		otherEnzymeReactions.setBorder(BorderFactory.createTitledBorder(null,
				" Other Enzyme Reaction ", TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, titleFont, borderColor));

		jRadioButtonOtherEnzymCONV = new JRadioButton();
		jRadioButtonOtherEnzymCONV
				.setSelected(Kinetics.valueOf(settings.get(
						CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS).toString()) == Kinetics.CONVENIENCE_KINETICS);
		jRadioButtonOtherEnzymCONV.setEnabled(true);
		jRadioButtonOtherEnzymCONV
				.setToolTipText("<html>Reactions, which are enzyme-catalyzed, and follow<br>"
						+ "none of the schemes uni-uni, bi-uni and bi-bi.</html>");
		jRadioButtonOtherEnzymCONV.setText("Convenience kinetics");
		jRadioButtonOtherEnzymCONV.setEnabled(false);
		jRadioButtonOtherEnzymCONV.setBackground(Color.WHITE);

		buttonGroup = new ButtonGroup();
		buttonGroup.add(jRadioButtonOtherEnzymCONV);

		otherEnzymeReactions.add(jRadioButtonOtherEnzymCONV);
		otherEnzymeReactions.setBackground(Color.WHITE);

		// Add all sub-panels to the reaction mechanism panel:
		GridBagLayout layout = (GridBagLayout) leftMechanismPanel.getLayout();
		LayoutHelper.addComponent(leftMechanismPanel, layout, nonEnzyme, 0, 0,
				1, 1, 1, 0);
		LayoutHelper.addComponent(leftMechanismPanel, layout, uniUniReactions,
				0, 1, 1, 1, 1, 0);
		LayoutHelper.addComponent(leftMechanismPanel, layout, biUniReactions,
				0, 2, 1, 1, 1, 0);
		layout = (GridBagLayout) rightMechanismPanel.getLayout();
		LayoutHelper.addComponent(rightMechanismPanel, layout, geneRegulation,
				0, 0, 1, 1, 1, 0);
		LayoutHelper.addComponent(rightMechanismPanel, layout, biBiReactions,
				0, 1, 1, 1, 1, 0);
		LayoutHelper.addComponent(rightMechanismPanel, layout,
				otherEnzymeReactions, 0, 2, 1, 1, 1, 0);

		add(leftMechanismPanel);
		add(rightMechanismPanel);

		jRadioButtonUniUniMMK.setEnabled(true);
		jRadioButtonUniUniCONV.setEnabled(true);
		jRadioButtonBiUniORD.setEnabled(true);
		jRadioButtonBiUniCONV.setEnabled(true);
		jRadioButtonBiUniRND.setEnabled(true);
		jRadioButtonBiBiPP.setEnabled(true);
		jRadioButtonBiBiORD.setEnabled(true);
		jRadioButtonBiBiCONV.setEnabled(true);
		jRadioButtonBiBiRND.setEnabled(true);

		jRadioButtonGMAK.addItemListener(this);
		hillKineticsRadioButton.addItemListener(this);
		jRadioButtonUniUniMMK.addItemListener(this);
		jRadioButtonUniUniCONV.addItemListener(this);
		jRadioButtonBiUniORD.addItemListener(this);
		jRadioButtonBiUniCONV.addItemListener(this);
		jRadioButtonBiUniRND.addItemListener(this);
		jRadioButtonBiBiRND.addItemListener(this);
		jRadioButtonBiBiCONV.addItemListener(this);
		jRadioButtonBiBiORD.addItemListener(this);
		jRadioButtonBiBiPP.addItemListener(this);
		jRadioButtonOtherEnzymCONV.addItemListener(this);

		if (settings.get(CfgKeys.KINETICS_UNI_UNI_TYPE) == Kinetics.MICHAELIS_MENTEN)
			jRadioButtonUniUniMMK.setSelected(true);
		else
			jRadioButtonUniUniMMK.setSelected(false);

		if (settings.get(CfgKeys.KINETICS_UNI_UNI_TYPE) == Kinetics.CONVENIENCE_KINETICS)
			jRadioButtonUniUniCONV.setSelected(true);
		else
			jRadioButtonUniUniCONV.setSelected(false);

		if (settings.get(CfgKeys.KINETICS_BI_UNI_TYPE) == Kinetics.ORDERED_MECHANISM)
			jRadioButtonBiUniORD.setSelected(true);
		else
			jRadioButtonBiUniORD.setSelected(false);

		if (settings.get(CfgKeys.KINETICS_BI_UNI_TYPE) == Kinetics.CONVENIENCE_KINETICS)
			jRadioButtonBiUniCONV.setSelected(true);
		else
			jRadioButtonBiUniCONV.setSelected(false);

		if (settings.get(CfgKeys.KINETICS_BI_UNI_TYPE) == Kinetics.RANDOM_ORDER_MECHANISM)
			jRadioButtonBiUniRND.setSelected(true);
		else
			jRadioButtonBiUniRND.setSelected(false);

		if (settings.get(CfgKeys.KINETICS_BI_BI_TYPE) == Kinetics.PING_PONG_MECAHNISM)
			jRadioButtonBiBiPP.setSelected(true);
		else
			jRadioButtonBiBiPP.setSelected(false);

		if (settings.get(CfgKeys.KINETICS_BI_BI_TYPE) == Kinetics.ORDERED_MECHANISM)
			jRadioButtonBiBiORD.setSelected(true);
		else
			jRadioButtonBiBiORD.setSelected(false);

		if (settings.get(CfgKeys.KINETICS_BI_BI_TYPE) == Kinetics.CONVENIENCE_KINETICS)
			jRadioButtonBiBiCONV.setSelected(true);
		else
			jRadioButtonBiBiCONV.setSelected(false);

		if (settings.get(CfgKeys.KINETICS_BI_BI_TYPE) == Kinetics.RANDOM_ORDER_MECHANISM)
			jRadioButtonBiBiRND.setSelected(true);
		else
			jRadioButtonBiBiRND.setSelected(false);

		jRadioButtonOtherEnzymCONV.setSelected(true);

		jRadioButtonUniUniMMK.setEnabled(true);
		jRadioButtonUniUniCONV.setEnabled(true);
		jRadioButtonBiUniORD.setEnabled(true);
		jRadioButtonBiUniCONV.setEnabled(true);
		jRadioButtonBiUniRND.setEnabled(true);
		jRadioButtonBiBiPP.setEnabled(true);
		jRadioButtonBiBiORD.setEnabled(true);
		jRadioButtonBiBiCONV.setEnabled(true);
		jRadioButtonBiBiRND.setEnabled(true);
		jRadioButtonOtherEnzymCONV.setEnabled(false);

//		Class<?> l[] = Reflect.getAllClassesInPackage(
//				"org.sbml.squeezer.kinetics", false, true,
//				BasicKineticLaw.class);
//		for (Class<?> c : l)
//			if (!Modifier.isAbstract(c.getModifiers())) {
//				System.out.println(c.getCanonicalName());
//				for (Class<?> interf : c.getInterfaces()) {
//					if (interf.getCanonicalName().endsWith("GeneRegulatoryKinetics"))
//						System.out.println(" implementiert interface ");
//				}
//				try {
//				} catch (SecurityException e) {
//					e.printStackTrace();
//				} catch (NoSuchMethodException e) {
//					e.printStackTrace();
//				} catch (IllegalArgumentException e) {
//					e.printStackTrace();
//				} catch (IllegalAccessException e) {
//					e.printStackTrace();
//				} catch (InvocationTargetException e) {
//					e.printStackTrace();
//				}
//			}
		// System.out.println("next try...");
		// List<String> list = Reflect
		//.getClassesFromClassPath("org.sbml.squeezer.kinetics.BasicKineticLaw")
		// ;
		// for (String string : list) {
		// System.out.println(string);
		// }
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
		if (e.getSource().equals(jRadioButtonGMAK)) {
			if (jRadioButtonGMAK.isSelected())
				settings.put(CfgKeys.KINETICS_NONE_ENZYME_REACTIONS,
						Kinetics.GENERALIZED_MASS_ACTION);
		} else if (e.getSource().equals(jRadioButtonUniUniMMK)) {
			if (jRadioButtonUniUniMMK.isSelected())
				settings.put(CfgKeys.KINETICS_UNI_UNI_TYPE,
						Kinetics.MICHAELIS_MENTEN);
		} else if (e.getSource().equals(jRadioButtonUniUniCONV)) {
			if (jRadioButtonUniUniCONV.isSelected())
				settings.put(CfgKeys.KINETICS_UNI_UNI_TYPE,
						Kinetics.CONVENIENCE_KINETICS);
		} else if (e.getSource().equals(jRadioButtonBiUniRND)) {
			if (jRadioButtonBiUniRND.isSelected())
				settings.put(CfgKeys.KINETICS_BI_UNI_TYPE,
						Kinetics.RANDOM_ORDER_MECHANISM);
		} else if (e.getSource().equals(jRadioButtonBiUniCONV)) {
			if (jRadioButtonBiUniCONV.isSelected())
				settings.put(CfgKeys.KINETICS_BI_UNI_TYPE,
						Kinetics.CONVENIENCE_KINETICS);
		} else if (e.getSource().equals(jRadioButtonBiUniORD)) {
			if (jRadioButtonBiUniORD.isSelected())
				settings.put(CfgKeys.KINETICS_BI_UNI_TYPE,
						Kinetics.ORDERED_MECHANISM);
		} else if (e.getSource().equals(jRadioButtonBiBiRND)) {
			if (jRadioButtonBiBiRND.isSelected())
				settings.put(CfgKeys.KINETICS_BI_BI_TYPE,
						Kinetics.RANDOM_ORDER_MECHANISM);
		} else if (e.getSource().equals(jRadioButtonBiBiCONV)) {
			if (jRadioButtonBiBiCONV.isSelected())
				settings.put(CfgKeys.KINETICS_BI_BI_TYPE,
						Kinetics.CONVENIENCE_KINETICS);
		} else if (e.getSource().equals(jRadioButtonBiBiORD)) {
			if (jRadioButtonBiBiORD.isSelected())
				settings.put(CfgKeys.KINETICS_BI_BI_TYPE,
						Kinetics.ORDERED_MECHANISM);
		} else if (e.getSource().equals(jRadioButtonBiBiPP)) {
			if (jRadioButtonBiBiPP.isSelected())
				settings.put(CfgKeys.KINETICS_BI_BI_TYPE,
						Kinetics.PING_PONG_MECAHNISM);
		} else if (e.getSource().equals(jRadioButtonOtherEnzymCONV)) {
			if (jRadioButtonOtherEnzymCONV.isSelected())
				settings.put(CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS,
						Kinetics.CONVENIENCE_KINETICS);
		} else if (e.getSource().equals(hillKineticsRadioButton)) {
			if (hillKineticsRadioButton.isSelected())
				settings.put(CfgKeys.KINETICS_GENE_REGULATION,
						Kinetics.HILL_EQUATION);
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
