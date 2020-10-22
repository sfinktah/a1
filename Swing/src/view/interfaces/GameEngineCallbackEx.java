package view.interfaces;

import model.interfaces.BlackEngine;
import model.interfaces.Player;

@SuppressWarnings("unused")
public interface GameEngineCallbackEx {
   // No longer needed
   // void addPerson(Player player, BlackEngine engine);

   // No longer needed
   // void removePerson(Player player, BlackEngine engine);

   void setBet(Player player, int bet, BlackEngine engine);

   void setStatus(String s);
}
