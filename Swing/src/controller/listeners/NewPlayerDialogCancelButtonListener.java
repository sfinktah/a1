package controller.listeners;

import view.NewPersonDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewPlayerDialogCancelButtonListener implements ActionListener {
   private final NewPersonDialog dialog;

   public NewPlayerDialogCancelButtonListener(NewPersonDialog dialog) {
      this.dialog = dialog;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      dialog.dispose();
   }

}
