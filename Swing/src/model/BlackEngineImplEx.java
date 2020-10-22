package model;

import controller.listeners.MenuItemPlayerListener;
import model.interfaces.BlackEngine;
import model.interfaces.Player;
import view.BlackEngineCallbackGuiEx;
import view.BlackEngineCallbackGui;
import view.interfaces.BlackEngineCallback;
import viewmodel.PersonList;
import viewmodel.PersonWrapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


// Item 18: Favor composition over inheritance [Bloch17]
// Wrapper class - uses composition in place of inheritance
public class BlackEngineImplEx extends ForwardingGameEngine {

   protected final Set<BlackEngineCallbackGuiEx> gameEngineCallbacks = new LinkedHashSet<>();
   private final PersonList playerList = new PersonList();

   // Create a single threaded Executor, which is much better for queuing stuff in another thread
   // **without** having multiple things happen at once.
   private final Executor executor;

   public BlackEngineImplEx(BlackEngine gameEngine) {
      super(gameEngine);
      executor = Executors.newSingleThreadExecutor();
   }

   @Override
   public boolean placeBet(Player player, int bet) {
      boolean result = super.placeBet(player, bet);
      // add special sauce
      for (BlackEngineCallbackGuiEx gecex : getBlackEngineCallbacks()) {
         gecex.setBet(player, bet, this);
      }
      // This is just to increase the UI usability.  Removing the last results when a
      // player makes a new bet makes it more obvious in the summary table.
      player.setResult(0);
      return result;
   }

   public List<BlackEngineCallbackGuiEx> getBlackEngineCallbacks() {
      return new ArrayList<>(gameEngineCallbacks);
   }

   @Override
   public void addPerson(Player player) {
      super.addPerson(player);
      // PersonList.from will detect that `player` is new, and notify
      // interested observers.
      playerList.get(player);
   }

   @Override
   public boolean removePerson(Player player) {
      // PersonList.from will notify interested observers that player has been removed
      return super.removePerson(player) && playerList.get(player).remove();
   }

   /**
    * Used by {@link BlackEngineCallbackGui} to record when a player
    * has had cards dealt to them. A list of players not yet dealt is
    * also created and passed as an argument to the setStatus Consumer.
    *
    * @param player    currently being dealt
    * @param dealt     true to set, false to clear
    * @param setStatus a function to enable status text to be updated, or null
    */
   public void setPersonDealt(PersonWrapper player, boolean dealt, Consumer<String> setStatus) {
      if (player.isDealer()) {
         return;
      }

      if (!dealt) {
         player.has("dealt", false);
         return;
      }

      player.has("dealt", true);
      int playersLeft =
            playerList.getAll().size()
                  - playerList.thatHave("dealt").size()
                  - 1;

      if (setStatus != null && playersLeft > 0) {
         // make a comma separated list of the players remaining to be dealt.
         //
         // normally, i'd use a `.filter()`, a `join(", ", x)`, and a `map()` to add the `<bold>`, but
         // we're not allowed to use lambdas, so I have elected to be more surgical and use the more
         // complex `reduce()` method to perform all the functions in one call.
         String playerNames = "";
         playerNames = playerList.getAll().stream().reduce(
               playerNames,
               this::accumulateUnDealtPersons,
               this::combinePersonNames // annoying unnecessary argument to keep Java happy, see link below:
               // https://stackoverflow.com/a/33971436/912236
         );

         setStatus.accept(String.format("%d player(s) left until the house deals <i>(%s)</i>",
               playersLeft,
               playerNames));
      }
   }

   // used in reduce()
   private String combinePersonNames(String s, String s2) {
      if (s.length() > 0) {
         return s + ", " + s2;
      }
      return s2;
   }

   // used in reduce()
   private String accumulateUnDealtPersons(String s, PersonWrapper player1) {
      if (!player1.isDealer() && !player1.has("dealt")) {
         return combinePersonNames(s, "<b>" + player1.getPersonName() + "</b>");
      }
      return s;
   }

   /**
    * Used by {@link BlackEngineCallbackGuiEx}
    * to determine if it's time for the house to deal
    *
    * @return true if all players have been dealt cards this round
    */
   public boolean isEveryoneDealt() {
      int playerCount = 0;
      for (PersonWrapper player : playerList.withoutDealer()) {
         playerCount ++;
         if (!player.has("dealt")) {
            return false;
         }
      }
      return (playerCount > 0);
   }

   public void addBlackEngineCallback(BlackEngineCallback gameEngineCallback) {
      // We'll store the Ex callbacks, and let the
      // original BlackEngineImpl store the A1 style
      // callbacks.
      if (gameEngineCallback instanceof BlackEngineCallbackGuiEx) {
         gameEngineCallbacks.add((BlackEngineCallbackGuiEx) gameEngineCallback);
         return;
      }
      super.addBlackEngineCallback(gameEngineCallback);
   }

   public boolean removeBlackEngineCallback(BlackEngineCallback gameEngineCallback) {
      if (gameEngineCallback instanceof BlackEngineCallbackGuiEx) {
         BlackEngineCallbackGuiEx gameEngineCallbackEx = (BlackEngineCallbackGuiEx) gameEngineCallback;
         return gameEngineCallbacks.remove(gameEngineCallbackEx);
      }
      return super.removeBlackEngineCallback(gameEngineCallback);
   }

   /**
    * Used by {@link BlackEngineCallbackGuiEx}, {@link controller.AppController},
    * {@link controller.listeners.MenuItemListener}, {@link BlackEngineImplEx}
    *
    * @return a single threaded {@link Executor}
    */
   public Executor getExecutor() {
      return executor;
   }

   /**
    * Returns a list of all PersonWrappers, used by
    * {@link controller.AppController}
    * {@link controller.listeners.MenuItemListener}
    * {@link MenuItemPlayerListener}
    * {@link controller.listeners.SetBetButtonListener}
    * {@link model.BlackEngineImplEx}
    * {@link view.AppView}
    * {@link BlackEngineCallbackGui}
    * {@link BlackEngineCallbackGuiEx}
    * {@link view.NewPersonDialog}
    *
    * @return the single {@link PersonList} instance
    */
   public PersonList getPersonList() {
      return playerList;
   }
}



