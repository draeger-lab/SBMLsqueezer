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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.ProgressMonitor;

import de.zbit.util.ResourceManager;

/**
 * This class starts SBMLsqueezer from as a CellDesigner plug-in. Actually, this
 * class only forwards commands from CellDesigner to the plugin class that
 * really starts SBMLsqueezer.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.0
 */
public class PluginAction extends jp.sbi.celldesigner.plugin.PluginAction {
  
  /**
   * A {@link Logger} for this class.
   */
  private static transient final Logger logger = Logger.getLogger(PluginAction.class.getName());
  
  /**
   * Localization support.
   */
  private static transient final ResourceBundle bundle = ResourceManager.getBundle(PluginAction.class.getPackage().getName() + ".Messages");
  
  /**
   * A serial version number.
   */
  private static final long serialVersionUID = 4134514954192751545L;
  
  /**
   * An instance of the CellDesigner plug-in.
   */
  private Plugin plugin;
  
  /**
   * 
   * @param sbmlSqueezerPlugin
   */
  public PluginAction(Plugin sbmlSqueezerPlugin) {
    plugin = sbmlSqueezerPlugin;
  }
  
  /**
   * Runs two threads for checking if an update for SBMLsqueezer is available
   * and for initializing an instance of the
   * {@link org.sbml.squeezer.gui.SBMLsqueezerUI}.
   * 
   * @param e
   */
  @Override
  public void myActionPerformed(ActionEvent e) {
    if (e.getSource() instanceof JMenuItem) {
      String item = ((JMenuItem) e.getSource()).getText();
      PluginWorker worker = new PluginWorker(plugin, Mode.getMode(item));
      final ProgressMonitor progressMonitor = new ProgressMonitor(null,
        bundle.getString("BUILDING_DATA_STRUCTURES"), "", 0, 100);
      worker.addPropertyChangeListener(
        new PropertyChangeListener() {
          /* (non-Javadoc)
           * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
           */
          @Override
          public  void propertyChange(PropertyChangeEvent evt) {
            if ("progress".equals(evt.getPropertyName())) {
              progressMonitor.setProgress((Integer) evt.getNewValue());
            }
          }
        });
      worker.execute();
    } else {
      logger.warning(MessageFormat.format(
        bundle.getString("UNSUPPORTED_SOURCE_OF_ACTION"),
        e.getSource().getClass().getName()));
    }
  }
  
}
