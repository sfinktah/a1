package view.interfaces;

import model.interfaces.BlackEngine;
import model.interfaces.Player;
import model.interfaces.PokerCard;

/**
 * Assignment interface for Further Programming to notify client of BlackEngine events<br>
 * i.e. the state of the cards as dealt
 *
 * @author Caspar Ryan
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public interface BlackEngineCallback {
   /**
    * <pre>called for each card as the house is dealing to a Player, use this to
    * update your display for each card or log to console
    *
    * @param player
    *            the Player who is receiving cards
    * @param card
    *            the next card that was dealt
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see model.interfaces.BlackEngine
    * </pre>
    */
   void nextCard(Player player, PokerCard card, BlackEngine engine);

   /**
    * <pre>
    * called when the card causes the player to bust
    * this method is called instead of {@link #nextCard(Player, PokerCard, BlackEngine)}
    * this method is called before {@link #result(Player, int, BlackEngine)}
    * use this to update your display for each card or log to console
    *
    * NOTE: If player gets 42 exactly then this method IS NOT called
    *
    * @param player
    *             the Player who is receiving cards
    * @param card
    *             the bust card that was dealt
    * @param engine
    *             a convenience reference to the engine so the receiver can call
    *             methods if necessary
    * @see model.interfaces.BlackEngine
    * </pre>
    */
   void bustCard(Player player, PokerCard card, BlackEngine engine);

   /**
    * <pre>called after the player has bust with final result (result is score prior
    * to the last card that caused the bust)
    *
    * Called from {@link BlackEngine#dealPerson(Player, int)}
    *
    * @param player
    *            the current Player
    * @param result
    *            the final score of the hand
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see model.interfaces.BlackEngine
    * </pre>
    */
   void result(Player player, int result, BlackEngine engine);

   /**
    * <pre>called as the house is dealing their own hand, use this to update your
    * display for each card or log to console
    *
    * @param card
    *            the next card that was dealt
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see model.interfaces.BlackEngine
    * </pre>
    */
   void nextHouseCard(PokerCard card, BlackEngine engine);

   /**
    * <pre>HOUSE version of
    * {@link BlackEngineCallback#bustCard(Player, PokerCard, BlackEngine)}
    *
    * @param card
    *            the bust card that was dealt
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see model.interfaces.BlackEngine
    * </pre>
    */
   void houseBustCard(PokerCard card, BlackEngine engine);

   /**
    * <pre>called when the HOUSE has bust with final result (result is score prior to the last card
    * that caused the bust)
    *
    * NOTE: If house gets 42 exactly then this method IS NOT called
    *
    * PRE-CONDITION: This method should only be called AFTER bets have been updated on all Persons
    * so this callback can log Player results
    *
    * Called from {@link BlackEngine#dealHouse(int)}
    *
    * @param result
    *            the final score of the dealers (house) hand
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see model.interfaces.BlackEngine
    * </pre>
    */
   void houseResult(int result, BlackEngine engine);
}
