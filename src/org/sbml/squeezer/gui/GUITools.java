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
package org.sbml.squeezer.gui;

import java.awt.Image;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.sbml.squeezer.resources.Resource;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-04
 */
public class GUITools {

	public static Icon LATEX_ICON_TINY;
	public static Icon LEMON_ICON_TINY;
	public static Icon LEMON_ICON_SMALL;
	public static Icon RIGHT_ARROW;
	public static Icon DOWN_ARROW;
	public static Icon LOGO_SMALL;

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = 5662654607939013825L;

	static {
		try {
			LEMON_ICON_TINY = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/Lemon_tiny.png")));
			LATEX_ICON_TINY = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/SBML2LaTeX_vertical_tiny.png")));
			LEMON_ICON_SMALL = new ImageIcon(ImageIO.read(Resource.class
					.getResource("img/Lemon_small.png")));
			// .getScaledInstance(100, 100, Image.SCALE_SMOOTH));
			Image image = ImageIO.read(Resource.class
					.getResource("img/rightarrow.png"));
			RIGHT_ARROW = new ImageIcon(image.getScaledInstance(10, 10,
					Image.SCALE_SMOOTH));
			image = ImageIO.read(Resource.class
					.getResource("img/downarrow.png"));
			DOWN_ARROW = new ImageIcon(image.getScaledInstance(10, 10,
					Image.SCALE_SMOOTH));
			image = ImageIO.read(Resource.class
					.getResource("img/logo_small.png")); // title_small.jpg
			// image = image.getScaledInstance(490, 150, Image.SCALE_SMOOTH);
			LOGO_SMALL = new ImageIcon(image);
		} catch (IOException e) {
			LEMON_ICON_TINY = null;
			LATEX_ICON_TINY = null;
			LEMON_ICON_SMALL = null;
			RIGHT_ARROW = null;
			DOWN_ARROW = null;
			LOGO_SMALL = null;
			JOptionPane.showMessageDialog(null, "<html>" + e.getMessage()
					+ "</html>", e.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}
