/*
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 * Oct 29, 2007
 * Compiler: JDK 1.6.0
 */
package org.sbml.squeezer.math;


/**
 * This class contains a multitude of well defined mathematical functions
 * like the faculty, logarithms and several trigonometric functions.
 *
 * @since 1.3
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Diedonne Wouamba <dwouamba@yahoo.fr>
 * @date Oct 29, 2007
 **/
public final class Functions extends eva2.tools.Mathematics {

	/**
	 * There shouldn't be any instances of this class.
	 */
	private Functions() {}

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
    if ((n == 0) || (n == 1)) return 1;
    return n * factorial(n - 1);
  }


}
