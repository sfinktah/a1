package model;

import model.interfaces.PokerCard;
import util.Randomizer;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Deck of PokerCards
 */
public class Deck {
   public static final int NUM_CARDS = 28;
   private final PokerCardImpl[] gCards = new PokerCardImpl[NUM_CARDS];
   // We will use a secure randomizer (good practice for gambling apps)
   private transient Randomizer r = Randomizer.getRandomizer();
   private int position;

   /**
    * Create deck with 28 sequential (unsorted) cards
    */
   public Deck() {
      this.position = 0;
      for (int i = 0; i < NUM_CARDS; i++) {
         this.gCards[i] = new PokerCardImpl(i);
      }
   }

   @SuppressWarnings({"unused", "RedundantSuppression"})
   public Deck(long seed) {
      this();
      setSeed(seed);
   }

   /**
    * @param seed randomizer seed
    */
   public void setSeed(long seed) {
      if (seed != 0L) {
         this.r = Randomizer.getRandomizer();
         this.r.setSeed(seed);
      }
   }

   /**
    * restore full deck (set deck position to top of deck), usually followed by {@link #shuffle()}
    *
    * @see Deck#shuffle()
    */
   public void reset() {
      this.position = 0;
   }

   /**
    * shuffles deck (should call {@link #reset()} first)
    *
    * @see Deck#reset()
    */
   public void shuffle() {
      for (int i = 0; i < NUM_CARDS; i++) {
         int j = i + this.r.nextInt(NUM_CARDS - i);
         PokerCardImpl tempCard = this.gCards[j];
         this.gCards[j] = this.gCards[i];
         this.gCards[i] = tempCard;
      }
      this.position = 0;
   }

   /**
    * Deals card from top of deck, then moves deck position to next card (emulates discard of dealt card)
    *
    * @return PokerCard on top of the deck
    */
   public PokerCardImpl dealCard() {
      if (this.position >= NUM_CARDS) {
         reset();
         shuffle();
      }
      if (this.position < NUM_CARDS) {
         return this.gCards[(this.position++)];
      }
      else {
         throw new RuntimeException();
      }
   }

   /**
    * @return Unused cards in deck, as a LinkedList
    */
   public LinkedList<PokerCard> asLinkedList() {
      return new LinkedList<>(Arrays.asList(gCards).subList(this.position, NUM_CARDS));
   }

   // We never call this, but I had the code from a previous gambling project.
   // If I were to rewrite it today, I would use streams and lambdas, which I'm
   // not allowed to do -- so, it is what it is (ugly).
   public String toString() {
      StringBuilder s = new StringBuilder();
      s.append("* ");
      for (int i = 0; i < this.position; i++) {
         s.append(this.gCards[i].toString()).append(" ");
      }
      s.append("\n* ");
      for (int i = this.position; i < NUM_CARDS; i++) {
         s.append(this.gCards[i].toString()).append(" ");
      }
      return s.toString();
   }
}
