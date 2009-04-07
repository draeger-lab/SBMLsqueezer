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
 * @author Hannes Borch <hannes.borch@googlemail.com>
 */
public class SBMLsqueezerPlugin extends CellDesignerPlugin {

	/**
	 * The number of the current SBMLsqueezer version.
	 */

	private String versionNumber = "1.2";

	/**
	 * Tells if SBMLsqueezer checked for an update after CellDesigner has been
	 * run.
	 */

	private boolean isUpdateChecked = false;

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
				getSqueezeContextMenuItemText(), action);
		contextMenu.add(contextMenuItem);
		contextMenuItem = new PluginMenuItem(getExportContextMenuItemText(),
				action);
		contextMenu.add(contextMenuItem);

		addReactionPopupMenuSeparator();
		addReactionPopupMenu(contextMenu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#addPluginMenu()
	 */
	public void addPluginMenu() {
	}

	public String getExportContextMenuItemText() {
		return "Export Reaction to other Format";
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
	public String getSqueezeContextMenuItemText() {
		return "Squeeze Kinetic Law";
	}

	/**
	 * @return isUpdateChecked
	 */

	public boolean getUpdateChecked() {
		return isUpdateChecked;
	}

	/**
	 * 
	 * @return versionNumber
	 */

	public String getVersionNumber() {
		return versionNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.sbi.celldesigner.plugin.CellDesignerPlug#modelClosed(jp.sbi.celldesigner
	 * .plugin.PluginSBase)
	 */
	public void modelClosed(PluginSBase arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.sbi.celldesigner.plugin.CellDesignerPlug#modelOpened(jp.sbi.celldesigner
	 * .plugin.PluginSBase)
	 */
	public void modelOpened(PluginSBase arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.sbi.celldesigner.plugin.CellDesignerPlug#modelSelectChanged(jp.sbi
	 * .celldesigner.plugin.PluginSBase)
	 */
	public void modelSelectChanged(PluginSBase arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.sbi.celldesigner.plugin.CellDesignerPlug#SBaseAdded(jp.sbi.celldesigner
	 * .plugin.PluginSBase)
	 */
	public void SBaseAdded(PluginSBase arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.sbi.celldesigner.plugin.CellDesignerPlug#SBaseChanged(jp.sbi.celldesigner
	 * .plugin.PluginSBase)
	 */
	public void SBaseChanged(PluginSBase arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.sbi.celldesigner.plugin.CellDesignerPlug#SBaseDeleted(jp.sbi.celldesigner
	 * .plugin.PluginSBase)
	 */
	public void SBaseDeleted(PluginSBase arg0) {
	}

	/**
	 * Sets the value of isUpdateChecked to the value of b.
	 * 
	 * @param b
	 */

	public void setUpdateChecked(boolean b) {
		isUpdateChecked = b;
	}

	/**
	 * Sets the value of versionNumber to the value of s.
	 * 
	 * @param s
	 */

	public void setVersionNumber(String s) {
		versionNumber = s;
	}
}
