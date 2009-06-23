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
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.sbmlsqueezer.io.MyFileFilter;

/**
 * @since 1.2
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Jan 2009
 */
public class LaTeXExportDialogPanel extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5056629254462180004L;

	String[] paperSizes;

	Short[] fontSizes;

	private JPanel filePanel;
	private JPanel formatPanel;
	private JPanel optionsPanel;

	private JButton jButtonTeXFile = new JButton("Browse");

	private JTextField fileField = new JTextField(15);

	private JComboBox jComboBoxPaperSize;
	private JComboBox jComboBoxFontSize;

	private JCheckBox jCheckBoxIDsInTWFont = new JCheckBox(
			"IDs in typewriter font", true);
	private JCheckBox jCheckBoxLandscape = new JCheckBox("Landscape", false);
	// private JCheckBox jCheckBoxImplicitUnit = new JCheckBox(
	// "Show implicit unit declarations", true);
	private JCheckBox jCheckBoxTitlePage = new JCheckBox("Create title page",
			false);
	private JCheckBox jCheckBoxNameInEquations = new JCheckBox(
			"Set name in equations", false);

	// private JCheckBox jCheckBoxNumberEquations = new JCheckBox
	// ("Number equations consecutively", true);

	public LaTeXExportDialogPanel() {
		super(new BorderLayout());
		paperSizes = new String[43];
		paperSizes[0] = "letter";
		paperSizes[1] = "legal";
		paperSizes[2] = "executive";
		char[] prefixes = new char[] { 'a', 'b', 'c', 'd' };
		for (int i = 0; i < prefixes.length; i++) {
			for (int j = 0; j < 10; j++) {
				paperSizes[3 + i * 10 + j] = prefixes[i] + String.valueOf(j);
			}
		}
		fontSizes = new Short[] { 8, 9, 10, 11, 12, 14, 17 };

		filePanel = new JPanel(new GridBagLayout());
		filePanel
				.setBorder(BorderFactory.createTitledBorder("Select TeX file"));
		jButtonTeXFile.addActionListener(this);

		LayoutHelper.addComponent(filePanel, (GridBagLayout) filePanel
				.getLayout(), jButtonTeXFile, 5, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(filePanel, (GridBagLayout) filePanel
				.getLayout(), fileField, 0, 0, 5, 1, 1, 1);

		formatPanel = new JPanel(new GridBagLayout());
		formatPanel.setBorder(BorderFactory
				.createTitledBorder("Format options"));
		jComboBoxPaperSize = new JComboBox(paperSizes);
		jComboBoxPaperSize.setSelectedIndex(7);
		jComboBoxFontSize = new JComboBox(fontSizes);
		jComboBoxFontSize.setSelectedIndex(3);
		LayoutHelper
				.addComponent(formatPanel, (GridBagLayout) formatPanel
						.getLayout(), new JLabel("Select paper size"), 0, 0, 3,
						1, 1, 1);
		LayoutHelper.addComponent(formatPanel, (GridBagLayout) formatPanel
				.getLayout(), jComboBoxPaperSize, 3, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(formatPanel, (GridBagLayout) formatPanel
				.getLayout(), new JLabel("Select font size"), 0, 1, 3, 1, 1, 1);
		LayoutHelper.addComponent(formatPanel, (GridBagLayout) formatPanel
				.getLayout(), jComboBoxFontSize, 3, 1, 1, 1, 1, 1);

		optionsPanel = new JPanel(new GridBagLayout());
		optionsPanel.setBorder(BorderFactory
				.createTitledBorder("Export options"));
		LayoutHelper.addComponent(optionsPanel, (GridBagLayout) optionsPanel
				.getLayout(), jCheckBoxIDsInTWFont, 0, 0, 1, 1, 1, 1);
		LayoutHelper.addComponent(optionsPanel, (GridBagLayout) optionsPanel
				.getLayout(), jCheckBoxLandscape, 0, 1, 1, 1, 1, 1);
		// LayoutHelper.addComponent(optionsPanel, (GridBagLayout) optionsPanel
		// .getLayout(), jCheckBoxImplicitUnit, 0, 2, 1, 1, 1, 1);
		LayoutHelper.addComponent(optionsPanel, (GridBagLayout) optionsPanel
				.getLayout(), jCheckBoxTitlePage, 0, 3, 1, 1, 1, 1);
		LayoutHelper.addComponent(optionsPanel, (GridBagLayout) optionsPanel
				.getLayout(), jCheckBoxNameInEquations, 0, 4, 1, 1, 1, 1);
		/*
		 * LayoutHelper.addComponent(optionsPanel, (GridBagLayout) optionsPanel
		 * .getLayout(), jCheckBoxNumberEquations, 0, 5, 1, 1, 1, 1);
		 */

		add(filePanel, BorderLayout.PAGE_START);
		add(formatPanel, BorderLayout.CENTER);
		add(optionsPanel, BorderLayout.PAGE_END);

		// ContainerHandler.setAllBackground(this, Color.WHITE);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new MyFileFilter(false, true));
			if (chooser.showOpenDialog(getParent()) == JFileChooser.APPROVE_OPTION) {
				String path = chooser.getSelectedFile().getAbsolutePath();
				fileField.setText(path);
			}
		}
	}

	public short getFontSize() {
		return Short.valueOf(jComboBoxFontSize.getSelectedItem().toString());
	}

	/*
	 * public boolean isImplicitUnit() { return
	 * jCheckBoxImplicitUnit.isSelected(); }
	 */

	public String getPaperSize() {
		return jComboBoxPaperSize.getSelectedItem().toString();
	}

	public String getTeXFile() {
		return fileField.getText();
	}

	/*
	 * public boolean isNumberEquations () { return
	 * jCheckBoxNumberEquations.isSelected(); }
	 */

	public boolean isIDsInTWFont() {
		return jCheckBoxIDsInTWFont.isSelected();
	}

	public boolean isLandscape() {
		return jCheckBoxLandscape.isSelected();
	}

	public boolean isNameInEquations() {
		return jCheckBoxNameInEquations.isSelected();
	}

	public boolean isTitlePage() {
		return jCheckBoxTitlePage.isSelected();
	}
}
