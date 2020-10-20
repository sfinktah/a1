package controller.app;

import model.interfaces.GameEngine;
import model.interfaces.Player;
import util.Debug;
import view.AppView;
import view.AppViewHelpers;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

// https://stackoverflow.com/questions/58939/jcombobox-selection-change-listener
// int selectedIndex = myComboBox.getSelectedIndex();
// Object selectedObject = myComboBox.getSelectedItem();
// String selectedValue = myComboBox.getSelectedValue().toString();
public class PlayerSelectionChangeListener implements ItemListener {
   private final AppView appView;
   private final GameEngine model;

   public PlayerSelectionChangeListener(GameEngine model, AppView appView) {
      this.appView = appView;
      this.model = model;
   }

   @Override
   public void itemStateChanged(ItemEvent event) {
      if (event.getStateChange() == ItemEvent.SELECTED) {

         @SuppressWarnings("unchecked")
         JComboBox<AppViewHelpers.JComboBoxItem<String, String>> jPlayerSelection = ((JComboBox<AppViewHelpers.JComboBoxItem<String, String>>) event.getSource());
         @SuppressWarnings("unchecked")
         AppViewHelpers.JComboBoxItem<String, String> playerItem = (AppViewHelpers.JComboBoxItem<String, String>) jPlayerSelection.getSelectedItem();
         Debug.format("PlayerSelectionChangeListener: %s, %s\n", playerItem.getName(), playerItem.getValue());
         Player player;
         appView.setActivePlayer(player = model.getPlayer(playerItem.getValue()));
         int playerId;
         if ((playerId = Integer.parseInt(playerItem.getValue())) > -1) {
            Player activePlayer = appView.getActivePlayer();
//            if (activePlayer != null) {
            appView.activatePlayerPanel(activePlayer);
//            }
         }
         appView.setBetEnabled(playerId > 0 && !appView.getGameEngine().isPlayerDealt(player));
      }
   }
}
