package grorbits;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.event.SwingPropertyChangeSupport;

public class PotentialDrawingPanel extends DrawingPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    Orbit orbit;
    PropertyChangeSupport support = new SwingPropertyChangeSupport(this);
    double rLocalMinUpperOutside, rLocalMaxUpperOutside, rLocalMinLowerOutside, rLocalMaxLowerOutside;
    double rLocalMinUpperInside, rLocalMaxUpperInside, rLocalMinLowerInside, rLocalMaxLowerInside;
    double vmMin, vmMax;
    double rMax = 25;
    boolean necessaryToRescale = false;
    boolean isStopped = true;
    boolean cursorOver = false, notDotDragged = false, mouseOverRightEdge = false;
    int curI, curJ;
    double rOld;
    double y2Old, y1Old, yCurDown;
    int jCurDown;

    public PotentialDrawingPanel(Orbit orbit) {
        setSquareAspect(false);
        this.orbit = orbit;
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);

    }

    public void draw(Graphics2D g2) {
        //We don't want the computeScale() to be executed on every repaint.
        //Therefore we do it only when something changes...
        if (necessaryToRescale) {
            setNewScale();
            necessaryToRescale = false;
        }

        float[] dashed = {2f, 2f};
        BasicStroke dbs = new BasicStroke(0.75f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1.0f, dashed, 0.0f);
        BasicStroke bs = new BasicStroke(0.75f);

        g2.setStroke(bs);
        //drawing shaded area representing ergosphere between horizon and static limit
        g2.setColor(new Color(128, 128, 128));
        g2.fill(new Rectangle2D.Double(0, 0, xToPix(2), size.getHeight()));

        //drawing shaded area representing black hole region...
        g2.setColor(Color.black);
        g2.fill(new Rectangle2D.Double(0, 0, xToPix(orbit.getRHorizon()), size.getHeight()));

        //draws INNER Cauchy horizon
        double rInt = xToPix(orbit.getRInnerHorizon()) - xToPix(0);
        //...coordinate of horizon in pixels
        g2.setColor(Color.green);
        g2.draw(new Line2D.Double(rInt, 0, rInt, size.getHeight()));

        //drawing the graph...
        double r, dr;
        double jVr, jVrpdr;
        double Vr, Vrpdr;

        dr = rMax / size.getWidth();

        // from inner horizon to singularity
        //starting dot on inner potential
        r = orbit.getRInnerHorizon() - 1e-10;
        Vr = orbit.getVmUpper(r);
        g2.setColor(Color.white);
        g2.draw(new Ellipse2D.Double(xToPix(r) - 5, yToPix(Vr) - 5, 10, 10));
        g2.setColor(new Color(128, 128, 250, 200));
        g2.fill(new Ellipse2D.Double(xToPix(r) - 5, yToPix(Vr) - 5, 10, 10));

        g2.setColor(Color.white);
        r = orbit.getRInnerHorizon() - 1e-10;
        Vr = orbit.getVmUpper(r);
        Vrpdr = orbit.getVmUpper(r - dr);
        while (r > dr) {

            jVr = yToPix(Vr);
            jVrpdr = yToPix(Vrpdr);

            g2.draw(new Line2D.Double(xToPix(r), jVr, xToPix(r - dr), jVrpdr));
            r -= dr;
            Vr = orbit.getVmUpper(r);
            Vrpdr = orbit.getVmUpper(r - dr);
        }
        // from outer horizon to rMax
        //starting dot on outer potential
        r = orbit.getRHorizon() + 1e-10;
        Vr = orbit.getVmUpper(r);
        g2.setColor(Color.black);
        g2.draw(new Ellipse2D.Double(xToPix(r) - 5, yToPix(Vr) - 5, 10, 10));
        g2.setColor(new Color(128, 128, 250, 200));
        g2.fill(new Ellipse2D.Double(xToPix(r) - 5, yToPix(Vr) - 5, 10, 10));

        g2.setColor(Color.black);
        Vrpdr = orbit.getVmUpper(r + dr);
        while (r < 1.1 * rMax) {

            jVr = yToPix(Vr);
            jVrpdr = yToPix(Vrpdr);

            g2.draw(new Line2D.Double(xToPix(r), jVr, xToPix(r + dr), jVrpdr));
            r += dr;
            Vr = orbit.getVmUpper(r);
            Vrpdr = orbit.getVmUpper(r + dr);
        }

        //Lower potential curve
        if (orbit.twoPotentials) {
            // from inner horizon to singularity
            g2.setColor(Color.white);
            r = orbit.getRInnerHorizon() - 1e-10;
            Vr = orbit.getVmLower(r);
            Vrpdr = orbit.getVmLower(r - dr);
            while (r > dr) {

                jVr = yToPix(Vr);
                jVrpdr = yToPix(Vrpdr);

                g2.draw(new Line2D.Double(xToPix(r), jVr, xToPix(r - dr), jVrpdr));
                r -= dr;
                Vr = orbit.getVmLower(r);
                Vrpdr = orbit.getVmLower(r - dr);
            }
            // from outer horizon to rMax
            g2.setColor(Color.black);
            r = orbit.getRHorizon();
            Vr = orbit.getVmAtHorizon();
            Vrpdr = orbit.getVmLower(r + dr);
            while (r < 1.1 * rMax) {

                jVr = yToPix(Vr);
                jVrpdr = yToPix(Vrpdr);

                g2.draw(new Line2D.Double(xToPix(r), jVr, xToPix(r + dr), jVrpdr));
                r += dr;
                Vr = orbit.getVmLower(r);
                Vrpdr = orbit.getVmLower(r + dr);
            }
        }

        //drawing Energy red line
        g2.setColor(Color.red);
        g2.draw(new Line2D.Double(0, yToPix(orbit.getIC().getEffPotParameter()), size.getWidth(),
                yToPix(orbit.getIC().getEffPotParameter())));

        //draws a dot representing current position of orbiter in Eff. potential diagram
        if (orbit.getR() < orbit.getRHorizon()) {
            g2.setColor(new Color(0, 255, 255));
        } else {
            g2.setColor(Color.red);
        }
        g2.fill(new Ellipse2D.Double(xToPix(orbit.getR()) - 2.5, yToPix(orbit.getIC().getEffPotParameter()) - 2.5, 5, 5));

        //  draws a dot representing initial position of orbiter in Eff. potential diagram
        if (orbit.getIC().getR() < orbit.getRHorizon()) {
            g2.setColor(Color.white);
        } else {
            g2.setColor(Color.black);
        }
        g2.fill(new Ellipse2D.Double(xToPix(orbit.getIC().getR()) - 2.5, yToPix(orbit.getIC().getEffPotParameter()) - 2.5, 5, 5));

        //  drawing scale...
        g2.setFont(f2);
        if (orbit.getR() < orbit.getRHorizon()) {
            g2.setColor(Color.white);
        } else {
            g2.setColor(Color.black);
        }
        double y1 = pixToY(size.height);
        double y2 = pixToY(0);
        double scaleDivision = 0.5
                * Math.pow(2, Math.round((float) (Math.log(0.5 * (y2 - y1)) / Math.log(2))));
        int digits = -Math.round((float) (Math.log(0.02 * scaleDivision)
                / Math.log(10)));
        double y = scaleDivision * Math.floor(y1 / scaleDivision);

        while (y < y2) {
            y += scaleDivision;
            g2.setStroke(dbs);
            g2.setColor(new Color(200, 200, 200));
            //horizontal dashed lines
            g2.draw(new Line2D.Double(0, yToPix(y), size.getWidth(), yToPix(y)));
            g2.setStroke(bs);
            g2.setColor(Color.black);
            g2.draw(new Line2D.Double(size.getWidth() - 5, yToPix(y), size.getWidth(), yToPix(y)));
            FontMetrics fm = g2.getFontMetrics();
            String s = MyUtils.roundOff(y, digits);
            int sW = fm.stringWidth(s) + 4;
            int sH = fm.getHeight();
            if ((Math.abs(yToPix(y) - yToPix(orbit.getIC().getEffPotParameter()) + 2)
                    >= sH / 2 + 1) || (Math.abs(xToPix(orbit.getR()) - size.getWidth() + 2) >= sW + 3)) {
                g2.setColor(new Color(247, 247, 247));
                g2.fill(new Rectangle2D.Double(size.getWidth() - 6 - sW, yToPix(y) - 4, sW, sH));
                g2.setColor(Color.black);
                g2.draw(new Rectangle2D.Double(size.getWidth() - 6 - sW, yToPix(y) - 4, sW, sH));
                MyUtils.drawString(s, size.getWidth() - 7, yToPix(y), 1, f2, g2);
            }
        }
        //vertical dashed lines
        double rr = 0;
        double exponent = Math.floor(Math.log(rMax) / Math.log(2));
        double gridDivision = Math.pow(2, exponent - 3);
        if (gridDivision < 0.000976562) {
            gridDivision = 0.000976562;
        }
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(dbs);
        while (rr < 1.1 * rMax) {
            g2.draw(new Line2D.Double(xToPix(rr), 0,
                    xToPix(rr), size.getHeight()));
            rr += 4 * gridDivision;
        }
        g2.setColor(Color.black);
        g2.setStroke(bs);

        if (mouseOverRightEdge) {
            //draws a transparent strip to the right of the plot
            //indicating user can drag the plot up and down
            g2.setColor(new Color(200, 200, 200, 100));
            double space = 2;
            double ww = 30;
            g2.fill(new RoundRectangle2D.Double(size.getWidth() - ww, space, ww - space, size.getHeight() - 2 * space, 20, 20));
            g2.setColor(Color.gray);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Line2D.Double(size.getWidth() - ww + 8, 15, size.getWidth() - ww / 2 - space / 2, 10));
            g2.draw(new Line2D.Double(size.getWidth() - space - 8, 15, size.getWidth() - ww / 2 - space / 2, 10));
            g2.draw(new Line2D.Double(size.getWidth() - ww + 8, size.getHeight() - 15, size.getWidth() - ww / 2 - space / 2, size.getHeight() - 10));
            g2.draw(new Line2D.Double(size.getWidth() - space - 8, size.getHeight() - 15, size.getWidth() - ww / 2 - space / 2, size.getHeight() - 10));

        }

        if (cursorOver) {
            //draws blue rectangle in the lower right of trajectory plot
            //with cursor position
            g2.setFont(f2);
            double curR = pixToX(curI);
            double curEm = pixToY(curJ);
            FontMetrics fm = g2.getFontMetrics();

            String sText = "r = ".concat(MyUtils.roundOff(curR, 3)).concat(" M;  ").concat(orbit.getIC().getEffPotParameterLabel()).concat(MyUtils.roundOff(curEm, digits)).concat(orbit.getIC().getEffPotParameterUnit());
            int sW = fm.stringWidth(sText) + 6;
            int sH = fm.getHeight() + 2;
            g2.setColor(new Color(255, 207, 207, 220));
            g2.fill(new Rectangle2D.Double(0, size.getHeight() - sH, sW, sH));
            g2.setColor(Color.black);
            //g2.draw(new Rectangle2D.Double(0, size.getHeight()-sH, sW, sH));
            g2.drawString(sText, 3, size.height - 3);

        }

    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void rescale() {
        necessaryToRescale = true;
    }

    private void setNewScale() {
        // computes the scale for for Vm diagram
        // also it sets value of public variables rLocalMinUpperOutside, rLocalMaxUpperOutside, ...
        // if rLocalMin...==0 then there's no local minimum in Vm(r)
        // else there's local minimum in Vm(r) at r=rLocalMin...
        // the same is true about rLocalMax...
        double min, max, rA, rB, rC;
        double dr;
        double rStart, rStop;

        //scan upper potential above the horizon
        min = 1e14;
        max = -1e14;
        rLocalMinUpperOutside = 0;
        rLocalMaxUpperOutside = 0;

        rStart = orbit.getRHorizon();
        rStop = rMax;
        dr = (rStop-rStart)/500;
        
        rA = rStart + 1e-10;
        rB = rA + dr;
        rC = rA + 2 * dr;

        while (rA <= rStop - 2 * dr) {
            double VmrA = orbit.getVmUpper(rA);
            double VmrB = orbit.getVmUpper(rB);
            double VmrC = orbit.getVmUpper(rC);
            if (VmrA < min) {
                min = VmrA;
            }
            if (VmrA > max) {
                max = VmrA;
            }
            if ((VmrB - VmrA) * (VmrC - VmrB) < 0) {
                if ((VmrB - VmrA) > 0) {
                    rLocalMaxUpperOutside = rB;
                } else {
                    rLocalMinUpperOutside = rB;
                }
            }
            rA += dr;
            rB += dr;
            rC += dr;
        }

        //scan lower potential above the horizon
        min = 1e14;
        max = -1e14;
        rLocalMinLowerOutside = 0;
        rLocalMaxLowerOutside = 0;
        rStart = orbit.getRHorizon();
        rStop = rMax;
        dr = (rStop-rStart)/500;

        rA = rStart + 1e-10;
        rB = rA + dr;
        rC = rA + 2 * dr;

        while (rA <= rStop - 2 * dr) {
            double VmrA = orbit.getVmLower(rA);
            double VmrB = orbit.getVmLower(rB);
            double VmrC = orbit.getVmLower(rC);
            if (VmrA < min) {
                min = VmrA;
            }
            if (VmrA > max) {
                max = VmrA;
            }
            if ((VmrB - VmrA) * (VmrC - VmrB) < 0) {
                if ((VmrB - VmrA) > 0) {
                    rLocalMaxLowerOutside = rB;
                } else {
                    rLocalMinLowerOutside = rB;
                }
            }
            rA += dr;
            rB += dr;
            rC += dr;
        }

        //scan upper potential below the horizon
        min = 1e14;
        max = -1e14;
        rLocalMinUpperInside = 0;
        rLocalMaxUpperInside = 0;

        rStart = 0;
        rStop = orbit.getRInnerHorizon();
        dr = (rStop-rStart)/500;

        rA = rStart + 1e-10;
        rB = rA + dr;
        rC = rA + 2 * dr;

        while (rA <= rStop - 2 * dr) {
            double VmrA = orbit.getVmUpper(rA);
            double VmrB = orbit.getVmUpper(rB);
            double VmrC = orbit.getVmUpper(rC);
            if (VmrA < min) {
                min = VmrA;
            }
            if (VmrA > max) {
                max = VmrA;
            }
            if ((VmrB - VmrA) * (VmrC - VmrB) < 0) {
                if ((VmrB - VmrA) > 0) {
                    rLocalMaxUpperInside = rB;
                } else {
                    rLocalMinUpperInside = rB;
                }
            }
            rA += dr;
            rB += dr;
            rC += dr;
        }

        //scan lower potential above the horizon
        min = 1e14;
        max = -1e14;
        rLocalMinLowerInside = 0;
        rLocalMaxLowerInside = 0;
        rStart = 0;
        rStop = orbit.getRInnerHorizon();
        dr = (rStop-rStart)/500;

        rA = rStart + 1e-10;
        rB = rA + dr;
        rC = rA + 2 * dr;

        while (rA <= rStop - 2 * dr) {
            double VmrA = orbit.getVmLower(rA);
            double VmrB = orbit.getVmLower(rB);
            double VmrC = orbit.getVmLower(rC);
            if (VmrA < min) {
                min = VmrA;
            }
            if (VmrA > max) {
                max = VmrA;
            }
            if ((VmrB - VmrA) * (VmrC - VmrB) < 0) {
                if ((VmrB - VmrA) > 0) {
                    rLocalMaxLowerInside = rB;
                } else {
                    rLocalMinLowerInside = rB;
                }
            }
            rA += dr;
            rB += dr;
            rC += dr;
        }

        //this is here to enable horizontal resizing with a mouse
        if ((orbit.getIC().getR() > 0.8 * rMax) && (rMax < 32768)) {
            rMax = 1.1 * orbit.getIC().getR();
        } else if ((orbit.getIC().getR() < 0.3 * rMax) && (rMax > 1e-10)) {
            rMax = 0.9 * rMax;
        }

        //******
        double y1 = sy1;
        double y2 = sy2;
        if (orbit.getIC().getEffPotParameter() > y2) {
            y2 = orbit.getIC().getEffPotParameter() + 0.05 * (orbit.getIC().getEffPotParameter() - y1);
        }
        if (orbit.getIC().getEffPotParameter() < y1) {
            y1 = orbit.getIC().getEffPotParameter() - 0.05 * (y2 - orbit.getIC().getEffPotParameter());
        }

        setPreferredMinMaxX(0, rMax);
        setPreferredMinMaxY(y1, y2);
    }

    public void setRMax(double rMax) {
        this.rMax = rMax;
    }

    public double getRMax() {
        return rMax;
    }

    public double getRLocalMinUpperOutside() {
        setNewScale();

        if (rLocalMinUpperOutside < 1e-10) {
            return 0;
        } else {
            return findStationaryPositionUpper(rLocalMinUpperOutside - rMax / (size.getWidth()), rLocalMinUpperOutside + rMax / (size.getWidth()));
        }

    }

    public double getRLocalMaxUpperOutside() {
        setNewScale();
        if (rLocalMaxUpperOutside < 1e-10) {
            return 0;
        } else {
            return findStationaryPositionUpper(rLocalMaxUpperOutside - rMax / (size.getWidth()), rLocalMaxUpperOutside + rMax / (size.getWidth()));
        }
    }

    public double getRLocalMinLowerOutside() {
        setNewScale();

        if (rLocalMinLowerOutside < 1e-10) {
            return 0;
        } else {
            return findStationaryPositionLower(rLocalMinLowerOutside - rMax / (size.getWidth()), rLocalMinLowerOutside + rMax / (size.getWidth()));
        }

    }

    public double getRLocalMaxLowerOutside() {
        setNewScale();
        if (rLocalMaxLowerOutside < 1e-10) {
            return 0;
        } else {
            return findStationaryPositionLower(rLocalMaxLowerOutside - rMax / (size.getWidth()), rLocalMaxLowerOutside + rMax / (size.getWidth()));
        }
    }

    public double getRLocalMaxUpperInside() {
        setNewScale();
        if (rLocalMaxUpperInside < 1e-10) {
            return 0;
        } else {
            return findStationaryPositionUpper(rLocalMaxUpperInside - rMax / (size.getWidth()), rLocalMaxUpperInside + rMax / (size.getWidth()));
        }
    }

    public double getRLocalMinLowerInside() {
        setNewScale();

        if (rLocalMinLowerInside < 1e-10) {
            return 0;
        } else {
            return findStationaryPositionLower(rLocalMinLowerInside - rMax / (size.getWidth()), rLocalMinLowerInside + rMax / (size.getWidth()));
        }

    }

    private double findStationaryPositionUpper(double a, double b) {
        //we suppose that a < b
        double eps = 1e-10;
        double newA = a, newB = b;
        double v1, v2;
        double minAbsSlope = 1e12;
        double absSlope;

        double x = a;
        double dx = (b - a) / 10.0;

        while (x < b) {
            v1 = orbit.getVmUpper(x);
            v2 = orbit.getVmUpper(x + dx);

            absSlope = Math.abs((v2 - v1) / dx);
            if (absSlope < minAbsSlope) {
                minAbsSlope = absSlope;
                newA = x;
                newB = x + dx;
            }

            x += dx;
        }

        if (dx < eps) {
            return (newA + newB) / 2.0;
        } else {
            return findStationaryPositionUpper(newA, newB);
        }
    }

    private double findStationaryPositionLower(double a, double b) {
        //we suppose that a < b
        double eps = 1e-10;
        double newA = a, newB = b;
        double v1, v2;
        double minAbsSlope = 1e12;
        double absSlope;

        double x = a;
        double dx = (b - a) / 10.0;

        while (x < b) {
            v1 = orbit.getVmLower(x);
            v2 = orbit.getVmLower(x + dx);

            absSlope = Math.abs((v2 - v1) / dx);
            if (absSlope < minAbsSlope) {
                minAbsSlope = absSlope;
                newA = x;
                newB = x + dx;
            }

            x += dx;
        }

        if (dx < eps) {
            return (newA + newB) / 2.0;
        } else {
            return findStationaryPositionLower(newA, newB);
        }
    }

    public boolean isStopped() {
        return isStopped;
    }

    public void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    public boolean adjustICToMinLPlusOutside() {
        //If there's a local minimum in effective potential
        //it sets r-coordinate value to this minimum and sets Em value
        //to its smallest allowed value so that
        //the orbiter follows a circular orbit
        //Otherwise nothing is done

        boolean hasMin = false;
        double rr;
        rr = getRLocalMinUpperOutside();
        if (rr < 1e-10) {
            hasMin = false;
            //do nothing
        } else {
            hasMin = true;
            //set initial conditions
            orbit.getIC().setR(rr);
            orbit.getIC().setEffPotParameter(orbit.getVmUpper(rr)+1e-12);
            rescale();
        }

        return hasMin;
    }

    public boolean adjustICToMaxLPlusOutside() {
        //If there's a local minimum in effective potential
        //it sets r-coordinate value to this minimum and sets Em value
        //to its smallest allowed value so that
        //the orbiter follows a circular orbit
        //Otherwise nothing is done

        boolean hasMin = false;
        double rr;
        rr = getRLocalMaxUpperOutside();
        if (rr < 1e-10) {
            hasMin = false;
            //do nothing
        } else {
            hasMin = true;
            //set initial conditions
            orbit.getIC().setR(rr);
            orbit.getIC().setEffPotParameter(orbit.getVmUpper(rr)+1e-12);
            rescale();
        }

        return hasMin;
    }

    public boolean adjustICToMinLMinusOutside() {
        //If there's a local minimum in effective potential
        //it sets r-coordinate value to this minimum and sets Em value
        //to its smallest allowed value so that
        //the orbiter follows a circular orbit
        //Otherwise nothing is done

        boolean hasMin = false;
        double rr;
        rr = getRLocalMinLowerOutside();
        if (rr < 1e-10) {
            hasMin = false;
            //do nothing
        } else {
            hasMin = true;
            //set initial conditions
            orbit.getIC().setR(rr);
            orbit.getIC().setEffPotParameter(orbit.getVmLower(rr)-1e-12);
            rescale();
        }

        return hasMin;
    }

    public boolean adjustICToMaxLMinusOutside() {
        //If there's a local minimum in effective potential
        //it sets r-coordinate value to this minimum and sets Em value
        //to its smallest allowed value so that
        //the orbiter follows a circular orbit
        //Otherwise nothing is done

        boolean hasMin = false;
        double rr;
        rr = getRLocalMaxLowerOutside();
        if (rr < 1e-10) {
            hasMin = false;
            //do nothing
        } else {
            hasMin = true;
            //set initial conditions
            orbit.getIC().setR(rr);
            orbit.getIC().setEffPotParameter(orbit.getVmLower(rr)-1e-12);
            rescale();
        }

        return hasMin;
    }

    public boolean adjustICToMaxLPlusInside() {
        //If there's a local maximum in upper effective potential
        //it sets r-coordinate value to this maximum and sets the value of eff. pot. paramater
        //to its smallest allowed value so that
        //the orbiter follows a circular orbit
        //Otherwise nothing is done

        boolean hasMax = false;
        double rr;
        rr = getRLocalMaxUpperInside();
        if (rr < 1e-10) {
            hasMax = false;
            //do nothing

        } else {
            hasMax = true;

            //set initial conditions
            orbit.getIC().setR(rr);
            orbit.getIC().setEffPotParameter(orbit.getVmUpper(rr)+1e-12);
            rescale();
        }

        return hasMax;
    }

    public boolean adjustICToMinLMinusInside() {
        //If there's a local minimum in the lower effective potential
        //it sets r-coordinate value to this minimum and sets the value of eff. pot. paramater
        //to its largest allowed value so that
        //the orbiter follows a circular orbit
        //Otherwise nothing is done

        boolean hasMax = false;
        double rr;
        rr = getRLocalMinLowerInside();
        if (rr < 1e-10) {
            hasMax = false;
            //do nothing

        } else {
            hasMax = true;

            //set initial conditions
            orbit.getIC().setR(rr);
            orbit.getIC().setEffPotParameter(orbit.getVmLower(rr)-1e-12);
            rescale();
        }

        return hasMax;
    }

    public void mousePressed(MouseEvent e) {
        if (isStopped()) {
            int i = e.getX();
            int j = e.getY();

            double r = pixToX(i);

            double param = pixToY(j);
            int ir = Math.round((float) xToPix(orbit.getIC().getR()));
            int iParam = Math.round((float) yToPix(orbit.getIC().getEffPotParameter()));

            if (!(Math.abs(ir - i) < 5) || !(Math.abs(iParam - j) < 5)) {//changing scale
                notDotDragged = true;
                jCurDown = j;
                rOld = r;
                y1Old = sy1;
                y2Old = sy2;
            } else if (mouseOverRightEdge) {
                jCurDown = j;
                y1Old = sy1;
                y2Old = sy2;
            } else {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                orbit.getIC().setR(r);
                orbit.getIC().setEffPotParameter(param);
                orbit.reset();
                rescale();
                support.firePropertyChange("effPotMouseChange", null, null);
            }

        }
    }

    public void mouseDragged(MouseEvent e) {
        if (isStopped()) {
            int i = e.getX();
            int j = e.getY();

            double r = pixToX(i);

            double param = pixToY(j);

            if (notDotDragged) {//changing scale

                if (e.isAltDown()) {//vertical resize
                    setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));

                    double quot = (j - jCurDown) / (yToPix(y1Old) - yToPix(y2Old));
                    double y2New = (y1Old + y2Old) / 2 + (y2Old - (y1Old + y2Old) / 2) * (1 + quot);
                    double y1New = (y1Old + y2Old) / 2 + (y1Old - (y1Old + y2Old) / 2) * (1 + quot);
                    setPreferredMinMaxY(Math.min(y1New, y2New), Math.max(y1New, y2New));

                    /**
                     * OLD METHOD double
                     * icParam=orbit.getIC().getEffPotParameter(); double
                     * pixIcParam =yToPix(icParam);
                     * if(Math.abs(j-pixIcParam)>1e-5){ double
                     * quot=Math.abs((jCurDown-pixIcParam)/(j-pixIcParam));
                     * double y2New = icParam + (y2Old-icParam)*quot; double
                     * y1New = icParam + (y1Old-icParam)*quot;
                     *
                     * setPreferredMinMaxY(Math.min(y1New,y2New),
                     * Math.max(y1New,y2New));
                     *
                     * }*
                     */
                } else if (mouseOverRightEdge) {
                    setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                    double y2New = y2Old - (pixToY(j) - pixToY(jCurDown));
                    double y1New = y1Old - (pixToY(j) - pixToY(jCurDown));
                    setPreferredMinMaxY(y1New, y2New);
                } else {//horizontal resize
                    setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
                    double newRMax = getRMax() * rOld / r;
                    if ((r > 1e-3) && (newRMax > 1e-3)) {
                        setRMax(newRMax);
                    }
                    support.firePropertyChange("effPotMouseXResize", null, null);
                }
            } else {//dragging the dot
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                orbit.getIC().setR(r);
                orbit.getIC().setEffPotParameter(param);
                orbit.reset();
                rescale();
                support.firePropertyChange("effPotMouseChange", null, null);
            }

        }

        //do always
        curI = e.getX();
        curJ = e.getY();
        if (curI < 0) {
            curI = 0; //we don't want negative r
        }
        cursorOver = true;
        repaint();

    }

    public void mouseReleased(MouseEvent e) {
        if (isStopped()) {
            int i = e.getX();
            int j = e.getY();

            double r = pixToX(i);

            double param = pixToY(j);

            if (notDotDragged) {//changing scale
                notDotDragged = false;
                if (e.isAltDown()) {//vertical resize
                    setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                    double quot = (j - jCurDown) / (yToPix(y1Old) - yToPix(y2Old));
                    double y2New = (y1Old + y2Old) / 2 + (y2Old - (y1Old + y2Old) / 2) * (1 + quot);
                    double y1New = (y1Old + y2Old) / 2 + (y1Old - (y1Old + y2Old) / 2) * (1 + quot);
                    setPreferredMinMaxY(Math.min(y1New, y2New), Math.max(y1New, y2New));

                    /**
                     * OLD RESIZE METHOD
                     *
                     * double icParam=orbit.getIC().getEffPotParameter(); double
                     * pixIcParam =yToPix(icParam);
                     * if(Math.abs(j-pixIcParam)>1e-5){ double
                     * quot=Math.abs((jCurDown-pixIcParam)/(j-pixIcParam));
                     * double y2New = icParam + (y2Old-icParam)*quot; double
                     * y1New = icParam + (y1Old-icParam)*quot;
                     * setPreferredMinMaxY(Math.min(y1New,y2New),
                     * Math.max(y1New,y2New));
                     *
                     * }
                     *
                     */
                } else if (mouseOverRightEdge) {
                    setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                    double y2New = y2Old - (pixToY(j) - pixToY(jCurDown));
                    double y1New = y1Old - (pixToY(j) - pixToY(jCurDown));
                    setPreferredMinMaxY(y1New, y2New);
                } else {
                    setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                    double newRMax = getRMax() * rOld / r;
                    if ((r > 1e-3) && (newRMax > 1e-3)) {
                        setRMax(newRMax);
                    }
                    support.firePropertyChange("effPotMouseXResize", null, null);
                }
            } else {//dot dragged
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                orbit.getIC().setR(r);
                orbit.getIC().setEffPotParameter(param);
                orbit.reset();
                rescale();
                support.firePropertyChange("effPotMouseChange", null, null);
            }

        }

        //  do always
        curI = e.getX();
        if (curI < 0) {
            curI = 0; //we don't want negative r
        }
        curJ = e.getY();
        cursorOver = true;
        repaint();

    }

    public void mouseEntered(MouseEvent e) {
        if (isStopped()) {
            support.firePropertyChange("potMouseEntered", null, null);
        }
    }

    public void mouseExited(MouseEvent e) {
        if (isStopped()) {
            support.firePropertyChange("potMouseExited", null, null);
        }
        mouseOverRightEdge = false;
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        cursorOver = false;
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
        if (isStopped()) {
            int i = e.getX();
            int j = e.getY();
            int ir = Math.round((float) xToPix(orbit.getIC().getR()));
            int iParam = Math.round((float) yToPix(orbit.getIC().getEffPotParameter()));
            if ((Math.abs(ir - i) < 5) && (Math.abs(iParam - j) < 5)) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
            }

            if ((i > size.getWidth() - 30) && size.getWidth() > 0) {
                mouseOverRightEdge = true;
            } else {
                mouseOverRightEdge = false;
            }
        }

        //do always
        curI = e.getX();
        curJ = e.getY();
        cursorOver = true;
        repaint();

    }

    public void mouseClicked(MouseEvent e) {

    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (isStopped()) {
            int notches = e.getWheelRotation();
            double y2;
            double y1;

            if (notches < 0) {//scroll up
                y2 = sy2 + 0.1 * (sy2 - sy1);
                y1 = sy1 + 0.1 * (sy2 - sy1);
            } else {//scroll down
                y2 = sy2 - 0.1 * (sy2 - sy1);
                y1 = sy1 - 0.1 * (sy2 - sy1);
            }
            setPreferredMinMaxY(y1, y2);
            repaint();
        }
    }

}
