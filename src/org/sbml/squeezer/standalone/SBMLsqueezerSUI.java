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
package org.sbml.squeezer.standalone;

import java.awt.CardLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.sbml.Model;
import org.sbml.squeezer.gui.ModelComponentsPanel;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.io.SBFileFilter;
import org.sbml.squeezer.resources.Resource;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLsqueezerSUI extends SBMLsqueezerUI implements ActionListener {

	JTabbedPane tabbedPane;
	
	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 5662654607939013825L;

	/**
	 * @throws HeadlessException
	 */
	public SBMLsqueezerSUI() throws HeadlessException {
		super();
		init();
	}

	public SBMLsqueezerSUI(Model model) {
		this();
		addModel(model);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	// @Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) e.getSource();
			if (item.getText().equals("Open")) {
				JFileChooser chooser = new JFileChooser();
				SBFileFilter filter = new SBFileFilter(SBFileFilter.SBML_FILES);
				chooser.setFileFilter(filter);
				if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
					addModel(new LibSBMLReader(chooser.getSelectedFile()
							.getAbsolutePath()).getModel());
			} else if (item.getText().equals("Save")) {
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
				if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
					// TODO
					System.err.println("not yet implemented");
				}
			} else if (item.getText().equals("Squeeze")) {

			} else if (item.getText().equals("Exit")) {
				System.exit(0);
			} else if (item.getText().equals("")) {

			}
		}
	}

	private void addModel(Model model) {
		tabbedPane.addTab(model.getId(), new ModelComponentsPanel(model));
	}

	private JMenuBar createMenuBar() {
		JMenu fileMenu = new JMenu("File");
		JMenuItem openItem = new JMenuItem("Open", UIManager
				.getIcon("FileView.directoryIcon"));
		JMenuItem closeItem = new JMenuItem("Save", UIManager
				.getIcon("FileView.floppyDriveIcon"));
		JMenuItem exitItem = new JMenuItem("Exit");
		fileMenu.add(openItem);
		fileMenu.add(closeItem);
		fileMenu.add(exitItem);
		openItem.addActionListener(this);
		closeItem.addActionListener(this);
		exitItem.addActionListener(this);

		JMenu editMenu = new JMenu("Edit");
		JMenuItem squeezeItem = new JMenuItem("Squeeze");
		squeezeItem.addActionListener(this);
		try {
			squeezeItem.setIcon(new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/Lemon_tiny.png"))));
		} catch (IOException e) {
		}
		editMenu.add(squeezeItem);

		JMenuBar mBar = new JMenuBar();
		mBar.add(fileMenu);
		mBar.add(editMenu);
		return mBar;
	}

	// @Override
	protected void init() {
		tabbedPane = new JTabbedPane();
		getContentPane().add(tabbedPane);
		setJMenuBar(createMenuBar());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
