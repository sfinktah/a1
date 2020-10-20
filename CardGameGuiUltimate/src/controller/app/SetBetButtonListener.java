package controller.app;

import model.interfaces.GameEngine;
import util.StringHelpers;
import view.AppView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SetBetButtonListener implements ActionListener {
   private final GameEngine model;
   private final AppView appView;

   public SetBetButtonListener(GameEngine model, AppView appView) {
      this.model = model;
      this.appView = appView;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      int bet = StringHelpers.stringAsInt(appView.getBetField().getText(), -1);
      if (bet < 0 || bet > appView.getActivePlayer().getPoints()) {
         JOptionPane.showMessageDialog(null, "Invalid value for bet", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      if (appView.getGameEngine().getNumPlayerDealt() == 0) {
         appView.clearResults();
      }

      model.placeBet(appView.getActivePlayer(), bet);
      appView.clearCards(appView.getActivePlayer());
   }
}
