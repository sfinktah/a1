package controller.listeners;

import model.GameEngineImplEx;
import view.NewPlayerDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddPlayerButtonListener implements ActionListener {
   private final GameEngineImplEx model;

   public AddPlayerButtonListener(GameEngineImplEx model) {
      this.model = model;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      NewPlayerDialog dialog = new NewPlayerDialog(model, new javax.swing.JFrame(), true);

      dialog.getOkayButton().addActionListener(new NewPlayerDialogOkayButtonListener(dialog, model));
      dialog.getCancelButton().addActionListener(new NewPlayerDialogCancelButtonListener(dialog));

      dialog.setVisible(true);
   }
}
