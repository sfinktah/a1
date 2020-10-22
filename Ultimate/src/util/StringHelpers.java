package util;

public class StringHelpers {
   /**
    * Integer.parseInt with fallback to default value on exception
    *
    * @param text         string containing int
    * @param defaultValue default int
    *
    * @return int
    */
   public static int stringAsInt(String text, int defaultValue) {
      try {
         return Integer.parseInt(safeString(text));
      } catch (NumberFormatException ignored) {
      }
      return defaultValue;
   }

   public static String safeString(Object value) {
      return value == null ? "" : value.toString();
   }

   public static boolean isNullOrEmpty(String s) {
      if (s == null) {
         return true;
      }
      return s.isEmpty();
   }

}
