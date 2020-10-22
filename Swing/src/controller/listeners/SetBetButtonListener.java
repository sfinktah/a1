package controller.listeners;

import model.BlackEngineImplEx;
import viewmodel.PersonWrapper;
import util.StringHelpers;
import view.AppView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SetBetButtonListener implements ActionListener {
   private final BlackEngineImplEx model;
   private final AppView appView;

   public SetBetButtonListener(BlackEngineImplEx model, AppView appView) {
      this.model = model;
      this.appView = appView;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      int bet = StringHelpers.stringAsInt(appView.getBetField().getText(), -1);
      PersonWrapper activePerson = appView.getActivePerson();
      if (bet < 0 || bet > activePerson.getPoints()) {
         JOptionPane.showMessageDialog(null, "Invalid value for bet", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      if (model.getPersonList().getDealer().has("dealt")) {
         for (PersonWrapper pw : model.getPersonList().withoutDealer())
            pw.has("dealt", false);
         model.getPersonList().getDealer().has("dealt", false);
         appView.setStatus("A new round has just begun");
         appView.clearBets();
      }

      appView.clearResult(activePerson);
      model.placeBet(activePerson, bet);
      appView.onClearCards(activePerson);
   }
}
