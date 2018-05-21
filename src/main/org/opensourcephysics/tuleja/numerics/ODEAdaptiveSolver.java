package org.opensourcephysics.tuleja.numerics;

/*
 * The org.opensourcephysics.numerics package contains numerical methods
 * for the book Simulations in Physics.
 * Copyright (c) 2001  H. Gould, J. Tobochnik, and W. Christian.
 */

 //package org.opensourcephysics.numerics;

/**
 * ODEAdaptiveSolver extends the ODE solver to add adaptive step size capabilities.
 *
 * Adaptive ODE solvers adjust the step size until that the desired tolerance is reached.
 *
 * @author       Wolfgang Christian
 */

public interface ODEAdaptiveSolver extends ODESolver{
  public void setTolerance(double tol);
  public double  getTolerance();
}
