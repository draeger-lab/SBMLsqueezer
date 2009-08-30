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
package org.sbml.squeezer.gui;

import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.Model;
import org.sbml.Reaction;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.io.SBFileFilter;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.resources.Resource;
import org.sbml.squeezer.resources.cfg.CfgKeys;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLsqueezerUI extends JFrame implements ActionListener,
		WindowListener, ChangeListener {
	private SBMLio sbmlIO;

	public static final String SQUEEZE = "squeeze_reaction";
	public static final String TO_LATEX = "reaction_toLaTeX";


	JTabbedPaneWithCloseIcons tabbedPane;
	private JMenuItem saveItem;
	private JMenuItem closeItem;

	private JMenuItem squeezeItem;

	private JMenuItem latexItem;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 5662654607939013825L;

	/**
	 * @throws HeadlessException
	 */
	public SBMLsqueezerUI(SBMLio io) throws HeadlessException {
		super("SBMLsqueezer " + SBMLsqueezer.getVersionNumber());
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
		try {
			Image image = ImageIO.read(Resource.class
					.getResource("img/icon.png"));
			setIconImage(image);
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(this, "<html>" + exc.getMessage()
					+ "</html>", exc.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		}
		this.sbmlIO = io;
		init();
		pack();
	}

	// @Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals(SQUEEZE)) {
			if (e.getSource() instanceof Reaction) {
				new KineticLawSelectionDialog(this, sbmlIO, (Reaction) e
						.getSource());
			} else if (e.getSource() instanceof Model) {
				KineticLawSelectionDialog klsd = new KineticLawSelectionDialog(
						this, sbmlIO);
				klsd.setVisible(true);
			} else if (e.getActionCommand().equals(TO_LATEX)) {
				if (e.getSource() instanceof Reaction) {
					new KineticLawSelectionDialog(this, (Reaction) e
							.getSource());
				} else if (e.getSource() instanceof Model) {
					// TODO
				}
			}
		} else if (e.getSource() instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) e.getSource();
			if (item.getText().equals("Open")) {
				JFileChooser chooser = new JFileChooser();
				SBFileFilter filter = new SBFileFilter(SBFileFilter.SBML_FILES);
				chooser.setFileFilter(filter);
				String dir = SBMLsqueezer.getProperty(CfgKeys.OPEN_DIR.toString()).toString();
				if (dir != null) {
					if (dir.startsWith("user."))
						dir = System.getProperty(dir);
					chooser.setCurrentDirectory(new File(dir));
				}
				if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					addModel(sbmlIO.readModel(chooser.getSelectedFile()
							.getAbsolutePath()));
					String path = chooser.getSelectedFile().getAbsolutePath();
					path = path.substring(0, path.lastIndexOf('/'));
					if (!path.equals(dir))
						SBMLsqueezer.setProperty(CfgKeys.OPEN_DIR, path);
				}
			} else if (item.getText().equals(latexItem.getText())) {
				// TODO
				JFileChooser chooser = new JFileChooser();
				SBFileFilter filterTeX = new SBFileFilter(
						SBFileFilter.TeX_FILES);
				chooser.addChoosableFileFilter(filterTeX);
				String dir = SBMLsqueezer
						.getProperty(CfgKeys.SAVE_DIR.toString()).toString();
				if (dir != null) {
					if (dir.startsWith("user."))
						dir = System.getProperty(dir);
					chooser.setCurrentDirectory(new File(dir));
				}
				if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
					// TODO
					System.err.println("not yet implemented");
					String path = chooser.getSelectedFile().getAbsolutePath();
					path = path.substring(0, path.lastIndexOf('/'));
					if (!path.equals(dir))
						SBMLsqueezer.setProperty(CfgKeys.OPEN_DIR, path);
				}
			} else if (item.getText().equals(saveItem.getText())) {
				JFileChooser chooser = new JFileChooser();
				SBFileFilter filterSBML = new SBFileFilter(
						SBFileFilter.SBML_FILES);
				SBFileFilter filterText = new SBFileFilter(
						SBFileFilter.TEXT_FILES);
				SBFileFilter filterTeX = new SBFileFilter(
						SBFileFilter.TeX_FILES);
				chooser.addChoosableFileFilter(filterSBML);
				chooser.addChoosableFileFilter(filterText);
				chooser.addChoosableFileFilter(filterTeX);
				String dir = SBMLsqueezer
						.getProperty(CfgKeys.SAVE_DIR.toString()).toString();
				if (dir != null) {
					if (dir.startsWith("user."))
						dir = System.getProperty(dir);
					chooser.setCurrentDirectory(new File(dir));
				}
				if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
					// TODO
					System.err.println("not yet implemented");
					String path = chooser.getSelectedFile().getAbsolutePath();
					path = path.substring(0, path.lastIndexOf('/'));
					if (!path.equals(dir))
						SBMLsqueezer.setProperty(CfgKeys.OPEN_DIR, path);
				}
			} else if (item.getText().equals(closeItem.getText())) {
				if (tabbedPane.getComponentCount() > 0)
					tabbedPane.remove(tabbedPane.getSelectedComponent());
				if (tabbedPane.getComponentCount() == 0)
					setModelsOpened(false);
			} else if (item.getText().equals("Squeeze")) {
				KineticLawSelectionDialog klsd = new KineticLawSelectionDialog(
						this, sbmlIO);
				klsd.setVisible(true);

			} else if (item.getText().equals("Exit")) {
				SBMLsqueezer.saveProperties();
				System.exit(0);
			} else if (item.getText().equals("About")) {
				JBrowser browser = new JBrowser(Resource.class
						.getResource("html/about.htm"));
				browser.removeHyperlinkListener(browser);
				browser.addHyperlinkListener(new SystemBrowser());
				browser.setBorder(BorderFactory.createEtchedBorder());
				JOptionPane.showMessageDialog(this, browser,
						"About SBMLsqueezer", JOptionPane.INFORMATION_MESSAGE);
			} else if (item.getText().equals("Online help")) {
				JHelpBrowser helpBrowser = new JHelpBrowser(this,
						"SBMLsqueezer " + SBMLsqueezer.getVersionNumber() + " - Online Help");
				// helpBrowser.addWindowListener(this);
				helpBrowser.setLocationRelativeTo(this);
				helpBrowser.setSize(640, 640);
				helpBrowser.setVisible(true);
				helpBrowser.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			}
		}
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource().equals(tabbedPane)) {
			if (tabbedPane.getComponentCount() == 0) {
				setModelsOpened(false);
			}
		}
	}

	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowClosing(WindowEvent arg0) {
		SBMLsqueezer.saveProperties();
	}

	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	private void addModel(Model model) {
		SBMLModelSplitPane split = new SBMLModelSplitPane(model);
		split.addActionListener(this);
		tabbedPane.add(model.getId(), split);
		tabbedPane.setSelectedIndex(tabbedPane.getComponentCount() - 1);
		setModelsOpened(true);
		pack();
	}

	private JMenuBar createMenuBar() {
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open", UIManager
				.getIcon("FileView.directoryIcon"));
		saveItem = new JMenuItem("Save", UIManager
				.getIcon("FileView.floppyDriveIcon"));
		closeItem = new JMenuItem("Close");
		JMenuItem exitItem = new JMenuItem("Exit");
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(closeItem);
		fileMenu.add(exitItem);
		openItem.addActionListener(this);
		closeItem.addActionListener(this);
		exitItem.addActionListener(this);

		JMenu editMenu = new JMenu("Edit");
		squeezeItem = new JMenuItem("Squeeze");
		squeezeItem.addActionListener(this);
		try {
			squeezeItem.setIcon(new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/Lemon_tiny.png"))));
		} catch (IOException e) {
		}
		latexItem = new JMenuItem("Export to LaTeX");
		try {
			latexItem.setIcon(new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/SBML2LaTeX_vertical_tiny.png"))));
		} catch (IOException e) {
		}
		latexItem.addActionListener(this);
		latexItem.setActionCommand("toLaTeX");
		editMenu.add(squeezeItem);
		editMenu.add(latexItem);

		JMenu helpMenu = new JMenu("Help");
		JMenuItem about = new JMenuItem("About");
		about.addActionListener(this);
		JMenuItem help = new JMenuItem("Online help");
		help.addActionListener(this);
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

	private void setModelsOpened(boolean state) {
		saveItem.setEnabled(state);
		closeItem.setEnabled(state);
		squeezeItem.setEnabled(state);
		latexItem.setEnabled(state);
	}

	// @Override
	protected void init() {
		setJMenuBar(createMenuBar());
		setModelsOpened(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(this);
		tabbedPane = new JTabbedPaneWithCloseIcons();
		for (Model m : sbmlIO.getListOfModels())
			addModel(m);
		tabbedPane.addChangeListener(this);
		tabbedPane.addChangeListener(sbmlIO);
		getContentPane().add(tabbedPane);
	}
}
