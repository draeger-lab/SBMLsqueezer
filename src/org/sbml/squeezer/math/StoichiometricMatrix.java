package org.sbml.squeezer.math;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This Class represents a m x m stoichimetric matrix
 * 
 * @author <a href="mailto:a.doerr@uni-tuebingen.de">Alexander D&ouml;rr</a>
 * @date
 *
 */
public class StoichiometricMatrix extends StabilityMatrix {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a m x n stoichiometric matrix
	 * 
	 * @param m
	 *            number of rows
	 * @param n
	 *            number of columns
	 */
	public StoichiometricMatrix(int m, int n) {
		super(m, n);

	}

	/**
	 * Creates a m x n stoichiometric matrix with all entries set to c
	 * 
	 * @param m
	 *            number of rows
	 * @param n
	 *            number of columns
	 * @param c
	 *            scalar value
	 */
	public StoichiometricMatrix(int m, int n, int c) {
		super(m, n, c);

	}

	/**
	 * Creates a m x n stoichiometric matrix with the entries given in array
	 * 
	 * @param array
	 *            entries
	 * @param m
	 *            number of rows
	 * @param n
	 *            number of columns
	 */
	public StoichiometricMatrix(double[][] array, int m, int n) {
		super(array, m, n);

	}

	public StabilityMatrix getConservationRelations() {
		// tableauLeft and tableauRight are representing together the whole
		// tableau T
		StabilityMatrix tableauLeft = this;
		StabilityMatrix tableauRight = new IdentityMatrix(this
				.getRowDimension());
		// backup varibales for the tableaux
		StabilityMatrix backupLeft, backupRight;

		List<Set<Integer>> listOfSetsS;
		ArrayList<int[]> combinations = new ArrayList<int[]>();
		ArrayList<Integer> listOfZeroRows = new ArrayList<Integer>();

		int j = 0, i = 0, rownum = 0;

		// for each column and as long as there are remaining tableaux to build
		// and matrixS contains not only zero values
		for (j = 0; j < tableauLeft.getColumnDimension()
				&& !tableauLeft.allZero(); j++) {
			// build sets
			listOfSetsS = buildSets(tableauRight);
			for (i = 0; i < tableauLeft.getRowDimension(); i++) {
				// for each row
				if (tableauLeft.get(i, j) == 0) {
					listOfZeroRows.add(Integer.valueOf(i));
					continue; // violating condition (3.7) anyways.
				}
				// for each remaining row
				for (int k = i + 1; k < tableauLeft.getRowDimension(); k++) {
					// check conditions
					if (tableauLeft.get(k, j) == 0)
						continue; // otherwise violating condition (3.7)

					if (tableauLeft.get(i, j) * tableauLeft.get(k, j) < 0) {
						// condition (3.7) satisfied now checking (3.8)
						Set<Integer> intersection = new HashSet<Integer>(
								listOfSetsS.get(i));
						intersection.retainAll(listOfSetsS.get(k));
						boolean isSubset = false;
						for (int l = 0; l < listOfSetsS.size() && !isSubset; l++) {
							if (l == k || l == i)
								continue;
							if (listOfSetsS.get(l).containsAll(intersection))
								isSubset = true;
						}
						if (!isSubset) // condition (3.8) satisfied
							// create a new combination of rows
							combinations.add(new int[] { i, k });
					}
				}
			}
			backupLeft = new StabilityMatrix(combinations.size()
					+ listOfZeroRows.size(), tableauLeft.getColumnDimension(),
					0);
			backupRight = new StabilityMatrix(combinations.size()
					+ listOfZeroRows.size(), tableauRight.getColumnDimension(),
					0);

			// build T(j+1)

			// uses all found combinations to construct rows for T(j+1)
			// single rows for addition und multiplication
			StabilityMatrix rowS1 = new StabilityMatrix(1, this
					.getColumnDimension());
			StabilityMatrix rowS2 = new StabilityMatrix(1, this
					.getColumnDimension());
			StabilityMatrix rowI1 = new StabilityMatrix(1, this
					.getRowDimension());
			StabilityMatrix rowI2 = new StabilityMatrix(1, this
					.getRowDimension());
			// compute all theta values
			for (rownum = 0; rownum < combinations.size(); rownum++) {
				// calculate a row for the left tableau
				// ith row of left tableau
				rowS1
						.setRow(0, tableauLeft
								.getRow(combinations.get(rownum)[0]));
				// kth element of column j in left tableau
				rowS1.multi(Math.abs(tableauLeft.get(
						combinations.get(rownum)[1], j)));
				// kth row of left tableau
				rowS2
						.setRow(0, tableauLeft
								.getRow(combinations.get(rownum)[1]));
				// ith element of column j in left tableau
				rowS2.multi(Math.abs(tableauLeft.get(
						combinations.get(rownum)[0], j)));
				// theta left:
				backupLeft.setRow(rownum, rowS1.plusRow(0, rowS2.getRow(0)));

				// calculate a row for the right tableau
				// ith row of right tableau
				rowI1.setRow(0, tableauRight
						.getRow(combinations.get(rownum)[0]));
				// kth element of jth column in left tableau (because j is in
				// [0,..,r])
				rowI1.multi(Math.abs(tableauLeft.get(
						combinations.get(rownum)[1], j)));
				// kth row of right tableau
				rowI2.setRow(0, tableauRight
						.getRow(combinations.get(rownum)[1]));
				// ith element of jth column in left tableau
				rowI2.multi(Math.abs(tableauLeft.get(
						combinations.get(rownum)[0], j)));
				// theta right:
				backupRight.setRow(rownum, rowI1.plusRow(0, rowI2.getRow(0)));
			}

			// takes over all rows from T(j) to T(j+1) where the entry at column
			// j is zero
			for (i = 0; i < listOfZeroRows.size(); i++) {
				backupLeft.setRow(rownum, tableauLeft.getRow(listOfZeroRows
						.get(i).intValue()));
				backupRight.setRow(rownum, tableauRight.getRow(listOfZeroRows
						.get(i).intValue()));
				rownum++;
			}
			tableauLeft = backupLeft.clone();
			tableauRight = backupRight.clone();

			combinations.clear();
			listOfZeroRows.clear();

		}
		return tableauRight;
	}

