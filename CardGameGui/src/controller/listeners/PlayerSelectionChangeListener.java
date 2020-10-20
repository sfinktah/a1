package controller.listeners;

import model.GameEngineImplEx;
import viewmodel.PlayerWrapper;
import view.AppView;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

// https://stackoverflow.com/questions/58939/jcombobox-selection-change-listener
// int selectedIndex = myComboBox.getSelectedIndex();
// Object selectedObject = myComboBox.getSelectedItem();
// String selectedValue = myComboBox.getSelectedValue().toString();
public class PlayerSelectionChangeListener implements ItemListener {
   private final GameEngineImplEx model;
   private final AppView appView;

   public PlayerSelectionChangeListener(GameEngineImplEx model, AppView appView) {
      this.model = model;
      this.appView = appView;
   }

   @Override
   public void itemStateChanged(ItemEvent event) {
      if (event.getStateChange() == ItemEvent.SELECTED) {
         int playerIndex = appView.getPlayerSelection().getSelectedIndex();
         PlayerWrapper player = model.getPlayerList().at(playerIndex);
         appView.activatePlayerPanel(player);
         appView.onSetBetEnabled(!player.isDealer() && !player.has("dealt"));
      }
   }
}
