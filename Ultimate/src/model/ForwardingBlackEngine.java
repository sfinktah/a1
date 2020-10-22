package model;

import model.interfaces.BlackEngine;
import model.interfaces.Player;
import model.interfaces.PokerCard;
import view.interfaces.BlackEngineCallback;

import java.util.Collection;
import java.util.Deque;

// Item 18: Favor composition over inheritance [Bloch17]
// Reusable forwarding class
public class ForwardingBlackEngine implements BlackEngine {
   private final BlackEngine gameEngine;

   public ForwardingBlackEngine(BlackEngine gameEngine) {
      this.gameEngine = gameEngine;
   }

   @Override
   public String toString() {
      return gameEngine.toString();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      ForwardingBlackEngine that = (ForwardingBlackEngine) o;

      return gameEngine.equals(that.gameEngine);
   }

   @Override
   public int hashCode() {
      return gameEngine.hashCode();
   }

   public void dealPerson(Player player, int delay) throws IllegalArgumentException {
      gameEngine.dealPerson(player, delay);
   }

   public void dealHouse(int delay) throws IllegalArgumentException {
      gameEngine.dealHouse(delay);
   }

   public void applyWinLoss(Player player, int houseResult) {
      gameEngine.applyWinLoss(player, houseResult);
   }

   public void addPerson(Player player) {
      gameEngine.addPerson(player);
   }

   public void addBlackEngineCallback(BlackEngineCallback gameEngineCallback) {
      gameEngine.addBlackEngineCallback(gameEngineCallback);
   }

   public boolean removePerson(Player player) {
      return gameEngine.removePerson(player);
   }

   public boolean placeBet(Player player, int bet) {
      return gameEngine.placeBet(player, bet);
   }

   public boolean removeBlackEngineCallback(BlackEngineCallback gameEngineCallback) {
      return gameEngine.removeBlackEngineCallback(gameEngineCallback);
   }

   public Player getPerson(String id) {
      return gameEngine.getPerson(id);
   }

   public Collection<Player> getAllPersons() {
      return gameEngine.getAllPersons();
   }

   public Deque<PokerCard> getShuffledHalfDeck() {
      return gameEngine.getShuffledHalfDeck();
   }
}
