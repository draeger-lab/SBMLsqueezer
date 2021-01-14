package org.sbml.squeezer.functionTermGenerator;

import de.zbit.gui.GUITools;
import de.zbit.util.ResourceManager;
import de.zbit.util.StringUtil;
import de.zbit.util.progressbar.AbstractProgressBar;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.squeezer.util.Bundles;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static de.zbit.util.Utils.getMessage;

/**
 * Creates a table that displays all created function terms (Aug 20, 2020)
 *
 * Based on:
 * @see org.sbml.squeezer.gui.KineticLawTable
 * by:
 * @author Andreas Dr&auml;ger
 *
 * Adapted by:
 * @author Eike Pertuch
 *
 * @since 2.1.2
 */
public class FunctionTermTable extends JTable implements MouseInputListener {

    /**
     * Generated serial version ID
     */
    private static final long serialVersionUID = -1575566223506382693L;

    public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);

    private boolean editing;

    private FunctionTermGenerator ftg;

    /**
     *
     * Worker which generates table of newly created function terms
     * in a dedicated worker thread
     *
     * Based on
     * @see org.sbml.squeezer.gui.KineticLawTable
     * by:
     * @author Andreas Dr&auml;ger
     *
     * Adapted by:
     * @author Eike Pertuch
     *
     * @since 2.1.2
     */
    private static final class TableModelWorker extends SwingWorker<FunctionTermTableModel, Void> {

        private FunctionTermGenerator ftg;
        private FunctionTermTable table;
        private AbstractProgressBar progressBar;
        private PropertyChangeListener listener;

        /**
         *
         * @param ftg
         * @param progressBar
         * @param table
         */
        public TableModelWorker(FunctionTermGenerator ftg, AbstractProgressBar progressBar,
                                FunctionTermTable table, PropertyChangeListener listener) {
            super();
            this.ftg = ftg;
            this.table = table;
            this.progressBar = progressBar;
            this.listener = listener;
        }

        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#doInBackground()
         */
        @Override
        protected FunctionTermTableModel doInBackground() throws Exception {
            return new FunctionTermTableModel(ftg, progressBar);
        }

        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#done()
         */
        @Override
        protected void done() {
            try {
                table.setModel(get());
                table.setColumnWidthAppropriately();
                table.setRowHeightAppropriately();
                listener.propertyChange(new PropertyChangeEvent(table, "done", null, table.getModel()));
            } catch (Exception exc) {
                Logger.getLogger(FunctionTermTable.TableModelWorker.class.getName()).fine(getMessage(exc));
            }
        }

    }

    /**
     *
     * @param ftg
     * @param progressBar
     * @param listener
     */
    public FunctionTermTable(FunctionTermGenerator ftg, AbstractProgressBar progressBar, PropertyChangeListener listener) {
        super();
        TableModelWorker tmw = new TableModelWorker(ftg, progressBar, this, listener);
        tmw.execute();
        this.ftg = ftg;
        getModel().addTableModelListener(this);
        setDefaultRenderer(Object.class, new FunctionTermTableCellRenderer());
        getTableHeader().setToolTipText(
                StringUtil.toHTML(MESSAGES.getString("FUNCTION_TERM_HEADER_TOOLTIP"), 40));
        setCellSelectionEnabled(true);
        setEnabled(true);
        addMouseListener(this);
        editing = false;
    }

    /**
     * Sets up a window in which a generated in which a function term can be viewed in more
     * detail (as a LaTeX view)
     *
     * @param rowIndex
     * @throws IllegalArgumentException
     * @throws SecurityException
     */
    private void setCellEditor(final int rowIndex) throws SecurityException,
            IllegalArgumentException {
        if ((dataModel.getRowCount() > 0) && (dataModel.getColumnCount() > 0)) {
            Transition transition = ftg.getModifiedTransitions().get(rowIndex);
            try {
                final FunctionTermDisplayPanel ftdp = new FunctionTermDisplayPanel(transition);
                final JOptionPane pane = new JOptionPane(ftdp,
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.DEFAULT_OPTION, UIManager
                        .getIcon("SBMLsqueezerIcon_64"));
                pane.selectInitialValue();
                Container container = getTopLevelAncestor();
                final JDialog dialog;
                if (container instanceof Frame) {
                    dialog = new JDialog((Frame) container,
                            MessageFormat.format(MESSAGES.getString("DISPLAY_OF_SELECTED_FUNCTION_TERM"),
                                    transition.getId()));
                } else if (container instanceof Dialog) {
                    dialog = new JDialog((Dialog) container,
                            MessageFormat.format(MESSAGES.getString("DISPLAY_OF_SELECTED_FUNCTION_TERM"),
                                    transition.getId()));
                } else {
                    dialog = new JDialog();
                    dialog.setTitle(MessageFormat.format(MESSAGES.getString("DISPLAY_OF_SELECTED_FUNCTION_TERM"),
                            transition.getId()));
                }
                Container content = dialog.getContentPane();
                content.setLayout(new BorderLayout());
                content.add(pane, BorderLayout.CENTER);
                dialog.setModal(true);

                WindowAdapter adapter = new WindowAdapter() {
                    private boolean gotFocus = false;

                    /* (non-Javadoc)
                     * @see java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent)
                     */
                    @Override
                    public void windowClosing(WindowEvent we) {
                        pane.setValue(null);
                    }

                    /* (non-Javadoc)
                     * @see java.awt.event.WindowAdapter#windowGainedFocus(java.awt.event.WindowEvent)
                     */
                    @Override
                    public void windowGainedFocus(WindowEvent we) {
                        // Once window gets focus, set initial focus
                        if (!gotFocus) {
                            pane.selectInitialValue();
                            gotFocus = true;
                        }
                    }
                };

                pane.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        if(dialog.isVisible()) {
                            dialog.setVisible(false);
                        }
                    }
                });

                dialog.addWindowListener(adapter);
                dialog.addWindowFocusListener(adapter);
                dialog.addComponentListener(new ComponentAdapter() {

                    /* (non-Javadoc)
                     * @see java.awt.event.ComponentAdapter#componentShown(java.awt.event.ComponentEvent)
                     */
                    @Override
                    public void componentShown(ComponentEvent ce) {
                        // reset value to ensure closing works properly
                        pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                    }

                });



                dialog.pack();
                dialog.setResizable(false);
                dialog.setSize(new Dimension(700, 300));
                dialog.setLocationRelativeTo(dialog.getOwner());
                dialog.setVisible(true);
                dialog.dispose();
            } catch (Throwable exc) {
                GUITools.showErrorMessage(this, exc);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        if (!editing) {
            try {
                setCellEditor(rowIndex);
            } catch (Throwable exc) {
                GUITools.showErrorMessage(this, exc);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        if (!editing) {
            try {
                setCellEditor(rowIndex);
            } catch (Throwable exc) {
                GUITools.showErrorMessage(this, exc);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     *
     */
    private void setRowHeightAppropriately() {
        setRowHeight(getFont().getSize() * 2);
    }

    /**
     *
     */
    private void setColumnWidthAppropriately() {
        for (int col = 0; col < getModel().getColumnCount(); col++) {
            int maxLength = getColumnModel().getColumn(col).getHeaderValue()
                    .toString().length();
            for (int row = 0; row < getRowCount(); row++) {
                if (maxLength < getValueAt(row, col).toString().length()) {
                    maxLength = getValueAt(row, col).toString().length();
                }
            }
            getColumnModel().getColumn(col).setPreferredWidth(
                    3 * getFont().getSize() / 5 * maxLength + 10) ;
        }
    }

}
