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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.io.SBFileFilter;

/**
 * A {@link JPanel} to configure all necessary options to perform a LaTeX export
 * of a model.
 * 
 * @since 1.2
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Jan 2009
 */
public class SettingsPanelLaTeX extends JPanel implements ActionListener,
		ItemListener {

	private JTextField fileField;
	private JComboBox jComboBoxPaperSize;
	private JComboBox jComboBoxFontSize;
	private JCheckBox jCheckBoxIDsInTWFont;
	private JCheckBox jCheckBoxLandscape;
	private JCheckBox jCheckBoxTitlePage;
	private JCheckBox jCheckBoxNameInEquations;
	private Properties settings;
	private boolean browse;
	private List<ChangeListener> changeListeners;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 5056629254462180004L;

	/**
	 * 
	 */
	private static String[] paperSizes;

	/**
	 * 
	 */
	private static final Short[] fontSizes = new Short[] { 8, 9, 10, 11, 12,
			14, 17 };

	static {
		paperSizes = new String[43];
		paperSizes[0] = "letter";
		paperSizes[1] = "legal";
		paperSizes[2] = "executive";
		char[] prefixes = new char[] { 'a', 'b', 'c', 'd' };
		for (int i = 0; i < prefixes.length; i++)
			for (int j = 0; j < 10; j++)
				paperSizes[3 + i * 10 + j] = prefixes[i] + String.valueOf(j);
	}

	/**
	 * 
	 * @param properties
	 *            The settings for this panel
	 * @param browse
	 *            if true a browse button will appear that allows to select a
	 *            LaTeX file. If false this button will only allow to select a
	 *            directory for LaTeX files.
	 */
	public SettingsPanelLaTeX(Properties properties, boolean browse) {
		super(new BorderLayout());
		settings = new Properties();
		for (Object key : properties.keySet())
			if (key.toString().startsWith("LATEX_"))
				settings.put(key, properties.get(key));
		this.changeListeners = new LinkedList<ChangeListener>();
		this.browse = browse;
		JPanel filePanel = new JPanel();
		LayoutHelper lh = new LayoutHelper(filePanel);
		fileField = new JTextField(15);
		fileField.setText(settings.get(CfgKeys.LATEX_DIR).toString());
		fileField.setEditable(false);
		lh.add(new JPanel(), 0, 0, 4, 1, 0, 0);
		lh.add(new JPanel(), 0, 1, 1, 1, 0, 0);
		lh.add(fileField, 1, 1, 1, 1, .7, .8);
		lh.add(new JPanel(), 2, 1, 1, 1, 0, 0);
		JButton jButtonTeXFile;
		if (browse) {
			filePanel
					.setBorder(BorderFactory
							.createTitledBorder(" Select a LaTeX file for the output "));
			jButtonTeXFile = new JButton("Browse", GUITools.ICON_SAVE);
		} else {
			filePanel
					.setBorder(BorderFactory
							.createTitledBorder(" Select the standard directory for LaTeX files "));
			jButtonTeXFile = new JButton("Browse", GUITools.ICON_OPEN);
		}
		jButtonTeXFile.addActionListener(this);
		lh.add(jButtonTeXFile, 3, 1, 1, 1, 0, .8);
		lh.add(new JPanel(), 4, 1, 1, 1, 0, 0);
		lh.add(new JPanel(), 0, 2, 4, 1, 0, 0);
		add(filePanel, BorderLayout.NORTH);

		int i = 0;
		while (i < paperSizes.length
				&& !paperSizes[i].equals(settings.get(CfgKeys.LATEX_PAPER_SIZE)
						.toString()))
			i++;
		jComboBoxPaperSize = new JComboBox(paperSizes);
		jComboBoxPaperSize.setSelectedIndex(i);
		i = 0;
		while (i < fontSizes.length
				&& fontSizes[i].shortValue() != Short.parseShort(settings.get(
						CfgKeys.LATEX_FONT_SIZE).toString()))
			i++;
		jComboBoxFontSize = new JComboBox(fontSizes);
		jComboBoxFontSize.setSelectedIndex(i);
		int row = -1;
		JPanel formatPanel = new JPanel();
		formatPanel.setBorder(BorderFactory
				.createTitledBorder(" Format options "));
		lh = new LayoutHelper(formatPanel);
		lh.add(new JPanel(), 0, ++row, 5, 1, 0, 0);
		lh.add(new JPanel(), 0, ++row, 1, 1, 0, 0);
		lh.add(new JLabel("Paper size"), 1, ++row, 1, 1, 0, 0);
		lh.add(new JPanel(), 2, row, 1, 1, 0, 0);
		lh.add(jComboBoxPaperSize, 3, row, 1, 1, 1, 0);
		lh.add(new JPanel(), 5, row, 1, 1, 0, 0);
		lh.add(new JPanel(), 0, ++row, 5, 1, 0, 0);
		lh.add(new JLabel("Font size"), 1, ++row, 1, 1, 0, 0);
		lh.add(jComboBoxFontSize, 3, row, 1, 1, 1, 0);
		lh.add(new JPanel(), 0, ++row, 5, 1, 0, 0);

		jCheckBoxIDsInTWFont = new JCheckBox("IDs in typewriter font",
				((Boolean) settings.get(CfgKeys.LATEX_IDS_IN_TYPEWRITER_FONT))
						.booleanValue());
		jCheckBoxLandscape = new JCheckBox("Landscape", ((Boolean) settings
				.get(CfgKeys.LATEX_LANDSCAPE)).booleanValue());
		jCheckBoxTitlePage = new JCheckBox("Create title page",
				((Boolean) settings.get(CfgKeys.LATEX_TITLE_PAGE))
						.booleanValue());
		jCheckBoxNameInEquations = new JCheckBox("Set name in equations",
				((Boolean) settings.get(CfgKeys.LATEX_NAMES_IN_EQUATIONS))
						.booleanValue());
		lh.add(jCheckBoxIDsInTWFont, 1, ++row, 2, 1, 1, 1);
		if (!browse) {
			JPanel panel = new JPanel(new BorderLayout());
			JPanel p = new JPanel(new BorderLayout());
			p.add(new JLabel(GUITools.ICON_LATEX_SMALL), BorderLayout.SOUTH);
			panel.add(p, BorderLayout.EAST);
			lh.add(panel, 3, row, 2, 4, 1, 1);
		}
		lh.add(jCheckBoxLandscape, 1, ++row, 2, 1, 1, 1);
		lh.add(jCheckBoxTitlePage, 1, ++row, 2, 1, 1, 1);
		lh.add(jCheckBoxNameInEquations, 1, ++row, 2, 1, 1, 1);
		lh.add(new JPanel(), 0, ++row, 5, 1, 0, 0);
		add(formatPanel, BorderLayout.CENTER);

		jComboBoxPaperSize.addItemListener(this);
		jComboBoxFontSize.addItemListener(this);
		jCheckBoxIDsInTWFont.addItemListener(this);
		jCheckBoxLandscape.addItemListener(this);
		jCheckBoxTitlePage.addItemListener(this);
		jCheckBoxNameInEquations.addItemListener(this);
	}

	/**
	 * 
	 * @param settingsDialog
	 */
	public void addChangeListener(ChangeListener cl) {
		this.changeListeners.add(cl);
	}

	/**
	 * 
	 * @return
	 */
	public Properties getProperties() {
		return settings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JFileChooser chooser = GUITools.createJFileChooser(fileField
					.getText(), false, false, JFileChooser.FILES_ONLY);
			boolean approve = false;
			if (browse) {
				chooser.addChoosableFileFilter(SBFileFilter.TeX_FILE_FILTER);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);
				approve = chooser.showSaveDialog(getParent()) == JFileChooser.APPROVE_OPTION;
			} else {
				chooser.setAcceptAllFileFilterUsed(true);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				approve = chooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION;
			}
			if (approve) {
				String path = chooser.getSelectedFile().getAbsolutePath();
				fileField.setText(path);
				if (!browse)
					settings.put(CfgKeys.LATEX_DIR, path);
				for (ChangeListener cl : changeListeners)
					cl.stateChanged(new ChangeEvent(e));
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public String getTeXFile() {
		return fileField.getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent id) {
		if (id.getSource() instanceof JComboBox) {
			if (id.getSource().equals(jComboBoxPaperSize))
				settings.put(CfgKeys.LATEX_PAPER_SIZE, jComboBoxPaperSize
						.getSelectedItem().toString());
			else if (id.getSource().equals(jComboBoxFontSize))
				settings.put(CfgKeys.LATEX_FONT_SIZE, Integer
						.parseInt(jComboBoxFontSize.getSelectedItem()
								.toString()));
		} else if (id.getSource() instanceof JCheckBox) {
			if (id.getSource().equals(jCheckBoxIDsInTWFont))
				settings.put(CfgKeys.LATEX_IDS_IN_TYPEWRITER_FONT, Boolean
						.valueOf(jCheckBoxIDsInTWFont.isSelected()));
			else if (id.getSource().equals(jCheckBoxLandscape))
				settings.put(CfgKeys.LATEX_LANDSCAPE, Boolean
						.valueOf(jCheckBoxLandscape.isSelected()));
			else if (id.getSource().equals(jCheckBoxTitlePage))
				settings.put(CfgKeys.LATEX_TITLE_PAGE, Boolean
						.valueOf(jCheckBoxTitlePage.isSelected()));
			else if (id.getSource().equals(jCheckBoxNameInEquations))
				settings.put(CfgKeys.LATEX_NAMES_IN_EQUATIONS, Boolean
						.valueOf(jCheckBoxNameInEquations.isSelected()));
		}
		for (ChangeListener cl : changeListeners)
			cl.stateChanged(new ChangeEvent(id));
	}
}
