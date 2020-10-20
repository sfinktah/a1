package controller.app;

import model.interfaces.GameEngine;
import model.interfaces.Player;
import util.Debug;
import view.AppView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executor;

public class DealActionListener implements ActionListener {
   private final GameEngine model;
   private final AppView appView;
   private final Executor executor;

   public DealActionListener(GameEngine model, AppView appView, Executor executor) {
      this.model = model;
      this.appView = appView;
      this.executor = executor;
   }

   public void actionPerformed(ActionEvent e) {
      // int bet = SafeParse.stringAsInt(appView.getBetField().getText(), -1);
      final Player player = appView.getActivePlayer();

      int bet = player.getBet();
      if (bet < 1 || bet > player.getPoints()) {
         JOptionPane.showMessageDialog(null, "Invalid value for bet", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      if (appView.getGameEngine().getNumPlayerDealt() == 0) {
         appView.clearResults();
      }

      appView.setBetEnabled(false);
      executor.execute(
            new Runnable() {
               @Override
               public void run() {
                  Debug.format("Running deal for player %s\n", player.toString());
                  appView.clearCards(player);
                  appView.activatePlayerPanel(player);

                  appView.getPlayerSelection().setSelectedIndex(appView.getGameEngine().getPlayerIndex(player) + 1);
                  util.Timers.wait(200);
                  model.dealPlayer(player, 100);
               }
            }
      );
   }
}
