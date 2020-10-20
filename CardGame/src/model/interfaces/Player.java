package model.interfaces;

/**
 * <pre>Assignment interface for FP representing the player
 * to be implemented by SimplePlayer class with the following constructor:
 * 	  public SimplePlayer(String id, String playerName, int initialPoints)
 *
 * NOTE: player id is unique and if another player with same id is added it replaces the previous player
 *
 * @author Caspar Ryan</pre>
 */
public interface Player extends Comparable<Player> {
   /**
    * @return human readable player name
    */
   String getPlayerName();

   /**
    * @param playerName - human readable player name
    */
   @SuppressWarnings({"unused", "RedundantSuppression"})
   void setPlayerName(String playerName);

   /**
    * @return number of points from setPoints()
    */
   int getPoints();

   /**
    * <pre>
    * @param points - for betting
    *    NOTE: (updated by {@link GameEngine#applyWinLoss(Player, int)} with each win or loss)
    *  </pre>
    */
   void setPoints(int points);

   /**
    * @return the player ID which is generated by the implementing class
    */
   String getPlayerId();

   /**
    * <b>NOTE:</b> must use resetBet() for 0 bet since not valid for this method
    *
    * @param bet - the bet in points
    *
    * @return true if bet is greater than 0 and player has sufficient points to place the bet<br>
    */
   boolean setBet(int bet);

   /**
    * @return the bet as set with setBet()
    */
   int getBet();

   /**
    * reset the bet to 0 for next round (in case player does not bet again in next round)
    */
   void resetBet();

   /**
    * @return the result of the most recent hand as set by {@link Player#setResult(int)}
    */
   int getResult();

   /**
    * <pre>
    * @param result
    *            the result of the most recent hand (updated from the GameEngine via {@link GameEngine#dealPlayer(Player, int)})
    * </pre>
    */
   void setResult(int result);

   /**
    * @param player - another player to compare with
    *
    * @return true - if the player id is equal
    */
   @SuppressWarnings({"unused", "RedundantSuppression"})
   boolean equals(Player player);

   /**
    * <pre> <b>NOTE:</b> this is the java.lang.Object @Override
    *
    * the implementation should cast and call through to the type checked method above</pre>
    *
    * @param player - another player to compare with
    *
    * @return true - if the player id is equal
    */
   @Override
   boolean equals(Object player);

   /**
    * @return if equals() is true then generated hashCode should also be equal
    */
   @Override
   int hashCode();

   /**
    * <pre>Used to compare order based on player id ascending for {@literal <}, {@literal >} or equality
    * See API docs of java.lang.Comparable{@literal <}T{@literal >} for details</pre>
    */
   @Override
   int compareTo(Player player);

   /**
    * <pre>@return
    *          a human readable String that lists the values of this Player
    *          including their last result
    *
    *          e.g. "Player: id=1, name=The Shark, bet=100, points=900, RESULT .. 38"
    *          (see OutputTrace.pdf)
    * </pre>
    */
   @Override
   String toString();
}
