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

import org.sbml.AssignmentRule;
import org.sbml.Compartment;
import org.sbml.Constraint;
import org.sbml.Event;
import org.sbml.EventAssignment;
import org.sbml.FunctionDefinition;
import org.sbml.KineticLaw;
import org.sbml.ListOf;
import org.sbml.MathContainer;
import org.sbml.Model;
import org.sbml.ModifierSpeciesReference;
import org.sbml.NamedSBase;
import org.sbml.Parameter;
import org.sbml.RateRule;
import org.sbml.Reaction;
import org.sbml.SBO;
import org.sbml.SBase;
import org.sbml.SimpleSpeciesReference;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.StoichiometryMath;
import org.sbml.Symbol;
import org.sbml.UnitDefinition;
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
		if (sbase instanceof SimpleSpeciesReference)
			addProperties((SimpleSpeciesReference) sbase);
		if (sbase instanceof MathContainer)
			addProperties((MathContainer) sbase);
		if (sbase instanceof Symbol)
			addProperties((Symbol) sbase);
		if (sbase instanceof ListOf)
			addProperties((ListOf) sbase);
		if (sbase instanceof Model)
			addProperties((Model) sbase);
		if (sbase instanceof UnitDefinition)
			addProperties((UnitDefinition) sbase);
		if (sbase instanceof Compartment)
			addProperties((Compartment) sbase);
		if (sbase instanceof Species)
			addProperties((Species) sbase);
		if (sbase instanceof Parameter)
			addProperties((Parameter) sbase);
		if (sbase instanceof Constraint)
			addProperties((Constraint) sbase);
		if (sbase instanceof Reaction)
			addProperties((Reaction) sbase);
		if (sbase instanceof Event)
			addProperties((Event) sbase);
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
	 * @param c
	 */
	private void addProperties(Compartment c) {
		if (c.isSetCompartmentType() || editable) {
			lh.add(new JLabel("Compartment type: "), 0, ++row, 1, 1, 1, 1);
			JTextField tf = new JTextField(c.getCompartmentTypeInstance()
					.toString());
			tf.setEditable(editable);
			lh.add(tf, 1, ++row, 1, 1, 1, 1);
		}
		if (c.isSetOutside() || editable) {
			lh.add(new JLabel("Outside: "), 0, ++row, 1, 1, 1, 1);
			JTextField tf = new JTextField(c.getOutsideInstance().toString());
			tf.setEditable(editable);
			lh.add(tf, 1, row, 1, 1, 1, 1);
		}
		lh.add(new JLabel("Spatial dimensions: "), 0, ++row, 1, 1, 1, 1);
		JSpinner spinner = new JSpinner(new SpinnerNumberModel(c
				.getSpatialDimensions(), 0, 3, 1));
		spinner.setEnabled(editable);
		lh.add(spinner, 1, row, 1, 1, 1, 1);
	}

	/**
	 * 
	 * @param c
	 */
	private void addProperties(Constraint c) {
		if (c.isSetMessage() || editable) {
			lh.add(new JLabel("Message: "), 0, ++row, 1, 1, 1, 1);
			JTextField tf = new JTextField(c.getMessage());
			tf.setEditable(editable);
			lh.add(tf, 1, ++row, 1, 1, 1, 1);
		}
	}

	/**
	 * 
	 * @param e
	 */
	private void addProperties(Event e) {
		JCheckBox check = new JCheckBox("Uses values from trigger time", e
				.getUseValuesFromTriggerTime());
		check.setEnabled(editable);
		lh.add(check, 0, ++row, 2, 1, 1, 1);
		if (e.isSetTrigger())
			lh.add(new SBasePanel(e.getTrigger()), 0, ++row, 2, 1, 1, 1);
		if (e.isSetDelay())
			lh.add(new SBasePanel(e.getDelay()), 0, ++row, 2, 1, 1, 1);
		if (e.isSetTimeUnits())
			lh.add(new SBasePanel(e.getTimeUnitsInstance()), 0, ++row, 2, 1, 1,
					1);
		for (EventAssignment ea : e.getListOfEventAssignments())
			lh.add(new SBasePanel(ea), 0, ++row, 2, 1, 1, 1);
	}

	/**
	 * 
	 * @param list
	 */
	private void addProperties(ListOf<? extends SBase> list) {
		// TODO
		list.size();
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
			} else if (mc instanceof AssignmentRule) {
				AssignmentRule ar = (AssignmentRule) mc;
				laTeXpreview.append(LaTeX.mbox(ar.getVariable()));
				laTeXpreview.append('=');
			} else if (mc instanceof RateRule) {
				RateRule rr = (RateRule) mc;
				String d = LaTeX.mbox("d").toString();
				laTeXpreview.append(LaTeX.frac(LaTeX.times(d, LaTeX.mbox(rr
						.getVariable())), d));
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
		String columnNames[] = new String[] { "Element", "Quantity" };
		String rowData[][] = new String[][] {
				{ "Function definitions",
						Integer.toString(m.getNumFunctionDefinitions()) },
				{ "Unit definitions",
						Integer.toString(m.getNumUnitDefinitions()) },
				{ "Compartment types",
						Integer.toString(m.getNumCompartmentTypes()) },
				{ "Species types", Integer.toString(m.getNumSpeciesTypes()) },
				{ "Compartments", Integer.toString(m.getNumCompartments()) },
				{ "Species", Integer.toString(m.getNumSpecies()) },
				{ "Global parameters", Integer.toString(m.getNumParameters()) },
				{ "Local parameters",
						Integer.toString(m.getNumLocalParameters()) },
				{ "Initial assignments",
						Integer.toString(m.getNumInitialAssignments()) },
				{ "Rules", Integer.toString(m.getNumRules()) },
				{ "Constraints", Integer.toString(m.getNumConstraints()) },
				{ "Reactions", Integer.toString(m.getNumReactions()) },
				{ "Events", Integer.toString(m.getNumEvents()) } };
		JTable table = new JTable(rowData, columnNames);
		table.setEnabled(editable);
		table.setPreferredScrollableViewportSize(new Dimension(200, table
				.getRowCount()
				* table.getRowHeight()));
		lh.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), 0, ++row, 2, 1, 1,
				1);
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
		if (nsb.isSetId() || editable) {
			lh.add(new JLabel("Identifier: "), 0, ++row, 1, 1, 1, 1);
			JTextField tf = new JTextField(nsb.getId());
			tf.setEditable(editable);
			lh.add(tf, 1, row, 1, 1, 1, 1);
		}
		if (nsb.isSetName() || editable) {
			lh.add(new JLabel("Name: "), 0, ++row, 1, 1, 1, 1);
			JTextField tf = new JTextField(nsb.getName());
			tf.setEditable(editable);
			lh.add(tf, 1, row, 1, 1, 1, 1);
		}
	}

	/**
	 * 
	 * @param sbase
	 */
	private void addProperties(Parameter p) {
		// TODO
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
		if (sbase.isSetMetaId() || editable) {
			lh.add(new JLabel("Meta identifier: "), 0, ++row, 1, 1, 1, 1);
			JTextField tf = new JTextField(sbase.getMetaId());
			tf.setEditable(editable);
			lh.add(tf, 1, row, 1, 1, 1, 1);
		}
		if (sbase.isSetNotes() || editable) {
			lh.add(new JLabel("Notes: "), 0, ++row, 1, 1, 1, 1);
			JEditorPane notesArea = new JEditorPane("text/html", "");
			notesArea.setEditable(editable);
			if (sbase.isSetNotes()) {
				String text = sbase.getNotesString();
				if (text.startsWith("<notes") && text.endsWith("</notes>"))
					text = text.substring(8,
							sbase.getNotesString().length() - 9);
				text = text.trim();
				if (!text.startsWith("<body") && !text.endsWith("</body>"))
					text = "<body>" + text + "</body>";
				text = "<html><head></head>" + text + "</html>";
				notesArea.setText(text);
			}
			notesArea.addHyperlinkListener(new SystemBrowser());
			notesArea.setBorder(BorderFactory.createLoweredBevelBorder());
			JScrollPane scroll = new JScrollPane(notesArea,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			notesArea.setPreferredSize(new Dimension(350, 200));
			lh.add(scroll, 1, row, 1, 1, 1, 1);
		}
		lh.add(new JLabel("SBO term: "), 0, ++row, 1, 1, 1, 1);
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
		if (ssr.isSetSpecies()) {
			lh.add(new JLabel("Species"), 1, row, 1, 1, 1, 1);
			lh.add(new JLabel(ssr.getSpeciesInstance().toString()), 1, row, 1,
					1, 1, 1);
		}
		if (ssr instanceof SpeciesReference)
			addProperties((SpeciesReference) ssr);
		else if (ssr instanceof ModifierSpeciesReference)
			addProperties((ModifierSpeciesReference) ssr);
		if (ssr.isSetSpecies())
			lh.add(new SBasePanel(ssr.getSpeciesInstance()), 0, ++row, 2, 1, 1,
					1);
	}

	/**
	 * 
	 * @param sbase
	 */
	private void addProperties(Species species) {
		JSpinner spinCharge = new JSpinner(new SpinnerNumberModel(species
				.getCharge(), -10, 10, 1));
		lh.add(new JLabel("Charge: "), 0, ++row, 1, 1, 1, 1);
		spinCharge.setEnabled(editable);
		lh.add(spinCharge, 1, row, 1, 1, 1, 1);
		if (species.isSetSpeciesType()) {
			lh.add(new JLabel("Species type: "), 0, ++row, 1, 1, 1, 1);
			JTextField tf = new JTextField(species.getSpeciesTypeInstance()
					.toString());
			tf.setEditable(editable);
			lh.add(tf, 0, ++row, 1, 1, 1, 1);
		}
		lh.add(new JLabel("Compartment: "), 0, ++row, 1, 1, 1, 1);
		JTextField tf = new JTextField(species.getCompartmentInstance()
				.toString());
		tf.setEditable(editable);
		lh.add(tf, 1, row, 1, 1, 1, 1);
		if (species.isSetSpeciesType() || editable) {
			lh.add(new JLabel("Species type: "), 0, row, 1, 1, 1, 1);
			tf = new JTextField(species.getSpeciesTypeInstance().toString());
			tf.setEditable(editable);
			lh.add(tf, 0, row, 1, 1, 1, 1);
		}
		JCheckBox check = new JCheckBox("Boundary condition", species
				.getBoundaryCondition());
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

	/**
	 * 
	 * @param s
	 */
	private void addProperties(Symbol s) {
		String label = null;
		SpinnerNumberModel spinModel = new SpinnerNumberModel(0, 0d, 1000, .1d);
		if (s instanceof Species) {
			Species species = (Species) s;
			label = "Initial amount: ";
			if (species.isSetInitialAmount())
				spinModel = new SpinnerNumberModel(species.getInitialAmount(),
						0d, Math.max(species.getInitialAmount(), 1000), .1d);
			else if (species.isSetInitialConcentration()) {
				spinModel = new SpinnerNumberModel(species
						.getInitialConcentration(), 0d, Math.max(species
						.getInitialConcentration(), 1000), .1d);
				label = "Initial concentration: ";
			}
		} else if (s instanceof Compartment) {
			Compartment c = (Compartment) s;
			if (c.isSetSize())
				spinModel = new SpinnerNumberModel(c.getSize(),
						c.getSize() - 1000, c.getSize() + 1000, .1d);
			label = "Size: ";
		} else {
			Parameter p = (Parameter) s;
			if (p.isSetValue())
				spinModel = new SpinnerNumberModel(p.getValue(),
						p.getValue() - 1000, p.getValue() + 1000, .1d);
			label = "Value: ";
		}
		lh.add(new JLabel(label), 0, ++row, 1, 1, 1, 1);
		JSpinner spinValue = new JSpinner(spinModel);
		spinValue.setEnabled(editable);
		lh.add(spinValue, 1, row, 1, 1, 0, 1);
		lh
				.add(new JLabel(s instanceof Species ? "Substance unit: "
						: "Unit: "), 0, ++row, 1, 1, 1, 1);
		StringBuffer laTeXpreview = new StringBuffer();
		laTeXpreview.append(LaTeX.eqBegin);
		if (s.isSetUnits())
			laTeXpreview.append((new LaTeXExport()).format(s.getUnits())
					.toString().replace("\\up", "\\").replace("mathrm", "mbox")
					.replace("text", "mbox").replace("mathtt", "mbox"));
		laTeXpreview.append(LaTeX.eqEnd);
		JPanel preview = new JPanel(new BorderLayout());
		preview.add(new sHotEqn(laTeXpreview.toString()), BorderLayout.CENTER);
		preview.setBackground(Color.WHITE);
		preview.setBorder(BorderFactory.createLoweredBevelBorder());
		lh.add(preview, 1, row, 1, 1, 1, 1);
		JCheckBox check = new JCheckBox("Constant", s.isConstant());
		check.setEnabled(editable);
		lh.add(check, 0, ++row, 2, 1, 1, 1);
	}

	/**
	 * 
	 * @param ud
	 */
	private void addProperties(UnitDefinition ud) {
		lh.add(new JLabel("Unit: "), 0, ++row, 1, 1, 1, 1);
		StringBuffer laTeXpreview = new StringBuffer();
		laTeXpreview.append(LaTeX.eqBegin);
		System.out.println((new LaTeXExport()).format(ud));
		laTeXpreview.append((new LaTeXExport()).format(ud).toString().replace(
				"\\up", "\\").replace("mathrm", "mbox").replace("text", "mbox")
				.replace("mathtt", "mbox"));
		laTeXpreview.append(LaTeX.eqEnd);
		JPanel preview = new JPanel(new BorderLayout());
		preview.add(new sHotEqn(laTeXpreview.toString()), BorderLayout.CENTER);
		preview.setBackground(Color.WHITE);
		preview.setBorder(BorderFactory.createLoweredBevelBorder());
		lh.add(preview, 1, row, 1, 1, 1, 1);
	}
}
