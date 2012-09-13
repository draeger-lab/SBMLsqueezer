/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
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
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.SubmodelController;
import org.sbml.squeezer.gui.wizard.KineticLawSelectionWizard;
import org.sbml.squeezer.io.IOOptions;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.SqSBMLReader;
import org.sbml.squeezer.resources.Resource;
import org.sbml.squeezer.sabiork.wizard.SABIORKWizard;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.gui.LaTeXExportDialog;
import org.sbml.tolatex.io.LaTeXReportGenerator;
import org.sbml.tolatex.io.TextExport;

import de.zbit.AppConf;
import de.zbit.gui.BaseFrame;
import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.JTabbedPaneCloseListener;
import de.zbit.gui.JTabbedPaneDraggableAndCloseable;
import de.zbit.gui.JTabbedPaneDraggableAndCloseable.TabCloseEvent;
import de.zbit.gui.actioncommand.ActionCommand;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.sbml.gui.SBMLModelSplitPane;
import de.zbit.sbml.gui.SBMLNode;
import de.zbit.sbml.gui.SBMLReadingTask;
import de.zbit.sbml.gui.SBMLTree;
import de.zbit.sbml.io.OpenedFile;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;

/**
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.3
 * @version $Rev$
 */
class FileReaderThread extends Thread implements Runnable {
	/**
	 * 
	 */
	private File file;
	/**
	 * 
	 */
	private SBMLsqueezerUI reader;

