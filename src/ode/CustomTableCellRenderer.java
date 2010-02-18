package ode;

import java.awt.Component;
import java.awt.Color;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @date 01.08.2007
 */
public class CustomTableCellRenderer extends JTextArea implements TableCellRenderer{

	private int maxNumberOfSpecies;
	
	public CustomTableCellRenderer(int maxSpecies) {
		maxNumberOfSpecies=maxSpecies;
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		
		if (value instanceof String ){
			setText(value.toString());
			setBackground( Color.white );
		}
		if (value instanceof Integer){
			Integer amount = (Integer) value;
	        if( amount.intValue() > maxNumberOfSpecies-1 ){
	            setBackground( Color.red );
	            setText(value.toString());
	        }
	        else{
	           setBackground( Color.white );
	           setText(value.toString());
	        }
		}
	return this;
	}
}
