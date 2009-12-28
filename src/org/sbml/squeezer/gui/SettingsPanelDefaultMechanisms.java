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
import java.util.Arrays;
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
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.io.StringTools;
import org.sbml.squeezer.kinetics.BasicKineticLaw;

/**
 * A {@link JPanel} to let the user select all default instances of
 * {@link BasicKineticLaw} that implement the designated interfaces to be
 * available for certain reaction types.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-22
 * @since 1.3
 */
public class SettingsPanelDefaultMechanisms extends JPanel implements
		ItemListener, ChangeListener {

	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = 243553812503691739L;

	private static final Font titleFont = new Font("Dialog", Font.BOLD, 12);

	private static final Color borderColor = new Color(51, 51, 51);

	private Properties settings;

	private Properties origSett;

	private List<ItemListener> itemListeners;

	/**
	 * Reaction Mechanism Panel
	 * 
	 * @param properties
	 */
	public SettingsPanelDefaultMechanisms(Properties properties) {
		settings = new Properties();
		for (Object key : properties.keySet())
			if (key.toString().startsWith("KINETICS_"))
				settings.put(key, properties.get(key));
		itemListeners = new LinkedList<ItemListener>();
		origSett = properties;
		init(((Boolean) origSett
				.get(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE))
				.booleanValue());
	}

	/**
	 * Initializes the selection of default mechanisms.
	 * 
	 * @param properties
	 */
	private void init(boolean treatReactionsReversible) {
		setLayout(new GridLayout(1, 2));

		JPanel leftMechanismPanel = new JPanel();
		LayoutHelper lh = new LayoutHelper(leftMechanismPanel);

		lh.add(createButtonGroupPanel(ReactionType
				.getKineticsNonEnzyme(treatReactionsReversible),
				CfgKeys.KINETICS_NONE_ENZYME_REACTIONS));
		lh.add(createButtonGroupPanel(ReactionType
				.getKineticsUniUni(treatReactionsReversible),
				CfgKeys.KINETICS_UNI_UNI_TYPE));
		lh.add(createButtonGroupPanel(ReactionType
				.getKineticsBiUni(treatReactionsReversible),
				CfgKeys.KINETICS_BI_UNI_TYPE));

		JPanel rightMechanismPanel = new JPanel();
		lh = new LayoutHelper(rightMechanismPanel);
		lh.add(createButtonGroupPanel(ReactionType
				.getKineticsBiBi(treatReactionsReversible),
				CfgKeys.KINETICS_BI_BI_TYPE));
		lh.add(createButtonGroupPanel(ReactionType
				.getKineticsArbitraryEnzyme(treatReactionsReversible),
				CfgKeys.KINETICS_OTHER_ENZYME_REACTIONS));
		lh.add(createButtonGroupPanel(ReactionType
				.getKineticsGeneRegulation(treatReactionsReversible),
				CfgKeys.KINETICS_GENE_REGULATION));

		add(leftMechanismPanel);
		add(rightMechanismPanel);
		GUITools.setAllBackground(this, Color.WHITE);
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
		if (!oneIsSelected) // no one is selected
			jRButton[pos].setSelected(true);
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
			settings.put(CfgKeys.valueOf(rbutton.getActionCommand()),
					SBMLsqueezer.KINETICS_PACKAGE + '.' + rbutton.getText());
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
