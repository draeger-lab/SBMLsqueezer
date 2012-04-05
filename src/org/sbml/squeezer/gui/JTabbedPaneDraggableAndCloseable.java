/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;

/**
 * A JTabbedPane which has a close ('X') icon on each tab,
 * in addition the tabs are draggable
 * 
 * To add a tab, use the method addTab(String, Component)
 * 
 * To have an extra icon on each tab (e.g., like in JBuilder, showing the file
 * type) use the method addTab(String, Component, Icon). Only clicking the 'X'
 * closes the tab.
 * 
 * @author Sebastian Nagel
 * @since 1.4
 * @version $Rev$
 */
public class JTabbedPaneDraggableAndCloseable extends JTabbedPane implements DropTargetListener{
	
	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = -7618281593485131907L;
	
	/**
	 * 
	 */
	public static final transient ResourceBundle BASE = ResourceManager.getBundle("de.zbit.locales.BaseAction");
	
	private static final int LINEWIDTH = 3;
	private static final String NAME = "test";
	private final GhostGlassPane glassPane = new GhostGlassPane();
	private final Rectangle2D lineRect = new Rectangle2D.Double();
	private final Color lineColor= new Color(0, 100, 255);
	private int dragTabIndex = -1;
	private boolean hasGhost = true;

	/**
	 * 
	 */
	public JTabbedPaneDraggableAndCloseable() {
		super();
		
		final DragSourceListener dsl = new DragSourceListener() {
			/* (non-Javadoc)
			 * @see java.awt.dnd.DragSourceListener#dragEnter(java.awt.dnd.DragSourceDragEvent)
			 */
			public void dragEnter(DragSourceDragEvent e) {
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}
			
			/* (non-Javadoc)
			 * @see java.awt.dnd.DragSourceListener#dragExit(java.awt.dnd.DragSourceEvent)
			 */
			public void dragExit(DragSourceEvent e) {
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				lineRect.setRect(0,0,0,0);
				glassPane.setPoint(new Point(-1000,-1000));
				glassPane.repaint();
			}
			
			/* (non-Javadoc)
			 * @see java.awt.dnd.DragSourceListener#dragOver(java.awt.dnd.DragSourceDragEvent)
			 */
			public void dragOver(DragSourceDragEvent e) {
				//This method returns a Point indicating the cursor location in screen coordinates at the moment
				Point tabPt = e.getLocation();
				SwingUtilities.convertPointFromScreen(tabPt, JTabbedPaneDraggableAndCloseable.this);
				Point glassPt = e.getLocation();
				SwingUtilities.convertPointFromScreen(glassPt, glassPane);
				int targetIdx = getTargetTabIndex(glassPt);
				if (getTabAreaBound().contains(tabPt) && targetIdx>=0 &&
						targetIdx!=dragTabIndex && targetIdx!=dragTabIndex+1) {
					e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
				} else {
					e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				}
			}
			
			/* (non-Javadoc)
			 * @see java.awt.dnd.DragSourceListener#dragDropEnd(java.awt.dnd.DragSourceDropEvent)
			 */
			public void dragDropEnd(DragSourceDropEvent e) {
				lineRect.setRect(0, 0, 0, 0);
				dragTabIndex = -1;
				if (hasGhost()) {
					glassPane.setVisible(false);
					glassPane.setImage(null);
				}
			}
			
			public void dropActionChanged(DragSourceDragEvent e) {}
		};
		final Transferable t = new Transferable() {
			private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);
			
			/* (non-Javadoc)
			 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
			 */
			public Object getTransferData(DataFlavor flavor) {
				return JTabbedPaneDraggableAndCloseable.this;
			}
			
			/* (non-Javadoc)
			 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
			 */
			public DataFlavor[] getTransferDataFlavors() {
				DataFlavor[] f = new DataFlavor[1];
				f[0] = this.FLAVOR;
				return f;
			}
			
			/* (non-Javadoc)
			 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
			 */
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return flavor.getHumanPresentableName().equals(NAME);
			}

		};
		final DragGestureListener dgl = new DragGestureListener() {
			
			/* (non-Javadoc)
			 * @see java.awt.dnd.DragGestureListener#dragGestureRecognized(java.awt.dnd.DragGestureEvent)
			 */
			public void dragGestureRecognized(DragGestureEvent e) {
				Point tabPt = e.getDragOrigin();
				dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
				if (dragTabIndex<0) {
					return;
				}
				initGlassPane(e.getComponent(), e.getDragOrigin());
				try {
					e.startDrag(DragSource.DefaultMoveDrop, t, dsl);
				} catch(InvalidDnDOperationException idoe) {
					idoe.printStackTrace();
				}
			}

		};

		new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);
	}
	
	/**
	 * adds the close button and its MouseListener to the tab at index i
	 * @param i
	 */
	public void setTabComponentAt(int i) {
		setTabComponentAt(i, (Icon) null);
	}
	
	/**
	 * adds the close button and its MouseListener to the tab at index i
	 * @param i
	 * @param extraIcon 
	 */
	public void setTabComponentAt(int i, Icon extraIcon) {
		String title = getTitleAt(i);
		JPanel panel = new JPanel();
		panel.add(new JLabel(title));
		JLabel closeButton = new JLabel(new CloseIcon(extraIcon));
		closeButton.setToolTipText(BASE.getString("FILE_CLOSE").split(";")[1]);
		panel.setOpaque(false);
		closeButton.addMouseListener(new MouseListener() {
			/* (non-Javadoc)
			 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
			 */
			public void mouseReleased(MouseEvent e) {}
			
			/* (non-Javadoc)
			 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
			 */
			public void mousePressed(MouseEvent e) {}
			
			/* (non-Javadoc)
			 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
			 */
			public void mouseExited(MouseEvent e) {}
			
			/* (non-Javadoc)
			 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
			 */
			public void mouseEntered(MouseEvent e) {}
			
			/* (non-Javadoc)
			 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1
						&& e.getSource() instanceof Component) {
					int tabIndex = getTabIndexByComponent((Component) e.getSource());
					if (tabIndex >= 0) {
						JTabbedPaneDraggableAndCloseable.this.removeTabAt(tabIndex);
						for (ChangeListener cl : getChangeListeners())
							cl.stateChanged(new ChangeEvent(JTabbedPaneDraggableAndCloseable.this));
					}
				}
			}
		});
	
		panel.add(closeButton);
		setTabComponentAt(i, panel);
	}
	
	/**
	 * if a tab contains comp return its index, otherwise -1
	 * 
	 * @param comp
	 * @return
	 */
	public int getTabIndexByComponent(Component comp) {
		for (int i=0; i<this.getTabCount(); i++) {
			if (this.getTabComponentAt(i) instanceof JPanel) {
				JPanel panel = (JPanel) this.getTabComponentAt(i);
				for (Component c : panel.getComponents()) {
					if (c.equals(comp)) {
						return i;
					}
				}
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JTabbedPane#addTab(java.lang.String, java.awt.Component)
	 */
	@Override
	public void addTab(String title, Component component) {
		this.addTab(title, component, null);
	}

	/**
	 * 
	 * @param title
	 * @param component
	 * @param extraIcon
	 */
	private void addTab(String title, Component component, Icon extraIcon) {
		super.addTab(title, component);
		setTabComponentAt(getTabCount() - 1, extraIcon);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.dnd.DropTargetListener#dragEnter(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dragEnter(DropTargetDragEvent e) {
		if (isDragAcceptable(e)) {
			e.acceptDrag(e.getDropAction());
		}
		else {
			e.rejectDrag();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.dnd.DropTargetListener#dragOver(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dragOver(final DropTargetDragEvent e) {
		if (getTabPlacement()==JTabbedPane.TOP || getTabPlacement()==JTabbedPane.BOTTOM) {
			initTargetLeftRightLine(getTargetTabIndex(e.getLocation()));
		} else {
			initTargetTopBottomLine(getTargetTabIndex(e.getLocation()));
		}
		repaint();
		if (hasGhost()) {
			glassPane.setPoint(e.getLocation());
			glassPane.repaint();
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.dnd.DropTargetListener#drop(java.awt.dnd.DropTargetDropEvent)
	 */
	public void drop(DropTargetDropEvent e) {
		if (isDropAcceptable(e)) {
			convertTab(dragTabIndex, getTargetTabIndex(e.getLocation()));
			e.dropComplete(true);
		} else {
			e.dropComplete(false);
		}
		repaint();
	}
	
	/**
	 * 
	 * @param e
	 * @return
	 */
	public boolean isDragAcceptable(DropTargetDragEvent e) {
		Transferable t = e.getTransferable();
		if (t==null) {
			return false;
		}
		DataFlavor[] f = e.getCurrentDataFlavors();
		if (t.isDataFlavorSupported(f[0]) && dragTabIndex>=0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param e
	 * @return
	 */
	public boolean isDropAcceptable(DropTargetDropEvent e) {
		Transferable t = e.getTransferable();
		if (t==null) {
			return false;
		}
		DataFlavor[] f = t.getTransferDataFlavors();
		return (t.isDataFlavorSupported(f[0]) && dragTabIndex>=0);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.dnd.DropTargetListener#dragExit(java.awt.dnd.DropTargetEvent)
	 */
	public void dragExit(DropTargetEvent e) {
	}
	
	/* (non-Javadoc)
	 * @see java.awt.dnd.DropTargetListener#dropActionChanged(java.awt.dnd.DropTargetDragEvent)
	 */
	public void dropActionChanged(DropTargetDragEvent e) {
	}
	
	/**
	 * 
	 * @param flag
	 */
	public void setPaintGhost(boolean flag) {
		hasGhost = flag;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasGhost() {
		return hasGhost;
	}
	
	/**
	 * 
	 * @param glassPt
	 * @return
	 */
	private int getTargetTabIndex(Point glassPt) {
		Point tabPt = SwingUtilities.convertPoint(glassPane, glassPt, JTabbedPaneDraggableAndCloseable.this);
		boolean isTB = (getTabPlacement() == JTabbedPane.TOP) || (getTabPlacement() == JTabbedPane.BOTTOM);
		for(int i=0;i<getTabCount();i++) {
			Rectangle r = getBoundsAt(i);
			if (isTB) {
				r.setRect(r.x-r.width/2, r.y,r.width, r.height);
			} else {
				r.setRect(r.x, r.y-r.height/2, r.width, r.height);
			}
			if (r.contains(tabPt)) {
				return i;
			}
		}
		Rectangle r = getBoundsAt(getTabCount()-1);
		if (isTB) {
			r.setRect(r.x + r.width / 2, r.y,r.width, r.height);
		} else {
			r.setRect(r.x, r.y + r.height / 2, r.width, r.height);
		}
		return r.contains(tabPt)?getTabCount():-1;
	}
	
	/**
	 * 
	 * @param prev
	 * @param next
	 */
	private void convertTab(int prev, int next) {
		if ((next < 0) || (prev == next)) {
			return;
		}
		Component cmp = getComponentAt(prev);
		String str = getTitleAt(prev);
		if (next==getTabCount()) {
			remove(prev);
			addTab(str, cmp);
			setSelectedIndex(getTabCount()-1);
			setTabComponentAt(getTabCount()-1);
		} else if (prev>next) {
			remove(prev);
			insertTab(str, null, cmp, null, next);
			setSelectedIndex(next);
			setTabComponentAt(next);
		} else {
			remove(prev);
			insertTab(str, null, cmp, null, next-1);
			setSelectedIndex(next-1);
			setTabComponentAt(next-1);
		}
	}

	/**
	 * 
	 * @param next
	 */
	private void initTargetLeftRightLine(int next) {
		if ((next < 0) || (dragTabIndex == next) || (next - dragTabIndex == 1)) {
			lineRect.setRect(0, 0, 0, 0);
		} else if (next==getTabCount()) {
			Rectangle rect = getBoundsAt(getTabCount()-1);
			lineRect.setRect(rect.x+rect.width-LINEWIDTH/2,rect.y,LINEWIDTH,rect.height);
		} else if (next==0) {
			Rectangle rect = getBoundsAt(0);
			lineRect.setRect(-LINEWIDTH/2,rect.y,LINEWIDTH,rect.height);
		} else {
			Rectangle rect = getBoundsAt(next - 1);
			lineRect.setRect(rect.x+rect.width-LINEWIDTH/2,rect.y,LINEWIDTH,rect.height);
		}
	}
	
	/**
	 * 
	 * @param next
	 */
	private void initTargetTopBottomLine(int next) {
		if ((next < 0) || (dragTabIndex == next) || (next - dragTabIndex == 1)) {
			lineRect.setRect(0, 0, 0, 0);
		} else if (next == getTabCount()) {
			Rectangle rect = getBoundsAt(getTabCount()-1);
			lineRect.setRect(rect.x,rect.y+rect.height-LINEWIDTH/2,rect.width,LINEWIDTH);
		} else if (next == 0) {
			Rectangle rect = getBoundsAt(0);
			lineRect.setRect(rect.x,-LINEWIDTH / 2,rect.width,LINEWIDTH);
		} else {
			Rectangle rect = getBoundsAt(next - 1);
			lineRect.setRect(rect.x,rect.y+rect.height-LINEWIDTH/2,rect.width,LINEWIDTH);
		}
	}

	/**
	 * 
	 * @param c
	 * @param tabPt
	 */
	private void initGlassPane(Component c, Point tabPt) {
		getRootPane().setGlassPane(glassPane);
		if (hasGhost()) {
			Rectangle rect = getBoundsAt(dragTabIndex);
			BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.getGraphics();
			c.paint(g);
			image = image.getSubimage(rect.x,rect.y,rect.width,rect.height);
			glassPane.setImage(image);
		}
		Point glassPt = SwingUtilities.convertPoint(c, tabPt, glassPane);
		glassPane.setPoint(glassPt);
		glassPane.setVisible(true);
	}

	/**
	 * 
	 * @return
	 */
	private Rectangle getTabAreaBound() {
		Rectangle lastTab= getUI().getTabBounds(this, getTabCount()-1);
		return new Rectangle(0,0,getWidth(),lastTab.y+lastTab.height);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (dragTabIndex>=0) {
			Graphics2D g2 = (Graphics2D)g;
			g2.setPaint(lineColor);
			g2.fill(lineRect);
		}
	}
}

/**
 * 
 * @author Sebastian Nagel
 * @since 1.4
 */
class GhostGlassPane extends JPanel {
	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 6230514850602998964L;
	
	private final AlphaComposite composite;
	private Point location = new Point(0, 0);
	private BufferedImage draggingGhost = null;
	
	/**
	 * 
	 */
	public GhostGlassPane() {
		setOpaque(false);
		composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(Graphics g) {
		if (draggingGhost == null) {
			return;
		}
		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(composite);
		double xx = location.getX() - (draggingGhost.getWidth(this) /2d);
		double yy = location.getY() - (draggingGhost.getHeight(this)/2d);
		g2.drawImage(draggingGhost, (int) xx, (int) yy , null);
	}

	/**
	 * 
	 * @param draggingGhost
	 */
	public void setImage(BufferedImage draggingGhost) {
		this.draggingGhost = draggingGhost;
	}
	
	/**
	 * 
	 * @param location
	 */
	public void setPoint(Point location) {
		this.location = location;
	}

}
