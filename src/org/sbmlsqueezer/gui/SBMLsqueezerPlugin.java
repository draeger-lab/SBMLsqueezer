package org.sbmlsqueezer.gui;

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;
import jp.sbi.celldesigner.plugin.PluginMenu;
import jp.sbi.celldesigner.plugin.PluginMenuItem;
import jp.sbi.celldesigner.plugin.PluginSBase;

/**
 * TODO: comment missing
 *
 * @since 2.0
 * @version
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0 Aug 3, 2007
 */
public class SBMLsqueezerPlugin extends CellDesignerPlugin {

	/**
	 * Initializes a new SBMLsqueezerPlugin instance.
	 */
	public SBMLsqueezerPlugin() {
		SBMLsqueezerPluginAction action = new SBMLsqueezerPluginAction(this);
		String title = "SBMLsqueezer";
		PluginMenu menu = new PluginMenu(title);
		PluginMenuItem menuItem = new PluginMenuItem(getMainPluginItemText(),
		    action);
		menu.add(menuItem);
		menuItem = new PluginMenuItem(getExporterItemText(), action);
		menu.add(menuItem);
		this.addCellDesignerPluginMenu(menu);

		// Popup menu
		PluginMenu contextMenu = new PluginMenu(title);
		PluginMenuItem contextMenuItem = new PluginMenuItem(
		    getContextMenuItemText(), action);
		contextMenu.add(contextMenuItem);
		addReactionPopupMenuSeparator();
		addReactionPopupMenu(contextMenu);
	}

	/**
	 * TODO: comment missing
	 *
	 * @return
	 */
	public String getExporterItemText() {
		return "Export Model to other Format";
	}

	/**
	 * Returns the label of the menu item that points to this plugin.
	 *
	 * @return Returns the label of the menu item that points to this plugin.
	 */
	public String getMainPluginItemText() {
		return "Squeeze Kinetic Laws";
	}

	/**
	 * TODO: comment missing
	 *
	 * @return
	 */
	public String getContextMenuItemText() {
		return "Squeeze Kinetic Law";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#SBaseAdded(jp.sbi.celldesigner.plugin.PluginSBase)
	 */
	public void SBaseAdded(PluginSBase arg0) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#SBaseChanged(jp.sbi.celldesigner.plugin.PluginSBase)
	 */
	public void SBaseChanged(PluginSBase arg0) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#SBaseDeleted(jp.sbi.celldesigner.plugin.PluginSBase)
	 */
	public void SBaseDeleted(PluginSBase arg0) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#addPluginMenu()
	 */
	public void addPluginMenu() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#modelClosed(jp.sbi.celldesigner.plugin.PluginSBase)
	 */
	public void modelClosed(PluginSBase arg0) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#modelOpened(jp.sbi.celldesigner.plugin.PluginSBase)
	 */
	public void modelOpened(PluginSBase arg0) {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#modelSelectChanged(jp.sbi.celldesigner.plugin.PluginSBase)
	 */
	public void modelSelectChanged(PluginSBase arg0) {
	}

}
