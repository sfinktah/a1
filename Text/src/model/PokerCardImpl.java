package model;

import model.interfaces.PokerCard;

import java.util.Objects;

/**
 * PokerCard Implementation
 *
 * @see model.interfaces.PokerCard
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public final class PokerCardImpl implements PokerCard {
   public static final int NUM_RANKS = Value.values().length;

   /**
    * @implNote This implementation uses the integer `gIndex` to represent all information about a card
    */
   private int gIndex;
   private Value rank = null;
   private Suit suit = null;

   public PokerCardImpl() {
      this.gIndex = -1;
   }

   // NOTE: Constructor added per advice of A1 marker.
   public PokerCardImpl(Suit suit, Value rank) {
      this();
      this.suit = suit;
      this.rank = rank;
      this.gIndex = suit.ordinal() * NUM_RANKS + rank.ordinal();
   }

   public PokerCardImpl(Value rank, Suit suit) {
      this(suit, rank);
   }

   public PokerCardImpl(int index) {
      this(Suit.values()[index / NUM_RANKS], Value.values()[index % NUM_RANKS]);
      if (index < 0 || index >= DECK_SIZE) {
         throw new IllegalArgumentException("Invalid card index");
      }
   }

   /**
    * @param string to Titlecase
    *
    * @return "Titlecase" version of String (first letter capitalised, remainder lowercase)
    */
   private static String toTitleCase(String string) {
      // Don't throw if string is empty
      if (Objects.isNull(string) || string.isEmpty()) {
         return string;
      }
      return Character.toTitleCase(string.charAt(0)) + string.substring(1).toLowerCase();
   }


   /**
    * @param e an enum
    *
    * @return a "Titlecase" version of e.toString()
    */
   private String enumAsTitleCase(Enum<?> e) {
      return toTitleCase(e.toString());
   }

   /**
    * @return the score value of this card (Ace=11, J, Q, K=10, All others int of face value)
    */
   @Override
   public int getScore() {
      int rank = this.gIndex % NUM_RANKS;
      switch (rank) {
         case 0:
            return 8;
         case 1:
            return 9;
         case 6:
            return 11;
         default:
            return 10;
      }
   }

   /**
    * <pre>@return a human readable String that lists the values of this PokerCard
    *         instance (see OutputTrace.pdf)
    *
    *         e.g. "Suit: Clubs, Value: Five, Score: 5" for Five of Clubs
    *
    *  NOTE: Case MUST match as above</pre>
    */
   public String toString() {
      String result = "** Error **";
      if (valid()) {
         result = String.format("Suit: %s, Value: %s, Score: %d",
               enumAsTitleCase(getSuit()),
               enumAsTitleCase(getValue()),
               getScore());
      }
      return result;
   }

   /**
    * @param card - another card to compare with
    *
    * @return true - if the face value and suit is equal
    */
   @Override
   public boolean equals(PokerCard card) {
      return equals((Object) card);
   }

   /**
    * <pre><b>NOTE:</b> this is the java.lang.Object @Override
    *
    * the implementation should cast and call through to the type checked method above</pre>
    *
    * @param o - another card to compare with
    *
    * @return true - if the face value and suit is equal
    */
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }
      PokerCardImpl that = (PokerCardImpl) o;
      return rank == that.rank &&
            suit == that.suit;
   }

   @Override
   public int hashCode() {
      return Objects.hash(rank, suit);
   }

   /**
    * @return card validity
    */
   private boolean valid() {
      return this.gIndex >= 0 && this.gIndex < DECK_SIZE;
   }


   /**
    * @return the suit of this card as a {@link model.interfaces.PokerCard.Suit}
    */
   public Suit getSuit() {
      return suit;
   }

   /**
    * @return the rank of this card as a {@link model.interfaces.PokerCard.Value}
    */
   public Value getValue() {
      return rank;
   }
}
