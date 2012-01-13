/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.prefs.BackingStoreException;

import javax.swing.Icon;
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

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.gui.LaTeXExportDialog;
import org.sbml.tolatex.io.LaTeXReportGenerator;
import org.sbml.tolatex.io.TextExport;

import de.zbit.AppConf;
import de.zbit.gui.ActionCommand;
import de.zbit.gui.BaseFrame;
import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.ImageTools;
import de.zbit.io.SBFileFilter;
import de.zbit.sbml.gui.SBMLModelSplitPane;
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
		ChangeListener, WindowListener {

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
		 * 
		 * @see de.zbit.gui.ActionCommand#getName()
		 */
		public String getName() {
			return StringUtil.firstLetterUpperCase(toString().toLowerCase()
					.replace('_', ' '));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.zbit.gui.ActionCommand#getToolTip()
		 */
		public String getToolTip() {
			return null;
		}
	}

	static {
		ImageTools.initImages(LaTeXExportDialog.class.getResource("img"));
		ImageTools.initImages(SBMLsqueezer.class.getResource("resources/img"));
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
				JOptionPane.showMessageDialog(parent, StringUtil.toHTML(
						message, 40), "Error", JOptionPane.ERROR_MESSAGE);
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
	private JTabbedPaneWithCloseIcons tabbedPane;

	/**
	 * 
	 */
	private SBPreferences prefs;

	/**
	 * 
	 * @param io
	 * @param appConf 
	 */
	public SBMLsqueezerUI(SBMLio io, AppConf appConf) {
		super(appConf);
		this.prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
		this.sbmlIO = io;
		setEnabled(false, Command.SQUEEZE, Command.TO_LATEX,
				Command.CHECK_STABILITY, Command.STRUCTURAL_KINETIC_MODELLING,
				Command.SIMULATE);
		setSBMLsqueezerBackground();
		// TODO
		// setIconImage(UIManager.getIcon("IMAGE_LEMON"));
		for (Model m : sbmlIO.getListOfModels()) {
			checkForSBMLErrors(this, m, sbmlIO.getWarnings(), prefs
					.getBoolean(SqueezerOptions.SHOW_SBML_WARNINGS));
			if (m != null) {
				addModel(m);
			}
		}
	}

  /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		switch (Command.valueOf(e.getActionCommand())) {
		case SQUEEZE:
			KineticLawSelectionDialog klsd;
			if (e.getSource() instanceof Reaction) {
				// just one reaction
				klsd = new KineticLawSelectionDialog(this, sbmlIO,
						((Reaction) e.getSource()).getId());
			} else {
				// whole model
				klsd = new KineticLawSelectionDialog(this, sbmlIO);
				klsd.addWindowListener(this);
				klsd.setVisible(true);
			}
			if (klsd.isKineticsAndParametersStoredInSBML()) {
				SBMLModelSplitPaneExtended split = (SBMLModelSplitPaneExtended) tabbedPane
						.getSelectedComponent();
				try {
					split.init(sbmlIO.getSelectedModel(), true);
				} catch (Exception e1) {
					e1.printStackTrace();
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
		SBMLModelSplitPaneExtended split;
		try {
			split = new SBMLModelSplitPaneExtended(model);
			split.addActionListener(this);
			tabbedPane.add(model.getId(), split);
			tabbedPane.setSelectedIndex(tabbedPane.getComponentCount() - 1);
			setEnabled(true, BaseAction.FILE_SAVE_AS, BaseAction.FILE_CLOSE,
					Command.SQUEEZE, Command.TO_LATEX, Command.CHECK_STABILITY,
					Command.STRUCTURAL_KINETIC_MODELLING, Command.SIMULATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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
				GUITools.createJMenuItem(this, Command.SQUEEZE, UIManager
						.getIcon("ICON_LEMON_TINY"), KeyStroke.getKeyStroke(
						'Q', InputEvent.CTRL_DOWN_MASK)),
				GUITools.createJMenuItem(this, Command.TO_LATEX, UIManager
						.getIcon("ICON_LATEX_16"), KeyStroke.getKeyStroke(
						'E', InputEvent.CTRL_DOWN_MASK)) };
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
			checkForSBMLErrors(this, model, sbmlIO.getWarnings(), prefs
					.getBoolean(SqueezerOptions.SHOW_SBML_WARNINGS));
			if (model != null) {
				addModel(model);
				String path = file.getAbsolutePath();
				String oldPath = prefs.get(SqueezerOptions.SBML_IN_FILE)
						.toString();
				if (!path.equals(oldPath)) {
					prefs.put(SqueezerOptions.SBML_IN_FILE, path);
					prefs.flush();
				}
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
				setEnabled(false, BaseAction.FILE_SAVE_AS, BaseAction.FILE_CLOSE,
						Command.SQUEEZE, Command.TO_LATEX,
						Command.CHECK_STABILITY,
						Command.STRUCTURAL_KINETIC_MODELLING, Command.SIMULATE);
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
					SBMLModelSplitPaneExtended split = (SBMLModelSplitPaneExtended) tabbedPane
							.getSelectedComponent();
					try {
						split.init(sbmlIO.getSelectedModel(), true);
					} catch (Exception e) {
						e.printStackTrace();
					}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#closeFile()
	 */
	public boolean closeFile() {
		boolean change = false;
		if (tabbedPane.getComponentCount() > 0) {
			tabbedPane.remove(tabbedPane.getSelectedComponent());
			change = true;
		}
		if (tabbedPane.getComponentCount() == 0) {
			setEnabled(false, BaseAction.FILE_SAVE_AS, BaseAction.FILE_CLOSE,
					Command.SQUEEZE, Command.TO_LATEX, Command.CHECK_STABILITY,
					Command.STRUCTURAL_KINETIC_MODELLING, Command.SIMULATE);
		}
		return change;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#createJToolBar()
	 */
	protected JToolBar createJToolBar() {
		return  createDefaultToolBar();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#createMainComponent()
	 */
	protected Component createMainComponent() {
		colorDefault = getContentPane().getBackground();
		Icon icon = UIManager.getIcon("ICON_LOGO_SMALL");
		logo = new JLabel(StringUtil.toHTML("<br><br><br><br><br>Version: "
				+ System.getProperty("app.version")), icon, JLabel.CENTER);
		if (icon != null) {
			logo.setPreferredSize(new Dimension(icon.getIconWidth() + 125, icon
					.getIconHeight() + 75));
		}
		tabbedPane = new JTabbedPaneWithCloseIcons();
		tabbedPane.addChangeListener(this);
		tabbedPane.addChangeListener(sbmlIO);
		return tabbedPane;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#exit()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#getURLAboutMessage()
	 */
	public URL getURLAboutMessage() {
		return getClass().getResource("../resources/html/about.htm");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#getURLLicense()
	 */
	public URL getURLLicense() {
		return getClass().getResource("../resources/html/License.html");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#getURLOnlineHelp()
	 */
	public URL getURLOnlineHelp() {
		return getClass().getResource("../resources/html/help.html");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#openFile(java.io.File[])
	 */
	public File[] openFile(File... files) {
		SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
		if ((files == null) || (files.length == 0)) {
			files = GUITools.openFileDialog(this, prefs
					.get(GUIOptions.OPEN_DIR), false, true,
					JFileChooser.FILES_ONLY, SBFileFilter
							.createSBMLFileFilter());
		}
		if (files != null) {
			for (File file : files) {
				new FileReaderThread(this, file);
			}
		}
		return files;
	}

	/* (non-Javadoc)
	 * @see de.zbit.gui.BaseFrame#saveFile()
	 */
	public File saveFile() {
		SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
		SBFileFilter filterText = SBFileFilter.createTextFileFilter();
		SBFileFilter filterTeX = SBFileFilter.createTeXFileFilter();
		SBFileFilter filterSBML = SBFileFilter.createSBMLFileFilter();
		JFileChooser chooser = GUITools.createJFileChooser(prefs
				.get(GUIOptions.SAVE_DIR), false, false,
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
        if (SBFileFilter.hasFileType(out, SBFileFilter.FileType.SBML_FILES)) {
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
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.zbit.gui.BaseFrame#getMaximalFileHistorySize()
	 */
	public short getMaximalFileHistorySize() {
		return 10;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}
}
