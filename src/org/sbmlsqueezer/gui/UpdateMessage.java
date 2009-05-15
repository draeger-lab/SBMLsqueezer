package org.sbmlsqueezer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
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
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import org.sbmlsqueezer.resources.Resource;

/**
 * This class implements a JWindow object which is shown on the bottom right
 * corner of the screen. It notifies the user that a more recent version of
 * SBMLsqueezer is available. The release notes of this version can be shown.
 * 
 * @author Hannes Borch <hannes.borch@googlemail.com>
 */

public class UpdateMessage extends JWindow implements ActionListener,
		HyperlinkListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1923146558856297087L;

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
		this.setAlwaysOnTop(true);
		URL url = new URL(u);

		okButton = new JButton("OK");
		okButton.addActionListener(this);
		showHideButton = getShowHideButton();

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(showHideButton);
		buttonPanel.add(okButton);

		contentPanel = getContentPanel(url);
		contentPanel.setVisible(false);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createTitledBorder(new LineBorder(
				Color.BLACK), "An update for SBMLsqueezer is available.",
				TitledBorder.CENTER, TitledBorder.BELOW_TOP));
		mainPanel.add(contentPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.PAGE_END);

		this.setContentPane(mainPanel);
		this.pack();

		adjustLocation();
	}

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
		if (compareVersionNumbers(plugin.getVersionNumber(), out))
			showUpdateMessage("http://www.ra.cs.uni-tuebingen.de/software/SBMLsqueezer/downloads/releaseNotes1.1.htm");
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
				this.dispose();
			}
		}
		this.validate();
		this.pack();
		adjustLocation();
	}

	/**
	 * If the mouse cursor lies over a text part which is a hyperlink, it
	 * becomes a hand cursor. Otherwise, it is the default cursor. By clicking
	 * in a hyperlink, this link is opened in a external web browser
	 * application. In case of Windows or Macintosh operating systems, the
	 * default browser is used. In case of Linux, a available web browser is
	 * searched an run.
	 */

	public void hyperlinkUpdate(HyperlinkEvent event) {
		if (event.getEventType() == HyperlinkEvent.EventType.ENTERED) {
			((JEditorPane) event.getSource()).setCursor(Cursor
					.getPredefinedCursor(Cursor.HAND_CURSOR));
		} else if (event.getEventType() == HyperlinkEvent.EventType.EXITED) {
			((JEditorPane) event.getSource()).setCursor(Cursor
					.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			JEditorPane pane = (JEditorPane) event.getSource();
			if (event instanceof HTMLFrameHyperlinkEvent) {
				HTMLDocument doc = (HTMLDocument) pane.getDocument();
				doc
						.processHTMLFrameHyperlinkEvent((HTMLFrameHyperlinkEvent) event);
			} else
				try {
					if (System.getProperty("os.name").contains("Windows"))
						Runtime.getRuntime().exec(
								"rundll32 url.dll,FileProtocolHandler "
										+ event.getURL());
					else if (System.getProperty("os.name").contains("Mac OS"))
						Runtime.getRuntime().exec("open " + event.getURL());
					else {
						String[] browsers = { "firefox", "opera", "konqueror",
								"epiphany", "mozilla", "netscape" };
						String browser = null;
						for (int i = 0; i < browsers.length && browser == null; i++)
							if (Runtime.getRuntime().exec(
									"which " + browsers[i]).waitFor() == 0)
								browser = browsers[i];
						if (browser == null)
							throw new Exception("Could not find web browser");
						else
							Runtime.getRuntime().exec(
									browser + " " + event.getURL());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * Adjusts the location of the update message window to it's actual size.
	 */

	private void adjustLocation() {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(d.width - this.getWidth(), d.height - this.getHeight()
				- 30);
	}

	/**
	 * Returns the JEditorPane containing the release notes, included in a
	 * JPanel object.
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */

	private JPanel getContentPanel(URL url) throws IOException {
		Scanner scanner = new Scanner(url.openStream());
		String s = scanner.useDelimiter("<h1>").next();
		s = "<html><body>" + scanner.useDelimiter("\\Z").next();

		JEditorPane pane = new JEditorPane("text/html", s);
		pane.setEditable(false);
		pane.addHyperlinkListener(this);

		JPanel panel = new JPanel(new BorderLayout());
		JScrollPane scroll = new JScrollPane(pane,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setPreferredSize(new Dimension(550, 400));
		panel.add(scroll, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Returns the arrow symbol used for the "show release notes" button.
	 * 
	 * @return
	 * @throws IOException
	 */

	private Image getIcon() throws IOException {
		Image image = ImageIO.read(Resource.class
				.getResource("img/rightarrow.png"));
		image = image.getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		return image;
	}

	/**
	 * Initializes the "show release notes" button.
	 * 
	 * @return
	 * @throws IOException
	 */

	private JButton getShowHideButton() throws IOException {
		JButton button = new JButton("show release notes");

		button.setIcon(new ImageIcon(getIcon()));
		button.setIconTextGap(5);
		button.setBorderPainted(false);
		button.setSize(150, 20);
		button.addActionListener(this);

		return button;
	}
}
