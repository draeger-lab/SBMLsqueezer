package org.sbmlsqueezer.gui;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import jp.sbi.celldesigner.plugin.PluginReaction;

/**
 * This thread initializes one of the different kinds of the
 * SBMLsqueezerUI-Class, according to the selected JMenuItem.
 * 
 * @author Hannes Borch <hannes.borch@googlemail.com>
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
	
	@Override
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
