package view;

import controller.AppController;
import model.BlackEngineImplEx;
import model.interfaces.BlackEngine;
import model.interfaces.Player;
import view.interfaces.GameEngineCallbackEx;
import viewmodel.PersonWrapper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executor;

public class BlackEngineCallbackGuiEx extends BlackEngineCallbackGui
      implements GameEngineCallbackEx {

   private final BlackEngineImplEx gameEngineImplEx;
   private final AppView appView;
   private final Executor executor;
   private Timer checkDealHouseTimer = null;

   public BlackEngineCallbackGuiEx(BlackEngineImplEx gameEngineImplEx, AppView appView) {
      super(gameEngineImplEx, appView);
      this.gameEngineImplEx = gameEngineImplEx;
      this.appView = appView;
      this.executor = gameEngineImplEx.getExecutor();
      startCheckDealHouseTimer();
   }

   // No longer needed
   // public void addPerson(Player player, BlackEngine engine) { }

   // No longer needed
   // public void removePerson(Player player, BlackEngine engine) { }

   @SuppressWarnings("unused")
   @Override
   public void setBet(Player player, int bet, BlackEngine engine) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            appView.onBetPlaced(gameEngineImplEx.getPersonList().get(player));
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
      if (gameEngineImplEx.isEveryoneDealt() && !isHouseDealing && !gameEngineImplEx.getPersonList().getDealer().has("dealt")) {

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
                              PersonWrapper dealer = gameEngineImplEx.getPersonList().getDealer();
                              appView.onClearCards(dealer);
                              appView.activatePersonPanel(dealer);
                           }
                        });
                  util.Timers.wait(200);
                  gameEngineImplEx.dealHouse(AppController.DELAY);

                  SwingUtilities.invokeLater(
                        new Runnable() {
                           @Override
                           public void run() {
                              appView.getAddPersonButton().setEnabled(true);
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

