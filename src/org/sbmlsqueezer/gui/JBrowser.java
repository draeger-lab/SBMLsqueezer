/**
 * Jul 17, 2007
 * 
 * @since 2.0
 * @author Andreas Dr&auml;ger (draeger) Copyright (c) ZBiT, University of
 *         T&uuml;bingen, Germany Compiler: JDK 1.6.0
 */
package org.sbmlsqueezer.gui;

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
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) Copyright (c) ZBiT, University of
 *         T&uuml;bingen, Germany Compiler: JDK 1.6.0 Jul 17, 2007
 * @link http://www.galileocomputing.de/openbook/javainsel6/javainsel_14_016.htm#Xxx1001419
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
    setEditable(false);
    addHyperlinkListener(this);
    setBackground(Color.WHITE);
    try {
      setPage(location);
      history = new LinkedList<URL>();
      history.addLast(location);
      currentPosition = history.size() - 1;
    } catch (IOException exc) {
      JOptionPane.showMessageDialog(this, exc.getMessage(), exc.getClass()
          .getName(), JOptionPane.ERROR_MESSAGE);
      exc.printStackTrace();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.HyperlinkListener#hyperlinkUpdate(javax.swing.event.HyperlinkEvent)
   */
  public void hyperlinkUpdate(HyperlinkEvent event) {
    HyperlinkEvent.EventType typ = event.getEventType();

    if (typ == HyperlinkEvent.EventType.ACTIVATED) {
      URL url = event.getURL();
      visitPage(url);
    }
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
      else currentPosition = history.indexOf(url);
    } catch (IOException exc) {
      JOptionPane.showMessageDialog(this, "Can't follow link to "
          + url.toExternalForm(), exc.getClass().getName(),
          JOptionPane.ERROR_MESSAGE);
    }    
  }

  /**
   * Goes one page back in history.
   * @return Returns false if we reached the first visited page. True otherwise. 
   */
  public boolean back() {
    if (currentPosition > 0) 
      visitPage(history.get(--currentPosition));
    if (currentPosition == 0)
      return false;
    return true;
  }
  
  /**
   * Goes one page forward in history
   * @return Returns false if we reached the last visited page. True otherwise.
   */
  public boolean next() {
    if (currentPosition < history.size() - 1)
      visitPage(history.get(++currentPosition));
    if (currentPosition == history.size() - 1)
      return false;
    return true;
  }

}
