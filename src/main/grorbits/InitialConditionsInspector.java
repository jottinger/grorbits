package grorbits;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Locale;

import javax.swing.JScrollPane;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class InitialConditionsInspector extends javax.swing.JComponent implements TableModelListener, MouseListener, MouseMotionListener {
    
    public RXTable table;
    Orbit orbit;
    PropertyChangeSupport support = new SwingPropertyChangeSupport(this);
    ExcelAdapter myAd;
    boolean isStopped = true;
    
    public InitialConditionsInspector(Orbit orbit) {
        this.orbit = orbit;
        
        table = new RXTable(new MyTableModel());
        table.getModel().addTableModelListener(this);
        table.setSelectAllForEdit(true);

        //  Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        table.setCellSelectionEnabled(true);
        //this line of code adds the ability to communicate with Excel
        myAd = new ExcelAdapter(table);

        //Add the scroll pane to this panel.
        setLayout(new GridLayout(1, 0));
        add(scrollPane);
        setPreferredSize(new Dimension(0, 70));
        table.addMouseListener(this);
        table.addMouseMotionListener(this);
        table.setRowHeight(20);
        table.setLocale(Locale.getDefault());
        
    }

    //inner class implementing custom TableModel
    class MyTableModel extends AbstractTableModel {
        
        private String[] columnNames = {"property", "value"};
        private Object[][] data = orbit.getIC().getICData();
        
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            return data.length;
        }
        
        public String getColumnName(int col) {
            return columnNames[col];
        }
        
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 1) {
                return false;
            } else {
                return true;
            }
        }


        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            if (isCellEditable(row, col)) {
                data[row][col] = value;
                fireTableCellUpdated(row, col);
            }
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
     */
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        //int column = e.getColumn();
        //TableModel model = (TableModel)e.getSource();
        if ((orbit.getState().length==6)) {//if we have MATTER orbit
            if (row == 1) {//setting Em
                support.firePropertyChange("icInspectorChangeEm", null, null);
            } else if (row == 2) {//setting Lm
                support.firePropertyChange("icInspectorChangeLm", null, null);
                
            } else if (row == 3) {//setting r
                support.firePropertyChange("icInspectorChangeR", null, null);
            } else if (row == 0) {//setting a
                support.firePropertyChange("icInspectorChangeA", null, null);
            } else if (row == 6) {//setting dt
                support.firePropertyChange("icInspectorChangeDT", null, null);
            } else if (row == 4) {//setting v0
                support.firePropertyChange("icInspectorChangeV0", null, null);
            } else if (row == 5) {//setting theta0
                support.firePropertyChange("icInspectorChangeTheta0", null, null);
            } else if (row == 7) {//setting numPoints
                support.firePropertyChange("icInspectorChangeNumPoints", null, null);
            }
        } else {//if we have LIGHT orbit
            if (row == 1) {//setting 1/invB !!! changed
                support.firePropertyChange("icInspectorChangeEm", null, null);
            } else if (row == 2) {//setting Lm
                support.firePropertyChange("icInspectorChangeR", null, null);
                
            } else if (row == 0) {//setting a
                support.firePropertyChange("icInspectorChangeA", null, null);
            } else if (row == 4) {//setting dt
                support.firePropertyChange("icInspectorChangeDT", null, null);
            } else if (row == 3) {//setting theta0
                support.firePropertyChange("icInspectorChangeTheta0", null, null);
            } else if (row == 5) {//setting numPoints
                support.firePropertyChange("icInspectorChangeNumPoints", null, null);
            }
        }
        
    }
    
    public boolean isStopped() {
        return isStopped;
    }
    
    public void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }
    
    public void mousePressed(MouseEvent e) {
        
    }
    
    public void mouseDragged(MouseEvent e) {
        
    }
    
    public void mouseReleased(MouseEvent e) {
        
    }
    
    public void mouseEntered(MouseEvent e) {
        if (isStopped()) {
            support.firePropertyChange("icMouseEntered", null, null);
        }
        
    }
    
    public void mouseExited(MouseEvent e) {
        if (isStopped()) {
            support.firePropertyChange("icMouseExited", null, null);
        }
    }
    
    public void mouseClicked(MouseEvent e) {
        
    }
    
    public void mouseMoved(MouseEvent e) {
        
    }
    
}
