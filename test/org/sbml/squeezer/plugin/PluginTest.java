/**
 * 
 */
package org.sbml.squeezer.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;

/**
 * @author draeger
 * 
 */
public class PluginTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new PluginTest();
	}

	public PluginTest() {
		try {
			createPluginMenu(new File("lib"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			createPluginMenu(new File("."));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * createPluginMenu
	 */
	private void createPluginMenu(File pluginFiles) {
		try {
			// listOfPlugin = new ArrayList();
			//
			// JMenu pluginMenu = new JMenu("Plugin");
			// mainMenuBar.add(pluginMenu, PLUGIN_MENU_INDEX);

			File[] files = pluginFiles.listFiles();

			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					File file = files[i];

					URL url = new URL("file", "", file.getAbsolutePath());
					URL[] urls = new URL[1];
					urls[0] = url;
					URLClassLoader classLoader = new URLClassLoader(urls);
					JarFile jar = null;
					try {
						jar = new JarFile(file.getAbsolutePath());
					} catch (Exception e) {
						continue;
					}

					boolean isSucceeded = false;
					Enumeration<JarEntry> entries = jar.entries();
					while (entries.hasMoreElements()) {
						// get the entry
						JarEntry jarEntry = entries.nextElement();
						String entry = jarEntry.toString();
						if (entry.endsWith("class")) {

							entry = entry.replaceAll("\\.class$", "");
							entry = entry.replaceAll("/", ".");

							if (!(isClassPlugin(classLoader, entry))) {
								continue;
							}
							Class<?> pluginClass = classLoader.loadClass(entry);
							CellDesignerPlugin plugin = (CellDesignerPlugin) pluginClass
									.newInstance();

							// add Plugin
							// listOfPlugin.add(plugin);

							isSucceeded = true;
						}
					}

					if (!isSucceeded) {
						System.out.println("*******load failed plugin = "
								+ jar.getName());
					}
				}
			}
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
	}

	/**
	 * Determines whether the class with a particular name extends
	 * AbstractPlugin.
	 * 
	 * @param classLoader
	 * @param name
	 *            the name of the putative plugin class
	 */
	private boolean isClassPlugin(ClassLoader classLoader, String name) {
		Class<?> c = null;
		try {
			// Modification
//			 System.out.println(name);
//			 Class<?> c1 = Class.forName(name);
			// End
			c = classLoader.loadClass(name);
		} catch (Throwable e) {
			System.out.println(name);
			e.printStackTrace();
			return false;
		}
		Class<?> p = CellDesignerPlugin.class;
		return p.isAssignableFrom(c);
	}
}
