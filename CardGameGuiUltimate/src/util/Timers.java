package util;

import java.util.function.Supplier;

public class Timers {
   /**
    * Creates and returns a new debounced version of the passed function which will postpone its execution until
    * after wait milliseconds have elapsed since the last time it was invoked. Useful for implementing behavior that
    * should only happen after the input has stopped arriving. For example: rendering a preview of a Markdown comment,
    * recalculating a layout after the window has stopped being resized, and so on.
    * <p>
    * At the end of the wait interval, the function will be called with the arguments that were passed most
    * recently to the debounced function.
    *
    * <p>Example usage:</p>
    * <code>
    * if (debouncedUpdateScaledInstance == null) {
    * final PlayingCardPanel self = this;
    * Supplier<Void> updateScaledInstance = new Supplier<Void>() {
    * public Void get() {
    * if (!lastRenderedCardSize.equals(getSize())) {
    * lastRenderedCardSize = getSize();
    * lastBufferedCardImage = getScaledInstance(bufferedCardImage, getWidth(), getHeight(), RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    * }
    * self.repaint();
    * return null;
    * }
    * };
    * debouncedUpdateScaledInstance = debounce(updateScaledInstance, 300);
    * }
    * debouncedUpdateScaledInstance.get();
    * </code>
    *
    * @param function Supplier<T> function to call after delay
    * @param delay    milliseconds
    * @param <T>      return type
    *
    * @return a Supplier<> you can .get()</>
    */
   public static <T> Supplier<T> debounce(final Supplier<T> function, final int delay) {
      return new Supplier<T>() {
         private java.util.concurrent.ScheduledFuture<T> timeout;

         @Override
         public T get() {
            clearTimeout(timeout);
            timeout = delay(function, delay);
            return null;
         }
      };
   }

   public static <T> java.util.concurrent.ScheduledFuture<T> delay(final Supplier<T> function,
                                                                   final int delay) {
      final java.util.concurrent.ScheduledExecutorService scheduler =
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
      final java.util.concurrent.ScheduledFuture<T> future = scheduler.schedule(
            function::get, delay, java.util.concurrent.TimeUnit.MILLISECONDS);
      scheduler.shutdown();
      return future;
   }

   public static void clearTimeout(java.util.concurrent.ScheduledFuture<?> scheduledFuture) {
      if (scheduledFuture != null) {
         scheduledFuture.cancel(true);
      }
   }

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
