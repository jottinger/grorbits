package org.opensourcephysics.tuleja.numerics;

/**
 * Exception created when RK45 does not converge...
 */

public class InsufficientConvergenceException extends Exception {
   public InsufficientConvergenceException(){
      super("Numerical method failed to converge ...");
   }
}
