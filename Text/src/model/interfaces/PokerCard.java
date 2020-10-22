package model.interfaces;

/**
 * <pre>Assignment interface for FP representing a PokerCard
 * (setting values is handled by the implementing class constructor(s))
 *
 * @author Caspar Ryan
 * </pre>
 */
public interface PokerCard {
   /**
    * 28 cards as specified above
    */
   int DECK_SIZE = Suit.values().length * Value.values().length;

   /**
    * @return the suit of this card based on {@link Suit}
    */
   Suit getSuit();

   /**
    * @return the face value of this card based on {@link Value}
    */
   Value getValue();

   /**
    * @return the score value of this card (Ace=11, J, Q, K=10, All others int of face value)
    */
   int getScore();

   /**
    * <pre>@return a human readable String that lists the values of this PokerCard
    *         instance (see OutputTrace.pdf)
    *
    *         e.g. "Suit: Clubs, Value: Five, Score: 5" for Five of Clubs
    *
    *  NOTE: Case MUST match as above</pre>
    */
   @Override
   String toString();

   /**
    * @param card - another card to compare with
    *
    * @return true - if the face value and suit is equal
    */
   @SuppressWarnings({"unused", "RedundantSuppression"})
   boolean equals(PokerCard card);

   /**
    * <pre><b>NOTE:</b> this is the java.lang.Object @Override
    *
    * the implementation should cast and call through to the type checked method above</pre>
    *
    * @param card - another card to compare with
    *
    * @return true - if the face value and suit is equal
    */
   @Override
   boolean equals(Object card);

   /**
    * @return if equals() is true then generated hashCode should also be equal
    */
   @Override
   int hashCode();

   enum Suit {
      HEARTS, SPADES, CLUBS, DIAMONDS
   }

   enum Value {
      EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
   }
}