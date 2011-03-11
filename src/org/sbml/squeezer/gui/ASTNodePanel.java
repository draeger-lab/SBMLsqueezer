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

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.compilers.LaTeXCompiler;

import atp.sHotEqn;
import de.zbit.gui.LayoutHelper;
import de.zbit.util.StringUtil;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-05-11
 */
public class ASTNodePanel extends JPanel {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -7683674821264614573L;

	/**
	 * 
	 * @param node
	 */
	public ASTNodePanel(ASTNode node) {
		super();

		LayoutHelper lh = new LayoutHelper(this);
		lh.add(new JPanel(), 0, 0, 1, 1, 0, 0);
		lh.add(createPanel(node), 1, 0, 1, 1, 0, 0);
		lh.add(new JPanel(), 2, 0, 1, 1, 0, 0);

		setBorder(BorderFactory.createTitledBorder(String.format(" %s %s ",
				node.getClass().getSimpleName(), node.toString())));
	}

	/**
	 * @param node
	 * @param settings
	 * @return
	 */
	private Component createPanel(ASTNode node) {
		LayoutHelper lh = new LayoutHelper(new JPanel());
		boolean enabled = false;
		JSpinner spinner;
		JTextField tf;
		String name;

		name = node.getParent() == null ? "undefined" : node.getParent()
				.toString();
		tf = new JTextField(name);
		tf.setEditable(enabled);
		lh.add("Parent node", tf, true);

		if (node.getParentSBMLObject() == null) {
			name = "undefined";
		} else {
			MathContainer parent = node.getParentSBMLObject();
			name = parent.getClass().getSimpleName();
			name += " " + node.getParentSBMLObject().toString();
		}
		JEditorPane editor = new JEditorPane("text/html", StringUtil.toHTML(name, 60));
		editor.setEditable(enabled);
		editor.setBorder(BorderFactory.createLoweredBevelBorder());
		lh.add("Parent SBML object", editor, true);

		tf = new JTextField(Integer.toString(node.getNumChildren()));
		tf.setEditable(false);
		lh.add("Number of children", tf, true);

		JTextArea area = new JTextArea();
		area.setColumns(60);
		try {
			area.setText(node.toFormula());
		} catch (SBMLException e) {
			area.setText("invalid");
		}
		area.setEditable(false);
		area.setBorder(BorderFactory.createLoweredBevelBorder());
		lh.add("Formula", area, true);

		JComboBox opt = new JComboBox();
		for (ASTNode.Type t : ASTNode.Type.values()) {
			opt.addItem(t);
			if (t.equals(node.getType())) {
				opt.setSelectedItem(t);
			}
		}
		opt.setEditable(enabled);
		opt.setEnabled(enabled);
		lh.add("Type", opt, true);

		if (node.isRational()) {
			spinner = new JSpinner(new SpinnerNumberModel(node.getNumerator(),
					-1E10, 1E10, 1));
			spinner.setEnabled(enabled);
			lh.add("Numerator", spinner, true);
			spinner = new JSpinner(new SpinnerNumberModel(
					node.getDenominator(), -1E10, 1E10, 1));
			spinner.setEnabled(enabled);
			lh.add("Denominator", spinner, true);
		}

		if (node.isReal()) {
			spinner = new JSpinner(new SpinnerNumberModel(node.getMantissa(),
					-1E10, 1E10, 1));
			spinner.setEnabled(enabled);
			lh.add("Mantissa", spinner, true);
			spinner = new JSpinner(new SpinnerNumberModel(node.getExponent(),
					-1E10, 1E10, 1));
			spinner.setEnabled(enabled);
			lh.add("Exponent", spinner, true);
		}

		if (node.isString()) {
			tf = new JTextField(node.getName());
			tf.setEditable(enabled);
			lh.add("Name", tf, true);
			if (node.getVariable() != null) {
				lh.add(new SBasePanel(node.getVariable()), 0, lh
						.getRow() + 1, 3, 1, 0, 0);
				lh.add(new JPanel(), 0, lh.getRow() + 1, 3, 1, 0, 0);
			}
		}

		/*
		 * Units
		 */
		JPanel unitPanel = new JPanel();
		LayoutHelper l = new LayoutHelper(unitPanel);
		JCheckBox chck = new JCheckBox("Contains undeclared units", node
				.containsUndeclaredUnits());
		chck.setEnabled(enabled);
		l.add(chck, 0, 0, 3, 1);
		UnitDefinition ud;
		try {
			ud = node.deriveUnit();
		} catch (SBMLException e) {
			ud = new UnitDefinition();
		}
		JEditorPane unitPane = GUITools.unitPreview(ud != null ? ud
				: new UnitDefinition());
		unitPane.setBorder(BorderFactory.createLoweredBevelBorder());
		l.add(new JPanel(), 0, 1, 3, 1);
		l.add("Derived unit:", unitPane, true);
		unitPanel
				.setBorder(BorderFactory.createTitledBorder(" Derived units "));
		lh.add(unitPanel, 0, lh.getRow() + 1, 3, 1, 0, 0);
		lh.add(new JPanel(), 0, lh.getRow() + 1, 3, 1, 0, 0);

		StringBuilder latex = new StringBuilder();
		latex.append(LaTeXCompiler.eqBegin);
		String ltx;
		try {
			ltx = node.compile(new LaTeXCompiler()).toString().replace("mathrm",
			"mbox").replace("text", "mbox").replace("mathtt", "mbox");
		} catch (SBMLException e) {
			ltx = "invalid";
		}
		latex.append(ltx);
		latex.append(LaTeXCompiler.eqEnd);
		sHotEqn preview = new sHotEqn(latex.toString());
		preview.setBorder(BorderFactory.createLoweredBevelBorder());
		JScrollPane scroll = new JScrollPane(preview,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createTitledBorder(" Preview "));
		lh.add(scroll, 0, lh.getRow() + 1, 3, 1, 0, 0);
		lh.add(new JPanel(), 0, lh.getRow() + 1, 3, 1, 0, 0);

		return lh.getContainer();
	}
}
