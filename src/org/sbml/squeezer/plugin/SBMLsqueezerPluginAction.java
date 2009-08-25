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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JMenuItem;

import jp.sbi.celldesigner.plugin.PluginAction;
import jp.sbi.celldesigner.plugin.PluginReaction;

import org.sbml.squeezer.gui.KineticLawSelectionDialog;
import org.sbml.squeezer.gui.UpdateMessage;
import org.sbml.squeezer.resources.Resource;

/**
 * TODO: comment missing
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 */
public class SBMLsqueezerPluginAction extends PluginAction {

	static {
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					Resource.class.getResource("txt/disclaimer.txt").getFile()));
			String line;
			while ((line = br.readLine()) != null)
				System.out.println(line);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * A serial version number.
	 */
	private static final long serialVersionUID = 4134514954192751545L;

	/**
	 * The CellDesigner plugin to which this action belongs.
	 */
	private SBMLsqueezerPlugin plugin;

	/**
	 * @param plugin
	 */
	public SBMLsqueezerPluginAction(SBMLsqueezerPlugin plugin) {
		this.plugin = plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.sbi.celldesigner.plugin.PluginActionListener#myActionPerformed(java
	 * .awt.event.ActionEvent)
	 */

	/**
	 * Runs two threads for checking if an update for SBMLsqueezer is available
	 * and for initializing an instance of the SBMLsqueezerUI.
	 * 
	 * @param e
	 */
	public void myActionPerformed(ActionEvent e) {
		new Thread(new Runnable() {
			public void run() {
				try {
					UpdateMessage.checkForUpdate(plugin);
				} catch (IOException exc) {
					// JOptionPane.showMessageDialog(null, exc.getMessage());
					// exc.printStackTrace();
				}
			}
		}).start();
		if (e.getSource() instanceof JMenuItem) {
			final String item = ((JMenuItem) e.getSource()).getText();
			new Thread(new Runnable() {
				public void run() {
					startSBMLsqueezer(item);
				}
			}).start();
		} else
			System.err.println("Unsupported source of action "
					+ e.getSource().getClass().getName());
	}

	/**
	 * Starts the SBMLsqueezer dialog window for kinetic law selection or LaTeX
	 * export.
	 * 
	 * @param item
	 */
	private void startSBMLsqueezer(String item) {
		PluginSBMLReader reader = new PluginSBMLReader(plugin
				.getSelectedModel());
		if (item.equals(plugin.getMainPluginItemText()))
			(new KineticLawSelectionDialog(null, reader)).setVisible(true);
		else if (item.equals(plugin.getSqueezeContextMenuItemText()))
			new KineticLawSelectionDialog(null, reader, reader
					.convert((PluginReaction) plugin.getSelectedReactionNode()
							.get(0)));
		else if (item.equals(plugin.getExportContextMenuItemText()))
			new KineticLawSelectionDialog(null, reader.getSelectedModel()
					.getReaction(
							((PluginReaction) plugin.getSelectedReactionNode()
									.get(0)).getId()));
		else if (item.equals(plugin.getExporterItemText()))
			if (plugin.getSelectedModel() != null)
				new KineticLawSelectionDialog(null, reader.getSelectedModel());
	}
}
