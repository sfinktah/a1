package controller.listeners;

import model.BlackEngineImplEx;
import view.NewPersonDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddPlayerButtonListener implements ActionListener {
   private final BlackEngineImplEx model;

   public AddPlayerButtonListener(BlackEngineImplEx model) {
      this.model = model;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      NewPersonDialog dialog = new NewPersonDialog(model, new javax.swing.JFrame(), true);

      dialog.getOkayButton().addActionListener(new NewPlayerDialogOkayButtonListener(dialog, model));
      dialog.getCancelButton().addActionListener(new NewPlayerDialogCancelButtonListener(dialog));

      dialog.setVisible(true);
   }
}
