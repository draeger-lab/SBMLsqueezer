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

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * A Browser like Editor pane.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @link 
 *       http://www.galileocomputing.de/openbook/javainsel6/javainsel_14_016.htm#
 *       Xxx1001419
 */
public class JBrowser extends JEditorPane implements HyperlinkListener {

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

	/**
	 * A list that loggs all visited pages
	 */
	private LinkedList<URL> history;
	/**
	 * The current position within the list of visited web sites.
	 */
	private int currentPosition;

	/**
	 * @param url
	 * @throws MalformedURLException
	 */
	public JBrowser(String url) throws MalformedURLException {
		this(new URL(url));
	}

	/**
	 * 
	 * @param location
	 */
	public JBrowser(URL location) {
		super();
		setEditable(false);
		addHyperlinkListener(this);
		setBackground(Color.WHITE);
		try {
			setPage(location);
			history = new LinkedList<URL>();
			history.addLast(location);
			currentPosition = history.size() - 1;
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(this, exc.getMessage(), exc
					.getClass().getName(), JOptionPane.ERROR_MESSAGE);
			exc.printStackTrace();
		}
	}

	/**
	 * Goes one page back in history.
	 * 
	 * @return Returns false if we reached the first visited page. True
	 *         otherwise.
	 */
	public boolean back() {
		if (currentPosition > 0)
			visitPage(history.get(--currentPosition));
		if (currentPosition == 0)
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event
	 * .HyperlinkEvent)
	 */
	public void hyperlinkUpdate(HyperlinkEvent event) {
		HyperlinkEvent.EventType typ = event.getEventType();

		if (typ == HyperlinkEvent.EventType.ACTIVATED) {
			URL url = event.getURL();
			visitPage(url);
		}
	}

	/**
	 * Goes one page forward in history
	 * 
	 * @return Returns false if we reached the last visited page. True
	 *         otherwise.
	 */
	public boolean next() {
		if (currentPosition < history.size() - 1)
			visitPage(history.get(++currentPosition));
		if (currentPosition == history.size() - 1)
			return false;
		return true;
	}

	/**
	 * Sets the current page to be displayed to the given URL.
	 * 
	 * @param url
	 */
	private void visitPage(URL url) {
		try {
			setPage(url);
			if (!history.contains(url))
				history.add(++currentPosition, url);
			else
				currentPosition = history.indexOf(url);
		} catch (IOException exc) {
			JOptionPane.showMessageDialog(this, "Can't follow link to "
					+ url.toExternalForm(), exc.getClass().getName(),
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Returns the number of pages visited so far.
	 * 
	 * @return
	 */
	public int getNumPagesVisited() {
		return history.size();
	}

}
