package org.sbml.squeezer.math;

import java.util.ArrayList;
import java.util.Random;

public class RNG extends Random {
	/**
	 * Generated serial version uid.
	 */
	private static final long serialVersionUID = -5720896380268385416L;
	private static Random random;
	private static long randomSeed;
	/**
   *
   */
	static {
		randomSeed = System.currentTimeMillis();
		random = new Random(randomSeed);
	}

	/**
  *
  */
	public static void setRandomSeed(long new_seed) {
		// counter++;
		randomSeed = new_seed;
		if (randomSeed == 0)
			setRandomSeed();
		else
			random.setSeed(randomSeed);
	}

	/**
	 * Set the random seed without replacing zero with current system time.
	 */
	public static void setRandomSeedStrict(long new_seed) {
		randomSeed = new_seed;
		random.setSeed(randomSeed);
	}

	/**
   *
   */
	public static void setRandomSeed() {
		randomSeed = System.currentTimeMillis();
		random = new Random(randomSeed);
	}

	/**
   *
   */
	public static void setRandom(Random base_random) {
		random = base_random;
	}

	/**
   *
   */
	public static long getRandomSeed() {
		return randomSeed;
	}

	/**
	 * Returns 0 or 1 evenly distributed.
	 */
	public static int randomInt() {
		return randomInt(0, 1);
	}

	/**
	 * Returns an evenly distributes int value between zero and upperLim-1.
	 * 
	 * @param upperLim
	 *            upper exclusive limit of the random int
	 */
	public static int randomInt(int upperLim) {
		return randomInt(0, upperLim - 1);
	}

	/**
	 * This method returns a evenly distributed int value. The boundarys are
	 * included.
	 * 
	 * @param lo
	 *            Lower bound.
	 * @param hi
	 *            Upper bound.
	 * @return int
	 */
	public static int randomInt(int lo, int hi) {
		if (hi < lo) {
			System.err.println("Invalid boundary values! Returning zero.");
			return -1;
		}
		int result = (Math.abs(random.nextInt()) % (hi - lo + 1)) + lo;
		if ((result < lo) || (result > hi)) {
			System.err.println("Error in RNG.randomInt!");
			result = Math.abs(random.nextInt() % (hi - lo + 1)) + lo;
		}
		return result;
	}

	/**
	 * This method returns a random permutation of n int values
	 * 
	 * @param length
	 *            The number of int values
	 * @return The permutation [0-length-1]
	 */
	public static int[] randomPermutation(int length) {
		boolean[] validList = new boolean[length];
		int[] result = new int[length];
		int index;
		for (int i = 0; i < validList.length; i++)
			validList[i] = true;
		for (int i = 0; i < result.length; i++) {
			index = randomInt(0, length - 1);
			while (!validList[index]) {
				index++;
				if (index == length)
					index = 0;
			}
			validList[index] = false;
			result[i] = index;
		}
		return result;
	}

	/**
	 * This method returns a random permutation of n int values
	 * 
	 * @param length
	 *            The number of int values
	 * @return The permutation [0-length-1]
	 */
	public static int[] randomPerm(int length) {
		ArrayList<Integer> intList = new ArrayList<Integer>(length);
		int[] result = new int[length];
		for (int i = 0; i < length; i++) {
			intList.add(new Integer(i));
		}
		for (int i = 0; i < length - 1; i++) {
			int index = randomInt(intList.size());
			result[i] = intList.get(index);
			intList.remove(index);

		}
		if (intList.size() > 1)
			System.err.println("Error in randomPerm!");
		result[length - 1] = intList.get(0);
		return result;
	}

	/**
   *
   */
	public static long randomLong() {
		return randomLong(0, 1);
	}

	/**
   *
   */
	public static long randomLong(long lo, long hi) {
		return (Math.abs(random.nextLong()) % (hi - lo + 1)) + lo;
	}

	/**
   *
   */
	public static float randomFloat() {
		return random.nextFloat();
	}

	/**
   *
   */
	public static float randomFloat(float lo, float hi) {
		return (hi - lo) * random.nextFloat() + lo;
	}

	/**
	 * A random double value between 0 and 1.
	 */
	public static double randomDouble() {
		return random.nextDouble();
	}

	/**
   *
   */
	public static double randomDouble(double lo, double hi) {
		return (hi - lo) * random.nextDouble() + lo;
	}

	/**
	 * Create a uniform random vector within the given bounds.
	 */
	public static double[] randomDoubleArray(double[] lo, double[] hi) {
		double[] xin = new double[lo.length];
		for (int i = 0; i < lo.length; i++)
			xin[i] = (hi[i] - lo[i]) * random.nextDouble() + lo[i];
		return xin;
	}

