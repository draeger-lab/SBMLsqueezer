/**
 * 
 */
package org.sbml.squeezer.test.gui;

import eva2.gui.Plot;

/**
 * @author draeger
 *
 */
public class PlotTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Plot p = new Plot("Test Plot", "X", "Y");
		int length = 100;
		for (int i=1; i<length; i++) {
			double x = i;
			x /= length;
			p.setConnectedPoint(x, Math.log(x), 1);
			p.setUnconnectedPoint(x, Math.sin(x), 2);
		}
		p.setInfoString(1, "Log", 1);
		p.setInfoString(2, "Log", 1);
		p.getFunctionArea().setShowLegend(true);
	}

}
