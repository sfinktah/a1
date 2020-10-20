package controller.listeners;

import model.GameEngineImplEx;
import viewmodel.PlayerWrapper;
import util.StringHelpers;
import view.AppView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SetBetButtonListener implements ActionListener {
   private final GameEngineImplEx model;
   private final AppView appView;

   public SetBetButtonListener(GameEngineImplEx model, AppView appView) {
      this.model = model;
      this.appView = appView;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      int bet = StringHelpers.stringAsInt(appView.getBetField().getText(), -1);
      PlayerWrapper activePlayer = appView.getActivePlayer();
      if (bet < 0 || bet > activePlayer.getPoints()) {
         JOptionPane.showMessageDialog(null, "Invalid value for bet", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      if (model.getPlayerList().getDealer().has("dealt")) {
         for (PlayerWrapper pw : model.getPlayerList().withoutDealer())
            pw.has("dealt", false);
         model.getPlayerList().getDealer().has("dealt", false);
         appView.setStatus("A new round has just begun");
         appView.clearBets();
      }

      appView.clearResult(activePlayer);
      model.placeBet(activePlayer, bet);
      appView.onClearCards(activePlayer);
   }
}