	/**
	 * Create a uniform random vector within the given bounds.
	 */
	public static double[] randomDoubleArray(double[][] range) {
		double[] xin = new double[range.length];
		for (int i = 0; i < xin.length; i++)
			xin[i] = (range[i][1] - range[i][0]) * random.nextDouble()
					+ range[i][0];
		return xin;
	}

	/**
	 * Create a uniform random double vector within the given bounds (inclusive)
	 * in every dimension.
	 * 
	 * @param lower
	 * @param upper
	 * @param size
	 * @return
	 */
	public static double[] randomDoubleArray(double lower, double upper,
			int size) {
		double[] result = new double[size];
		for (int i = 0; i < result.length; i++) {
			result[i] = RNG.randomDouble(lower, upper);
		}
		return result;
		// double[] xin = new double[size];
		// for (int i=0;i<size;i++)
		// xin[i] = (hi-lo)*random.nextDouble()+lo;
		// return xin;
	}

	/**
   *
   */
	public static double[] randomDoubleArray(double[] lo, double[] hi,
			double[] xin) {
		// counter++;
		for (int i = 0; i < lo.length; i++)
			xin[i] = (hi[i] - lo[i]) * random.nextDouble() + lo[i];
		return xin;
	}

	/**
	 * Create a uniform random integer vector within the given bounds
	 * (inclusive) in every dimension.
	 * 
	 * @param n
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static int[] randomIntArray(int lower, int upper, int size) {
		int[] result = new int[size];
		for (int i = 0; i < result.length; i++) {
			result[i] = RNG.randomInt(lower, upper);
		}
		return result;
	}

	/**
   *
   */
	public static boolean randomBoolean() {
		// counter++;
		return (randomInt() == 1);
	}

	/**
   *
   */
	public static int randomBit() {
		// counter++;
		return randomInt();
	}

	/**
	 * Returns true with probability p.
	 * 
	 * @param p
	 * @return true with probability p, else false
	 */
	public static boolean flipCoin(double p) {
		// counter++;
		return (randomDouble() < p ? true : false);
	}

	/**
   *
   */
	public static float gaussianFloat(float dev) {
		// counter++;
		return (float) random.nextGaussian() * dev;
	}

	/**
	 * Return a Gaussian double with mean 0 and deviation dev.
	 * 
	 * @param dev
	 *            the deviation of the distribution.
	 * @return a Gaussian double with mean 0 and given deviation.
	 */
	public static double gaussianDouble(double dev) {
		// counter++;
		return random.nextGaussian() * dev;
	}

	/**
   *
   */
	public static float exponentialFloat(float mean) {
		// counter++;
		return (float) (-mean * Math.log(randomDouble()));
	}

	/**
   *
   */
	public static double exponentialDouble(double mean) {
		// counter++;
		return -mean * Math.log(randomDouble());
	}

	/**
	 * Returns a vector denoting a random point around the center - inside a
	 * hypersphere of uniform distribution if nonUnif=0, - inside a hypersphere
	 * of non-uniform distribution if nonUnif > 0, - inside a D-Gaussian if
	 * nonUnif < 0. For case 2, the nonUnif parameter is used as standard
	 * deviation (instead of 1/D), the parameter is not further used in the
	 * other two cases. Original code by Maurice Clerc, from the TRIBES package
	 * 
	 * @param center
	 *            center point of the distribution
	 * @param radius
	 *            radius of the distribution
	 * @param nonUnif
	 *            kind of distribution
	 * 
	 **/
	public static double[] randHypersphere(double[] center, double radius,
			double nonUnif) {
		double[] x = new double[center.length];
		int j;
		double xLen, r;
		int D = center.length;

		// ----------------------------------- Step 1. Direction
		xLen = 0;
		for (j = 0; j < D; j++) {
			r = gaussianDouble(1);
			x[j] = r;
			xLen += x[j] * x[j];
		}

		xLen = Math.sqrt(xLen);

		// ----------------------------------- Step 2. Random radius

		r = randomDouble();
		if (nonUnif < 0)
			r = gaussianDouble(r / 2); // D-Gaussian
		else if (nonUnif > 0)
			r = Math.pow(r, nonUnif); // non-uniform hypersphere
		else
			r = Math.pow(r, 1. / D); // Real hypersphere

		for (j = 0; j < D; j++) {
			x[j] = center[j] + radius * r * x[j] / xLen;
		}
		return x;
	}

	/**
	 * Adds Gaussian noise to a double vector
	 * 
	 * @param v
	 *            the double vector
	 * @param dev
	 *            the Gaussian deviation
	 */
	public static void addNoise(double[] v, double dev) {
		for (int i = 0; i < v.length; i++) {
			// add noise to the value
			v[i] += gaussianDouble(dev);
		}
	}

}
