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
package org.sbml.squeezer.math;

/**
 * An implementation of an n-metric. An n-metric is basically the n-th root of
 * the sum of the distances of every single element in two vectors (arrays),
 * where this distance will always be exponentiated by the value of n.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2007-04-17
 */
public class NMetric extends Distance {

	/**
	 * Constructs a new NMetric with a default root of two. This will result in
	 * the Euclidean distance. Other metrics can be used by either setting the
	 * root to another value or explicitly using the distance function where the
	 * root must be given as an argument.
	 */
	public NMetric() {
		super();
	}

	/**
	 * Constructs a new NMetric with a costumized root. Depending on the values
	 * of root this results in different metrics. Some are especially important:
	 * <ul>
	 * <li>one is the Manhatten Norm or the city block metric.</li>
	 * <li>two is the Euclidean metric.</li>
	 * <li>Infinity is the maximum norm.</li>
	 * </ul>
	 * 
	 * @param root
	 */
	public NMetric(double root) {
		super(root);
	}

	/**
	 * Computes the root-Distance function. For example root = 2 gives the
	 * Euclidian Distance. If one array is longer than the other one only the
	 * first elements contribute to the distance.
	 * 
	 * @param x
	 *            a vector
	 * @param y
	 *            another vector
	 * @param root
	 *            what kind of distance function
	 * @return the distance of x and y
	 */
	public double distance(double[] x, double[] y, double root)
			throws IllegalArgumentException {
		if (x.length > y.length) {
			double[] swap = x;
			x = y;
			y = swap;
		}
		if (root == 0d) {
			throw new IllegalArgumentException("There is no 0-root!");
		}
		double d = 0;
		for (int i = 0; i < x.length; i++) {
			if (!Double.isNaN(y[i]) && !Double.isNaN(x[i]) && (y[i] != x[i])) {
				d += Math.pow(Math.abs(x[i] - y[i]), root);
			}
		}
		return Math.pow(d, 1d / root);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.math.Distance#getName()
	 */
	@Override
	public String getName() {
		if (root == 1d)
			return "Manhattan";
		else if (root == 2d)
			return "Euclidean";
		return root + "-metric";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.math.Distance#getStandardParameter()
	 */
	@Override
	public double getDefaultParameter() {
		return 2d;
	}

}
