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
		super(root, Double.NaN);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.math.Distance#additiveTerm(double, double, double,
	 * double)
	 */
	@Override
	double additiveTerm(double x_i, double y_i, double root, double defaultValue) {
		return Math.pow(Math.abs(x_i - y_i), root);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.math.Distance#distance(java.lang.Iterable,
	 * java.lang.Iterable, double, double)
	 */
	@Override
	public double distance(Iterable<? extends Number> x,
			Iterable<? extends Number> y, double root, double defaultValue) {
		return root == 0d ? defaultValue : super.distance(x, y, root,
				defaultValue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.math.Distance#getStandardParameter()
	 */
	@Override
	public double getDefaultRoot() {
		return 2d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.math.Distance#getName()
	 */
	@Override
	public String getName() {
		if (root == 1d) {
			return "Manhattan";
		} else if (root == 2d) {
			return "Euclidean";
		}
		return root + "-metric";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.math.Distance#overallDistance(double, double,
	 * double)
	 */
	@Override
	double overallDistance(double d, double root, double defaultValue) {
		return Math.pow(d, 1d / root);
	}

}
