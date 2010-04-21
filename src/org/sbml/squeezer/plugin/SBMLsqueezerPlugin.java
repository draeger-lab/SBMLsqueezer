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

import java.util.Properties;

import javax.swing.JOptionPane;

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;
import jp.sbi.celldesigner.plugin.PluginMenu;
import jp.sbi.celldesigner.plugin.PluginMenuItem;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSBase;

import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBO;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.gui.KineticLawSelectionDialog;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.gui.SettingsDialog;
import org.sbml.squeezer.io.SBMLio;

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
							.getPossibleEnzymeTypes())), new PluginSBMLWriter(
							this));
			sbmlSqueezer.checkForUpdate(true);
			/*
			 * Initializing CellDesigner's menu entries
			 */
			SBMLsqueezerPluginAction action = new SBMLsqueezerPluginAction(this);
			String title = "SBMLsqueezer " + SBMLsqueezer.getVersionNumber();
			PluginMenu menu = new PluginMenu(title);
			// Squeeze all
			PluginMenuItem menuItem = new PluginMenuItem(Mode.SQUEEZE_ALL
					.getText(), action);
			menu.add(menuItem);
			// Export
			menuItem = new PluginMenuItem(Mode.EXPORT_ALL.getText(), action);
			menu.add(menuItem);
			// Options
			menuItem = new PluginMenuItem(Mode.CONFIGURE.getText(), action);
			menu.add(menuItem);
			// Help
			menuItem = new PluginMenuItem(Mode.ONLINE_HELP.getText(), action);
			menu.add(menuItem);
			addCellDesignerPluginMenu(menu);

			/*
			 * Popup menu
			 */
			PluginMenu contextMenu = new PluginMenu(title);
			PluginMenuItem contextMenuItem = new PluginMenuItem(
					Mode.SQUEEZE_REACTION.getText(), action);
			contextMenu.add(contextMenuItem);
			contextMenuItem = new PluginMenuItem(
					Mode.EXPORT_REACTION.getText(), action);
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
	 * 
	 * @author Andreas Dr&auml;ger <a
	 *         href="mailto:andreas.draeger@uni-tuebingen.de"
	 *         >andreas.draeger@uni-tuebingen.de</a>
	 * @date 2009-10-22
	 */
	public enum Mode {
		/**
		 * Returns the label of the menu item that points to this plug-in.
		 */
		SQUEEZE_ALL,
		/**
		 * This is the method that returns the label for the item in
		 * CellDesigner's menu that allows to "squeeze" a kinetic equation from
		 * a given reaction.
		 */
		SQUEEZE_REACTION,
		/**
		 * Command to change the configuration of SBMLsqueezer (start the option
		 * menu).
		 */
		CONFIGURE,
		/**
		 * Export a single reaction int another format.
		 */
		EXPORT_REACTION,
		/**
		 * This method returns the text for the menu item that allows the user
		 * to export the model into another format, such as LaTeX.
		 */
		EXPORT_ALL,
		/**
		 * Show the online help.
		 */
		ONLINE_HELP;

		/**
		 * A human-readable label for each mode.
		 * 
		 * @return
		 */
		public String getText() {
			switch (this) {
			case SQUEEZE_ALL:
				return "Squeeze kinetic laws";
			case SQUEEZE_REACTION:
				return "Squeeze kinetic law";
			case CONFIGURE:
				return "Preferences";
			case EXPORT_REACTION:
				return "Export reaction to other format";
			case EXPORT_ALL:
				return "Export model to other format";
			case ONLINE_HELP:
				return "Help";
			default:
				return "invalid option";
			}
		}

		/**
		 * Returns the mode for the given label or null.
		 * 
		 * @param text
		 * @return
		 */
		public static Mode getMode(String text) {
			for (Mode m : values())
				if (text.equals(m.getText()))
					return m;
			return null;
		}
	}

	/**
	 * Starts the SBMLsqueezer dialog window for kinetic law selection or LaTeX
	 * export.
	 * 
	 * @param mode
	 * @throws SBMLException 
	 */
	public void startSBMLsqueezerPlugin(String mode) throws SBMLException {
		SBMLio io = sbmlSqueezer.getSBMLIO();
		Properties p = SBMLsqueezer.getProperties();
		io.readModel(getSelectedModel());
		KineticLawSelectionDialog klsd;
		switch (Mode.getMode(mode)) {
		case SQUEEZE_ALL:
			klsd = new KineticLawSelectionDialog(null, p, io);
			klsd.setVisible(true);
			break;
		case SQUEEZE_REACTION:
			klsd = new KineticLawSelectionDialog(null, p, io,
					((PluginReaction) getSelectedReactionNode().get(0)).getId());
			break;
		case CONFIGURE:
			SettingsDialog dialog = new SettingsDialog(null);
			if (dialog.showSettingsDialog((Properties) SBMLsqueezer
					.getProperties().clone()) == SettingsDialog.APPROVE_OPTION)
				for (Object key : dialog.getSettings().keySet())
					SBMLsqueezer.getProperties().put(key,
							dialog.getSettings().get(key));
			break;
		case EXPORT_REACTION:
			klsd = new KineticLawSelectionDialog(null, p, io.getSelectedModel()
					.getReaction(
							((PluginReaction) getSelectedReactionNode().get(0))
									.getId()));
			break;
		case EXPORT_ALL:
			if (getSelectedModel() != null)
				klsd = new KineticLawSelectionDialog(null, p, io
						.getSelectedModel());
			else
				System.err.println("no selected model available");
			break;
		case ONLINE_HELP:
			SBMLsqueezerUI.showOnlineHelp(null, null);
		default:
			System.err.println("unsuported action");
			break;
		}
		try {
			SBMLsqueezer.saveProperties(p);
		} catch (Exception exc) {
			exc.printStackTrace();
			JOptionPane.showMessageDialog(null, exc.getMessage(), exc
					.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
		}
	}
}
