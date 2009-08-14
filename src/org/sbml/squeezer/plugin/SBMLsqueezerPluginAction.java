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
import java.io.PrintStream;

import javax.swing.JMenuItem;

import jp.sbi.celldesigner.plugin.PluginAction;
import jp.sbi.celldesigner.plugin.PluginReaction;

import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.gui.UpdateMessageThread;

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
		PrintStream out = System.out;
		out.println("-----------------------------------------------------");
		out.println("SBMLsqueezer Copyright (C) 2009 Andreas Dr\u00e4ger");
		out.println("This program comes with ABSOLUTELY NO WARRANTY.");
		out.println("This is free software, and you are welcome");
		out.println("to redistribute it under certain conditions;");
		out.println("see http://www.gnu.org/copyleft/gpl.html for details.");
		out.println("-----------------------------------------------------");
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
		UpdateMessageThread umThread = new UpdateMessageThread(plugin);
		umThread.start();
		if (e.getSource() instanceof JMenuItem) {
			String item = ((JMenuItem) e.getSource()).getText();
			if (item.equals(plugin.getMainPluginItemText()))
				new Thread(new Runnable() {
					public void run() {
						(new SBMLsqueezerUI(plugin)).setVisible(true);
					}
				}).start();
			else if (item.equals(plugin.getSqueezeContextMenuItemText()))
				new Thread(new Runnable() {
					public void run() {
						new SBMLsqueezerUI(plugin, (PluginReaction) plugin
								.getSelectedReactionNode().get(0));
					}
				}).start();
			else if (item.equals(plugin.getExportContextMenuItemText()))
				new Thread(new Runnable() {
					public void run() {
						new SBMLsqueezerUI(plugin.getSelectedModel(),
								(PluginReaction) plugin
										.getSelectedReactionNode().get(0));
					}
				}).start();
			else if (item.equals(plugin.getExporterItemText()))
				if (plugin.getSelectedModel() != null)
					new Thread(new Runnable() {
						public void run() {
							new SBMLsqueezerUI(plugin.getSelectedModel());
						}
					}).start();
		} else
			System.err.println("Unsupported source of action "
					+ e.getSource().getClass().getName());
	}
}
