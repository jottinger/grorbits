package org.opensourcephysics.tuleja.numerics;

/**
 *February 9, 2003
 *Slavomir Tuleja: I have removed maxmessages variable and also System.out.printlns
 *I have replaced it with throwing InsufficientConvergenceException
 */

/**
 * Title:        RK45MultiStep
 * Description:  Perform multiple RK4/5 ODE steps so that a uniform step size is maintained
 *
 * @author       Wolfgang Christian
 * @version 1.0
 */

public class RK45GRorbitsMultiStep extends RK45GRorbits implements ODEAdaptiveSolver {

  private double     fixedStepSize = 0.1;
  private int maxNumIt=20; 
  
  /**
   * Constructs the RK45MultiStep ODESolver for a system of ordinary  differential equations.
   *
   * @param _ode the system of differential equations.
   */
  public RK45GRorbitsMultiStep(ODE _ode) {
    super(_ode);
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
     if(fixedStepSize > 0) {
        return fixedStepSize - plus();
     } else {
        return fixedStepSize - minus();
     }
  }


  /**
   * Steps the ode with a postive stepsize.
   *
   * @return the step size
   */
  private double plus() throws InsufficientConvergenceException {  // positive step size
    double remainder = fixedStepSize;  // dt will keep track of the remaining time
    if((super.getStepSize() <= 0) || (                            // is the stepsize postive?
        super.getStepSize() > fixedStepSize) || (                 //is the stepsize larger than what is requested?
            fixedStepSize - super.getStepSize() == fixedStepSize  // is the stepsize smaller than the precision?
                )) {
      super.setStepSize(fixedStepSize);                           // reset the step size and let it adapt to an optimum size
    }
    
    int numberOfIterations=0;
    while(remainder > tol * fixedStepSize) {           // check to see if we are close enough
      double oldRemainder = remainder;
      if(remainder < super.getStepSize()) {            // temporarily reduce the step size so that we hit the exact dt value
        double tempStep = super.getStepSize();         // save the current optimum step size
        super.setStepSize(remainder);                  // set the RK4/5 step size to the remainder
        double delta = super.step();
        remainder -= delta;
        super.setStepSize(Math.min(tempStep, delta));  // restore the optimum step size
      } else {
        remainder -= super.step();                     // do a rk45 step and set the remainder
      }
      // check to see if roundoff error prevents further calculation.
      if((Math.abs(oldRemainder - remainder) <= 100 * Double.MIN_VALUE)
          || (tol * fixedStepSize / 100.0 > super.getStepSize())) {
        //Added by Slavomir Tuleja
        throw new InsufficientConvergenceException();
      }
      
      //Added by Slavo Tuleja
      numberOfIterations++;
      if(numberOfIterations==maxNumIt){
        throw new InsufficientConvergenceException();
      }
    }
    return remainder;
  }

  /**
   * Steps the ode with a negative stepsize.
   *
   * @return the step size
   */
  private double minus() throws InsufficientConvergenceException {  // negative step size
    double remainder = fixedStepSize;  // dt will keep track of the remaining time
    if((super.getStepSize() >= 0) || (                            // is the step negative?
        super.getStepSize() < fixedStepSize) || (                 // is the stepsize larger than what is requested?
            fixedStepSize - super.getStepSize() == fixedStepSize  // is the stepsize smaller than the precision?
                )) {
      super.setStepSize(fixedStepSize);                           // reset the step size and let it adapt to an optimum size
    }
    
    int numberOfIterations=0;
    while(remainder < tol * fixedStepSize) {           // check to see if we are close enough
      double oldRemainder = remainder;
      if(remainder > super.getStepSize()) {
        double tempStep = super.getStepSize();         // save the current optimum step size
        super.setStepSize(remainder);                  // set the step RK4/5 size to the remainder
        double delta = super.step();
        remainder -= delta;
        super.setStepSize(Math.max(tempStep, delta));  // restore the optimum step size
      } else {
        remainder -= super.step();                     // do a rk45 step and set the remainder
      }
      // check to see if roundoff error prevents further calculation.
      if((Math.abs(oldRemainder - remainder) <= 100 * Double.MIN_VALUE)
          || (tol * fixedStepSize / 100.0 < super.getStepSize())) {
        //Added by Slavomir Tuleja
        throw new InsufficientConvergenceException();
      }
      
      //Added by Slavo Tuleja
      numberOfIterations++;
      if(numberOfIterations==maxNumIt){
        throw new InsufficientConvergenceException();
      }
    }
    return remainder;
  }

  /**
   * Initializes the ODE solver.
   *
   * Temporary state and rate arrays are allocated by invoking the superclass method.
   *
   * @param stepSize
   */
  public void initialize(double stepSize) {
    fixedStepSize = stepSize;
    super.initialize(stepSize);
  }

  /**
   * Method setStepSize
   *
   * @param _stepSize
   */
  public void setStepSize(double _stepSize) {
    fixedStepSize = _stepSize;  // the fixed step size
    super.setStepSize(_stepSize);  // the variable step size
  }

  /**
   * Gets the step size.
   *
   * The step size is the fixed step size, not the size of the RK4/5 steps that are combined into a single step.
   *
   * @return the step size
   */
  public double getStepSize() {
    return fixedStepSize;
  }
}
