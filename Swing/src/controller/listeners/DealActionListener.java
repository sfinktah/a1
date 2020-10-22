package controller.listeners;

import controller.AppController;
import model.BlackEngineImplEx;
import viewmodel.PersonWrapper;
import view.AppView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Executor;

public class DealActionListener implements ActionListener {
   private final BlackEngineImplEx model;
   private final AppView appView;
   private final Executor executor;

   public DealActionListener(BlackEngineImplEx model, AppView appView, Executor executor) {
      this.model = model;
      this.appView = appView;
      this.executor = executor;
   }

   public void actionPerformed(ActionEvent e) {
      final PersonWrapper player = appView.getActivePerson();

      int bet = player.getBet();
      if (bet < 1 || bet > player.getPoints()) {
         JOptionPane.showMessageDialog(null, "Invalid value for bet", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }

      appView.onSetBetEnabled(false);
      executor.execute(
            new Runnable() {
               @Override
               public void run() {
                  model.dealPerson(player, AppController.DELAY);
               }
            }
      );
   }
}
