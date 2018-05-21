package grorbits;

import java.text.DecimalFormat;
import org.opensourcephysics.tuleja.numerics.*;

public class OrbitNewton extends Orbit {

    public OrbitNewton() {
        super();
        twoPotentials = false;
    }

    public void initializeVariables() {
        state = new double[]{10.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    //  ...initial state: {r,dr/dt,phi,dphi/dt,tau,t}

        numPoints = 1500;
        ic = new InitialConditionsNewton(this, 0, -0.05, 4, 20, -1, 1);//{a,Em,Lm,r,sign,dt}
        t = 0;
        orbitData = new Double[numPoints][4];
    }

    public void initialize(double a, double r, double v0, double theta0, double dt, int numPoints) {
        state = new double[]{10.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    //  ...initial state: {r,dr/dt,phi,dphi/dt,tau,t}

        this.numPoints = numPoints;

        ic = new InitialConditionsNewton(this, a, r, v0, theta0, dt);
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
        rates[0] = state[1];
        rates[1] = state[0] * state[3] * state[3] - 1.0 / (state[0] * state[0]);
        rates[2] = state[3];
        rates[3] = -2.0 / state[0] * state[1] * state[3];
        rates[4] = 1;
        rates[5] = 1;
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

    public double getRHorizon() {
        return 1e-5;
    }

    public double getRInnerHorizon() {
        return 0;
    }

    public double rTorPlot(double r) {
        return r;
    }

    public double rPlotTor(double rPlot) {
        return rPlot;
    }

    public double getVmUpper(double r) {
        double Lm = ic.getLm();
        return -1.0 / r + (Lm * Lm) / (2.0 * r * r);
    }

    public double getVmLower(double r) {//the same as getVm()
        double Lm = ic.getLm();
        return -1.0 / r + (Lm * Lm) / (2.0 * r * r);
    }

    public double getVmAtHorizon() {
        return getVmUpper(2);//instead of Infinity
    }

    public double getRingAngle() {
        return 0;
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

class InitialConditionsNewton extends InitialConditionsM {

    public InitialConditionsNewton(Orbit orbit, double a, double Em, double Lm, double r, double sign, double dt) {
        super(orbit, a, Em, Lm, r, sign, dt);
    }

    public InitialConditionsNewton(Orbit orbit, double a, double r, double v0, double theta0, double dt) {
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
        double r = getR();
        double v0 = Math.sqrt(2 * (Em + 1 / r));
        double sinTheta = Lm / (r * v0);
        double theta0;
        theta0 = Math.asin(sinTheta);
        if (this.sign < 0) {
            theta0 = Math.PI - theta0;
        }
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

        double theta0 = getTheta0();
        double r = getR();

        double dphidt = v0 * Math.sin(theta0) / r;
        double Em = 0.5 * (v0 * v0) - 1 / r;
        double Lm = dphidt * r * r;

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
        double r = getR();
        double Lm = getLm();
        double Em = getEm();
        double phi = 0;

        orbit.state[0] = r;
        orbit.state[1] = sign * Math.sqrt(2.0 * Em - Lm * Lm / (r * r) + 2.0 / r);
        orbit.state[2] = phi;
        orbit.state[3] = Lm / (r * r);
        orbit.state[4] = 0;
        orbit.state[5] = 0;
    }

}
