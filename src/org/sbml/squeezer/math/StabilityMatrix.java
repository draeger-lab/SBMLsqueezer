package org.sbml.squeezer.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import eva2.tools.math.Jama.EigenvalueDecomposition;
import eva2.tools.math.Jama.Matrix;
import eva2.tools.math.Jama.QRDecomposition;

/**
 * This Class extends the representation of a m x n Matrix with some additional
 * functions
 * 
 * @author <a href="mailto:a.doerr@uni-tuebingen.de">Alexander D&ouml;rr</a>
 * @date 2009-12-18
 * @since 1.4
 */
public class StabilityMatrix extends Matrix {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a m x n StabilityMatrix
	 * 
	 * @param m
	 *            number of rows
	 * @param n
	 *            number of columns
	 */
	public StabilityMatrix(int m, int n) {
		super(m, n);

	}

	/**
	 * Creates a m x n StabilityMatrix with all entries set to c
	 * 
	 * @param m
	 *            number of rows
	 * @param n
	 *            number of columns
	 * @param c
	 *            scalar value
	 */
	public StabilityMatrix(int m, int n, int c) {
		super(m, n, c);

	}

	/**
	 * Creates a m x n StabilityMatrix with the entries given in array
	 * 
	 * @param array
	 *            entries
	 * @param m
	 *            number of rows
	 * @param n
	 *            number of columns
	 */
	public StabilityMatrix(double[][] array, int m, int n) {
		super(array, m, n);

	}

	/**
	 * Returns the real part of the eigenvalues of this matrix
	 * 
	 * @return array with the eigenvalues
	 */
	public double[] getEigenvalues() {

		return (new EigenvalueDecomposition(this)).getRealEigenvalues();

	}

	/**
	 * Substitutes the values of row m with the given values
	 * 
	 * @param m
	 *            number of the row to be changed
	 * @param row
	 *            the new values of row m
	 */
	public void setRow(int m, double[] row) {
		for (int i = 0; i < this.getColumnDimension(); i++)
			this.set(m, i, row[i]);

	}

	/**
	 * Copy a row from the matrix.
	 * 
	 * @return row m in a one-dimensional array
	 */
	public double[] getRow(int m) {
		double[] vals = new double[this.getColumnDimension()];
		for (int i = 0; i < this.getColumnDimension(); i++) {
			vals[i] = this.get(m, i);
		}
		return vals;
	}

	/**
	 * Substitutes the values of column n with the given values
	 * 
	 * @param n
	 *            number of the column to be changed
	 * @param row
	 *            the new values of column n
	 */
	public void setColumn(int n, double[] column) {
		for (int i = 0; i < this.getRowDimension(); i++)
			this.set(i, n, column[i]);

	}

	/**
	 * Returns a copy of this matrix without row m and column n
	 * 
	 * @param m
	 *            row index
	 * @param n
	 *            column index
	 * @return this matrix without row m and column n
	 */
	public StabilityMatrix getSubmatrix(int m, int n) {

		StabilityMatrix submatrix = new StabilityMatrix(
				this.getRowDimension() - 1, this.getColumnDimension() - 1);
		int subm = 0, subn = 0;

		for (int row = 0; row < this.getRowDimension(); row++) {

			if (row == m)
				continue;

			for (int column = 0; column < this.getColumnDimension(); column++) {

				if (column == n)
					continue;

				submatrix.set(subm, subn, this.get(row, column));
				subn++;
			}
			subn = 0;
			subm++;

		}

		return submatrix;
	}

	/**
	 * Returns a copy of this matrix without rows and columns with an number
	 * greater than index
	 * 
	 * @param index
	 *            max row/column number
	 * @return this matrix without rows and columns greater than the given index
	 */
	public StabilityMatrix getSubmatrix(int index) {
		StabilityMatrix submatrix = new StabilityMatrix(index + 1, index + 1);

		for (int r = 0; r <= index; r++) {

			for (int c = 0; c <= index; c++)
				submatrix.set(r, c, this.get(r, c));

		}

		return submatrix;
	}

	/**
	 * Returns a new StabilityMatrix without the given columns
	 * 
	 * @param integers
	 *            array with indices of the columns to be dropped, the indices
	 *            have to be unique and in ascending order
	 * 
	 * @return
	 */
	public StabilityMatrix dropColumns(Integer[] integers) {
		StabilityMatrix submatrix = new StabilityMatrix(getRowDimension(),
				getColumnDimension() - integers.length);

		for (int i = 0, n = 0; i < getColumnDimension(); i++) {

			if (Arrays.binarySearch(integers, i) < 0) {
				submatrix.setColumn(n, getColumn(i));
				n++;
			}
		}

		return submatrix;
	}

	/**
	 * Returns a new StabilityMatrix without the given rows
	 * 
	 * @param integers array with indices of the rows to be dropped, the indices
	 *            have to be unique and in ascending order
	 * 
	 * @return
	 */
	public StabilityMatrix dropRows(Integer[] integers) {
		StabilityMatrix submatrix = new StabilityMatrix(getRowDimension()
				- integers.length, getColumnDimension());

		for (int i = 0, n = 0; i < getRowDimension(); i++) {

			if (Arrays.binarySearch(integers, i) < 0) {
				submatrix.setRow(n, getRow(i));
				n++;
			}
		}

		return submatrix;
	}

	/**
	 * Changes the position of column i with column j and vice versa
	 * 
	 * @param i
	 * @param j
	 */
	public void swapColumns(int i, int j) {
		double[] columni = this.getColumn(i);

		this.setColumn(i, this.getColumn(j));
		this.setColumn(j, columni);

	}
	
