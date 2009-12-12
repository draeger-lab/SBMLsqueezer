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
package org.sbmlsqueezer.gui;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import jp.sbi.celldesigner.plugin.PluginReaction;

/**
 * This thread initializes one of the different kinds of the
 * SBMLsqueezerUI-Class, according to the selected JMenuItem.
 * 
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @since 1.2
 * 
 */

public class SBMLsqueezerUIThread extends Thread {

	private ActionEvent e;

	SBMLsqueezerPlugin plugin;

	/**
	 * Main constructor
	 * 
	 * @param event
	 * @param plugin
	 */
	public SBMLsqueezerUIThread(ActionEvent event, SBMLsqueezerPlugin plugin) {
		super();
		this.plugin = plugin;
		e = event;
	}

	/**
	 * Inherited from java.lang.Thread.
	 */
	// @Override
	public void run() {
		if (e.getSource() instanceof JMenuItem) {
			String item = ((JMenuItem) e.getSource()).getText();
			if (item.equals(plugin.getMainPluginItemText()))
				(new SBMLsqueezerUI(plugin)).setVisible(true);
			else if (item.equals(plugin.getSqueezeContextMenuItemText()))
				new SBMLsqueezerUI(plugin, (PluginReaction) plugin
						.getSelectedReactionNode().get(0));
			else if (item.equals(plugin.getExportContextMenuItemText()))
				new SBMLsqueezerUI(plugin.getSelectedModel(),
						(PluginReaction) plugin.getSelectedReactionNode()
								.get(0));
			else if (item.equals(plugin.getExporterItemText()))
				if (plugin.getSelectedModel() != null)
					new SBMLsqueezerUI(plugin.getSelectedModel());
		} else
			System.err.println("Unsupported source of action "
					+ e.getSource().getClass().getName());
	}

}
