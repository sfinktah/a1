package model;

import model.interfaces.BlackEngine;
import model.interfaces.Player;
import model.interfaces.PokerCard;
import view.interfaces.BlackEngineCallback;

import java.util.Collection;
import java.util.Deque;

// Item 18: Favor composition over inheritance [Bloch17]
// Reusable forwarding class
// @SuppressWarnings("ALL")
public abstract class ForwardingGameEngine implements BlackEngine {
   private final BlackEngine gameEngine;

   public ForwardingGameEngine(BlackEngine gameEngine) {
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

      ForwardingGameEngine that = (ForwardingGameEngine) o;

      return gameEngine.equals(that.gameEngine);
   }

   @Override
   public int hashCode() {
      return gameEngine.hashCode();
   }

   @Override
   public void dealPerson(Player player, int delay) throws IllegalArgumentException {
      gameEngine.dealPerson(player, delay);
   }

   @Override
   public void dealHouse(int delay) throws IllegalArgumentException {
      gameEngine.dealHouse(delay);
   }

   @Override
   public void applyWinLoss(Player player, int houseResult) {
      gameEngine.applyWinLoss(player, houseResult);
   }

   @Override
   public void addPerson(Player player) {
      gameEngine.addPerson(player);
   }

   @Override
   public void addBlackEngineCallback(BlackEngineCallback gameEngineCallback) {
      gameEngine.addBlackEngineCallback(gameEngineCallback);
   }

   @Override
   public boolean removePerson(Player player) {
      return gameEngine.removePerson(player);
   }

   @Override
   public boolean placeBet(Player player, int bet) {
      return gameEngine.placeBet(player, bet);
   }

   @Override
   public boolean removeBlackEngineCallback(BlackEngineCallback gameEngineCallback) {
      return gameEngine.removeBlackEngineCallback(gameEngineCallback);
   }

   @Override
   public Player getPerson(String id) {
      return gameEngine.getPerson(id);
   }

   @Override
   public Collection<Player> getAllPersons() {
      return gameEngine.getAllPersons();
   }

   @Override
   public Deque<PokerCard> getShuffledHalfDeck() {
      return gameEngine.getShuffledHalfDeck();
   }
}
