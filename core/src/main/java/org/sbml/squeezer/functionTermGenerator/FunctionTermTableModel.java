package org.sbml.squeezer.functionTermGenerator;

import de.zbit.util.ResourceManager;
import de.zbit.util.progressbar.AbstractProgressBar;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.squeezer.util.Bundles;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Data model for the {@link FunctionTermTable} that determines, which information
 * is displayed there and it provides methods to access column or row names and
 * single elements in the table. (Aug 20, 21)
 *
 * Based on
 * @see org.sbml.squeezer.gui.KineticLawTableModel
 * by:
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 *
 * Adapted by:
 * @author Eike Pertuch
 *
 * @since 2.1.2
 *
 */
public class FunctionTermTableModel extends AbstractTableModel {

    /**
     *
     */
    public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);

    /**
     * Generated serial ID.
     */
    private static final long serialVersionUID = -5755507700427800869L;

    private Object[][] data;

    private String[] columnNames;

    /**
     *
     * @param ftg
     * @param progressBar
     */
    public FunctionTermTableModel(FunctionTermGenerator ftg, AbstractProgressBar progressBar) {

        int functionTermNum = 0;
        columnNames = new String[] {
                MESSAGES.getString("COL_TRANSITION"),
                MESSAGES.getString("COL_FUNCTION_TERM")
        };

        data = new Object[ftg.getCreatedFunctionTermsCount()][columnNames.length];
        ArrayList<Transition> modifiedTransitions = ftg.getModifiedTransitions();
        double startTime = System.currentTimeMillis();
        for(Transition t: modifiedTransitions) {

            data[functionTermNum][0] = t.getId();
            data[functionTermNum][1] = t.getListOfFunctionTerms().get(0).getMath().toFormula();

            // Notify progress listener:
            double percent = functionTermNum * 100d/ftg.getCreatedFunctionTermsCount();
            double remainingTime = 100 * ((System.currentTimeMillis() - startTime) / percent);
            progressBar.percentageChanged((int) Math.round(percent), remainingTime, MESSAGES.getString("FUNCTION_TERM_RESULTS_PROGRESS"));
            functionTermNum++;
        }
        progressBar.finished();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return data.length;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int column) {
        return data[row][column];
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return col == 1;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        data[rowIndex][columnIndex] = aValue;
        fireTableCellUpdated(rowIndex, columnIndex);
    }


}
