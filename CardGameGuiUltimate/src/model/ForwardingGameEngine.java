package model;

import model.interfaces.GameEngine;
import model.interfaces.Player;
import model.interfaces.PlayingCard;
import view.interfaces.GameEngineCallback;

import java.util.Collection;
import java.util.Deque;

// Item 18: Favor composition over inheritance [Bloch17]
// Reusable forwarding class
public class ForwardingGameEngine implements GameEngine {
   private final GameEngine gameEngine;

   public ForwardingGameEngine(GameEngine gameEngine) {
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

   public void dealPlayer(Player player, int delay) throws IllegalArgumentException {
      gameEngine.dealPlayer(player, delay);
   }

   public void dealHouse(int delay) throws IllegalArgumentException {
      gameEngine.dealHouse(delay);
   }

   public void applyWinLoss(Player player, int houseResult) {
      gameEngine.applyWinLoss(player, houseResult);
   }

   public void addPlayer(Player player) {
      gameEngine.addPlayer(player);
   }

   public void addGameEngineCallback(GameEngineCallback gameEngineCallback) {
      gameEngine.addGameEngineCallback(gameEngineCallback);
   }

   public boolean removePlayer(Player player) {
      return gameEngine.removePlayer(player);
   }

   public boolean placeBet(Player player, int bet) {
      return gameEngine.placeBet(player, bet);
   }

   public boolean removeGameEngineCallback(GameEngineCallback gameEngineCallback) {
      return gameEngine.removeGameEngineCallback(gameEngineCallback);
   }

   public Player getPlayer(String id) {
      return gameEngine.getPlayer(id);
   }

   public Collection<Player> getAllPlayers() {
      return gameEngine.getAllPlayers();
   }

   public Deque<PlayingCard> getShuffledHalfDeck() {
      return gameEngine.getShuffledHalfDeck();
   }
}
