package controller.listeners;

import model.GameEngineImplEx;
import model.SimplePlayer;
import util.StringHelpers;
import view.NewPlayerDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewPlayerDialogOkayButtonListener implements ActionListener {
   private final NewPlayerDialog dialog;
   private final GameEngineImplEx model;

   public NewPlayerDialogOkayButtonListener(NewPlayerDialog dialog, GameEngineImplEx model) {
      this.dialog = dialog;
      this.model = model;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (dialog.getTextFieldText(1).length() == 0) {
         JOptionPane.showMessageDialog(null, "Invalid value for PlayerWrapper Name", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      int newPlayerId = StringHelpers.stringAsInt(dialog.getTextFieldText(0), -1);
      if (newPlayerId < 1) {
         JOptionPane.showMessageDialog(null, "Invalid value for PlayerWrapper ID", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      int chips = StringHelpers.stringAsInt(dialog.getTextFieldText(2), -1);
      if (chips < 1) {
         JOptionPane.showMessageDialog(null, "Invalid value for Chips", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      model.addPlayer(new SimplePlayer(String.valueOf(newPlayerId), dialog.getTextFieldText(1), chips));
      dialog.dispose();
   }
}
