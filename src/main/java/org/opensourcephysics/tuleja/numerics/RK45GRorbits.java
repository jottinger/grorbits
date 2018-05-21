package org.opensourcephysics.tuleja.numerics;

/*
 * The org.opensourcephysics.numerics package contains numerical methods
 * for the book Simulations in Physics.
 * Copyright (c) 2001  H. Gould, J. Tobochnik, and W. Christian.
 */



/**
 *February 9, 2003
 *Slavomir Tuleja: I have enabled the method step() to throw the InsufficientConvergenceException
 */

/**
 * Title:        RK45
 * Description:  RK4/5 ODE solver with variable step size.
 * @author       Wolfgang Christian
 * @version 1.0
 */

public class RK45GRorbits implements ODEAdaptiveSolver {
  static final double[][] b        = {
    {2.0 / 9.0}, {1.0 / 12.0, 1.0 / 4.0}, {69.0 / 128.0, -243.0 / 128.0, 135.0 / 64.0},
    {-17.0 / 12.0, 27.0 / 4.0, -27.0 / 5.0, 16.0 / 15.0},
    {65.0 / 432.0, -5.0 / 16.0, 13.0 / 16.0, 4.0 / 27.0, 5.0 / 144.0}
  };
  static final double[]   ch       = {47.0 / 450.0, 0.0, 12.0 / 25.0, 32.0 / 225.0, 1.0 / 30.0, 6.0 / 25.0};
  static final double[]   ct       = {-1.0 / 150.0, 0.0, 3.0 / 100.0, -16.0 / 75.0, -1.0 / 20.0, 6.0 / 25.0};
  private double          stepSize = 0.01;
  private int             numEqn   = 0;
  private double[]        tempState;
  private double[][]      f;
  private double          truncErr;
  private double          err;
  private ODE             ode;
  protected double        tol = 1.0e-6;
  int maxNumIt=20; //maximum number of iterations to achieve a given precission
  
  
  /**
   * Constructs the RK45 ODESolver for a system of ordinary  differential equations.
   *
   * @param _ode the system of differential equations.
   */
  public RK45GRorbits(ODE _ode) {
    ode = _ode;
    initialize(stepSize);
  }

  /**
   * Initializes the ODE solver.
   *
   * Temporary state and rate arrays are allocated.
   * The number of differential equations is determined by invoking getState().length on the ODE.
   *
   * @param _stepSize
   */
  public void initialize(double _stepSize) {
    stepSize = _stepSize;
    double state[] = ode.getState();
    if(state == null) {  // state vector not defined.
      return;
    }
    if(numEqn != state.length) {
      numEqn    = state.length;
      tempState = new double[numEqn];
      f         = new double[6][numEqn];  // the six intermediate rates
    }
  }

  /**
   * Steps (advances) the differential equations by the stepSize.
   *
   * The ODESolver invokes the ODE's getRate method to obtain the initial state of the system.
   * The ODESolver then advances the solution and copies the new state into the
   * state array at the end of the solution step.
   *
   * @return the step size
   */
  public double step() throws InsufficientConvergenceException {
    double state[] = ode.getState();
    if(state.length != numEqn) {
      initialize(stepSize);
    }
    int    i, j, k;  // counters
    double currentStep = stepSize;
    System.arraycopy(state, 0, tempState, 0, numEqn);  // save the initial state
    ode.getRate(state, f[0]);                          // get the initial rates
    
    int numberOfIterations=0;
    do {
      currentStep = stepSize;
      for(k = 1; k < 6; k++) {
        for(i = 0; i < numEqn; i++) {
          state[i] = tempState[i];                 // reset to the initial state
          for(j = 0; j < k; j++) {
            state[i] = state[i] + stepSize * b[k - 1][j] * f[j][i];
          }
        }
        ode.getRate(state, f[k]);                  // get the intermediate rates
      }
      err = 0;
      for(i = 0; i < numEqn; i++) {
        state[i] = tempState[i];                   // reset the initial state
        truncErr = 0;
        for(k = 0; k < 6; k++) {
          state[i] += stepSize * ch[k] * f[k][i];  // step the inital state
          truncErr += stepSize * ct[k] * f[k][i];  // estimate the error
        }
        truncErr = Math.abs(truncErr);
        if(err < truncErr) {
          err = truncErr;                          // find the maximum error
        }
      }
      if(err <= Double.MIN_VALUE) {
        err = tol / 1.0e5;                         // force the stepsize to grow by 10
      }
      // find h step for the next try.
      if(err > tol) {                              // shrink if error is too large
        stepSize = 0.9 * stepSize * Math.pow(tol / err, 0.25);
      } else if(err < 2.0 * tol) {                 //grow if the error is too small
        stepSize = 0.9 * stepSize * Math.pow(tol / err, 0.2);
      }
      numberOfIterations++;
    } while((err > tol)&&(numberOfIterations<maxNumIt));
    
    //Added by Slavo Tuleja
    if(numberOfIterations==maxNumIt){
      throw new InsufficientConvergenceException();
    }
    return currentStep;  // the value of the step that was actually taken.
  }

  /**
   * Sets the step size.
   *
   * The step size may change when the step method is invoked.
   *
   * @param _stepSize
   */
  public void setStepSize(double _stepSize) {
    stepSize = _stepSize;
  }

  /**
   * Gets the step size.
   *
   * The stepsize is adaptive and may change as the step() method is invoked.
   *
   * @return the step size
   */
  public double getStepSize() {
    return stepSize;
  }

  /**
   * Method setTolerance
   *
   * @param _tol
   */
  public void setTolerance(double _tol) {
    tol = Math.abs(_tol);
  }

  /**
   * Method getTolerance
   *
   *
   * @return
   */
  public double getTolerance() {
    return tol;
  }
}
