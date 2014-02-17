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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import jp.sbi.celldesigner.plugin.PluginListOf;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.celldesigner.PluginChangeListener;
import org.sbml.jsbml.util.ProgressListener;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.gui.KineticLawSelectionDialog;
import org.sbml.squeezer.gui.LaTeXRenderer;
import org.sbml.squeezer.gui.wizard.KineticLawSelectionWizard;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.gui.SBML2LaTeXGUI;

import de.zbit.AppConf;
import de.zbit.gui.GUITools;
import de.zbit.gui.JHelpBrowser;
import de.zbit.gui.prefs.PreferencesDialog;
import de.zbit.io.OpenedFile;
import de.zbit.sbml.gui.SBMLModelSplitPane;
import de.zbit.sbml.io.SBMLfileChangeListener;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 2.0
 */
public class PluginWorker extends SwingWorker<SBMLDocument, Void> {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(PluginWorker.class.getName());
  
  /**
   * Localization support.
   */
  private static final transient ResourceBundle bundle = ResourceManager.getBundle(PluginWorker.class.getPackage().getName() + ".Messages");
  
  private Plugin plugin;
  private Mode mode;
  private PluginListOf reactionNode;
  
  /**
   * 
   * @param plugin
   * @param mode
   */
  public PluginWorker(Plugin plugin, Mode mode) {
    this.plugin = plugin;
    this.mode = mode;
    
    if ((this.mode == Mode.EXPORT_REACTION) || (this.mode == Mode.SQUEEZE_REACTION)) {
      // This is to avoid that users click on something else while the document is converted...
      reactionNode = plugin.getSelectedReactionNode();
    }
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected SBMLDocument doInBackground() throws Exception {
    if ((mode == Mode.CONFIGURE) || (mode == Mode.ONLINE_HELP)) {
      return null;
    }
    logger.fine(bundle.getString("CONVERTING_DATA_STRUCTURE"));
    SBMLio<PluginModel> sbmlIo = plugin.getSBMLsqueezer().getSBMLIO();
    sbmlIo.setListener(new ProgressListener() {
      
      /**
       * Total number of expected calls.
       */
      private int total;
      
      /* (non-Javadoc)
       * @see org.sbml.jsbml.util.ProgressListener#progressStart(int)
       */
      @Override
      public void progressStart(int total) {
        this.total = total;
      }
      
      /* (non-Javadoc)
       * @see org.sbml.jsbml.util.ProgressListener#progressUpdate(int)
       */
      @Override
      public void progressUpdate(int progress) {
        setProgress(progress * 100/total);
      }
      
      /* (non-Javadoc)
       * @see org.sbml.jsbml.util.ProgressListener#progressFinish()
       */
      @Override
      public void progressFinish() {
        setProgress(100);
      }
      
    });
    Model convertedModel = sbmlIo.convertModel(plugin.getSelectedModel());
    SBMLDocument doc = convertedModel.getSBMLDocument();
    doc.addTreeNodeChangeListener(new PluginChangeListener(plugin));
    return doc;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void done() {
    try {
      SBMLDocument doc = get();
      switch (mode) {
        case SQUEEZE_ALL:
          logger.fine(bundle.getString("SQUEEZING_ALL"));
          KineticLawSelectionWizard wizard = new KineticLawSelectionWizard(null, plugin.getSBMLsqueezer().getSBMLIO());
          wizard.showModalDialog();
          wizard.isKineticsAndParametersStoredInSBML();
          break;
        case SQUEEZE_REACTION:
          logger.fine(MessageFormat.format(bundle.getString(""), reactionNode.get(0)));
          try {
            new KineticLawSelectionDialog(null, plugin.getSBMLsqueezer().getSBMLIO(), ((PluginReaction) reactionNode.get(0)).getId());
          } catch (Throwable exc) {
            GUITools.showErrorMessage(null, exc);
          }
          break;
        case CONFIGURE:
          logger.fine(bundle.getString("CONFIGURATION"));
          PreferencesDialog.showPreferencesDialog(SBMLsqueezer.getInteractiveConfigOptionsArray());
          break;
        case EXPORT_REACTION:
          PluginReaction reaction = (PluginReaction) reactionNode.get(0);
          logger.fine(MessageFormat.format(bundle.getString("SBML2LATEX_FOR_REACTION"), reaction.getId()));
          new SBML2LaTeXGUI(doc.getModel().getReaction(reaction.getId()));
          break;
        case EXPORT_ALL:
          logger.fine(bundle.getString("SBML2LATEX_FOR_DOCUMENT"));
          new SBML2LaTeXGUI(doc);
          break;
        case ONLINE_HELP:
          AppConf appConf = plugin.getSBMLsqueezer().getAppConf();
          ResourceBundle resources = ResourceManager
              .getBundle(StringUtil.RESOURCE_LOCATION_FOR_LABELS);
          JHelpBrowser.showOnlineHelp(null, null, String.format(
            resources.getString("ONLINE_HELP_FOR_THE_PROGRAM"),
            appConf.getApplicationName() + " " + appConf.getVersionNumber()),
            SBMLsqueezer.class.getResource(
              ResourceManager.getBundle(Bundles.MESSAGES).getString("URL_ONLINE_HELP")),
              appConf.getCmdOptions());
          break;
        case SHOW_JSBML_MODEL:
          JDialog d = new JDialog();
          d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
      if (t.getCause() != null) {
        message += '\n' + t.getCause().getLocalizedMessage() + '\n';
        message += Arrays.toString(t.getCause().getStackTrace()).replace(',', '\n');
      }
      GUITools.showErrorMessage(null, t, message.substring(1, message.length() - 1));
    }
    super.done();
  }
  
}
