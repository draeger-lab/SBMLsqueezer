package org.sbml.squeezer.math.jama;

public class Maths {

   /** sqrt(a^2 + b^2) without under/overflow. **/

   public static double hypot(double a, double b) {
      double r;
      double aa = Math.abs(a);
      double bb = Math.abs(b);
      if (aa > bb) {
         r = b/a;
         r = aa*Math.sqrt(1+r*r);
      } else if (b != 0) {
         r = a/b;
         r = bb*Math.sqrt(1+r*r);
      } else
         r = 0.0;
      return r;
   }
}
