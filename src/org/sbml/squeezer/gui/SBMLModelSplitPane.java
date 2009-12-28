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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SBaseChangedListener;

/**
 * A specialized {@link JSplitPane} that displays a {@link JTree} containing all
 * model elements of a JSBML model on the left hand side and an
 * {@link SBasePanel} showing details of the active element in the tree on the
 * right hand side.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @since 1.3
 */
public class SBMLModelSplitPane extends JSplitPane implements
		TreeSelectionListener, SBaseChangedListener {

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private Set<ActionListener> actionListeners;
	private Properties settings;

	/**
	 * 
	 */
	private SBMLTree tree;

	/**
	 * 
	 * @param model
	 */
	public SBMLModelSplitPane(Model model, Properties settings) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		actionListeners = new HashSet<ActionListener>();
		this.settings = settings;
		model.addChangeListener(this);
		init(model, false);
	}

	/**
	 * 
	 * @param al
	 */
	public void addActionListener(ActionListener al) {
		tree.addActionListener(al);
		actionListeners.add(al);
	}

	/**
	 * 
	 * @param sbase
	 * @return
	 */
	private JScrollPane createRightComponent(SBase sbase) {
		JPanel p = new JPanel();
		p.add(new SBasePanel(sbase, settings));
		JScrollPane scroll = new JScrollPane(p,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return scroll;
	}

	/**
	 * 
	 * @param model
	 * @param keepDivider
	 */
	public void init(Model model, boolean keepDivider) {
		int proportionalLocation = getDividerLocation();
		TreePath path = null;
		if (tree != null)
			path = (TreePath) tree.getSelectionPath();
		tree = new SBMLTree(model);
		tree.setShowsRootHandles(true);
		tree.setScrollsOnExpand(true);
		for (ActionListener al : actionListeners)
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
		validate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.io.SBaseChangedListener#sbaseAdded(org.sbml.jlibsbml
	 * .SBase)
	 */
	public void sbaseAdded(SBase sb) {
		// TreePath path = tree.getSelectionPath();
		// init(sb.getModel(), true);
		// tree.setSelectionPath(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.io.SBaseChangedListener#sbaseRemoved(org.sbml.jlibsbml
	 * .SBase)
	 */
	public void sbaseRemoved(SBase sb) {
		// TreePath path = tree.getSelectionPath();
		// init(sb.getModel(), true);
		// tree.setSelectionPath(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.io.SBaseChangedListener#stateChanged(org.sbml.jlibsbml
	 * .SBase)
	 */
	public void stateChanged(SBase sb) {
		// TreePath path = tree.getSelectionPath();
		// init(sb.getModel(), true);
		// tree.setSelectionPath(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event
	 * .TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
				.getLastSelectedPathComponent();
		if (node == null)
			// Nothing is selected.
			return;
		Object nodeInfo = node.getUserObject();
		if (nodeInfo instanceof SBase) {
			int proportionalLocation = getDividerLocation();
			setRightComponent(createRightComponent((SBase) nodeInfo));
			setDividerLocation(proportionalLocation);
		} else {
			// displayURL(helpURL);
		}
	}
}
