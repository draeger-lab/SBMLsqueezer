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

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.sbml.Model;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.Species;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLsqueezer {

	public SBMLsqueezer(String fileName) {
		LibSBMLReader converter = new LibSBMLReader(fileName);
		showGUI(converter.getModel());
	}

	private void showGUI(Model m) {
		// TODO: only for testing.
		JFrame f = new JFrame("SBMLsqueezer Stand Alone");
		JPanel p = new JPanel(new BorderLayout());
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
				new JScrollPane(model2tree(m),
						JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				new JPanel());
		p.add(split, BorderLayout.CENTER);
		f.getContentPane().add(p);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	/**
	 * 
	 * @param m
	 * @return
	 */
	public JTree model2tree(Model m) {
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
		return new JTree(modelNode);
	}
	
	public JPanel display(Species s) {
		JPanel p = new JPanel();
		p.add(new JLabel(s.getId()));
		return p;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.loadLibrary("sbmlj");
		new SBMLsqueezer(args[0]);
	}

}
