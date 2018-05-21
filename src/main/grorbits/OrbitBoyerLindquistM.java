package grorbits;

import java.text.DecimalFormat;
import org.opensourcephysics.tuleja.numerics.*;

public class OrbitBoyerLindquistM extends Orbit {

    public OrbitBoyerLindquistM() {
        super();
        twoPotentials = true;
    }

    public void initializeVariables() {
        state = new double[]{10.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    //  ...initial state: {r,dr/dt,phi,dphi/dt,tau,t}

        numPoints = 1500;
        ic = new InitialConditionsBoyerLindquistM(this, 0, 0.95, 4, 20, -1, 1);//{a,Em,Lm,r,sign,dt}
        t = 0;
        orbitData = new Double[numPoints][4];
    }

    public void initialize(double a, double r, double v0, double theta0, double dt, int numPoints) {
        state = new double[]{10.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        //  ...initial state: {r,dr/dt,phi,dphi/dt,tau,t}
        this.numPoints = numPoints;

        ic = new InitialConditionsBoyerLindquistM(this, a, r, v0, theta0, dt);
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
        //  Lagrange eqns
        double a = ic.getA();
        double dtaudt;
        rates[0] = state[1];
        rates[1] = (((a * a - 2 * state[0] + state[0] * state[0]) * (-1 + 2 * a * state[3] + state[3] * state[3] * (state[0] * state[0] * state[0] - a * a))) / (state[0] * state[0] * state[0] * state[0])) - ((state[1] * state[1] * (-2 * a * a + 2 * a * a * a * state[3] + a * a * state[0] - 3 * state[0] * state[0] + 6 * a * state[3] * state[0] * state[0])) / (state[0] * state[0] * (a * a - 2 * state[0] + state[0] * state[0])));
        rates[2] = state[3];
        rates[3] = -(2 * state[1] * (a - 2 * a * a * state[3] + a * a * a * state[3] * state[3] - 3 * state[3] * state[0] * state[0] + 3 * a * state[3] * state[3] * state[0] * state[0] + state[3] * state[0] * state[0] * state[0])) / (state[0] * state[0] * (a * a - 2 * state[0] + state[0] * state[0]));
        dtaudt = (1 - 2 / state[0]) + (4 * a * state[3]) / (state[0]) - (state[1] * state[1]) / (1 - 2 / state[0] + (a * a) / (state[0] * state[0])) - (1 + (a * a) / (state[0] * state[0]) + (2 * a * a) / (state[0] * state[0] * state[0])) * state[0] * state[0] * state[3] * state[3];
        if (dtaudt >= 0) {
            rates[4] = Math.sqrt(dtaudt);
        } else {
            rates[4] = 0;
        }
        rates[5] = 1;
    }

    public double getRingAngle() {
        double r = ic.getR();
        double t = getT();
        double a = ic.getA();
        return 2 * a / (r * (r * r + a * a + 2 * a * a / r)) * t;
    }

    public double getR() {
        return state[0];
    }

    public double getPhi() {
        return state[2];
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
        return ((2 * a * Lm) + Math.sqrt(r * (a * a - 2 * r + r * r) * (r * r * r + a * a * (r + 2) + r * Lm * Lm))) / (r * r * r + a * a * (r + 2));
    }

    public double getVmLower(double r) {
        double a = ic.getA();
        double Lm = ic.getLm();
        return ((2 * a * Lm) - Math.sqrt(r * (a * a - 2 * r + r * r) * (r * r * r + a * a * (r + 2) + r * Lm * Lm))) / (r * r * r + a * a * (r + 2));
    }

    public double getVmAtHorizon() {
        double a = ic.getA();
        double Lm = ic.getLm();
        return a * Lm / (2.0 * (1 + Math.sqrt(1 - a * a)));
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

class InitialConditionsBoyerLindquistM extends InitialConditionsM {

    public InitialConditionsBoyerLindquistM(Orbit orbit, double a, double Em, double Lm, double r, double sign, double dt) {
        super(orbit, a, Em, Lm, r, sign, dt);
    }

    public InitialConditionsBoyerLindquistM(Orbit orbit, double a, double r, double v0, double theta0, double dt) {
        super(orbit, a, r, v0, theta0, dt);
    }

    /**
     * According to known values of initial r, phi, Em, Lm, and sign it adjusts
     * values of v0 and theta0
     *
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
     * of Em, Lm, and sign
     *
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

    public void computeInitialState() {
    //sets and computes initial conditions r, dr/dt, phi, dphi/dt from
        //given vlaues of energy Em and angular momentum Lm
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

        orbit.state[0] = r;
        orbit.state[1] = sign * ((r * (a * a + r * (r - 2))) * Math.sqrt((r * r * (2 - r + Em * Em * r) + Lm * Lm * (2 - r) - 4 * a * Em * Lm + a * a * (Em * Em * (2 + r) - r)) / (r * r * r))) / (Em * r * r * r - 2 * a * Lm + a * a * Em * (2 + r));
        orbit.state[2] = phi;
        orbit.state[3] = (Lm * (r - 2) + 2 * a * Em) / (Em * r * r * r - 2 * a * Lm + a * a * Em * (2 + r));
        orbit.state[4] = 0;
        orbit.state[5] = 0;
    }

}
