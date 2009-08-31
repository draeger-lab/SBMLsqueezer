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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
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
	public static final String ONLINE_HELP = "online_help";
	public static final String OPEN_FILE = "open_file";
	public static final String SAVE_FILE = "save_file";
	public static final String CLOSE_FILE = "close_file";

	private JTabbedPaneWithCloseIcons tabbedPane;
	private JToolBar toolbar;

	private static Icon latexIcon;
	private static Icon lemonIcon;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 5662654607939013825L;

	static {
		try {
			lemonIcon = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/Lemon_tiny.png")));
			latexIcon = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/SBML2LaTeX_vertical_tiny.png")));
		} catch (IOException e) {
			lemonIcon = null;
			e.printStackTrace();
		}
	}

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
			} else /*
					 * if (e.getSource() instanceof Model) {
					 * KineticLawSelectionDialog klsd = new
					 * KineticLawSelectionDialog( this, sbmlIO);
					 * klsd.setVisible(true); } else
					 */{
				KineticLawSelectionDialog klsd = new KineticLawSelectionDialog(
						this, sbmlIO);
				klsd.setVisible(true);
			}
		} else if (e.getActionCommand().equals(TO_LATEX)) {
			if (e.getSource() instanceof Reaction) {
				new KineticLawSelectionDialog(this, (Reaction) e.getSource());
			} else if (e.getSource() instanceof Model) {
				// TODO
				System.err.println("not yet implemented");
			} else {
				// TODO
				JFileChooser chooser = new JFileChooser();
				SBFileFilter filterTeX = new SBFileFilter(
						SBFileFilter.TeX_FILES);
				chooser.addChoosableFileFilter(filterTeX);
				String dir = SBMLsqueezer.getProperty(CfgKeys.SAVE_DIR)
						.toString();
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
			}
		} else if (e.getActionCommand().equals(OPEN_FILE)) {
			JFileChooser chooser = new JFileChooser();
			SBFileFilter filter = new SBFileFilter(SBFileFilter.SBML_FILES);
			chooser.setFileFilter(filter);
			Object dir = SBMLsqueezer.getProperty(CfgKeys.OPEN_DIR);
			if (dir != null) {
				if (dir.toString().startsWith("user."))
					dir = System.getProperty(dir.toString());
				chooser.setCurrentDirectory(new File(dir.toString()));
			}
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				addModel(sbmlIO.readModel(chooser.getSelectedFile()
						.getAbsolutePath()));
				String path = chooser.getSelectedFile().getAbsolutePath();
				path = path.substring(0, path.lastIndexOf('/'));
				if (!path.equals(dir))
					SBMLsqueezer.setProperty(CfgKeys.OPEN_DIR, path);
			}
		} else if (e.getActionCommand().equals(SAVE_FILE)) {
			JFileChooser chooser = new JFileChooser();
			SBFileFilter filterSBML = new SBFileFilter(SBFileFilter.SBML_FILES);
			SBFileFilter filterText = new SBFileFilter(SBFileFilter.TEXT_FILES);
			SBFileFilter filterTeX = new SBFileFilter(SBFileFilter.TeX_FILES);
			chooser.addChoosableFileFilter(filterSBML);
			chooser.addChoosableFileFilter(filterText);
			chooser.addChoosableFileFilter(filterTeX);
			String dir = SBMLsqueezer.getProperty(CfgKeys.SAVE_DIR).toString();
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
		} else if (e.getActionCommand().equals(CLOSE_FILE)) {
			if (tabbedPane.getComponentCount() > 0)
				tabbedPane.remove(tabbedPane.getSelectedComponent());
			if (tabbedPane.getComponentCount() == 0)
				setEnabled(false, SAVE_FILE, CLOSE_FILE, SQUEEZE, TO_LATEX);
		} else if (e.getActionCommand().equals(ONLINE_HELP)) {
			JHelpBrowser helpBrowser = new JHelpBrowser(this, "SBMLsqueezer "
					+ SBMLsqueezer.getVersionNumber() + " - Online Help");
			helpBrowser.addWindowListener(this);
			helpBrowser.setLocationRelativeTo(this);
			helpBrowser.setSize(640, 640);
			helpBrowser.setVisible(true);
			helpBrowser.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			setEnabled(false, ONLINE_HELP);
		} else if (e.getSource() instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) e.getSource();
			if (item.getText().equals("Exit")) {
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
			}
		}
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
			if (tabbedPane.getComponentCount() == 0)
				setEnabled(false, SAVE_FILE, CLOSE_FILE, SQUEEZE, TO_LATEX);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	public void windowClosing(WindowEvent we) {
		if (we.getSource() instanceof JHelpBrowser)
			setEnabled(true, ONLINE_HELP);
		else if (we.getSource() instanceof SBMLsqueezerUI)
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
		setEnabled(true, SAVE_FILE, CLOSE_FILE, SQUEEZE, TO_LATEX);
		pack();
	}

	private JMenuBar createMenuBar() {
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(fileMenu.getText().charAt(0));
		JMenuItem openItem = new JMenuItem("Open", UIManager
				.getIcon("FileView.directoryIcon"));
		openItem.setActionCommand(OPEN_FILE);
		openItem.setAccelerator(KeyStroke.getKeyStroke('O',
				InputEvent.CTRL_DOWN_MASK));
		JMenuItem saveItem = new JMenuItem("Save", UIManager
				.getIcon("FileView.floppyDriveIcon"));
		saveItem.setActionCommand(SAVE_FILE);
		saveItem.setAccelerator(KeyStroke.getKeyStroke('S',
				InputEvent.CTRL_DOWN_MASK));
		JMenuItem closeItem = new JMenuItem("Close", new CloseIcon(false));
		closeItem.setAccelerator(KeyStroke.getKeyStroke('W',
				InputEvent.CTRL_DOWN_MASK));
		closeItem.setActionCommand(CLOSE_FILE);
		JMenuItem exitItem = new JMenuItem("Exit");
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4,
				InputEvent.ALT_DOWN_MASK));
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(closeItem);
		fileMenu.add(exitItem);
		openItem.addActionListener(this);
		closeItem.addActionListener(this);
		exitItem.addActionListener(this);

		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(editMenu.getText().charAt(0));
		JMenuItem squeezeItem = new JMenuItem("Squeeze");
		squeezeItem.setAccelerator(KeyStroke.getKeyStroke('Q',
				InputEvent.CTRL_DOWN_MASK));
		squeezeItem.addActionListener(this);
		squeezeItem.setActionCommand(SQUEEZE);
		squeezeItem.setIcon(lemonIcon);
		JMenuItem latexItem = new JMenuItem("Export to LaTeX");
		latexItem.setAccelerator(KeyStroke.getKeyStroke('E',
				InputEvent.CTRL_DOWN_MASK));
		latexItem.setIcon(latexIcon);
		latexItem.addActionListener(this);
		latexItem.setActionCommand(TO_LATEX);
		editMenu.add(squeezeItem);
		editMenu.add(latexItem);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(helpMenu.getText().charAt(0));
		JMenuItem about = new JMenuItem("About");
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		about.addActionListener(this);
		JMenuItem help = new JMenuItem("Online help");
		help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		help.addActionListener(this);
		help.setActionCommand(ONLINE_HELP);
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

	private JToolBar createToolBar() {
		toolbar = new JToolBar("Edit", JToolBar.HORIZONTAL);
		JButton openButton = new JButton(UIManager
				.getIcon("FileView.directoryIcon"));
		openButton.addActionListener(this);
		openButton.setActionCommand(OPEN_FILE);
		toolbar.add(openButton);
		JButton saveButton = new JButton(UIManager
				.getIcon("FileView.floppyDriveIcon"));
		saveButton.addActionListener(this);
		saveButton.setActionCommand(SAVE_FILE);
		toolbar.add(saveButton);
		JButton closeButton = new JButton(new CloseIcon(false));
		closeButton.setActionCommand(CLOSE_FILE);
		closeButton.addActionListener(this);
		toolbar.add(closeButton);
		toolbar.addSeparator();
		if (lemonIcon != null) {
			JButton squeezeButton = new JButton(lemonIcon);
			squeezeButton.setActionCommand(SQUEEZE);
			squeezeButton.addActionListener(this);
			toolbar.add(squeezeButton);
		}
		if (latexIcon != null) {
			JButton latexButton = new JButton(latexIcon);
			latexButton.addActionListener(this);
			latexButton.setActionCommand(TO_LATEX);
			toolbar.add(latexButton);
		}
		toolbar.addSeparator();
		JButton helpButton = new JButton("?");
		helpButton.addActionListener(this);
		helpButton.setActionCommand(ONLINE_HELP);
		toolbar.add(helpButton);
		return toolbar;
	}

	/**
	 * 
	 * @param state
	 * @param commands
	 */
	private void setEnabled(boolean state, String... commands) {
		int i, j;
		Set<String> setOfCommands = new HashSet<String>();
		for (String command : commands)
			setOfCommands.add(command);
		for (i = 0; i < getJMenuBar().getMenuCount(); i++) {
			JMenu menu = getJMenuBar().getMenu(i);
			for (j = 0; j < menu.getItemCount(); j++) {
				JMenuItem item = menu.getItem(j);
				if (setOfCommands.contains(item.getActionCommand()))
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
	private void init() {
		setJMenuBar(createMenuBar());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(this);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(createToolBar(), BorderLayout.NORTH);
		setEnabled(false, SAVE_FILE, CLOSE_FILE, SQUEEZE, TO_LATEX);
		tabbedPane = new JTabbedPaneWithCloseIcons();
		for (Model m : sbmlIO.getListOfModels())
			addModel(m);
		tabbedPane.addChangeListener(this);
		tabbedPane.addChangeListener(sbmlIO);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
	}
}
