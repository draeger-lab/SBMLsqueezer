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
package org.sbml.squeezer;

import java.io.IOException;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.sun.xml.internal.bind.v2.runtime.property.Property;

/**
 * This class manages storing and retriving a configuration of the user.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-21
 * 
 */
public class Configuration {

	/**
	 * An optional comment for the configuration file.
	 */
	private String commentCfgFile = null;

	/**
	 * Path to the configuration file with default properties.
	 */
	private String defaultsCfgFile;

	/**
	 * 
	 */
	private Class<? extends Enum> keys;

	/**
	 * A hash table of key value pairs representing the user's program
	 * configuration.
	 */
	private Properties properties = null;

	/**
	 * The root node for the user's preferences.
	 */
	private String userRootNode;

	/**
	 * 
	 * @param <T>
	 * @param keys
	 * @param userRootNode
	 * @param defaultsCfgFile
	 */
	public <T extends Enum<?>> Configuration(Class<T> keys,
			String userRootNode, String defaultsCfgFile) {
		this.keys = keys;
		this.userRootNode = userRootNode;
		this.defaultsCfgFile = defaultsCfgFile;
	}

	/**
	 * 
	 * @param args
	 * @return
	 */
	public Properties analyzeCommandLineArguments(String[] args) {
		Properties p = new Properties();
		for (String string : args) {
			while (string.startsWith("-")) {
				string = string.substring(1);
			}
			Object value = Boolean.TRUE;
			if (string.contains("=")) {
				String keyVal[] = string.split("=");
				if (keyVal.length > 2) {
					throw new IllegalArgumentException(
							"Key-value pair must contain exactly one '=' symbol.");
				}
				string = keyVal[0];
				value = keyVal[1];
			}
			string = string.toUpperCase().replace('-', '_');
			p.put(Enum.valueOf(keys, string), value);
		}
		return correctProperties(p);
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 * @throws BackingStoreException
	 */
	public Properties convert(Preferences prefs) throws BackingStoreException {
		Properties p = new Properties();
		for (String key : prefs.keys()) {
			p.put(Enum.valueOf(keys, key), prefs.get(key, "null"));
		}
		return correctProperties(p);
	}

	/**
	 * Creates an instance of the given properties, in which all keys are
	 * literals from the configuration enum and all values are objects such as
	 * Boolean, Integer, Double, Kinetics and so on.
	 * 
	 * @param properties
	 * @return
	 */
	public Properties correctProperties(Properties properties) {
		Object k[] = properties.keySet().toArray();
		Properties props = new Properties();
		for (int i = k.length - 1; i >= 0; i--) {
			String val = properties.get(k[i]).toString();

			if (val.startsWith("user.")) {
				props.put(Enum.valueOf(keys, k[i].toString()), System
						.getProperty(val));
			} else if (val.equalsIgnoreCase("true")
					|| val.equalsIgnoreCase("false")) {
				props.put(Enum.valueOf(keys, k[i].toString()), Boolean
						.parseBoolean(val));
			} else {
				try {
					props.put(Enum.valueOf(keys, k[i].toString()), Integer
							.valueOf(val));
				} catch (NumberFormatException e1) {
					try {
						props.put(Enum.valueOf(keys, k[i].toString()), Float
								.valueOf(val));
					} catch (NumberFormatException e2) {
						try {
							props.put(Enum.valueOf(keys, k[i].toString()),
									Double.valueOf(val));
						} catch (NumberFormatException e3) {
							if (val.length() == 1) {
								props.put(Enum.valueOf(keys, k[i].toString()),
										Character.valueOf(val.charAt(0)));
							} else {
								props.put(Enum.valueOf(keys, k[i].toString()),
										val);
							}
						}
					}
				}
			}
		}
		return props;
	}

	/**
	 * @return the commentCfgFile
	 */
	public String getCommentCfgFile() {
		return commentCfgFile;
	}

	/**
	 * Reads the default configuration file and returns a properties hash map
	 * that contains pairs of configuration keys and the entries from the file.
	 * 
	 * @return
	 */
	public Properties getDefaultProperties() {
		Properties defaults = new Properties();
		try {
			defaults.loadFromXML(defaults.getClass().getResourceAsStream(
					defaultsCfgFile));
		} catch (Exception exc) {
			try {
				defaults.load(defaults.getClass().getResourceAsStream(
						defaultsCfgFile));
				// defaults = Resource.readProperties(defaultsCfgFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return correctProperties(defaults);
	}

	/**
	 * 
	 * @return
	 */
	public String getDefaultsCfgFile() {
		return defaultsCfgFile;
	}

	/**
	 * 
	 * @return
	 */
	public Properties getProperties() {
		if (properties == null) {
			try {
				initProperties();
			} catch (BackingStoreException e) {
				properties = new Properties();
			}
		}
		return properties;
	}

	/**
	 * Returns the property associated to this enum element.
	 * 
	 * @return
	 */
	public Object getProperty() {
		return properties.get(this);
	}

	/**
	 * 
	 * @return
	 */
	public String getUserPrefNode() {
		return userRootNode;
	}

	/**
	 * 
	 * @return
	 * @throws BackingStoreException
	 */
	public Properties initProperties() throws BackingStoreException {
		Preferences prefs = Preferences.userRoot().node(userRootNode);
		Properties defaults = getDefaultProperties();
		boolean change = false;
		for (Object k : defaults.keySet()) {
			Object v = defaults.get(k);
			if (prefs.get(k.toString(), "null").equals("null")) {
				put(prefs, k, v);
				change = true;
			}
		}
		if (change) {
			prefs.flush();
		}
		Properties props = convert(prefs);
		properties = props;
		return properties;
	}

	/**
	 * 
	 * @param prefs
	 * @param k
	 * @param v
	 */
	private void put(Preferences prefs, Object k, Object v) {
		if (v instanceof Boolean) {
			prefs.putBoolean(k.toString(), ((Boolean) v).booleanValue());
		} else if (v instanceof Double) {
			prefs.putDouble(k.toString(), ((Double) v).doubleValue());
		} else if (v instanceof Float) {
			prefs.putFloat(k.toString(), ((Float) v).floatValue());
		} else if (v instanceof Integer) {
			prefs.putInt(k.toString(), ((Integer) v).intValue());
		} else if (v instanceof Long) {
			prefs.putLong(k.toString(), ((Long) v).longValue());
		} else {
			prefs.put(k.toString(), v.toString());
		}
	}

	/**
	 * 
	 * @param value
	 *            The new {@link Property} value for this key.
	 * @return the previous value belonging to this key or null if there was no
	 *         other value.
	 */
	public Object putProperty(Object value) {
		return properties.put(this, value);
	}

	/**
	 * Saves the currently valid {@link Properties}.
	 * 
	 * @see #saveProperties(Properties)
	 * @throws BackingStoreException
	 */
	public void saveProperties() throws BackingStoreException {
		saveProperties(properties);
	}

	/**
	 * 
	 * @param props
	 * @throws BackingStoreException
	 */
	public void saveProperties(Properties props) throws BackingStoreException {
		if (!initProperties().equals(props)) {
			Preferences pref = Preferences.userRoot().node(userRootNode);
			for (Object key : props.keySet()) {
				put(pref, key, props.get(key));
			}
			properties = props;
			pref.flush();
		}
	}

	/**
	 * @param commentCfgFile
	 *            the commentCfgFile to set
	 */
	public void setCommentCfgFile(String commentCfgFile) {
		this.commentCfgFile = commentCfgFile;
	}

	/**
	 * 
	 * @param defaultsCfgFile
	 */
	public void setDefaultsCfgFile(String defaultsCfgFile) {
		this.defaultsCfgFile = defaultsCfgFile;
	}

	/**
	 * 
	 * @param usrPrefNode
	 */
	public void setUserPrefNode(String usrPrefNode) {
		userRootNode = usrPrefNode;
	}

}
