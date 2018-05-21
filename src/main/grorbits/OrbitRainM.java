package grorbits;

import java.text.DecimalFormat;
import org.opensourcephysics.tuleja.numerics.*;

public class OrbitRainM extends Orbit {

    public OrbitRainM() {
        super();
        twoPotentials = true;
    }

    public void initializeVariables() {
        state = new double[]{10, 0, 0, 0, 0, 0};
    //...initial state: {r,phi,pR,pPhi,tau,t}

        numPoints = 1500;
        ic = new InitialConditionsRainM(this, 0, 0.95, 4, 20, -1, 1);//{a,Em,Lm,r,sign,dt}
        t = 0;
        orbitData = new Double[numPoints][4];
    }

    public void initialize(double a, double r, double v0, double theta0, double dt, int numPoints) {
        state = new double[]{10, 0, 0, 0, 0, 0};
    //...initial state: {r,phi,pR,pPhi,tau,t}

        this.numPoints = numPoints;

        ic = new InitialConditionsRainM(this, a, r, v0, theta0, dt);
        t = 0;
        orbitData = new Double[numPoints][4];

        //  inicialization of ODE Solver
        odeSolver = new RK45GRorbitsMultiStep(this);
        odeSolver.setTolerance(1e-6);
    //odeSolver.setMaximumNumberOfErrorMessages(1);
        //...error in one step
        odeSolver.initialize(ic.getDT());
    //...size of a fixed step

        reset();
    }

    public void getRate(double[] state, double[] rates) {
        double a = ic.getA();
        double r, pR, pPhi;
        double b, c, rho;
        double D, H, m, B1, B2, B3;
        r = state[0];
        if (r < 1e-10) {
            r = 1e-10;
            state[0] = 1e-10;
            rates[0] = 0;
            rates[1] = 0;
            rates[2] = 0;
            rates[3] = 0;
            rates[4] = 0;
            rates[5] = 1;
        } else {
            pR = state[2];
            pPhi = state[3];

            b = Math.sqrt(r * r + a * a);
            c = Math.sqrt(2 * r);
            rho = Math.sqrt(r * r);//in general rho depends on theta
            m = 1;
            D = Math.sqrt(((b * b - c * c) / (rho * rho) + b * b * c * c / (rho * rho * rho * rho)) * pR * pR + 2 * a * c * pR * pPhi / (b * rho * rho) + pPhi * pPhi / (b * b) + m * m);
            H = -b * c * pR / (rho * rho) + D;

            B1 = (c / b) * (1 + b * b / (2 * r * r) - 2 * b * b / (rho * rho));
            B2 = 1 - c * c / (2 * r * r) - (b * b - c * c) / (rho * rho) + b * c * B1 / (rho * rho);
            B3 = 1 - b * b / (2 * r * r) + 2 * b * b / (rho * rho);

            rates[0] = ((b * b - c * c) * pR - b * c * H + a * c * pPhi / b) / (rho * rho * D);
            rates[1] = (a * c * pR / b + (rho * rho * pPhi) / (b * b)) / (rho * rho * D);
            rates[2] = r * B1 * pR / (rho * rho) + (r / (D * rho * rho)) * (-B2 * pR * pR + a * c * B3 * pR * pPhi / (b * b * b) + rho * rho * pPhi * pPhi / (b * b * b * b));
            rates[3] = 0;
            rates[4] = 1.0 / D;
            rates[5] = 1;
        }
    }

    public double getRingAngle() {
        return 0;
    }

    public double getR() {
        return state[0];
    }

    public double getPhi() {
        return state[1];
    }

    public double getTau() {
        return state[4];
    }

    public double getT() {
        return state[5];
    }

    public void setT(double t) {
        state[5] = t;
    }

    public double getVmUpper(double r) {
        double a = ic.getA();
        double Lm = ic.getLm();
        double pPhi;
        double b, c;
        double m;
        pPhi = Lm;
        b = Math.sqrt(r * r + a * a);
        c = Math.sqrt(2 * r);
        m = 1;
        if (b * b - c * c < 0) {
            return 0; //r between inner and outer horizon...Vm is complex; we replace it by -Infinity
        } else {
            return (a * c * c * pPhi + Math.sqrt(b * b - c * c) * Math.sqrt((r * r * b * b + a * a * c * c) * (r * r * m * m) + r * r * r * r * pPhi * pPhi)) / (r * r * b * b + a * a * c * c);
        }
    }

    public double getVmLower(double r) {
        double a = ic.getA();
        double Lm = ic.getLm();
        double pPhi;
        double b, c;
        double m;
        pPhi = Lm;
        b = Math.sqrt(r * r + a * a);
        c = Math.sqrt(2 * r);
        m = 1;
        if (b * b - c * c < 0) {
            return 0; //r between inner and outer horizon...Vm is complex; we replace it by -Infinity
        } else {
            return (a * c * c * pPhi - Math.sqrt(b * b - c * c) * Math.sqrt((r * r * b * b + a * a * c * c) * (r * r * m * m) + r * r * r * r * pPhi * pPhi)) / (r * r * b * b + a * a * c * c);
        }
    }

    public double getVmAtHorizon() {
        double a = ic.getA();
        double Lm = ic.getLm();
        double r;
        double b, c;
        r = getRHorizon();
        b = Math.sqrt(r * r + a * a);
        c = Math.sqrt(2 * r);
        return a * c * c * Lm / (r * r * b * b + a * a * c * c);
    }

