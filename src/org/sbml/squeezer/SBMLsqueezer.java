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
package org.sbml.squeezer;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import javax.swing.JMenuItem;

import jp.sbi.celldesigner.plugin.PluginAction;
import jp.sbi.celldesigner.plugin.PluginReaction;

import org.sbml.SBO;
import org.sbml.squeezer.gui.KineticLawSelectionDialog;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.gui.UpdateMessage;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.plugin.PluginSBMLReader;
import org.sbml.squeezer.plugin.PluginSBMLWriter;
import org.sbml.squeezer.plugin.SBMLsqueezerPlugin;
import org.sbml.squeezer.resources.Resource;
import org.sbml.squeezer.standalone.LibSBMLReader;
import org.sbml.squeezer.standalone.LibSBMLWriter;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @since 1.3
 * @version
 */
public class SBMLsqueezer extends PluginAction {

	public Set<Integer> possibleEnzymes;

	static {
		try {
			properties = Resource.readProperties(Resource.class.getResource(
					"cfg/SBMLsqueezer.cfg").getPath());
		} catch (IOException e) {
			e.printStackTrace();
			properties = new Properties();
		}
	}

	/**
	 * The number of the current SBMLsqueezer version.
	 */
	private static final String versionNumber = "1.2.1";

	/**
	 * Configuration of SBMLsqueezer
	 */
	private static Properties properties;

	/**
	 * 
	 * @return
	 */
	public static Object getProperty(Object key) {
		return properties.get(key);
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static Object setProperty(Object key, Object value) {
		return properties.put(key, value);
	}

	/**
	 * 
	 */
	public static void saveProperties() {
		try {
			String resourceName = Resource.class.getResource(
					"cfg/SBMLsqueezer.cfg").getPath();
			Properties p = Resource.readProperties(resourceName);
			if (!p.equals(properties))
				Resource.writeProperties(properties, resourceName);
		} catch (IOException e) {
		}
	}

	/**
	 * 
	 * @return versionNumber
	 */
	public static final String getVersionNumber() {
		return versionNumber;
	}

	/**
	 * Possible command line options
	 * 
	 * @author Andreas Dr&auml;ger <a
	 *         href="mailto:andreas.draeger@uni-tuebingen.de"
	 *         >andreas.draeger@uni-tuebingen.de</a>
	 * 
	 */
	private enum Keys {
		/**
		 * Can be used in combination with = true or = false or just --gui.
		 */
		GUI,
		/**
		 * 
		 */
		SBML_FILE
	}

	/**
	 * A serial version number.
	 */
	private static final long serialVersionUID = 4134514954192751545L;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.loadLibrary("sbmlj");
		new SBMLsqueezer(args);
	}

