package ode;


import java.io.*;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class SaveSettings {

	BufferedWriter out;

    public SaveSettings(){
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
    	d.showSaveDialog( null );
    	File file = d.getSelectedFile();
        try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getPath())));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}  
    	
    }

    public void write(String str){
        try {
            out.write( str );
            out.newLine();
            } 
        catch (IOException ex) {
                ex.printStackTrace();  
        } 
    }
    
    public void append(String str){
        try {
			out.write(str);
			 out.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
    } 
        
    public void close(){
        try {  
            out.close();
        } 
        catch (IOException ex) {
                ex.printStackTrace();  
        } 
    }	    
	    
}