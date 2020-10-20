package view.interfaces;

import model.interfaces.GameEngine;
import model.interfaces.Player;

@SuppressWarnings("unused")
public interface GameEngineCallbackEx {
   // No longer needed
   // void addPlayer(Player player, GameEngine engine);

   // No longer needed
   // void removePlayer(Player player, GameEngine engine);

   void setBet(Player player, int bet, GameEngine engine);

   void setStatus(String s);
}
