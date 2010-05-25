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

import java.util.HashMap;

import javax.swing.table.AbstractTableModel;

/**
 * @author Andreas Dr&auml;ger
 * 
 */
public class TableModelDoubleMatrix extends AbstractTableModel {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 4899276425058689424L;

	/**
	 * The names of the columns
	 */
	private Object colNames[];

	/**
	 * The actual data
	 */
	private double data[][];

	/**
	 * 
	 */
	public TableModelDoubleMatrix() {
		super();
	}

	/**
	 * 
	 * @param data
	 */
	public TableModelDoubleMatrix(double[][] data) {
		this();
		setData(data);
	}

	/**
	 * 
	 * @param data
	 * @param colNames
	 */
	public TableModelDoubleMatrix(double[][] data, Object[] colNames) {
		this(data);
		setColumnNames(colNames);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return data == null || data.length == 0 ? 0 : data[0].length;
	}

	/**
	 * Returns a double array that contains all values of the specified column.
	 * 
	 * @param columnIndex
	 * @return
	 */
	public double[] getColumnData(int columnIndex) {
		double col[] = new double[getRowCount()];
		for (int i = 0; i < col.length; i++)
			col[i] = getValueAt(i, columnIndex);
		return col;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	public String getColumnName(int column) {
		if (colNames == null)
			return super.getColumnName(column);
		return colNames[column].toString();
	}

	/**
	 * 
	 * @return
	 */
	public double[][] getData() {
		return data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return data == null ? 0 : data.length;
	}

	/**
	 * Returns the specified row in an array.
	 * 
	 * @param rowIndex
	 * @return
	 */
	public double[] getRowData(int rowIndex) {
		return data[rowIndex];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Double getValueAt(int rowIndex, int columnIndex) {
		return Double.valueOf(data[rowIndex][columnIndex]);
	}

	/**
	 * Set the column names for this table.
	 * 
	 * @param colNames
	 */
	public void setColumnNames(Object[] colNames) {
		this.colNames = colNames;
	}

	/**
	 * Set the data for this table.
	 * 
	 * @param data
	 */
	public void setData(double data[][]) {
		this.data = data;
		if (colNames != null && colNames.length != getColumnCount())
			colNames = null;
	}

	/**
	 * @param colNames
	 */
	public void swapColumns(String[] colNames) {
		HashMap<Integer, Integer> oldPosNewPos = new HashMap<Integer, Integer>();
		for (int i = 0; i < this.colNames.length; i++) {
			for (int j = 0; j < colNames.length; j++) {
				if (this.colNames[i].equals(colNames[j])) {
					oldPosNewPos.put(Integer.valueOf(i), Integer.valueOf(j));
					break;
				}
			}
		}
		for (Integer key : oldPosNewPos.keySet()) {
			swap(key.intValue(), oldPosNewPos.get(key).intValue());
		}
	}

	/**
	 * Swaps a and b.
	 * 
	 * @param a
	 * @param b
	 */
	private void swap(Object a, Object b) {
		Object swap = b;
		b = a;
		a = swap;
	}

	/**
	 * 
	 * @param fromIdx
	 * @param toIdx
	 */
	public void swapColumn(int fromIdx, int toIdx) {
		if (fromIdx != toIdx) {
			for (int i = 0; i < data.length; i++) {
				swap(data[i][fromIdx], data[i][toIdx]);
				swap(colNames[fromIdx], colNames[toIdx]);
			}
		}
	}

}
