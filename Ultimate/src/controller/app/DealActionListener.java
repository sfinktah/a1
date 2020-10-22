package controller.app;

import model.interfaces.BlackEngine;
import model.interfaces.Player;
import util.Debug;
import view.AppView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executor;

public class DealActionListener implements ActionListener {
   private final BlackEngine model;
   private final AppView appView;
   private final Executor executor;

   public DealActionListener(BlackEngine model, AppView appView, Executor executor) {
      this.model = model;
      this.appView = appView;
      this.executor = executor;
   }

   public void actionPerformed(ActionEvent e) {
      // int bet = SafeParse.stringAsInt(appView.getBetField().getText(), -1);
      final Player player = appView.getActivePerson();

      int bet = player.getBet();
      if (bet < 1 || bet > player.getPoints()) {
         JOptionPane.showMessageDialog(null, "Invalid value for bet", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      if (appView.getBlackEngine().getNumPersonDealt() == 0) {
         appView.clearResults();
      }

      appView.setBetEnabled(false);
      executor.execute(
            new Runnable() {
               @Override
               public void run() {
                  Debug.format("Running deal for player %s\n", player.toString());
                  appView.clearCards(player);
                  appView.activatePersonPanel(player);

                  appView.getPersonSelection().setSelectedIndex(appView.getBlackEngine().getPersonIndex(player) + 1);
                  util.Timers.wait(200);
                  model.dealPerson(player, 100);
               }
            }
      );
   }
}
