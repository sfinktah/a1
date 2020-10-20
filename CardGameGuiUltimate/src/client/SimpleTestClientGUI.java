package client;

import controller.FatController;
import model.GameEngineGuiDecor;
import model.GameEngineImpl;
import model.interfaces.GameEngine;
import model.interfaces.PlayingCard;
import validate.Validator;
import view.AppView;
import view.GameEngineCallbackGUI;
import view.GameEngineCallbackImpl;

import javax.swing.*;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;


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
public class SimpleTestClientGUI {
   // Enable DEBUG to debug your deck of cards creation
   final public static boolean DEBUG = false;

   public static void main(String[] args) {
      final GameEngine gameEngine = new GameEngineImpl();

      // call method in Validator.jar to test *structural* correctness
      // just passing this does not mean it actually works .. you need to test yourself!
      // pass false if you want to disable logging .. (i.e. once it passes)
      Validator.validate(true);

      // create two test players
//      Player[] players = new Player[] {
//            new SimplePlayer("2", "The Shark", 1000),
//            new SimplePlayer("1", "The Loser", 500),
//      };

      try {
         for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
               javax.swing.UIManager.setLookAndFeel(info.getClassName());
               break;
            }
         }
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
         java.util.logging.Logger.getLogger(AppView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }

      // add GUI callback in a more "MVC" way.
      //
      // Model m = new Model();
      // View v = new View();
      // Controller c = new Controller(m, v);

      GameEngineGuiDecor model = new GameEngineGuiDecor(gameEngine);
      AppView appView = new AppView(model);
      new FatController(model, appView);

      gameEngine.addGameEngineCallback(new GameEngineCallbackGUI(model, appView));
      gameEngine.addGameEngineCallback(new GameEngineCallbackImpl());
      Logger logger = Logger.getLogger(GameEngineCallbackImpl.class.getName());
      view.GameEngineCallbackImpl.setAllHandlers(Level.INFO, logger, true);

      // Set DEBUG to true to see your deck of cards creation
      if (DEBUG) {
         Deque<PlayingCard> shuffledDeck = gameEngine.getShuffledHalfDeck();
         printCards(shuffledDeck);
      }
   }

   private static void printCards(Deque<PlayingCard> deck) {
      for (PlayingCard card : deck)
         GameEngineCallbackImpl.logger.log(Level.INFO, card.toString());
   }
}
