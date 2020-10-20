package controller.app;

import controller.FatController;
import model.SimplePlayer;
import model.interfaces.GameEngine;
import util.StringHelpers;
import view.NewPlayerDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddPlayerButtonListener implements ActionListener {
   private final GameEngine model;

   public AddPlayerButtonListener(GameEngine model) {
      this.model = model;
   }

   public void actionPerformed(ActionEvent e) {
      NewPlayerDialog dialog = new NewPlayerDialog(model, new javax.swing.JFrame(), true);

      dialog.getOkayButton().addActionListener(
            new ActionListener() {
               @Override
               public void actionPerformed(ActionEvent e1) {

                  if (dialog.getTextFieldText(1).length() == 0) {
                     JOptionPane.showMessageDialog(null, "Invalid value for Player Name", "Error", JOptionPane.INFORMATION_MESSAGE);
                     return;
                  }
                  int nextPlayerId = FatController.getNewPlayerId(model);
                  int bet = StringHelpers.stringAsInt(dialog.getTextFieldText(2), -1);
                  if (bet < 1) {
                     JOptionPane.showMessageDialog(null, "Invalid value for Chips", "Error", JOptionPane.INFORMATION_MESSAGE);
                     return;
                  }
                  model.addPlayer(new SimplePlayer(String.valueOf(nextPlayerId), dialog.getTextFieldText(1), bet));
                  dialog.dispose();
               }
            }
      );

      dialog.getCancelButton().addActionListener(
            new ActionListener() {
               @Override
               public void actionPerformed(ActionEvent e1) {
                  dialog.dispose();
               }
            }
      );

      dialog.setVisible(true);
   }

}
