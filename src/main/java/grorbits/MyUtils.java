package grorbits;


/**
 * Contains useful methods used frequently
 * (c) Slavomir Tuleja, August 2002
 */

import java.awt.*;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
public class MyUtils {
  
  public static String roundOff(double d, int i){
    //rounds off number d to i decimal places
    Locale currentLocale = new Locale("en", "EN");
    DecimalFormatSymbols unusualSymbols = new DecimalFormatSymbols(currentLocale);
    String decSep = String.valueOf(unusualSymbols.getDecimalSeparator());
    
    long l = Math.round(d * Math.pow(10, i));
    boolean flag;
    if(l < 0)
    {
      flag = true;
      l *= -1;
    } else
    {
      flag = false;
    }
    String s = String.valueOf(l);
    int j = s.length();
    String s1;
    String s2;
    if(j > i)
    {
      s1 = s.substring(0, j - i);
      s2 = s.substring(j - i, j);
    } else
    {
      String s3 = new String("000000000000000000000000000000000");
      s1 = new String("0");
      s2 = s3.substring(0, i - j).concat(s);
    }
    if(i != 0)
      s = s1.concat(decSep).concat(s2);
    if(flag)
      s = "-".concat(s);
    return s;
  }
  
  public static void drawString(String s, double ii, double jj, int alignment, Font f, Graphics2D g2){
    //prints out string s at position [i,j] using font f
    // alignment 1 ... left [i,j]
    //           2 ... center
    //           3 ... right
    int i=Math.round((float)ii);
    int j=Math.round((float)jj);
    int width, height;
    width = g2.getFontMetrics().stringWidth(s);
    height = g2.getFontMetrics().getHeight();
    switch(alignment){
    case 1: //left from [i,j]
      g2.drawString(s,i-width,j+height/2);
      return;
    case 2: //center
      g2.drawString(s,i-width/2,j+height/2);
      return;
    case 3://right
      g2.drawString(s,i,j+height/2);
      return;
    default:
      g2.drawString(s,i,j);
    return;
    }
  }
  
}
