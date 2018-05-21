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
 * ODE defines a minimal differential equation solver.
 * @author       Wolfgang Christian
 */
public interface ODESolver {

  /**
   * Initializes the ODE solver.
   *
   * ODE solvers use this method to allocate temporary arrays that may be required to carry out the solution.
   * The number of differential equations is determined by invoking getState().length on the ODE.
   *
   * @param stepSize
   */
  public void initialize(double stepSize);

  /**
   * Steps (advances) the differential equations by the stepSize.
   *
   * The ODESolver invokes the ODE's getRate method to obtain the initial state of the system.
   * The ODESolver then advances the solution and copies the new state into the
   * state array at the end of the solution step.
   *
   * @return the step size
   */
  public double step() throws InsufficientConvergenceException;

  /**
   * Sets the initial step size.
   *
   * The step size may change if the ODE solver implements an adaptive step size algorithm
   * such as RK4/5.
   *
   * @param stepSize
   */
  public void setStepSize(double stepSize);

  /**
   * Gets the step size.
   *
   * @return the step size
   */
  public double getStepSize();
}
