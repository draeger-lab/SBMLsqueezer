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
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.io.IOProgressListener;
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
 * 
 */
class FileReaderThread extends Thread implements Runnable {

	private File file;
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
		WindowListener, ChangeListener, IOProgressListener {

	/**
	 * This is what the graphical user interface of SBMLsqueezer can do...
	 * 
	 * @author Andreas Dr&auml;ger <a
	 *         href="mailto:andreas.draeger@uni-tuebingen.de"
	 *         >andreas.draeger@uni-tuebingen.de</a>
	 * @date 2009-09-11
	 */
	public static enum Command {
		/**
		 * 
		 */
		CHECK_STABILITY,
		/**
		 * 
		 */
		CLOSE_FILE,
		/**
		 * 
		 */
		ONLINE_HELP,
		/**
		 * 
		 */
		OPEN_FILE,
		/**
		 * Opens the SBML file that was opened last time.
		 */
		OPEN_LAST_FILE,
		/**
		 * 
		 */
		SAVE_FILE,
		/**
		 * 
		 */
		SET_PREFERENCES,
		/**
		 * 
		 */
		SQUEEZE,
		/**
		 * 
		 */
		STRUCTURAL_KINETIC_MODELLING,
		/**
		 * 
		 */
		TO_LATEX
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
		JHelpBrowser helpBrowser = new JHelpBrowser(owner, String.format(
				"SBMLsqueezer %s - Online Help", SBMLsqueezer
						.getVersionNumber()));
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

	private JLabel label;

	/**
	 * 
	 */
	private JLabel logo;

	private JDialog progressDialog;

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
		this.sbmlIO.addIOProgressListener(this);
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
		boolean done = false;
		if (e.getSource() instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) e.getSource();
			if (item.getText().equals("Exit")) {
				SBMLsqueezer.saveProperties(settings);
				System.exit(0);
			} else if (item.getText().equals("About")) {
				JBrowser browser = new JBrowser(Resource.class
						.getResource("html/about.htm"));
				browser.removeHyperlinkListener(browser);
				browser.addHyperlinkListener(new SystemBrowser());
				browser.setBorder(BorderFactory.createEtchedBorder());
				JOptionPane.showMessageDialog(this, browser,
						"About SBMLsqueezer", JOptionPane.INFORMATION_MESSAGE);
				done = true;
			}
		}
		if (!done) {
			JFileChooser chooser;
			switch (Command.valueOf(e.getActionCommand())) {
			case SQUEEZE:
				KineticLawSelectionDialog klsd;
				if (e.getSource() instanceof Reaction) {
					// just one reaction
					klsd = new KineticLawSelectionDialog(this, settings,
							sbmlIO, ((Reaction) e.getSource()).getId());
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
							JFileChooser.FILES_ONLY,
							SBFileFilter.TeX_FILE_FILTER);
					if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
						File out = chooser.getSelectedFile();
						String path = out.getParent();
						if (!path.equals(dir))
							settings.put(CfgKeys.LATEX_DIR, path);
						if (!out.exists()
								|| GUITools.overwriteExistingFileDialog(this,
										out) == JOptionPane.YES_OPTION)
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
				chooser = GUITools.createJFileChooser(settings.get(
						CfgKeys.OPEN_DIR).toString(), false, false,
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
				chooser = GUITools.createJFileChooser(settings.get(
						CfgKeys.SAVE_DIR).toString(), false, false,
						JFileChooser.FILES_ONLY, filterText, filterTeX,
						filterSBML);
				if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
					final File out = chooser.getSelectedFile();
					if (out.getParentFile() != null)
						settings.put(CfgKeys.SAVE_DIR, out.getParentFile()
								.getAbsolutePath());
					if (!out.exists()
							|| GUITools.overwriteExistingFileDialog(this, out) == JOptionPane.YES_OPTION) {
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
							Command.STRUCTURAL_KINETIC_MODELLING);
					setEnabled(true, Command.OPEN_LAST_FILE);
				}
				break;
			case ONLINE_HELP:
				setEnabled(false, Command.ONLINE_HELP);
				showOnlineHelp(this, this);
				break;
			case CHECK_STABILITY:
				// Next version!
				// StabilityDialog stabilitydialog = new StabilityDialog(this);
				// stabilitydialog.showStabilityDialog(settings, sbmlIO);
				break;
			case STRUCTURAL_KINETIC_MODELLING:
				break;
			default:
				break;
			}
		}
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
				Command.STRUCTURAL_KINETIC_MODELLING);
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
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				InputEvent.ALT_DOWN_MASK));
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(closeItem);
		fileMenu.add(lastOpened);
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
		JMenuItem squeezeItem = new JMenuItem("Squeeze");
		squeezeItem.setAccelerator(KeyStroke.getKeyStroke('Q',
				InputEvent.CTRL_DOWN_MASK));
		squeezeItem.addActionListener(this);
		squeezeItem.setActionCommand(Command.SQUEEZE.toString());
		squeezeItem.setIcon(GUITools.ICON_LEMON_TINY);
		JMenuItem latexItem = new JMenuItem("Export to LaTeX");
		latexItem.setAccelerator(KeyStroke.getKeyStroke('E',
				InputEvent.CTRL_DOWN_MASK));
		latexItem.setIcon(GUITools.ICON_LATEX_TINY);
		latexItem.addActionListener(this);
		latexItem.setActionCommand(Command.TO_LATEX.toString());
		JMenuItem stabilityItem = new JMenuItem("Analyze Stability");
		stabilityItem.setIcon(GUITools.ICON_STABILITY_SMALL);
		stabilityItem.addActionListener(this);
		stabilityItem.setActionCommand(Command.CHECK_STABILITY.toString());
		JMenuItem structuralItem = new JMenuItem("Structural Kinetic Modelling");
		// stabilityItem.setIcon();
		structuralItem.addActionListener(this);
		structuralItem.setActionCommand(Command.STRUCTURAL_KINETIC_MODELLING
				.toString());
		JMenuItem preferencesItem = new JMenuItem("Preferences",
				GUITools.ICON_TICK_TINY);
		preferencesItem.setActionCommand(Command.SET_PREFERENCES.toString());
		preferencesItem.addActionListener(this);
		preferencesItem.setMnemonic(preferencesItem.getText().charAt(0));
		editMenu.add(squeezeItem);
		editMenu.add(latexItem);
		// Next version!
		// editMenu.add(stabilityItem);
		// editMenu.add(structuralItem);
		editMenu.add(preferencesItem);

		/*
		 * Help menu
		 */
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(helpMenu.getText().charAt(0));
		JMenuItem about = new JMenuItem("About", GUITools.ICON_INFO_TINY);
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		about.addActionListener(this);
		JMenuItem help = new JMenuItem("Online help", GUITools.ICON_HELP_TINY);
		help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		help.addActionListener(this);
		help.setActionCommand(Command.ONLINE_HELP.toString());
		helpMenu.add(help);
		helpMenu.add(about);

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
		JButton closeButton = new JButton(new CloseIcon(false));
		closeButton.setActionCommand(Command.CLOSE_FILE.toString());
		closeButton.addActionListener(this);
		closeButton.setToolTipText(GUITools.toHTML(
				"Close the current model without saving.", 40));
		toolbar.add(closeButton);
		toolbar.addSeparator();
		if (GUITools.ICON_LEMON_TINY != null) {
			JButton squeezeButton = new JButton(GUITools.ICON_LEMON_TINY);
			squeezeButton.setActionCommand(Command.SQUEEZE.toString());
			squeezeButton.addActionListener(this);
			squeezeButton
					.setToolTipText(GUITools
							.toHTML(
									"Generate kinetic equations for all reactions in this model in one step.",
									40));
			toolbar.add(squeezeButton);
		}
		if (GUITools.ICON_LATEX_TINY != null) {
			JButton latexButton = new JButton(GUITools.ICON_LATEX_TINY);
			latexButton.addActionListener(this);
			latexButton.setActionCommand(Command.TO_LATEX.toString());
			latexButton.setToolTipText(GUITools.toHTML(
					"Export this model to a LaTeX report.", 40));
			toolbar.add(latexButton);
		}
		// Next version!
		// if (GUITools.ICON_STABILITY_SMALL != null) {
		// JButton stabilityButton = new JButton(GUITools.ICON_STABILITY_SMALL);
		// stabilityButton.addActionListener(this);
		// stabilityButton
		// .setActionCommand(Command.CHECK_STABILITY.toString());
		// stabilityButton.setToolTipText(GUITools.toHTML(
		// "Analyze the stability properties of the selected model.",
		// 40));
		// toolbar.add(stabilityButton);
		// }
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
				Command.STRUCTURAL_KINETIC_MODELLING);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.io.IOProgressListener#progress(java.lang.Object)
	 */
	public void ioProgressOn(Object currObject) {
		if (currObject != null) {
			if (label == null)
				label = new JLabel();
			StringBuilder sb = new StringBuilder();
			sb.append(currObject.getClass().getSimpleName());
			if (currObject instanceof NamedSBase) {
				sb.append(' ');
				NamedSBase nsb = (NamedSBase) currObject;
				sb.append(nsb.getId());
				if (nsb.getName() != null && nsb.getName().length() > 0) {
					sb.append(' ');
					sb.append(nsb.getName());
				}
			}
			label.setText(GUITools.toHTML(sb.toString(), 40));
			if (progressDialog == null) {
				progressDialog = new JDialog(this, "SBML IO progress");
				progressDialog.getContentPane().add(label);
				progressDialog.setSize(200, 150);
				progressDialog.setLocationRelativeTo(this);
				progressDialog
						.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
				progressDialog.setVisible(true);
			}
		} else if (progressDialog != null)
			progressDialog.dispose();
	}

	/**
	 * Reads a model from the given file and adds it to the GUI if possible.
	 * 
	 * @param file
	 */
	void readModel(File file) {
		try {
			Model model = sbmlIO.readModel(file.getAbsolutePath());
			ioProgressOn(null);
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
						Command.STRUCTURAL_KINETIC_MODELLING);
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
		if (we.getWindow() instanceof KineticLawSelectionDialog) {
			KineticLawSelectionDialog klsd = (KineticLawSelectionDialog) we
					.getWindow();
			if (klsd.isKineticsAndParametersStoredInSBML()) {
				SBMLModelSplitPane split = (SBMLModelSplitPane) tabbedPane
						.getSelectedComponent();
				split.init(sbmlIO.getSelectedModel(), true);
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
			setEnabled(true, Command.ONLINE_HELP);
		else if (we.getSource() instanceof SBMLsqueezerUI)
			SBMLsqueezer.saveProperties(settings);
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
