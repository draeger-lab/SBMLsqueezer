/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016 by the University of Tuebingen, Germany.
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
package org.sbml.squeezer.gui;

import static de.zbit.util.Utils.getMessage;

import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.ext.qual.QualConstants;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.jsbml.util.ProgressListener;
import org.sbml.squeezer.ConsistencyReportBuilder;
import org.sbml.squeezer.OptionsGeneral;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.functionTermGenerator.FunctionTermSelectionDialog;
import org.sbml.squeezer.functionTermGenerator.wizard.FunctionTermGeneratorWizard;
import org.sbml.squeezer.gui.wizard.KineticLawSelectionWizard;
import org.sbml.squeezer.io.IOOptions;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.SqSBMLReader;
import org.sbml.squeezer.sabiork.wizard.SABIORKWizard;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.LaTeXOptions.PaperSize;
import org.sbml.tolatex.gui.LaTeXExportDialog;
import org.sbml.tolatex.gui.SBML2LaTeXGUI;
import org.sbml.tolatex.io.LaTeXReportGenerator;
import org.sbml.tolatex.io.TextExport;

import de.zbit.AppConf;
import de.zbit.garuda.GarudaActions;
import de.zbit.garuda.GarudaFileSender;
import de.zbit.garuda.GarudaGUIfactory;
import de.zbit.garuda.GarudaOptions;
import de.zbit.garuda.GarudaSoftwareBackend;
import de.zbit.gui.BaseFrame;
import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.JTabbedPaneDraggableAndCloseable;
import de.zbit.gui.TabClosingListener;
import de.zbit.gui.actioncommand.ActionCommand;
import de.zbit.io.FileTools;
import de.zbit.io.OpenedFile;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.sbml.gui.SBMLModelSplitPane;
import de.zbit.sbml.gui.SBMLNode;
import de.zbit.sbml.gui.SBMLReadingTask;
import de.zbit.sbml.gui.SBMLTree;
import de.zbit.text.HTMLFormatter;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;

/**
 * The central class for SBMLsqueezer's graphical user interface. This class has
 * actually nothing to do with earlier versions of SBMLsqueezerUI because the
 * class that had this name before has now become the
 * {@link KineticLawSelectionDialog}. This UI class provides several additional
 * features: It displays all model components in a
 * {@link JTabbedPaneDraggableAndCloseable} so that multiple models can be opened at
 * the same time and can be closed easily and it provides several options to
 * manipulate the model. Each tab in this tabbed pane contains a
 * {@link SBMLModelSplitPane} showing the details of the selected model. This
 * class contains the enumeration {@link Command} that contains the names of all
 * possible actions this UI can start. As the
 * {@link SBMLsqueezerUI#actionPerformed(ActionEvent)} is a public method, any
 * one of the given commands can be passed through {@link ActionEvent} objects
 * to this UI class and will therefore start the given action.
 *
 * @author Andreas Dr&auml;ger
 * @since 1.0
 */
