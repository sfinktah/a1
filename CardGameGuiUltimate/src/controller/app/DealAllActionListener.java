package controller.app;

import model.interfaces.GameEngine;
import model.interfaces.Player;
import util.Debug;
import view.AppView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executor;

public class DealAllActionListener implements ActionListener {
   private final GameEngine model;
   private final AppView appView;
   private final Executor executor;
   private final boolean dealHouseOnly;

   public DealAllActionListener(GameEngine model, AppView appView, Executor executor, boolean dealHouseOnly) {
      this.model = model;
      this.appView = appView;
      this.executor = executor;
      this.dealHouseOnly = dealHouseOnly;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (dealHouseOnly) {
         dealHouse();
      }
      else {
         dealAllPlayers();
      }
   }

   public void dealAllPlayers() {
      if (appView.getGameEngine().getNumPlayerDealt() == 0) {
         appView.clearResults();
      }
      int bet = 100;
      appView.getDealAllButton().setEnabled(false);
      appView.getDealHouseButton().setEnabled(false);
      if (appView.getGameEngine().getNumPlayerDealt() == 0) {
         util.Timers.wait(500);
         if (appView.getActivePlayer() != null) {
            appView.clearCards(appView.getActivePlayer());
         }
      }
      for (Player player : model.getAllPlayers()) {
         if (!appView.getGameEngine().isPlayerDealt(player)) {
            executor.execute(
                  new Runnable() {
                     @Override
                     public void run() {
                        Debug.format("Running deal for player %s\n", player.toString());
                        if (Integer.parseInt(player.getPlayerId()) < 1) {
                           Debug.println("Skipping dealer");
                           return;
                        }
                        util.Timers.wait(500);
                        appView.activatePlayerPanel(player);
                        appView.setBetEnabled(false);
                        util.Timers.wait(500);
                        appView.clearCards(player);
                        if (player.getPoints() < bet) {
                           Debug.println("Skipping broke player");
                           model.removePlayer(player);
                           appView.setBetEnabled(false);
                           return;
                        }
                        util.Timers.wait(500);

                        model.placeBet(player, bet);

                        appView.getPlayerSelection().setSelectedIndex(appView.getGameEngine().getPlayerIndex(player) + 1);
                        model.dealPlayer(player, 100);
                     }
                  });
         }
      }
   }

   public void dealHouse() {
      appView.getDealAllButton().setEnabled(false);
      appView.getDealHouseButton().setEnabled(false);
      appView.getAddPlayerButton().setEnabled(false);

      executor.execute(
            new Runnable() {
               @Override
               public void run() {
                  appView.getPlayerSelection().setSelectedIndex(1);
                  appView.activatePlayerPanel(appView.getGameEngine().getPlayerByIndex(0));
                  util.Timers.wait(500);
                  appView.clearCards(appView.getGameEngine().getPlayerByIndex(0));
                  util.Timers.wait(500);
                  model.dealHouse(100);
                  appView.getDealHouseButton().setEnabled(true);
                  appView.getDealAllButton().setEnabled(true);
                  appView.getAddPlayerButton().setEnabled(true);
               }
            });
   }
}
