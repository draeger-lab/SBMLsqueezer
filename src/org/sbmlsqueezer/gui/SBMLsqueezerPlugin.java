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

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;
import jp.sbi.celldesigner.plugin.PluginMenu;
import jp.sbi.celldesigner.plugin.PluginMenuItem;
import jp.sbi.celldesigner.plugin.PluginSBase;

/**
 * TODO: comment missing
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author Hannes Borch <hannes.borch@googlemail.com>
 */
public class SBMLsqueezerPlugin extends CellDesignerPlugin {

	/**
	 * The number of the current SBMLsqueezer version.
	 */
	private static final String versionNumber = "1.2.1";

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

	public static final String getVersionNumber() {
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
}
