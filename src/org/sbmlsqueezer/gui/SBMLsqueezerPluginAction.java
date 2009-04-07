package org.sbmlsqueezer.gui;

import java.awt.event.ActionEvent;

import jp.sbi.celldesigner.plugin.PluginAction;

/**
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger <andreas.draeger@uni-tuebingen.de> Copyright (c)
 *         ZBiT, University of T&uuml;bingen, Germany Compiler: JDK 1.6.0 Aug 3,
 *         2007
 * @author Hannes Borch <hannes.borch@googlemail.com>
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

	/**
	 * Runs two threads for checking if an update for SBMLsqueezer is available
	 * and for initializing an instance of the SBMLsqueezerUI.
	 * 
	 * @param e
	 */

	public void myActionPerformed(ActionEvent e) {
		UpdateMessageThread umThread = new UpdateMessageThread(plugin);
		umThread.start();
		SBMLsqueezerUIThread uiThread = new SBMLsqueezerUIThread(e, plugin);
		uiThread.start();
	}
}
