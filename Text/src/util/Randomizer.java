package util;

// We need to use secure random numbers for gambling :)

import java.security.SecureRandom;
import java.util.Objects;

@SuppressWarnings({"unused", "serial", "RedundantSuppression"})
public class Randomizer extends SecureRandom {
   private static Randomizer staticRand = null;

   private Randomizer() {
      super();
   }

   public static synchronized Randomizer getRandomizer() {
      return Objects.nonNull(staticRand) ? staticRand : (staticRand = new Randomizer());
   }

   @SuppressWarnings("EmptyMethod")
   public int nextInt() { return super.nextInt(); }
}