public class SBMLsqueezerUI extends BaseFrame implements ActionListener,
        ChangeListener, PropertyChangeListener, TabClosingListener {

    /**
     * A {@link Logger} for this class.
     */
    private static final transient Logger logger = Logger.getLogger(SBMLsqueezerUI.class.getName());
    /**
     * Localization support.
     */
    private static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
    /**
     * Localization support.
     */
    private static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);

    /**
     * This is what the graphical user interface of SBMLsqueezer can do... (2009-09-11)
     *
     * @author Andreas Dr&auml;ger
     * @since 1.3
     */
    public static enum Command implements ActionCommand {
        /**
         * SABIO-RK
         */
        SABIO_RK,
        /**
         * Generate kinetic equations or default function terms.
         */
        SQUEEZE,
        /**
         * Convert the current model or the current SBML object into a LaTeX
         * report.
         */
        TO_LATEX;

        /* (non-Javadoc)
         * @see de.zbit.gui.ActionCommand#getName()
         */
        @Override
        public String getName() {
            return MESSAGES.getString(name());
        }

        /* (non-Javadoc)
         * @see de.zbit.gui.ActionCommand#getToolTip()
         */
        @Override
        public String getToolTip() {
            return MESSAGES.getString(name() + "_TOOLTIP");
        }
    }

    /**
     *
     */
    public static void initImages() {
        LaTeXExportDialog.initImages();
        String iconPaths[] = {
                "ICON_FORWARD.png",
                "ICON_LEFT_ARROW.png",
                "ICON_LOGO_SMALL.png",
                "ICON_SABIO-RK_16.png",
                "ICON_ZOOM_IN_16.png",
                "ICON_ZOOM_OUT_16.png",
                "SBMLsqueezerIcon_256.png",
                "SBMLsqueezerIcon_128.png",
                "SBMLsqueezerIcon_48.png",
                "SBMLsqueezerIcon_64.png",
                "SBMLsqueezerIcon_32.png",
                "SBMLsqueezerIcon_16.png",
                "SBMLsqueezerWatermark.png"
        };
        String prefix = "resources/img/";
        for (String path : iconPaths) {
            URL url = SBMLsqueezer.class.getResource(prefix + path);
            if (url != null) {
                String key = path.substring(0, path.lastIndexOf('.'));
                UIManager.put(key, new ImageIcon(url));
                if (UIManager.getIcon(key) == null) {
                    logger.warning(MessageFormat.format("COULD_NOT_LOAD_IMAGE", prefix + path));
                }
            } else {
                logger.warning(MessageFormat.format("INVALID_URL", prefix + path));
            }
        }
    }

    /**
     * Generated serial version uid.
     */
    private static final long serialVersionUID = 5461285676322858005L;

    /**
     * Checks if the model loaded as the last one contains any errors or
     * warnings.
     */
    public static final void checkForSBMLErrors(Component parent, Model model,
                                                List<SBMLException> excl, boolean showWarnings) {
        if ((excl.size() > 0) && showWarnings) {
            ConsistencyReportBuilder reportBuilder = new ConsistencyReportBuilder();
            reportBuilder.setFormatter(new HTMLFormatter());
            JEditorPane area = new JEditorPane("text/html", reportBuilder.format(excl));
            area.setEditable(false);
            area.setBackground(Color.WHITE);
            JScrollPane scroll = new JScrollPane(area);
            scroll.setPreferredSize(new Dimension(450, 200));
            JOptionPane.showMessageDialog(parent, scroll, WARNINGS.getString("SBML_WARNINGS"),
                    JOptionPane.WARNING_MESSAGE);
            if (model == null) {
                GUITools.showErrorMessage(parent, WARNINGS.getString("UNABLE_TO_LOAD_MODEL"));
            }
        }
    }

    /**
     * Manages all models, storage, loading and selecting models.
     */
    private SBMLio<?> sbmlIO;
    /**
     *
     */
    private JTabbedPaneDraggableAndCloseable tabbedPane;

    /**
     *
     */
    private SBPreferences prefs;

    /**
     * A reference to the Garuda core.
     */
    private GarudaSoftwareBackend garudaBackend;

    static {
        initImages();
    }

    /**
     * @param io
     * @param appConf
     */
    public SBMLsqueezerUI(SBMLio<?> io, AppConf appConf) {
        super(appConf);
        prefs = SBPreferences.getPreferencesFor(OptionsGeneral.class);
        sbmlIO = io;
        GUITools.setEnabled(false, getJMenuBar(), getJToolBar(), Command.SQUEEZE, Command.SABIO_RK, Command.TO_LATEX);
        int[] resolutions = new int[]{16, 32, 48, 128, 256};
        List<Image> icons = new LinkedList<Image>();
        for (int res : resolutions) {
            Object icon = UIManager.get("SBMLsqueezerIcon_" + res);
            if ((icon != null) && (icon instanceof ImageIcon)) {
                icons.add(((ImageIcon) icon).getImage());
            }
        }
        setIconImages(icons);
        tabbedPane.addChangeListener(sbmlIO);
        List<OpenedFile<SBMLDocument>> listOfOpenedFiles = sbmlIO.getListOfOpenedFiles();
        for (OpenedFile<SBMLDocument> file : listOfOpenedFiles) {
            Model m = file.getDocument().getModel();
            checkForSBMLErrors(this, m, sbmlIO.getWarnings(), prefs
                    .getBoolean(OptionsGeneral.SHOW_SBML_WARNINGS));
            if (m != null) {
                addModel(file);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(GarudaActions.SENT_TO_GARUDA.toString())) {
            OpenedFile<SBMLDocument> openedFile = sbmlIO.getSelectedOpenedFile();
            File file = openedFile.getFile();
            if (openedFile.isChanged()) {
                try {
                    file = File.createTempFile(FileTools.trimExtension(file.getName()), '.' + FileTools.getExtension(file.getName()));
                    file.deleteOnExit();
                    sbmlIO.writeSelectedModelToSBML(file.getAbsolutePath());
                } catch (IOException exc) {
                    GUITools.showErrorMessage(this, exc);
                }
            }
            GarudaFileSender sender = new GarudaFileSender(this, garudaBackend, file, "sbml");
            sender.execute();
        } else {
            SBMLModelSplitPane split = (SBMLModelSplitPane) tabbedPane.getSelectedComponent();

            switch (Command.valueOf(e.getActionCommand())) {
                case SABIO_RK:
                    SBMLDocument sbmlDoc = sbmlIO.getSelectedModel().getSBMLDocument();
                    SBPreferences prefs = SBPreferences.getPreferencesFor(OptionsGeneral.class);
                    if (e.getSource() instanceof Reaction) {
                        SABIORKWizard.getResultGUI(this,
                                ModalityType.APPLICATION_MODAL, sbmlDoc, ((Reaction) e.getSource()).getId());
                    } else {
                        SABIORKWizard.getResultGUI(this, ModalityType.APPLICATION_MODAL, sbmlDoc, (prefs.getBoolean(OptionsGeneral.OVERWRITE_EXISTING_RATE_LAWS)));
                    }
                    try {
                        split.updateUI();
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                    break;
                case SQUEEZE:
                    boolean changesStoredInSBML = false;
                    if (e.getSource() instanceof Reaction) {
                        // single reaction
                        try {
                            KineticLawSelectionDialog klsd = new KineticLawSelectionDialog(this, sbmlIO, ((Reaction) e.getSource()).getId());
                            changesStoredInSBML = klsd.isKineticsAndParametersStoredInSBML();
                        } catch (Throwable exc) {
                            GUITools.showErrorMessage(this, exc);
                        }
                    } else if (e.getSource() instanceof Transition) {
                        // single transition
                        try {
                            FunctionTermSelectionDialog ftsd = new FunctionTermSelectionDialog(this, sbmlIO,
                                    ((Transition) e.getSource()).getId());
                            changesStoredInSBML = ftsd.areFunctionTermsStoredInSBML();
                        } catch (Throwable exc) {
                            GUITools.showErrorMessage(this, exc);
                        }
                    }
                    else {
                        Model model = sbmlIO.getSelectedModel();
                        QualModelPlugin qm = (QualModelPlugin)(model.getPlugin(QualConstants.shortLabel));
                        boolean isQuan = model.getReactionCount() > 0;
                        boolean isQual = qm.getTransitionCount() > 0;
                        if (isQuan && isQual) {
                            // whole mixed model
                            GeneratorSelectionDialog dialog = new GeneratorSelectionDialog(this, sbmlIO);
                            dialog.setVisible(true);
                            changesStoredInSBML = dialog.areChangesStoredInSBML();
                        }
                        else if(isQuan) {
                            // whole quantitative model
                            KineticLawSelectionWizard wizard = new KineticLawSelectionWizard(this, sbmlIO);
                            wizard.getDialog().addWindowListener(EventHandler.create(WindowListener.class, this,
                                    "windowClosed", ""));
                            wizard.showModalDialog();
                            changesStoredInSBML = wizard.isKineticsAndParametersStoredInSBML();
                        }
                        else if(isQual) {
                            // whole qualitative model
                            FunctionTermGeneratorWizard wizard = new FunctionTermGeneratorWizard(this, sbmlIO);
                            wizard.getDialog().addWindowListener(EventHandler.create(WindowListener.class, this,
                                    "windowClosed", ""));
                            wizard.showModalDialog();
                            changesStoredInSBML = wizard.areFunctionTermsStoredInSBML();
                        }
                        else {
                            // neither qualitative nor quantitative model
                            JOptionPane.showMessageDialog(null, "<html>" +
                                    MESSAGES.getString("NO_QUAL_OR_QUAN_DIALOG_1") + "<br>" +
                                    MESSAGES.getString("NO_QUAL_OR_QUAN_DIALOG_2") + "</html>");
                        }
                    }
                    if (changesStoredInSBML) {
                        try {
                            split.updateUI();
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    }
                    break;
                case TO_LATEX:
                    if (e.getSource() instanceof Reaction) {
                        new SBML2LaTeXGUI(this, (Reaction) e.getSource());
                    }
                    else if (e.getSource() instanceof Model) {
                        new SBML2LaTeXGUI(this, ((Model) e.getSource()).getParentSBMLObject());
                    } else {
                        if (sbmlIO.getSelectedModel() != null) {
                            new SBML2LaTeXGUI(this, sbmlIO.getSelectedModel().getParentSBMLObject());
                        } else {
                            SBPreferences guiPrefs = SBPreferences.getPreferencesFor(GUIOptions.class);
                            String dir = guiPrefs.get(GUIOptions.OPEN_DIR);
                            File out = GUITools.saveFileDialog(this, dir, false, false,
                                    JFileChooser.FILES_ONLY, SBFileFilter.createTeXFileFilter());
                            if (out != null) {
                                String path = out.getParent();
                                if (!path.equals(dir)) {
                                    guiPrefs.put(GUIOptions.OPEN_DIR, path);
                                    try {
                                        guiPrefs.flush();
                                    } catch (BackingStoreException exc) {
                                        GUITools.showErrorMessage(this, exc);
                                    }
                                }
                                if (!out.exists()
                                        || GUITools.overwriteExistingFile(this, out)) {
                                    writeLaTeX(out);
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    /**
     * Adds the given new model into the {@link JTabbedPane} on the main panel.
     *
     * @param openedFile
     */
    private void addModel(OpenedFile<SBMLDocument> openedFile) {
        SBMLModelSplitPane split = new SBMLModelSplitPane(openedFile, true);
        split.setEquationRenderer(new LaTeXRenderer());

        openedFile.addPropertyChangeListener(OpenedFile.FILE_CONTENT_CHANGED_EVENT, this);

        String title = Integer.toString(tabbedPane.getTabCount() + 1);
        Model m = null;
        if (openedFile.getDocument() != null) {
            SBMLDocument doc = openedFile.getDocument();
            if (doc.isSetModel()) {
                m = doc.getModel();
                if (m.isSetName()) {
                    title = m.getName();
                } else if (m.isSetId()) {
                    title = m.getId();
                }
            }
        }
        sbmlIO.getListOfOpenedFiles().add(openedFile);
        tabbedPane.add(title, split);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
        setupContextMenu(split, null, null);
        if (sbmlIO.getSelectedModel().getReactionCount() > 0) {
            GUITools.setEnabled(true, getJMenuBar(), getJToolBar(), Command.SQUEEZE, Command.SABIO_RK, Command.TO_LATEX);
        }
        if (((QualModelPlugin) sbmlIO.getSelectedModel().getPlugin(QualConstants.shortLabel)).getTransitionCount() > 0) {
            GUITools.setEnabled(true, getJMenuBar(), getJToolBar(), Command.SQUEEZE);
            GUITools.setEnabled(false, getJMenuBar(), getJToolBar(), Command.SABIO_RK, Command.TO_LATEX);
        }
        if (garudaBackend != null) {
            GUITools.setEnabled(true, getJMenuBar(), getJToolBar(), GarudaActions.SENT_TO_GARUDA);
        }

        // Save location of the file for convenience:
        try {
            String path = openedFile.getFile().getAbsolutePath();
            String oldPath = prefs.get(IOOptions.SBML_IN_FILE);
            if (!path.equals(oldPath)) {
                prefs.put(IOOptions.SBML_IN_FILE, path);
                prefs.flush();
            }
        } catch (BackingStoreException exc) {
            GUITools.showErrorMessage(this, exc);
        }
    }


    /**
     * @param split
     */
    @SuppressWarnings("unchecked")
    private void setupContextMenu(SBMLModelSplitPane split, TreePath path, TreePath selectionPath) {
        SBMLTree tree = split.getTree();

        if (path != null) {
            int i;
            ArrayList<TreeNode> p = new ArrayList<TreeNode>(path.getPathCount());
            for (i = 0; i < path.getPathCount(); i++) {
                SBMLNode node = (SBMLNode) path.getPathComponent(i);
                p.add(node.getUserObject());
            }
            ArrayList<TreeNode> selected = new ArrayList<TreeNode>(selectionPath.getPathCount());
            for (i = 0; i < selectionPath.getPathCount(); i++) {
                SBMLNode node = (SBMLNode) selectionPath.getPathComponent(i);
                selected.add(node.getUserObject());
            }
            tree.expandAll(p, true, null, false, false, selected);
        }

        tree.addActionListener(this);
        JMenuItem sabioItem = GUITools.createJMenuItem(tree, Command.SABIO_RK, UIManager.getIcon("ICON_SABIO-RK_16"));
        JMenuItem squeezeItem = GUITools.createJMenuItem(tree, Command.SQUEEZE, UIManager.getIcon("SBMLsqueezerIcon_16"));
        JMenuItem latexItem = GUITools.createJMenuItem(tree, Command.TO_LATEX, UIManager.getIcon("ICON_LATEX_16"));

        if (sbmlIO.getSelectedModel().getReactionCount() > 0) {
            tree.addPopupMenuItem(sabioItem, Reaction.class, Model.class, SBMLDocument.class);
            tree.addPopupMenuItem(squeezeItem, Reaction.class, Model.class, SBMLDocument.class);
            tree.addPopupMenuItem(latexItem, Reaction.class, Model.class, SBMLDocument.class);
        }
        else if(((QualModelPlugin) sbmlIO.getSelectedModel().getPlugin(QualConstants.shortLabel)).getTransitionCount() > 0) {
            tree.addPopupMenuItem(sabioItem);
            tree.addPopupMenuItem(squeezeItem, Transition.class, Model.class, SBMLDocument.class);
            tree.addPopupMenuItem(latexItem);
        }
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#additionalEditMenuItems()
     */
    @Override
    protected JMenuItem[] additionalEditMenuItems() {
        List<JMenuItem> items = new ArrayList<JMenuItem>(4);
        items.add(GUITools.createJMenuItem(this,
                Command.SABIO_RK, UIManager.getIcon("ICON_SABIO-RK_16")));
        items.add(GUITools.createJMenuItem(this,
                Command.SQUEEZE, UIManager.getIcon("SBMLsqueezerIcon_16"),
                KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK)));
        items.add(GUITools.createJMenuItem(this,
                Command.TO_LATEX, UIManager.getIcon("ICON_LATEX_16"),
                KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK)));
        return items.toArray(new JMenuItem[0]);
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#additionalFileMenuItems()
     */
    @Override
    protected JMenuItem[] additionalFileMenuItems() {
        if (!appConf.getCmdArgs().containsKey(GarudaOptions.CONNECT_TO_GARUDA)
                || appConf.getCmdArgs().getBoolean(GarudaOptions.CONNECT_TO_GARUDA)) {
            return new JMenuItem[]{GarudaGUIfactory.createGarudaMenu(this)};
        }
        return super.additionalFileMenuItems();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource().equals(tabbedPane)) {
            if (tabbedPane.getTabCount() == 0) {
                GUITools.setEnabled(false, getJMenuBar(), getJToolBar(), BaseAction.FILE_SAVE, BaseAction.FILE_SAVE_AS,
                        BaseAction.FILE_CLOSE, Command.SQUEEZE, Command.SABIO_RK, Command.TO_LATEX, GarudaActions.SENT_TO_GARUDA);
            } else {
                OpenedFile<SBMLDocument> selectedFile = sbmlIO.getSelectedOpenedFile();
                if (sbmlIO.getSelectedModel().getReactionCount() > 0) {
                    GUITools.setEnabled(true, getJMenuBar(), getJToolBar(), Command.SQUEEZE, Command.SABIO_RK, Command.TO_LATEX);
                }
                if (((QualModelPlugin) sbmlIO.getSelectedModel().getPlugin(QualConstants.shortLabel)).getTransitionCount() > 0) {
                    GUITools.setEnabled(true, getJMenuBar(), getJToolBar(), Command.SQUEEZE);
                    GUITools.setEnabled(false, getJMenuBar(), getJToolBar(), Command.SABIO_RK, Command.TO_LATEX);
                }
                if (selectedFile != null) {
                    setFileStateMark(selectedFile);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see  java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
     */
    public void windowClosed(WindowEvent we) {
        if (we.getWindow() instanceof JDialog) {
            if (we.getWindow() instanceof KineticLawSelectionDialog) {
                KineticLawSelectionDialog klsd = (KineticLawSelectionDialog) we.getWindow();
                if (klsd.isKineticsAndParametersStoredInSBML()) {
                    SBMLModelSplitPane split = (SBMLModelSplitPane) tabbedPane.getSelectedComponent();
                    try {
                        split.init(sbmlIO.getSelectedModel().getSBMLDocument(), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Write the selected model into a LaTeX file.
     *
     * @param out
     */
    private void writeLaTeX(final File out) {
        try {
            SBPreferences lprefs = SBPreferences
                    .getPreferencesFor(LaTeXOptions.class);
            LaTeXReportGenerator export = new LaTeXReportGenerator(lprefs
                    .getBoolean(LaTeXOptions.LANDSCAPE), lprefs
                    .getBoolean(LaTeXOptions.TYPEWRITER),
                    lprefs.getShort(LaTeXOptions.FONT_SIZE),
                    PaperSize.valueOf(lprefs.getString(LaTeXOptions.PAPER_SIZE)),
                    lprefs.getBoolean(LaTeXOptions.SHOW_PREDEFINED_UNITS),
                    lprefs.getBoolean(LaTeXOptions.TITLE_PAGE), lprefs
                    .getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE));
            export.toLaTeX(sbmlIO.getSelectedModel(), out);
            // new Thread(new Runnable() {
            //
            // public void run() {
            // try {
            // Desktop.getDesktop().open(out);
            // } catch (IOException e) {
            // // e.printStackTrace();
            // }
            // }
            // }).start();
        } catch (Exception exc) {
            GUITools.showErrorMessage(this, exc);
        }
    }

    /**
     * Write the selected model into an SBML file.
     *
     * @param out
     */
    private void writeSBML(File out) {
        try {
            sbmlIO.writeSelectedModelToSBML(out.getAbsolutePath());
        } catch (Exception exc) {
            GUITools.showErrorMessage(this, exc);
        }
    }

    /**
     * Writes the currently selected model into an ASCII text file.
     *
     * @param out
     */
    private void writeText(final File out) {
        try {
            new TextExport(sbmlIO.getSelectedModel(), out);
            // new Thread(new Runnable() {
            //
            // public void run() {
            // try {
            // Desktop.getDesktop().edit(out);
            // } catch (IOException e) {
            // // e.printStackTrace();
            // }
            // }
            // }).start();

        } catch (Exception exc) {
            GUITools.showErrorMessage(this, exc);
        }
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#closeFile()
     */
    @Override
    public boolean closeFile() {
        int oldTabCount = tabbedPane.getTabCount();
        int index = tabbedPane.getSelectedIndex();
        if (tabClosing(index)) {
            try {
                tabbedPane.remove(index);
            } catch (Throwable t) {
                GUITools.showErrorMessage(this, t);
                tabbedPane.removeAll();
            }
        }
        return tabbedPane.getTabCount() != oldTabCount;
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.TabClosingListener#tabClosing(int)
     */
    @Override
    public boolean tabClosing(int index) {
        int choice = JOptionPane.NO_OPTION;
        OpenedFile<SBMLDocument> openedFile = sbmlIO.getOpenedFile(index);
        if (openedFile.isChanged()) {
            choice = JOptionPane.showConfirmDialog(this,
                    StringUtil.toHTML(MessageFormat.format(MESSAGES.getString("SAVE_BEFORE_CLOSING"), openedFile.getFile().getName()), 40),
                    MESSAGES.getString("SAVE_BEFORE_CLOSING_TITLE"),
                    JOptionPane.YES_NO_CANCEL_OPTION);
        }

        File savedFile = null;
        if (choice == JOptionPane.YES_OPTION) {
            savedFile = saveFile();
            if (savedFile != null) {
                openedFile.setChanged(false);
            }
        } else if ((choice != JOptionPane.CANCEL_OPTION) && (choice != JOptionPane.CLOSED_OPTION)) {
            setFileStateMark(null);
        }
        if ((savedFile != null) || (choice == JOptionPane.NO_OPTION)) {
            return true;
        }
        return (choice != JOptionPane.CANCEL_OPTION) && (choice != JOptionPane.CLOSED_OPTION);
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#createJToolBar()
     */
    @Override
    protected JToolBar createJToolBar() {
        return createDefaultToolBar();
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#createMainComponent()
     */
    @Override
    protected Component createMainComponent() {
        Icon icon = UIManager.getIcon("SBMLsqueezerWatermark");
        tabbedPane = new JTabbedPaneDraggableAndCloseable((ImageIcon) icon);
        tabbedPane.addChangeListener(this);
        tabbedPane.addTabClosingListener(this);
        return tabbedPane;
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#exit()
     */
    @Override
    public void exit() {
        int i = 0;
        boolean veto = true, changed = false;
        List<OpenedFile<SBMLDocument>> listOfOpenedFiles = sbmlIO.getListOfOpenedFiles();
        for (OpenedFile<SBMLDocument> openedFile : listOfOpenedFiles) {
            if (openedFile.isChanged()) {
                veto &= !tabClosing(i);
                changed = true;
            }
            i++;
        }
        if (changed && veto) {
            return;
        }
        StringBuilder exception = new StringBuilder();
        if (appConf.getInteractiveOptions() != null) {
            for (Class<? extends KeyProvider> clazz : appConf.getInteractiveOptions()) {
                SBPreferences prefs = SBPreferences.getPreferencesFor(clazz);
                try {
                    prefs.flush();
                } catch (Exception exc) {
                    exception.append(getMessage(exc));
                    exception.append('\n');
                }
            }
            if (exception.length() > 0) {
                GUITools.showErrorMessage(this, exception.toString());
            }
        }
        dispose();
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#getURLAboutMessage()
     */
    @Override
    public URL getURLAboutMessage() {
        return SBMLsqueezer.class.getResource(MESSAGES.getString("URL_ABOUT_MESSAGE"));
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#getURLLicense()
     */
    @Override
    public URL getURLLicense() {
        return SBMLsqueezer.class.getResource(MESSAGES.getString("URL_LICENSE_FILE"));
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#getURLOnlineHelp()
     */
    @Override
    public URL getURLOnlineHelp() {
        return SBMLsqueezer.class.getResource(MESSAGES.getString("URL_ONLINE_HELP"));
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#openFile(de.zbit.sbml.io.OpenedFile<org.sbml.jsbml.SBMLDocument>[])
     */
    @Override
    public File[] openFile(File... files) {
        if ((files == null) || (files.length == 0)) {
            files = GUITools.openFileDialog(
                    this,
                    prefs.get(OPEN_DIR),
                    false,
                    true,
                    JFileChooser.FILES_ONLY,
                    SBFileFilter.createSBMLFileFilterList()
            );
        }
        if (files != null) {
            boolean usesJSBML = sbmlIO.getReader() instanceof SqSBMLReader;
            for (final File file : files) {
                if (usesJSBML) {
                    try {
                        SBMLReadingTask reader = new SBMLReadingTask(file, this, new PropertyChangeListener() {
                            /* (non-Javadoc)
                             * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
                             */
                            @Override
                            @SuppressWarnings("unchecked")
                            public void propertyChange(PropertyChangeEvent evt) {
                                if (evt.getPropertyName().equals(SBMLReadingTask.SBML_READING_SUCCESSFULLY_DONE)) {
                                    OpenedFile<SBMLDocument> openFile = (OpenedFile<SBMLDocument>) evt.getNewValue();
                                    addModel(openFile);
                                }
                            }
                        });
                        reader.execute();
                    } catch (FileNotFoundException exc) {
                        GUITools.showErrorMessage(this, exc);
                    }
                } else {
                    final SBMLsqueezerUI ui = this;
                    final boolean showWarnings = SBPreferences.getPreferencesFor(OptionsGeneral.class).getBoolean(OptionsGeneral.SHOW_SBML_WARNINGS);

                    SwingWorker<SBMLDocument, Void> worker = new SwingWorker<SBMLDocument, Void>() {

                        /* (non-Javadoc)
                         * @see javax.swing.SwingWorker#doInBackground()
                         */
                        @Override
                        protected SBMLDocument doInBackground() throws Exception {
                            sbmlIO.setListener(new ProgressListener() {
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
                                 * @see org.sbml.jsbml.util.ProgressListener#progressUpdate(int, java.lang.String)
                                 */
                                @Override
                                public void progressUpdate(int progress, String message) {
                                    setProgress(progress * 100 / total);
                                    if (message != null) {
                                        logger.fine(message);
                                    }
                                }

                                /* (non-Javadoc)
                                 * @see org.sbml.jsbml.util.ProgressListener#progressFinish()
                                 */
                                @Override
                                public void progressFinish() {
                                    setProgress(100);
                                }

                            });
                            SBMLDocument doc = sbmlIO.convertSBMLDocument(file);
                            return doc;
                        }

                        /* (non-Javadoc)
                         * @see javax.swing.SwingWorker#done()
                         */
                        @Override
                        protected void done() {
                            try {
                                SBMLDocument doc = get();
                                if (doc != null) {
                                    SBMLsqueezerUI.checkForSBMLErrors(ui, doc.getModel(), sbmlIO.getWarnings(), showWarnings);
                                    addModel(new OpenedFile<SBMLDocument>(file, doc));
                                }
                            } catch (Exception exc) {
                                GUITools.showErrorMessage(ui, exc);
                            }
                        }
                    };
                    final ProgressMonitor progressMonitor = new ProgressMonitor(null,
                            MESSAGES.getString("BUILDING_DATA_STRUCTURES"), "", 0, 100);
                    worker.addPropertyChangeListener(
                            new PropertyChangeListener() {
                                /* (non-Javadoc)
                                 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
                                 */
                                @Override
                                public void propertyChange(PropertyChangeEvent evt) {
                                    if ("progress".equals(evt.getPropertyName())) {
                                        progressMonitor.setProgress((Integer) evt.getNewValue());
                                    }
                                }
                            });
                    worker.execute();
                }
            }
        }
        return files;
    }

    /**
     * @param of
     */
    private void save(final OpenedFile<SBMLDocument> of) {
        final File out = of.getFile();
        if (SBFileFilter.hasFileType(out, SBFileFilter.FileType.SBML_FILES)) {
            new Thread(new Runnable() {
                /* (non-Javadoc)
                 * @see java.lang.Runnable#run()
                 */
                @Override
                public void run() {
                    writeSBML(out);
                    of.setChanged(false);
                }
            }).start();
        } else if (SBFileFilter.createTeXFileFilter().accept(out)) {
            writeLaTeX(out);
        } else if (SBFileFilter.createTextFileFilter().accept(out)) {
            writeText(out);
        }
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#saveFile()
     */
    @Override
    public File saveFile() {
        OpenedFile<SBMLDocument> savedFile = sbmlIO.getSelectedOpenedFile();
        logger.info(savedFile.getFile().getAbsolutePath());

        if (savedFile != null) {
            save(savedFile);
        }

        return savedFile.getFile();
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#saveFileAs()
     */
    @Override
    public File saveFileAs() {
        File savedFile = null;
        SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
        SBFileFilter filterText = SBFileFilter.createTextFileFilter();
        SBFileFilter filterTeX = SBFileFilter.createTeXFileFilter();
        SBFileFilter filterSBML = SBFileFilter.createSBMLFileFilter();
        JFileChooser chooser = GUITools.createJFileChooser(prefs
                        .get(GUIOptions.SAVE_DIR), false, false,
                JFileChooser.FILES_ONLY, filterSBML, filterTeX, filterText);
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            SBFileFilter selectedFileFilter = (SBFileFilter) chooser.getFileFilter();
            String filepath = chooser.getSelectedFile().getPath();
            logger.info(filepath);
            if (!(filepath.endsWith(".xml") || filepath.endsWith(".sbml") || filepath.endsWith(".tex") || filepath.endsWith(".txt"))) {
                if (selectedFileFilter.equals(filterSBML)) {
                    filepath += ".xml";
                } else if (selectedFileFilter.equals(filterTeX)) {
                    filepath += ".tex";
                } else if (selectedFileFilter.equals(filterText)) {
                    filepath += ".txt";
                }
            }
            final File out = new File(filepath);
            savedFile = out;
            if (out.getParentFile() != null) {
                prefs.put(GUIOptions.SAVE_DIR, out.getParentFile()
                        .getAbsolutePath());
                try {
                    prefs.flush();
                } catch (BackingStoreException exc) {
                    GUITools.showErrorMessage(this, exc);
                }
            }
            OpenedFile<SBMLDocument> of = sbmlIO.getSelectedOpenedFile();
            of.setFile(out);
            if (!out.exists() || GUITools.overwriteExistingFile(this, out)) {
                save(of);
            }
        }
        return savedFile;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (propName.equals(OpenedFile.FILE_CONTENT_CHANGED_EVENT)) {
            setFileStateMark(sbmlIO.getSelectedOpenedFile());
        } else {
            if (propName.equals(GarudaSoftwareBackend.GARUDA_ACTIVATED)) {
                garudaBackend = (GarudaSoftwareBackend) evt.getNewValue();
                if (sbmlIO.getListOfOpenedFiles().size() > 0) {
                    GUITools.setEnabled(true, getJMenuBar(), getJToolBar(), GarudaActions.SENT_TO_GARUDA);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see de.zbit.gui.BaseFrame#showSaveMenuEntry()
     */
    @Override
    protected boolean showsSaveMenuEntry() {
        return true;
    }

}
