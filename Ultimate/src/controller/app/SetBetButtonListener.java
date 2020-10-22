package controller.app;

import model.interfaces.BlackEngine;
import util.StringHelpers;
import view.AppView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SetBetButtonListener implements ActionListener {
   private final BlackEngine model;
   private final AppView appView;

   public SetBetButtonListener(BlackEngine model, AppView appView) {
      this.model = model;
      this.appView = appView;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      int bet = StringHelpers.stringAsInt(appView.getBetField().getText(), -1);
      if (bet < 0 || bet > appView.getActivePerson().getPoints()) {
         JOptionPane.showMessageDialog(null, "Invalid value for bet", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      if (appView.getBlackEngine().getNumPersonDealt() == 0) {
         appView.clearResults();
      }

      model.placeBet(appView.getActivePerson(), bet);
      appView.clearCards(appView.getActivePerson());
   }
}
