package viewmodel;

import model.interfaces.Player;
import util.StringHelpers;

import java.awt.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class PersonWrapper implements Player, Cloneable, Serializable {

   public static final float HUE_OFFSET_PER_PLAYER = 180f + 30f;
   @SuppressWarnings("unused")
   static protected final int INDEX_UNSET = -2;
   @SuppressWarnings("unused")
   static protected final int INDEX_INVALID = -1;
   @SuppressWarnings("unused")
   static protected final int INDEX_MINIMUM = 0;
   transient protected final Set<String> attributes = new HashSet<>();
   transient private final Player player;
   transient private final PersonList playerList;
   transient protected int index;

   protected PersonWrapper(PersonList playerList, Player player) {
      this(playerList, player, false);
   }

   protected PersonWrapper(PersonList playerList, Player player, boolean clone) {
      this.playerList = playerList;
      this.player = player;
      if (!clone) {
         if (playerList.sortedPersons.contains(player)) {
            this.index = playerList.getPersonIndex(player);
         }
         else {
            this.index = playerList.sortedPersons.size();
         }
         playerList.getListReference().add(this);
         playerList.addEventListener("reindexEvent", this, this::reindexEvent);
      }
   }

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();
      s.writeChars(" ^" + index + ";" + player.getPersonId() + "#" + player.getPersonName() + ":" + player.getPoints() + "$ ");
   }

   private void reindexEvent(PersonList.ListEvent listEvent) {
      if (index == -1) {
         playerList.removeEventListener("reindexEvent", this);
      }
      int type = listEvent.getID();
      PersonWrapper pw = ((PersonWrapper) listEvent.getSource());
      if (type == PersonList.ListEvent.ITEM_REMOVED) {
         if (index > pw.index) {
            index--;
         }
         else if (index == pw.index) {
            // side effect: every successive entry will be reduced by 1, since it will be compared to
            // -1. Fortunately this makes no difference, as we have to lower all following entries anyway.
            index = -1;
         }
      }
      else if (type == PersonList.ListEvent.ITEM_ADDED) {
         if (pw.index <= index && pw != this) {
            throw new IllegalArgumentException("PersonWrapper: " + getPersonName() + " says: tried to add index that was lower than expected. "
                  + pw.index + " <= " + index);
         }
      }
   }

   /**
    * Generates a unique-ish color using a hue shift on #009900, rotating HUE_OFFSET_PER_PLAYER degrees per playerId.
    *
    * @return A new and unique java.awt.Color
    */
   public Color getColor() {
      return new Color(
            Color.HSBtoRGB(
                  (130f / 360f) + (HUE_OFFSET_PER_PLAYER
                        / 360f * StringHelpers.stringAsInt(player.getPersonId(),
                        0)),
                  0.7f,
                  1.0f
            ));
   }

   public int getIndex() {
      return index;
   }

   public int getSortedIndex() {
      return playerList.getPersonIndex(player);
   }

   public int getSortedPersonIndex() {
      return playerList.getPersonIndex(player);
   }

   @SuppressWarnings("UnusedReturnValue")
   public boolean remove() {
      if (playerList.sortedPersons.contains(player)) {
         PersonWrapper pwc = ((PersonWrapper) clone());
         playerList.dispatchEvent(PersonList.ListEvent.ITEM_REMOVED, pwc);
         playerList.sortedPersons.remove(player);
         playerList.getListReference().remove(this);
         playerList.removeEventListener("reindexEvent", this);
         return true;
      }
      return false;
   }

   @SuppressWarnings({"CloneDoesntDeclareCloneNotSupportedException", "MethodDoesntCallSuperMethod"})
   @Override
   protected Object clone() {
      PersonWrapper pw = new PersonWrapper(playerList, player, true);
      pw.index = index;
      pw.attributes.addAll(attributes);
      return pw;
   }

   //@formatter:off
   @Override public String  getPersonId()                    { return player.getPersonId();          }
   @Override public String  getPersonName()                  { return player.getPersonName();        }

   @Override public void    setPersonName(String playerName) { player.setPersonName(playerName);     }

   @Override public int     getBet()                         { return player.getBet();               }

   @Override public int     getPoints()                      { return player.getPoints();            }

   @Override public void    setPoints(int points)            { player.setPoints(points);             }

   @Override public int     getResult()                      { return player.getResult();            }

   @Override public void    setResult(int result)            { player.setResult(result);             }

   @Override public void    resetBet()                       { player.resetBet();                    }

   @Override public boolean equals(Player player)            { return this.player.equals(player);    }

   @Override public boolean setBet(int bet)                  { return player.setBet(bet);            }

   @Override public int     compareTo(Player player)         { return this.player.compareTo(player); }
   //@formatter:on

   public boolean isDealer() {
      return getIndex() == 0;
   }

   @Override
   public String toString() {
      return "PersonWrapper{" +
            "index=" + index +
            ", sortedIndex=" + getSortedPersonIndex() +
            ", attributes=" + attributes +
            ", player=" + player +
            '}';
   }

   public boolean has(String attribute) {
      return attributes.contains(attribute);
   }

   public void has(String attribute, boolean set) {
      if (set) {
         attributes.add(attribute);
      }
      else {
         attributes.remove(attribute);
      }
   }

   public boolean isValid() {
      return getIndex() > -1;
   }
}
