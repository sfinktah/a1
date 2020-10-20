package controller.app;

import view.AppView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class PlayerTableListSelectionListener implements ListSelectionListener {
   private final AppView appView;

   public PlayerTableListSelectionListener(AppView appView) {
      this.appView = appView;
   }

   public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting()) {
         return;
      }

      ListSelectionModel selectionModel = (ListSelectionModel) e.getSource();
      if (!selectionModel.isSelectionEmpty()) {
         int selectedRow = selectionModel.getMinSelectionIndex();
         appView.getPlayerSelection().setSelectedIndex(selectedRow + 1);
      }
   }
}
