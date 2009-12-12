/**
 * 
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
import javax.swing.UIManager;

import org.sbmlsqueezer.resources.Resource;

/**
 * @author andreas
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

	/**
	 * Initialize this Window.
	 * 
	 */
	private void init() {
		browser = new JBrowser(/*
								 * "file://" + System.getProperty("user.dir") +
								 * System.getProperty("file.separator") +
								 * "resources" +
								 * System.getProperty("file.separator") + "html"
								 * + System.getProperty("file.separator") +
								 */Resource.class
				.getResource("html/index.html"));
		JPanel content = new JPanel(new BorderLayout());
		content.add(new JScrollPane(browser,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
				BorderLayout.CENTER);
		JToolBar toolbar = new JToolBar();
		JButton backButton, nextButton;
		try {
			Image image = ImageIO.read(Resource.class.getResource(
			/*
			 * new File(System.getProperty("user.dir") +
			 * System.getProperty("file.separator") + "resources" +
			 * System.getProperty("file.separator") + "images" +
			 * System.getProperty("file.separator") +
			 */"img/back.png"));
			// image = image.getScaledInstance(22, 22, Image.SCALE_SMOOTH);*/

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
			Image image = ImageIO.read(Resource.class.getResource(
			/*
			 * new File(System.getProperty("user.dir") +
			 * System.getProperty("file.separator") + "resources" +
			 * System.getProperty("file.separator") + "images" +
			 * System.getProperty("file.separator") +
			 */"forward.png"));
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
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
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
}