	/**
	 * Returns a list of sets (one for each row) of a matrix with the column
	 * indices where the elements are zero
	 * 
	 * @param matrixI
	 *            matrix from which to build the sets
	 * @param listofsets
	 *            ArrayList to save the sets
	 */
	private List<Set<Integer>> buildSets(StabilityMatrix matrixI) {
		List<Set<Integer>> listofsets = new ArrayList<Set<Integer>>();
		for (int i = 0; i < matrixI.getRowDimension(); i++)
			listofsets.add(matrixI.getColIndecesEqual(i, 0));

		return listofsets;

	}

	public StabilityMatrix getNullSpace() {
		int[] constraints = new int[this.getColumnDimension()];
		int[] meta = new int[this.getRowDimension()];
		int unconstrained = 0;
		getFluxConstraints(constraints, unconstrained);
		getMetaboliteConstraints(meta);
		Integer[] toDrop;
		int dropCounter=0;

		// tableauLeft and tableauRight are representing together the whole
		// tableau T
		StabilityMatrix tableauLeft =	new StabilityMatrix(this.transpose()
				.getArray(), this.getColumnDimension(), this
				.getRowDimension());
		
		StabilityMatrix tableauRight = new IdentityMatrix(tableauLeft
				.getRowDimension());

		StabilityMatrix tableauLeftEx = new StabilityMatrix(unconstrained,
				tableauLeft.getRowDimension());
		StabilityMatrix tableauRightEx = new StabilityMatrix(unconstrained,
				tableauRight.getRowDimension());
		
		toDrop = new Integer[unconstrained];
		
		for (int i = 0; i < constraints.length; i++) {
			if (constraints[i] == -1){
			tableauLeft.mulitplyRow(i, -1);
			//tableauRight.mulitplyRow(i, -1); ????
			}
			else if(constraints[i] == 0){
				tableauLeftEx.setRow(dropCounter, tableauLeftEx.getRow(i));
				tableauRightEx.setRow(dropCounter, tableauRightEx.getRow(i));
				toDrop[dropCounter] = i;
				dropCounter++;
			}
				
		}
		tableauLeft.dropColumns(toDrop);
		tableauRight.dropColumns(toDrop);
		
		
		

		// backup varibales for the tableaux
		StabilityMatrix backupLeft, backupRight;

		List<Set<Integer>> listOfSetsS;
		ArrayList<int[]> combinations = new ArrayList<int[]>();
		ArrayList<Integer> listOfZeroRows = new ArrayList<Integer>();

		int j = 0, i = 0, rownum = 0;

	
		
		// for each column and as long as there are remaining tableau to build
		// and matrixS contains not only zero values
		for (j = 0; j < tableauLeft.getColumnDimension()
				&& !tableauLeft.allZero(); j++) {
			
			if  (meta[j] != 1){
				continue;
			}
				
			// build sets
			listOfSetsS = buildSets(tableauRight);
			for (i = 0; i < tableauLeft.getRowDimension(); i++) {
				// for each row
				if (tableauLeft.get(i, j) == 0) {
					listOfZeroRows.add(Integer.valueOf(i));
					continue; // violating condition (3.7) anyways.
				}
				// for each remaining row
				for (int k = i + 1; k < tableauLeft.getRowDimension(); k++) {
					// check conditions
					if (tableauLeft.get(k, j) == 0)
						continue; // otherwise violating condition (3.7)

					if (tableauLeft.get(i, j) * tableauLeft.get(k, j) < 0) {
						// condition (3.7) satisfied now checking (3.8)
						Set<Integer> intersection = new HashSet<Integer>(
								listOfSetsS.get(i));
						intersection.retainAll(listOfSetsS.get(k));
						boolean isSubset = false;
						for (int l = 0; l < listOfSetsS.size() && !isSubset; l++) {
							if (l == k || l == i)
								continue;
							if (listOfSetsS.get(l).containsAll(intersection))
								isSubset = true;
						}
						if (!isSubset) // condition (3.8) satisfied
							// create a new combination of rows
							combinations.add(new int[] { i, k });
					}
				}
			}
			backupLeft = new StabilityMatrix(combinations.size()
					+ listOfZeroRows.size(), tableauLeft.getColumnDimension(),
					0);
			backupRight = new StabilityMatrix(combinations.size()
					+ listOfZeroRows.size(), tableauRight.getColumnDimension(),
					0);

			// build T(j+1)

			// uses all found combinations to construct rows for T(j+1)
			// single rows for addition und multiplication
			StabilityMatrix rowS1 = new StabilityMatrix(1, this
					.getColumnDimension());
			StabilityMatrix rowS2 = new StabilityMatrix(1, this
					.getColumnDimension());
			StabilityMatrix rowI1 = new StabilityMatrix(1, this
					.getRowDimension());
			StabilityMatrix rowI2 = new StabilityMatrix(1, this
					.getRowDimension());
			// compute all theta values
			for (rownum = 0; rownum < combinations.size(); rownum++) {
				// calculate a row for the left tableau
				// ith row of left tableau
				rowS1
						.setRow(0, tableauLeft
								.getRow(combinations.get(rownum)[0]));
				// kth element of column j in left tableau
				rowS1.multi(Math.abs(tableauLeft.get(
						combinations.get(rownum)[1], j)));
				// kth row of left tableau
				rowS2
						.setRow(0, tableauLeft
								.getRow(combinations.get(rownum)[1]));
				// ith element of column j in left tableau
				rowS2.multi(Math.abs(tableauLeft.get(
						combinations.get(rownum)[0], j)));
				// theta left:
				backupLeft.setRow(rownum, rowS1.plusRow(0, rowS2.getRow(0)));

				// calculate a row for the right tableau
				// ith row of right tableau
				rowI1.setRow(0, tableauRight
						.getRow(combinations.get(rownum)[0]));
				// kth element of jth column in left tableau (because j is in
				// [0,..,r])
				rowI1.multi(Math.abs(tableauLeft.get(
						combinations.get(rownum)[1], j)));
				// kth row of right tableau
				rowI2.setRow(0, tableauRight
						.getRow(combinations.get(rownum)[1]));
				// ith element of jth column in left tableau
				rowI2.multi(Math.abs(tableauLeft.get(
						combinations.get(rownum)[0], j)));
				// theta right:
				backupRight.setRow(rownum, rowI1.plusRow(0, rowI2.getRow(0)));
			}

			// takes over all rows from T(j) to T(j+1) where the entry at column
			// j is zero
			for (i = 0; i < listOfZeroRows.size(); i++) {
				backupLeft.setRow(rownum, tableauLeft.getRow(listOfZeroRows
						.get(i).intValue()));
				backupRight.setRow(rownum, tableauRight.getRow(listOfZeroRows
						.get(i).intValue()));
				rownum++;
			}
			tableauLeft = backupLeft.clone();
			tableauRight = backupRight.clone();

			combinations.clear();
			listOfZeroRows.clear();

		}
		
		
		
		for (i = 0; i < tableauLeft.getColumnDimension(); i++) {
			
			for (j = 0; j < tableauLeft.getRowDimension(); j++) {
				
				
			}
		}

		return tableauRight;
	}
	


	private void getFluxConstraints(int[] constraints, int unconstrained) {
		for (int i = 0; i < constraints.length; i++) {
			constraints[i] = -1;
		}
	}
	
	private void getMetaboliteConstraints(int[] constraints){
		for (int i = 0; i < constraints.length; i++) {
			constraints[i] = 1;
		}
	}

}
