package client;

import model.BlackEngineImpl;
import model.SimplePerson;
import model.interfaces.BlackEngine;
import model.interfaces.Player;
import model.interfaces.PokerCard;
//import validate.Validator;
import view.BlackEngineCallbackImpl;

import java.util.Deque;
import java.util.logging.Level;

/**
 * Simple console client for FP assignment 2, 2020
 * NOTE: This code will not compile until you have implemented code for the supplied interfaces!
 * <p>
 * You must be able to compile your code WITHOUT modifying this class.
 * Additional testing should be done by copying and adding to this class while ensuring this class still works.
 * <p>
 * The provided Validator.jar will check if your code adheres to the specified interfaces!
 *
 * @author Caspar Ryan
 */
public class TextClient {
   // Enable DEBUG to debug your deck of cards creation
   final public static boolean DEBUG = false;

   public static void main(String[] args) {
      final BlackEngine gameEngine = new BlackEngineImpl();

      // call method in Validator.jar to test *structural* correctness
      // just passing this does not mean it actually works .. you need to test yourself!
      // pass false if you want to disable logging .. (i.e. once it passes)
//      Validator.validate(true);

      // create two test players
      Player[] players = new Player[]{
            new SimplePerson("2", "The Shark", 1000),
            new SimplePerson("1", "The Loser", 500),
      };

      // add logging callback
      gameEngine.addBlackEngineCallback(new BlackEngineCallbackImpl());

      // Set DEBUG to true to see your deck of cards creation
      if (DEBUG) {
         Deque<PokerCard> shuffledDeck = gameEngine.getShuffledHalfDeck();
         printCards(shuffledDeck);
      }

      // main loop to add players, place a bet and receive hand
      for (Player player : players) {
         if (player.getPoints() <= 100) {
            continue;
         }

         gameEngine.addPerson(player);
         gameEngine.placeBet(player, 100);
         gameEngine.dealPerson(player, 10);
      }

      // all players have played so now house deals
      // BlackEngineCallBack.houseResult() is called to log all players (after results are calculated)
      gameEngine.dealHouse(10);

   }

   @SuppressWarnings({"unused", "RedundantSuppression"})
   private static void printCards(Deque<PokerCard> deck) {
      for (PokerCard card : deck)
         BlackEngineCallbackImpl.logger.log(Level.INFO, card.toString());
   }
}
