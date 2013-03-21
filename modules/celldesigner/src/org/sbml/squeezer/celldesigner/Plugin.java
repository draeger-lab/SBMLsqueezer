/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2013 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.celldesigner;

import java.awt.Dialog;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JDialog;

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;
import jp.sbi.celldesigner.plugin.PluginMenu;
import jp.sbi.celldesigner.plugin.PluginMenuItem;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSBase;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.cdplugin.PluginChangeListener;
import org.sbml.jsbml.cdplugin.PluginSBMLReader;
import org.sbml.jsbml.cdplugin.PluginSBMLWriter;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.gui.KineticLawSelectionDialog;
import org.sbml.squeezer.gui.LaTeXRenderer;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.gui.wizard.KineticLawSelectionWizard;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.tolatex.gui.LaTeXExportDialog;

import de.zbit.gui.GUITools;
import de.zbit.gui.ImageTools;
import de.zbit.gui.prefs.PreferencesDialog;
import de.zbit.io.OpenedFile;
import de.zbit.sbml.gui.SBMLModelSplitPane;
import de.zbit.sbml.io.SBMLfileChangeListener;
import de.zbit.util.ResourceManager;

/**
 * This is the main class for the CellDesigner plugin mode of SBMLsqueezer.
 * 
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @version $Rev$
 * @since 1.0
 */
public class Plugin extends CellDesignerPlugin {
	
	/**
	 * A {@link Logger} for this class.
	 */
	private static final transient Logger logger = Logger.getLogger(Plugin.class.getName());
	
	/**
	 * Localization support.
	 */
	private static final transient ResourceBundle bundle = ResourceManager.getBundle(Plugin.class.getPackage().getName() + ".Messages");
	

	/**
	 * 
	 */
	private SBMLsqueezer sbmlSqueezer;

