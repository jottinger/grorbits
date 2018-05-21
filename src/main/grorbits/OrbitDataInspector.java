package grorbits;


import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

import java.awt.event.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;



public class OrbitDataInspector extends javax.swing.JComponent implements MouseListener, MouseMotionListener  {
  double Em, Lm, rInit, phiInit;
  JTable table;
  Orbit orbit;
  boolean isStopped=true;
  PropertyChangeSupport support = new SwingPropertyChangeSupport(this);
  
  public OrbitDataInspector(Orbit orbit){
    this.orbit=orbit;
    
    table = new JTable(new MyTableModel());
    //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
    //  Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(table);
    table.setCellSelectionEnabled(true);
    //this line of code adds the ability to communicate with Excel
    ExcelAdapter myAd = new ExcelAdapter(table);
    
    //Add the scroll pane to this panel.
    setLayout(new GridLayout(1,0));
    add(scrollPane);
    setPreferredSize(new Dimension(0,100));
    
    table.addMouseListener(this);
    table.addMouseMotionListener(this);
  }
  
  public void setTableModel(){
    table.setModel(new MyTableModel());
  }
  
//inner class implementing custom TableModel
  class MyTableModel extends AbstractTableModel {
    private String[] columnNames = {"t*","r*","\u03d5 (rad)","\u03c4*"};
    private Object[][] data = orbit.getOrbitData();
    
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
      return false;
    }
    
    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
      if(isCellEditable(row,col)) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
      }
    }
  } 
  
  public void addPropertyChangeListener(PropertyChangeListener listener){
    support.addPropertyChangeListener(listener);
  }
  
  
  public boolean isStopped(){
    return isStopped;
  }
  
  public void setStopped(boolean isStopped){
    this.isStopped = isStopped;
  }
  
  public void mousePressed(MouseEvent e) {
   
  }
  
  
  public void mouseDragged(MouseEvent e) {
  
  }
  
  public void mouseReleased(MouseEvent e) {
    
  }
  
  public void mouseEntered(MouseEvent e) {
    if(isStopped()){
      support.firePropertyChange("odMouseEntered",null,null);
    }
   
  }
  
  public void mouseExited(MouseEvent e) {
    if(isStopped()){
      support.firePropertyChange("odMouseExited",null,null);
    }
  }
  
  public void mouseClicked(MouseEvent e) {
    
  }
  
  public void mouseMoved(MouseEvent e) {
    
  }
  
  
}
