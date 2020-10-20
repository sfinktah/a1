package util;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class Debug {

   private static final boolean DEBUG = false;

   public static void format(String format, Object... args) {
      println(String.format(format, args));
   }

   public static void println(String s) {
      if (DEBUG) {
         System.out.println(s);
      }
   }

   public static void println() {
      println("");
   }
}
