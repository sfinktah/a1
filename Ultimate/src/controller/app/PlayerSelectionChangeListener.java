package controller.app;

import model.interfaces.BlackEngine;
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
   private final BlackEngine model;

   public PlayerSelectionChangeListener(BlackEngine model, AppView appView) {
      this.appView = appView;
      this.model = model;
   }

   @Override
   public void itemStateChanged(ItemEvent event) {
      if (event.getStateChange() == ItemEvent.SELECTED) {

         @SuppressWarnings("unchecked")
         JComboBox<AppViewHelpers.JComboBoxItem<String, String>> jPersonSelection = ((JComboBox<AppViewHelpers.JComboBoxItem<String, String>>) event.getSource());
         @SuppressWarnings("unchecked")
         AppViewHelpers.JComboBoxItem<String, String> playerItem = (AppViewHelpers.JComboBoxItem<String, String>) jPersonSelection.getSelectedItem();
         Debug.format("PlayerSelectionChangeListener: %s, %s\n", playerItem.getName(), playerItem.getValue());
         Player player;
         appView.setActivePerson(player = model.getPerson(playerItem.getValue()));
         int playerId;
         if ((playerId = Integer.parseInt(playerItem.getValue())) > -1) {
            Player activePerson = appView.getActivePerson();
//            if (activePerson != null) {
            appView.activatePersonPanel(activePerson);
//            }
         }
         appView.setBetEnabled(playerId > 0 && !appView.getBlackEngine().isPersonDealt(player));
      }
   }
}
