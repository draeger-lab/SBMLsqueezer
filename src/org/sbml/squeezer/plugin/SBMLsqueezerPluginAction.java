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
import javax.swing.JOptionPane;

import org.sbml.jsbml.SBMLException;

import jp.sbi.celldesigner.plugin.PluginAction;

/**
 * This class starts SBMLsqueezer from as a CellDesigner plug-in. Actually, this
 * class only forwards commands from CellDesigner to the plugin class that
 * really starts SBMLsqueezer.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLsqueezerPluginAction extends PluginAction {

	/**
	 * A serial version number.
	 */
	private static final long serialVersionUID = 4134514954192751545L;

	/**
	 * An instance of the CellDesigner plug-in.
	 */
	private SBMLsqueezerPlugin plugin;

	/**
	 * 
	 * @param sbmlSqueezerPlugin
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
					try {
						plugin.startSBMLsqueezerPlugin(item);
					} catch (SBMLException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, e.getMessage(), e
								.getClass().getSimpleName(),
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}).start();
		} else
			System.err.printf("Unsupported source of action %s\n", e
					.getSource().getClass().getName());
	}
}
