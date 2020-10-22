package controller.listeners;

import model.BlackEngineImplEx;
import model.SimplePerson;
import util.StringHelpers;
import view.NewPersonDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewPlayerDialogOkayButtonListener implements ActionListener {
   private final NewPersonDialog dialog;
   private final BlackEngineImplEx model;

   public NewPlayerDialogOkayButtonListener(NewPersonDialog dialog, BlackEngineImplEx model) {
      this.dialog = dialog;
      this.model = model;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      if (dialog.getTextFieldText(1).length() == 0) {
         JOptionPane.showMessageDialog(null, "Invalid value for PersonWrapper Name", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      int newPersonId = StringHelpers.stringAsInt(dialog.getTextFieldText(0), -1);
      if (newPersonId < 1) {
         JOptionPane.showMessageDialog(null, "Invalid value for PersonWrapper ID", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      int chips = StringHelpers.stringAsInt(dialog.getTextFieldText(2), -1);
      if (chips < 1) {
         JOptionPane.showMessageDialog(null, "Invalid value for Chips", "Error", JOptionPane.INFORMATION_MESSAGE);
         return;
      }
      model.addPerson(new SimplePerson(String.valueOf(newPersonId), dialog.getTextFieldText(1), chips));
      dialog.dispose();
   }
}
