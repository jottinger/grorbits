package grorbits;

import java.text.DecimalFormat;
import org.opensourcephysics.tuleja.numerics.*;

public class OrbitBoyerLindquistL extends Orbit {

    public OrbitBoyerLindquistL() {
        super();
        twoPotentials = true;
    }

    public void initializeVariables() {
        state = new double[]{10.0, 0.0, 0.0, 0.0, 0.0};
    //  ...initial state: {r,dr/dt,phi,tau,t}

        numPoints = 1500;
        ic = new InitialConditionsBoyerLindquistL(this, 0, 0.03, 20, -1, 1);//{a,invB,r,sign,dt}
        t = 0;
        orbitData = new Double[numPoints][4];
    }

    public void initialize(double a, double r, double v0, double theta0, double dt, int numPoints) {
        state = new double[]{10.0, 0.0, 0.0, 0.0, 0.0};
        //  ...initial state: {r,dr/dt,phi,tau,t}
        this.numPoints = numPoints;

        ic = new InitialConditionsBoyerLindquistL(this, a, r, theta0, dt);
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
        double b = 1 / ic.getInvB();
        double a = ic.getA();
        double r = state[0];
        double rates1 = -((a * a - 2 * r + r * r) * (2 * a * a * a * (a - b) * (a - b) * (a - b) + a * (a - b) * (a - b) * (3 * a * a * a + 4 * a - 4 * b) * r + a * (a - b) * (a * a * a * a + a * a * a * b - 4 * a * a + 14 * a * b - 10 * b * b) * r * r - 2 * a * b * (3 * a * a - 5 * a * b + 2 * b * b) * r * r * r + 2 * (a * a * a * a + 6 * a * b - 5 * b * b - a * a * (b * b + 1)) * r * r * r * r + ((-5) * a * a + 7 * b * b) * r * r * r * r * r + (a * a - b * b) * r * r * r * r * r * r - 2 * r * r * r * r * r * r * r)) / (r * r * Math.pow(2 * a * (a - b) + a * a * r + r * r * r, 3));;

        if ((r < getRHorizon() + 1e-2) && (rates1 < 1e-5)) {
            rates[0] = 0;
            rates[1] = 0;
        } else {
            rates[0] = state[1];
            rates[1] = rates1;
        }
        rates[2] = (2 * a - 2 * b + b * r) / (2 * a * a - 2 * a * b + a * a * r + r * r * r);
        rates[3] = 0;
        rates[4] = 1;
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
        return state[3];
    }

    public double getT() {
        return state[4];
    }

    public void setT(double t) {
        state[4] = t;
    }

    public double getVmUpper(double r) {
        double a = ic.getA();
        return (2 * a + Math.sqrt(r * r * (a * a + r * r - 2 * r))) / (r * r * r + a * a * (r + 2));
    }

    public double getVmLower(double r) {
        double a = ic.getA();
        return (2 * a - Math.sqrt(r * r * (a * a + r * r - 2 * r))) / (r * r * r + a * a * (r + 2));
    }

    public double getVmAtHorizon() {
        double a = ic.getA();
        return a / (2 * (1 + Math.sqrt(1 - a * a)));
    }

    @Override
    public String getOrbitInfo() {
        DecimalFormat format = new DecimalFormat("0.000");
        String sA = "J/M = ".concat(format.format(ic.getA())).concat(" M");
        String sInvB = "M/b = ".concat(format.format(ic.getInvB()));
        String sDt = "dt = ".concat(format.format(getODESolver().getStepSize())).concat(" M");
        return sA + "   " + sInvB + "   " + sDt;
    }

}

class InitialConditionsBoyerLindquistL extends InitialConditionsL {

    public InitialConditionsBoyerLindquistL(Orbit orbit, double a, double invB, double r, double sign, double dt) {
        super(orbit, a, invB, r, sign, dt);
    }

