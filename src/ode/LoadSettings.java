package ode;

import java.io.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class LoadSettings {
    
    private String dataDir;
    private String dataName;
    BufferedReader in;
    
    public LoadSettings(String stringDataDir, String stringDataName) {
        dataDir = stringDataDir;
        dataName= stringDataName;
        try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(dataDir+dataName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}      
    }
    
    public LoadSettings(){
    	JFileChooser d = new JFileChooser();
    	d.setFileFilter( new FileFilter()
    	{ 
    		public boolean accept(File f){
    			return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
    		}
    		public String getDescription(){		
    			return "*.txt";
    		}
    	} );
    	d.showOpenDialog( null );
    	File file = d.getSelectedFile();
    	
        try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file.getPath())));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
    	
    }
      
    public void close(){
    	try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public String read(){
        try {
        	String s = in.readLine();
            return s;
        } 
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}