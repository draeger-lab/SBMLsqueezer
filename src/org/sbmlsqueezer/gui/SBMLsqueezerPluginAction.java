package org.sbmlsqueezer.gui;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import jp.sbi.celldesigner.plugin.PluginAction;
import jp.sbi.celldesigner.plugin.PluginReaction;

/**
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger <andreas.draeger@uni-tuebingen.de> Copyright (c)
 *         ZBiT, University of T&uuml;bingen, Germany Compiler: JDK 1.6.0 Aug 3,
 *         2007
 */
public class SBMLsqueezerPluginAction extends PluginAction {
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
	public void myActionPerformed(ActionEvent e) {
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