	/**
	 * Initializes a new SBMLsqueezerPlugin instance.
	 */
	public Plugin() {
		super();
		try {
			/*
			 * Initializing all necessary SysBio and SBMLsqueezer images
			 */
			ImageTools.initImages();
			SBMLsqueezerUI.initImages();
			
			/*
			 * Starting SBMLsqueezer...
			 */
			sbmlSqueezer = new SBMLsqueezer(
					new PluginSBMLReader(SBO.getPossibleEnzymes(SBMLsqueezer
							.getPossibleEnzymeTypes())), new PluginSBMLWriter(
							this));
			sbmlSqueezer.checkForUpdate();
			/*
			 * Initializing CellDesigner's menu entries
			 */
			PluginAction action = new PluginAction(this);
			String title = sbmlSqueezer.getAppName() + ' ' + sbmlSqueezer.getVersionNumber();
			PluginMenu menu = new PluginMenu(title);
			// Squeeze all
			PluginMenuItem menuItem = new PluginMenuItem(Mode.SQUEEZE_ALL.getText(), action);
			menuItem.setToolTipText(Mode.SQUEEZE_ALL.getToolTipText());
			menu.add(menuItem);
			// Export
			menuItem = new PluginMenuItem(Mode.EXPORT_ALL.getText(), action);
			menuItem.setToolTipText(Mode.EXPORT_ALL.getToolTipText());
			menu.add(menuItem);
			// Options
			menuItem = new PluginMenuItem(Mode.CONFIGURE.getText(), action);
			menuItem.setToolTipText(Mode.CONFIGURE.getToolTipText());
			menu.add(menuItem);
			// Help
			menuItem = new PluginMenuItem(Mode.ONLINE_HELP.getText(), action);
			menuItem.setToolTipText(Mode.ONLINE_HELP.getToolTipText());
			menu.add(menuItem);
			addCellDesignerPluginMenu(menu);
			// Debug
			menuItem = new PluginMenuItem(Mode.SHOW_JSBML_MODEL.getText(), action);
			menuItem.setToolTipText(Mode.SHOW_JSBML_MODEL.getToolTipText());
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
			logger.severe(bundle.getString("CANNOT_INITIALIZE_SBMLSQUEEZER"));
			exc.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#addPluginMenu()
	 */
	public void addPluginMenu() {
	}

	/* (non-Javadoc)
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#modelClosed(jp.sbi.celldesigner.plugin.PluginSBase)
	 */
	public void modelClosed(PluginSBase arg0) {
	}

	/* (non-Javadoc)
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#modelOpened(jp.sbi.celldesigner.plugin.PluginSBase)
	 */
	public void modelOpened(PluginSBase arg0) {
	}

	/* (non-Javadoc)
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#modelSelectChanged(jp.sbi.celldesigner.plugin.PluginSBase)
	 */
	public void modelSelectChanged(PluginSBase arg0) {
	}

	/* (non-Javadoc)
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#SBaseAdded(jp.sbi.celldesigner.plugin.PluginSBase)
	 */
	public void SBaseAdded(PluginSBase arg0) {
	}

	/* (non-Javadoc)
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#SBaseChanged(jp.sbi.celldesigner.plugin.PluginSBase)
	 */
	public void SBaseChanged(PluginSBase arg0) {
	}

	/* (non-Javadoc)
	 * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#SBaseDeleted(jp.sbi.celldesigner.plugin.PluginSBase)
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
		ONLINE_HELP,
		/**
		 * Just for debugging purposes.
		 */
		SHOW_JSBML_MODEL;

		/**
		 * A human-readable label for each mode.
		 * 
		 * @return
		 */
		public String getText() {
			String key = toString();
			if (bundle.containsKey(key)) {
				return bundle.getString(key);
			}
			return bundle.getString("DEFAULT_OPTION");
		}

		/**
		 * A short description of the action.
		 * 
		 * @return
		 */
		public String getToolTipText() {
			String key = toString() + "_TOOLTIP";
			if (bundle.containsKey(key)) {
				return bundle.getString(key);
			}
			return null;
		}

		/**
		 * Returns the mode for the given label or {@code null}.
		 * 
		 * @param text
		 * @return
		 */
		public static Mode getMode(String text) {
			for (Mode m : values()) {
				if (text.equals(m.getText())) {
					return m;
				}
			}
			return null;
		}
	}

	/**
	 * Starts the SBMLsqueezer dialog window for kinetic law selection or LaTeX
	 * export.
	 * 
	 * @param mode
	 */
	public void startSBMLsqueezerPlugin(String mode) {
		try {
			SBMLio io = sbmlSqueezer.getSBMLIO();
			Model convertedModel = io.convertModel(getSelectedModel());
			SBMLDocument doc = convertedModel.getSBMLDocument();
			convertedModel.addTreeNodeChangeListener(new PluginChangeListener(doc, this));
			switch (Mode.getMode(mode)) {
				case SQUEEZE_ALL:
					KineticLawSelectionWizard wizard = new KineticLawSelectionWizard(null, io);
					wizard.showModalDialog();
					wizard.isKineticsAndParametersStoredInSBML();
					break;
				case SQUEEZE_REACTION:
					try {
						new KineticLawSelectionDialog(null, io, ((PluginReaction) getSelectedReactionNode().get(0)).getId());
					} catch (Throwable exc) {
						GUITools.showErrorMessage(null, exc);
					}
					break;
				case CONFIGURE:
					PreferencesDialog.showPreferencesDialog(SBMLsqueezer.getInteractiveConfigOptionsArray());
					break;
				case EXPORT_REACTION:
					new LaTeXExportDialog((Dialog) null, convertedModel.getReaction(
						((PluginReaction) getSelectedReactionNode().get(0)).getId()));
					break;
				case EXPORT_ALL:
					if (getSelectedModel() != null) {
						new LaTeXExportDialog((Dialog) null, convertedModel);
					} else {
						logger.warning(bundle.getString("NO_SELECTED_MODEL_AVAILABLE"));
					}
					break;
				case ONLINE_HELP:
					new SBMLsqueezerUI(io, null).showOnlineHelp();
					break;
				case SHOW_JSBML_MODEL:
					JDialog d = new JDialog();
					d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					d.setTitle(bundle.getString("INTERNAL_DATA_STRUCTURE"));
					OpenedFile<SBMLDocument> openedFile = new OpenedFile<SBMLDocument>(doc);
					doc.addTreeNodeChangeListener(new SBMLfileChangeListener(openedFile));
					SBMLModelSplitPane split = new SBMLModelSplitPane(openedFile, true);
					split.setEquationRenderer(new LaTeXRenderer());
					d.getContentPane().add(split);
					d.pack();
					d.setLocationRelativeTo(null);
					d.setModal(true);
					d.setVisible(true);
					break;
				default:
					logger.warning(MessageFormat.format(bundle.getString("UNSUPPORTED_ACTION"), mode));
					break;
			}
		} catch (Throwable t) {
			String message = Arrays.toString(t.getStackTrace()).replace(',', '\n');
			GUITools.showErrorMessage(null, t, message.substring(1, message.length() - 1));
		}
	}

}
