package controller.listeners;

import model.GameEngineImplEx;
import view.AppView;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RemovePlayerButtonListener implements ActionListener {
   private final GameEngineImplEx model;
   private final AppView appView;

   public RemovePlayerButtonListener(GameEngineImplEx model, AppView appView) {
      this.model = model;
      this.appView = appView;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      model.removePlayer(appView.getActivePlayer());
   }
}
