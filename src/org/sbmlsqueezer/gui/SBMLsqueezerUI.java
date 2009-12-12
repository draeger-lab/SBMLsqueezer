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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbmlsqueezer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;

import org.sbmlsqueezer.gui.table.KineticLawJTable;
import org.sbmlsqueezer.gui.table.KineticLawTableModel;
import org.sbmlsqueezer.io.LaTeXExport;
import org.sbmlsqueezer.io.MyFileFilter;
import org.sbmlsqueezer.io.ODEwriter;
import org.sbmlsqueezer.kinetics.IllegalFormatException;
import org.sbmlsqueezer.kinetics.KineticLawGenerator;
import org.sbmlsqueezer.kinetics.LawListener;
import org.sbmlsqueezer.kinetics.ModificationException;
import org.sbmlsqueezer.kinetics.RateLawNotApplicableException;
import org.sbmlsqueezer.resources.Resource;

/**
 * This is the main GUI class.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:supper@genomatix.de">Jochen Supper</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 3, 2007
 */
public class SBMLsqueezerUI extends JFrame implements ActionListener,
		WindowListener, LawListener {

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -5980678130366530716L;

	private static final int fullHeight = 720;

	// UI ELEMENTS DEFINITION: ReactionFrame
	private boolean KineticsAndParametersStoredInSBML = false;

	// CELL DESIGNER VARIABELS
	private SBMLsqueezerPlugin plugin;

	private int numOfWarnings = 0;

	private JSettingsPanel settingsPanel;

	private JButton options;

	private JPanel centralPanel;

	private JButton helpButton;

	private KineticLawGenerator klg;

	private JPanel footPanel;

	private JProgressBar progressBar;

	private JDialog progressDialog;

	/**
	 * Constructor
	 */
	public SBMLsqueezerUI() {
		super();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		} catch (InstantiationException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		} catch (IllegalAccessException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		} catch (UnsupportedLookAndFeelException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		}
		plugin = null;
		setTitle("SBMLsqueezer");
		setAlwaysOnTop(true);
		try {
			Image image = ImageIO.read(Resource.class
					.getResource("img/icon.png")
			/*
			 * new File(System.getProperty("user.dir") +
			 * System.getProperty("file.separator") + "resources" +
			 * System.getProperty("file.separator") + "images" +
			 * System.getProperty("file.separator") + "icon.png")
			 */);
			setIconImage(image);
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		}
	}

	/**
	 * This constructor allows us to store the given model in a text file. This
	 * can be a LaTeX or another format.
	 * 
	 * @param model
	 */
	public SBMLsqueezerUI(PluginModel model) {
		this();
		ImageIcon icon = null;
		try {
			Image image = ImageIO.read(Resource.class
					.getResource("img/Lemon_small.png"));
			icon = new ImageIcon(image);
			// .getScaledInstance(100, 100, Image.SCALE_SMOOTH));
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(null, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		}
		LaTeXExportDialogPanel panel = new LaTeXExportDialogPanel();
		if (JOptionPane.showConfirmDialog(this, panel, "LaTeX export",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				icon) == JOptionPane.OK_OPTION) {
			try {
				BufferedWriter buffer = new BufferedWriter(new FileWriter(panel
						.getTeXFile()));
				LaTeXExport exporter = new LaTeXExport(panel.isLandscape(),
						panel.isIDsInTWFont(), panel.getFontSize(), panel
								.getPaperSize(), panel.isTitlePage(), panel
								.isNameInEquations());
				buffer.write(exporter.toLaTeX(model).toString());
				buffer.close();
				dispose();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param model
	 * @param reaction
	 */
	public SBMLsqueezerUI(PluginModel model, PluginReaction reaction) {
		this();
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new MyFileFilter(false, true));
		File file = null;
		int state = chooser.showOpenDialog(null);
		if (state == JFileChooser.APPROVE_OPTION)
			file = chooser.getSelectedFile();
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
			buffer.write(new LaTeXExport().toLaTeX(model, reaction).toString());
			buffer.close();
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(null, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * DEFAULT Constructor
	 * 
	 * @param plugin
	 */
	public SBMLsqueezerUI(SBMLsqueezerPlugin plugin) {
		this();
		this.plugin = plugin;
		init();
	}

	/**
	 * This constructor is necessary for the GUI to generate just one single
	 * rate equation for the given reaction.
	 * 
	 * @param plugin
	 * @param reaction
	 */
	public SBMLsqueezerUI(SBMLsqueezerPlugin plugin, PluginReaction reaction) {
		this();
		this.plugin = plugin;

		PluginModel model = plugin.getSelectedModel();
		ImageIcon icon = null;
		try {
			Image image = ImageIO.read(Resource.class
					.getResource("img/Lemon_small.png"));
			icon = new ImageIcon(image);
			// .getScaledInstance(100, 100, Image.SCALE_SMOOTH));
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(null, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		}
		try {
			klg = new KineticLawGenerator(plugin);
			KineticLawSelectionPanel messagePanel = new KineticLawSelectionPanel(
					klg, model, reaction);
			if (JOptionPane.showConfirmDialog(this, messagePanel,
					"SBMLsqueezer", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE, icon) == JOptionPane.OK_OPTION) {
				if (!messagePanel.getExistingRateLawSelected()) {
					short equationType = messagePanel.getSelectedKinetic();
					reaction.setReversible(messagePanel.getReversible());
					plugin.notifySBaseChanged(reaction);
					reaction = klg.storeLaw(plugin, klg.createKineticLaw(model,
							reaction, equationType, messagePanel
									.getReversible()), messagePanel
							.getReversible());
					klg.removeUnnecessaryParameters(plugin);
				}
			}
		} catch (RateLawNotApplicableException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		} catch (RuntimeException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", "Warning", JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", "Warning", JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		} catch (IllegalFormatException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", "Warning", JOptionPane.WARNING_MESSAGE);
			exc.printStackTrace();
		}
	}

	// public static void listLoadedLibraries() throws Exception {
	// Field loadedLibraryNamesField = ClassLoader.class
	// .getDeclaredField("loadedLibraryNames");
	// loadedLibraryNamesField.setAccessible(true);
	// @SuppressWarnings("unchecked")
	// Vector<String> loadedLibraryNames = (Vector<String>)
	// loadedLibraryNamesField
	// .get(null);
	// for (String string : loadedLibraryNames) {
	// System.out.println(string);
	// }
	// }

	/**
	 * Just for debuggin purposes.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SBMLsqueezerUI ui = new SBMLsqueezerUI();
		ui.init();
		ui.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			String text = ((JButton) e.getSource()).getText();
			if (text.equals("show options")) {
				showSettingsPanel();
			} else if (text.equals("hide options")) {
				try {
					Image image = ImageIO.read(Resource.class.getResource(
					/*
					 * new File(System.getProperty("user.dir") +
					 * System.getProperty("file.separator") + "resources" +
					 * System.getProperty("file.separator") + "images" +
					 * System.getProperty("file.separator") +
					 */"img/rightarrow.png"));
					image = image.getScaledInstance(10, 10, Image.SCALE_SMOOTH);
					options.setIcon(new ImageIcon(image));
					options.setIconTextGap(5);
				} catch (IOException exc) {
					exc.printStackTrace();
				}
				options.setText("show options");
				options
						.setToolTipText("<html>Customize the advanced settings.</html>");
				centralPanel.removeAll();
				JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				panel.add(options);
				centralPanel.add(panel, BorderLayout.NORTH);
				validate();
				pack();

			} else if (text.equals("Help")) {
				JHelpBrowser helpBrowser = new JHelpBrowser(this,
						"SBMLsqueezer - online help");
				helpBrowser.addWindowListener(this);
				helpBrowser.setLocationRelativeTo(this);
				helpBrowser.setSize(640, 640);
				helpButton.setEnabled(false);
				helpBrowser.setVisible(true);

			} else if (text.equals("Cancel")) {
				dispose();

				/*
				 * } else if (text.equals("Apply")) {
				 * getContentPane().remove(settingsPanel); pack();
				 * setLocationRelativeTo(null);//
				 */

			} else if (text.equals("Restore")) {
				settingsPanel.restoreDefaults();
				getContentPane().remove(settingsPanel);
				pack();
				setLocationRelativeTo(null);

			} else if (text.equals("Generate")) {
				if (plugin != null)
					try {
						klg = new KineticLawGenerator(
								plugin,
								settingsPanel
										.isSetAllReactionsAreEnzymeCatalyzed(),
								settingsPanel
										.isSetAllParametersAreAddedGlobally(),
								settingsPanel
										.isSetGenerateKineticsForAllReactions(),
								settingsPanel.getUniUniType(), settingsPanel
										.getBiUniType(), settingsPanel
										.getBiBiType(), settingsPanel
										.getListOfPossibleEnzymes());
						klg.findExistingLawsAndGenerateMissingLaws(plugin
								.getSelectedModel(), settingsPanel
								.isSetTreatAllReactionsReversible());
						if (klg.getFastReactions().size() > 0) {
							String message = "<html><head></head><body><p>The model contains ";
							if (klg.getFastReactions().size() > 1)
								message += "fast reactions";
							else
								message += "the fast reaction "
										+ klg.getFastReactions().get(0).getId();
							message += ". This feature is currently not<br>"
									+ "supported by SBMLsqueezer. Rate laws can still be generated properly<br>"
									+ "but the fast attribute ";
							if (klg.getFastReactions().size() > 1) {
								message += "of the following reactions is beeing ignored:<br>"
										+ "<ul type=\"disc\">";
								for (int i = 0; i < klg.getFastReactions()
										.size(); i++)
									message += "<li>"
											+ klg.getFastReactions().get(i)
													.getId() + "</li>";
								message += "</ul>";
							} else
								message += "is beeing ignored.";
							message += "</p></body></html>";
							JOptionPane.showMessageDialog(this, message,
									"Fast Reactions",
									JOptionPane.WARNING_MESSAGE);
						}

						JPanel reactionsPanel = new JPanel(new BorderLayout());
						JTable tableOfKinetics = new KineticLawJTable(klg,
								settingsPanel
										.getMaxRealisticNumberOfReactants(),
								settingsPanel
										.isSetTreatAllReactionsReversible());
						numOfWarnings = ((KineticLawTableModel) tableOfKinetics
								.getModel()).getNumOfWarnings();
						tableOfKinetics
								.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

						JScrollPane scroll;

						if (tableOfKinetics.getRowCount() == 0) {
							JEditorPane pane = new JEditorPane(
									plugin.getSelectedModel().getNumReactions() > 0 ? Resource.class
											.getResource("html/NoNewKineticsCreated.html")
											: Resource.class
													.getResource("html/ModelDoesNotContainAnyReactions.html"));
							pane.addHyperlinkListener(new SystemBrowser());
							pane.setBackground(Color.WHITE);
							pane.setEditable(false);
							scroll = new JScrollPane(pane,
									JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						} else {
							scroll = new JScrollPane(tableOfKinetics,
									JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						}
						scroll.setBorder(BorderFactory
								.createBevelBorder(BevelBorder.LOWERED));
						scroll.setBackground(Color.WHITE);
						reactionsPanel.add(scroll, BorderLayout.CENTER);

						JLabel numberOfWarnings = new JLabel(
								"<html><table align=left width=500 cellspacing=10><tr><td>"
										+ "<b>Kinetic Equations</b></td><td>"
										+ "<td>Number of warnings (red): "
										+ numOfWarnings
										+ "</td></tr></table></htlm>");
						numberOfWarnings
								.setToolTipText("<html>The number of reactions an unlikely number "
										+ "of reactants<br>"
										+ "These are also highlighted in red in the table.</html>");
						reactionsPanel
								.add(numberOfWarnings, BorderLayout.NORTH);

						centralPanel.removeAll();
						centralPanel.add(reactionsPanel, BorderLayout.CENTER);
						getContentPane().remove(footPanel);
						getContentPane().add(footPanel = getFootPanel(1),
								BorderLayout.SOUTH);
						setResizable(true);
						setSize(getWidth(), 640);
						validate();

					} catch (IllegalFormatException exc) {
						JOptionPane.showMessageDialog(this, "<html>"
								+ exc.getMessage() + "</html>", exc.getClass()
								.getName(), JOptionPane.ERROR_MESSAGE);
						exc.printStackTrace();
					} catch (ModificationException exc) {
						JOptionPane.showMessageDialog(this, "<html>"
								+ exc.getMessage() + "</html>", exc.getClass()
								.getName(), JOptionPane.ERROR_MESSAGE);
						exc.printStackTrace();
					} catch (RateLawNotApplicableException exc) {
						JOptionPane.showMessageDialog(this, "<html>"
								+ exc.getMessage() + "</html>", exc.getClass()
								.getName(), JOptionPane.ERROR_MESSAGE);
						exc.printStackTrace();
					} catch (IOException exc) {
						JOptionPane.showMessageDialog(this, "<html>"
								+ exc.getMessage() + "</html>", exc.getClass()
								.getName(), JOptionPane.ERROR_MESSAGE);
						exc.printStackTrace();
					}

				else
					klg = null;

			} else if (text.equals("Back")) {
				getContentPane().remove(footPanel);
				getContentPane().add(footPanel = getFootPanel(0),
						BorderLayout.SOUTH);
				getContentPane().remove(centralPanel);
				getContentPane().add(centralPanel = initOptionsPanel(),
						BorderLayout.CENTER);
				setResizable(false);
				showSettingsPanel();
			} else if (text.equals("Export")) {
				/*
				 * new Thread(new Runnable() { public void run() {//
				 */
				exportKineticEquations();
				/*
				 * } }).start();/
				 */

			} else if (text.equals("Apply")) {
				dispose();
				if (!KineticsAndParametersStoredInSBML) {
					KineticsAndParametersStoredInSBML = true;
					if (plugin != null)
						klg.storeKineticsAndParameters(plugin, settingsPanel
								.isSetTreatAllReactionsReversible(), this);
				}

			} else if (text.equals("Save")) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new MyFileFilter(true, false));
				if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
					settingsPanel.save(chooser.getSelectedFile());
			} else if (text.equals("Load")) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new MyFileFilter(true, false));
				if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
					try {
						settingsPanel.loadSettings(chooser.getSelectedFile());
					} catch (IOException exc) {
						JOptionPane.showMessageDialog(this, "<html>"
								+ exc.getMessage() + "</html>", exc.getClass()
								.getName(), JOptionPane.ERROR_MESSAGE);
						exc.printStackTrace();
					} catch (NumberFormatException exc) {
						JOptionPane.showMessageDialog(this, "<html>"
								+ exc.getMessage() + "</html>", exc.getClass()
								.getName(), JOptionPane.ERROR_MESSAGE);
						exc.printStackTrace();
					}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.LawListener#currentNumber(int)
	 */
	public void currentNumber(int num) {
		progressBar.setValue(num);
		if (num >= progressBar.getMaximum())
			progressDialog.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.LawListener#totalNumber(int)
	 */
	public void totalNumber(int count) {
		progressBar = new JProgressBar(0, count);
		progressBar.setToolTipText("Storing kinetic laws");
		progressBar.setValue(0);
		progressDialog = new JDialog(this);
		GridBagLayout layout = new GridBagLayout();
		progressDialog.setLayout(layout);
		LayoutHelper.addComponent(progressDialog.getContentPane(), layout,
				progressBar, 0, 0, 1, 1, 1, 1);
		progressDialog.pack();
		progressDialog.setAlwaysOnTop(true);
		progressDialog.setLocationRelativeTo(null);
		progressDialog.setVisible(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent e) {
		if (e.getSource() instanceof JHelpBrowser)
			helpButton.setEnabled(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeactivated(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent
	 * )
	 */
	public void windowDeiconified(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	public void windowIconified(WindowEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	public void windowOpened(WindowEvent e) {
	}

	/**
	 * This method allows to write the generated kinetic equations to an ASCII
	 * or TeX file.
	 */
	private void exportKineticEquations() {
		if (!KineticsAndParametersStoredInSBML) {
			if (plugin != null)
				klg.storeKineticsAndParameters(plugin, settingsPanel
						.isSetTreatAllReactionsReversible(), this);
			KineticsAndParametersStoredInSBML = true;
		}
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new MyFileFilter(true, true));
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
			try {
				new ODEwriter(chooser.getSelectedFile(), klg);
			} catch (IOException exc) {
				JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
						+ "</html>", exc.getClass().getName(),
						JOptionPane.ERROR_MESSAGE);
				exc.printStackTrace();
			}
	}

	/**
	 * Returns the panel on the bottom of this window.
	 * 
	 * @param type
	 *            if i = 0 this will return the foot containing the buttons
	 *            "Help", "Cancel" and "Generate" for i = 1 there will be
	 *            "Export", "Cancel", "Back" and "Apply".
	 * @return
	 */
	private JPanel getFootPanel(int type) {
		GridBagLayout gbl = new GridBagLayout();
		JPanel south = new JPanel(gbl), rightPanel = new JPanel(new FlowLayout(
				FlowLayout.RIGHT)), leftPanel = new JPanel();

		JButton cancel = new JButton("Cancel"), apply = new JButton();
		cancel
				.setToolTipText("<html>Exit SBMLsqueezer without saving changes.</html>");
		cancel.addActionListener(this);
		apply.addActionListener(this);

		rightPanel.add(cancel);
		switch (type) {
		case 0:
			apply.setToolTipText("<html>Start generating an ordinary "
					+ "differential equation system.</html>");
			apply.setText("Generate");
			helpButton = new JButton("Help");
			helpButton.addActionListener(this);
			leftPanel.add(helpButton);
			break;
		default:
			apply.setToolTipText("<html>Write the generated kinetics and "
					+ "parameters to the SBML file.</html>");
			apply.setText("Apply");

			JButton jButtonReactionsFrameSave = new JButton();
			jButtonReactionsFrameSave.setEnabled(true);
			jButtonReactionsFrameSave.setBounds(35, 285, 100, 25);
			jButtonReactionsFrameSave
					.setToolTipText("<html>Transfers the kinetics and parameters "
							+ "to CellDesigner and<br>"
							+ "allowes to save the generated differential equations as<br>"
							+ "*.txt or *.tex files.</html>");
			jButtonReactionsFrameSave.setText("Export");
			jButtonReactionsFrameSave.addActionListener(this);
			leftPanel.add(jButtonReactionsFrameSave);
			JButton back = new JButton("Back");
			back.addActionListener(this);
			rightPanel.add(back);
			break;
		}
		rightPanel.add(apply);
		LayoutHelper.addComponent(south, gbl, leftPanel, 0, 0, 1, 1, 0, 0);
		LayoutHelper.addComponent(south, gbl, rightPanel, 1, 0, 3, 1, 1, 1);

		return south;
	}

	/**
	 * This method initializes a Panel that shows all possible settings of the
	 * program.
	 * 
	 * @return javax.swing.JPanel
	 */
	private JSettingsPanel getJSettingsPanel() {
		if (settingsPanel == null) {
			settingsPanel = new JSettingsPanel();
			// settingsPanel.setBackground(Color.WHITE);
		}
		return settingsPanel;
	}

	/**
	 * This method actually initializes the GUI.
	 */
	private void init() {
		centralPanel = initOptionsPanel();

		setLayout(new BorderLayout());
		getContentPane().add(centralPanel, BorderLayout.CENTER);

		try {
			Image image = ImageIO.read(Resource.class
					.getResource("img/title_small.jpg"));
			// image = image.getScaledInstance(490, 150, Image.SCALE_SMOOTH);
			JLabel label = new JLabel(new ImageIcon(image));
			label.setBackground(Color.WHITE);
			JPanel p = new JPanel();
			p.add(label);
			p.setBackground(Color.WHITE);
			JScrollPane scroll = new JScrollPane(p,
					JScrollPane.VERTICAL_SCROLLBAR_NEVER,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroll.setBackground(Color.WHITE);
			getContentPane().add(scroll, BorderLayout.NORTH);
			// ContainerHandler.setAllBackground(getContentPane(), Color.WHITE);
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		}

		footPanel = getFootPanel(0);
		getContentPane().add(footPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setDefaultLookAndFeelDecorated(true);
		setLocationByPlatform(true);
		setResizable(false);
		pack();

		int height = getHeight();
		setSize(getWidth(), fullHeight);
		setLocationRelativeTo(null);
		setSize(getWidth(), height);
		settingsPanel = getJSettingsPanel();
	}

	/**
	 * Returns a JPanel that displays the user options.
	 * 
	 * @return
	 */
	private JPanel initOptionsPanel() {
		JPanel p = new JPanel(new BorderLayout());

		options = new JButton("show options");
		try {
			Image image = ImageIO.read(Resource.class.getResource(
			/*
			 * new File(System.getProperty("user.dir") +
			 * System.getProperty("file.separator") + "resources" +
			 * System.getProperty("file.separator") + "images" +
			 * System.getProperty("file.separator") +
			 */"img/rightarrow.png"));
			image = image.getScaledInstance(10, 10, Image.SCALE_SMOOTH);
			options.setIcon(new ImageIcon(image));
			options.setIconTextGap(5);
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		}
		options.setBorderPainted(false);
		options.setSize(150, 20);
		options.setToolTipText("Customize the advanced settings.");
		options.addActionListener(this);
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(options);
		options.setBackground(new Color(panel.getBackground().getRGB()));
		p.add(panel, BorderLayout.NORTH);

		return p;
	}

	private void showSettingsPanel() {
		try {
			Image image = ImageIO.read(Resource.class.getResource(
			/*
			 * new File(System.getProperty("user.dir") +
			 * System.getProperty("file.separator") + "resources" +
			 * System.getProperty("file.separator") + "images" +
			 * System.getProperty("file.separator") +
			 */"img/downarrow.png"));
			image = image.getScaledInstance(10, 10, Image.SCALE_SMOOTH);
			options.setIcon(new ImageIcon(image));
			options.setIconTextGap(5);
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		options.setToolTipText("<html>Hide detailed options</html>");
		options.setText("hide options");
		settingsPanel = getJSettingsPanel();
		JScrollPane scroll = new JScrollPane(settingsPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBackground(Color.WHITE);
		centralPanel.add(scroll, BorderLayout.CENTER);
		centralPanel.validate();
		/*
		 * setSize(getWidth(), (int) Math.min(fullHeight,
		 * GraphicsEnvironment.getLocalGraphicsEnvironment()
		 * .getMaximumWindowBounds().getHeight()));//
		 */
		pack();
		validate();
	}
}
