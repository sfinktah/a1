package view;

import model.BlackEngineImplEx;
import model.interfaces.BlackEngine;
import model.interfaces.Player;
import model.interfaces.PokerCard;
import util.Timers;
import view.interfaces.BlackEngineCallback;
import viewmodel.PersonWrapper;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

// @SuppressWarnings({"unused", "RedundantSuppression"})
public class BlackEngineCallbackGui implements BlackEngineCallback {

   private final BlackEngineImplEx gameEngineImplEx;
   private final AppView appView;

   public BlackEngineCallbackGui(BlackEngineImplEx gameEngine, AppView appView) {
      this.appView = appView;
      this.gameEngineImplEx = gameEngine;
   }

   @Override
   public void nextCard(Player player, PokerCard card, BlackEngine engine) {
      callOnNewCard(player, card, false);
   }

   @Override
   public void bustCard(Player player, PokerCard card, BlackEngine engine) {
      callOnNewCard(player, card, true);
   }

   @Override
   public void nextHouseCard(PokerCard card, BlackEngine engine) {
      callOnNewCard(gameEngineImplEx.getPersonList().getDealer(), card, false);
   }

   @Override
   public void houseBustCard(PokerCard card, BlackEngine engine) {
      callOnNewCard(gameEngineImplEx.getPersonList().getDealer(), card, true);
   }

   @Override
   public void result(Player player, int result, BlackEngine engine) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            appView.onPersonResult(gameEngineImplEx.getPersonList().get(player), result);
         }});
   }

   private void callOnNewCard(Player player, PokerCard card, boolean bustCard) {
      PersonWrapper playerWrapper = gameEngineImplEx.getPersonList().get(player);
      gameEngineImplEx.setPersonDealt(playerWrapper, true, appView::setStatusOnADT);
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            appView.onSetBetEnabled(false);
            appView.onNewCard(gameEngineImplEx.getPersonList().get(playerWrapper), card, bustCard);
         }
      });
   }

   @Override
   public void houseResult(int result, BlackEngine engine) {
      gameEngineImplEx.getPersonList().getDealer().setResult(result);
      gameEngineImplEx.getPersonList().getDealer().has("dealt", true);

      try {
         // synchronous ADT thread required to prevent race condition that
         // will wipe out player bet before view can calculate results
         SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
               appView.onHouseResult(result);
            }
         });
      } catch (InterruptedException | InvocationTargetException e) {
         e.printStackTrace();
      }

      ArrayList<PersonWrapper> removalRequired = new ArrayList<>();
      for (PersonWrapper player : gameEngineImplEx.getPersonList().withoutDealer()) {
         if (player.getPoints() <= 0) {
            // mark player for later removal
            removalRequired.add(player);
         }
         player.has("dealt", false);
      }

      if (removalRequired.size() > 0) {
         gameEngineImplEx.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
               Timers.wait(3000);
               while (!removalRequired.isEmpty()) {
                  removalRequired.get(0).remove();
                  Timers.wait(1000);
                  removalRequired.remove(0);
               }
            }
         });
      }
   }
}
