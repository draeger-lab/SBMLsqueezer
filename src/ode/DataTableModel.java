package ode;

import java.util.HashMap;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class DataTableModel extends AbstractTableModel
{
	private Object[][] data;
	private String[] columnNames = {"#Reactants", "ID", "Species", "Parameters", "Kinetic Equation" };
	
	public DataTableModel() {
	    	Object[][] dat = new Object[5][5];

			dat[0][0] =2;
			dat[0][1] ="a1";
			dat[0][2] ="a2";
			dat[0][3] ="a3";
			dat[0][4] ="a4";
			
			dat[1][0] =6;
			dat[1][1] ="b1";
			dat[1][2] ="b2";
			dat[1][3] ="bwefffffff\nrbsebs\nersrerefffffffffffffffffff3";
			dat[1][4] ="blök"; 
			
			dat[2][0] =2;
			dat[2][1] ="befwwwwwwww1";
			dat[2][2] ="bwefffffffffff2";
			dat[2][3] ="bweffffff\nfffffff3";
			dat[2][4] ="bfwefffffffffefwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwfffffffwef4";
			
			dat[3][0] =3;
			dat[3][1] ="wefffffffb1";
			dat[3][2] ="bwefff2";
			dat[3][3] ="bweffff3";
			dat[3][4] ="b4weffff";
			
			dat[4][0] =6;
			dat[4][1] ="bwefffff1";
			dat[4][2] ="bwefffff2";
			dat[4][3] ="beffffffffffffffffffffffffffff3";
			dat[4][4] ="bweffffffffffffff4";
			
			
			data = dat;
		}
        
    public DataTableModel(HashMap<String, String> allODE, HashMap<Integer, String> allSpecies) {
		
    	Object[][] dat = new Object[allSpecies.size()][5];

    	for(int i = 0; i < allSpecies.size(); i++){
    		    		
    		String species = allSpecies.get(i);
    		String kinetic = allODE.get(allSpecies.get(i));   		
    		String id	   = allSpecies.get(i);
    		String param   = allODE.get(allSpecies.get(i)); 
    		String numReac = allSpecies.get(i);
    		
    		dat [i][0]=numReac;    		
     		dat [i][1]=id;    		
     		dat [i][2]=species;    		
     		dat [i][3]=param;    		
     		dat [i][4]=kinetic;    		

    	}
    	data=dat;
	}

	public DataTableModel(
						HashMap<Integer, String> reactionNumAndKinetic, 
						List<Integer> reacNumOfNotExistKinetics, 
						HashMap<Integer, List<String>> reactionNumAndParameters, 
						HashMap<Integer, String> idOfReaction, 
						HashMap<Integer, Integer> numOfreactants, 
						HashMap<Integer, List<String>> reactantsOfReaction, 
						HashMap<Integer, List<String>> productsOfReaction,
						HashMap<Integer, String> reactionNumAndKineticBezeichnung
						){
		Object[][] dat = new Object[reacNumOfNotExistKinetics.size()][5];
		for(int i = 0; i < reacNumOfNotExistKinetics.size(); i++){
			String kineticBezeich	= reactionNumAndKineticBezeichnung.get(reacNumOfNotExistKinetics.get(i));
    		List<String> reactants 	= reactantsOfReaction.get(reacNumOfNotExistKinetics.get(i));
    		List<String> products 	= productsOfReaction.get(reacNumOfNotExistKinetics.get(i));;
    		String kinetic 			= kineticBezeich + ":\n" + reactionNumAndKinetic.get(reacNumOfNotExistKinetics.get(i)); 
    		String id	   			= idOfReaction.get(reacNumOfNotExistKinetics.get(i));
    		List<String> param   	= reactionNumAndParameters.get(reacNumOfNotExistKinetics.get(i)); 
    		int numReac = numOfreactants.get(reacNumOfNotExistKinetics.get(i));
    		String reac = "R:\n";
    		String pro = "P:\n";
    		String para = "";
    		String species;
    		
    		for(int j = 0; j < reactants.size(); j++)
    		{
    			if(j < (reactants.size()-1))
    				reac = reac + reactants.get(j) + ", ";
    			else
    				reac = reac + reactants.get(j) + "\n";
    		}
    		for(int j = 0; j < products.size(); j++)
    		{
    			if(j < (products.size()-1))
    				pro = pro + products.get(j) + ", ";
    			else
    				pro = pro + products.get(j);	
    		}
    		species = reac + pro;
    		for(int j = 0; j < param.size(); j++)
    		{
    			if(j < (param.size()-1))
    				para = para + param.get(j) + "\n";
    			else
    				para = para + param.get(j);	
    		}

    		dat [i][0]=numReac;    		
     		dat [i][1]=id;    		
     		dat [i][2]=species;    		
     		dat [i][3]=para;    		
     		dat [i][4]=kinetic;    		
    	}
    	data=dat;	
	}


	public Class getColumnClass( int column ) 
    {
        return getValueAt(0, column).getClass();
    }
	
    public int getColumnCount() 
    {
        return columnNames.length;
    }
    
    public String getColumnName( int column ) 
    {
        return columnNames[column];
    }
    
    public int getRowCount() 
    {
        return data.length;
    }
    
    public Object getValueAt( int row, int column ) 
    {
        return data[row][column];
    }
}
