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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.sbml.FunctionDefinition;
import org.sbml.KineticLaw;
import org.sbml.ListOf;
import org.sbml.MathContainer;
import org.sbml.Model;
import org.sbml.ModifierSpeciesReference;
import org.sbml.NamedSBase;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.SBO;
import org.sbml.SBase;
import org.sbml.SimpleSpeciesReference;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.StoichiometryMath;
import org.sbml.squeezer.io.LaTeX;
import org.sbml.squeezer.io.LaTeXExport;

import atp.sHotEqn;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBasePanel extends JPanel {

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -4969096536922920641L;

	public SBasePanel(SBase sbase) {
		super();
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		int row = -1;
		String className = sbase.getClass().getName();
		className = className.substring(className.lastIndexOf('.') + 1);
		setBorder(BorderFactory.createTitledBorder(" " + className + " "));
		if (sbase instanceof NamedSBase) {
			NamedSBase nsb = (NamedSBase) sbase;
			LayoutHelper.addComponent(this, gbl, new JLabel("Identifier"), 0,
					++row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JTextField(nsb.getId()),
					1, row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JLabel("Name"), 0, ++row,
					1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JTextField(nsb.getName()),
					1, row, 1, 1, 1, 1);
		}
		LayoutHelper.addComponent(this, gbl, new JLabel("Meta identifier"), 0,
				++row, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, gbl, new JTextField(sbase.getMetaId()),
				1, row, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, gbl, new JLabel("Notes"), 0, ++row, 1,
				1, 1, 1);
		JEditorPane notesArea = new JEditorPane("text/html", "");
		notesArea.setEditable(false);
		if (sbase.isSetNotes()) {
			String text = sbase.getNotesString();
			if (text.startsWith("<notes") && text.endsWith("</notes>"))
				text = text.substring(8, sbase.getNotesString().length() - 9);
			text = text.trim();
			if (!text.startsWith("<body") && !text.endsWith("</body>"))
				text = "<body>" + text + "</body>";
			text = "<html><head></head>" + text + "</html>";
			notesArea.setText(text);
		}
		notesArea.addHyperlinkListener(new SystemBrowser());
		notesArea.setPreferredSize(new Dimension(350, 200));
		notesArea.setBorder(BorderFactory.createLoweredBevelBorder());
		JScrollPane scroll = new JScrollPane(notesArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		LayoutHelper.addComponent(this, gbl, scroll, 1, row, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, gbl, new JLabel("SBO term"), 0, ++row,
				1, 1, 1, 1);
		JTextField sboTermField = new JTextField();
		if (sbase.isSetSBOTerm()) {
			sboTermField.setText(SBO.getTerm(sbase.getSBOTerm())
					.getDescription());
			sboTermField.setColumns(sboTermField.getText().length());
		}
		LayoutHelper.addComponent(this, gbl, sboTermField, 1, row, 1, 1, 1, 1);
		if (sbase instanceof ListOf) {
			// TODO
		} else if (sbase instanceof Model) {
			Model m = (Model) sbase;
			LayoutHelper.addComponent(this, gbl, new JLabel("Compartments: "),
					0, ++row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JLabel(Integer.toString(m
					.getNumCompartments())), 1, row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JLabel("Species: "), 0,
					++row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JLabel(Integer.toString(m
					.getNumSpecies())), 1, row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JLabel("Parameters: "), 0,
					++row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JLabel(Integer.toString(m
					.getNumParameters())), 1, row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JLabel("Reactions: "), 0,
					++row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JLabel(Integer.toString(m
					.getNumReactions())), 1, row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JLabel("Events: "), 0,
					++row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JLabel(Integer.toString(m
					.getNumEvents())), 1, row, 1, 1, 1, 1);
			// TODO
		} else if (sbase instanceof SimpleSpeciesReference) {
			SimpleSpeciesReference ssr = (SimpleSpeciesReference) sbase;
			LayoutHelper.addComponent(this, gbl, new JLabel("Species"), 1, row,
					1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JLabel(ssr
					.getSpeciesInstance().toString()), 1, row, 1, 1, 1, 1);
			if (sbase instanceof SpeciesReference) {
				SpeciesReference specRef = (SpeciesReference) sbase;
				if (specRef.isSetStoichiometryMath()) {
					StoichiometryMath sMath = specRef.getStoichiometryMath();
					JPanel p = new JPanel(new GridLayout(1, 1));
					p.setBorder(BorderFactory.createTitledBorder(" "
							+ sMath.getClass().getCanonicalName() + ' '));
					sHotEqn eqn = new sHotEqn(sMath.getMath().toLaTeX()
							.toString());
					eqn.setBorder(BorderFactory.createLoweredBevelBorder());
					p.add(eqn);
					LayoutHelper.addComponent(this, gbl, p, 1, ++row, 1, 1, 1,
							1);
				} else {
					LayoutHelper.addComponent(this, gbl, new JLabel(
							"Stoichiometry"), 0, ++row, 1, 1, 1, 1);
					LayoutHelper.addComponent(this, gbl, new JSpinner(
							new SpinnerNumberModel(specRef.getStoichiometry(),
									specRef.getStoichiometry() - 1000, specRef
											.getStoichiometry() + 1000, .1d)),
							1, row, 1, 1, 1, 1);
				}
			} else if (sbase instanceof ModifierSpeciesReference) {
				// TODO
				ModifierSpeciesReference msr = (ModifierSpeciesReference) sbase;
			}
			LayoutHelper.addComponent(this, gbl, new SBasePanel(ssr
					.getSpeciesInstance()), 0, ++row, 2, 1, 1, 1);
		} else if (sbase instanceof Parameter) {
			Parameter p = (Parameter) sbase;
			LayoutHelper.addComponent(this, gbl, new JLabel("Value"), 0, ++row,
					1, 1, 1, 1);
			double value = p.isSetValue() ? p.getValue() : 0;
			LayoutHelper.addComponent(this, gbl, new JSpinner(
					new SpinnerNumberModel(value, value - 1000d, value + 1000d,
							1d)), 1, row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JCheckBox("Constant", p
					.getConstant()), 0, ++row, 2, 1, 1, 1);
		} else if (sbase instanceof Reaction) {
			Reaction reaction = (Reaction) sbase;
			LayoutHelper.addComponent(this, gbl, new JCheckBox("Reversible",
					reaction.getReversible()), 0, ++row, 2, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JCheckBox("Fast", reaction
					.getFast()), 0, ++row, 2, 1, 1, 1);

			// Create Table of reactants, modifiers and products
			String rmp[][] = new String[Math.max(reaction.getNumReactants(),
					Math.max(reaction.getNumModifiers(), reaction
							.getNumProducts()))][3];
			String colNames[] = new String[] { "Reactants", "Modifiers",
					"Products" };
			int count = 0;
			for (SpeciesReference specRef : reaction.getListOfReactants())
				rmp[count++][0] = specRef.getSpeciesInstance().toString();
			count = 0;
			for (ModifierSpeciesReference mSpecRef : reaction
					.getListOfModifiers())
				rmp[count++][1] = mSpecRef.getSpeciesInstance().toString();
			count = 0;
			for (SpeciesReference specRef : reaction.getListOfProducts())
				rmp[count++][2] = specRef.getSpeciesInstance().toString();
			JTable table = new JTable(rmp, colNames);
			table.setPreferredScrollableViewportSize(new Dimension(200, table
					.getRowCount()
					* table.getRowHeight()));
			LayoutHelper.addComponent(this, gbl, new JScrollPane(table,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), 0, ++row, 2,
					1, 1, 1);

			JPanel rEqPanel = new JPanel(new GridLayout(1, 1));
			sHotEqn rEqn = new sHotEqn(LaTeXExport.reactionEquation(reaction));
			rEqn.setBorder(BorderFactory.createLoweredBevelBorder());
			rEqPanel.add(rEqn);
			rEqPanel.setBorder(BorderFactory
					.createTitledBorder(" Reaction equation "));
			LayoutHelper
					.addComponent(this, gbl, rEqPanel, 0, ++row, 2, 1, 1, 1);
			if (reaction.isSetKineticLaw())
				LayoutHelper.addComponent(this, gbl, new SBasePanel(reaction
						.getKineticLaw()), 0, ++row, 2, 1, 1, 1);
		} else if (sbase instanceof MathContainer) {
			MathContainer mc = (MathContainer) sbase;
			if (mc.isSetMath()) {
				StringBuffer laTeXpreview = new StringBuffer();
				laTeXpreview.append(LaTeX.eqBegin);
				if (mc instanceof KineticLaw) {
					KineticLaw k = (KineticLaw) mc;
					laTeXpreview.append("v_");
					laTeXpreview.append(LaTeX.mbox(k.getParentSBMLObject().getId()));
					laTeXpreview.append('=');
				} else if (mc instanceof FunctionDefinition) {
					FunctionDefinition f = (FunctionDefinition) mc;
					laTeXpreview.append(LaTeX.mbox(f.getId()));
				}
				laTeXpreview.append(mc.getMath().toLaTeX().toString().replace(
						"mathrm", "mbox").replace("text", "mbox").replace(
						"mathtt", "mbox"));
				laTeXpreview.append(LaTeX.eqEnd);
				JPanel preview = new JPanel(new BorderLayout());
				preview.add(new sHotEqn(laTeXpreview.toString()),
						BorderLayout.CENTER);
				preview.setBackground(Color.WHITE);
				preview.setBorder(BorderFactory.createLoweredBevelBorder());
				LayoutHelper.addComponent(this, gbl, preview, 0, ++row, 2, 1,
						1, 1);
			}
		} else if (sbase instanceof Species) {
			Species species = (Species) sbase;
			JSpinner spinInitial;
			if (species.isSetInitialAmount()) {
				spinInitial = new JSpinner(new SpinnerNumberModel(species
						.getInitialAmount(), 0d, Math.max(species
						.getInitialAmount(), 1000), .1d));
				LayoutHelper.addComponent(this, gbl, new JLabel(
						"Initial amount"), 0, ++row, 1, 1, 1, 1);
			} else {
				if (species.isSetInitialConcentration())
					spinInitial = new JSpinner(new SpinnerNumberModel(species
							.getInitialConcentration(), 0d, Math.max(species
							.getInitialConcentration(), 1000), .1d));
				else
					spinInitial = new JSpinner(new SpinnerNumberModel(0, 0d,
							1000, .1d));
				LayoutHelper.addComponent(this, gbl, new JLabel(
						"Initial amount"), 0, ++row, 1, 1, 1, 1);
			}
			LayoutHelper.addComponent(this, gbl, spinInitial, 1, row, 1, 1, 0,
					1);
			JSpinner spinCharge = new JSpinner(new SpinnerNumberModel(species
					.getCharge(), -10, 10, 1));
			LayoutHelper.addComponent(this, gbl, new JLabel("Charge"), 0,
					++row, 1, 1, 1, 1);
			LayoutHelper
					.addComponent(this, gbl, spinCharge, 1, row, 1, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JCheckBox(
					"Boundary condition", species.getBoundaryCondition()), 0,
					++row, 2, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JCheckBox("Constant",
					species.getConstant()), 0, ++row, 2, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JCheckBox(
					"Has only substance units", species
							.getHasOnlySubstanceUnits()), 0, ++row, 2, 1, 1, 1);
		}
	}
}
