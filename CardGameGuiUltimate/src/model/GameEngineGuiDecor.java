package model;

import model.interfaces.GameEngine;
import model.interfaces.Player;
import view.AppView;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;


// Item 18: Favor composition over inheritance [Bloch17]
// Wrapper class - uses composition in place of inheritance
public class GameEngineGuiDecor extends ForwardingGameEngine {


   /**
    * An index of the {@link #sortedPlayers} SortedSet
    *
    * @see #sortedPlayers
    */
   private final HashMap<Player, Integer> sortedPlayerIndex = new HashMap<>();
   /**
    * A sorted list of current players, used to contiguously populate the drop-down player list and table summary
    */
   private final SortedSet<Player> sortedPlayers = Collections.synchronizedSortedSet(new TreeSet<>(new Comparator<Player>() {
      @Override
      public int compare(Player o1, Player o2) {
         int a = (Integer.parseInt(o1.getPlayerId()));
         int b = (Integer.parseInt(o2.getPlayerId()));

         return Integer.compare(a, b);
      }
   }));
   private final Set<Player> playersDealt = new HashSet<>();
   private final Set<Player> playersFinished = new HashSet<>();
   private AppView appView = null;

   public GameEngineGuiDecor(GameEngine gameEngine) {
      super(gameEngine);
   }

   /**
    * <b>NOTE:</b> id is unique and if another player with same id is added
    * it replaces the previous player
    *
    * @param player - to add to game
    */
   @Override
   public void addPlayer(Player player) {
      super.addPlayer(player);
      // add special sauce
      sortedPlayers.add(player);
      indexPlayers();
      appView.onNewPlayer(player);
   }

   /**
    * @param player - to remove from game
    *
    * @return true if the player existed and was removed
    */
   @Override
   public boolean removePlayer(Player player) {
      boolean result = super.removePlayer(player);
      // add special sauce
      int playerIndex = getPlayerIndex(player);
      if (playerIndex < 1) {
         String err = "Invalid playerIndex while trying to remove player " + player.toString();
         illegalArgument(err);
      }
      if (sortedPlayers.remove(player)) {
         appView.onPlayerLeft(playerIndex);
         indexPlayers();
      }
      else {
         String err = "Couldn't remove player for sortedPlayers " + player.toString();
         illegalArgument(err);
      }
      return result;
   }

   private void indexPlayers() {
      int index = 0;
      for (Player p : sortedPlayers) {
         getSortedPlayerIndex().put(p, index++);
      }
   }

   /**
    * the implementation should forward the call to the player class so the bet is set per player
    *
    * @param player the player who is placing the bet
    * @param bet    the bet in points
    *
    * @return true if the player had sufficient points and the bet was valid and placed
    *
    * @see Player#setBet(int)
    */
   @Override
   public boolean placeBet(Player player, int bet) {
      boolean result = super.placeBet(player, bet);
      // add special sauce
      appView.onBetPlaced(player);
      return result;
   }

   @Override
   public void applyWinLoss(Player player, int houseResult) {
      super.applyWinLoss(player, houseResult);

   }

   public void setAppView(AppView appView) {
      this.appView = appView;
   }

   public int getPlayerIndex(Player player) {
      int index = -1;
      try {
         index = sortedPlayerIndex.get(player);
      } catch (NullPointerException ignored) {
      }
      return index;
   }

   public Player getPlayerByIndex(Integer index) {
      for (Map.Entry<Player, Integer> entry : sortedPlayerIndex.entrySet()) {
         if (entry.getValue().equals(index)) {
            return entry.getKey();
         }
      }
      String err = "Couldn't find player for index " + index;
      illegalArgument(err);
      // superfluous, but gives warning with
      return null;
   }

   /**
    * Prints an error message to stdout and throws an {@link IllegalArgumentException}
    *
    * @param err error string to pass to exception
    *
    * @throws IllegalArgumentException with specified error
    */
   public void illegalArgument(String err) throws IllegalArgumentException {
      System.err.println(err);
      throw new IllegalArgumentException(err);
   }

   private HashMap<Player, Integer> getSortedPlayerIndex() {
      return sortedPlayerIndex;
   }

   public int getNumPlayerDealt() {
      return playersDealt.size();
   }

   public Set<Player> getPlayersDealt() {
      return playersDealt;
   }

   public void clearDealtPlayers() {
      for (Player player : getAllPlayers()) {
         if (player.getPoints() > 0) {
            playersDealt.clear();
         }
         else if ((Integer.parseInt(player.getPlayerId())) > 0) {
            removeBrokePlayer(player);
         }
      }
   }

   protected void removeBrokePlayer(Player player) {
      // Need to remove players with no money, because they can't bet or deal, therefore
      // the automatic house deal breaks.
      if (player.getPoints() < 1) {
         setStatus("Removing broke player " + player.getPlayerName());
         javax.swing.Timer removeBrokePlayerTimer = (new Timer(2000, new ActionListener() {
            @Override
            public synchronized void actionPerformed(ActionEvent e) {
               removePlayer(player);
               setStatus("Removed broke player " + player.getPlayerName());
            }
         }));

         removeBrokePlayerTimer.start();
         removeBrokePlayerTimer.setRepeats(false);
      }
   }

   private void setStatus(String text) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            appView.setStatus(text);
         }
      });
   }


   public void setPlayerFinished(Player player, boolean finished) {
      if (finished) {
         playersFinished.add(player);
      }
      else {
         playersFinished.remove(player);
      }
   }

   @SuppressWarnings("BooleanMethodIsAlwaysInverted")
   public boolean isPlayerDealt(Player player) {
      return playersDealt.contains(player);
   }

   public void setPlayerDealt(Player player) {
      playersDealt.add(player);
   }

   public boolean isPlayerFinished(Player player) {
      return playersFinished.contains(player);
   }

   public int getSortedPlayerCount() {
      return getSortedPlayerIndex().size();
   }
}