	/**
	 * Changes the position of column i with column j and vice versa
	 * 
	 * @param i
	 * @param j
	 */
	public void swapRows(int i, int j) {
		double[] rowi = this.getRow(i);

		this.setRow(i, this.getRow(j));
		this.setRow(j, rowi);

	}

	/**
	 * Returns a HashSet with all column indeces at which the entry is equal to
	 * value
	 * 
	 * @param m
	 *            row index
	 * @return
	 */
	public Set<Integer> getColIndecesEqual(int m, double value) {
		HashSet<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < getColumnDimension(); i++) {
			if (get(m, i) == value)
				set.add(i);
		}

		return set;
	}

	/**
	 * Returns an array with all column indeces at which the entry is not equal
	 * to the given value
	 * 
	 * @param m
	 *            row index
	 * @return
	 */
	public Integer[] getColIndecesDiffer(int m, double value) {
		ArrayList<Integer> list = new ArrayList<Integer>();

		for (int i = 0; i < getColumnDimension(); i++) {
			if (get(m, i) != value)
				list.add(i);

			
		
		
		}
				
		Integer arr[] = new Integer[list.size()];

		return list.toArray(arr);
	}

	/**
	 * Checks if all entries in this matrix are zero
	 * 
	 * @return
	 */
	public boolean allZero() {
		boolean result = true;
		int i = 0;

		while (result && i < this.getRowDimension()) {
			if (getColIndecesEqual(i, 0).size() != this.getColumnDimension())
				result = false;

			i++;
		}

		return result;
	}

	/**
	 * Sums up the given row with the row at position m and returns the result
	 * 
	 * @param m
	 * @param tosum
	 * @return
	 */
	public double[] plusRow(int m, double[] tosum) {
		double[] row = this.getRow(m);

		for (int i = 0; i < row.length; i++) {
			row[i] += tosum[i];
		}

		return row;
	}
	
	/**
	 * 
	 * @param m
	 * @param scalar
	 * @return
	 */
	public double[] mulitplyRow(int m, double scalar) {
		double[] row = this.getRow(m);

		for (int i = 0; i < row.length; i++) {
			row[i] = row[i] * scalar;
		}

		return row;
	}

	/**
	 * Clone the StabilityMatrix object.
	 */
	@Override
	public StabilityMatrix clone() {
		return this.copy();
	}

	/**
	 * Returns a deep copy of a this StabilityMatrix
	 */
	@Override
	public StabilityMatrix copy() {
		StabilityMatrix copy = new StabilityMatrix(this.getRowDimension(), this
				.getColumnDimension());
		double[][] array = copy.getArray();
		for (int i = 0; i < this.getRowDimension(); i++) {
			for (int j = 0; j < this.getColumnDimension(); j++) {
				array[i][j] = this.get(i, j);
			}
		}
		return copy;
	}

	public StabilityMatrix times(StabilityMatrix B) {
		return new StabilityMatrix((((Matrix) (this)).times((Matrix) B))
				.getArray(), this.getRowDimension(), B.getColumnDimension());

	}

	/**
	 * Searches in this Matrix for columns that are linear dependent on other
	 * columns in this matrix (not working correctly)
	 * 
	 * @return ArrayList with arrays where the columns in each array are
	 *         dependent from each other
	 */
	@Deprecated
	public ArrayList<Integer[]> getDependencies() {
		ArrayList<Integer[]> dependencies = new ArrayList<Integer[]>();
		ArrayList<Integer> columns = new ArrayList<Integer>();
		ArrayList<Integer> remove = new ArrayList<Integer>();
		ArrayList<Integer> map = new ArrayList<Integer>();
		StabilityMatrix matrix = this;
		int listIndex = 0;
		int colIndex = 0;
		QRDecomposition qrDec;
		StabilityMatrix matrixA;
		StabilityMatrix matrixB = new StabilityMatrix(matrix.getRowDimension(),
				1, 0);
		Matrix matrixX;
		boolean dependency = false;
		boolean addedB = false;
		double value;
		Integer array[];

		for (int i = 0; i < getColumnDimension(); i++) {
			map.add(i);
		}

		while (matrix.getColumnDimension() > colIndex) {
			System.out.println("colsize: " + matrix.getColumnDimension());
			System.out.println("mapsize: " + map.size());
			matrixA = matrix.dropColumns(new Integer[] { colIndex });
			matrixB.setColumn(0, matrix.getColumn(colIndex));
			qrDec = new QRDecomposition(matrixA);

			matrixX = qrDec.solve(matrixB);
			System.out.println(matrixB.toString());
			System.out.println(matrixX.toString());

			for (int i = 0; i < matrixX.getRowDimension(); i++) {
				value = Math.round(matrixX.get(i, 0) * 100000.0);
				value = value / 10000;

				if (value % 10 == 0 && value != 0) {

					if (!addedB) {
						columns.add(map.get(colIndex));
						addedB = true;
					}
					// wenn pointer auf B kleiner als i
					if (colIndex <= i) {
						columns.add(map.get(i + 1));
						remove.add(i + 1);
					}

					else {
						columns.add(map.get(i));
						remove.add(i);
					}
					dependency = true;
				}

			}

			if (dependency) {
				remove.add(colIndex);
				map.removeAll(remove);

				array = new Integer[columns.size()];
				array = columns.toArray(array);
				System.out.println();
				dependencies.add(array);

				System.out.println("removing: "
						+ dependencies.get(listIndex).length);
				matrix = dropColumns(dependencies.get(listIndex));
				listIndex++;
				colIndex = 0;
				dependency = false;
				addedB = false;

			} else
				colIndex++;

		}

		return dependencies;
	}
}
