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
 * division by zero. Actually, the exponent in this error function is 2 (squared
 * error). Irrespectively, it is possible to set the exponent to different
 * values.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2007-04-17
 */
public class RSE extends Distance {

	/**
	 * Constructs a new RelativeSquaredError with a default root of 10,000. Here
	 * the root is the default value to be returned by the distance function.
	 */
	public RSE() {
		this(1E4d);
	}

	/**
	 * Constructs a new relative squared error object with the specified default
	 * value do avoid division by zero. The standard value is 10,000.
	 * 
	 * @param standard
	 */
	public RSE(double standard) {
		super(2d, standard);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.math.Distance#additiveTerm(double, double, double,
	 * double)
	 */
	@Override
	double additiveTerm(double x, double y, double root, double defaultValue) {
		return (y != 0d) ? Math.pow((x - y) / y, root) : defaultValue;
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
		String name = "Relative Squared Error";
		if (getRoot() != 2d) {
			name += ", exponent = " + getRoot();
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.math.Distance#overallDistance(double, double,
	 * double)
	 */
	@Override
	double overallDistance(double distance, double root, double defaultValue) {
		return distance;
	}

}
