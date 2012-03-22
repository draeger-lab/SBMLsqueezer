/*
 * $Id$
 * $URL$
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
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TableModelEvent;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.util.compilers.LaTeXCompiler;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.kinetics.TypeStandardVersion;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.LaTeXOptions;

import atp.sHotEqn;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.prefs.SBPreferences;

/**
 * Creates a table that displays all created kinetic equationns.
 * 
 * @author Andreas Dr&auml;ger
 * @date Nov 13, 2007
 * @since 1.0
 * @version $Rev$
 */
public class KineticLawTable extends JTable implements MouseInputListener {
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);

	/**
	 * Generated serial version ID
	 */
	private static final long serialVersionUID = -1575566223506382693L;

	private boolean editing, reversibility;

	private KineticLawGenerator klg;

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
		this.reversibility = klg.getPreferences().getBoolean(
				SqueezerOptions.TREAT_ALL_REACTIONS_REVERSIBLE);
		getModel().addTableModelListener(this);
		setColumnWidthAppropriately();
		setRowHeightAppropriately();
		setDefaultRenderer(Object.class, new KineticLawTableCellRenderer(klg
				.getPreferences().getInt(
						SqueezerOptions.MAX_NUMBER_OF_REACTANTS)));
		getTableHeader().setToolTipText(
				StringUtil.toHTML(MESSAGES.getString("KINTEIC_LAW_TABLE_HEADER_TOOLTIP"), 40));
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
				String LaTeX;
				try {
					SBPreferences prefs = SBPreferences
							.getPreferencesFor(LaTeXOptions.class);
					LaTeX = kinetic.getMath().compile(new LaTeXCompiler(
											prefs.getBoolean(LaTeXOptions.PRINT_NAMES_IF_AVAILABLE)))
							.toString().replace("text", "mbox").replace(
									"mathrm", "mbox").replace("mathtt", "mbox");
				} catch (SBMLException e1) {
					LaTeX = "invalid";
				}
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
						MessageFormat.format(MESSAGES.getString("RATE_LAW_OF_REACTION"), 
								kinetic.getParentSBMLObject().getId()),
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
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
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
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
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
				GUITools.showErrorMessage(this, exc);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
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
				final Class<?> possibleTypes[] = this.klg.getReactionType(
						reaction.getId()).identifyPossibleKineticLaws();
				final BasicKineticLaw possibleLaws[] = new BasicKineticLaw[possibleTypes.length];
				int selected = 0;
				final BasicKineticLaw oldLaw = ((BasicKineticLaw) klg
						.getModifiedReaction(reaction.getId()).getKineticLaw());
				SBPreferences prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
				boolean reversibility = prefs.getBoolean(SqueezerOptions.TREAT_ALL_REACTIONS_REVERSIBLE);
				double defaultParamVal = prefs.getDouble(SqueezerOptions.DEFAULT_NEW_PARAMETER_VAL);
				TypeStandardVersion version = TypeStandardVersion.valueOf(prefs.get(SqueezerOptions.TYPE_STANDARD_VERSION));
				UnitConsistencyType consistency = UnitConsistencyType.valueOf(prefs.get(SqueezerOptions.TYPE_UNIT_CONSISTENCY));
				for (int i = 0; i < possibleLaws.length; i++) {
					possibleLaws[i] = klg.createKineticLaw(reaction,
							possibleTypes[i], reversibility, version, consistency, defaultParamVal);
					if (possibleLaws[i].getSimpleName().equals(
							oldLaw.getSimpleName()))
						selected = i;
				}
				klg.getPreferences().flush();
				final KineticLawSelectionPanel klsp = new KineticLawSelectionPanel(
						possibleLaws, selected);
				final JOptionPane pane = new JOptionPane(klsp,
						JOptionPane.QUESTION_MESSAGE,
						JOptionPane.OK_CANCEL_OPTION, UIManager
								.getIcon("ICON_LEMON_SMALL"));
				pane.selectInitialValue();
				Container container = getTopLevelAncestor();
				final JDialog dialog;
				if (container instanceof Frame)
					dialog = new JDialog((Frame) container,
							MessageFormat.format(MESSAGES.getString("CHOOSE_ALTERNATIVE_KINETIC_LAW"),
									reaction.getId()));
				else if (container instanceof Dialog)
					dialog = new JDialog((Dialog) container,
							MessageFormat.format(MESSAGES.getString("CHOOSE_ALTERNATIVE_KINETIC_LAW"),
									reaction.getId()));
				else {
					dialog = new JDialog();
					dialog.setTitle(MessageFormat.format(MESSAGES.getString("CHOOSE_ALTERNATIVE_KINETIC_LAW"),
							reaction.getId()));
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
	@SuppressWarnings("deprecation")
	public void updateTable(KineticLaw kineticLaw) {
		// Reaction Identifier, Kinetic Law, SBO, #Reactants,
		// Reactants, Products, Parameters, Formula
		int i;
		StringBuffer params = new StringBuffer();
		for (i = kineticLaw.getNumLocalParameters() - 1; i > 0; i--) {
			params.append(kineticLaw.getLocalParameter(i));
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
						kineticLaw.getParentSBMLObject().getId()))) {
			i++;
		}
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
			for (int row = 0; row < getRowCount(); row++) {
				if (maxLength < getValueAt(row, col).toString().length()) {
					maxLength = getValueAt(row, col).toString().length();
				}
			}
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
