package view;

import model.interfaces.BlackEngine;
import model.interfaces.Player;
import model.interfaces.PokerCard;
import view.interfaces.BlackEngineCallback;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

public class BlackEngineCallbackGUI implements BlackEngineCallback {

   private final BlackEngine gameEngine;
   private AppView appView = null;

   public BlackEngineCallbackGUI(BlackEngine gameEngine, AppView appView) {
      this.gameEngine = gameEngine;

      if (Objects.nonNull(appView)) {
         this.appView = appView;
         appView.setVisible(true);
      }
   }

   /**
    * <pre>called for each card as the house is dealing to a Player, use this to
    * update your display for each card or log to console
    *
    * @param player
    *            the Player who is receiving cards
    * @param card
    *            the next card that was dealt
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see BlackEngine
    * </pre>
    */
   @Override
   public void nextCard(Player player, PokerCard card, BlackEngine engine) {
      callOnNewCard(player, card, false);
   }

   /**
    * <pre>
    * called when the card causes the player to bust
    * this method is called instead of {@link #nextCard(Player, PokerCard, BlackEngine)}
    * this method is called before {@link #result(Player, int, BlackEngine)}
    * use this to update your display for each card or log to console
    *
    * NOTE: If player gets 42 exactly then this method IS NOT called
    *
    * @param player
    *             the Player who is receiving cards
    * @param card
    *             the bust card that was dealt
    * @param engine
    *             a convenience reference to the engine so the receiver can call
    *             methods if necessary
    * @see BlackEngine
    * </pre>
    */
   @Override
   public void bustCard(Player player, PokerCard card, BlackEngine engine) {
      callOnNewCard(player, card, true);
   }

   /**
    * <pre>called after the player has bust with final result (result is score prior
    * to the last card that caused the bust)
    *
    * Called from {@link BlackEngine#dealPerson(Player, int)}
    *
    * @param player
    *            the current Player
    * @param result
    *            the final score of the hand
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see BlackEngine
    * </pre>
    */
   @Override
   public void result(Player player, int result, BlackEngine engine) {
      appView.onPersonResult(player, result);

      // Check if it's time for the house to deal
      if (appView.getBlackEngine().getPersonsDealt().size() >= appView.getBlackEngine().getSortedPersonCount() - 1) {
         appView.getDealAllButton().setEnabled(false);
         // It's an invisible button, but we are using it to trigger an action, so we have to enable it
         appView.getDealHouseButton().setEnabled(true);
         appView.setDealHouseTimer(new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               appView.getDealHouseButton().doClick();
            }
         }));
         appView.setStatus("House will deal shortly...");
         appView.getDealHouseTimer().start();
         appView.getDealHouseTimer().setRepeats(false);
      }

   }


   /**
    * <pre>called as the house is dealing their own hand, use this to update your
    * display for each card or log to console
    *
    * @param card
    *            the next card that was dealt
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see BlackEngine
    * </pre>
    */
   @Override
   public void nextHouseCard(PokerCard card, BlackEngine engine) {
      callOnNewCard(gameEngine.getPerson("0"), card, false);
   }

   /**
    * <pre>HOUSE version of
    * {@link BlackEngineCallback#bustCard(Player, PokerCard, BlackEngine)}
    *
    * @param card
    *            the bust card that was dealt
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see BlackEngine
    * </pre>
    */
   @Override
   public void houseBustCard(PokerCard card, BlackEngine engine) {
      callOnNewCard(gameEngine.getPerson("0"), card, true);
   }

   private void callOnNewCard(Player player, PokerCard card, boolean b) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            if (appView.getBlackEngine().isPersonFinished(player)) {
               appView.clearCards(player);
               appView.getBlackEngine().setPersonFinished(player, false);
            }
            appView.getBlackEngine().setPersonDealt(player);
            appView.setBetEnabled(false);

            appView.onNewCard(player, card, b);
         }
      });
   }

   /**
    * <pre>called when the HOUSE has bust with final result (result is score prior to the last card
    * that caused the bust)
    *
    * NOTE: If house gets 42 exactly then this method IS NOT called
    *
    * PRE-CONDITION: This method should only be called AFTER bets have been updated on all Persons
    * so this callback can log Player results
    *
    * Called from {@link BlackEngine#dealHouse(int)}
    *
    * @param result
    *            the final score of the dealers (house) hand
    * @param engine
    *            a convenience reference to the engine so the receiver can call
    *            methods if necessary
    * @see BlackEngine
    * </pre>
    */
   @Override
   public void houseResult(int result, BlackEngine engine) {
      // The dealer is player 0
      gameEngine.getPerson("0").setResult(result);
      appView.onHouseResult(result);

      // reset the set of dealt players, so they can bet and deal again
      appView.getBlackEngine().clearDealtPersons();
   }


}
