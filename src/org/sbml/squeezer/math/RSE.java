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
 * An implementation of the relative squared error with a default value to avoid
 * division by zero.
 * 
 * @version 0.1
 * @since 1.4
 * @author Andreas Dr&auml;ger <andreas.draeger@uni-tuebingen.de>
 * @date 17.04.2007
 * @uml.dependency supplier="org.jcell.server.math.Distance"
 */
public class RSE extends Distance {

	/**
	 * Constructs a new RelativeSquaredError with a default root of 10,000. Here
	 * the root is the default value to be returned by the distance function.
	 */
	public RSE() {
		super();
	}

	/**
	 * Constructs a new relative squared error object with the specified default
	 * value do avoid division by zero. The standard value is 10,000.
	 * 
	 * @param standard
	 */
	public RSE(double standard) {
		super(standard);
	}

	/**
	 * Computes the relative distance of vector x to vector y. Therefore the
	 * difference of x[i] and y[i] is divided by y[i] for every i. If y[i] is
	 * zero, the default value <code>def</code> is used instead. The sum of
	 * these differences gives the distance function. It is possible that one
	 * array is longer than the other one. If so, the additional values in the
	 * longer array are ignored and do not contribute to the distance.
	 * <code>NaN</code> values do also not contribute to the distance.
	 * 
	 * @param x
	 *            A vector
	 * @param y
	 *            The reference vector
	 * @param def
	 *            The default value to be use to avoid division by zero.
	 * @return The relative distance of x to y. If x is shorter than y both
	 *         arrays are swapped and this method returns the relative distance
	 *         of y to x.
	 * @throws Exception
	 */
	public double distance(double[] x, double[] y, double def) {
		if (x.length < y.length) {
			double swap[] = x;
			x = y;
			y = swap;
		}
		double d = 0;
		for (int i = 0; i < y.length; i++)
			if ((y[i] != Double.NaN) && (x[i] != Double.NaN) && (y[i] != x[i]))
				d += (y[i] != 0) ? Math.pow((x[i] - y[i]) / y[i], 2) : def;
		return d;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.squeezer.math.Distance#getName()
	 */
	@Override
	public String getName() {
		return "Relative Squared Error (RSE)";
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.squeezer.math.Distance#getStandardParameter()
	 */
	@Override
	public double getStandardParameter() {
		return 10000d;
	}

}
