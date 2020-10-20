package util;

// @formatter:off
public class MyMath {
   /**
    * Simple utility function which clamps the given value to be strictly
    * between the min and max values (inclusive).  Similar to
    * {@code com.sun.javafx.util.Utils#clamp}
    * but with a different argument order, and expressed as a generic method.
    * <p>
    * <i>Note: we're not supposed to be using javafx :p</i>
    * <p>
    * My original C++ implementation:
    * <pre>{@code
    *   template <typename T>
    *   constexpr const T& clamp(const T& val, const T& lo, const T& hi) {
    *      return (val < lo) ? lo : (val > hi) ? hi : val;
    *   }
    * }</pre>
    * <p>
    *
    * @param val value to clamp
    * @param lo lower limit (inclusive)
    * @param hi higher limit
    */
   public static <T extends Comparable<T>> T MyClamp(T val, T lo, T hi) {
      return
            val.compareTo(lo) < 0 ? lo :
            val.compareTo(hi) > 0 ? hi : val;
   }
}