    public InitialConditionsBoyerLindquistL(Orbit orbit, double a, double r, double theta0, double dt) {
        super(orbit, a, r, theta0, dt);
    }

    /**
     * According to known values of initial r, phi, invB, and sign it adjusts
     * value theta0
     *
     */
    public void adjustV0Theta0() {
        double invB = getInvB();
        double b = 1 / invB;
        double a = getA();
        double r = getR();
        double drdt = sign * Math.sqrt((Math.pow(a * a + r * r - 2 * r, 2) * (r * r * r - 4 * a * b + b * b * (2 - r) + a * a * (2 + r))) / (r * Math.pow(r * r * r - 2 * a * b + a * a * (2 + r), 2)));
        double dphidt = (2 * a - 2 * b + b * r) / (2 * a * a - 2 * a * b + a * a * r + r * r * r);
        double v0SinTheta = (a * a + 2 * a * a / r + r * r) * (dphidt - (2 * a) / (r * (a * a + 2 * a * a / r + r * r))) / Math.sqrt(a * a - 2 * r + r * r);
        double x = 1 + 2 * a * a / (r * r * r) + a * a / (r * r);
        double sqrtx;
        if (x < 0) {
            sqrtx = 0;
        } else {
            sqrtx = Math.sqrt(x);
        }
        double v0CosTheta = drdt * sqrtx / (1 + a * a / (r * r) - 2 / r);
        double theta0;
        theta0 = Math.atan2(v0SinTheta, v0CosTheta);
        icData[3][1] = new Double(Math.toDegrees(theta0));
    }

    /**
     * According to known values of initial r, phi, theta0 it adjusts values of
     * invB and sign
     *
     */
    public void adjustEmLmSign() {
        double theta0 = getTheta0();
        double PI = Math.PI;
        double epsTh = 1.745329252e-3;
        if ((theta0 > PI - epsTh) && (theta0 <= PI)) {
            theta0 = PI - epsTh;
        }
        if ((theta0 < -PI + epsTh) && (theta0 >= -PI)) {
            theta0 = -PI + epsTh;
        }
        if ((theta0 < epsTh) && (theta0 >= 0)) {
            theta0 = epsTh;
        }
        if ((theta0 > -epsTh) && (theta0 <= 0)) {
            theta0 = -epsTh;
        }
        icData[3][1] = new Double(Math.toDegrees(theta0));

        double a = getA();
        double r = getR();
        double dphidt = Math.sin(theta0) * Math.sqrt(a * a + r * r - 2 * r) / (r * r + a * a + 2 * a * a / r) + 2 * a / (r * (r * r + a * a + 2 * a * a / r));
        double invB = (2 * a * dphidt + r - 2) / (dphidt * (r * r * r + a * a * (2 + r)) - 2 * a);

        if (Math.cos(theta0) > 0) {
            this.sign = 1;
        } else {
            this.sign = -1;
        }
        icData[1][1] = new Double(1 / invB);//!!!changed

    }

    public void computeInitialState() {
    //sets and computes initial conditions r, dr/dt, phi, dphi/dt from
        //given vlaue of invB
        double eps = 1e-10;
        double a = getA();
        double rHor = 1 + Math.sqrt(1 - a * a);
        double r = getR();
        double phi = 0;
        double b = 1 / getInvB();

        if (Math.abs(r - rHor) < eps) {
            if (r > rHor) {
                r = rHor + eps;
            } else {
                r = rHor - eps;
            }
        }

        orbit.state[0] = r;
        orbit.state[1] = sign * Math.sqrt((Math.pow(a * a + r * r - 2 * r, 2) * (r * r * r - 4 * a * b + b * b * (2 - r) + a * a * (2 + r))) / (r * Math.pow(r * r * r - 2 * a * b + a * a * (2 + r), 2)));
        orbit.state[2] = phi;
        orbit.state[3] = 0;
        orbit.state[4] = 0;

    }

}
