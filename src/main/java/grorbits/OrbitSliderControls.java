package grorbits;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.SwingPropertyChangeSupport;

public class OrbitSliderControls extends javax.swing.JComponent implements ChangeListener, MouseListener, MouseMotionListener{
  public JSlider slA, slLm, slB;
  
  double Lm,a,b;
  DecimalFormat format = new DecimalFormat("0.000");
  Orbit orbit;
  PropertyChangeSupport support = new SwingPropertyChangeSupport(this);
  boolean isStopped=true;
  
  public OrbitSliderControls(Orbit orbit){
    this.orbit=orbit;
    Font f= new Font("SansSerif", Font.PLAIN, 11);
    
    
    
    //  JSliders...
    a=0;
    slA = new JSlider(JSlider.VERTICAL, -1000, 1000, 0);
    TitledBorder borderA=BorderFactory. createTitledBorder("J/M = a = " + format.format(a) + " M");
    borderA.setTitleJustification(TitledBorder.CENTER);
    slA.setBorder(borderA);
    slA.setPaintTicks(true);
    
    //  Create the label table
    Hashtable labelTableA = new Hashtable();
    
    JLabel lbl=new JLabel("-1.00 M");
    lbl.setFont(f);
    labelTableA.put( new Integer( -1000 ), lbl);
    lbl=new JLabel("-0.75 M");
    lbl.setFont(f);
    labelTableA.put( new Integer( -750 ), lbl);
    lbl=new JLabel("-0.50 M");
    lbl.setFont(f);
    labelTableA.put( new Integer( -500 ), lbl);
    lbl=new JLabel("-0.25 M");
    lbl.setFont(f);
    labelTableA.put( new Integer( -250 ), lbl);
    lbl=new JLabel(" 0.00 M");
    lbl.setFont(f);
    labelTableA.put( new Integer( 0 ), lbl);
    lbl=new JLabel(" 0.25 M");
    lbl.setFont(f);
    labelTableA.put( new Integer( 250 ), lbl);
    lbl=new JLabel(" 0.50 M");
    lbl.setFont(f);
    labelTableA.put( new Integer( 500 ), lbl);
    lbl=new JLabel(" 0.75 M");
    lbl.setFont(f);
    labelTableA.put( new Integer( 750 ), lbl);
    lbl=new JLabel(" 1.0 M");
    lbl.setFont(f);
    labelTableA.put( new Integer( 1000 ), lbl);
    slA.setLabelTable( labelTableA );
    slA.setPaintLabels(true);
    slA.setMajorTickSpacing(250);
    slA.setMinorTickSpacing(50);
    slA.setPreferredSize(new Dimension(25, 50));
    slA.addChangeListener(this);
    
    Lm=4;
    slLm = new JSlider(JSlider.VERTICAL, -20000, 20000, 4000);
    TitledBorder borderLm=BorderFactory.createTitledBorder("L/m = " + format.format(Lm) + " M");
    borderLm.setTitleJustification(TitledBorder.CENTER);
    slLm.setBorder(borderLm);
    slLm.setPaintTicks(true);
    //  Create the label table
    Hashtable labelTableLm = new Hashtable();
    lbl=new JLabel("-20 M");
    lbl.setFont(f);
    labelTableLm.put( new Integer( -20000 ), lbl);
    lbl=new JLabel("-15 M");
    lbl.setFont(f);
    labelTableLm.put( new Integer( -15000 ), lbl);
    lbl=new JLabel("-10 M");
    lbl.setFont(f);
    labelTableLm.put( new Integer( -10000 ), lbl);
    lbl=new JLabel(" -5 M");
    lbl.setFont(f);
    labelTableLm.put( new Integer( -5000 ), lbl);
    lbl=new JLabel("  0 M");
    lbl.setFont(f);
    labelTableLm.put( new Integer( 0 ), lbl);
    lbl=new JLabel("  5 M");
    lbl.setFont(f);
    labelTableLm.put( new Integer( 5000 ), lbl);
    lbl=new JLabel(" 10 M");
    lbl.setFont(f);
    labelTableLm.put( new Integer( 10000 ), lbl);
    lbl=new JLabel(" 15 M");
    lbl.setFont(f);
    labelTableLm.put( new Integer( 15000 ), lbl);
    lbl=new JLabel(" 20 M");
    lbl.setFont(f);
    labelTableLm.put( new Integer( 20000 ), lbl);
    slLm.setLabelTable( labelTableLm );
    slLm.setPaintLabels(true);
    slLm.setMajorTickSpacing(5000);
    slLm.setMinorTickSpacing(1000);
    slLm.setPreferredSize(new Dimension(25, 50));
    slLm.addChangeListener(this);
    
    
    b=4;
    slB = new JSlider(JSlider.VERTICAL, -40000, 40000, 4000);
    TitledBorder borderB=BorderFactory.createTitledBorder("b = " + format.format(b) + " M");
    borderB.setTitleJustification(TitledBorder.CENTER);
    slB.setBorder(borderB);
    slB.setPaintTicks(true);
    
    //  Create the label table
    Hashtable labelTableB = new Hashtable();
    lbl=new JLabel("-40 M");
    lbl.setFont(f);
    labelTableB.put( new Integer( -40000 ), lbl);
    lbl=new JLabel("-30 M");
    lbl.setFont(f);
    labelTableB.put( new Integer( -30000 ), lbl);
    lbl=new JLabel("-20 M");
    lbl.setFont(f);
    labelTableB.put( new Integer( -20000 ), lbl);
    lbl=new JLabel("-10 M");
    lbl.setFont(f);
    labelTableB.put( new Integer( -10000 ), lbl);
    lbl=new JLabel("  0 M");
    lbl.setFont(f);
    labelTableB.put( new Integer( 0 ), lbl);
    lbl=new JLabel(" 10 M");
    lbl.setFont(f);
    labelTableB.put( new Integer( 10000 ), lbl);
    lbl=new JLabel(" 20 M");
    lbl.setFont(f);
    labelTableB.put( new Integer( 20000 ), lbl);
    lbl=new JLabel(" 30 M");
    lbl.setFont(f);
    labelTableB.put( new Integer( 30000 ), lbl);
    lbl=new JLabel(" 40 M");
    lbl.setFont(f);
    labelTableB.put( new Integer( 40000 ), lbl);
    slB.setLabelTable( labelTableB );
    slB.setPaintLabels(true);
    slB.setMajorTickSpacing(10000);
    slB.setMinorTickSpacing(2000);
    slB.setPreferredSize(new Dimension(25, 50));
    slB.addChangeListener(this);
 
    
    
    setLayout(new GridLayout(1,2));
    add(slA);
    add(slLm);
    
    slA.addMouseListener(this);
    slA.addMouseMotionListener(this);
    slLm.addMouseListener(this);
    slLm.addMouseMotionListener(this);
    slB.addMouseListener(this);
    slB.addMouseMotionListener(this);
    
    slB.getInputMap().put(KeyStroke.getKeyStroke("released UP"),"up");
    slB.getActionMap().put("up", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        support.firePropertyChange("ICChange",null,null); 
      }
    });
    
    slB.getInputMap().put(KeyStroke.getKeyStroke("released DOWN"),"down");
    slB.getActionMap().put("down", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        support.firePropertyChange("ICChange",null,null); 
      }
    });
    
    slB.getInputMap().put(KeyStroke.getKeyStroke("released RIGHT"),"right");
    slB.getActionMap().put("right", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        support.firePropertyChange("ICChange",null,null); 
      }
    });
    
    slB.getInputMap().put(KeyStroke.getKeyStroke("released LEFT"),"left");
    slB.getActionMap().put("left", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        support.firePropertyChange("ICChange",null,null); 
      }
    });
    
    slB.getInputMap().put(KeyStroke.getKeyStroke("released HOME"),"home");
    slB.getActionMap().put("home", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        support.firePropertyChange("ICChange",null,null); 
      }
    });
    
    slB.getInputMap().put(KeyStroke.getKeyStroke("released END"),"end");
    slB.getActionMap().put("end", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        support.firePropertyChange("ICChange",null,null); 
      }
    });
    
    slB.getInputMap().put(KeyStroke.getKeyStroke("released PAGE_UP"),"pgup");
    slB.getActionMap().put("pgup", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        support.firePropertyChange("ICChange",null,null); 
      }
    });
    
    slB.getInputMap().put(KeyStroke.getKeyStroke("released PAGE_DOWN"),"pgdn");
    slB.getActionMap().put("pgdn", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        support.firePropertyChange("ICChange",null,null); 
      }
    });
  }
  
  public void addPropertyChangeListener(PropertyChangeListener listener){
    support.addPropertyChangeListener(listener);
  }
  
  public void stateChanged(ChangeEvent event) {
    //defines the method stateChanged(...) of interface ChangeListener
    JSlider source = (JSlider) event.getSource();

    if (source.equals(slA)) {
      //after adjusting slA
      a=slA.getValue()/1000.0;
      TitledBorder borderA=BorderFactory. createTitledBorder("J/M = a = " + format.format(a) + " M");
      borderA.setTitleJustification(TitledBorder.CENTER);
      slA.setBorder(borderA);
      support.firePropertyChange("slAChange",null,null);
    }
    else if (source.equals(slLm)) {
      //after adjusting slLm
      Lm=slLm.getValue()/1000.0;
      TitledBorder borderLm=BorderFactory.createTitledBorder("L/m = " + format.format(Lm) + " M");
      borderLm.setTitleJustification(TitledBorder.CENTER);
      slLm.setBorder(borderLm);
      support.firePropertyChange("slLmChange",null,null); 
    }
    else if(source.equals(slB)) {
      //after adjusting slB
      b=slB.getValue()/1000.0;
      TitledBorder borderB=BorderFactory.createTitledBorder("b = " + format.format(b) + " M");
      borderB.setTitleJustification(TitledBorder.CENTER);
      slB.setBorder(borderB);
      support.firePropertyChange("slBChange",null,null);
    }
    
    
    
    
    
  }
  
  
  
  

  public double getA(){
    return a;
  }
  
  public double getLm(){
    return Lm;
  }
  
  public double getB(){
    return b;
  }
  
  public void setLm(double Lm){
    this.Lm=Lm;
    slLm.setValue(Math.round((float)(Lm*1000)));
  }
  
  public void setA(double a){
    this.a=a;
    slA.setValue(Math.round((float)(a*1000)));
  }
  
  public void setB(double b){
    this.b=b;
    slB.setValue(Math.round((float)(b*1000)));
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
    support.firePropertyChange("ICChange",null,null); 
  }
  
  public void mouseEntered(MouseEvent e) {
    if(isStopped()){
      support.firePropertyChange("slidersMouseEntered",null,null);
    }
   
  }
  
  public void mouseExited(MouseEvent e) {
    if(isStopped()){
      support.firePropertyChange("slidersMouseExited",null,null);
    }
  }
  
  public void mouseClicked(MouseEvent e) {
    
  }
  
  public void mouseMoved(MouseEvent e) {
    support.firePropertyChange("ICChange",null,null); 
  }
  
  
  
}


