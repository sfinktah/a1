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
   public static int stringAsInt(Object text, int defaultValue) {
      try {
         return Integer.parseInt(text == null ? "" : String.valueOf(text));
      } catch (NumberFormatException ignored) {
      }
      return defaultValue;
   }

   public static String safeString(Object value) {
      return value == null ? "" : String.valueOf(value);
   }

}
