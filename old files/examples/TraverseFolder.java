/*
 * $Id:  TraverseFolder.java 2:22:58 PM jpfeuffer$
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
package org.sbml.squeezer.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * @author Julianus Pfeuffer
 * 
 * @since 2.0
 */
public class TraverseFolder {
  
  private ArrayList<File> files = new ArrayList<File>();
  
  /**
   * Returns all files, that have been seen while traversing.
   * @return
   */
  public ArrayList<File> getFiles() {
    return files;
  }
  
  /**
   * Saves the files beyond a specified folder/file in this Object.
   * @param givenFile
   */
  public void traverse(File givenFile) throws FileNotFoundException {
    if (!givenFile.isHidden()) {
      if (givenFile.isDirectory()) {
        File filesOfFolder[] = givenFile.listFiles();
        for (File aFile : filesOfFolder) {
          traverse(aFile);
        }
        
      } else if (givenFile.isFile()) {
        files.add(givenFile);
      }
    }
  }
  
}
