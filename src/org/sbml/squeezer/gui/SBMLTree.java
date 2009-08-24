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

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.sbml.Compartment;
import org.sbml.Event;
import org.sbml.Model;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.Species;

/**
 * @author <a href="mailto:simon.schaefer@uni-tuebingen.de">Simon
 *         Sch&auml;fer</a>
 * 
 */
public class SBMLTree extends JTree {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = -3081533906479036522L;

	public SBMLTree(Model m) {
		super(createNodes(m));
	}

	private static DefaultMutableTreeNode createNodes(Model m) {
		DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode(m);
		DefaultMutableTreeNode node;
		// FunctionDefinitions
		// UnitDefinitions
		// CompartmentTypes
		// SpeciesTypes
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
		// InitialAssignments
		// Rules
		// Constraints
		if (m.getNumReactions() > 0) {
			node = new DefaultMutableTreeNode("Reactions");
			modelNode.add(node);
			for (Reaction r : m.getListOfReactions())
				node.add(new DefaultMutableTreeNode(r));
		}
		if (m.getNumEvents() > 0) {
			node = new DefaultMutableTreeNode("Events");
	          modelNode.add(node);
	          for (Event e : m.getListOfEvents())
	        	  node.add(new DefaultMutableTreeNode(e));
		}
		return modelNode;
	}
}
