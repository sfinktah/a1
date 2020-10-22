package controller.app;

import controller.FatController;
import model.SimplePerson;
import model.interfaces.BlackEngine;
import util.StringHelpers;
import view.NewPersonDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddPlayerButtonListener implements ActionListener {
   private final BlackEngine model;

   public AddPlayerButtonListener(BlackEngine model) {
      this.model = model;
   }

   public void actionPerformed(ActionEvent e) {
      NewPersonDialog dialog = new NewPersonDialog(model, new javax.swing.JFrame(), true);

      dialog.getOkayButton().addActionListener(
            new ActionListener() {
               @Override
               public void actionPerformed(ActionEvent e1) {

                  if (dialog.getTextFieldText(1).length() == 0) {
                     JOptionPane.showMessageDialog(null, "Invalid value for Player Name", "Error", JOptionPane.INFORMATION_MESSAGE);
                     return;
                  }
                  int nextPersonId = FatController.getNewPersonId(model);
                  int bet = StringHelpers.stringAsInt(dialog.getTextFieldText(2), -1);
                  if (bet < 1) {
                     JOptionPane.showMessageDialog(null, "Invalid value for Chips", "Error", JOptionPane.INFORMATION_MESSAGE);
                     return;
                  }
                  model.addPerson(new SimplePerson(String.valueOf(nextPersonId), dialog.getTextFieldText(1), bet));
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
