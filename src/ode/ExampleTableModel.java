package ode;

import javax.swing.table.AbstractTableModel;
public class ExampleTableModel extends AbstractTableModel
{
	private Object[][] data;
    
	public ExampleTableModel(Object[][] tableData) {
    	final Object[][] dat = tableData;
    	data=dat;
	}
    
	public ExampleTableModel() {
		final Object[][] dat = {
                {new Integer (3), "1", "bl dc wdvd we", "e e f w w d f", "a+b=c-d^f" },
                {new Integer (4), "2", "bl dcd wvd  wdfwe", "e ew  f w w d f", "a+b=c-d^f" },
                {new Integer (2), "3", "bl dcvd wfdwe", "e e f w w w d f", "a+b=c-d^f" },
                {new Integer (5), "4", "bl dcwf vd we", "e e f w w w d f", "a+b=c-d^f" },
                {new Integer (7), "5", "bl dw  cvd we", "e e f w w  wd f", "a+b=c-d^f" },
                {new Integer (32), "6", "bl wdcvd we", "e e f w w d  ww w w w  wf", "a+b=c-d^f" },
                {new Integer (-5), "7", "bl  dcvd we", "e e f w w d w f", "a+b=c-d^f" },
                {new Integer (6), "8", "bld dcvd we", "e e f w w d w wf", "a+b=c-d^f" },
                {new Integer (-3), "9", "was a u c h im mer", "b l o e k", "a+b+c+v+f++" }
            };
		data = dat;
	}
    
    private String[] columnNames = {"#Reactants", "Nr.", "Species", "Parameters", "Kinetic Equation" };

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
