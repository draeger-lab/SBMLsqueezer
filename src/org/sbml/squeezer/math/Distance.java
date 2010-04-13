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
 * This class is the basis of various implementations of distance functions.
 * 
 * @version 0.1
 * @since 1.4
 * @author Andreas Dr&auml;ger
 * @date 17.04.2007
 */
public abstract class Distance {

	protected double root;

	/**
	 * Default constructor. This sets the standard value for the parameter as
	 * given by the getStandardParameter() method.
	 */
	public Distance() {
		this.root = getStandardParameter();
	}

	/**
	 * Constructor, which allows setting the parameter value for root.
	 * 
	 * @param root
	 *            The parameter for this distance.
	 */
	public Distance(double root) {
		this.root = root;
	}

	/**
	 * Returns the distance of the two vectors x and y where the currently set
	 * root is used. This can be obtained by invoking the {@see getRoot} method.
	 * It is possible that one matrix contains more columns than the other one.
	 * If so, the additional values in the bigger matrix are ignored and do not
	 * contribute to the distance. <code>NaN</code> values do also not
	 * contribute to the distance.
	 * 
	 * @param x
	 * @param y
	 * @return
	 * @throws IllegalArgumentException
	 */
	public double distance(double[] x, double[] y) {
		return distance(x, y, root);
	}

	/**
	 * Returns the distance of the two vectors x and y with the given root. This
	 * may be the root in a formal way or a default value to be returned if the
	 * distance uses a non defined operation. If one array is longer than the
	 * other one additional values do not contribute to the distance.
	 * <code>NaN</code> values are also ignored.
	 * 
	 * @param x
	 *            an array
	 * @param y
	 *            another array
	 * @param root
	 *            Some necessary parameter.
	 * @return
	 * @throws IllegalArgumentException
	 */
	public abstract double distance(double[] x, double[] y, double root);

	/**
	 * Computes the distance of two matrices as the sum of the distances of each
	 * row. It is possible that one matrix contains more columns than the other
	 * one. If so, the additional values in the bigger matrix are ignored and do
	 * not contribute to the distance. <code>NaN</code> values do also not
	 * contribute to the distance.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public double distance(double[][] x, double[][] y) {
		if (x.length > y.length) {
			double[][] swap = y;
			y = x;
			x = swap;
		}
		double d = 0;
		for (int i = 0; i < x.length; i++)
			d += distance(x[i], y[i]);
		return d;
	}

	/**
	 * The name of this distance measurement.
	 * 
	 * @return
	 */
	public abstract String getName();

	@Override
	public String toString() {
		return getName() + " with root = " + getRoot();
	}

	/**
	 * Returns the currently set root or default value for the distance
	 * function.
	 * 
	 * @return
	 */
	public double getRoot() {
		return this.root;
	}

	/**
	 * Returns the standard value for the parameter to compute the distance.
	 * 
	 * @return
	 */
	public abstract double getStandardParameter();

	/**
	 * Set the current root to be used in the distance function to the specified
	 * value.
	 * 
	 * @param root
	 */
	public void setRoot(double root) {
		this.root = root;
	}

}
