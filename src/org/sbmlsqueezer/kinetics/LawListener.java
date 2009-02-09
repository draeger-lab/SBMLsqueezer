package org.sbmlsqueezer.kinetics;

public interface LawListener {

	/**
	 * Allows you to tell this listener which number (in a list or in an array
	 * or what ever) you are currently working with.
	 * 
	 * @param num
	 *            The current element.
	 */
	public void currentNumber(int num);

	/**
	 * Allows you to tell this listener the total number of elements to work
	 * with.
	 * 
	 * @param i
	 *            Number of elements.
	 */
	public void totalNumber(int i);

}
