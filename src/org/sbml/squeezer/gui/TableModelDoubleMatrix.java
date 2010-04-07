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

}
