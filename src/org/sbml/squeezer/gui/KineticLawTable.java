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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TableModelEvent;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.io.LaTeX;
import org.sbml.squeezer.kinetics.BasicKineticLaw;

import atp.sHotEqn;

/**
 * Creates a table that displays all created kinetic equationns.
 * 
 * @since 1.0
 * @version
 * @author Andreas Dr&auml;ger
 * @date Nov 13, 2007
 */
public class KineticLawTable extends JTable implements MouseInputListener {

	/**
	 * Generated serial version ID
	 */
	private static final long serialVersionUID = -1575566223506382693L;

	private boolean editing;

	private KineticLawGenerator klg;

	private boolean reversibility;

	// private static final int widthMultiplier = 7;

	/**
	 * TODO
	 * 
	 * @param klg
	 * @param maxEducts
	 * @param reversibility
	 */
	public KineticLawTable(KineticLawGenerator klg) {
		super(new KineticLawTableModel(klg));
		this.klg = klg;
		this.reversibility = ((Boolean) klg.getSettings().get(
				CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE)).booleanValue();
		getModel().addTableModelListener(this);
		setColumnWidthAppropriately();
		setRowHeightAppropriately();
		setDefaultRenderer(Object.class, new KineticLawTableCellRenderer(
				((Integer) (klg.getSettings()
						.get(CfgKeys.OPT_MAX_NUMBER_OF_REACTANTS))).intValue()));
		getTableHeader().setToolTipText(
				GUITools.toHTML("Double click on the kinetic "
						+ "law to apply another formalism. "
						+ "Single click on any other "
						+ "column to get a formatted equation preview.", 40));
		setCellSelectionEnabled(true);
		setEnabled(true);
		addMouseListener(this);
		editing = false;
	}

