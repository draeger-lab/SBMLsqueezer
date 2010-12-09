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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.prefs.BackingStoreException;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.resources.Resource;
import org.sbml.tolatex.gui.LaTeXExportDialog;
import org.sbml.tolatex.io.LaTeXReportGenerator;
import org.sbml.tolatex.io.TextExport;

import de.zbit.gui.ActionCommand;
import de.zbit.gui.BaseFrame;
import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.ImageTools;
import de.zbit.gui.JBrowserPane;
import de.zbit.gui.SystemBrowser;
import de.zbit.gui.prefs.PreferencesDialog;
import de.zbit.io.SBFileFilter;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.3
 * 
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
	public FileReaderThread(SBMLsqueezerUI ui, File f) {
		super();
		this.reader = ui;
		this.file = f;
		start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
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
 * {@link JTabbedPaneWithCloseIcons} so that multiple models can be opened at
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
		ChangeListener {

	/**
	 * This is what the graphical user interface of SBMLsqueezer can do...
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2009-09-11
	 * @since 1.3
	 */
	public static enum Command implements ActionCommand {
		/**
		 * Check whether the current model is stable.
		 */
		CHECK_STABILITY,
		/**
		 * Opens the SBML file that was opened last time.
		 */
		OPEN_LAST_FILE,
		/**
		 * Simulate the dynamics of the current model.
		 */
		SIMULATE,
		/**
		 * Generate kinetic equations.
		 */
		SQUEEZE,
		/**
		 * Perform structural kinetic modeling, i.e., detect the key reactions
		 * within the model.
		 */
		STRUCTURAL_KINETIC_MODELLING,
		/**
		 * Convert the current model or the current SBML object into a LaTeX
		 * report.
		 */
		TO_LATEX;

		/*
		 * (non-Javadoc)
		 * @see de.zbit.gui.ActionCommand#getName()
		 */
		public String getName() {
			return StringUtil.firstLetterUpperCase(toString().toLowerCase().replace('_', ' '));
		}

		/*
		 * (non-Javadoc)
		 * @see de.zbit.gui.ActionCommand#getToolTip()
		 */
		public String getToolTip() {
			return null;
		}
	}

	static {
		ImageTools.initImages(SBMLsqueezer.class.getResource("../resources/img"));
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
				warnings.append(exc.getMessage().replace("<", "&lt;").replace(
						">", "&gt;"));
				warnings.append("</p>");
			}
			JEditorPane area = new JEditorPane("text/html", StringUtil
					.toHTML(warnings.toString()));
			area.setEditable(false);
			area.setBackground(Color.WHITE);
			JScrollPane scroll = new JScrollPane(area,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			scroll.setPreferredSize(new Dimension(450, 200));
			JOptionPane.showMessageDialog(parent, scroll, "SBML warnings",
					JOptionPane.WARNING_MESSAGE);
			if (m == null) {
				String message = "Unable to load this model "
						+ "due to one or several errors. "
						+ "Please use the SBML online validator "
						+ "to check why this model is not correct.";
				JOptionPane.showMessageDialog(parent, StringUtil.toHTML(message,
						40), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Shows a dialog window with the online help.
	 * 
	 * @param owner
	 * @param wl
	 * @param title
	 * @param fileLocation
	 */
	public static void showOnlineHelp(Frame owner, WindowListener wl,
			String title, String fileLocation) {
		JHelpBrowser helpBrowser = new JHelpBrowser(owner, title, fileLocation);
		helpBrowser.addWindowListener(wl);
		helpBrowser.setLocationRelativeTo(owner);
		helpBrowser.setSize(640, 640);
		helpBrowser.setVisible(true);
		helpBrowser.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
	private JTabbedPaneWithCloseIcons tabbedPane;

	/**
	 * 
	 */
	private JToolBar toolbar;

	/**
	 * 
	 * @param io
	 */
	public SBMLsqueezerUI(SBMLio io) {
		super();
		this.sbmlIO = io;
		this.sbmlIO.addIOProgressListener(new ProgressDialog(this,
				"SBML IO progress"));
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser;
		switch (Command.valueOf(e.getActionCommand())) {
		case SQUEEZE:
			KineticLawSelectionDialog klsd;
			if (e.getSource() instanceof Reaction) {
				// just one reaction
				klsd = new KineticLawSelectionDialog(this, properties, sbmlIO,
						((Reaction) e.getSource()).getId());
			} else {
				// whole model
				klsd = new KineticLawSelectionDialog(this, properties, sbmlIO);
				klsd.addWindowListener(this);
				klsd.setVisible(true);
			}
			if (klsd.isKineticsAndParametersStoredInSBML()) {
				SBMLModelSplitPane split = (SBMLModelSplitPane) tabbedPane
						.getSelectedComponent();
				split.init(sbmlIO.getSelectedModel(), true);
			}
			break;
		case TO_LATEX:
			if (e.getSource() instanceof Reaction) {
				new LaTeXExportDialog(this, properties, (Reaction) e
						.getSource());
			} else if (e.getSource() instanceof Model) {
				new LaTeXExportDialog(this, properties, (Model) e.getSource());
			} else {
				String dir = properties.get(CfgKeys.SqueezerOptions).toString();
				File out = GUITools.saveFileDialog(this, dir, false, false,
						JFileChooser.FILES_ONLY, SBFileFilter.createTeXFileFilter());
				if (out != null) {
					String path = out.getParent();
					if (!path.equals(dir)) {
						properties.put(CfgKeys.SqueezerOptions, path);
					}
					if (!out.exists()
							|| GUITools.overwriteExistingFile(this, out)) {
						writeLaTeX(out);
					}
				}
			}
			break;
		case OPEN_LAST_FILE:
			File f = new File(properties.get(SqueezerOptions.SBML_FILE).toString());
			if (f.exists() && f.isFile()) {
				new FileReaderThread(this, f);
				setEnabled(false, Command.OPEN_LAST_FILE);
			}
			break;
		case CHECK_STABILITY:
			// TODO: Not in this version
			// StabilityDialog stabilitydialog = new StabilityDialog(this);
			// stabilitydialog.showStabilityDialog(settings, sbmlIO);
			break;
		case STRUCTURAL_KINETIC_MODELLING:
			break;
		case SIMULATE:
			// TODO: Not in this version
			// showSimulationControl(false);
			break;
		default:
			break;
		}
	}

	// TODO: Not in this version
	// /**
	// * Opens and displays a simulation dialog.
	// *
	// * @param modal
	// * If true the dialog will be modal.
	// * @return If a model has been loaded, a simulation dialog. It should be
	// * noted that null will be returned if no model has been loadad yet.
	// *
	// */
	// public SimulationDialog showSimulationControl(boolean modal) {
	// return showSimulationControl(modal, null);
	// }
	//
	// /**
	// *
	// * @param modal
	// * @param csvFile
	// */
	// public SimulationDialog showSimulationControl(boolean modal, String
	// csvFile) {
	// Model model = sbmlIO.getSelectedModel();
	// if (model != null) {
	// SimulationDialog d = new SimulationDialog(this, model, settings);
	// if (csvFile != null)
	// try {
	// d.openExperimentalData(csvFile);
	// } catch (IOException exc) {
	// exc.printStackTrace();
	// JOptionPane.showMessageDialog(this, exc.getMessage(), exc
	// .getClass().getSimpleName(),
	// JOptionPane.ERROR_MESSAGE);
	// }
	// setEnabled(false, Command.SIMULATE);
	// d.addWindowListener(this);
	// d.setModal(modal);
	// d.setVisible(true);
	// return d;
	// }
	// return null;
	// }

	/**
	 * Adds the given new model into the tabbed pane on the main panel.
	 * 
	 * @param model
	 */
	private void addModel(Model model) {
		SBMLModelSplitPane split = new SBMLModelSplitPane(model, properties);
		split.addActionListener(this);
		tabbedPane.add(model.getId(), split);
		tabbedPane.setSelectedIndex(tabbedPane.getComponentCount() - 1);
		setEnabled(true, Command.SAVE_FILE, Command.CLOSE_FILE,
				Command.SQUEEZE, Command.TO_LATEX, Command.CHECK_STABILITY,
				Command.STRUCTURAL_KINETIC_MODELLING, Command.SIMULATE);
		setEnabled(false, Command.OPEN_LAST_FILE);
	}

	/**
	 * Creates the menu bar for SBMLsqueezer's UI.
	 * 
	 * @return
	 */
	private JMenuBar createMenuBar() {
		/*
		 * File menu
		 */
		// TODO: Use methods from GUITools!
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(fileMenu.getText().charAt(0));
		JMenu lastOpened = new JMenu("Last opened");
		lastOpened.setEnabled(false);
		fileMenu.add(openItem);
		fileMenu.addSeparator();
		fileMenu.add(lastOpened);

		Object path = properties.get(SqueezerOptions.SBML_FILE);
		File sbmlFile = new File(path == null ? "" : path.toString());
		if (sbmlFile.exists() && sbmlFile.isFile()) {
			JMenuItem lastSBMLFile = new JMenuItem(sbmlFile.getName());
			lastSBMLFile.addActionListener(this);
			lastSBMLFile.setActionCommand(Command.OPEN_LAST_FILE.toString());
			lastOpened.add(lastSBMLFile);
			lastOpened.setEnabled(true);
		}

		/*
		 * Edit menu
		 */
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(editMenu.getText().charAt(0));

		editMenu.add(GUITools.createJMenuItem("Squeeze", this, Command.SQUEEZE,
				UIManager.getIcon("ICON_LEMON_TINY"), KeyStroke.getKeyStroke(
						'Q', InputEvent.CTRL_DOWN_MASK)));
		editMenu.add(GUITools.createJMenuItem("Export to LaTeX", this,
				Command.TO_LATEX, UIManager.getIcon("ICON_LATEX_TINY"),
				KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK)));

		// TODO: Not in this version
		// editMenu.addSeparator();
		// editMenu.add(GUITools.createMenuItem("Analyze Stability",
		// Command.CHECK_STABILITY, this, GUITools.ICON_STABILITY_SMALL));
		// editMenu.add(GUITools.createMenuItem("Structural Kinetic Modelling",
		// Command.STRUCTURAL_KINETIC_MODELLING, this,
		// GUITools.ICON_STRUCTURAL_MODELING_TINY));
		// editMenu.add(GUITools.createMenuItem("Simulation", Command.SIMULATE,
		// 'S', this, GUITools.ICON_DIAGRAM_TINY));

		return mBar;
	}

	/**
	 * Sets up this GUI.
	 */
	private void init() {
		setJMenuBar(createMenuBar());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(this);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		colorDefault = getContentPane().getBackground();
		Icon icon = UIManager.getIcon("ICON_LOGO_SMALL");
		logo = new JLabel(StringUtil.toHTML("<br><br><br><br><br>Version: "
				+ SBMLsqueezer.getVersionNumber()), icon, JLabel.CENTER);
		if (icon != null) {
			logo.setPreferredSize(new Dimension(icon.getIconWidth() + 125, icon
					.getIconHeight() + 75));
		}
		setEnabled(false, Command.SAVE_FILE, Command.CLOSE_FILE,
				Command.SQUEEZE, Command.TO_LATEX, Command.CHECK_STABILITY,
				Command.STRUCTURAL_KINETIC_MODELLING, Command.SIMULATE);
		tabbedPane = new JTabbedPaneWithCloseIcons();
		tabbedPane.addChangeListener(this);
		tabbedPane.addChangeListener(sbmlIO);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		setSBMLsqueezerBackground();
		// TODO
//		setIconImage(UIManager.getIcon("IMAGE_LEMON"));
		for (Model m : sbmlIO.getListOfModels()) {
			checkForSBMLErrors(this, m, sbmlIO.getWarnings(),
					((Boolean) properties.get(SqueezerOptions.SHOW_SBML_WARNINGS))
							.booleanValue());
			if (m != null) {
				addModel(m);
			}
		}
	}

	/**
	 * Reads a model from the given file and adds it to the GUI if possible.
	 * 
	 * @param file
	 */
	void readModel(File file) {
		try {
			Model model = sbmlIO.convert2Model(file.getAbsolutePath());
			checkForSBMLErrors(this, model, sbmlIO.getWarnings(),
					((Boolean) properties.get(SqueezerOptions.SHOW_SBML_WARNINGS))
							.booleanValue());
			if (model != null) {
				addModel(model);
				String path = file.getAbsolutePath();
				String oldPath = properties.get(SqueezerOptions.SBML_FILE).toString();
				if (!path.equals(oldPath)) {
					for (int i = 0; i < getJMenuBar().getMenuCount(); i++) {
						JMenu menu = getJMenuBar().getMenu(i);
						for (int j = 0; j < menu.getItemCount(); j++) {
							Object item = menu.getItem(j);
							if (item != null
									&& item instanceof JMenu
									&& ((JMenu) item).getActionCommand()
											.equals("Last opened")) {
								JMenu m = (JMenu) item;
								m.removeAll();
								JMenuItem mItem = new JMenuItem(file.getName());
								mItem.addActionListener(this);
								mItem.setActionCommand(Command.OPEN_LAST_FILE
										.toString());
								m.add(mItem);
								mItem.setEnabled(false);
							}
						}
					}
					properties.put(SqueezerOptions.SBML_FILE, path);
				}
				if (!file.getParentFile().equals(
						properties.get(CfgKeys.SqueezerOptions).toString())){
					properties.put(CfgKeys.SqueezerOptions, file.getParentFile());}
			}
		} catch (Exception exc) {
			GUITools.showErrorMessage(this, exc);
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
		GUITools.setEnabled(state, getJMenuBar(), toolbar, commands);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(tabbedPane)) {
			if (tabbedPane.getComponentCount() == 0) {
				setEnabled(false, BaseAction.FILE_SAVE, BaseAction.FILE_CLOSE,
						Command.SQUEEZE, Command.TO_LATEX,
						Command.CHECK_STABILITY,
						Command.STRUCTURAL_KINETIC_MODELLING, Command.SIMULATE);
				if (properties.get(SqueezerOptions.SBML_FILE).toString()
						.length() > 0) {
					setEnabled(true, Command.OPEN_LAST_FILE);
				}
			}
			setSBMLsqueezerBackground();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent we) {
		if (we.getWindow() instanceof JDialog) {
			if (we.getWindow() instanceof KineticLawSelectionDialog) {
				KineticLawSelectionDialog klsd = (KineticLawSelectionDialog) we
						.getWindow();
				if (klsd.isKineticsAndParametersStoredInSBML()) {
					SBMLModelSplitPane split = (SBMLModelSplitPane) tabbedPane
							.getSelectedComponent();
					split.init(sbmlIO.getSelectedModel(), true);
				}
			} else {
				setEnabled(true, Command.SIMULATE);
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
			LaTeXReportGenerator.writeLaTeX(sbmlIO.getSelectedModel(), out);
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

		} catch (IOException exc) {
			GUITools.showErrorMessage(this, exc);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#closeFile()
	 */
	public boolean closeFile() {
		boolean change = false;
		if (tabbedPane.getComponentCount() > 0) {
			tabbedPane.remove(tabbedPane.getSelectedComponent());
			change = true;
		}
		if (tabbedPane.getComponentCount() == 0) {
			setEnabled(false, BaseAction.FILE_SAVE, BaseAction.FILE_CLOSE,
					Command.SQUEEZE, Command.TO_LATEX,
					Command.CHECK_STABILITY,
					Command.STRUCTURAL_KINETIC_MODELLING, Command.SIMULATE);
			setEnabled(true, Command.OPEN_LAST_FILE);
		}
		return change;
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#createJToolBar()
	 */
	protected JToolBar createJToolBar() {
		toolbar = new JToolBar("Edit", JToolBar.HORIZONTAL);
		System.out.println(UIManager.getIcon("ICON_OPEN"));
		JButton openButton = new JButton(UIManager.getIcon("ICON_OPEN"));
		openButton.addActionListener(this);
		openButton.setActionCommand(BaseAction.FILE_OPEN.toString());
		openButton.setToolTipText(StringUtil.toHTML(
				"Opens a file in SBML format.", 40));
		toolbar.add(openButton);
		JButton saveButton = new JButton(UIManager.getIcon("ICON_SAVE"));
		saveButton.addActionListener(this);
		saveButton.setActionCommand(BaseAction.FILE_SAVE.toString());
		saveButton.setToolTipText(StringUtil.toHTML(
								"Save the current version of the model in SBML format or export it to a text or LaTeX report.",
								40));
		toolbar.add(saveButton);
		JButton closeButton = new JButton(UIManager.getIcon("ICON_TRASH_TINY")); // new
		// CloseIcon(false)
		closeButton.setActionCommand(BaseAction.FILE_CLOSE.toString());
		closeButton.addActionListener(this);
		closeButton.setToolTipText(StringUtil.toHTML(
				"Close the current model without saving.", 40));
		toolbar.add(closeButton);
		toolbar.addSeparator();
		if (UIManager.getIcon("ICON_LEMON_TINY") != null) {
			toolbar.add(GUITools
							.createButton(UIManager.getIcon("ICON_LEMON_TINY"),
									this, Command.SQUEEZE,
									"Generate kinetic equations for all reactions in this model in one step."));
		}
		if (UIManager.getIcon("ICON_LATEX_TINY") != null) {
			toolbar.add(GUITools.createButton(UIManager
					.getIcon("ICON_LATEX_TINY"), this, Command.TO_LATEX,
					"Export this model to a LaTeX report."));
		}
		// TODO: Not in this version
		// if (GUITools.ICON_STABILITY_SMALL != null)
		// toolbar.add(GUITools.createButton(GUITools.ICON_STABILITY_SMALL,
		// this, Command.CHECK_STABILITY,
		// "Analyze the stability properties of the selected model."));
		// if (GUITools.ICON_STRUCTURAL_MODELING_TINY != null)
		// toolbar.add(GUITools.createButton(
		// GUITools.ICON_STRUCTURAL_MODELING_TINY, this,
		// Command.STRUCTURAL_KINETIC_MODELLING,
		// "Identify key reactins with structural kinetic modeling."));
		// if (GUITools.ICON_DIAGRAM_TINY != null)
		// toolbar.add(GUITools
		// .createButton(GUITools.ICON_DIAGRAM_TINY, this,
		// Command.SIMULATE,
		// "Dynamically simulate the current model."));

		if (UIManager.getIcon("ICON_SETTINGS_TINY") != null) {
			toolbar.add(GUITools.createButton(UIManager
					.getIcon("ICON_SETTINGS_TINY"), this,
					BaseAction.EDIT_PREFERENCES, "Adjust your preferences."));
		}
		toolbar.addSeparator();
		JButton helpButton = new JButton(UIManager.getIcon("ICON_HELP_TINY"));
		helpButton.addActionListener(this);
		helpButton.setActionCommand(BaseAction.HELP_ONLINE.toString());
		helpButton.setToolTipText(StringUtil.toHTML("Open the online help.", 40));
		toolbar.add(helpButton);
		return toolbar;
	}

	@Override
	protected Component createMainComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#exit()
	 */
	public void exit() {
		try {
			SBMLsqueezer.saveProperties();
		} catch (Exception exc) {
			GUITools.showErrorMessage(this, exc);
		}
		// TODO: Not in this version
		/*
		 * else if (we.getSource() instanceof SimulationDialog) { settings
		 * .putAll(((SimulationDialog) we.getSource()).getProperties()); }
		 */
		System.exit(0);
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getApplicationName()
	 */
	public String getApplicationName() {
		return "SBMLsqueezer";
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getCommandLineOptions()
	 */
	public Class<? extends KeyProvider>[] getCommandLineOptions() {
		return SBMLsqueezer.getCommandLineOptions();
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getDottedVersionNumber()
	 */
	public String getDottedVersionNumber() {
		return SBMLsqueezer.getVersionNumber();
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLAboutMessage()
	 */
	public URL getURLAboutMessage() {
		return getClass().getResource("../resources/html/about.htm");
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLLicense()
	 */
	public URL getURLLicense() {
		return getClass().getResource("../resources/html/License.html");
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLOnlineHelp()
	 */
	public URL getURLOnlineHelp() {
		return getClass().getResource("../resources/html/help.html");
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#getURLOnlineUpdate()
	 */
	public URL getURLOnlineUpdate() {
		try {
			return new URL(
				"http://www.ra.cs.uni-tuebingen.de/software/SBMLsqueezer/downloads/");
		} catch (MalformedURLException exc) {
			GUITools.showErrorMessage(this, exc);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#openFile()
	 */
	public void openFile() {
		SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
		JFileChooser chooser = GUITools.createJFileChooser(prefs
				.get(GUIOptions.OPEN_DIR), false, false,
				JFileChooser.FILES_ONLY, SBFileFilter.createSBMLFileFilter());
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			new FileReaderThread(this, chooser.getSelectedFile());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#saveFile()
	 */
	public void saveFile() {
		SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
		SBFileFilter filterText = SBFileFilter.createTextFileFilter();
		SBFileFilter filterTeX = SBFileFilter.createTeXFileFilter();
		SBFileFilter filterSBML = SBFileFilter.createSBMLFileFilter();
		JFileChooser chooser = GUITools.createJFileChooser(prefs.get(GUIOptions.SAVE_DIR), false, false,
				JFileChooser.FILES_ONLY, filterText, filterTeX, filterSBML);
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			final File out = chooser.getSelectedFile();
			if (out.getParentFile() != null) {
				prefs.put(GUIOptions.SAVE_DIR, out.getParentFile()
						.getAbsolutePath());
				try {
					prefs.flush();
				} catch (BackingStoreException exc) {
					GUITools.showErrorMessage(this, exc);
				}
			}
			if (!out.exists() || GUITools.overwriteExistingFile(this, out)) {
				if (filterSBML.accept(out)) {
					new Thread(new Runnable() {
						/*
						 * (non-Javadoc)
						 * 
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
	}
}
