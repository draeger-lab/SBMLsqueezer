/*
 * Nov 13, 2007 Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 */
package org.sbmlsqueezer.gui.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.MouseInputListener;
import javax.swing.event.TableModelEvent;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;

import org.sbmlsqueezer.kinetics.BasicKineticLaw;
import org.sbmlsqueezer.kinetics.KineticLawGenerator;
import org.sbmlsqueezer.kinetics.ModificationException;
import org.sbmlsqueezer.kinetics.RateLawNotApplicableException;

import atp.sHotEqn;

/**
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Nov 13, 2007
 */
public class KineticLawJTable extends JTable implements MouseInputListener,
		ActionListener {

	/**
	 * Generated serial version ID
	 */
	private static final long serialVersionUID = -1575566223506382693L;

	private KineticLawGenerator klg;

	private boolean reversibility;

	private boolean editing;

	// private static final int widthMultiplier = 7;

	/**
	 * TODO
	 * 
	 * @param klg
	 * @param maxEducts
	 * @param reversibility
	 */
	public KineticLawJTable(KineticLawGenerator klg, int maxEducts,
			boolean reversibility) {
		super(new KineticLawTableModel(klg, maxEducts));
		this.klg = klg;
		this.reversibility = reversibility;
		getModel().addTableModelListener(this);
		// setRowHeightAppropriately();
		setColumnWidthAppropriately();
		setDefaultRenderer(Object.class, new KineticLawCellRenderer(maxEducts));
		getTableHeader()
				.setToolTipText(
						"<html>Double click on the kinetic law to apply another formalism.<br>"
								+ "Single click on any other column to get a formatted equation preview.</html>");
		setCellSelectionEnabled(true);
		setEnabled(true);
		addMouseListener(this);
		editing = false;
	}

	private void setColumnWidthAppropriately() {
		for (int col = 0; col < getColumnCount(); col++) {
			int maxLength = getColumnModel().getColumn(col).getHeaderValue()
					.toString().length();
			for (int row = 0; row < getRowCount(); row++)
				if (maxLength < getValueAt(row, col).toString().length())
					maxLength = getValueAt(row, col).toString().length();
			getColumnModel().getColumn(col).setPreferredWidth(
					3 * getFont().getSize() / 5 * maxLength);
		}
	}

	/**
	 * Specifies weather or not all reactions will be modeled in a reversible
	 * manner or as specified by the SBML model.
	 * 
	 * @param reversibility
	 */
	public void setReversibility(boolean reversibility) {
		this.reversibility = reversibility;
	}

	// /**
	// * Counts the number of new line symbols in each row of this table and
	// sets
	// * the row hight accordingly to a greater value, if necessary.
	// */
	// private void setRowHeightAppropriately() {
	// int newLines = 0;
	// for (int row = 0; row < dataModel.getRowCount(); row++) {
	// int maxNewLines = 0; // reset length
	// for (int column = 0; column < dataModel.getColumnCount(); column++) {
	// if (dataModel.getValueAt(row, column) != null) {
	// String value = dataModel.getValueAt(row, column).toString();
	// // max length update
	// StringTokenizer st = new StringTokenizer(value, "\n");
	// newLines = st.countTokens();
	// if (maxNewLines <= newLines)
	// maxNewLines = newLines;
	// newLines = 0;
	// }
	// }
	// // hier wird die groesste variable als zeilenhoehe gesetz bei der
	// // aktuellen Spalte
	// setRowHeight(row, maxNewLines * 18 + 18);
	// }
	// }

	/**
	 * Sets up a combo box, which allows to select an appropriate value for a
	 * kinetic law in the given row.
	 * 
	 * @param rowIndex
	 */
	private void setCellEditor(int rowIndex) {
		if ((dataModel.getRowCount() > 0) && (dataModel.getColumnCount() > 0)) {
			PluginModel model = klg.getModel();
			PluginReaction reaction = model.getReaction(dataModel.getValueAt(
					rowIndex, 0).toString());
			try {
				short[] possibleTypes = this.klg.identifyPossibleReactionTypes(
						model, reaction);

				Vector<BasicKineticLaw> possibleLaws = new Vector<BasicKineticLaw>();
				for (int i = 0; i < possibleTypes.length; i++)
					try {
						possibleLaws.add(klg.createKineticLaw(model, reaction,
								possibleTypes[i], reversibility));
					} catch (ModificationException exc) {
						exc.printStackTrace();
					} catch (RateLawNotApplicableException exc) {
						exc.printStackTrace();
					}
				JComboBox kineticLawComboBox = new JComboBox(possibleLaws);
				kineticLawComboBox.addActionListener(this);
				kineticLawComboBox.setEditable(false);
				kineticLawComboBox.setBackground(Color.WHITE);
				cellEditor = new DefaultCellEditor(kineticLawComboBox);
				((DefaultCellEditor) cellEditor).setClickCountToStart(2);
				getColumnModel().getColumn(convertColumnIndexToView(1))
						.setCellEditor(cellEditor);
			} catch (RateLawNotApplicableException e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), e
						.getClass().getName(), JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/*
	 * ====================== Action Event Handler =====================
	 */

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JComboBox) {
			int i;
			JComboBox combo = (JComboBox) e.getSource();
			BasicKineticLaw kineticLaw = (BasicKineticLaw) combo
					.getSelectedItem();
			// Reaction Identifier, Kinetic Law, SBO, #Reactants,
			// Reactants, Products, Parameters, Formula
			String params = kineticLaw.getLocalParameters().get(
					kineticLaw.getLocalParameters().size() - 1);
			for (i = kineticLaw.getLocalParameters().size() - 2; i > 0; i--)
				params = kineticLaw.getLocalParameters().get(i) + ", " + params;
			dataModel.setValueAt(kineticLaw, getSelectedRow(), 1);
			dataModel.setValueAt(new String(kineticLaw.getSBO()),
					getSelectedRow(), 2);
			dataModel.setValueAt(params, getSelectedRow(), dataModel
					.getColumnCount() - 2);
			dataModel.setValueAt(new String(kineticLaw.getFormula()),
					getSelectedRow(), dataModel.getColumnCount() - 1);
			i = 0;
			while ((i < klg.getModel().getNumReactions())
					&& (!klg.getModel().getReaction(i).getId().equals(
							kineticLaw.getParentReaction().getId())))
				i++;
			klg.getReactionNumAndKineticLaw().put(Integer.valueOf(i),
					kineticLaw);
			setColumnWidthAppropriately();
			editing = false;
		}
	}

	/*
	 * ====================== Mouse Event Handler =====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		// Point p = e.getPoint();
		// int rowIndex = rowAtPoint(p);
		// int colIndex = columnAtPoint(p);
		// // System.out.println("Mouse clicked, Zeile: " + rowIndex +
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
			BasicKineticLaw kinetic = (BasicKineticLaw) dataModel.getValueAt(
					rowIndex, 1);
			String LaTeX = kinetic.getKineticTeX().replaceAll("text", "mbox")
					.replaceAll("mathrm", "mbox");
			JComponent component = new sHotEqn("\\begin{equation}" + LaTeX
					+ "\\end{equation}");
			JPanel panel = new JPanel(new BorderLayout());
			component.setBackground(Color.WHITE);
			panel.setBackground(Color.WHITE);
			panel.add(component, BorderLayout.CENTER);
			panel.setLocation(((int) MouseInfo.getPointerInfo().getLocation()
					.getX())
					- this.getTopLevelAncestor().getX(), this.getY() + 10);
			panel.setBorder(BorderFactory.createLoweredBevelBorder());
			JOptionPane.showMessageDialog(getParent(), panel,
					"Rate Law of Reaction " + kinetic.getParentReactionID(),
					JOptionPane.INFORMATION_MESSAGE);
			// JLayeredPane.getLayeredPaneAbove(getParent()).add(component,
			// JLayeredPane.POPUP_LAYER);
			validate();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		// Point p = e.getPoint();
		// int rowIndex = rowAtPoint(p);
		// int colIndex = convertColumnIndexToModel(columnAtPoint(p));
		// // System.out.println("Mouse entered, Zeile: " + rowIndex +
		// "\tSpalte: "
		// // + colIndex);
		// if (colIndex == dataModel.getColumnCount() - 1) {
		// JComponent component = new sHotEqn("\\begin{equation}"
		// + ((BasicKineticLaw) dataModel.getValueAt(rowIndex, 1))
		// .getKineticTeX().replaceAll("text", "mbox") + "\\end{equation}");
		// component.setBackground(Color.WHITE);
		// component.setLocation(((int) MouseInfo.getPointerInfo().getLocation()
		// .getX())
		// - this.getTopLevelAncestor().getX(), this.getY() + 10);
		// // component.setSize(100, 20);
		// component.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		// JOptionPane.showMessageDialog(getParent(), component);
		// //JLayeredPane.getLayeredPaneAbove(getParent()).add(component,
		// // JLayeredPane.POPUP_LAYER);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		// Point p = e.getPoint();
		// int rowIndex = rowAtPoint(p);
		// int colIndex = columnAtPoint(p);
		// System.out.println("Mouse exited, Zeile: " + rowIndex + "\tSpalte: "
		// + colIndex);
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
			setCellEditor(rowIndex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
		// Point p = e.getPoint();
		// int rowIndex = rowAtPoint(p);
		// int colIndex = columnAtPoint(p);
		// System.out.println("Mouse released, Zeile: " + rowIndex + "\tSpalte:
		// "
		// + colIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	public void mouseDragged(MouseEvent e) {
		// Point p = e.getPoint();
		// int rowIndex = rowAtPoint(p);
		// int colIndex = columnAtPoint(p);
		// System.out.println("Mouse draged, Zeile: " + rowIndex + "\tSpalte: "
		// + colIndex);
	}

	public void mouseMoved(MouseEvent e) {
		// Point p = e.getPoint();
		// int rowIndex = rowAtPoint(p);
		// int colIndex = columnAtPoint(p);
		// System.out.println("Mouse moved, Zeile: " + rowIndex + "\tSpalte: "
		// + colIndex);
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);
	}

}
