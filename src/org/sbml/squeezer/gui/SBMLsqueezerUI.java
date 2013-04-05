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
package org.sbml.squeezer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Image;
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

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.squeezer.OptionsGeneral;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.gui.wizard.KineticLawSelectionWizard;
import org.sbml.squeezer.io.IOOptions;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.SqSBMLReader;
import org.sbml.squeezer.sabiork.wizard.SABIORKWizard;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.LaTeXOptions.PaperSize;
import org.sbml.tolatex.gui.LaTeXExportDialog;
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
	
	private static final transient Logger logger = Logger.getLogger(SBMLsqueezerUI.class.getName());
	private static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
	private static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);

	/**
	 * This is what the graphical user interface of SBMLsqueezer can do...
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2009-09-11
	 * @since 1.3
	 */
	public static enum Command implements ActionCommand {
		/**
		 * SABIO-RK
		 */
		SABIO_RK,
		/**
		 * Generate kinetic equations.
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
		public String getName() {
			return MESSAGES.getString(name());
		}

		/* (non-Javadoc)
		 * @see de.zbit.gui.ActionCommand#getToolTip()
		 */
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
				"SBMLsqueezerLogo_256.png",
				"SBMLsqueezerLogo_128.png",
				"SBMLsqueezerLogo_64.png",
				"SBMLsqueezerLogo_16.png",
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
	public static final void checkForSBMLErrors(Component parent, Model m,
			List<SBMLException> excl, boolean showWarnings) {
		if (excl.size() > 0 && showWarnings) {
			StringBuilder warnings = new StringBuilder();
			for (SBMLException exc : excl) {
				warnings.append("<p>");
				warnings.append(exc.getMessage().replace("<", "&lt;").replace(">", "&gt;"));
				warnings.append("</p>");
			}
			JEditorPane area = new JEditorPane("text/html", StringUtil.toHTML(warnings.toString()));
			area.setEditable(false);
			area.setBackground(Color.WHITE);
			JScrollPane scroll = new JScrollPane(area);
			scroll.setPreferredSize(new Dimension(450, 200));
			JOptionPane.showMessageDialog(parent, scroll, WARNINGS.getString("SBML_WARNINGS"),
					JOptionPane.WARNING_MESSAGE);
			if (m == null) {
				GUITools.showErrorMessage(parent, WARNINGS.getString("UNABLE_TO_LOAD_MODEL"));
			}
		}
	}

	/**
	 * Manages all models, storage, loading and selecting models.
	 */
	private SBMLio sbmlIO;
	/**
	 * 
	 */
	private JTabbedPaneDraggableAndCloseable tabbedPane;

	/**
	 * 
	 */
	private SBPreferences prefs;
	
	/**
	 * 
	 */
	private GarudaSoftwareBackend garudaBackend;

	static {
		initImages();
	}
	
	/**
	 * 
	 * @param io
	 * @param appConf 
	 */
	public SBMLsqueezerUI(SBMLio io, AppConf appConf) {
		super(appConf);
		this.prefs = SBPreferences.getPreferencesFor(OptionsGeneral.class);
		this.sbmlIO = io;
		GUITools.setEnabled(false, getJMenuBar(), getJToolBar(), Command.SQUEEZE, Command.SABIO_RK, Command.TO_LATEX);
		int[] resolutions=new int[]{16, 32, 48, 128, 256};
		List<Image> icons = new LinkedList<Image>();
		for (int res: resolutions) {
		  Object icon = UIManager.get("SBMLsqueezerIcon_" + res);
		  if ((icon != null) && (icon instanceof ImageIcon)) {
		    icons.add(((ImageIcon) icon).getImage());
		  }
		}
		setIconImages(icons);
		tabbedPane.addChangeListener(sbmlIO);
		for (OpenedFile<SBMLDocument> file : sbmlIO.getListOfOpenedFiles()) {
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
			GarudaFileSender sender = new GarudaFileSender(this, garudaBackend, file, "SBML");
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
					boolean kineticsAndParametersStoredInSBML = false;
					if (e.getSource() instanceof Reaction) {
						// single reaction
						try {
							KineticLawSelectionDialog klsd = new KineticLawSelectionDialog(this, sbmlIO, ((Reaction) e.getSource()).getId());
							kineticsAndParametersStoredInSBML = klsd.isKineticsAndParametersStoredInSBML();
						} catch (Throwable exc) {
							GUITools.showErrorMessage(this, exc);
						}
					} else {
						// whole model
						KineticLawSelectionWizard wizard = new KineticLawSelectionWizard(this, sbmlIO);
						wizard.getDialog().addWindowListener(EventHandler.create(WindowListener.class, this, "windowClosed", ""));
						wizard.showModalDialog();
						kineticsAndParametersStoredInSBML = wizard.isKineticsAndParametersStoredInSBML();
					}
					if (kineticsAndParametersStoredInSBML) {
						try {
							split.updateUI();
						} catch (Exception exc) {
							exc.printStackTrace();
						}
					}
					break;
				case TO_LATEX:
					if (e.getSource() instanceof Reaction) {
						new LaTeXExportDialog(this, (Reaction) e.getSource());
					} else if (e.getSource() instanceof Model) {
						new LaTeXExportDialog(this, (Model) e.getSource());
					} else {
						SBPreferences guiPrefs = SBPreferences
								.getPreferencesFor(GUIOptions.class);
						String dir = guiPrefs.get(GUIOptions.OPEN_DIR);
						File out = GUITools.saveFileDialog(this, dir, false, false,
							JFileChooser.FILES_ONLY, SBFileFilter
							.createTeXFileFilter());
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
					break;
				default:
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
		
		setupContextMenu(split, null, null);
		
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
		if (m != null) {
			m.putUserObject(SBMLio.ORIGINAL_MODEL_KEY, m);
		}
		tabbedPane.add(title, split);
		tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
		GUITools.setEnabled(true,  getJMenuBar(), getJToolBar(), Command.SQUEEZE, Command.SABIO_RK, Command.TO_LATEX);
		if (this.garudaBackend != null) {
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
	 * 
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
		JMenuItem squeezeItem = GUITools.createJMenuItem(tree, Command.SQUEEZE, UIManager.getIcon("SBMLsqueezerLogo_16")); 
		JMenuItem latexItem = GUITools.createJMenuItem(tree,  Command.TO_LATEX, UIManager.getIcon("ICON_LATEX_16"));
		
		tree.addPopupMenuItem(sabioItem, Reaction.class, Model.class, SBMLDocument.class);
		tree.addPopupMenuItem(squeezeItem, Reaction.class, Model.class, SBMLDocument.class);
		tree.addPopupMenuItem(latexItem, Reaction.class, Model.class, SBMLDocument.class);
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
					Command.SQUEEZE, UIManager.getIcon("SBMLsqueezerLogo_16"),
					KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK)));
		items.add(GUITools.createJMenuItem(this,
					Command.TO_LATEX, UIManager.getIcon("ICON_LATEX_16"),
					KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK)));
		if (!appConf.getCmdArgs().containsKey(GarudaOptions.CONNECT_TO_GARUDA)
				|| appConf.getCmdArgs().getBoolean(GarudaOptions.CONNECT_TO_GARUDA)) {
			items.add(GarudaGUIfactory.createGarudaMenu(this));
		}
		return items.toArray(new JMenuItem[0]);
	}

	/**
	 * Reads a model from the given file and adds it to the GUI if possible.
	 * 
	 * @param file
	 */
	private void readModel(File file) {
		try {
			Model model = sbmlIO.convertModel(file.getAbsolutePath());
			checkForSBMLErrors(this, model, sbmlIO.getWarnings(), 
				prefs.getBoolean(OptionsGeneral.SHOW_SBML_WARNINGS));
			if (model != null) {
				addModel(new OpenedFile<SBMLDocument>(file, model.getSBMLDocument()));
			}
		} catch (Exception exc) {
			GUITools.showErrorMessage(this, exc);
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(tabbedPane)) {
			if (tabbedPane.getTabCount() == 0) {
				GUITools.setEnabled(false, getJMenuBar(), getJToolBar(), BaseAction.FILE_SAVE, BaseAction.FILE_SAVE_AS,
					BaseAction.FILE_CLOSE, Command.SQUEEZE, Command.SABIO_RK, Command.TO_LATEX, GarudaActions.SENT_TO_GARUDA);
			} else {
				OpenedFile<SBMLDocument> selectedFile = sbmlIO.getSelectedOpenedFile();
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
	protected JToolBar createJToolBar() {
		return  createDefaultToolBar();
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#createMainComponent()
	 */
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
		for (OpenedFile<SBMLDocument> openedFile : sbmlIO.getListOfOpenedFiles()) {
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
					exception.append(exc.getLocalizedMessage());
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
	public URL getURLAboutMessage() {
		return SBMLsqueezer.class.getResource(MESSAGES.getString("URL_ABOUT_MESSAGE"));
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLLicense()
	 */
	public URL getURLLicense() {
		return SBMLsqueezer.class.getResource(MESSAGES.getString("URL_LICENSE_FILE"));
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLOnlineHelp()
	 */
	public URL getURLOnlineHelp() {
		return SBMLsqueezer.class.getResource(MESSAGES.getString("URL_ONLINE_HELP"));
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#openFile(de.zbit.sbml.io.OpenedFile<org.sbml.jsbml.SBMLDocument>[])
	 */
	public File[] openFile(File... files) {
		SBPreferences prefs = SBPreferences.getPreferencesFor(getClass());
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
							@SuppressWarnings("unchecked")
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getPropertyName().equals(SBMLReadingTask.SBML_READING_SUCCESSFULLY_DONE)) {
									addModel((OpenedFile<SBMLDocument>) evt.getNewValue());
								}
							}
						});
						reader.execute();
					} catch (FileNotFoundException exc) {
						GUITools.showErrorMessage(this, exc);
					}
				} else {
					final SBMLsqueezerUI reader = this;
					final File f = file;
					(new Thread(new Runnable() {
						    /* (non-Javadoc)
						     * @see java.lang.Thread#run()
						     */
						    //@Override
						    public void run() {
							    reader.readModel(f);
						    }
					    }
					  )
					).start();
				}
			}
		}
		return files;
	}

	/**
	 * 
	 * @param of
	 */
	private void save(final OpenedFile<SBMLDocument> of) {
		final File out = of.getFile();
		if (SBFileFilter.hasFileType(out, SBFileFilter.FileType.SBML_FILES)) {
			new Thread(new Runnable() {
				/* (non-Javadoc)
				 * @see java.lang.Runnable#run()
				 */
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
			final File out = chooser.getSelectedFile();
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
	// @Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propName = evt.getPropertyName(); 
		if (propName.equals(OpenedFile.FILE_CONTENT_CHANGED_EVENT)) {
			setFileStateMark(sbmlIO.getSelectedOpenedFile());
		} else if (propName.equals(GarudaSoftwareBackend.GARUDA_ACTIVATED)) {
			this.garudaBackend = (GarudaSoftwareBackend) evt.getNewValue();
			if (sbmlIO.getListOfOpenedFiles().size() > 0) {
				GUITools.setEnabled(true, getJMenuBar(), getJToolBar(), GarudaActions.SENT_TO_GARUDA);
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
