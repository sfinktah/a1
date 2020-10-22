package viewmodel;

import model.interfaces.Player;
import util.StringHelpers;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("serial")
public class PersonList implements Serializable {

   public transient final SortedSet<Player> sortedPersons = Collections.synchronizedSortedSet(new TreeSet<>(new Comparator<Player>() {
      @Override
      public int compare(Player o1, Player o2) {
         int a = StringHelpers.stringAsInt(o1.getPersonId(), -1);
         int b = StringHelpers.stringAsInt(o2.getPersonId(), -1);

         return Integer.compare(a, b);
      }
   }));
   /**
    * A sorted list of current players, used to contiguously populate the drop-down player list and table summary
    */
   private transient final ArrayList<PersonWrapper> sortedPersonWrappers = new ArrayList<>(16);
   private transient final Map<Pair<String, Object>, ListListener> eventListeners = new LinkedHashMap<>();

   private void writeObject(ObjectOutputStream s) throws IOException {
      s.defaultWriteObject();
      // Write out all elements in the proper order.
      for (PersonWrapper pw : sortedPersonWrappers) {
         s.writeObject(pw);
      }
   }

   /**
    * Dispatches a ListEvent to ListListeners, "switching" to the ADT thread.
    * if the ListListener is on a Swing thread (i.e. implements the ListListenerSwing
    * interface).
    * @param type ITEM_ADD or ITEM_REMOVE
    * @param playerWrapper the player being added or removed
    */
   public synchronized void dispatchEvent(int type, PersonWrapper playerWrapper) {
      for (ListListener listener : eventListeners.values()) {
         Runnable fn = new Runnable() {
            @SuppressWarnings({"serial", "RedundantSuppression"})
            @Override
            public void run() {
               listener.actionPerformed(new ListEvent(playerWrapper, type) { });
            }
         };
         // While it might seem easier to just dispatch all Swing event via `invokeLater`,
         // there were some difficulties with the initial player list being added OUT OF ORDER,
         // suggesting that invokeLater may not be a FIFO queue.  Therefore we only use it if
         // we have to.
         // Usage of `invokeAndWait` for synchronous dispatch may also have solved this issue.
         if (listener instanceof ListListenerSwing && !SwingUtilities.isEventDispatchThread()) {
            try {
               SwingUtilities.invokeAndWait(fn);
            } catch (InterruptedException | InvocationTargetException e) {
               e.printStackTrace();
            }
         }
         else {
            fn.run();
         }
      }
   }

   public void addEventListener(String listenerName, Object listenerObject, ListListener ll) {
      eventListeners.put(new Pair<>(listenerName, listenerObject), ll);
   }

   public void removeEventListener(String listenerName, Object listenerObject) {
      eventListeners.remove(new Pair<>(listenerName, listenerObject));
   }

   public ArrayList<PersonWrapper> getAll() {
      return new ArrayList<>(sortedPersonWrappers);
   }

   public ArrayList<PersonWrapper> getListReference() {
      return sortedPersonWrappers;
   }


   public PersonWrapper getDealer() {
      return at(0);
   }

   /**
    * Get or create {@link PersonWrapper} for {@link Player},
    * dispatching a {@link ListEvent#ITEM_ADDED} event if a
    * new player is created.
    *
    * @param player player
    *
    * @return PersonWrapper
    */
   public PersonWrapper get(Player player) {
      if (player instanceof PersonWrapper) {
         PersonWrapper playerWrapper = (PersonWrapper) player;
         if (playerWrapper.isValid())
            return playerWrapper;
         throw new ArrayIndexOutOfBoundsException("PersonWrapper::get index for "+playerWrapper.getPersonName()+" was "+playerWrapper.getIndex() + "/" + playerWrapper.getSortedIndex());
      }
      if (sortedPersons.contains(player)) {
         return at(getPersonIndex(player));
      }

      // Make new player
      PersonWrapper newPerson = new PersonWrapper(this, player);
      sortedPersons.add(player);
      dispatchEvent(ListEvent.ITEM_ADDED, newPerson);

      return newPerson;
   }

   /**
    * Translate between Player objects and the contiguous 0 based index of players we use
    * with the JComboBox and JTable
    *
    * @param index index
    *
    * @return PersonWrapper or null on failure
    */
   public PersonWrapper at(int index) {
      try {

         PersonWrapper playerWrapper = sortedPersonWrappers.get(index);
         if (playerWrapper == null)
            throw new NullPointerException("PersonWrapper::at index was null "+index);
         if (playerWrapper.isValid())
            return playerWrapper;
         throw new ArrayIndexOutOfBoundsException("PersonWrapper::get index for "+playerWrapper.getPersonName()+" was "+playerWrapper.getIndex());
//               sortedPersonWrappers.get(index);
      } catch (IndexOutOfBoundsException e) {
         return null;
      }
   }

