package org.sbml.squeezer.math;

/**
 * This Class represents a m x m identity matrix
 * 
 * @author <a href="mailto:a.doerr@uni-tuebingen.de">Alexander D&ouml;rr</a>
 * @date
 * 
 */
public class IdentityMatrix extends StabilityMatrix {

	private static final long serialVersionUID = 1L;

	public IdentityMatrix(int m) {
		super(m, m, 0);
		setOne();

	}

	/**
	 * Sets in each row/column a one at the position characteristic for an
	 * identity matrix
	 */
	private void setOne() {

		for (int i = 0; i < this.getColumnDimension(); i++) {
			this.set(i, i, 1);
		}
	}

}
