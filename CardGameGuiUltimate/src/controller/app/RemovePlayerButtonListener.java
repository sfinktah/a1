package controller.app;

import model.interfaces.GameEngine;
import view.AppView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RemovePlayerButtonListener implements ActionListener {
   private final GameEngine model;
   private final AppView appView;

   public RemovePlayerButtonListener(GameEngine model, AppView appView) {
      this.model = model;
      this.appView = appView;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      model.removePlayer(appView.getActivePlayer());
   }
}
