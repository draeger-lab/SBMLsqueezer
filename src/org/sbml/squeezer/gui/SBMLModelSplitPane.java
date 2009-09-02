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

import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.sbml.AbstractSBase;
import org.sbml.Model;
import org.sbml.SBase;
import org.sbml.squeezer.io.SBaseChangedListener;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLModelSplitPane extends JSplitPane implements
		TreeSelectionListener, SBaseChangedListener {

	/**
	 * 
	 */
	private SBMLTree tree;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param model
	 */
	public SBMLModelSplitPane(Model model) {
		super(JSplitPane.HORIZONTAL_SPLIT, true);
		model.addChangeListener(this);
		init(model, false);
	}

	/**
	 * 
	 * @param al
	 */
	public void addActionListener(ActionListener al) {
		tree.addActionListener(al);
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
			validate();
		} else {
			// displayURL(helpURL);
		}
	}

	/**
	 * 
	 * @param sbase
	 * @return
	 */
	private JScrollPane createRightComponent(SBase sbase) {
		JPanel p = new JPanel();
		p.add(new SBasePanel(sbase));
		return new JScrollPane(p, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

	/**
	 * 
	 * @param model
	 * @param keepDivider
	 */
	private void init(Model model, boolean keepDivider) {
		int proportionalLocation = getDividerLocation();
		tree = new SBMLTree(model);
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
	 * org.sbml.squeezer.io.SBaseChangedListener#sbaseAdded(org.sbml.AbstractSBase
	 * )
	 */
	public void sbaseAdded(AbstractSBase sb) {
		init(sb.getModel(), true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.io.SBaseChangedListener#sbaseRemoved(org.sbml.AbstractSBase
	 * )
	 */
	public void sbaseRemoved(AbstractSBase sb) {
		init(sb.getModel(), true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.io.SBaseChangedListener#stateChanged(org.sbml.AbstractSBase
	 * )
	 */
	public void stateChanged(AbstractSBase sb) {
		init(sb.getModel(), true);
	}
}
