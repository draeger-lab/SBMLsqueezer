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

import java.awt.LayoutManager;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-13
 * 
 */
public abstract class SettingsPanel extends JPanel implements KeyListener {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 1852850798328875230L;
	/**
	 * 
	 */
	private List<KeyListener> keyListeners;

	/**
	 * 
	 */
	public SettingsPanel() {
		super();
		init();
	}

	/**
	 * 
	 * @param layout
	 */
	public SettingsPanel(LayoutManager layout) {
		super(layout);
		init();
	}

	/**
	 * 
	 */
	private void init() {
		keyListeners = new LinkedList<KeyListener>();
	}

	/**
	 * 
	 */
	public void addKeyListener(KeyListener kl) {
		keyListeners.add(kl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e) {
		for (KeyListener kl : keyListeners)
			kl.keyTyped(e);
	}

}
