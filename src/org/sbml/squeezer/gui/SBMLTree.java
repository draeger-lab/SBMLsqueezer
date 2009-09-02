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
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.sbml.Compartment;
import org.sbml.CompartmentType;
import org.sbml.Constraint;
import org.sbml.Event;
import org.sbml.EventAssignment;
import org.sbml.FunctionDefinition;
import org.sbml.InitialAssignment;
import org.sbml.Model;
import org.sbml.ModifierSpeciesReference;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.Rule;
import org.sbml.SBase;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.SpeciesType;
import org.sbml.Unit;
import org.sbml.UnitDefinition;
import org.sbml.squeezer.resources.Resource;

/**
 * @author <a href="mailto:simon.schaefer@uni-tuebingen.de">Simon
 *         Sch&auml;fer</a>
 * 
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
		squeezeItem = new JMenuItem("Squeeze kinetic law");
		try {
			squeezeItem.setIcon(new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/Lemon_tiny.png"))));
		} catch (IOException e) {
		}
		squeezeItem.addActionListener(this);
		squeezeItem.setActionCommand(SBMLsqueezerUI.SQUEEZE);
		popup.add(squeezeItem);
		latexItem = new JMenuItem("Export to LaTeX");
		try {
			latexItem.setIcon(new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/SBML2LaTeX_vertical_tiny.png"))));
		} catch (IOException e) {
		}
		latexItem.addActionListener(this);
		latexItem.setActionCommand(SBMLsqueezerUI.TO_LATEX);
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

	private static DefaultMutableTreeNode createNodes(Model m) {
		DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode(m);
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
				DefaultMutableTreeNode unitDefNode = new DefaultMutableTreeNode(c); 
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
				if (r.isSetKineticLaw())
					currReacNode.add(new DefaultMutableTreeNode(r.getKineticLaw()));
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
		return modelNode;
	}

	public void actionPerformed(ActionEvent e) {
		popup.setVisible(false);
		for (ActionListener al : setOfActionListeners) {
			e.setSource(currSBase);
			al.actionPerformed(e);
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (popup.isVisible()) {
			currSBase = null;
			popup.setVisible(false);
		}
		if ((e.getClickCount() == 2) || (e.getButton() == MouseEvent.BUTTON3)
				&& setOfActionListeners.size() > 0) {
			if (getClosestPathForLocation(e.getX(), e.getY())
					.getLastPathComponent() instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) getSelectionPath()
						.getLastPathComponent();
				if (node.getUserObject() instanceof Reaction
						|| node.getUserObject() instanceof Model) {
					currSBase = (SBase) node.getUserObject();
					popup.setLocation(e.getLocationOnScreen());
					popup.setVisible(true);
				}
			}
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}
}
