package controller.listeners;

import view.NewPlayerDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewPlayerDialogCancelButtonListener implements ActionListener {
   private final NewPlayerDialog dialog;

   public NewPlayerDialogCancelButtonListener(NewPlayerDialog dialog) {
      this.dialog = dialog;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      dialog.dispose();
   }

}
