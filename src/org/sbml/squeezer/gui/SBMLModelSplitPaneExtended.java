/*
 * $Id: SBMLModelSplitPaneExtended.java 751 2012-01-03 16:25:28Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/SBMLModelSplitPaneExtended.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.gui;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.TreeNodeChangeListener;

import de.zbit.sbml.gui.SBasePanel;
import de.zbit.sbml.gui.SBMLModelSplitPane;
import de.zbit.sbml.gui.SBMLTree;
import de.zbit.sbml.gui.ASTNodePanel;

/**
 * A specialized {@link JSplitPane} that displays a {@link JTree} containing all
 * model elements of a JSBML model on the left hand side and an
 * {@link SBasePanel} showing details of the active element in the tree on the
 * right hand side.
 * 
 * @author Sebastian Nagel
 * @since 1.4
 * @version $Rev: 751 $
 */
public class SBMLModelSplitPaneExtended extends SBMLModelSplitPane implements TreeNodeChangeListener {

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
	
	/**
	 * @param model
	 * @throws SBMLException
	 * @throws IOException
	 */
	public SBMLModelSplitPaneExtended(Model model) throws SBMLException, IOException {
		super(model.getSBMLDocument(), true);
		
		init(model, false);
	}
	
	/**
	 * Function to display the properties of {@link ASTNode} objects.
	 * 
	 * @param nodeInfo
	 * @return
	 * @throws IOException 
	 * @throws SBMLException 
	 */
	private JScrollPane createRightComponent(Object o) throws SBMLException, IOException {
		JScrollPane scroll = null;
		JPanel p = new JPanel();
		JPanel panel = null;
		if (o instanceof ASTNode) {
			panel = new ASTNodePanel((ASTNode) o, true, true, new EquationRenderer());
		} else if (o instanceof SBase) {
			panel = new SBasePanel((SBase) o, true, new EquationRenderer());
		}
		if (panel != null) {
			p.add(panel);
		}
		scroll = new JScrollPane(p,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return scroll;
	}

	/**
	 * 
	 * @param model
	 * @param keepDivider
	 * @throws IOException 
	 * @throws SBMLException 
	 */
	public void init(Model model, boolean keepDivider) throws SBMLException, IOException {
		int proportionalLocation = getDividerLocation();
		TreePath path = null;
		if (this.getTree() != null){
			path = (TreePath) this.getTree().getSelectionPath();
		}
		SBMLTree tree = new SBMLTree(model);
		JPopupMenu popup = new JPopupMenu("SBMLsqueezer");
		JMenuItem squeezeItem = new JMenuItem("Squeeze kinetic law", UIManager
				.getIcon("ICON_LEMON_TINY"));
		squeezeItem.addActionListener(tree);
		squeezeItem.setActionCommand(SBMLsqueezerUI.Command.SQUEEZE.toString());
		popup.add(squeezeItem);
		JMenuItem latexItem = new JMenuItem("Export to LaTeX", UIManager
				.getIcon("ICON_LATEX_TINY"));
		latexItem.addActionListener(tree);
		latexItem.setActionCommand(SBMLsqueezerUI.Command.TO_LATEX.toString());
		popup.add(latexItem);
		popup.setOpaque(true);
		popup.setLightWeightPopupEnabled(true);

		tree.setPopupMenu(popup);
		
		tree.setShowsRootHandles(true);
		tree.setScrollsOnExpand(true);
		for (ActionListener al : this.getActionListeners())
			tree.addActionListener(al);
		if (path != null) {
			// tree.setSelectionPath(path);
			tree.setExpandsSelectedPaths(true);
			tree.expandPath(path);
		}
		tree.addTreeSelectionListener(this);
		tree.setSelectionRow(0);
		setLeftComponent(new JScrollPane(tree,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		setRightComponent(createRightComponent(model));
		if (keepDivider)
			setDividerLocation(proportionalLocation);
		
		this.setTree(tree);
		
		validate();
	}
	
	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeAdded(javax.swing.tree.TreeNode)
	 */
	public void nodeAdded(TreeNode node) {
		// TODO
		// TreePath path = tree.getSelectionPath();
		// init(node.getModel(), true);
		// tree.setSelectionPath(path);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeRemoved(javax.swing.tree.TreeNode)
	 */
	public void nodeRemoved(TreeNode node) {
		// TODO
		// TreePath path = tree.getSelectionPath();
		// init(node.getModel(), true);
		// tree.setSelectionPath(path);
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO
	}
}