	/**
	 * 
	 * @param ui
	 * @param f
	 */
	public FileReaderThread(SBMLsqueezerUI ui, File file) {
		super();
		this.reader = ui;
		this.file = file;
		start();
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		reader.readModel(file);
	}

}

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
		ChangeListener, JTabbedPaneCloseListener, PropertyChangeListener {
	
	private static final transient Logger logger = Logger.getLogger(SBMLsqueezerUI.class.getName());
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
	public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);

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
				"ICON_DIAGRAM_TINY.png",
				"ICON_DOWN_ARROW.png",
				"ICON_DOWN_ARROW_TINY.png",
				"ICON_FORWARD.png",
				"ICON_GEAR_TINY.png",
				"ICON_LEFT_ARROW.png",
				"ICON_LEMON_SMALL.png",
				"ICON_LEMON_TINY.png",
				"ICON_LOGO_SMALL.png",
				"ICON_PICTURE_TINY.png",
				"ICON_RIGHT_ARROW.png",
				"ICON_RIGHT_ARROW_TINY.png",
				"ICON_SABIO-RK_16.png",
				"ICON_STABILITY_SMALL.png",
				"ICON_STRUCTURAL_MODELING_TINY.png",
				"IMAGE_LEMON.png"
		    };
	    for (String path : iconPaths) {
	      URL u = Resource.class.getResource("img/" + path);
	      if (u != null) {
	        UIManager.put(path.substring(0, path.lastIndexOf('.')), new ImageIcon(u));
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
	 * Default background color.
	 */
	private Color colorDefault;

	/**
	 * 
	 */
	private JLabel logo;

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
		this.prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
		this.sbmlIO = io;
		setEnabled(false, Command.SQUEEZE, Command.SABIO_RK, Command.TO_LATEX);
		setSBMLsqueezerBackground();
		// TODO
		// setIconImage(UIManager.getIcon("IMAGE_LEMON"));
		tabbedPane.addChangeListener(sbmlIO);
		for (OpenedFile<SBMLDocument> file : sbmlIO.getListOfOpenedFiles()) {
			Model m = file.getDocument().getModel();
			checkForSBMLErrors(this, m, sbmlIO.getWarnings(), prefs
					.getBoolean(SqueezerOptions.SHOW_SBML_WARNINGS));
			if (m != null) {
				addModel(file);
			}
		}
	}

  /* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		switch (Command.valueOf(e.getActionCommand())) {
			case SABIO_RK:
				if (e.getSource() instanceof Reaction) {
					SBMLDocument result = SABIORKWizard.getResultGUI(this,
						ModalityType.APPLICATION_MODAL, sbmlIO.getSelectedModel()
								.getSBMLDocument(), ((Reaction) e.getSource()).getId());
				} else {
					SubmodelController controller = new SubmodelController(sbmlIO.getSelectedModel());
					SBMLDocument result = SABIORKWizard.getResultGUI(this, ModalityType.APPLICATION_MODAL, controller.getSubmodel().getSBMLDocument());
					for(Reaction r: result.getModel().getListOfReactions()) {
						if(r.isSetKineticLaw()) {
							controller.storeKineticLaw(r.getKineticLaw(), true);
						}
					}
					SABIORKWizard.getResultGUI(this, ModalityType.APPLICATION_MODAL, controller.getSubmodel().getSBMLDocument());
					// TODO store results controller.store...
				}
				break;
			case SQUEEZE:
				boolean KineticsAndParametersStoredInSBML = false;
				if (e.getSource() instanceof Reaction) {
					// single reaction
					try {
						KineticLawSelectionDialog klsd = new KineticLawSelectionDialog(this, sbmlIO, ((Reaction) e.getSource()).getId());
						KineticsAndParametersStoredInSBML = klsd.isKineticsAndParametersStoredInSBML();
					} catch (Throwable exc) {
						GUITools.showErrorMessage(this, exc);
					}
				} else {
					// whole model
					KineticLawSelectionWizard wizard;
					wizard = new KineticLawSelectionWizard(this, sbmlIO);
					wizard.getDialog().addWindowListener(EventHandler.create(WindowListener.class, this, "windowClosed", ""));
					wizard.showModalDialog();
					KineticsAndParametersStoredInSBML = wizard.isKineticsAndParametersStoredInSBML();
				}
				if (KineticsAndParametersStoredInSBML) {
					SBMLModelSplitPane split = (SBMLModelSplitPane) tabbedPane.getSelectedComponent();
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

	/**
	 * Adds the given new model into the tabbed pane on the main panel.
	 * 
	 * @param openedFile
	 */
	private void addModel(OpenedFile<SBMLDocument> openedFile) {
		SBMLModelSplitPane split;
		try {
			split = new SBMLModelSplitPane(openedFile, true);
			split.setEquationRenderer(new HotEquationRenderer());
			
			split.getOpenedFile().addPropertyChangeListener("openedFileChanged", this);

			setupContextMenu(split, null, null);
			
			tabbedPane.add(openedFile.getDocument().getModel().getId(), split);
			tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
			setEnabled(true, BaseAction.FILE_SAVE_AS, BaseAction.FILE_CLOSE,
					Command.SQUEEZE, Command.SABIO_RK, Command.TO_LATEX);
		} catch (Exception e) {
			e.printStackTrace();
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
		JMenuItem squeezeItem = GUITools.createJMenuItem(tree, Command.SQUEEZE, UIManager.getIcon("ICON_LEMON_TINY")); 
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
		// TODO: Not in this version
		// editMenu.addSeparator();
		// editMenu.add(GUITools.createMenuItem("Analyze Stability",
		// Command.CHECK_STABILITY, this, GUITools.ICON_STABILITY_SMALL));
		// editMenu.add(GUITools.createMenuItem("Structural Kinetic Modelling",
		// Command.STRUCTURAL_KINETIC_MODELLING, this,
		// GUITools.ICON_STRUCTURAL_MODELING_TINY));
		// editMenu.add(GUITools.createMenuItem("Simulation", Command.SIMULATE,
		// 'S', this, GUITools.ICON_DIAGRAM_TINY));

		return new JMenuItem[] {
				GUITools.createJMenuItem(this,
					Command.SABIO_RK, UIManager.getIcon("ICON_SABIO-RK_16")),
				GUITools.createJMenuItem(this,
					Command.SQUEEZE, UIManager.getIcon("ICON_LEMON_TINY"),
					KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK)),
				GUITools.createJMenuItem(this,
					Command.TO_LATEX, UIManager.getIcon("ICON_LATEX_16"),
					KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK)),
		};
	}

	/**
	 * Reads a model from the given file and adds it to the GUI if possible.
	 * 
	 * @param file
	 */
	void readModel(File file) {
		try {
			// TODO: initialize statusBar and hide it again
			Model model = sbmlIO.convertModel(file.getAbsolutePath());
			checkForSBMLErrors(this, model, sbmlIO.getWarnings(), 
				prefs.getBoolean(SqueezerOptions.SHOW_SBML_WARNINGS));
			if (model != null) {
				addModel(model, file);
			}
		} catch (Exception exc) {
			GUITools.showErrorMessage(this, exc);
		}
	}

	/**
	 * 
	 * @param model
	 * @param file
	 * @throws BackingStoreException
	 */
	private void addModel(Model model, File file) throws BackingStoreException {
		OpenedFile<SBMLDocument> openedFile = new OpenedFile<SBMLDocument>(file);
		openedFile.setDocument(model.getSBMLDocument());
		sbmlIO.getListOfOpenedFiles().add(openedFile);
		addModel(openedFile);
		String path = file.getAbsolutePath();
		String oldPath = prefs.get(IOOptions.SBML_IN_FILE);
		if (!path.equals(oldPath)) {
			prefs.put(IOOptions.SBML_IN_FILE, path);
			prefs.flush();
		}
	}

	/**
	 * Enables or disables actions that can be performed by SBMLsqueezer, i.e.,
	 * all menu items and buttons that are associated with the given actions are
	 * enabled or disabled.
	 * 
	 * @param state
	 *            if true buttons, items etc. are enabled, otherwise disabled.
	 * @param commands
	 */
	private void setEnabled(boolean state, Object... commands) {
		GUITools.setEnabled(state, getJMenuBar(), toolBar, commands);
	}

	/**
	 * 
	 */
	private void setSBMLsqueezerBackground() {
		if (tabbedPane.getComponentCount() == 0) {
			getContentPane().remove(tabbedPane);
			getContentPane().setBackground(Color.WHITE);
			getContentPane().add(logo, BorderLayout.CENTER);
		} else {
			getContentPane().setBackground(colorDefault);
			getContentPane().remove(logo);
			getContentPane().add(tabbedPane, BorderLayout.CENTER);
		}
		getContentPane().validate();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(tabbedPane)) {
			if (tabbedPane.getComponentCount() == 0) {
				setEnabled(false, BaseAction.FILE_SAVE, BaseAction.FILE_SAVE_AS,
					BaseAction.FILE_CLOSE, Command.SQUEEZE, Command.SABIO_RK,
					Command.TO_LATEX);
			} else {
				if (sbmlIO.getSelectedOpenedFile() != null) {
					setEnabled(sbmlIO.getSelectedOpenedFile().isChanged(), BaseAction.FILE_SAVE);
				}
			}
			setSBMLsqueezerBackground();
		}
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.JTabbedPaneCloseListener#tabAboutToBeClosed(de.zbit.gui.JTabbedPaneDraggableAndCloseable.TabCloseEvent)
	 */
	public boolean tabClosing(TabCloseEvent evt0) {
		boolean change = false;
		int choice = JOptionPane.showConfirmDialog(this, 
				"Would you like to save the document?",
			    "Save and close",
			    JOptionPane.YES_NO_CANCEL_OPTION); 
		
		File savedFile = null;
		if (choice == 0) {
			savedFile = saveFile();
		}
		if (savedFile != null || choice == 1) {
			change = (tabbedPane.getComponentCount() > 0);
			if (tabbedPane.getComponentCount() == 0) {
				setEnabled(false, BaseAction.FILE_SAVE, BaseAction.FILE_SAVE_AS,
					BaseAction.FILE_CLOSE, Command.SQUEEZE, Command.SABIO_RK,
					Command.TO_LATEX);
			} else {
				setEnabled(sbmlIO.getSelectedOpenedFile().isChanged(), BaseAction.FILE_SAVE);
			}
		}
		
		return change;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.JTabbedPaneCloseListener#tabClosed(de.zbit.gui.JTabbedPaneDraggableAndCloseable.TabCloseEvent)
	 */
	public void tabClosed(TabCloseEvent evt0) {
		
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
					lprefs.getShort(LaTeXOptions.FONT_SIZE), lprefs
							.get(LaTeXOptions.PAPER_SIZE), lprefs
							.getBoolean(LaTeXOptions.SHOW_PREDEFINED_UNITS),
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
	private void writeSBML(final File out) {
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
		tabbedPane.remove(tabbedPane.getSelectedComponent());
		if (tabbedPane.getTabCount() == 0) {
			GUITools.setEnabled(false, getJMenuBar(), getJToolBar(),
				BaseFrame.BaseAction.FILE_CLOSE, BaseFrame.BaseAction.FILE_SAVE,
				BaseFrame.BaseAction.FILE_SAVE_AS, Command.SQUEEZE, Command.SABIO_RK,
				Command.TO_LATEX);
		}
		return tabbedPane.getComponentCount() > 0;
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
		colorDefault = getContentPane().getBackground();
		Icon icon = UIManager.getIcon("ICON_LOGO_SMALL");
		logo = new JLabel(StringUtil.toHTML("<br><br><br><br><br>"+MESSAGES.getString("VERSION")+": "
				+ System.getProperty("app.version")), icon, JLabel.CENTER);
		if (icon != null) {
			logo.setPreferredSize(new Dimension(icon.getIconWidth() + 125, icon
					.getIconHeight() + 75));
		}
		tabbedPane = new JTabbedPaneDraggableAndCloseable();
		tabbedPane.addCloseListener(this);
		tabbedPane.addChangeListener(this);
		return tabbedPane;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#exit()
	 */
	@Override
	public void exit() {
    StringBuilder exception = new StringBuilder();
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
    dispose();
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLAboutMessage()
	 */
	public URL getURLAboutMessage() {
		return SBMLsqueezer.class.getResource("resources/html/about.html");
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLLicense()
	 */
	public URL getURLLicense() {
		return SBMLsqueezer.class.getResource("resources/html/License.html");
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLOnlineHelp()
	 */
	public URL getURLOnlineHelp() {
		return SBMLsqueezer.class.getResource("resources/html/help.html");
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#openFile(de.zbit.sbml.io.OpenedFile<org.sbml.jsbml.SBMLDocument>[])
	 */
	public File[] openFile(File... files) {
		SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
		if ((files == null) || (files.length == 0)) {
			files = GUITools.openFileDialog(
				this,
				prefs.get(GUIOptions.OPEN_DIR),
				false,
				true,
				JFileChooser.FILES_ONLY, 
				SBFileFilter.createSBMLFileFilter()
			);
		}
		if (files != null) {
			boolean usesJSBML = sbmlIO.getReader() instanceof SqSBMLReader;
			for (final File file : files) {
				if (usesJSBML) {
					try {
						SBMLReadingTask reader = new SBMLReadingTask(file, this);
						reader.addPropertyChangeListener(new PropertyChangeListener() {
							
							public void propertyChange(PropertyChangeEvent evt) {
								if (evt.getPropertyName().equals(SBMLReadingTask.SBML_READING_SUCCESSFULLY_DONE)) {
									readModel(file);
								}
							}
						});
						reader.execute();
					} catch (FileNotFoundException exc) {
						GUITools.showErrorMessage(this, exc);
					}
				} else {
					new FileReaderThread(this, file);
				}
			}
		}
		return files;
	}

	/**
	 * 
	 * @param out
	 */
	private void save(final File out) {
		SBFileFilter filterText = SBFileFilter.createTextFileFilter();
		SBFileFilter filterTeX = SBFileFilter.createTeXFileFilter();

		if (!out.exists() || GUITools.overwriteExistingFile(this, out)) {
		    if (SBFileFilter.hasFileType(out, SBFileFilter.FileType.SBML_FILES)) {
		      new Thread(new Runnable() {
		        /* (non-Javadoc)
		         * @see java.lang.Runnable#run()
		         */
		        public void run() {
		          writeSBML(out);
		        }
		      }).start();
			} else if (filterTeX.accept(out)) {
				writeLaTeX(out);
			} else if (filterText.accept(out)) {
				writeText(out);
			}
		}
	}
	

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#saveFile()
	 */
	@Override
	public File saveFile() {
		File savedFile = sbmlIO.getSelectedFile();
		logger.info(savedFile.getAbsolutePath());
		
		if (savedFile != null) {
			save(savedFile);
		} 
		
		return savedFile;
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
			sbmlIO.getSelectedOpenedFile().setFile(out);
			save(out);
		}
		return savedFile;
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	// @Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("openedFileChanged")) {
			setEnabled(sbmlIO.getSelectedOpenedFile().isChanged(), BaseAction.FILE_SAVE);
		}
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#showSaveMenuEntry()
	 */
	@Override
	protected boolean showSaveMenuEntry() {
		return true;
	}

}
