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

import eva2.tools.math.Mathematics;

/**
 * This class contains a multitude of well defined mathematical functions like
 * the faculty, logarithms and several trigonometric functions.
 * 
 * @since 1.3
 * @version
 * @author Andreas Dr&auml;ger
 * @author Diedonn&eacute; Mosu Wouamba
 * @author Alexander D&ouml;rr
 * @date 2007-10-29
 **/
public final class Functions extends Mathematics {

	/**
	 * There shouldn't be any instances of this class.
	 */
	private Functions() {
	}

	/**
	 * This method computes the logarithm of a number x to a giving base b.
	 * 
	 * @param number
	 * @param base
	 * @return
	 */
	public static final double logarithm(double number, double base) {
		return Math.log(number) / Math.log(base);
	}

	/**
	 * This method computes the factorial! function.
	 * 
	 * @param n
	 * @return
	 */
	public static final double factorial(double n) {
		if ((n == 0) || (n == 1)) {
			return 1;
		}
		return n * factorial(n - 1);
	}

	/**
	 * This method computes the arccosh of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double arccosh(double n) {
		return Math.log(n + (Math.sqrt(Math.pow(n, 2) - 1)));
	}

	/**
	 * This method computes the arccot of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double arccot(double n) {
		if (n == 0) {
			throw new java.lang.ArithmeticException("arccot(0) undefined");
		}
		return Math.atan(1 / n);
	}

	/**
	 * This method computes the arccoth of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double arccoth(double n) {
		if (n == 0) {
			throw new java.lang.ArithmeticException("arccoth(0) undefined");
		}
		return (Math.log(1 + (1 / n)) - Math.log(1 - (1 / n))) / 2;
	}

	/**
	 * This method computes the arccsc of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double arccsc(double n) {
		if (Math.asin(n) == 0) {
			throw new java.lang.ArithmeticException("Arccsc undefined");
		}
		return 1 / (Math.asin(n));
	}

	/**
	 * This method computes the arccsch of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double arccsch(double n) {
		if (Math.asin(n) == 0) {
			throw new java.lang.ArithmeticException("arccsch(0) undefined");
		}
		return Math.log(1 / n + Math.sqrt(Math.pow(1 / n, 2) + 1));
	}

	/**
	 * This method computes the arcsec of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double arcsec(double n) {
		if (n == 0) {
			throw new java.lang.ArithmeticException("arcsec undefined");
		}
		return 1 / n;
	}

	/**
	 * This method computes the arcsech of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double arcsech(double n) {
		if (n == 0) {
			throw new java.lang.ArithmeticException("arcsech(0) undefined");
		}
		return Math.log((1 / n) + (Math.sqrt(Math.pow(1 / n, 2) - 1)));
	}

	/**
	 * This method computes the arcsinh of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double arcsinh(double n) {
		return Math.log(n + Math.sqrt(Math.pow(n, 2) + 1));
	}

	/**
	 * This method computes the arctanh of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double arctanh(double n) {
		return (Math.log(1 + n) - Math.log(1 - n)) / 2;
	}

	/**
	 * This method computes the cot of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double cot(double n) {
		if (Math.sin(n) == 0) {
			throw new java.lang.ArithmeticException("cot undefined");
		}
		return Math.cos(n) / Math.sin(n);
	}

	/**
	 * This method computes the coth of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double coth(double n) {
		if (Math.sinh(n) == 0) {
			throw new java.lang.ArithmeticException("coth undefined");
		}
		return Math.cosh(n) / Math.sinh(n);
	}

	/**
	 * This method computes the csc of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double csc(double n) {
		if (Math.sin(n) == 0) {
			throw new java.lang.ArithmeticException("Csc undefined");
		}
		return 1 / Math.sin(n);
	}

	/**
	 * This method computes the csch of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double csch(double n) {
		if (Math.sinh(n) == 0) {
			throw new java.lang.ArithmeticException(
					"Csch undefined for argument " + n);
		}
		return 1 / Math.sinh(n);
	}

	/**
	 * This method computes the ln of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double ln(double n) {
		return Math.log(n);
	}

	/**
	 * This method computes the log of n to the base 10
	 * 
	 * @param n
	 * @return
	 */
	public static final double log(double n) {
		return Math.log10(n);
	}

	/**
	 * This method computes the log of n to the base of m
	 * 
	 * @param n
	 * @param base
	 * @return
	 */
	public static final double log(double n, double base) {
		return Functions.logarithm(n, base);

	}

	/**
	 * This method computes the rootExponent-th root of the radiant
	 * 
	 * @param radiant
	 * @param rootExponent
	 * @return
	 */
	public static final double root(double radiant, double rootExponent) {
		if (rootExponent != 0) {
			return Math.pow(radiant, 1 / rootExponent);
		}
		throw new java.lang.ArithmeticException(
				"Division by zero: the root exponent must not be zero.");
	}

	/**
	 * This method computes the sec of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double sec(double n) {
		if (n == 0) {
			throw new java.lang.ArithmeticException("sec(0) undefined");
		}
		return 1 / n;
	}

	/**
	 * This method computes the sech of n
	 * 
	 * @param n
	 * @return
	 */
	public static final double sech(double n) {
		if (Math.cosh(n) == 0) {
			throw new java.lang.ArithmeticException(
					"Sech undefined for argument " + n);
		}
		return 1 / Math.cosh(n);
	}

}
