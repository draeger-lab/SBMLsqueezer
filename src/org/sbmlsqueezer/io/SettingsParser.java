package org.sbmlsqueezer.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class SettingsParser {

  private String dataDir;

  private String dataName;

  private BufferedReader in = null;

  /**
   *
   * @param parent
   *          The component that is the parent of the file chooser dialog window
   *          to be shown.
   */
  public SettingsParser(File file) {
      try {
        in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
  }

  public SettingsParser(String stringDataDir, String stringDataName) {
    this.dataDir = stringDataDir;
    this.dataName = stringDataName;
    try {
      in = new BufferedReader(new InputStreamReader(new FileInputStream(dataDir
          + dataName)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void close() throws IOException {
    if (in != null)
      in.close();
  }

  public String read() throws IOException {
    if (in != null)
     return in.readLine();
    return "";
  }
}
