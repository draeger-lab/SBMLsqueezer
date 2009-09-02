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

import org.sbml.EventAssignment;
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
	private LayoutHelper lh;
	private boolean editable;
	private int row;

	/**
	 * 
	 * @param sbase
	 */
	public SBasePanel(SBase sbase) {
		super();
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		lh = new LayoutHelper(this, gbl);
		editable = false;
		row = -1;
		String className = sbase.getClass().getName();
		className = className.substring(className.lastIndexOf('.') + 1);
		setBorder(BorderFactory.createTitledBorder(" " + className + " "));
		if (sbase instanceof NamedSBase)
			addProperties((NamedSBase) sbase);
		addProperties(sbase);
		if (sbase instanceof ListOf)
			addProperties((ListOf) sbase);
		else if (sbase instanceof Model)
			addProperties((Model) sbase);
		else if (sbase instanceof SimpleSpeciesReference)
			addProperties((SimpleSpeciesReference) sbase);
		else if (sbase instanceof Parameter)
			addProperties((Parameter) sbase);
		else if (sbase instanceof Reaction)
			addProperties((Reaction) sbase);
		else if (sbase instanceof MathContainer)
			addProperties((MathContainer) sbase);
		else if (sbase instanceof Species)
			addProperties((Species) sbase);
	}

	/**
	 * 
	 * @return
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * 
	 * @param editable
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * 
	 * @param sbase
	 */
	private void addProperties(ListOf<? extends SBase> sbase) {
		// TODO
	}

	/**
	 * 
	 * @param mc
	 */
	private void addProperties(MathContainer mc) {
		if (mc.isSetMath()) {
			StringBuffer laTeXpreview = new StringBuffer();
			laTeXpreview.append(LaTeX.eqBegin);
			if (mc instanceof KineticLaw) {
				KineticLaw k = (KineticLaw) mc;
				laTeXpreview.append("v_");
				laTeXpreview
						.append(LaTeX.mbox(k.getParentSBMLObject().getId()));
				laTeXpreview.append('=');
			} else if (mc instanceof FunctionDefinition) {
				FunctionDefinition f = (FunctionDefinition) mc;
				laTeXpreview.append(LaTeX.mbox(f.getId()));
			} else if (mc instanceof EventAssignment) {
				EventAssignment ea = (EventAssignment) mc;
				laTeXpreview.append(LaTeX.mbox(ea.getVariable()));
				laTeXpreview.append('=');
			}
			laTeXpreview.append(mc.getMath().toLaTeX().toString().replace(
					"mathrm", "mbox").replace("text", "mbox").replace("mathtt",
					"mbox"));
			laTeXpreview.append(LaTeX.eqEnd);
			JPanel preview = new JPanel(new BorderLayout());
			preview.add(new sHotEqn(laTeXpreview.toString()),
					BorderLayout.CENTER);
			preview.setBackground(Color.WHITE);
			preview.setBorder(BorderFactory.createLoweredBevelBorder());
			lh.add(preview, 0, ++row, 2, 1, 1, 1);
		}
	}

	/**
	 * 
	 * @param m
	 */
	private void addProperties(Model m) {
		lh.add(new JLabel("Compartments: "), 0, ++row, 1, 1, 1, 1);
		lh.add(new JLabel(Integer.toString(m.getNumCompartments())), 1, row, 1,
				1, 1, 1);
		lh.add(new JLabel("Species: "), 0, ++row, 1, 1, 1, 1);
		lh.add(new JLabel(Integer.toString(m.getNumSpecies())), 1, row, 1, 1,
				1, 1);
		lh.add(new JLabel("Parameters: "), 0, ++row, 1, 1, 1, 1);
		lh.add(new JLabel(Integer.toString(m.getNumParameters())), 1, row, 1,
				1, 1, 1);
		lh.add(new JLabel("Reactions: "), 0, ++row, 1, 1, 1, 1);
		lh.add(new JLabel(Integer.toString(m.getNumReactions())), 1, row, 1, 1,
				1, 1);
		lh.add(new JLabel("Events: "), 0, ++row, 1, 1, 1, 1);
		lh.add(new JLabel(Integer.toString(m.getNumEvents())), 1, row, 1, 1, 1,
				1);
		// TODO
	}

	/**
	 * 
	 * @param sbase
	 */
	private void addProperties(ModifierSpeciesReference msr) {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * @param nsb
	 */
	private void addProperties(NamedSBase nsb) {
		lh.add(new JLabel("Identifier"), 0, ++row, 1, 1, 1, 1);
		JTextField tf = new JTextField(nsb.getId());
		tf.setEditable(editable);
		lh.add(tf, 1, row, 1, 1, 1, 1);
		lh.add(new JLabel("Name"), 0, ++row, 1, 1, 1, 1);
		tf = new JTextField(nsb.getName());
		tf.setEditable(editable);
		lh.add(tf, 1, row, 1, 1, 1, 1);
	}

	/**
	 * 
	 * @param sbase
	 */
	private void addProperties(Parameter p) {
		lh.add(new JLabel("Value"), 0, ++row, 1, 1, 1, 1);
		double value = p.isSetValue() ? p.getValue() : 0;
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(value,
				value - 1000d, value + 1000d, 1d));
		spinner.setEnabled(editable);
		lh.add(spinner, 1, row, 1, 1, 1, 1);
		JCheckBox check = new JCheckBox("Constant", p.getConstant());
		check.setEnabled(editable);
		lh.add(check, 0, ++row, 2, 1, 1, 1);
	}

	/**
	 * 
	 * @param sbase
	 */
	private void addProperties(Reaction reaction) {
		JCheckBox check = new JCheckBox("Reversible", reaction.getReversible());
		check.setEnabled(editable);
		lh.add(check, 0, ++row, 2, 1, 1, 1);
		check = new JCheckBox("Fast", reaction.getFast());
		check.setEnabled(editable);
		lh.add(check, 0, ++row, 2, 1, 1, 1);

		// Create Table of reactants, modifiers and products
		String rmp[][] = new String[Math.max(reaction.getNumReactants(), Math
				.max(reaction.getNumModifiers(), reaction.getNumProducts()))][3];
		String colNames[] = new String[] { "Reactants", "Modifiers", "Products" };
		int count = 0;
		for (SpeciesReference specRef : reaction.getListOfReactants())
			rmp[count++][0] = specRef.getSpeciesInstance().toString();
		count = 0;
		for (ModifierSpeciesReference mSpecRef : reaction.getListOfModifiers())
			rmp[count++][1] = mSpecRef.getSpeciesInstance().toString();
		count = 0;
		for (SpeciesReference specRef : reaction.getListOfProducts())
			rmp[count++][2] = specRef.getSpeciesInstance().toString();
		JTable table = new JTable(rmp, colNames);
		table.setPreferredScrollableViewportSize(new Dimension(200, table
				.getRowCount()
				* table.getRowHeight()));
		table.setEnabled(editable);
		lh.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), 0, ++row, 2, 1, 1,
				1);
		JPanel rEqPanel = new JPanel(new GridLayout(1, 1));
		sHotEqn rEqn = new sHotEqn(LaTeXExport.reactionEquation(reaction));
		rEqn.setBorder(BorderFactory.createLoweredBevelBorder());
		rEqPanel.add(rEqn);
		rEqPanel.setBorder(BorderFactory
				.createTitledBorder(" Reaction equation "));
		lh.add(rEqPanel, 0, ++row, 2, 1, 1, 1);
		if (reaction.isSetKineticLaw())
			lh.add(new SBasePanel(reaction.getKineticLaw()), 0, ++row, 2, 1, 1,
					1);
	}

	/**
	 * 
	 * @param sbase
	 */
	private void addProperties(SBase sbase) {
		lh.add(new JLabel("Meta identifier"), 0, ++row, 1, 1, 1, 1);
		JTextField tf = new JTextField(sbase.getMetaId());
		tf.setEditable(editable);
		lh.add(tf, 1, row, 1, 1, 1, 1);
		lh.add(new JLabel("Notes"), 0, ++row, 1, 1, 1, 1);
		JEditorPane notesArea = new JEditorPane("text/html", "");
		notesArea.setEditable(editable);
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
		lh.add(scroll, 1, row, 1, 1, 1, 1);
		lh.add(new JLabel("SBO term"), 0, ++row, 1, 1, 1, 1);
		JTextField sboTermField = new JTextField();
		sboTermField.setEditable(editable);
		if (sbase.isSetSBOTerm()) {
			sboTermField.setText(SBO.getTerm(sbase.getSBOTerm())
					.getDescription());
			sboTermField.setColumns(sboTermField.getText().length());
		}
		lh.add(sboTermField, 1, row, 1, 1, 1, 1);
	}

	/**
	 * 
	 * @param ssr
	 */
	private void addProperties(SimpleSpeciesReference ssr) {
		lh.add(new JLabel("Species"), 1, row, 1, 1, 1, 1);
		lh.add(new JLabel(ssr.getSpeciesInstance().toString()), 1, row, 1, 1,
				1, 1);
		if (ssr instanceof SpeciesReference)
			addProperties((SpeciesReference) ssr);
		else if (ssr instanceof ModifierSpeciesReference)
			addProperties((ModifierSpeciesReference) ssr);
		lh.add(new SBasePanel(ssr.getSpeciesInstance()), 0, ++row, 2, 1, 1, 1);
	}

	/**
	 * 
	 * @param sbase
	 */
	private void addProperties(Species species) {
		JSpinner spinInitial;
		if (species.isSetInitialAmount()) {
			spinInitial = new JSpinner(new SpinnerNumberModel(species
					.getInitialAmount(), 0d, Math.max(species
					.getInitialAmount(), 1000), .1d));
			lh.add(new JLabel("Initial amount"), 0, ++row, 1, 1, 1, 1);
		} else {
			if (species.isSetInitialConcentration())
				spinInitial = new JSpinner(new SpinnerNumberModel(species
						.getInitialConcentration(), 0d, Math.max(species
						.getInitialConcentration(), 1000), .1d));
			else
				spinInitial = new JSpinner(new SpinnerNumberModel(0, 0d, 1000,
						.1d));
			lh.add(new JLabel("Initial amount"), 0, ++row, 1, 1, 1, 1);
		}
		spinInitial.setEnabled(editable);
		lh.add(spinInitial, 1, row, 1, 1, 0, 1);
		JSpinner spinCharge = new JSpinner(new SpinnerNumberModel(species
				.getCharge(), -10, 10, 1));
		lh.add(new JLabel("Charge"), 0, ++row, 1, 1, 1, 1);
		spinCharge.setEnabled(editable);
		lh.add(spinCharge, 1, row, 1, 1, 1, 1);
		JCheckBox check = new JCheckBox("Boundary condition", species
				.getBoundaryCondition());
		check.setEnabled(editable);
		lh.add(check, 0, ++row, 2, 1, 1, 1);
		check = new JCheckBox("Constant", species.getConstant());
		check.setEnabled(editable);
		lh.add(check, 0, ++row, 2, 1, 1, 1);
		check = new JCheckBox("Has only substance units", species
				.getHasOnlySubstanceUnits());
		check.setEnabled(editable);
		lh.add(check, 0, ++row, 2, 1, 1, 1);
	}

	/**
	 * 
	 * @param sbase
	 */
	private void addProperties(SpeciesReference specRef) {
		if (specRef.isSetStoichiometryMath()) {
			StoichiometryMath sMath = specRef.getStoichiometryMath();
			JPanel p = new JPanel(new GridLayout(1, 1));
			p.setBorder(BorderFactory.createTitledBorder(" "
					+ sMath.getClass().getCanonicalName() + ' '));
			sHotEqn eqn = new sHotEqn(sMath.getMath().toLaTeX().toString());
			eqn.setBorder(BorderFactory.createLoweredBevelBorder());
			p.add(eqn);
			lh.add(p, 1, ++row, 1, 1, 1, 1);
		} else {
			lh.add(new JLabel("Stoichiometry"), 0, ++row, 1, 1, 1, 1);
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(specRef
					.getStoichiometry(), specRef.getStoichiometry() - 1000,
					specRef.getStoichiometry() + 1000, .1d));
			spinner.setEnabled(editable);
			lh.add(spinner, 1, row, 1, 1, 1, 1);
		}
	}
}
