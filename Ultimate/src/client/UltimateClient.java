package client;

import controller.FatController;
import model.BlackEngineGuiDecor;
import model.BlackEngineImpl;
import model.interfaces.BlackEngine;
import model.interfaces.PokerCard;
import view.AppView;
import view.BlackEngineCallbackGUI;
import view.BlackEngineCallbackImpl;

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
public class UltimateClient {
   // Enable DEBUG to debug your deck of cards creation
   final public static boolean DEBUG = false;

   public static void main(String[] args) {
      final BlackEngine gameEngine = new BlackEngineImpl();

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

      BlackEngineGuiDecor model = new BlackEngineGuiDecor(gameEngine);
      AppView appView = new AppView(model);
      new FatController(model, appView);

      gameEngine.addBlackEngineCallback(new BlackEngineCallbackGUI(model, appView));
      gameEngine.addBlackEngineCallback(new BlackEngineCallbackImpl());
      Logger logger = Logger.getLogger(BlackEngineCallbackImpl.class.getName());
      view.BlackEngineCallbackImpl.setAllHandlers(Level.INFO, logger, true);

      // Set DEBUG to true to see your deck of cards creation
      if (DEBUG) {
         Deque<PokerCard> shuffledDeck = gameEngine.getShuffledHalfDeck();
         printCards(shuffledDeck);
      }
   }

   private static void printCards(Deque<PokerCard> deck) {
      for (PokerCard card : deck)
         BlackEngineCallbackImpl.logger.log(Level.INFO, card.toString());
   }
}
