package client;

import model.GameEngineImpl;
import model.SimplePlayer;
import model.interfaces.GameEngine;
import model.interfaces.Player;
import model.interfaces.PlayingCard;
import validate.Validator;
import view.GameEngineCallbackImpl;

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
public class SimpleTestClient {
   // Enable DEBUG to debug your deck of cards creation
   final public static boolean DEBUG = false;

   public static void main(String[] args) {
      final GameEngine gameEngine = new GameEngineImpl();

      // call method in Validator.jar to test *structural* correctness
      // just passing this does not mean it actually works .. you need to test yourself!
      // pass false if you want to disable logging .. (i.e. once it passes)
      Validator.validate(true);

      // create two test players
      Player[] players = new Player[]{
            new SimplePlayer("2", "The Shark", 1000),
            new SimplePlayer("1", "The Loser", 500),
      };

      // add logging callback
      gameEngine.addGameEngineCallback(new GameEngineCallbackImpl());

      // Set DEBUG to true to see your deck of cards creation
      if (DEBUG) {
         Deque<PlayingCard> shuffledDeck = gameEngine.getShuffledHalfDeck();
         printCards(shuffledDeck);
      }

      // main loop to add players, place a bet and receive hand
      for (Player player : players) {
         if (player.getPoints() <= 100) {
            continue;
         }

         gameEngine.addPlayer(player);
         gameEngine.placeBet(player, 100);
         gameEngine.dealPlayer(player, 10);
      }

      // all players have played so now house deals
      // GameEngineCallBack.houseResult() is called to log all players (after results are calculated)
      gameEngine.dealHouse(10);

   }

   @SuppressWarnings({"unused", "RedundantSuppression"})
   private static void printCards(Deque<PlayingCard> deck) {
      for (PlayingCard card : deck)
         GameEngineCallbackImpl.logger.log(Level.INFO, card.toString());
   }
}
