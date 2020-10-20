package view;

import model.interfaces.GameEngine;
import model.interfaces.Player;
import model.interfaces.PlayingCard;
import view.interfaces.GameEngineCallback;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Skeleton/Partial example implementation of GameEngineCallback showing Java logging behaviour
 *
 * @author Caspar Ryan
 * @see view.interfaces.GameEngineCallback
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class GameEngineCallbackImpl implements GameEngineCallback {
   public static final Logger logger = Logger.getLogger(GameEngineCallbackImpl.class.getName());

   public GameEngineCallbackImpl() {
      // NOTE can also set the console to FINE in %JRE_HOME%\lib\logging.properties
      setAllHandlers(Level.FINE, logger, true);
   }

   // utility method to set output level of logging handlers
   @SuppressWarnings("UnusedReturnValue")
   public static Logger setAllHandlers(Level level, Logger logger, boolean recursive) {
      // end recursion?
      if (logger != null) {
         logger.setLevel(level);
         for (Handler handler : logger.getHandlers())
            handler.setLevel(level);
         // recursion
         setAllHandlers(level, logger.getParent(), recursive);
      }
      return logger;
   }

   @Override
   public void nextCard(Player player, PlayingCard card, GameEngine engine) {
      // intermediate results logged at Level.FINE
      // logger.log(Level.FINE, "Intermediate data to log .. String.format() is good here!");
      logger.log(Level.FINE, String.format("Card Dealt to %s .. %s", player.getPlayerName(),
            card.toString()));
   }

   @Override
   public void result(Player player, int result, GameEngine engine) {
      // final results logged at Level.INFO
      // logger.log(Level.INFO, "Result data to log .. String.format() is good here!");
      logger.log(Level.INFO, String.format("%s, final result=%d",
            player.getPlayerName(),
            result));
   }

   /**
    * <pre>
    * called when the card causes the player to bust
    * this method is called instead of {@link #nextCard(Player, PlayingCard, GameEngine)}
    * this method is called before {@link #result(Player, int, GameEngine)}
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
    * @see GameEngine
    * </pre>
    */
   @Override
   public void bustCard(Player player, PlayingCard card, GameEngine engine) {
      logger.log(Level.FINE, String.format("Card Dealt to %s .. %s ... YOU BUSTED!", player.getPlayerName(),
            card.toString()));
   }

   /**
    * <pre>called as the house is dealing their own hand, use this to update your
    * display for each card or log to console
    *
    * @param card
    *            the next card that was dealt
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see GameEngine
    * </pre>
    */
   @Override
   public void nextHouseCard(PlayingCard card, GameEngine engine) {
      logger.log(Level.FINE, String.format("Card Dealt to House .. %s",
            card.toString()));
   }

   /**
    * <pre>HOUSE version of
    * {@link GameEngineCallback#bustCard(Player, PlayingCard, GameEngine)}
    *
    * @param card
    *            the bust card that was dealt
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see GameEngine
    * </pre>
    */
   @Override
   public void houseBustCard(PlayingCard card, GameEngine engine) {
      logger.log(Level.FINE, String.format("Card Dealt to House .. %s ... HOUSE BUSTED!",
            card.toString()));
   }

   /**
    * <pre>called when the HOUSE has bust with final result (result is score prior to the last card
    * that caused the bust)
    *
    * NOTE: If house gets 42 exactly then this method IS NOT called
    *
    * PRE-CONDITION: This method should only be called AFTER bets have been updated on all Players
    * so this callback can log Player results
    *
    * Called from {@link GameEngine#dealHouse(int)}
    *
    * @param result
    *            the final score of the dealers (house) hand
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see GameEngine
    * </pre>
    */
   @Override
   public void houseResult(int result, GameEngine engine) {
      // final results logged at Level.INFO
      logger.log(Level.INFO, String.format("House, final result=%d", result));

      // Summarise player totals
      StringBuilder results = new StringBuilder();
      for (Player player : engine.getAllPlayers()) {
         results.append(String.format("%s%n", player.toString()));
      }

      logger.log(Level.INFO, String.format("Final Player Results%n%s", results.toString()));
   }


}
