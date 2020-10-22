package model;

import model.interfaces.BlackEngine;
import model.interfaces.Player;
import view.AppView;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;


// Item 18: Favor composition over inheritance [Bloch17]
// Wrapper class - uses composition in place of inheritance
public class BlackEngineGuiDecor extends ForwardingBlackEngine {


   /**
    * An index of the {@link #sortedPersons} SortedSet
    *
    * @see #sortedPersons
    */
   private final HashMap<Player, Integer> sortedPersonIndex = new HashMap<>();
   /**
    * A sorted list of current players, used to contiguously populate the drop-down player list and table summary
    */
   private final SortedSet<Player> sortedPersons = Collections.synchronizedSortedSet(new TreeSet<>(new Comparator<Player>() {
      @Override
      public int compare(Player o1, Player o2) {
         int a = (Integer.parseInt(o1.getPersonId()));
         int b = (Integer.parseInt(o2.getPersonId()));

         return Integer.compare(a, b);
      }
   }));
   private final Set<Player> playersDealt = new HashSet<>();
   private final Set<Player> playersFinished = new HashSet<>();
   private AppView appView = null;

   public BlackEngineGuiDecor(BlackEngine gameEngine) {
      super(gameEngine);
   }

   /**
    * <b>NOTE:</b> id is unique and if another player with same id is added
    * it replaces the previous player
    *
    * @param player - to add to game
    */
   @Override
   public void addPerson(Player player) {
      super.addPerson(player);
      // add special sauce
      sortedPersons.add(player);
      indexPersons();
      appView.onNewPerson(player);
   }

   /**
    * @param player - to remove from game
    *
    * @return true if the player existed and was removed
    */
   @Override
   public boolean removePerson(Player player) {
      boolean result = super.removePerson(player);
      // add special sauce
      int playerIndex = getPersonIndex(player);
      if (playerIndex < 1) {
         String err = "Invalid playerIndex while trying to remove player " + player.toString();
         illegalArgument(err);
      }
      if (sortedPersons.remove(player)) {
         appView.onPersonLeft(playerIndex);
         indexPersons();
      }
      else {
         String err = "Couldn't remove player for sortedPersons " + player.toString();
         illegalArgument(err);
      }
      return result;
   }

   private void indexPersons() {
      int index = 0;
      for (Player p : sortedPersons) {
         getSortedPersonIndex().put(p, index++);
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

   public int getPersonIndex(Player player) {
      int index = -1;
      try {
         index = sortedPersonIndex.get(player);
      } catch (NullPointerException ignored) {
      }
      return index;
   }

   public Player getPersonByIndex(Integer index) {
      for (Map.Entry<Player, Integer> entry : sortedPersonIndex.entrySet()) {
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

   private HashMap<Player, Integer> getSortedPersonIndex() {
      return sortedPersonIndex;
   }

   public int getNumPersonDealt() {
      return playersDealt.size();
   }

   public Set<Player> getPersonsDealt() {
      return playersDealt;
   }

   public void clearDealtPersons() {
      for (Player player : getAllPersons()) {
         if (player.getPoints() > 0) {
            playersDealt.clear();
         }
         else if ((Integer.parseInt(player.getPersonId())) > 0) {
            removeBrokePerson(player);
         }
      }
   }

   protected void removeBrokePerson(Player player) {
      // Need to remove players with no money, because they can't bet or deal, therefore
      // the automatic house deal breaks.
      if (player.getPoints() < 1) {
         setStatus("Removing broke player " + player.getPersonName());
         javax.swing.Timer removeBrokePersonTimer = (new Timer(2000, new ActionListener() {
            @Override
            public synchronized void actionPerformed(ActionEvent e) {
               removePerson(player);
               setStatus("Removed broke player " + player.getPersonName());
            }
         }));

         removeBrokePersonTimer.start();
         removeBrokePersonTimer.setRepeats(false);
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


   public void setPersonFinished(Player player, boolean finished) {
      if (finished) {
         playersFinished.add(player);
      }
      else {
         playersFinished.remove(player);
      }
   }

   @SuppressWarnings("BooleanMethodIsAlwaysInverted")
   public boolean isPersonDealt(Player player) {
      return playersDealt.contains(player);
   }

   public void setPersonDealt(Player player) {
      playersDealt.add(player);
   }

   public boolean isPersonFinished(Player player) {
      return playersFinished.contains(player);
   }

   public int getSortedPersonCount() {
      return getSortedPersonIndex().size();
   }
}



