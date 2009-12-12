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
package org.sbmlsqueezer.math;

/**
 * An implementation of the Gaussian method to compute the column rank of a
 * matrix.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Aug 1, 2007
 */
public class GaussianRank {

	private double[][] N;

	private int lineNum;

	private int columnNum;

	private int columnRank;

	/**
	 * Initialize this object with the given matrix. This will also compute the
	 * column rank and store it in an according field. The rank can be obtained
	 * by invoking the method {@see getColumnRank}.
	 * 
	 * @param matrix
	 *            This double matrix has to have an equal number of columns for
	 *            each row. Otherwise, i.e. if this matrix is not exactly of
	 *            rectangular shape, the results are undefined.
	 */
	public GaussianRank(double[][] matrix) {
		this.N = matrix;
		this.lineNum = N.length;
		this.columnNum = N[lineNum - 1].length;
		this.rowShapeTriangular();
		this.columnRank = computeColumnRank();
	}

	/**
	 * Returns the column rank of the given matrix.
	 * 
	 * @return
	 */
	public int getColumnRank() {
		return columnRank;
	}

	/**
	 * Returns true if the matrix has full rank.
	 * 
	 * @return
	 */
	public boolean hasFullRank() {
		if (this.columnRank == this.columnNum)
			return true;
		return false;
	}

	private void add(double la, int i, int j) {
		for (int k = 0; k < columnNum; ++k) { // la-faches der Zeile i zur Zeile
			// j.
			N[j][k] += N[i][k] * la;
		}
	}

	/**
	 * Computes the collumn rank of the given matrix N.
	 * 
	 * @return The collumn rank of the matrix N.
	 */
	private int computeColumnRank() {
		int c = 0, d, i, j;
		for (i = 0; i < lineNum; ++i) {
			d = 0;
			for (j = 0; j < columnNum; ++j)
				if (N[i][j] != 0)
					d = 1;
			if (d == 1)
				c++;
		}
		return c;
	}

	private void rowShapeTriangular() {
		int i = 0, j = 0;
		zst_rek(i, j);
	}

	private int searchPivot(int i, int j) {
		int piv = i;
		for (int k = i; k < lineNum; k++)
			if ((N[k][j]) > N[piv][j])
				piv = k;
		return piv;
	}

	private void swapRows(int i, int j) {
		for (int k = 0; k < columnNum; ++k) { // vertausche Zeilen i und j.
			double temp = N[i][k];
			N[i][k] = N[j][k];
			N[j][k] = temp;
		}
	}

	private void zst_rek(int i, int j) {
		if (i == lineNum - 1 || j >= columnNum) // Abbruchbedingung
			return;
		int piv = searchPivot(i, j); // suche Pivotelement unterhalb des Element
		// i,j
		if (N[piv][j] == 0) { // kein Pivotelement != 0 gefunden
			zst_rek(i, j + 1); // gleiche Zeile, naechste Spalte
			return;
		}
		swapRows(i, piv); // vertausche Zeile i mit Zeile piv
		for (int ii = i + 1; ii < lineNum; ++ii) {
			double d = -N[ii][j] / N[i][j];
			add(d, i, ii);
		}// mache j. Spalte unter i.ter Zeile zu 0
		zst_rek(i + 1, j + 1); // naechste Zeile, naechste Spalte
		return;
	}

}
