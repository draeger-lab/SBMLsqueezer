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
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.io.LaTeXExport;
import org.sbml.squeezer.io.SBFileFilter;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.TextExport;
import org.sbml.squeezer.resources.Resource;

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
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @since 1.0
 */
public class SBMLsqueezerUI extends JFrame implements ActionListener,
		WindowListener, ChangeListener {

	/**
	 * This is what the graphical user interface of SBMLsqueezer can do...
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2009-09-11
	 * @since 1.3
	 */
	public static enum Command {
		/**
		 * Show about message, i.e., information about the authors of this
		 * program.
		 */
		ABOUT,
		/**
		 * Check whether the current model is stable.
		 */
		CHECK_STABILITY,
		/**
		 * Close the current model.
		 */
		CLOSE_FILE,
		/**
		 * Close all files and leave the program.
		 */
		EXIT,
		/**
		 * Display the help.
		 */
		ONLINE_HELP,
		/**
		 * Open a file
		 */
		OPEN_FILE,
		/**
		 * Opens the SBML file that was opened last time.
		 */
		OPEN_LAST_FILE,
		/**
		 * Save all changes in the current model to a file.
		 */
		SAVE_FILE,
		/**
		 * Change the configuration.
		 */
		SET_PREFERENCES,
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
		TO_LATEX,
		/**
		 * Display the license of this project to the user.
		 */
		LICENSE
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
			JEditorPane area = new JEditorPane("text/html", GUITools
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
				JOptionPane.showMessageDialog(parent, GUITools.toHTML(message,
						40), "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Shows a dialog window with the online help.
	 * 
	 * @param owner
	 * @param wl
	 */
	public static void showOnlineHelp(Frame owner, WindowListener wl) {
		showOnlineHelp(owner, wl, String.format(
				"SBMLsqueezer %s - Online Help", SBMLsqueezer
						.getVersionNumber()), "html/help.html");
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
	private Properties settings;

	/**
	 * 
	 */
	private JTabbedPaneWithCloseIcons tabbedPane;

	/**
	 * 
	 */
	private JToolBar toolbar;

	/**
	 * @throws HeadlessException
	 */
	public SBMLsqueezerUI(SBMLio io, Properties settings)
			throws HeadlessException {
		super(SBMLsqueezer.class.getSimpleName() + ' '
				+ SBMLsqueezer.getVersionNumber());
		this.settings = settings;
		this.sbmlIO = io;
		this.sbmlIO.addIOProgressListener(new ProgressDialog(this,
				"SBML IO progress"));
		init();
		pack();
		Dimension dim = logo.getPreferredSize();
		setMinimumSize(new Dimension((int) dim.getWidth() + 50, (int) dim
				.getHeight() + 50));
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
		case EXIT:
			try {
				SBMLsqueezer.saveProperties(settings);
			} catch (FileNotFoundException exc) {
				exc.printStackTrace();
				JOptionPane.showMessageDialog(this, exc.getMessage(), exc
						.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			} catch (IOException exc) {
				exc.printStackTrace();
				JOptionPane.showMessageDialog(this, exc.getMessage(), exc
						.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
			System.exit(0);
		case ABOUT:
			JBrowser browser = new JBrowser(Resource.class
					.getResource("html/about.htm"));
			browser.removeHyperlinkListener(browser);
			browser.addHyperlinkListener(new SystemBrowser());
			browser.setBorder(BorderFactory.createEtchedBorder());
			JOptionPane.showMessageDialog(this, browser, "About SBMLsqueezer",
					JOptionPane.INFORMATION_MESSAGE);
			break;
		case SQUEEZE:
			KineticLawSelectionDialog klsd;
			if (e.getSource() instanceof Reaction) {
				// just one reaction
				klsd = new KineticLawSelectionDialog(this, settings, sbmlIO,
						((Reaction) e.getSource()).getId());
			} else {
				// whole model
				klsd = new KineticLawSelectionDialog(this, settings, sbmlIO);
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
				new KineticLawSelectionDialog(this, settings, (Reaction) e
						.getSource());
			} else if (e.getSource() instanceof Model) {
				new KineticLawSelectionDialog(this, settings, (Model) e
						.getSource());
			} else {
				String dir = settings.get(CfgKeys.LATEX_DIR).toString();
				chooser = GUITools.createJFileChooser(dir, false, false,
						JFileChooser.FILES_ONLY, SBFileFilter.TeX_FILE_FILTER);
				if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
					File out = chooser.getSelectedFile();
					String path = out.getParent();
					if (!path.equals(dir))
						settings.put(CfgKeys.LATEX_DIR, path);
					if (!out.exists()
							|| GUITools.overwriteExistingFile(this, out))
						writeLaTeX(out);
				}
			}
			break;
		case SET_PREFERENCES:
			SettingsDialog dialog = new SettingsDialog(this);
			if (dialog.showSettingsDialog((Properties) settings.clone()) == SettingsDialog.APPROVE_OPTION)
				for (Object key : dialog.getSettings().keySet())
					settings.put(key, dialog.getSettings().get(key));
			break;
		case OPEN_FILE:
			chooser = GUITools.createJFileChooser(settings
					.get(CfgKeys.OPEN_DIR).toString(), false, false,
					JFileChooser.FILES_ONLY, SBFileFilter.SBML_FILE_FILTER);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				new FileReaderThread(this, chooser.getSelectedFile());
			}
			break;
		case OPEN_LAST_FILE:
			File f = new File(settings.get(CfgKeys.SBML_FILE).toString());
			if (f.exists() && f.isFile()) {
				new FileReaderThread(this, f);
				setEnabled(false, Command.OPEN_LAST_FILE);
			}
			break;
		case SAVE_FILE:
			SBFileFilter filterText = SBFileFilter.TEXT_FILE_FILTER;
			SBFileFilter filterTeX = SBFileFilter.TeX_FILE_FILTER;
			SBFileFilter filterSBML = SBFileFilter.SBML_FILE_FILTER;
			chooser = GUITools.createJFileChooser(settings
					.get(CfgKeys.SAVE_DIR).toString(), false, false,
					JFileChooser.FILES_ONLY, filterText, filterTeX, filterSBML);
			if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				final File out = chooser.getSelectedFile();
				if (out.getParentFile() != null)
					settings.put(CfgKeys.SAVE_DIR, out.getParentFile()
							.getAbsolutePath());
				if (!out.exists() || GUITools.overwriteExistingFile(this, out)) {
					if (filterSBML.accept(out))
						new Thread(new Runnable() {
							public void run() {
								writeSBML(out);
							}
						}).start();
					else if (filterTeX.accept(out))
						writeLaTeX(out);
					else if (filterText.accept(out))
						writeText(out);
				}
			}
			break;
		case CLOSE_FILE:
			if (tabbedPane.getComponentCount() > 0)
				tabbedPane.remove(tabbedPane.getSelectedComponent());
			if (tabbedPane.getComponentCount() == 0) {
				setEnabled(false, Command.SAVE_FILE, Command.CLOSE_FILE,
						Command.SQUEEZE, Command.TO_LATEX,
						Command.CHECK_STABILITY,
						Command.STRUCTURAL_KINETIC_MODELLING, Command.SIMULATE);
				setEnabled(true, Command.OPEN_LAST_FILE);
			}
			break;
		case ONLINE_HELP:
			setEnabled(false, Command.ONLINE_HELP);
			showOnlineHelp(this, this);
			break;
		case LICENSE:
			setEnabled(false, Command.LICENSE);
			showOnlineHelp(this, this, String.format(
					"SBMLsqueezer %s - License", SBMLsqueezer
							.getVersionNumber()), "html/License.html");
			break;
		case CHECK_STABILITY:
			StabilityDialog stabilitydialog = new StabilityDialog(this);
			stabilitydialog.showStabilityDialog(settings, sbmlIO);
			break;
		case STRUCTURAL_KINETIC_MODELLING:
			break;
		case SIMULATE:
			showSimulationControl(false);
			break;
		default:
			break;
		}
	}

	/**
	 * Opens and displays a simulation dialog.
	 * 
	 * @param modal
	 *            If true the dialog will be modal.
	 * @return If a model has been loaded, a simulation dialog. It should be
	 *         noted that null will be returned if no model has been loadad yet.
	 * 
	 */
	public SimulationDialog showSimulationControl(boolean modal) {
		Model model = sbmlIO.getSelectedModel();
		if (model != null) {
			SimulationDialog d = new SimulationDialog(this, model, settings);
			setEnabled(false, Command.SIMULATE);
			d.addWindowListener(this);
			d.setModal(modal);
			d.setVisible(true);
			return d;
		}
		return null;
	}

	/**
	 * Adds the given new model into the tabbed pane on the main panel.
	 * 
	 * @param model
	 */
	private void addModel(Model model) {
		SBMLModelSplitPane split = new SBMLModelSplitPane(model, settings);
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
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(fileMenu.getText().charAt(0));
		JMenuItem openItem = new JMenuItem("Open", GUITools.ICON_OPEN);
		openItem.setActionCommand(Command.OPEN_FILE.toString());
		openItem.setAccelerator(KeyStroke.getKeyStroke('O',
				InputEvent.CTRL_DOWN_MASK));
		JMenuItem saveItem = new JMenuItem("Save as", GUITools.ICON_SAVE);
		saveItem.addActionListener(this);
		saveItem.setActionCommand(Command.SAVE_FILE.toString());
		saveItem.setAccelerator(KeyStroke.getKeyStroke('S',
				InputEvent.CTRL_DOWN_MASK));
		JMenuItem closeItem = new JMenuItem("Close", new CloseIcon(false));
		closeItem.setAccelerator(KeyStroke.getKeyStroke('W',
				InputEvent.CTRL_DOWN_MASK));
		closeItem.setActionCommand(Command.CLOSE_FILE.toString());
		JMenu lastOpened = new JMenu("Last opened");
		lastOpened.setEnabled(false);
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setActionCommand(Command.EXIT.toString());
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				InputEvent.ALT_DOWN_MASK));
		fileMenu.add(openItem);
		fileMenu.addSeparator();
		fileMenu.add(saveItem);
		fileMenu.add(closeItem);
		fileMenu.add(lastOpened);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		openItem.addActionListener(this);
		closeItem.addActionListener(this);
		exitItem.addActionListener(this);

		Object path = settings.get(CfgKeys.SBML_FILE);
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

		editMenu.add(GUITools.createMenuItem("Squeeze", KeyStroke.getKeyStroke(
				'Q', InputEvent.CTRL_DOWN_MASK), Command.SQUEEZE, this,
				GUITools.ICON_LEMON_TINY));
		editMenu.add(GUITools.createMenuItem("Export to LaTeX", KeyStroke
				.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK),
				Command.TO_LATEX, this, GUITools.ICON_LATEX_TINY));
		editMenu.addSeparator();
		editMenu.add(GUITools.createMenuItem("Analyze Stability",
				Command.CHECK_STABILITY, this, GUITools.ICON_STABILITY_SMALL));
		editMenu.add(GUITools.createMenuItem("Structural Kinetic Modelling",
				Command.STRUCTURAL_KINETIC_MODELLING, this,
				GUITools.ICON_STRUCTURAL_MODELING_TINY));
		editMenu.add(GUITools.createMenuItem("Simulation", Command.SIMULATE,
				'S', this, GUITools.ICON_DIAGRAM_TINY));
		editMenu.addSeparator();
		editMenu.add(GUITools
				.createMenuItem("Preferences", Command.SET_PREFERENCES, 'P',
						this, GUITools.ICON_SETTINGS_TINY));

		/*
		 * Help menu
		 */
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(helpMenu.getText().charAt(0));
		helpMenu.add(GUITools.createMenuItem("Online help", KeyStroke
				.getKeyStroke(KeyEvent.VK_F1, 0), Command.ONLINE_HELP, this,
				GUITools.ICON_HELP_TINY));
		helpMenu.add(GUITools.createMenuItem("About", KeyStroke.getKeyStroke(
				KeyEvent.VK_F2, 0), Command.ABOUT, this,
				GUITools.ICON_INFO_TINY));
		helpMenu.add(GUITools.createMenuItem("License", null, Command.LICENSE,
				'L', this, GUITools.ICON_LICENCE_TINY));

		JMenuBar mBar = new JMenuBar();
		mBar.add(fileMenu);
		mBar.add(editMenu);
		try {
			mBar.setHelpMenu(helpMenu);
		} catch (Error e) {
			mBar.add(helpMenu);
		}
		return mBar;
	}

	/**
	 * Creates the tool bar for SBMLsqueezer's UI.
	 * 
	 * @return
	 */
	private JToolBar createToolBar() {
		toolbar = new JToolBar("Edit", JToolBar.HORIZONTAL);
		JButton openButton = new JButton(GUITools.ICON_OPEN);
		openButton.addActionListener(this);
		openButton.setActionCommand(Command.OPEN_FILE.toString());
		openButton.setToolTipText(GUITools.toHTML(
				"Opens a file in SBML format.", 40));
		toolbar.add(openButton);
		JButton saveButton = new JButton(GUITools.ICON_SAVE);
		saveButton.addActionListener(this);
		saveButton.setActionCommand(Command.SAVE_FILE.toString());
		saveButton
				.setToolTipText(GUITools
						.toHTML(
								"Save the current version of the model in SBML format or export it to a text or LaTeX report.",
								40));
		toolbar.add(saveButton);
		JButton closeButton = new JButton(GUITools.ICON_TRASH_TINY); // new
		// CloseIcon(false)
		closeButton.setActionCommand(Command.CLOSE_FILE.toString());
		closeButton.addActionListener(this);
		closeButton.setToolTipText(GUITools.toHTML(
				"Close the current model without saving.", 40));
		toolbar.add(closeButton);
		toolbar.addSeparator();
		if (GUITools.ICON_LEMON_TINY != null)
			toolbar
					.add(GUITools
							.createButton(GUITools.ICON_LEMON_TINY, this,
									Command.SQUEEZE,
									"Generate kinetic equations for all reactions in this model in one step."));

		if (GUITools.ICON_LATEX_TINY != null)
			toolbar.add(GUITools.createButton(GUITools.ICON_LATEX_TINY, this,
					Command.TO_LATEX, "Export this model to a LaTeX report."));

		if (GUITools.ICON_STABILITY_SMALL != null)
			toolbar.add(GUITools.createButton(GUITools.ICON_STABILITY_SMALL,
					this, Command.CHECK_STABILITY,
					"Analyze the stability properties of the selected model."));

		if (GUITools.ICON_STRUCTURAL_MODELING_TINY != null)
			toolbar.add(GUITools.createButton(
					GUITools.ICON_STRUCTURAL_MODELING_TINY, this,
					Command.STRUCTURAL_KINETIC_MODELLING,
					"Identify key reactins with structural kinetic modeling."));

		if (GUITools.ICON_DIAGRAM_TINY != null)
			toolbar.add(GUITools
					.createButton(GUITools.ICON_DIAGRAM_TINY, this,
							Command.SIMULATE,
							"Dynamically simulate the current model."));
		if (GUITools.ICON_SETTINGS_TINY != null)
			toolbar.add(GUITools.createButton(GUITools.ICON_SETTINGS_TINY,
					this, Command.SET_PREFERENCES, "Adjust your preferences."));

		toolbar.addSeparator();
		JButton helpButton = new JButton(GUITools.ICON_HELP_TINY);
		helpButton.addActionListener(this);
		helpButton.setActionCommand(Command.ONLINE_HELP.toString());
		helpButton.setToolTipText(GUITools.toHTML("Open the online help.", 40));
		toolbar.add(helpButton);
		return toolbar;
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
		Icon icon = GUITools.ICON_LOGO_SMALL;
		logo = new JLabel(GUITools.toHTML("<br><br><br><br><br>Version: "
				+ SBMLsqueezer.getVersionNumber()), icon, JLabel.CENTER);
		logo.setPreferredSize(new Dimension(icon.getIconWidth() + 125, icon
				.getIconHeight() + 75));
		setEnabled(false, Command.SAVE_FILE, Command.CLOSE_FILE,
				Command.SQUEEZE, Command.TO_LATEX, Command.CHECK_STABILITY,
				Command.STRUCTURAL_KINETIC_MODELLING, Command.SIMULATE);
		tabbedPane = new JTabbedPaneWithCloseIcons();
		tabbedPane.addChangeListener(this);
		tabbedPane.addChangeListener(sbmlIO);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		setSBMLsqueezerBackground();
		setIconImage(GUITools.ICON_LEMON);
		for (Model m : sbmlIO.getListOfModels()) {
			checkForSBMLErrors(this, m, sbmlIO.getWarnings(),
					((Boolean) settings.get(CfgKeys.SHOW_SBML_WARNINGS))
							.booleanValue());
			if (m != null)
				addModel(m);
		}
	}

	/**
	 * Reads a model from the given file and adds it to the GUI if possible.
	 * 
	 * @param file
	 */
	void readModel(File file) {
		try {
			Model model = sbmlIO.readModel(file.getAbsolutePath());
			checkForSBMLErrors(this, model, sbmlIO.getWarnings(),
					((Boolean) settings.get(CfgKeys.SHOW_SBML_WARNINGS))
							.booleanValue());
			if (model != null) {
				addModel(model);
				String path = file.getAbsolutePath();
				String oldPath = settings.get(CfgKeys.SBML_FILE).toString();
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
					settings.put(CfgKeys.SBML_FILE, path);
				}
				if (!file.getParentFile().equals(
						settings.get(CfgKeys.OPEN_DIR).toString()))
					settings.put(CfgKeys.OPEN_DIR, file.getParentFile());
			}
		} catch (Exception exc) {
			JOptionPane.showMessageDialog(this, GUITools.toHTML(exc
					.getMessage(), 40), exc.getClass().getSimpleName(),
					JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
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
	private void setEnabled(boolean state, Command... commands) {
		int i, j;
		Set<String> setOfCommands = new HashSet<String>();
		for (Command command : commands)
			setOfCommands.add(command.toString());
		for (i = 0; i < getJMenuBar().getMenuCount(); i++) {
			JMenu menu = getJMenuBar().getMenu(i);
			for (j = 0; j < menu.getItemCount(); j++) {
				JMenuItem item = menu.getItem(j);
				if (item instanceof JMenu) {
					JMenu m = (JMenu) item;
					boolean containsCommand = false;
					for (int k = 0; k < m.getItemCount(); k++) {
						JMenuItem it = m.getItem(k);
						if (it != null
								&& it.getActionCommand() != null
								&& setOfCommands
										.contains(it.getActionCommand())) {
							it.setEnabled(state);
							containsCommand = true;
						}
					}
					if (containsCommand)
						m.setEnabled(state);
				}
				if (item != null && item.getActionCommand() != null
						&& setOfCommands.contains(item.getActionCommand()))
					item.setEnabled(state);
			}
		}
		for (i = 0; i < toolbar.getComponentCount(); i++) {
			Object o = toolbar.getComponent(i);
			if (o instanceof JButton) {
				JButton b = (JButton) o;
				if (setOfCommands.contains(b.getActionCommand())) {
					b.setEnabled(state);
					if (b.getIcon() != null && b.getIcon() instanceof CloseIcon)
						((CloseIcon) b.getIcon()).setColor(state ? Color.BLACK
								: Color.GRAY);
				}
			}
		}
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
				setEnabled(false, Command.SAVE_FILE, Command.CLOSE_FILE,
						Command.SQUEEZE, Command.TO_LATEX,
						Command.CHECK_STABILITY,
						Command.STRUCTURAL_KINETIC_MODELLING, Command.SIMULATE);
				if (settings.get(CfgKeys.SBML_FILE).toString().length() > 0)
					setEnabled(true, Command.OPEN_LAST_FILE);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent we) {
		if (we.getSource() instanceof JHelpBrowser)
			setEnabled(true, Command.ONLINE_HELP, Command.LICENSE);
		else if (we.getSource() instanceof SBMLsqueezerUI)
			try {
				SBMLsqueezer.saveProperties(settings);
			} catch (FileNotFoundException exc) {
				exc.printStackTrace();
				JOptionPane.showMessageDialog(this, exc.getMessage(), exc
						.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			} catch (IOException exc) {
				exc.printStackTrace();
				JOptionPane.showMessageDialog(this, exc.getMessage(), exc
						.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
			}
		else if (we.getSource() instanceof SimulationDialog)
			settings
					.putAll(((SimulationDialog) we.getSource()).getProperties());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeactivated(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeiconified(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent arg0) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent arg0) {
	}

	/**
	 * Write the selected model into a LaTeX file.
	 * 
	 * @param out
	 */
	private void writeLaTeX(final File out) {
		try {
			LaTeXExport.writeLaTeX(sbmlIO.getSelectedModel(), out, settings);
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
			JOptionPane.showMessageDialog(this, exc.getMessage(), exc
					.getClass().getName(), JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
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
		} catch (SBMLException exc) {
			JOptionPane.showMessageDialog(null, GUITools.toHTML(exc
					.getMessage(), 40), exc.getClass().getSimpleName(),
					JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(null, GUITools.toHTML(exc
					.getMessage(), 40), exc.getClass().getSimpleName(),
					JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		}
	}

	/**
	 * Writes the currently selected model into an ASCII text file.
	 * 
	 * @param out
	 */
	private void writeText(final File out) {
		try {
			new TextExport(sbmlIO.getSelectedModel(), out, settings);
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
			JOptionPane.showMessageDialog(this, exc.getMessage(), exc
					.getClass().getName(), JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		}
	}
}