	/**
	 * Displays a short copyright message on the screen.
	 */
	private static void showAboutMsg() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					Resource.class.getResource("html/about.htm").getFile()));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("<"))
					continue;
				line = line.replace("</a>", "");
				line = line.replace("&#169;", "\uf8e9");
				line = line.replace("&#228;", "\u00e4");
				line = line.replace("&#252;", "\u00fc");
				if (line.endsWith("<br>"))
					line = line.substring(0, line.length() - 4);
				if (line.contains("<a href"))
					System.out
							.print(line.substring(0, line.indexOf('<') - 1) + ' ');
				else
					System.out.println(line);
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}

	/**
	 * Tells if SBMLsqueezer checked for an update after CellDesigner has been
	 * run.
	 */
	private boolean isUpdateChecked = false;

	/**
	 * The CellDesigner plugin to which this action belongs.
	 */
	private SBMLsqueezerPlugin plugin;

	SBMLio sbmlIo;

	/**
	 * Initializes SBMLsqueezer as a CellDesigner plug-in
	 * 
	 * @param plugin
	 */
	public SBMLsqueezer(SBMLsqueezerPlugin plugin) {
		this.plugin = plugin;
		this.possibleEnzymes = SBO.getDefaultPossibleEnzymes();
		showAboutMsg();
	}

	/**
	 * Initializes SBMLsqueezer as a stand-alone program.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public SBMLsqueezer(String... args) {
		Properties p = analyzeCommandLineArguments(args);
		sbmlIo = new SBMLio(new LibSBMLReader(possibleEnzymes),
				new LibSBMLWriter());
		if (p.containsKey(Keys.SBML_FILE))
			sbmlIo.readModel(p.get(Keys.SBML_FILE));
		if (p.containsKey(Keys.GUI)
				&& Boolean.parseBoolean(p.get(Keys.GUI).toString())) {
			checkForUpdate(true);
			new Thread(new Runnable() {
				public void run() {
					showGUI();
				}
			}).start();
		} else {
			// Do a lot of other stuff...
			checkForUpdate(false);
			showAboutMsg();
		}
	}

	/**
	 * @return isUpdateChecked
	 */
	public boolean getUpdateChecked() {
		return isUpdateChecked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jp.sbi.celldesigner.plugin.PluginActionListener#myActionPerformed(java
	 * .awt.event.ActionEvent)
	 */
	/**
	 * Runs two threads for checking if an update for SBMLsqueezer is available
	 * and for initializing an instance of the SBMLsqueezerUI.
	 * 
	 * @param e
	 */
	public void myActionPerformed(ActionEvent e) {
		checkForUpdate(true);
		if (e.getSource() instanceof JMenuItem) {
			final String item = ((JMenuItem) e.getSource()).getText();
			new Thread(new Runnable() {
				public void run() {
					startSBMLsqueezerPlugin(item);
				}
			}).start();
		} else
			System.err.println("Unsupported source of action "
					+ e.getSource().getClass().getName());
	}

	/**
	 * Sets the value of isUpdateChecked to the value of b.
	 * 
	 * @param b
	 */
	public void setUpdateChecked(boolean b) {
		isUpdateChecked = b;
	}

	/**
	 * Shows the GUI of SBMLsqueezer stand-alone.
	 */
	public void showGUI() {
		SBMLsqueezerUI gui = new SBMLsqueezerUI(sbmlIo);
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
	}

	/**
	 * 
	 * @param args
	 * @return
	 */
	private Properties analyzeCommandLineArguments(String[] args) {
		Properties p = new Properties();
		for (String string : args) {
			while (string.startsWith("-"))
				string = string.substring(1);
			if (string.contains("=")) {
				String keyVal[] = string.split("=");
				p.put(Keys.valueOf(keyVal[0].toUpperCase().replace('-', '_')),
						keyVal[1]);
			} else
				p.put(Keys.valueOf(string.toUpperCase().replace('-', '_')),
						Boolean.TRUE.toString());
		}
		return p;
	}

	/**
	 * Let's see if there is a later version of SBMLsqueezer available.
	 * 
	 * @param gui
	 *            Decides whether or not the update message should appear in a
	 *            graphical mode (gui = true) or as a text on the console
	 *            otherwise.
	 */
	private void checkForUpdate(final boolean gui) {
		final SBMLsqueezer squeezer = this;
		new Thread(new Runnable() {
			public void run() {
				try {
					UpdateMessage.checkForUpdate(gui, squeezer);
				} catch (IOException exc) {
					// Don't annoy people
					// JOptionPane.showMessageDialog(squeezer,
					// exc.getMessage());
					// exc.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Starts the SBMLsqueezer dialog window for kinetic law selection or LaTeX
	 * export.
	 * 
	 * @param mode
	 */
	private void startSBMLsqueezerPlugin(String mode) {
		SBMLio sbmlIO = new SBMLio(new PluginSBMLReader(plugin
				.getSelectedModel(), possibleEnzymes), new PluginSBMLWriter(
				plugin));
		if (mode.equals(plugin.getMainPluginItemText()))
			(new KineticLawSelectionDialog(null, sbmlIO)).setVisible(true);
		else if (mode.equals(plugin.getSqueezeContextMenuItemText()))
			new KineticLawSelectionDialog(null, sbmlIO, sbmlIO
					.readReaction((PluginReaction) plugin
							.getSelectedReactionNode().get(0)));
		else if (mode.equals(plugin.getExportContextMenuItemText()))
			new KineticLawSelectionDialog(null, sbmlIO.getSelectedModel()
					.getReaction(
							((PluginReaction) plugin.getSelectedReactionNode()
									.get(0)).getId()));
		else if (mode.equals(plugin.getExporterItemText()))
			if (plugin.getSelectedModel() != null)
				new KineticLawSelectionDialog(null, sbmlIO.getSelectedModel());
	}

	/**
	 * 
	 * @return
	 */
	public Set<Integer> getPossibleEnzymes() {
		return possibleEnzymes;
	}
}