    @Override
    public String getOrbitInfo() {
        DecimalFormat format = new DecimalFormat("0.000");
        String sA = "J/M = ".concat(format.format(ic.getA())).concat(" M");
        String sEm = "E/m = ".concat(format.format(ic.getEm())).concat(" M");
        String sLm = "L/m = ".concat(format.format(ic.getLm())).concat(" M");
        String sDt = "dt = ".concat(format.format(getODESolver().getStepSize())).concat(" M");
        return sA + "   " + sEm + "   " + sLm + "   " + sDt;
    }

}

class InitialConditionsRainM extends InitialConditionsM {

    public InitialConditionsRainM(Orbit orbit, double a, double Em, double Lm, double r, double sign, double dt) {
        super(orbit, a, Em, Lm, r, sign, dt);
    }

    public InitialConditionsRainM(Orbit orbit, double a, double r, double v0, double theta0, double dt) {
        super(orbit, a, r, v0, theta0, dt);
    }

    /**
     * According to known values of initial r, phi, Em, Lm, and sign it adjusts
     * values of v0 and theta0.
     */
    public void adjustV0Theta0() {
        double Lm = getLm();
        double Em = getEm();
        double a = getA();
        double r = getR();
        double drdt = sign * ((r * (a * a + r * (r - 2))) * Math.sqrt((r * r * (2 - r + Em * Em * r) + Lm * Lm * (2 - r) - 4 * a * Em * Lm + a * a * (Em * Em * (2 + r) - r)) / (r * r * r))) / (Em * r * r * r - 2 * a * Lm + a * a * Em * (2 + r));
        double dphidt = (Lm * (r - 2) + 2 * a * Em) / (Em * r * r * r - 2 * a * Lm + a * a * Em * (2 + r));
        double v0 = Math.sqrt((drdt * drdt * r * (r * r * r + a * a * (2 + r))) / ((a * a + r * (r - 2)) * (a * a + r * (r - 2))) + (-2 * a + dphidt * r * r * r + a * a * dphidt * (2 + r)) * (-2 * a + dphidt * r * r * r + a * a * dphidt * (2 + r)) / (r * r * (a * a + r * (r - 2))));
        double v0SinTheta = (a * a + 2 * a * a / r + r * r) * (dphidt - (2 * a) / (r * (a * a + 2 * a * a / r + r * r))) / Math.sqrt(a * a - 2 * r + r * r);
        double v0CosTheta = drdt * Math.sqrt(1 + 2 * a * a / (r * r * r) + a * a / (r * r)) / (1 + a * a / (r * r) - 2 / r);
        double theta0;
        theta0 = Math.atan2(v0SinTheta, v0CosTheta);
        icData[4][1] = new Double(v0);
        icData[5][1] = new Double(Math.toDegrees(theta0));
    }

    /**
     * According to known values of initial r, phi, v0, theta0 it adjusts values
     * of Em, Lm, and sign.
     */
    public void adjustEmLmSign() {
        double v0 = getV0();
        if (v0 >= 1) {
            v0 = 0.99;
        }
        if (v0 < 0) {
            v0 = 0;
        }

        double theta0 = getTheta0();
        double a = getA();
        double r = getR();
        double drdt = (1 - 2 / r + a * a / (r * r)) / Math.sqrt(1 + a * a / (r * r) + 2 * a * a / (r * r * r)) * v0 * Math.cos(theta0);
        double dphidt = v0 * Math.sin(theta0) * Math.sqrt(a * a + r * r - 2 * r) / (r * r + a * a + 2 * a * a / r) + 2 * a / (r * (r * r + a * a + 2 * a * a / r));
        double dtaudt = Math.sqrt((1 - 2 / r) + 4 * a / r * dphidt - drdt * drdt / (1 - 2 / r + a * a / (r * r)) - (1 + a * a / (r * r) + 2 * a * a / (r * r * r)) * r * r * dphidt * dphidt);
        double Em = (1 - 2 / r) / dtaudt + 2 * a / r * dphidt / dtaudt;
        double Lm = (1 + a * a / (r * r) + 2 * a * a / (r * r * r)) * r * r * dphidt / dtaudt - 2 * a / r / dtaudt;

        if (Math.cos(theta0) > 0) {
            this.sign = 1;
        } else {
            this.sign = -1;
        }

        icData[1][1] = new Double(Em);
        icData[2][1] = new Double(Lm);
    }

    /**
     * Sets and computes initial conditions r, phi, pR, pPhi from given vlaues
     * of energy Em and angular momentum Lm.
     */
    public void computeInitialState() {
        double eps = 1e-10;
        double a = getA();
        double rHor = 1 + Math.sqrt(1 - a * a);
        double r = getR();
        double Lm = getLm();
        double Em = getEm();
        double phi = 0;

        if (Math.abs(r - rHor) < eps) {
            if (r > rHor) {
                r = rHor + eps;
            } else {
                r = rHor - eps;
            }
        }

        double pPhi;
        double b, c;
        double H, m, F;

        pPhi = Lm;

        b = Math.sqrt(r * r + a * a);
        c = Math.sqrt(2 * r);
        m = 1;
        H = Em;
        F = Math.pow((c / b) * (b * b * H - a * pPhi) / (b * b - c * c), 2) + ((b * b - a * a) * (H * H - m * m - pPhi * pPhi / (b * b))) / (b * b - c * c);

        orbit.state[0] = r;
        orbit.state[1] = phi;
        orbit.state[2] = (c / b) * (b * b * H - a * pPhi) / (b * b - c * c) + sign * Math.sqrt(F);
        orbit.state[3] = Lm;
        orbit.state[4] = 0;
        orbit.state[5] = 0;
    }

}
