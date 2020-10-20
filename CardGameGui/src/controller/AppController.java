package controller;

import controller.listeners.*;
import model.GameEngineImplEx;
import view.AppView;
import viewmodel.PlayerList;

import javax.swing.*;
import java.util.concurrent.Executor;

// https://medium.com/@ssaurel/learn-to-make-a-mvc-application-with-swing-and-java-8-3cd24cf7cb10
// https://stackoverflow.com/questions/5217611/the-mvc-pattern-and-swing
// https://www.techyourchance.com/thread-safe-observer-design-pattern-in-java/

public class AppController {
   public static final int DELAY = 100;
   private final GameEngineImplEx model;
   private final AppView appView;
   private final Executor executor;

   public AppController(GameEngineImplEx model, AppView appView) {
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
            appView.getAddPlayerButton().addActionListener(new AddPlayerButtonListener(model));
            appView.getDealButton().addActionListener(new DealActionListener(model, appView, executor));
            appView.getRemovePlayerButton().addActionListener(new RemovePlayerButtonListener(model, appView));
            appView.getSetButton().addActionListener(new SetBetButtonListener(model, appView));
            appView.getClearButton().addActionListener(new ClearBetButtonListener(model, appView));

            appView.getActionRouter().acceptActionRoute("MenuItem", new MenuItemListener(model, appView));
            appView.getActionRouter().acceptActionRoute("MenuItemPlayer", new MenuItemPlayerListener(model, appView));

            appView.getPlayerSelection().addItemListener(new PlayerSelectionChangeListener(model, appView));
            appView.getSummaryTable().getSelectionModel().addListSelectionListener(new SummaryTableListSelectionListener(appView));

            model.getPlayerList().addEventListener("AppView", appView, (PlayerList.ListListenerSwing) appView::onPlayerListChange);
         }
      });
   }
}