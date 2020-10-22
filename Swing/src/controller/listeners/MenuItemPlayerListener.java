package controller.listeners;

import model.BlackEngineImplEx;
import view.AppView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuItemPlayerListener implements ActionListener {
   private final BlackEngineImplEx model;
   private final AppView appView;

   public MenuItemPlayerListener(BlackEngineImplEx model, AppView appView) {
      this.model = model;
      this.appView = appView;
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      JMenuItem selectedItem = (JMenuItem) e.getSource();
      JMenu viewMenu = appView.getViewMenu();
      for (int i = 0; i < viewMenu.getItemCount(); i++) {
         if (viewMenu.getItem(i) == selectedItem) {
            appView.activatePersonPanel(model.getPersonList().at(i));
            return;
         }
      }
   }
}
