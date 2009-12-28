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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;

/**
 * A specialized {@link JTree} that shows the elements of a JSBML model as a
 * hierarchical structure.
 * 
 * @author <a href="mailto:simon.schaefer@uni-tuebingen.de">Simon
 *         Sch&auml;fer</a>
 * @author Andreas Dr&auml;ger
 * @since 1.3
 */
public class SBMLTree extends JTree implements MouseListener, ActionListener {

	private JPopupMenu popup;

	private JMenuItem squeezeItem;

	private JMenuItem latexItem;

	private SBase currSBase;

	private Set<ActionListener> setOfActionListeners;

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = -3081533906479036522L;

	public SBMLTree(Model m) {
		super(createNodes(m));
		setOfActionListeners = new HashSet<ActionListener>();
		popup = new JPopupMenu("SBMLsqueezer");
		squeezeItem = new JMenuItem("Squeeze kinetic law",
				GUITools.ICON_LEMON_TINY);
		squeezeItem.addActionListener(this);
		squeezeItem.setActionCommand(SBMLsqueezerUI.Command.SQUEEZE.toString());
		popup.add(squeezeItem);
		latexItem = new JMenuItem("Export to LaTeX", GUITools.ICON_LATEX_TINY);
		latexItem.addActionListener(this);
		latexItem.setActionCommand(SBMLsqueezerUI.Command.TO_LATEX.toString());
		popup.add(latexItem);
		popup.setOpaque(true);
		popup.setLightWeightPopupEnabled(true);
		addMouseListener(this);
	}

	/**
	 * 
	 * @param al
	 */
	public void addActionListener(ActionListener al) {
		setOfActionListeners.add(al);
	}

