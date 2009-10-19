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

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;
import jp.sbi.celldesigner.plugin.PluginMenu;
import jp.sbi.celldesigner.plugin.PluginMenuItem;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSBase;

import org.sbml.jsbml.SBO;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.gui.KineticLawSelectionDialog;

/**
 * This is the main class for the CellDesigner plugin mode of SBMLsqueezer.
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
	 * 
	 */
	private SBMLsqueezer sbmlSqueezer;

	/**
	 * Initializes a new SBMLsqueezerPlugin instance.
	 */
	public SBMLsqueezerPlugin() {
		super();
		try {
			/*
			 * Starting SBMLsqueezer...
			 */
			sbmlSqueezer = new SBMLsqueezer(
					new PluginSBMLReader(SBO.getPossibleEnzymes(SBMLsqueezer
							.getPossibleEnzymeTypes())), new PluginSBMLWriter(this));
			sbmlSqueezer.checkForUpdate(true);
			/*
			 * Initializing CellDesigner's menu entries
			 */
			SBMLsqueezerPluginAction action = new SBMLsqueezerPluginAction(this);
			String title = "SBMLsqueezer " + SBMLsqueezer.getVersionNumber();
			PluginMenu menu = new PluginMenu(title);
			PluginMenuItem menuItem = new PluginMenuItem(
					getMainPluginItemText(), action);
			menu.add(menuItem);
			menuItem = new PluginMenuItem(getExporterItemText(), action);
			menu.add(menuItem);
			addCellDesignerPluginMenu(menu);

			/*
			 * Popup menu
			 */
			PluginMenu contextMenu = new PluginMenu(title);
			PluginMenuItem contextMenuItem = new PluginMenuItem(
					getSqueezeContextMenuItemText(), action);
			contextMenu.add(contextMenuItem);
			contextMenuItem = new PluginMenuItem(
					getExportContextMenuItemText(), action);
			contextMenu.add(contextMenuItem);

			addReactionPopupMenuSeparator();
			addReactionPopupMenu(contextMenu);
		} catch (Exception exc) {
			System.err.println("unable to initialize SBMLsqueezer");
			exc.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#addPluginMenu()
	 */
	public void addPluginMenu() {
	}

	/**
	 * 
	 * @return
	 */
	public String getExportContextMenuItemText() {
		return "Export reaction to other format";
	}

	/**
	 * This method returns the text for the menu item that allows the user to
	 * exoport th emodel into another format, such as LaTeX.
	 * 
	 * @return
	 */
	public String getExporterItemText() {
		return "Export model to other format";
	}

	/**
	 * Returns the label of the menu item that points to this plugin.
	 * 
	 * @return Returns the label of the menu item that points to this plugin.
	 */
	public String getMainPluginItemText() {
		return "Squeeze kinetic laws";
	}

	/**
	 * This is the method that returns the label for the item in CellDesigner's
	 * menu that allows to "squeeze" a kinetic equation from a given reaction.
	 * 
	 * @return
	 */
	public String getSqueezeContextMenuItemText() {
		return "Squeeze kinetic law";
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
	 * Starts the SBMLsqueezer dialog window for kinetic law selection or LaTeX
	 * export.
	 * 
	 * @param mode
	 */
	public void startSBMLsqueezerPlugin(String mode) {
		sbmlSqueezer.getSBMLIO().readModel(getSelectedModel());
		if (mode.equals(getMainPluginItemText()))
			(new KineticLawSelectionDialog(null, SBMLsqueezer.getProperties(),
					sbmlSqueezer.getSBMLIO())).setVisible(true);
		else if (mode.equals(getSqueezeContextMenuItemText()))
			new KineticLawSelectionDialog(null, SBMLsqueezer.getProperties(),
					sbmlSqueezer.getSBMLIO(),
					((PluginReaction) getSelectedReactionNode().get(0)).getId());
		else if (mode.equals(getExportContextMenuItemText()))
			new KineticLawSelectionDialog(null, SBMLsqueezer.getProperties(),
					sbmlSqueezer.getSBMLIO().getSelectedModel().getReaction(
							((PluginReaction) getSelectedReactionNode().get(0))
									.getId()));
		else if (mode.equals(getExporterItemText()))
			if (getSelectedModel() != null)
				new KineticLawSelectionDialog(null, SBMLsqueezer
						.getProperties(), sbmlSqueezer.getSBMLIO()
						.getSelectedModel());
	}
}
