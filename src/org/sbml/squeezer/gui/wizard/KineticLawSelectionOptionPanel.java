/*
 * $Id: KineticLawSelectionOptionPanel.java 830 2012-02-26 00:33:31Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/wizard/KineticLawSelectionOptionPanel.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
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

package org.sbml.squeezer.gui.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.gui.GUITools;
import org.sbml.squeezer.util.Bundles;

import de.zbit.gui.prefs.PreferencesDialog;
import de.zbit.util.ResourceManager;

/**
 * This class implements the main panel. The panel includes the 
 * SBMLsqueezer logo and a button to show the preferences dialog
 * 
 * @author Sebastian Nagel
 * @date Feb 25, 2012
 * @since 1.4
 * @version $Rev: 830 $
 */
public class KineticLawSelectionOptionPanel extends JPanel implements ActionListener{
	
	/**
	 * Generated serial version identifier. 
	 */
	private static final long serialVersionUID = -4552303683388858130L;
	/**
	 * 
	 */
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
	/**
	 * 
	 */
	public static final transient ResourceBundle LABELS = ResourceManager.getBundle(Bundles.LABELS);
	/**
	 *
	 */
	public static final transient ResourceBundle BASE = ResourceManager.getBundle(Bundles.BASE);
	
	/**
	 * 
	 */
	public KineticLawSelectionOptionPanel() {
		super(new BorderLayout());
		init();
	}
	
	/**
	 * 
	 */
	public void init() {
		initOptions();
		initLogo();
	}
	
	/**
	 * initialize the kinetic law option (preferences) button 
	 */
	public void initOptions() {
		JButton options = new JButton(MESSAGES.getString("SHOW_OPTIONS"));
		Icon icon = UIManager.getIcon("ICON_PREFS_16");
		if (icon != null) {
			options.setIcon(icon);
		}
		options.setIconTextGap(5);
		options.setSize(150, 20);
		options.setToolTipText(MESSAGES.getString("SHOW_OPTIONS_TOOLTIP"));
		options.addActionListener(this);
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(options);
		options.setBackground(new Color(panel.getBackground().getRGB()));
		
		add(panel, BorderLayout.SOUTH);
	}
	
	/**
	 * initalize the SBMLsqueezer logo
	 */
	public void initLogo() {
		JLabel label = new JLabel(UIManager.getIcon("ICON_LOGO_SMALL"));
		label.setBackground(Color.WHITE);
		label.setText("<html><body><br/><br/><br/><br/><br/><br/>"
						+ MESSAGES.getString("VERSION") + " "
						+ System.getProperty("app.version") + "</body></html>");
		Dimension d = GUITools.getDimension(UIManager
				.getIcon("ICON_LOGO_SMALL"));
		d.setSize(d.getWidth() + 125, d.getHeight() + 10);
		label.setPreferredSize(new Dimension(d));
		JPanel p = new JPanel();
		p.add(label);
		JScrollPane scroll = new JScrollPane(p,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		GUITools.setAllBackground(scroll, Color.WHITE);
		add(scroll, BorderLayout.NORTH);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton button = (JButton) e.getSource();
			String text = button.getText();
			if (text.equals(MESSAGES.getString("SHOW_OPTIONS"))) {
				// show preferences dialog
				PreferencesDialog.showPreferencesDialog(SqueezerOptions.class);
			}
		}
	}

}
