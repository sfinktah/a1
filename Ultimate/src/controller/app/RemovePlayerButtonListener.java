package controller.app;

import model.interfaces.BlackEngine;
import view.AppView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RemovePlayerButtonListener implements ActionListener {
   private final BlackEngine model;
   private final AppView appView;

   public RemovePlayerButtonListener(BlackEngine model, AppView appView) {
      this.model = model;
      this.appView = appView;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      model.removePerson(appView.getActivePerson());
   }
}
