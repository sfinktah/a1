package view;

import controller.AppController;
import model.GameEngineImplEx;
import model.interfaces.GameEngine;
import model.interfaces.Player;
import view.interfaces.GameEngineCallbackEx;
import viewmodel.PlayerWrapper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executor;

public class GameEngineCallbackGuiEx extends GameEngineCallbackGUI
      implements GameEngineCallbackEx {

   private final GameEngineImplEx gameEngineImplEx;
   private final AppView appView;
   private final Executor executor;
   private Timer checkDealHouseTimer = null;

   public GameEngineCallbackGuiEx(GameEngineImplEx gameEngineImplEx, AppView appView) {
      super(gameEngineImplEx, appView);
      this.gameEngineImplEx = gameEngineImplEx;
      this.appView = appView;
      this.executor = gameEngineImplEx.getExecutor();
      startCheckDealHouseTimer();
   }

   // No longer needed
   // public void addPlayer(Player player, GameEngine engine) { }

   // No longer needed
   // public void removePlayer(Player player, GameEngine engine) { }

   @SuppressWarnings("unused")
   @Override
   public void setBet(Player player, int bet, GameEngine engine) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            appView.onBetPlaced(gameEngineImplEx.getPlayerList().get(player));
         }
      });
   }

   @Override
   public void setStatus(String text) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            appView.setStatus(text);
         }
      });
   }

   private boolean isHouseDealing = false;
   private synchronized boolean checkDealHouse() {
      if (gameEngineImplEx.isEveryoneDealt() && !isHouseDealing && !gameEngineImplEx.getPlayerList().getDealer().has("dealt")) {

         isHouseDealing = true;
         setStatus("The house is dealing <b>shortly</b>...");

         appView.setDealHouseTimer(new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               setStatus("The house is dealing <b>now");
               dealHouse();
               isHouseDealing = false;
            }
         }));
         setStatus("The house will deal <b>real soon now</b>...");
         util.Timers.wait(200);
         appView.getDealHouseTimer().start();
         appView.getDealHouseTimer().setRepeats(false);
         return true;
      }
      return false;
   }

   private void dealHouse() {
      // The dealer is tricked in as player 0
      executor.execute(
            new Runnable() {
               @Override
               public void run() {
                  SwingUtilities.invokeLater(
                        new Runnable() {
                           @Override
                           public void run() {
                              PlayerWrapper dealer = gameEngineImplEx.getPlayerList().getDealer();
                              appView.onClearCards(dealer);
                              appView.activatePlayerPanel(dealer);
                           }
                        });
                  util.Timers.wait(200);
                  gameEngineImplEx.dealHouse(AppController.DELAY);

                  SwingUtilities.invokeLater(
                        new Runnable() {
                           @Override
                           public void run() {
                              appView.getAddPlayerButton().setEnabled(true);
                           }
                        });

               }
            }
      );
   }

   private void startCheckDealHouseTimer() {
      checkDealHouseTimer = new Timer(1000, new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            // If we have just dealt the house cards, restart the timer to
            // force a 10 second wait before rechecking.  That way we can
            // be sure we don't try and deal twice, or something.
            if (checkDealHouse()) {
               checkDealHouseTimer.restart();
            }
         }
      });
      checkDealHouseTimer.setInitialDelay(10000); // 10 seconds
      checkDealHouseTimer.setRepeats(true);
      checkDealHouseTimer.start();
   }

}

