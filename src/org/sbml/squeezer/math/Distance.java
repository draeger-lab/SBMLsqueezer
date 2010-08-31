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

import java.util.Iterator;

import de.zbit.util.ArrayIterator;

import eva2.tools.math.des.Data;

/**
 * This class is the basis of various implementations of distance functions.
 * 
 * @since 1.4
 * @author Andreas Dr&auml;ger
 * @date 17.04.2007
 */
public abstract class Distance {

	/**
	 * The return value of the distance function in cases where the distance
	 * cannot be computed.
	 */
	protected double defaultValue;

	/**
	 * A value to express a parameter of the implementing class.
	 */
	protected double root;

	/**
	 * Default constructor. This sets the standard value for the parameter as
	 * given by the getStandardParameter() method. The default value is set to
	 * NaN.
	 */
	public Distance() {
		this.root = getDefaultRoot();
		this.defaultValue = Double.NaN;
	}

	/**
	 * Constructor, which allows setting the parameter value for root.
	 * 
	 * @param root
	 *            The parameter for this distance.
	 * @param defaultValue
	 */
	public Distance(double root, double defaultValue) {
		this.root = root;
		this.defaultValue = defaultValue;
	}

	/**
	 * The additive term to compute the distance when only two elements are
	 * given together with all default values.
	 * 
	 * @param x_i
	 * @param y_i
	 * @param root
	 * @param defaultValue
	 * @return
	 */
	abstract double additiveTerm(double x_i, double y_i, double root,
			double defaultValue);

	/**
	 * This method decides whether or not to consider the given values for the
	 * computation of a distance. This method checks if both arguments x_i and
	 * y_i are not {@link Double.NaN} and differ from each other. If other
	 * conditions should be checked, this method can be overridden.
	 * 
	 * @param x_i
	 * @param y_i
	 * @param root
	 * @param defaultValue
	 * @return True if the given values x_i and y_i are valid and should be
	 *         considered to compute the distance.
	 */
	boolean computeDistanceFor(double x_i, double y_i, double root,
			double defaultValue) {
		return !Double.isNaN(y_i) && !Double.isNaN(x_i) && (y_i != x_i);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public double distance(Data x, Data y) {
		if (x.getBlockCount() > y.getBlockCount()) {
			Data swap = y;
			y = x;
			x = swap;
		}
		double d = 0d;
		for (int i = 0; i < x.getBlockCount(); i++) {
			d += distance(x.getBlock(i), y.getBlock(i));
		}
		return overallDistance(d, getRoot(), getDefaultValue());
	}

	/**
	 * Computes the distance of two matrices as the sum of the distances of each
	 * row. It is possible that one matrix contains more columns than the other
	 * one. If so, the additional values in the bigger matrix are ignored and do
	 * not contribute to the distance. {@link Double.NaN} values do also not
	 * contribute to the distance. Only columns with matching identifiers are
	 * considered for the distance computation.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public double distance(Data.Block x, Data.Block y) {
		if (x.getColumnCount() > y.getColumnCount()) {
			Data.Block swap = y;
			y = x;
			x = swap;
		}
		double d = 0d;
		String identifiers[] = x.getIdentifiers();
		for (int i = 0; i < identifiers.length; i++) {
			d += distance(x.getColumn(i), y.getColumn(identifiers[i]));
		}
		return d;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public double distance(Double x[][], Double y[][]) {
		double d = 0;
		int j = 0;
		for (Double[] x_i : x) {
			d += distance(new ArrayIterator<Double>(x_i),
					new ArrayIterator<Double>(y[j++]));
		}
		return d;
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
	public double distance(Iterable<? extends Number> x,
			Iterable<? extends Number> y) {
		return distance(x, y, root, defaultValue);
	}

	/**
	 * Returns the distance of the two vectors x and y with the given root. This
	 * may be the root in a formal way or a default value to be returned if the
	 * distance uses a non defined operation. If one array is longer than the
	 * other one additional values do not contribute to the distance.
	 * {@link Double.NaN} values are also ignored.
	 * 
	 * @param x
	 *            an array
	 * @param y
	 *            another array
	 * @param root
	 *            Some necessary parameter.
	 * @param defaultValue
	 *            The value to be returned in cases in which no distance
	 *            computation is possible.
	 * @return The distance between the two arrays x and y.
	 * @throws IllegalArgumentException
	 */
	public double distance(Iterable<? extends Number> x,
			Iterable<? extends Number> y, double root, double defaultValue) {
		double d = 0;
		double x_i;
		double y_i;
		Iterator<? extends Number> yIterator = y.iterator();
		for (Number number : x) {
			if (!yIterator.hasNext()) {
				break;
			}
			x_i = number.doubleValue();
			y_i = yIterator.next().doubleValue();
			if (computeDistanceFor(x_i, y_i, root, defaultValue)) {
				d += additiveTerm(x_i, y_i, root, defaultValue);
			}
		}
		return overallDistance(d, root, defaultValue);
	}

	/**
	 * Computes the distance between two-dimensional {@link Iterable} elements.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public double distance(Iterable<Iterable<? extends Number>> x,
			Iterator<Iterable<? extends Number>> y) {
		double d = 0;
		for (Iterable<? extends Number> i : x) {
			d += distance(i, y.next());
		}
		return d;
	}

	/**
	 * Returns the default value for the parameter to compute the distance.
	 * 
	 * @return The root value of this {@link Distance} measure to be used if no
	 *         other value has been set.
	 */
	public abstract double getDefaultRoot();

	/**
	 * Returns the default value that is returned by the distance function in
	 * cases in which the computation of the distance is not possible.
	 * 
	 * @return
	 */
	public double getDefaultValue() {
		return defaultValue;
	}

	/**
	 * The name of this distance measurement.
	 * 
	 * @return A human-readable name representing the specific distance measure.
	 */
	public abstract String getName();

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
	 * This method allows to change the value of an already computed distance
	 * with the help of the given default values.
	 * 
	 * @param distance
	 * @param root
	 * @param defaultValue
	 * @return
	 */
	abstract double overallDistance(double distance, double root,
			double defaultValue);

	/**
	 * Set the value to be returned by the distance function in cases, in which
	 * no distance can be computed.
	 * 
	 * @param defaultValue
	 */
	public void setDefaultValue(double defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Set the current root to be used in the distance function to the specified
	 * value.
	 * 
	 * @param root
	 */
	public void setRoot(double root) {
		this.root = root;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s, root = %d, default = %d", getName(),
				getRoot(), getDefaultValue());
	}

}
