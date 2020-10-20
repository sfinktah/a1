package client;

// https://teams.microsoft.com/l/meetup-join/19%3ameeting_YzdmNzMxOWYtNDE4YS00YTU0LWFiNzEtYjQzN2QxOTc3YzYw%40thread.v2/0?context=%7b%22Tid%22%3a%22d1323671-cdbe-4417-b4d4-bdb24b51316b%22%2c%22Oid%22%3a%224c63957a-3227-4028-8d6a-e4ebd05a3c00%22%7d

import controller.AppController;
import model.GameEngineImpl;
import model.GameEngineImplEx;
import model.SimplePlayer;
import model.interfaces.GameEngine;
import validate.Validator;
import view.AppView;
import view.GameEngineCallbackGUI;
import view.GameEngineCallbackGuiEx;
import view.GameEngineCallbackImpl;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * Simple console client for FP assignment 2, 2020
 */
public class SimpleTestClientGUI {
   public static void main(String[] args) {
      final GameEngine gameEngine = new GameEngineImpl();

      // call method in Validator.jar to test *structural* correctness
      // just passing this does not mean it actually works .. you need to test yourself!
      // pass false if you want to disable logging .. (i.e. once it passes)
      Validator.validate(true);

      try {
         for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
               javax.swing.UIManager.setLookAndFeel(info.getClassName());
            }
            if ("Mac OS X".equals(info.getName())) {
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
      // View v = new View(m);
      // Controller c = new Controller(m, v);

      // Model
      GameEngineImplEx gameEngineImplEx = new GameEngineImplEx(gameEngine);
      gameEngineImplEx.addGameEngineCallback(new GameEngineCallbackImpl());

      // View
      AppView appView = new AppView(gameEngineImplEx);

      // Controller
      new AppController(gameEngineImplEx, appView);

      // Start it rolling
      appView.setVisible(true);

      // Add A1 style callback to A1 style model
      gameEngine.addGameEngineCallback(new GameEngineCallbackGUI(gameEngineImplEx, appView));

      // Add A2 extended callback to A2 extended model
      gameEngineImplEx.addGameEngineCallback(new GameEngineCallbackGuiEx(gameEngineImplEx, appView));

      Timer initPlayerTimer = new Timer(100, new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            // Players need to be added to A2 extended model, else the GUI won't be notified
            gameEngineImplEx.addPlayer(new SimplePlayer("0", "House", 0));
            gameEngineImplEx.addPlayer(new SimplePlayer("1", "The Loser", 500));
            gameEngineImplEx.addPlayer(new SimplePlayer("2", "The Shark", 1000));
         }
      });
      initPlayerTimer.setRepeats(false);
      initPlayerTimer.start();

//      GameEngineCallbackImpl.setAllHandlers(Level.INFO, GameEngineCallbackImpl.logger, true);
   }
}
