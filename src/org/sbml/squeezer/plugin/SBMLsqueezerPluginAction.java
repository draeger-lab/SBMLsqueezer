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
package org.sbml.squeezer.plugin;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import jp.sbi.celldesigner.plugin.PluginAction;

/**
 * @author draeger
 * 
 */
public class SBMLsqueezerPluginAction extends PluginAction {


	/**
	 * A serial version number.
	 */
	private static final long serialVersionUID = 4134514954192751545L;
	
	private SBMLsqueezerPlugin plugin;

	/**
	 * @param sbmlSqueezer
	 * 
	 */
	public SBMLsqueezerPluginAction(SBMLsqueezerPlugin sbmlSqueezerPlugin) {
		this.plugin = sbmlSqueezerPlugin;
	}

	/**
	 * Runs two threads for checking if an update for SBMLsqueezer is available
	 * and for initializing an instance of the SBMLsqueezerUI.
	 * 
	 * @param e
	 */
	public void myActionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JMenuItem) {
			final String item = ((JMenuItem) e.getSource()).getText();
			new Thread(new Runnable() {
				public void run() {
					plugin.startSBMLsqueezerPlugin(item);
				}
			}).start();
		} else
			System.err.println("Unsupported source of action "
					+ e.getSource().getClass().getName());
	}
}
