/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016 by the University of Tuebingen, Germany.
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
package org.sbml.squeezer.celldesigner;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;

/**
 * @author Andreas Dr&auml;ger
 * 
 * @since 1.0
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
              /*CellDesignerPlugin plugin = (CellDesignerPlugin)*/
              pluginClass.newInstance();
              
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
