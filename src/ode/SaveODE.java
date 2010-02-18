package ode;

import java.io.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class SaveODE {
    
    private BufferedWriter out;
    private String suffix=".txt";

    // Default constructor
    public SaveODE(){
    	JFileChooser d = new JFileChooser();
    	d.setFileFilter( new FileFilter(){ 
    		public boolean accept(File f){
    			return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt")|| f.getName().toLowerCase().endsWith(".tex");
    		}
    		public String getDescription(){		
    			return "*.txt;*.tex";
    		}
    	} );
    	
    	d.showSaveDialog( null );
    	File file = d.getSelectedFile();
        try {
			out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getPath())));
			
			if (file.getPath().endsWith(".txt"))
				suffix=".txt";
			else if (file.getPath().endsWith(".tex"))
				suffix=".tex";
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		}  
    	
    }

    public String getSuffix (){    	
    	return suffix;
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


