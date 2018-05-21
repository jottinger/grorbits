package org.opensourcephysics.tuleja.numerics;

/*
 * The org.opensourcephysics.numerics package contains numerical methods
 * for the book Simulations in Physics.
 * Copyright (c) 2001  H. Gould, J. Tobochnik, and W. Christian.
 */


/**
 * ODE defines a system of differential equations by providing access to the rate equations.
 *
 * @author       Wolfgang Christian
 */

public interface ODE {
  /**
   * Gets the state variables.
   *
   * The getState method is invoked by an ODESolver to obtain the initial state of the system.
   * The ODE solver advances the solution and then copies new values into the
   * state array at the end of the solution step.
   *
   * @return state  the state
   */
     public double[] getState();

  /**
   * Gets the rate of change using the argument's state variables.
   *
   * This method may be invoked many times with different intermediate states
   * as an ODESolver is carrying out the solution.
   *
   * @param state  the state array
   * @param rate   the rate array
   */
     public void getRate(double[] state, double[] rate );
}
