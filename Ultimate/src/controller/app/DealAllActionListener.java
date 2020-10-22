package controller.app;

import model.interfaces.BlackEngine;
import model.interfaces.Player;
import util.Debug;
import view.AppView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executor;

public class DealAllActionListener implements ActionListener {
   private final BlackEngine model;
   private final AppView appView;
   private final Executor executor;
   private final boolean dealHouseOnly;

   public DealAllActionListener(BlackEngine model, AppView appView, Executor executor, boolean dealHouseOnly) {
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
         dealAllPersons();
      }
   }

   public void dealAllPersons() {
      if (appView.getBlackEngine().getNumPersonDealt() == 0) {
         appView.clearResults();
      }
      int bet = 100;
      appView.getDealAllButton().setEnabled(false);
      appView.getDealHouseButton().setEnabled(false);
      if (appView.getBlackEngine().getNumPersonDealt() == 0) {
         util.Timers.wait(500);
         if (appView.getActivePerson() != null) {
            appView.clearCards(appView.getActivePerson());
         }
      }
      for (Player player : model.getAllPersons()) {
         if (!appView.getBlackEngine().isPersonDealt(player)) {
            executor.execute(
                  new Runnable() {
                     @Override
                     public void run() {
                        Debug.format("Running deal for player %s\n", player.toString());
                        if (Integer.parseInt(player.getPersonId()) < 1) {
                           Debug.println("Skipping dealer");
                           return;
                        }
                        util.Timers.wait(500);
                        appView.activatePersonPanel(player);
                        appView.setBetEnabled(false);
                        util.Timers.wait(500);
                        appView.clearCards(player);
                        if (player.getPoints() < bet) {
                           Debug.println("Skipping broke player");
                           model.removePerson(player);
                           appView.setBetEnabled(false);
                           return;
                        }
                        util.Timers.wait(500);

                        model.placeBet(player, bet);

                        appView.getPersonSelection().setSelectedIndex(appView.getBlackEngine().getPersonIndex(player) + 1);
                        model.dealPerson(player, 100);
                     }
                  });
         }
      }
   }

   public void dealHouse() {
      appView.getDealAllButton().setEnabled(false);
      appView.getDealHouseButton().setEnabled(false);
      appView.getAddPersonButton().setEnabled(false);

      executor.execute(
            new Runnable() {
               @Override
               public void run() {
                  appView.getPersonSelection().setSelectedIndex(1);
                  appView.activatePersonPanel(appView.getBlackEngine().getPersonByIndex(0));
                  util.Timers.wait(500);
                  appView.clearCards(appView.getBlackEngine().getPersonByIndex(0));
                  util.Timers.wait(500);
                  model.dealHouse(100);
                  appView.getDealHouseButton().setEnabled(true);
                  appView.getDealAllButton().setEnabled(true);
                  appView.getAddPersonButton().setEnabled(true);
               }
            });
   }
}
