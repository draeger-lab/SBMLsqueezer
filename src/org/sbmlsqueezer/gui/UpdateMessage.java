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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.sbmlsqueezer.resources.Resource;

/**
 * This class implements a JWindow object which is shown on the bottom right
 * corner of the screen. It notifies the user that a more recent version of
 * SBMLsqueezer is available. The release notes of this version can be shown.
 * 
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @since 1.2
 */

public class UpdateMessage extends JWindow implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1923146558856297087L;

	/**
	 * Checks if there is an update for SBMLsqueezer available.
	 * 
	 * @param plugin
	 * @throws IOException
	 */
	public static void checkForUpdate(SBMLsqueezerPlugin plugin)
			throws IOException {
		URL url = new URL(
				"http://www.ra.cs.uni-tuebingen.de/software/SBMLsqueezer/downloads/latest.txt");
		String out = (new Scanner(url.openStream())).next();
		String notes = "releaseNotes" + out;
		if (notes.endsWith(".0"))
			notes = notes.substring(0, notes.length() - 2);
		if (compareVersionNumbers(SBMLsqueezerPlugin.getVersionNumber(), out))
			showUpdateMessage("http://www.ra.cs.uni-tuebingen.de/software/SBMLsqueezer/downloads/"
					+ notes + ".htm");
		plugin.setUpdateChecked(true);
	}

	/**
	 * Compares the version delivered by SBMLsqueezer and the file "latest.txt"
	 * on www.
	 * 
	 * @param prog
	 * @param url
	 * @return
	 */
	private static boolean compareVersionNumbers(String prog, String url) {
		StringTokenizer progToken = new StringTokenizer(prog);
		StringTokenizer urlToken = new StringTokenizer(url);
		while (progToken.hasMoreElements() && urlToken.hasMoreElements())
			if (Integer.parseInt(progToken.nextToken(".")) < Integer
					.parseInt(urlToken.nextToken("."))) {
				return true;
			}
		if (urlToken.hasMoreElements()
				&& Integer.parseInt(urlToken.nextToken(".")) > 0)
			return true;
		return false;
	}

	/**
	 * Show the update message window.
	 * 
	 * @param url
	 * @throws IOException
	 */
	private static void showUpdateMessage(String url) throws IOException {
		(new UpdateMessage(url)).setVisible(true);
	}

	private JButton okButton;

	private JButton showHideButton;

	private JPanel contentPanel;

	/**
	 * Main constructor. Initializes a JWindow object containing a (at the
	 * beginning) invisible JEditorPane which contains SBMLsqueezer's release
	 * notes and two JButtons for showing them and exiting the Window.
	 * 
	 * @param u
	 * @throws IOException
	 */
	public UpdateMessage(String u) throws IOException {
		super();
		setBackground(Color.YELLOW);
		setAlwaysOnTop(true);
		URL url = new URL(u);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.setBackground(getBackground());

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		showHideButton = new JButton("show release notes");
		Image image = ImageIO.read(Resource.class
				.getResource("img/rightarrow.png"));
		showHideButton.setIcon(new ImageIcon(image.getScaledInstance(10, 10,
				Image.SCALE_SMOOTH)));
		showHideButton.setIconTextGap(5);
		showHideButton.setBorderPainted(false);
		showHideButton.setBackground(new Color(buttonPanel.getBackground()
				.getRGB()));
		showHideButton.setSize(150, 20);
		showHideButton.addActionListener(this);
		buttonPanel.add(showHideButton);
		buttonPanel.add(okButton);

		Scanner scanner = new Scanner(url.openStream());
		String s = scanner.useDelimiter("<h1>").next();
		s = "<html><body>" + scanner.useDelimiter("\\Z").next();
		JEditorPane pane = new JEditorPane("text/html", s);
		pane.setEditable(false);
		pane.addHyperlinkListener(new SystemBrowser());
		contentPanel = new JPanel(new BorderLayout());
		JScrollPane scroll = new JScrollPane(pane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(new Dimension(550, 400));
		contentPanel.add(scroll, BorderLayout.CENTER);

		contentPanel.setVisible(false);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBackground(getBackground());
		mainPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(
				Color.BLACK), "An update for SBMLsqueezer is available.",
				TitledBorder.CENTER, TitledBorder.BELOW_TOP));
		mainPanel.add(contentPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.PAGE_END);
		setContentPane(mainPanel);
		pack();
		adjustLocation();
	}

	/**
	 * If the "show release notes" button is hit, the JEditorPane object
	 * containing the release notes is made visible and the button's text is
	 * changed to "hide release notes". If "hide release notes" is hit, the
	 * JEditorPane gets invisible again, and the button's text is rechanged to
	 * it's default. If "OK" is hit, the window is closed.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			String buttonText = ((JButton) e.getSource()).getText();
			if (buttonText.equals("show release notes")) {
				contentPanel.setVisible(true);
				showHideButton.setText("hide release notes");
			} else if (buttonText.equals("hide release notes")) {
				contentPanel.setVisible(false);
				showHideButton.setText("show release notes");
			} else {
				dispose();
			}
		}
		validate();
		pack();
		adjustLocation();
	}

	/**
	 * Adjusts the location of the update message window to it's actual size.
	 */

	private void adjustLocation() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(d.width - this.getWidth(), d.height - this.getHeight()
				- 30);
	}
}
