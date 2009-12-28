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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * A helper class that provides several methods for working with a
 * {@link GridBagLayout}.
 * 
 * @since 1.0
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date 2005-07-29
 */
public class LayoutHelper {

	/**
	 * TODO
	 * 
	 * @param cont
	 * @param gbl
	 * @param c
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param weightx
	 * @param weighty
	 * @param ipadx
	 * @param ipady
	 */
	public static void addComponent(Container cont, GridBagLayout gbl,
			Component c, int x, int y, int width, int height, double weightx,
			double weighty, int ipadx, int ipady) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.ipadx = ipadx;
		gbc.ipady = ipady;
		gbl.setConstraints(c, gbc);
		cont.add(c);
	}

	/**
	 * 
	 * @param cont
	 * @param gbl
	 * @param c
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param weightx
	 * @param weighty
	 */
	public static void addComponent(Container cont, GridBagLayout gbl,
			Component c, int x, int y, int width, int height, double weightx,
			double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		addComponent(cont, gbl, c, x, y, width, height, weightx, weighty,
				gbc.ipadx, gbc.ipady);
	}

	private Container cont;
	private GridBagLayout gbl;
	private int row;

	/**
	 * 
	 * @param cont
	 * @param gbl
	 */
	public LayoutHelper(Container cont, GridBagLayout gbl) {
		this.cont = cont;
		this.gbl = gbl;
		this.cont.setLayout(this.gbl);
		this.row = 0;
	}

	/**
	 * Creates a new GridBaglayout and associates this with the given container.
	 * 
	 * @param cont
	 */
	public LayoutHelper(Container cont) {
		this.cont = cont;
		this.gbl = new GridBagLayout();
		this.cont.setLayout(gbl);
		this.row = 0;
	}

	/**
	 * 
	 * @param c
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param weightx
	 * @param weighty
	 */
	public void add(Component c, int x, int y, int width, int height,
			double weightx, double weighty) {
		LayoutHelper.addComponent(this.cont, this.gbl, c, x, y, width, height,
				weightx, weighty);
		row = y;
	}

	/**
	 * 
	 * @param c
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param weightx
	 * @param weighty
	 * @param ipadx
	 * @param ipady
	 */
	public void add(Component c, int x, int y, int width, int height,
			double weightx, double weighty, int ipadx, int ipady) {
		LayoutHelper.addComponent(this.cont, this.gbl, c, x, y, width, height,
				weightx, weighty, ipadx, ipady);
		row = y;
	}

	/**
	 * 
	 * @return
	 */
	public Container getContainer() {
		return this.cont;
	}

	/**
	 * adds this component in the next row.
	 * 
	 * @param c
	 */
	public void add(Component c) {
		add(c, 0, ++row, 1, 1, 1, 1);
	}

}
