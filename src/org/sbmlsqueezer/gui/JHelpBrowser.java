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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.sbmlsqueezer.resources.Resource;

/**
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * 
 */
public class JHelpBrowser extends JDialog implements ActionListener {

	/**
   * 
   */
	private static final long serialVersionUID = 5747595033121404644L;

	/**
	 * The actual browser.
	 */
	private JBrowser browser;

	/**
	 * Creates a new JDialog that shows a browser and a toolbar to display a
	 * help web site.
	 * 
	 * @param owner
	 *            The owner of this window.
	 * @param title
	 *            The title of this window.
	 * @see javax.swing.JDialog
	 */
	public JHelpBrowser(Dialog owner, String title) {
		super(owner, "SBMLsqueezer - Online Help");
		init();
	}

	/**
	 * Creates a new JDialog that shows a browser and a toolbar to display a
	 * help.
	 * 
	 * @param owner
	 * @param title
	 */
	public JHelpBrowser(Frame owner, String title) {
		super(owner, "SBMLsqueezer - Online Help");
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			String name = ((JButton) e.getSource()).getName();
			if (name.equals("back") && (browser != null)) {
				browser.back();

			} else if (name.equals("next") && (browser != null)) {
				browser.next();
			}
		}
	}

	/**
	 * Initialize this Window.
	 * 
	 */
	private void init() {
		browser = new JBrowser(Resource.class.getResource("html/index.html"));
		JPanel content = new JPanel(new BorderLayout());
		content.add(new JScrollPane(browser,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				BorderLayout.CENTER);
		JToolBar toolbar = new JToolBar();
		JButton backButton, nextButton;
		try {
			Image image = ImageIO.read(Resource.class
					.getResource("img/back.png"));
			// image = image.getScaledInstance(22, 22, Image.SCALE_SMOOTH);

			backButton = new JButton(new ImageIcon(image));
			backButton.setToolTipText("Last Page");
		} catch (IOException e) {
			backButton = new JButton("Back");
			e.printStackTrace();
		}
		backButton.setName("back");
		backButton.addActionListener(this);
		toolbar.add(backButton);

		try {
			Image image = ImageIO.read(Resource.class
					.getResource("img/forward.png"));
			// image = image.getScaledInstance(22, 22, Image.SCALE_SMOOTH);
			nextButton = new JButton(new ImageIcon(image));
			nextButton.setToolTipText("Next Page");
		} catch (IOException e) {
			nextButton = new JButton("Next");
			e.printStackTrace();
		}
		nextButton.setName("next");
		nextButton.addActionListener(this);
		toolbar.add(nextButton);
		content.add(toolbar, BorderLayout.NORTH);
		setContentPane(content);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		setDefaultLookAndFeelDecorated(true);
		setLocationByPlatform(true);
	}
}
