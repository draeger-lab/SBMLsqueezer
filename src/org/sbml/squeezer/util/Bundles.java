package org.sbml.squeezer.util;

import java.util.ResourceBundle;

import de.zbit.util.ResourceManager;

public interface Bundles {
	/**
	 * 
	 */
	public static final ResourceBundle MESSAGES = ResourceManager.getBundle("org.sbml.squeezer.gui.locales.Messages");

	/**
	 * 
	 */
	public static final ResourceBundle OPTIONS = ResourceManager.getBundle("org.sbml.squeezer.gui.locales.Options");
	
	/**
	 * 
	 */
	public static final ResourceBundle WARNINGS = ResourceManager.getBundle("org.sbml.squeezer.gui.locales.Warnings");

	/**
	 * 
	 */
	public static final ResourceBundle LABELS = ResourceManager.getBundle("de.zbit.locales.Labels");
	
	/**
	 * 
	 */
	public static final ResourceBundle BASE = ResourceManager.getBundle("de.zbit.locales.BaseAction");
}