	private void setRowHeightAppropriately() {
		setRowHeight(getFont().getSize() * 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		// Point p = e.getPoint();
		// int rowIndex = rowAtPoint(p);
		// int colIndex = columnAtPoint(p);
		// "\tSpalte: "
		// // + colIndex);
		// if (convertColumnIndexToModel(colIndex) == 1) {
		// // Kinetic Law column
		// setCellEditor(rowIndex);
		// }
		Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = convertColumnIndexToModel(columnAtPoint(p));
		if (colIndex != 1) {
			Object o = dataModel.getValueAt(rowIndex, 1);
			if (o instanceof BasicKineticLaw) {
				BasicKineticLaw kinetic = (BasicKineticLaw) o;
				String LaTeX = kinetic.getMath().compile(
						new LaTeX(klg.getSettings())).toString().replace(
						"text", "mbox").replace("mathrm", "mbox").replace(
						"mathtt", "mbox");
				JComponent component = new sHotEqn("\\begin{equation}" + LaTeX
						+ "\\end{equation}");
				JPanel panel = new JPanel(new BorderLayout());
				component.setBackground(Color.WHITE);
				panel.setBackground(Color.WHITE);
				panel.add(component, BorderLayout.CENTER);
				panel.setLocation(((int) MouseInfo.getPointerInfo()
						.getLocation().getX())
						- this.getTopLevelAncestor().getX(), this.getY() + 10);
				panel.setBorder(BorderFactory.createLoweredBevelBorder());
				JOptionPane.showMessageDialog(this, panel,
						"Rate Law of Reaction "
								+ kinetic.getParentSBMLObject().getId(),
						JOptionPane.INFORMATION_MESSAGE);
				// JLayeredPane.getLayeredPaneAbove(getParent()).add(component,
				// JLayeredPane.POPUP_LAYER);
				validate();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent )
	 */
	public void mouseDragged(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
		Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);
		// Kinetic Law column
		if ((convertColumnIndexToModel(colIndex) == 1) && !editing) {
			// setCellEditor(null);
			try {
				setCellEditor(rowIndex);
			} catch (Exception exc) {
				JOptionPane.showMessageDialog(this, GUITools.toHTML(exc
						.getMessage(), 40), exc.getClass().getName(),
						JOptionPane.WARNING_MESSAGE);
				exc.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * Sets up a combo box, which allows to select an appropriate value for a
	 * kinetic law in the given row.
	 * 
	 * @param rowIndex
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws NoSuchMethodException
	 * @throws ClassNotFoundException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 */
	private void setCellEditor(int rowIndex) throws SecurityException,
			IllegalArgumentException, ClassNotFoundException,
			NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		if ((dataModel.getRowCount() > 0) && (dataModel.getColumnCount() > 0)) {
			Reaction reaction = klg.getModel().getReaction(
					dataModel.getValueAt(rowIndex, 0).toString());
			try {
				final String possibleTypes[] = this.klg.getReactionType(
						reaction.getId()).identifyPossibleKineticLaws();
				final BasicKineticLaw possibleLaws[] = new BasicKineticLaw[possibleTypes.length];
				int selected = 0;
				final BasicKineticLaw oldLaw = ((BasicKineticLaw) klg
						.getModifiedReaction(reaction.getId()).getKineticLaw());
				for (int i = 0; i < possibleLaws.length; i++) {
					possibleLaws[i] = klg.createKineticLaw(reaction,
							possibleTypes[i], reversibility);
					if (possibleLaws[i].getSimpleName().equals(
							oldLaw.getSimpleName()))
						selected = i;
				}

				final KineticLawSelectionPanel klsp = new KineticLawSelectionPanel(
						possibleLaws, klg.getSettings(), selected);
				final JOptionPane pane = new JOptionPane(klsp,
						JOptionPane.QUESTION_MESSAGE,
						JOptionPane.OK_CANCEL_OPTION, GUITools.ICON_LEMON_SMALL);
				pane.selectInitialValue();
				Container container = getTopLevelAncestor();
				final JDialog dialog;
				if (container instanceof Frame)
					dialog = new JDialog((Frame) container,
							"Choose an alternative kinetic law for reaction "
									+ reaction.getId());
				else if (container instanceof Dialog)
					dialog = new JDialog((Dialog) container,
							"Choose an alternative kinetic law for reaction "
									+ reaction.getId());
				else {
					dialog = new JDialog();
					dialog
							.setTitle("Choose an alternative kinetic law for reaction "
									+ reaction.getId());
				}
				Container content = dialog.getContentPane();
				content.setLayout(new BorderLayout());
				content.add(pane, BorderLayout.CENTER);
				dialog.setModal(true);

				WindowAdapter adapter = new WindowAdapter() {
					private boolean gotFocus = false;

					public void windowClosing(WindowEvent we) {
						pane.setValue(null);
					}

					public void windowGainedFocus(WindowEvent we) {
						// Once window gets focus, set initial focus
						if (!gotFocus) {
							pane.selectInitialValue();
							gotFocus = true;
						}
					}
				};
				dialog.addWindowListener(adapter);
				dialog.addWindowFocusListener(adapter);
				dialog.addComponentListener(new ComponentAdapter() {
					public void componentShown(ComponentEvent ce) {
						// reset value to ensure closing works properly
						pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
					}
				});
				pane.addPropertyChangeListener(new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent event) {
						// Let the defaultCloseOperation handle the closing
						// if the user closed the window without selecting a
						// button
						// (newValue = null in that case). Otherwise, close the
						// dialog.
						if (dialog.isVisible()
								&& event.getSource() == pane
								&& (event.getPropertyName()
										.equals(JOptionPane.VALUE_PROPERTY))
								&& event.getNewValue() != null
								&& event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
							dialog.setVisible(false);
							int i = 0;
							if (((Integer) event.getNewValue()).intValue() == JOptionPane.OK_OPTION) {
								while (i < possibleTypes.length
										&& !possibleTypes[i].equals(klsp
												.getSelectedKinetic()))
									i++;
							} else {
								while (i < possibleTypes.length
										&& !possibleTypes[i].equals(oldLaw
												.getClass().getCanonicalName()))
									i++;
							}
							updateTable(possibleLaws[i]);
						}
					}
				});

				dialog.pack();
				dialog.setResizable(false);
				dialog.setLocationRelativeTo(dialog.getOwner());
				dialog.setVisible(true);
				dialog.dispose();

				// This would be to simple for CellDesigner. We need the more
				// complicated
				// code...
				// if (JOptionPane
				// .showConfirmDialog(this, klsp,
				// "Choose an alternative kinetic law",
				// JOptionPane.OK_CANCEL_OPTION,
				// JOptionPane.QUESTION_MESSAGE,
				// GUITools.LEMON_ICON_SMALL) == JOptionPane.OK_OPTION) {
				// int i = 0;
				// while (i < possibleTypes.length
				// && !possibleTypes[i].equals(klsp
				// .getSelectedKinetic()))
				// i++;
				// updateTable(possibleLaws[i]);
				// }
			} catch (Throwable e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), e
						.getClass().getName(), JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * 
	 * @param possibleTypes
	 * @param selectedKinetic
	 * @param possibleLaws
	 */
	public void updateTable(KineticLaw kineticLaw) {
		// Reaction Identifier, Kinetic Law, SBO, #Reactants,
		// Reactants, Products, Parameters, Formula
		int i;
		StringBuffer params = new StringBuffer();
		for (i = kineticLaw.getNumParameters() - 1; i > 0; i--) {
			params.append(kineticLaw.getParameter(i));
			if (i > 0)
				params.append(", ");
		}
		List<Parameter> referencedGlobalParameters = kineticLaw.getMath()
				.findReferencedGlobalParameters();
		for (i = referencedGlobalParameters.size() - 1; i > 0; i--) {
			params.append(referencedGlobalParameters.get(i));
			if (i > 0)
				params.append(", ");
		}
		String name = kineticLaw instanceof BasicKineticLaw ? ((BasicKineticLaw) kineticLaw)
				.getSimpleName()
				: kineticLaw.toString();
		dataModel.setValueAt(name, getSelectedRow(), 1);
		dataModel.setValueAt(kineticLaw.getSBOTermID(), getSelectedRow(), 2);
		dataModel.setValueAt(params, getSelectedRow(), dataModel
				.getColumnCount() - 2);
		dataModel.setValueAt(kineticLaw.getFormula(), getSelectedRow(),
				dataModel.getColumnCount() - 1);
		i = 0;
		while ((i < klg.getModel().getNumReactions())
				&& (!klg.getModel().getReaction(i).getId().equals(
						kineticLaw.getParentSBMLObject().getId())))
			i++;
		kineticLaw.getParentSBMLObject().setKineticLaw(kineticLaw);
		setColumnWidthAppropriately();
		editing = false;
	}

	/**
	 * 
	 */
	private void setColumnWidthAppropriately() {
		for (int col = 0; col < getColumnCount(); col++) {
			int maxLength = getColumnModel().getColumn(col).getHeaderValue()
					.toString().length();
			for (int row = 0; row < getRowCount(); row++)
				if (maxLength < getValueAt(row, col).toString().length())
					maxLength = getValueAt(row, col).toString().length();
			getColumnModel().getColumn(col).setPreferredWidth(
					3 * getFont().getSize() / 5 * maxLength + 10);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.JTable#tableChanged(javax.swing.event.TableModelEvent)
	 */
	// @Override
	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);
	}

}