   /**
    * Translate between Player objects and the contiguous 0 based index of players we use
    * with the JComboBox and JTable.  Used by PersonList to assign existing indexes to
    * new instantiations of PersonWrapper, e.g. when
    * {@code PersonWrapper pw = PersonList.get(player);}
    *
    * @param player player
    *
    * @return index or -1 on failure
    */
   public int getPersonIndex(Player player) {
      return sortedPersons.contains(player) ? sortedPersons.headSet(player).size() : -1;
   }

   public List<PersonWrapper> thatHave(String attribute) {
      return sortedPersonWrappers.stream().filter(new Predicate<PersonWrapper>() {
         @Override
         public boolean test(PersonWrapper playerWrapper) {
            return playerWrapper.has(attribute);
         }
      }).collect(toList());
   }

   public List<PersonWrapper> withoutDealer() {
      return sortedPersonWrappers.stream().filter(new Predicate<PersonWrapper>() {
         @Override
         public boolean test(PersonWrapper playerWrapper) {
            return !playerWrapper.isDealer();
         }
      }).collect(toList());
   }

   /**
    * The listener interface for receiving list events.
    * The class that is interested in processing a list event
    * implements this interface, and the object created with that
    * class is registered with a <code>addEventListener</code>
    * When the list event occurs, that object's
    * <code>actionPerformed</code> method is invoked.
    *
    * @see ListEvent
    */
   public interface ListListener extends EventListener {
      void actionPerformed(ListEvent e);
   }

   /**
    * This extension of ListListener will be invoked on an
    * ADT (Swing) thread if necessary.
    */
   public interface ListListenerSwing extends ListListener {
   }

   /**
    * @see PersonList.ListListener
    */
   // @SuppressWarnings({"unused", "serial", "RedundantSuppression"})
   public static class ListEvent extends EventObject {

      // This is how it is done in the AWT listeners, so we're going
      // to keep with tradition rather than use an enum.
      static public final int ACTION_FIRST = 1001;
      @SuppressWarnings("unused")
      static public final int ACTION_LAST = 1002;
      static public final int ITEM_ADDED = ACTION_FIRST;
      static public final int ITEM_REMOVED = ACTION_FIRST + 1;
      protected final int id;

      /**
       * Constructs an <code>ListEvent</code> object.
       *
       * @param source  The object that originated the event
       * @param id      An integer that identifies the event.
       *
       * @throws IllegalArgumentException if <code>source</code> is null
       * @see #getSource()
       * @see #getID()
       */
      public ListEvent(Object source, int id) {
         super(source);
         this.id = id;
      }

      /**
       * Returns the event type.
       */
      public int getID() {
         return id;
      }

      /**
       * @return a string identifying the event and its associated command
       */
      public String paramString() {
         String typeStr;
         switch (id) {
            case ITEM_ADDED:
               typeStr = "ITEM_ADDED";
               break;
            case ITEM_REMOVED:
               typeStr = "ITEM_REMOVED";
               break;
            default:
               typeStr = "unknown type";
         }
         return typeStr;
      }

      public String toString() {
         String srcName = null;
         return getClass().getName() + "[" + paramString() + "] on " +
               (srcName != null ? srcName : source);
      }
   }

   /**
    * A Java version of std::pair, modelled (but not copied) from javafx's util.Pair
    * which cannot be used without Eclipse giving out errors.
    *
    * @param <K> Key type
    * @param <V> Value type
    */
   @SuppressWarnings("unused")
   public static class Pair<K, V> {
      private final K key;
      private final V value;

      private Pair(K key, V value) {
         this.key = key;
         this.value = value;
      }

      public static <K, V> Pair<K, V> of(K key, V value) {
         return new Pair<>(key, value);
      }

      @Override
      public String toString() {
         return key + "=" + value;
      }

      @Override
      public int hashCode() {
         return key.hashCode() * 31 + (value == null ? 0 : value.hashCode());
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         }
         if (o == null || getClass() != o.getClass()) {
            return false;
         }
         Pair<?, ?> pair = (Pair<?, ?>) o;
         return key.equals(pair.key) &&
               Objects.equals(value, pair.value);
      }

      public K getKey() {
         return this.key;
      }

      public V getValue() {
         return this.value;
      }
   }
}
