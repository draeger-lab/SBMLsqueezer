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
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.sbml.Model;
import org.sbml.NamedSBase;
import org.sbml.Species;
import org.sbml.squeezer.resources.Resource;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLsqueezer extends JFrame implements TreeSelectionListener {

	private JPanel details;
	private SBMLTree tree;

	public SBMLsqueezer(String fileName) {
		super("SBMLsqueezer Stand Alone");
		LibSBMLReader converter = new LibSBMLReader(fileName);
		showGUI(converter.getModel());
	}

	private void showGUI(Model m) {
		// TODO: only for testing.
		JPanel p = new JPanel(new BorderLayout());
		details = new JPanel();
		this.tree = new SBMLTree(m);
		tree.addTreeSelectionListener(this);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
				new JScrollPane(tree, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
						JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), details);
		p.add(split, BorderLayout.CENTER);
		getContentPane().add(p);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public JPanel display(Species s) {
		JPanel p = display((NamedSBase) s);
		return p;
	}

	public JPanel display(NamedSBase nsb) {
		JPanel p = new JPanel();
		if (nsb.isSetId())
			p.add(new JLabel(nsb.getId()));
		if (nsb.isSetName())
			p.add(new JLabel(nsb.getName()));
		if (nsb.isSetNotes())
			p.add(new JLabel(nsb.getNotes()));
		return p;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.loadLibrary("sbmlj");
		new SBMLsqueezer(args[0]);
	}

	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();
		if (node == null)
			// Nothing is selected.
			return;
		Object nodeInfo = node.getUserObject();
		if (node.isLeaf()) {
			NamedSBase sbase = (NamedSBase) nodeInfo;
			System.out.println(sbase.getClass().getName());
			details.removeAll();
			details.add(display(sbase));
			validate();
			pack();
		} else {
			// displayURL(helpURL);
		}
	}

}
