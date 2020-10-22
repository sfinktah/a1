package util;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class Debug {

   public static boolean DEBUG = true;

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
