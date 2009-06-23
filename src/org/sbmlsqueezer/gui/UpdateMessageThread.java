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
package org.sbmlsqueezer.gui;

import java.io.IOException;

import javax.swing.JOptionPane;

/**
 * 
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @since 1.2
 */

public class UpdateMessageThread extends Thread {

	private SBMLsqueezerPlugin plugin;

	public UpdateMessageThread(SBMLsqueezerPlugin plugin) {
		super();
		this.plugin = plugin;
	}

	// @Override
	public void run() {
		if (!plugin.getUpdateChecked())
			try {
				UpdateMessage.checkForUpdate(plugin);
			} catch (IOException exc) {
				JOptionPane.showMessageDialog(null, exc.getMessage());
				exc.printStackTrace();
			}
	}
}
