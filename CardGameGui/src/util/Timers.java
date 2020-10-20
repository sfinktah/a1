package util;

public class Timers {
   /**
    * Causes the currently executing thread to sleep (temporarily cease
    * execution) for the specified number of milliseconds, subject to
    * the precision and accuracy of system timers and schedulers. The thread
    * does not lose ownership of any monitors.
    *
    * @param millis the length of time to sleep in milliseconds
    *
    * @throws IllegalArgumentException if the value of {@code millis} is negative
    */
   public static void wait(int millis) {
      try {
         Thread.sleep(millis);
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
      }
   }
}
