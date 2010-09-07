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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.IOProgressListener;
import org.sbml.squeezer.LawListener;

import de.zbit.gui.LayoutHelper;

/**
 * GUI class to visualize the progress when writing SBML or synchronizing rate
 * laws.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-04-08
 * @since 1.4
 * 
 */
public class ProgressDialog extends JDialog implements IOProgressListener,
		LawListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3540396520496679634L;
	/**
	 * 
	 */
	private JProgressBar progressBar;
	/**
	 * 
	 */
	private JLabel label;

	/**
	 * 
	 * @param owner
	 * @param title
	 */
	public ProgressDialog(JDialog owner, String title) {
		super(owner, title);
		init();
	}
	
	/**
	 * 
	 * @param owner
	 * @param title
	 */
	public ProgressDialog(JFrame owner, String title) {
		super(owner, title);
		init();
	}

	private void init() {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		label = new JLabel();
		getContentPane().add(label);
		setSize(200, 150);
		setLocationRelativeTo(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.LawListener#currentState(org.sbml.jsbml.SBase,
	 * int)
	 */
	public void currentState(SBase item, int num) {
		label.setText(" Done with " + Integer.toString(num) + " ");
		progressBar.setValue(num);
		if (num >= progressBar.getMaximum())
			dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.LawListener#initLawListener(java.lang.String, int)
	 */
	public void initLawListener(String className, int count) {
		if (count > 0) {
			if (!className.endsWith("s") && count != 1)
				className += "s";
			progressBar = new JProgressBar(0, count - 1);
			progressBar.setToolTipText("Saving changes in " + className);
			progressBar.setValue(0);
			if (getTitle().length() == 0)
				setTitle("Saving changes");
			setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
			JPanel p = new JPanel();
			LayoutHelper lh = new LayoutHelper(p);
			String space = " ";
			for (int i = 0; i < Integer.toString(count).length(); i++)
				space += ' ';
			label = new JLabel(" Done with " + Integer.valueOf(0) + space);
			lh.add(label, 0, 0, 1, 1, 1, 1);
			JPanel progress = new JPanel();
			progress.add(progressBar);
			progress.setBorder(BorderFactory.createLoweredBevelBorder());
			lh.add(progress, 1, 0, 1, 1, 1, 1);
			lh.add(new JLabel(" of " + count + ' ' + className), 2, 0, 1, 1, 1,
					1);
			JPanel outer = new JPanel();
			outer.add(p);
			getContentPane().add(outer);
			pack();
			setResizable(false);
			setLocationRelativeTo(null);
			setVisible(true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.io.IOProgressListener#progress(java.lang.Object)
	 */
	public void ioProgressOn(Object currObject) {
		if (currObject == null) {
			setVisible(false);
		} else {
			if (!isVisible())
				setVisible(true);
			StringBuilder sb = new StringBuilder();
			sb.append("writing ");
			sb.append(currObject.getClass().getSimpleName());
			if (currObject instanceof NamedSBase) {
				sb.append(' ');
				sb.append(((NamedSBase) currObject).getId());
			}
			label.setText(GUITools.toHTML(sb.toString(), 40));
		}
	}
}
