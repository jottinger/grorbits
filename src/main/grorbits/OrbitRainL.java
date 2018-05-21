package grorbits;

import java.text.DecimalFormat;
import org.opensourcephysics.tuleja.numerics.*;

public class OrbitRainL extends Orbit {

    public OrbitRainL() {
        super();
        twoPotentials = true;
    }

    public void initializeVariables() {
        state = new double[]{10.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    //...initial state: {r,phi,pR,pPhi,tau,t}

        numPoints = 1500;
        ic = new InitialConditionsRainL(this, 0, 0.03, 20, -1, 1);//{a,invB,r,sign,dt}
        t = 0;
        orbitData = new Double[numPoints][4];
    }

    public void initialize(double a, double r, double v0, double theta0, double dt, int numPoints) {
        state = new double[]{10.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    //...initial state: {r,phi,pR,pPhi,tau,t}

        this.numPoints = numPoints;

        ic = new InitialConditionsRainL(this, a, r, theta0, dt);
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
        if (r < 1e-5) {
            r = 1e-5;
            state[0] = 1e-5;
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
            m = 0;
            D = Math.sqrt(((b * b - c * c) / (rho * rho) + b * b * c * c / (rho * rho * rho * rho)) * pR * pR + 2 * a * c * pR * pPhi / (b * rho * rho) + pPhi * pPhi / (b * b) + m * m);
            H = -b * c * pR / (rho * rho) + D;

            B1 = (c / b) * (1 + b * b / (2 * r * r) - 2 * b * b / (rho * rho));
            B2 = 1 - c * c / (2 * r * r) - (b * b - c * c) / (rho * rho) + b * c * B1 / (rho * rho);
            B3 = 1 - b * b / (2 * r * r) + 2 * b * b / (rho * rho);

            rates[0] = ((b * b - c * c) * pR - b * c * H + a * c * pPhi / b) / (rho * rho * D);
            rates[1] = (a * c * pR / b + (rho * rho * pPhi) / (b * b)) / (rho * rho * D);
            rates[2] = r * B1 * pR / (rho * rho) + (r / (D * rho * rho)) * (-B2 * pR * pR + a * c * B3 * pR * pPhi / (b * b * b) + rho * rho * pPhi * pPhi / (b * b * b * b));
            rates[3] = 0;
            rates[4] = 0;//1.0/D;
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
        return (2 * a + Math.sqrt(r * r * (a * a + r * r - 2 * r))) / (r * r * r + a * a * (r + 2));
    }

    public double getVmLower(double r) {
        double a = ic.getA();
        return (2 * a - Math.sqrt(r * r * (a * a + r * r - 2 * r))) / (r * r * r + a * a * (r + 2));
    }

    public double getVmAtHorizon() {
        double a = ic.getA();
        double invB = ic.getInvB();
        double r;
        double b, c;
        double H;
        H = 1;
        r = getRHorizon();
        b = Math.sqrt(r * r + a * a);
        c = Math.sqrt(2 * r);
        return (H / invB) * a * c * c / (r * r * b * b + a * a * c * c);
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

class InitialConditionsRainL extends InitialConditionsL {

    public InitialConditionsRainL(Orbit orbit, double a, double invB, double r, double sign, double dt) {
        super(orbit, a, invB, r, sign, dt);
    }

    public InitialConditionsRainL(Orbit orbit, double a, double r, double theta0, double dt) {
        super(orbit, a, r, theta0, dt);
    }

    /**
     * According to known values of initial r, phi, invB, and sign it adjusts
     * value theta0.
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
     * invB and sign.
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
        icData[1][1] = new Double(1 / invB); //changed!!!

    }

    /**
     * Sets and computes initial conditions r, phi, pR, pPhi from given vlaues
     * of energy Em and angular momentum Lm.
     */
    public void computeInitialState() {
    //sets and computes initial conditions r, phi, pR, pPhi from
        //given vlaue of invB
        double eps = 1e-10;
        double a = getA();
        double rHor = 1 + Math.sqrt(1 - a * a);
        double r = getR();
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

        double H = 1, m, F;//we set H=1

    //double eps=1e-10;
        //if(Math.abs(r-getRHorizon())<eps)r=getRHorizon()+eps;
        pPhi = H / getInvB();

        b = Math.sqrt(r * r + a * a);//b here IS NOT an impact parameter
        c = Math.sqrt(2 * r);
        m = 0;
        F = Math.pow((c / b) * (b * b * H - a * pPhi) / (b * b - c * c), 2) + ((b * b - a * a) * (H * H - m * m - pPhi * pPhi / (b * b))) / (b * b - c * c);
        orbit.state[0] = r;
        orbit.state[1] = phi;
        orbit.state[2] = (c / b) * (b * b * H - a * pPhi) / (b * b - c * c) + sign * Math.sqrt(F);
        orbit.state[3] = pPhi;
        orbit.state[4] = 0;
        orbit.state[5] = 0;
    }

}
