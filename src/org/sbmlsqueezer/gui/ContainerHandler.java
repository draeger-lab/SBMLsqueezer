package org.sbmlsqueezer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

public class ContainerHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1283934349784406816L;

	public static void setAllBackground(Container c, Color color) {
		Component children[] = c.getComponents();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof Container) {
				setAllBackground((Container) children[i], color);
			}
			children[i].setBackground(color);
		}
	}

	public static void setAllEnabled(Container c, boolean enabled) {
		Component children[] = c.getComponents();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof Container) {
				setAllEnabled((Container) children[i], enabled);
			}
			children[i].setEnabled(enabled);
		}
	}
}
