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

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.sbml.Model;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.Species;

/**
 * @author <a href="mailto:simon.schaefer@uni-tuebingen.de">Simon Sch&auml;fer</a>
 *
 */
public class SBMLTree extends JTree {

	public SBMLTree(Model m) {
		super(createNodes(m));
	}

	private static DefaultMutableTreeNode createNodes(Model m) {
		DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode(m);
		if (m.getNumReactions() > 0) {
			DefaultMutableTreeNode reactionsNode = new DefaultMutableTreeNode(
					"Reactions");
			modelNode.add(reactionsNode);
			for (Reaction r : m.getListOfReactions())
				reactionsNode.add(new DefaultMutableTreeNode(r));
		}
		if (m.getNumSpecies() > 0) {
			DefaultMutableTreeNode speciesNode = new DefaultMutableTreeNode(
					"Species");
			modelNode.add(speciesNode);
			for (Species s : m.getListOfSpecies())
				speciesNode.add(new DefaultMutableTreeNode(s));
		}
		if (m.getNumParameters() > 0) {
			DefaultMutableTreeNode parametersNode = new DefaultMutableTreeNode(
					"Parameters");
			modelNode.add(parametersNode);
			for (Parameter p : m.getListOfParameters())
				parametersNode.add(new DefaultMutableTreeNode(p));
		}
		return modelNode;
	}
}
