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
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.sbml.squeezer.resources.Resource;

import de.zbit.gui.JBrowserPane;

/**
 * This is a specialized dialog that displays HTML pages and contains a toolbar
 * with two buttons for jumping forward or backward in the history of visited
 * pages.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.0
 */
public class JHelpBrowser extends JDialog implements ActionListener,
		HyperlinkListener {

	private JButton backButton, nextButton;

	/**
   * 
   */
	private static final long serialVersionUID = 5747595033121404644L;

	/**
	 * The actual browser.
	 */
	private JBrowserPane browser;

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
		super(owner, title);
		init("html/help.html");
	}

	/**
	 * Creates a new JDialog that shows a browser and a toolbar to display a
	 * help.
	 * 
	 * @param owner
	 * @param title
	 */
	public JHelpBrowser(Frame owner, String title) {
		super(owner, title);
		init("html/help.html");
	}

	/**
	 * 
	 * @param owner
	 * @param title
	 * @param helpFile
	 */
	public JHelpBrowser(Frame owner, String title, String helpFile) {
		super(owner, title);
		init(helpFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton button = (JButton) e.getSource();
			String name = button.getName();
			if (name.equals("back") && (browser != null)) {
				if (!browser.back()) {
					button.setEnabled(false);
					if (browser.getNumPagesVisited() > 1
							&& !nextButton.isEnabled())
						nextButton.setEnabled(true);
				} else if (!nextButton.isEnabled())
					nextButton.setEnabled(true);
			} else if (name.equals("next") && (browser != null)) {
				if (!browser.next()) {
					button.setEnabled(false);
					if (browser.getNumPagesVisited() > 1
							&& !backButton.isEnabled())
						backButton.setEnabled(true);
				} else if (!backButton.isEnabled())
					backButton.setEnabled(true);
			}
		}
	}

	/**
	 * Initialize this Window.
	 * 
	 * @param helpFile
	 * 
	 */
	private void init(String helpFile) {
		browser = new JBrowserPane(Resource.class.getResource(helpFile));
		browser.addHyperlinkListener(this);
		JPanel content = new JPanel(new BorderLayout());
		content.add(new JScrollPane(browser,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				BorderLayout.CENTER);
		JToolBar toolbar = new JToolBar();
		// image = image.getScaledInstance(22, 22, Image.SCALE_SMOOTH);
		backButton = new JButton(UIManager.getIcon("ICON_LEFT_ARROW"));
		backButton.setToolTipText("Last Page");
		backButton.setName("back");
		backButton.addActionListener(this);
		backButton.setEnabled(false);
		toolbar.add(backButton);

		Icon icon = UIManager.getIcon("ICON_FORWARD");
		if (icon != null) {
			nextButton = new JButton(icon);
		} else {
			nextButton = new JButton("Next");
		}
		nextButton.setToolTipText("Next Page");
		nextButton.setName("next");
		nextButton.addActionListener(this);
		nextButton.setEnabled(false);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event
	 * .HyperlinkEvent)
	 */
	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED
				&& !backButton.isEnabled())
			backButton.setEnabled(true);
	}
}
