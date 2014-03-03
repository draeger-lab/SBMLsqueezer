/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2014 by the University of Tuebingen, Germany.
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

import java.util.ResourceBundle;
import java.util.logging.Logger;

import jp.sbi.celldesigner.plugin.PluginMenu;
import jp.sbi.celldesigner.plugin.PluginMenuItem;
import jp.sbi.celldesigner.plugin.PluginModel;

import org.sbml.jsbml.SBO;
import org.sbml.jsbml.celldesigner.AbstractCellDesignerPlugin;
import org.sbml.jsbml.celldesigner.PluginSBMLReader;
import org.sbml.jsbml.celldesigner.PluginSBMLWriter;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.gui.SBMLsqueezerUI;

import de.zbit.gui.ImageTools;
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
public class Plugin extends AbstractCellDesignerPlugin {
  
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
  private SBMLsqueezer<PluginModel> sbmlSqueezer;
  
  /**
   * Initializes a new SBMLsqueezerPlugin instance.
   */
  public Plugin() {
    super();
    try {
      
      // Initialize CellDesigner/JSBML communication interface
      PluginSBMLReader reader = new PluginSBMLReader(
        SBO.getPossibleEnzymes(SBMLsqueezer.getPossibleEnzymeTypes()));
      PluginSBMLWriter writer = new PluginSBMLWriter(this);
      
      // Launch SBMLsqueezer
      SBMLsqueezer.setSABIORKEnabled(false);
      sbmlSqueezer = new SBMLsqueezer<PluginModel>(reader, writer);
      sbmlSqueezer.checkForUpdate();
      
      // Initializing all necessary SysBio and SBMLsqueezer images
      ImageTools.initImages();
      SBMLsqueezerUI.initImages();
      
      // Initializing CellDesigner's menu entries
      addPluginMenu();
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      exc.getCause().printStackTrace();
      System.out.println(exc.toString());
      logger.severe(bundle.getString("CANNOT_INITIALIZE_SBMLSQUEEZER"));
    }
  }
  
  /* (non-Javadoc)
   * @see jp.sbi.celldesigner.plugin.CellDesignerPlug#addPluginMenu()
   */
  @Override
  public void addPluginMenu() {
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
  }
  
  /**
   * 
   * @return
   */
  public SBMLsqueezer<PluginModel> getSBMLsqueezer() {
    return sbmlSqueezer;
  }
  
}