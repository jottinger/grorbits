package grorbits;

import java.awt.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.*;
import java.awt.RenderingHints;

/**
 * @author Slavomir Tuleja
 *
 * 
 */
public abstract class DrawingPanel extends javax.swing.JComponent {
  Dimension size;
  Font f0 = new Font("SansSerif", Font.PLAIN, 12);
  Font f1 = new Font("SansSerif", Font.BOLD, 12);
  Font f2 = new Font("SansSerif", Font.PLAIN, 9);
  
  //display scaling variables
  double sx1, sx2, sy1, sy2, si1, si2, sj1, sj2;
  double cX;
  boolean sqrAsp=false;
  
  
  /**
   * Constructor.
   *
   */
  public DrawingPanel(){
    size=getSize();
  }
  
  
  /**
   * 
   * @param g Graphics
   */
  public void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    
    //this is done in case the window is resized
    //from knowledge of sy1, sy2 we compute sx1, sx2 according to the size
    //of the display area
    size = getSize();
    si1 = 0;
    si2 = size.getWidth();
    sj1 = 0;
    sj2 = size.getHeight();
    cX=0;
    
    if(isSquareAspect()){
      sx1 = cX - 0.5 * (sy2 - sy1) / size.getHeight() * size.getWidth();
      sx2 = cX + 0.5 * (sy2 - sy1) / size.getHeight() * size.getWidth();
    }
    
    //background
    g2.setColor(Color.white);
    g2.fill(new Rectangle2D.Double(0, 0, size.width, size.height));
    g2.setColor(Color.black);
    draw(g2);
    
  }
  
  
  public abstract void draw(Graphics2D g2);
  
  
  public double xToPix(double x) {
    return si1 + (x - sx1) * (si2 - si1) / (sx2 - sx1);
  }

  public double yToPix(double y) {
    return sj1 + (sy2 - y) * (sj2 - sj1) / (sy2 - sy1);
  }

  public double pixToX(int i) {
    return sx1 + ((double) (i - si1)) * (sx2 - sx1) / (si2 - si1);
  }

  public double pixToY(int j) {
    return sy2 + ((double) (j - sj1)) * (sy1 - sy2) / (sj2 - sj1);
  }

  public void setPreferredMinMaxX(double sx1, double sx2) {
    this.sx1 = sx1;
    this.sx2 = sx2;
  }

  public void setPreferredMinMaxY(double sy1, double sy2) {
    this.sy1 = sy1;
    this.sy2 = sy2;
    if(isSquareAspect()){
      sx1 = cX - 0.5 * (sy2 - sy1) / size.getHeight() * size.getWidth();
      sx2 = cX + 0.5 * (sy2 - sy1) / size.getHeight() * size.getWidth();
    }
  }
  
  public void setSquareAspect(boolean sqrAsp){
    this.sqrAsp=sqrAsp;
  }
  
  boolean isSquareAspect(){
    return sqrAsp;
  }
}
