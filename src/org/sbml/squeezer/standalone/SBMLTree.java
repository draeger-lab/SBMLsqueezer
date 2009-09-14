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
package org.sbml.squeezer.standalone;

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

import org.sbml.jsbml.MathContainer;
import org.sbml.libsbml.Event;
import org.sbml.libsbml.KineticLaw;
import org.sbml.libsbml.Model;
import org.sbml.libsbml.Reaction;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.SBase;
import org.sbml.libsbml.UnitDefinition;
import org.sbml.squeezer.gui.GUITools;
import org.sbml.squeezer.gui.SBMLsqueezerUI;

/**
 * Just for testing purposes.
 * 
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
		squeezeItem = new JMenuItem("Squeeze kinetic law",
				GUITools.LEMON_ICON_TINY);
		squeezeItem.addActionListener(this);
		squeezeItem.setActionCommand(SBMLsqueezerUI.Command.SQUEEZE.toString());
		popup.add(squeezeItem);
		latexItem = new JMenuItem("Export to LaTeX", GUITools.LATEX_ICON_TINY);
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
		long i, j;
		if (m.getNumFunctionDefinitions() > 0) {
			node = new DefaultMutableTreeNode("Function Definitions");
			modelNode.add(node);
			for (i = 0; i < m.getNumFunctionDefinitions(); i++)
				node.add(new DefaultMutableTreeNode(m.getFunctionDefinition(i)
						.getId()));
		}
		if (m.getNumUnitDefinitions() > 0) {
			node = new DefaultMutableTreeNode("Unit Definitions");
			modelNode.add(node);
			for (i = 0; i < m.getNumUnitDefinitions(); i++) {
				UnitDefinition ud = m.getUnitDefinition(i);
				DefaultMutableTreeNode unitDefNode = new DefaultMutableTreeNode(
						ud.getId());
				node.add(unitDefNode);
				for (j = 0; j < ud.getNumUnits(); j++)
					unitDefNode.add(new DefaultMutableTreeNode(ud.getUnit(j)));
			}
		}
		if (m.getNumCompartmentTypes() > 0) {
			node = new DefaultMutableTreeNode("Compartment Types");
			modelNode.add(node);
			for (i = 0; i < m.getNumCompartmentTypes(); i++)
				node.add(new DefaultMutableTreeNode(m.getCompartmentType(i)
						.getId()));
		}
		if (m.getNumSpeciesTypes() > 0) {
			node = new DefaultMutableTreeNode("Species Types");
			modelNode.add(node);
			for (i = 0; i < m.getNumSpeciesTypes(); i++)
				node.add(new DefaultMutableTreeNode(m.getSpecies(i).getId()));
		}
		if (m.getNumCompartments() > 0) {
			node = new DefaultMutableTreeNode("Compartments");
			modelNode.add(node);
			for (i = 0; i < m.getNumCompartments(); i++)
				node
						.add(new DefaultMutableTreeNode(m.getCompartment(i)
								.getId()));
		}
		if (m.getNumSpecies() > 0) {
			node = new DefaultMutableTreeNode("Species");
			modelNode.add(node);
			for (i = 0; i < m.getNumSpecies(); i++)
				node.add(new DefaultMutableTreeNode(m.getSpecies(i).getId()));
		}
		if (m.getNumParameters() > 0) {
			node = new DefaultMutableTreeNode("Parameters");
			modelNode.add(node);
			for (i = 0; i < m.getNumParameters(); i++)
				node.add(new DefaultMutableTreeNode(m.getParameter(i).getId()));
		}
		if (m.getNumInitialAssignments() > 0) {
			node = new DefaultMutableTreeNode("Initial Assignments");
			modelNode.add(node);
			for (i = 0; i < m.getNumInitialAssignments(); i++)
				node.add(new DefaultMutableTreeNode(m.getInitialAssignment(i)));
		}
		if (m.getNumRules() > 0) {
			node = new DefaultMutableTreeNode("Rules");
			modelNode.add(node);
			for (i = 0; i < m.getNumRules(); i++)
				node.add(new DefaultMutableTreeNode(m.getRule(i)));
		}
		if (m.getNumConstraints() > 0) {
			node = new DefaultMutableTreeNode("Constraints");
			modelNode.add(node);
			for (i = 0; i < m.getNumConstraints(); i++)
				node.add(new DefaultMutableTreeNode(m.getConstraint(i)));
		}
		if (m.getNumReactions() > 0) {
			node = new DefaultMutableTreeNode("Reactions");
			modelNode.add(node);
			for (i = 0; i < m.getNumReactions(); i++) {
				Reaction r = m.getReaction(i);
				DefaultMutableTreeNode currReacNode = new DefaultMutableTreeNode(
						r.getId());
				node.add(currReacNode);
				if (r.getNumReactants() > 0) {
					DefaultMutableTreeNode reactants = new DefaultMutableTreeNode(
							"Reactants");
					currReacNode.add(reactants);
					for (j = 0; j < r.getNumReactants(); j++)
						reactants.add(new DefaultMutableTreeNode(r.getReactant(
								j).getSpecies()));
				}
				if (r.getNumProducts() > 0) {
					DefaultMutableTreeNode products = new DefaultMutableTreeNode(
							"Products");
					currReacNode.add(products);
					for (j = 0; j < r.getNumProducts(); j++)
						products.add(new DefaultMutableTreeNode(r.getProduct(j)
								.getSpecies()));
				}
				if (r.getNumModifiers() > 0) {
					DefaultMutableTreeNode modifiers = new DefaultMutableTreeNode(
							"Modifiers");
					currReacNode.add(modifiers);
					for (j = 0; j < r.getNumModifiers(); j++)
						modifiers.add(new DefaultMutableTreeNode(r.getModifier(
								j).getSpecies()));
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
						for (j = 0; j < kl.getNumParameters(); j++)
							n.add(new DefaultMutableTreeNode(kl.getParameter(j)
									.getId()));
					}
				}
			}
		}
		if (m.getNumEvents() > 0) {
			node = new DefaultMutableTreeNode("Events");
			modelNode.add(node);
			for (i = 0; i < m.getNumEvents(); i++) {
				Event e = m.getEvent(i);
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
					for (j = 0; j < e.getNumEventAssignments(); j++) {
						DefaultMutableTreeNode eaNode = new DefaultMutableTreeNode(
								e.getEventAssignment(j));
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
					popup.setLocation(e.getLocationOnScreen());
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
