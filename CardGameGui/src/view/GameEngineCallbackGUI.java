package view;

import model.GameEngineImplEx;
import model.interfaces.GameEngine;
import model.interfaces.Player;
import model.interfaces.PlayingCard;
import util.Timers;
import view.interfaces.GameEngineCallback;
import viewmodel.PlayerWrapper;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

// @SuppressWarnings({"unused", "RedundantSuppression"})
public class GameEngineCallbackGUI implements GameEngineCallback {

   private final GameEngineImplEx gameEngineImplEx;
   private final AppView appView;

   public GameEngineCallbackGUI(GameEngineImplEx gameEngine, AppView appView) {
      this.appView = appView;
      this.gameEngineImplEx = gameEngine;
   }

   @Override
   public void nextCard(Player player, PlayingCard card, GameEngine engine) {
      callOnNewCard(player, card, false);
   }

   @Override
   public void bustCard(Player player, PlayingCard card, GameEngine engine) {
      callOnNewCard(player, card, true);
   }

   @Override
   public void nextHouseCard(PlayingCard card, GameEngine engine) {
      callOnNewCard(gameEngineImplEx.getPlayerList().getDealer(), card, false);
   }

   @Override
   public void houseBustCard(PlayingCard card, GameEngine engine) {
      callOnNewCard(gameEngineImplEx.getPlayerList().getDealer(), card, true);
   }

   @Override
   public void result(Player player, int result, GameEngine engine) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            appView.onPlayerResult(gameEngineImplEx.getPlayerList().get(player), result);
         }});
   }

   private void callOnNewCard(Player player, PlayingCard card, boolean bustCard) {
      PlayerWrapper playerWrapper = gameEngineImplEx.getPlayerList().get(player);
      gameEngineImplEx.setPlayerDealt(playerWrapper, true, appView::setStatusOnADT);
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            appView.onSetBetEnabled(false);
            appView.onNewCard(gameEngineImplEx.getPlayerList().get(playerWrapper), card, bustCard);
         }
      });
   }

   @Override
   public void houseResult(int result, GameEngine engine) {
      gameEngineImplEx.getPlayerList().getDealer().setResult(result);
      gameEngineImplEx.getPlayerList().getDealer().has("dealt", true);

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

      ArrayList<PlayerWrapper> removalRequired = new ArrayList<>();
      for (PlayerWrapper player : gameEngineImplEx.getPlayerList().withoutDealer()) {
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
