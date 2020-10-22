package controller.listeners;

import model.BlackEngineImplEx;
import view.AppView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RemovePlayerButtonListener implements ActionListener {
   private final BlackEngineImplEx model;
   private final AppView appView;

   public RemovePlayerButtonListener(BlackEngineImplEx model, AppView appView) {
      this.model = model;
      this.appView = appView;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      model.removePerson(appView.getActivePerson());
   }
}
