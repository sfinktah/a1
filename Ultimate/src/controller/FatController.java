package controller;

import controller.app.*;
import model.interfaces.BlackEngine;
import model.interfaces.Player;
import view.AppView;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// https://medium.com/@ssaurel/learn-to-make-a-mvc-application-with-swing-and-java-8-3cd24cf7cb10
// https://stackoverflow.com/questions/5217611/the-mvc-pattern-and-swing
// https://www.techyourchance.com/thread-safe-observer-design-pattern-in-java/

public class FatController {
   static Executor executor;
   private final BlackEngine model;
   private final AppView appView;

   public FatController(BlackEngine model, AppView appView) {
      this.model = model;
      this.appView = appView;

      // Create a single threaded Executor, which is much better for queuing stuff in another thread
      // **without** having multiple things happen at once.
      executor = Executors.newSingleThreadExecutor();
      initView();
      initController();
   }

   public static int getNewPersonId(BlackEngine model) {
      int highestId = 0;
      for (Player player : model.getAllPersons()) {
         int id;
         if ((id = Integer.parseInt(player.getPersonId())) > highestId) {
            highestId = id;
         }
      }
      return ++highestId;
   }

   @SuppressWarnings("EmptyMethod")
   public void initView() {
      // It turns out, we don't need to inform the view of our controller -- must be good MVC!

      // appView.setController(this);
   }

   /**
    * Add listeners to the view
    */
   public void initController() {
      appView.getAddPersonButton().addActionListener(new AddPlayerButtonListener(model));
      appView.getDealAllButton().addActionListener(new DealAllActionListener(model, appView, executor, false));
      appView.getDealButton().addActionListener(new DealActionListener(model, appView, executor));
      appView.getDealHouseButton().addActionListener(new DealAllActionListener(model, appView, executor, true));
      appView.getPersonSelection().addItemListener(new PlayerSelectionChangeListener(model, appView));
      appView.getRemovePersonButton().addActionListener(new RemovePlayerButtonListener(model, appView));
      appView.getSetButton().addActionListener(new SetBetButtonListener(model, appView));
      appView.getClearButton().addActionListener(new ClearBetButtonListener(appView));

      appView.getSummaryTable().getSelectionModel().addListSelectionListener(new PlayerTableListSelectionListener(appView));
   }
}