	/**
	 * 
	 * @param m
	 * @return
	 */
	private static DefaultMutableTreeNode createNodes(Model m) {
		DefaultMutableTreeNode docNode = new DefaultMutableTreeNode(m
				.getSBMLDocument());
		DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode(m);
		docNode.add(modelNode);
		DefaultMutableTreeNode node;
		if (m.getNumFunctionDefinitions() > 0) {
			node = new DefaultMutableTreeNode("Function Definitions");
			modelNode.add(node);
			for (FunctionDefinition c : m.getListOfFunctionDefinitions())
				node.add(new DefaultMutableTreeNode(c));
		}
		if (m.getNumUnitDefinitions() > 0) {
			node = new DefaultMutableTreeNode("Unit Definitions");
			modelNode.add(node);
			for (UnitDefinition c : m.getListOfUnitDefinitions()) {
				DefaultMutableTreeNode unitDefNode = new DefaultMutableTreeNode(
						c);
				node.add(unitDefNode);
				for (Unit u : c.getListOfUnits())
					unitDefNode.add(new DefaultMutableTreeNode(u));
			}
		}
		if (m.getNumCompartmentTypes() > 0) {
			node = new DefaultMutableTreeNode("Compartment Types");
			modelNode.add(node);
			for (CompartmentType c : m.getListOfCompartmentTypes())
				node.add(new DefaultMutableTreeNode(c));
		}
		if (m.getNumSpeciesTypes() > 0) {
			node = new DefaultMutableTreeNode("Species Types");
			modelNode.add(node);
			for (SpeciesType c : m.getListOfSpeciesTypes())
				node.add(new DefaultMutableTreeNode(c));
		}
		if (m.getNumCompartments() > 0) {
			node = new DefaultMutableTreeNode("Compartments");
			modelNode.add(node);
			for (Compartment c : m.getListOfCompartments())
				node.add(new DefaultMutableTreeNode(c));
		}
		if (m.getNumSpecies() > 0) {
			node = new DefaultMutableTreeNode("Species");
			modelNode.add(node);
			for (Species s : m.getListOfSpecies())
				node.add(new DefaultMutableTreeNode(s));
		}
		if (m.getNumParameters() > 0) {
			node = new DefaultMutableTreeNode("Parameters");
			modelNode.add(node);
			for (Parameter p : m.getListOfParameters())
				node.add(new DefaultMutableTreeNode(p));
		}
		if (m.getNumInitialAssignments() > 0) {
			node = new DefaultMutableTreeNode("Initial Assignments");
			modelNode.add(node);
			for (InitialAssignment c : m.getListOfInitialAssignments())
				node.add(new DefaultMutableTreeNode(c));
		}
		if (m.getNumRules() > 0) {
			node = new DefaultMutableTreeNode("Rules");
			modelNode.add(node);
			for (Rule c : m.getListOfRules())
				node.add(new DefaultMutableTreeNode(c));
		}
		if (m.getNumConstraints() > 0) {
			node = new DefaultMutableTreeNode("Constraints");
			modelNode.add(node);
			for (Constraint c : m.getListOfConstraints())
				node.add(new DefaultMutableTreeNode(c));
		}
		if (m.getNumReactions() > 0) {
			node = new DefaultMutableTreeNode("Reactions");
			modelNode.add(node);
			for (Reaction r : m.getListOfReactions()) {
				DefaultMutableTreeNode currReacNode = new DefaultMutableTreeNode(
						r);
				node.add(currReacNode);
				if (r.getNumReactants() > 0) {
					DefaultMutableTreeNode reactants = new DefaultMutableTreeNode(
							"Reactants");
					currReacNode.add(reactants);
					for (SpeciesReference specRef : r.getListOfReactants())
						reactants.add(new DefaultMutableTreeNode(specRef));
				}
				if (r.getNumProducts() > 0) {
					DefaultMutableTreeNode products = new DefaultMutableTreeNode(
							"Products");
					currReacNode.add(products);
					for (SpeciesReference specRef : r.getListOfProducts())
						products.add(new DefaultMutableTreeNode(specRef));
				}
				if (r.getNumModifiers() > 0) {
					DefaultMutableTreeNode modifiers = new DefaultMutableTreeNode(
							"Modifiers");
					currReacNode.add(modifiers);
					for (ModifierSpeciesReference mSpecRef : r
							.getListOfModifiers())
						modifiers.add(new DefaultMutableTreeNode(mSpecRef));
				}
				if (r.isSetKineticLaw()) {
					KineticLaw kl = r.getKineticLaw();
					DefaultMutableTreeNode klNode = new DefaultMutableTreeNode(
							kl);
					currReacNode.add(klNode);
					if (kl.getNumParameters() > 0) {
						DefaultMutableTreeNode n = new DefaultMutableTreeNode(
								"Parameters");
						klNode.add(n);
						for (Parameter p : kl.getListOfParameters())
							n.add(new DefaultMutableTreeNode(p));
					}
				}
			}
		}
		if (m.getNumEvents() > 0) {
			node = new DefaultMutableTreeNode("Events");
			modelNode.add(node);
			for (Event e : m.getListOfEvents()) {
				DefaultMutableTreeNode eNode = new DefaultMutableTreeNode(e);
				node.add(eNode);
				if (e.isSetTrigger())
					eNode.add(new DefaultMutableTreeNode(e.getTrigger()));
				if (e.isSetDelay())
					eNode.add(new DefaultMutableTreeNode(e.getDelay()));
				if (e.getNumEventAssignments() > 0) {
					DefaultMutableTreeNode eas = new DefaultMutableTreeNode(
							"Event Assignments");
					eNode.add(eas);
					for (EventAssignment ea : e.getListOfEventAssignments()) {
						DefaultMutableTreeNode eaNode = new DefaultMutableTreeNode(
								ea);
						eas.add(eaNode);
					}
				}
			}
		}
		return docNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		popup.setVisible(false);
		for (ActionListener al : setOfActionListeners) {
			e.setSource(currSBase);
			al.actionPerformed(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		if (popup.isVisible()) {
			currSBase = null;
			popup.setVisible(false);
		}
		if ((e.getClickCount() == 2) || (e.getButton() == MouseEvent.BUTTON3)
				&& setOfActionListeners.size() > 0) {
			Object clickedOn = getClosestPathForLocation(e.getX(), e.getY())
					.getLastPathComponent();
			if (clickedOn instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) getSelectionPath()
						.getLastPathComponent();
				Object userObject = node.getUserObject();
				if (userObject instanceof Reaction
						|| userObject instanceof Model
						|| userObject instanceof SBMLDocument) {
					if (userObject instanceof SBMLDocument)
						currSBase = ((SBMLDocument) userObject).getModel();
					else
						currSBase = (SBase) userObject;
					popup.setLocation(e.getX()
							+ ((int) getLocationOnScreen().getX()), e.getY()
							+ ((int) getLocationOnScreen().getY()));// e.getLocationOnScreen());
					popup.setVisible(true);
				}
				if (((DefaultMutableTreeNode) clickedOn).getUserObject() instanceof MathContainer) {
					MathContainer mc = (MathContainer) ((DefaultMutableTreeNode) clickedOn)
							.getUserObject();
					JDialog dialog = new JDialog();
					dialog.getContentPane().add(
							new JTree((TreeNode) mc.getMath()));
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.pack();
					dialog.setModal(true);
					dialog.setLocationRelativeTo(null);
					dialog.setVisible(true);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}
}
