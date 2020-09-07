package org.sbml.squeezer.functionTermGenerator;

import de.zbit.gui.ColorPalette;
import org.sbml.jsbml.util.StringTools;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * A renderer that paints the background of every second row white or in light
 * blue. (Aug 20, 2020)
 *
 * Based on:
 * @see org.sbml.squeezer.gui.KineticLawTableCellRenderer
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
public class FunctionTermTableCellRenderer extends JTextArea implements TableCellRenderer {

    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = -7760600735675079594L;

    /**
     *
     */
    public FunctionTermTableCellRenderer() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        table.setGridColor(ColorPalette.slateGray3);
        table.setBackground(Color.WHITE);
        if (row % 2 == 0) {
            setBackground(Color.WHITE);
        } else {
            setBackground(ColorPalette.lightBlue);
        }
        setForeground(Color.BLACK);
        setFont(getFont().deriveFont(Font.PLAIN));
        if (value instanceof Double) {
            setText(StringTools.toString(((Double) value).doubleValue()));
        } else if (value instanceof String) {
            setText((String) value);
        } else {
            setText(value.toString());
        }
        return this;
    }

}
