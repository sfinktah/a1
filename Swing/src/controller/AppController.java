package controller;

import controller.listeners.*;
import model.BlackEngineImplEx;
import view.AppView;
import viewmodel.PersonList;

import javax.swing.*;
import java.util.concurrent.Executor;

// https://medium.com/@ssaurel/learn-to-make-a-mvc-application-with-swing-and-java-8-3cd24cf7cb10
// https://stackoverflow.com/questions/5217611/the-mvc-pattern-and-swing
// https://www.techyourchance.com/thread-safe-observer-design-pattern-in-java/

public class AppController {
   public static final int DELAY = 100;
   private final BlackEngineImplEx model;
   private final AppView appView;
   private final Executor executor;

   public AppController(BlackEngineImplEx model, AppView appView) {
      this.model = model;
      this.appView = appView;
      this.executor = model.getExecutor();
      initController();
   }

   /**
    * Add listeners to the view
    */
   public void initController() {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            appView.getAddPersonButton().addActionListener(new AddPlayerButtonListener(model));
            appView.getDealButton().addActionListener(new DealActionListener(model, appView, executor));
            appView.getRemovePersonButton().addActionListener(new RemovePlayerButtonListener(model, appView));
            appView.getSetButton().addActionListener(new SetBetButtonListener(model, appView));
            appView.getClearButton().addActionListener(new ClearBetButtonListener(model, appView));

            appView.getActionRouter().acceptActionRoute("MenuItem", new MenuItemListener(model, appView));
            appView.getActionRouter().acceptActionRoute("MenuItemPerson", new MenuItemPlayerListener(model, appView));

            appView.getPersonSelection().addItemListener(new PlayerSelectionChangeListener(model, appView));
            appView.getSummaryTable().getSelectionModel().addListSelectionListener(new SummaryTableListSelectionListener(appView));

            model.getPersonList().addEventListener("AppView", appView, (PersonList.ListListenerSwing) appView::onPersonListChange);
         }
      });
   }
}