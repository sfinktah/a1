package view;

import model.BlackEngineImplEx;
import model.interfaces.PokerCard;
import util.swing.containers.BackgroundImagePanel;
import util.swing.containers.ImagePanel;
import util.swing.jComponent;
import util.swing.layoutmanagers.AspectLayout;
import util.swing.layoutmanagers.CanvasLayout;
import viewmodel.PersonList;
import viewmodel.PersonWrapper;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.awt.event.KeyEvent.*;
import static javax.swing.KeyStroke.getKeyStroke;

/**
 * @author Christopher Anderson
 */
public class AppView extends JFrame {
   public static final float CARDPANEL_RATIO = 23 / 93f;
   public static final int FRAME_MIN_WIDTH = 800;
   public static final int FRAME_MIN_HEIGHT = 1080 / 2;
   private static final long serialVersionUID = 1L;
   private final BlackEngineImplEx gameEngineImplEx;
   private final HashMap<String, JPanel> playerPanels = new HashMap<>();
   private final ActionRouter actionRouter = new ActionRouter();
   private final Deque<String> statusStack = new LinkedList<>();
   private ImagePanel jBackgroundImagePanel;
   private JButton jAddPersonButton;
   private JButton jClearButton;
   private JButton jDealButton;
   private JButton jRemovePersonButton;
   private JButton jSetButton;
   private JComboBox<String> jPersonSelection;
   private JLabel jBetLabel;
   private JLabel jStatusLabel;
   private JPanel jPersonPanelsContainer;
   private JPanel jPersonPanelsContainerWrapper;
   private JScrollPane jScrollPanel;
   private JTable jSummaryTable;
   private JTextField jBetField;
   private JMenu jViewMenu;
   private PersonWrapper activePerson;
   private Timer dealHouseTimer;

   /**
    * Creates our main form/frame
    *
    * @param gameEngineImplEx an instance of BlackEngine
    */
   public AppView(BlackEngineImplEx gameEngineImplEx) {
      this.gameEngineImplEx = gameEngineImplEx;

      // disable annoying AWT logging
      Logger.getLogger("java.awt").setLevel(Level.OFF);
      Logger.getLogger("sun.awt").setLevel(Level.OFF);
      Logger.getLogger("javax.swing").setLevel(Level.OFF);


      // set app icon, title, and components.
      setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("clubs.png")));
      setTitle("My First Swing GUI by Christopher Anderson");
      initComponents();
   }

   private void initComponents() {

      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      setMinimumSize(new Dimension(FRAME_MIN_WIDTH, FRAME_MIN_HEIGHT));

      jScrollPanel = new JScrollPane();
      jPersonPanelsContainer = new JPanel();

      // When you absolutely positively need to have your panel take up exactly
      // the right amount of space for 7 cards, you have to override getPreferredSize
      jPersonPanelsContainerWrapper = new JPanel() {
         private static final long serialVersionUID = 1L;

         @Override
         public Dimension getPreferredSize() {
            if (getParent() != null) {
               float currentRatio = getWidth() / (float) getHeight();
               if (currentRatio < CARDPANEL_RATIO) {
                  return new Dimension(Math.round(getHeight() / CARDPANEL_RATIO), getHeight());
               }
               else {
                  return new Dimension(getWidth(), Math.round(getWidth() * CARDPANEL_RATIO));
               }
            }
            // if we have no parent, we can return any old thing
            return new Dimension(200, 200);
         }
      };


      jComponent.builder(jPersonPanelsContainerWrapper)
            .layout(new AspectLayout(new Dimension(93, 23)).setFill(true))
            .opaque(false)
            .add(jComponent.builder(jPersonPanelsContainer)
                  .opaque(false)
                  .boxLayout(BoxLayout.LINE_AXIS)
                  .get())
            .get();

      jBackgroundImagePanel = jComponent.builder(new BackgroundImagePanel())
            .layout(new BorderLayout())
            .opaque(true)
            .image(SimpleTestClientGUI.class.getClassLoader().getResource("table-texture-alpha.png"))
            .background(new Color(Color.HSBtoRGB(130f / 360f, 0.7f, 1.0f)))
            // add the ComboBox and Buttons that comprise the top toolbar
            .add(initToolBar(), BorderLayout.NORTH)
            // add panel containing the summary and card panels
            .add(initCenterPanel(), BorderLayout.CENTER)
            // add the status bar
            .add(initStatusPanel(), BorderLayout.SOUTH)
            .appendTo(this)
      ;

      initMenuBar();

      pack();
   }

   private JPanel initCenterPanel() {
      JPanel centerPanel = new JPanel();
      centerPanel = jComponent.builder(centerPanel)
            .boxLayout(BoxLayout.PAGE_AXIS)
            .opaque(false)
            .border(BorderFactory.createEmptyBorder(10, 10, 10, 10))
            .add(initSummaryPanel())
            .add(jPersonPanelsContainerWrapper)
            .get();
      return centerPanel;
   }

   private void initMenuBar() {
      final int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
      JMenuBar menuBar = new JMenuBar();

      // This ActionRoute is used to allow the View to create proxy ActionListeners
      // that will later be fulfilled by the Controller.
      ActionListener actionRoute = actionRouter.createActionRoute("MenuItem");

      // Note: this uses jComponent to aid in making the construction of menu
      // hierarchies clearer.  Though the syntax may grate on people who are
      // unfamiliar with jQuery style chained functions, the automatically
      // applied indents (thank-you IDE) do (it cannot be argued) force the
      // code to match the menu hierarchy.

      jComponent.builder(menuBar)
            .add(jComponent.menuBuilder("File", VK_F)

                  .add(jComponent.menuItemBuilder("New Player", VK_N, "New Player")
                        .accelerator(getKeyStroke(VK_N, MASK))
                        .action(actionRoute))

                  .add(jComponent.menuItemBuilder("Save", VK_O)
                        .accelerator(getKeyStroke(VK_S, MASK))
                        .action(actionRoute))

                  .addSeparator()

                  .add(jComponent.menuItemBuilder("Quit", VK_Q, "Exit")
                        .accelerator(getKeyStroke(VK_Q, MASK))
                        .action(actionRoute)))

            .add(jComponent.menuBuilder("Edit", VK_E)

                  .add(jComponent.menuItemBuilder("Copy", VK_C)
                        .accelerator(getKeyStroke(VK_C, MASK))
                        .action(actionRoute)))

            .add(jViewMenu = jComponent.builder(jComponent.menu("View", VK_V)).get())

            .add(jComponent.menuBuilder("Help", VK_H)

                  .add(jComponent.menuItemBuilder("About", VK_A)
                        .action(actionRoute)))

            .get();

      setJMenuBar(menuBar);
   }

   private JPanel initStatusPanel() {
      JPanel statusPanel = new JPanel();
      statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
      statusPanel.setPreferredSize(new Dimension(0, 32));
      statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
      jStatusLabel = new JLabel();
      jStatusLabel.setHorizontalAlignment(SwingConstants.LEFT);
      statusPanel.add(jStatusLabel);
      return statusPanel;
   }

   private JPanel initToolBar() {
      jBetField = new JTextField(12);
      jBetField.setPreferredSize(new Dimension(32, 32));
      jBetField.setMaximumSize(new Dimension(32, 32));
      jBetField.setMinimumSize(new Dimension(32, 32));
      jBetField.setColumns(6);
      jBetField.setText("100");

      jBetLabel = new JLabel("Bet: ", JLabel.TRAILING);
      jBetLabel.setLabelFor(jBetField);
      jBetLabel.setForeground(Color.WHITE);

      // Build the non-button part of the toolbar
      JPanel toolBar = jComponent.builder(new JPanel())
            .boxLayout(BoxLayout.LINE_AXIS)
            .opaque(false)
            .border(BorderFactory.createEmptyBorder(10, 10, 10, 10))
            .add(jPersonSelection = new JComboBox<>())
            .add(Box.createRigidArea(new Dimension(10, 0)))
            .add(jBetLabel)
            .add(jBetField)
            .add(Box.createRigidArea(new Dimension(10, 0)))
            .get();


      // add the buttons to the toolbar
      jSetButton = jComponent.builder(new JButton())
            .text("Set")
            .toolTipText("Set Bet for Current Player")
            .appendTo(toolBar);
      jDealButton = jComponent.builder(new JButton())
            .text("Deal")
            .toolTipText("Deal Current Player")
            .appendTo(toolBar);
      jClearButton = jComponent.builder(new JButton())
            .text("Clear")
            .toolTipText("Clear Bet for Current Player")
            .appendTo(toolBar);

      // add some glue, to separate the buttons when resized
      toolBar.add(Box.createHorizontalGlue());

      jRemovePersonButton = jComponent.builder(new JButton())
            .text("Remove Player")
            .toolTipText("Remove Current Player")
            .appendTo(toolBar);
      jAddPersonButton = jComponent.builder(new JButton())
            .text("Add Player")
            .toolTipText("Add New Player")
            .appendTo(toolBar);

      // Disable the many buttons that shouldn't be enabled unless the player is ready and able to bet
      setBetEnabledInternal(false);

      JMenuItem mi = new JMenuItem("Copy", KeyEvent.VK_C);
      mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
      return toolBar;
   }

   private JPanel initSummaryPanel() {
      jSummaryTable = new SummaryTable(gameEngineImplEx);

      jScrollPanel = jComponent.builder(new JScrollPane(jSummaryTable))
            .minimumSize(new Dimension(600, 180))
            .preferredSize(new Dimension(600, 180))
            .get();
      jScrollPanel.getViewport().setBackground(Color.BLACK);

      return jComponent.builder(new JPanel())
            .opaque(false)
            .boxLayout(BoxLayout.PAGE_AXIS)
            .add(jScrollPanel)
            .get();
   }

   /**
    * @param enabled should the betting and dealing buttons be enabled or disabled
    */
   public void setBetEnabledInternal(boolean enabled) {
      jBetLabel.setForeground(enabled ? Color.WHITE : Color.GRAY);
      jBetField.setEnabled(enabled);
      jDealButton.setEnabled(enabled);
      jSetButton.setEnabled(enabled);
      jClearButton.setEnabled(enabled);
      jRemovePersonButton.setEnabled(enabled);
   }

   /**
    * @param enabled should the betting and dealing buttons be enabled or disabled
    */
   public void onSetBetEnabled(boolean enabled) {

      setBetEnabledInternal(enabled);
   }

   /**
    * Observer/Listener for events from PersonList. This takes the place of manual notifications
    * that were previously sent from the removePerson and addPerson methods of BlackEngineImplEx.
    *
    * @param listEvent details the player involed, and whether they are being added or removed
    */
   public void onPersonListChange(PersonList.ListEvent listEvent) {

      int type = listEvent.getID();
      PersonWrapper pw = (PersonWrapper) listEvent.getSource();
      if (type == PersonList.ListEvent.ITEM_REMOVED) {
         onPersonLeft(pw);
         setStatus("Removed Player: <b>" + pw.getPersonName());
      }
      else if (type == PersonList.ListEvent.ITEM_ADDED) {
         onPersonJoined(pw);
         setStatus("Added Player: <b>" + pw.getPersonName());
      }
   }

   private void onPersonLeft(PersonWrapper player) {

      DefaultTableModel tableModel = getSummaryTableDefaultModel();
      tableModel.removeRow(player.getIndex());
      jPersonSelection.removeItemAt(player.getIndex());
      jViewMenu.remove(player.getIndex());

      playerPanels.get(player.getPersonId())
            .getParent()
            .remove(playerPanels.get(player.getPersonId()));
   }

   private void onPersonJoined(PersonWrapper player) {

      DefaultTableModel tableModel = getSummaryTableDefaultModel();
      tableModel.addRow(new Object[]{player.getPersonName(), player.getPoints()});

      // Create a new panel to contain this players dealt cards
      JPanel jPersonPanel = jComponent.builder(new JPanel())
            .opaque(false)
            .layout(new CanvasLayout())
            .appendTo(jPersonPanelsContainer);

      // add panel to map of panels
      playerPanels.put(player.getPersonId(), jPersonPanel);
      jPersonSelection.addItem(player.getPersonName());
      jPersonSelection.updateUI();

      // Add player to the View Menu, using an ActionRoute to connect to the
      // pre-existing ActionListener established by the Controller
      jComponent.builder(new JMenuItem())
            .text(player.getPersonName())
            .action(actionRouter.createActionRoute("MenuItemPerson"))
            .appendTo(jViewMenu);

      // Make this the active panel
      activatePersonPanel(player);
   }

   public JButton             getAddPersonButton()     {  return jAddPersonButton;                       }
   public JButton             getClearButton()         {  return jClearButton;                           }
   public JButton             getDealButton()          {  return jDealButton;                            }
   public JButton             getRemovePersonButton()  {  return jRemovePersonButton;                    }
   public JButton             getSetButton()           {  return jSetButton;                             }
   public JTable              getSummaryTable()        {  return jSummaryTable;                          }
   public JTextField          getBetField()            {  return jBetField;                              }
   public JMenu               getViewMenu()            {  return jViewMenu;                              }
   public PersonWrapper       getActivePerson()        { return activePerson;                           }
   public void                setActivePerson(PersonWrapper player)
                                                       { activePerson = player;                         }
   public Timer               getDealHouseTimer()      { return dealHouseTimer;                         }
   public void                setDealHouseTimer(Timer dealHouseTimer) 
                                                       { this.dealHouseTimer = dealHouseTimer;          }
   public JPanel              getPersonPanel(PersonWrapper player)
                                                       {  return playerPanels.get(player.getPersonId()); }
   public JComboBox<String>   getPersonSelection()     {  return jPersonSelection;                       }
   public void                setBetFieldValue(int bet){  jBetField.setText(String.valueOf(bet));        }
   //@formatter:on

   public void setStatusOnADT(String status) {
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            setStatus(status);
         }
      });
   }
   public void setStatus(String status) {

      // Wrap the status text to prevent overflow
      String wrapped = status;
      final int maxChars = jStatusLabel.getParent().getWidth() / 7;
      if (status.length() > maxChars) {
         wrapped = status.substring(0, util.MyMath.MyClamp(maxChars, 0, status.length())) + "…";
      }
      // Prepend <html> tag to allow status messages to use <b>bold</b> and other
      // HTML stuff without having to specifically write "<html>" at the start of
      // every setStatus() call.
      jStatusLabel.setText("<html>" + wrapped);

      // Clear previously stacked status, as they'll be out-of-date now
      statusStack.clear();
   }

   /**
    * pushes previous status text into stack, and restores when passed a null or empty string.
    * used by MouseOver events that need to restore the status after the mouse has left a
    * playing card.
    *
    * <p>note: stack is erased when regular setStatus() is called to remove outdated status text
    *
    * @param status new status bar text, or null to pop from stack
    */
   public void setStatusStacked(String status) {

      if (status != null && status.length() > 0) {
         statusStack.push(jStatusLabel.getText());
         jStatusLabel.setText("<html>" + status);
      }
      else if (statusStack.size() > 0) {
         jStatusLabel.setText(statusStack.pop());
      }
      else {
         // stack underflow (not an error, just a result of stack being
         // reset by a call to setStatus()
         jStatusLabel.setText("");
      }
   }

   /**
    * Called by {@link BlackEngineCallbackGuiEx} when a bet has been placed.
    *
    * @param player who placed the bet
    */
   public void onBetPlaced(PersonWrapper player) {

      DefaultTableModel tableModel = getSummaryTableDefaultModel();
      tableModel.setValueAt(player.getBet(), player.getIndex(), 2);
      tableModel.setValueAt("", player.getIndex(), 4);
   }


   /**
    * Hides all the player deal panels (the panels that wrap the cards dealt to each player), and shows
    * only the panel for the selected player (who becomes the active player).
    *
    * Also updates the status bar, and the player table selection.
    *
    * Note: this is a little vestigial, as each player's panel used to have it's own background texture,
    * but there is little gain (and perhaps a loss due to lack of a JPanel's innate buffering capability)
    * in converting to a single panel which must be redrawn each time a player-switch occurs.
    *
    * @param player The new active player
    */
   public void activatePersonPanel(PersonWrapper player) {
      // Since this is the method called on every change in player, it's
      // the best place to insert some fail-fast checks.
      if (player == null) {
         throw new IllegalArgumentException("activatePersonPanel: Attempt to activate panel for null player");
      }
      JPanel panel = getPersonPanel(player);
      if (panel == null) {
         throw new IllegalArgumentException("activatePersonPanel: getPersonPanel returned null");
      }

      // Hide all panels that are not the desired player's panel
      for (Component component : jPersonPanelsContainer.getComponents()) {
         if (component != panel) {
            component.setVisible(false);
         }
         else {
            // update the background color to match the desired player's color
            jBackgroundImagePanel.setBackground(player.getColor());
            component.setVisible(true);
            setActivePerson(player);
               setStatus("" + player.getPersonId() +
                     "#" + player.getIndex() +
                     "  <b>" + player.getPersonName() +
                     "</b>,  chips: <i>" + player.getPoints() +
                     "</i>,  bet: <i>" + player.getBet() +
                     "</i>,  last result: <i>" + player.getResult());

            int playerIndex = gameEngineImplEx.getPersonList().get(player).getIndex();
            jSummaryTable.setRowSelectionInterval(playerIndex, playerIndex);
            jPersonSelection.setSelectedIndex(playerIndex);
         }
      }
   }

   /**
    * Called by {@link BlackEngineCallbackGui} upon a card being dealt
    * @param player who was dealt
    * @param card that was dealt
    * @param bust did the card cause the player to bust
    */
   public void onNewCard(PersonWrapper player, PokerCard card, boolean bust) {

      JPanel playerPanel = getPersonPanel(player);

      // Add a left margin if this is the first card
      if (playerPanel.getComponentCount() == 0) {
         playerPanel.add(Box.createRigidArea(new Dimension(10, 0)));
      }

      // Create playing card panel, pass it a reference to a setStatus method
      // so it can create the MouseOvers (rather than pass it our entire view)
      playerPanel.add(new PokerCardPanel(card, bust, this::setStatusStacked));

      // Card won't appear unless we trigger validation
      playerPanel.revalidate();
   }


   public void onClearCards(PersonWrapper player) {

      // A little trick to save a force repaint of the entire panel
      // by hiding the cards first
      for (Component c : getPersonPanel(player).getComponents()) {
         c.setVisible(false);
      }
      // now we actually do the removal
      getPersonPanel(player).removeAll();
   }

   // clear the result column for a given player
   public void clearResult(PersonWrapper player) {
      DefaultTableModel tableModel = getSummaryTableDefaultModel();
      tableModel.setValueAt("", player.getIndex(), 3);
   }

   // clear the bet column for all bets
   public void clearBets() {
      DefaultTableModel tableModel = getSummaryTableDefaultModel();
      for (int i = 0; i < tableModel.getRowCount(); i++) {
         tableModel.setValueAt("", i, 2);
      }
   }

   // update the result column for a given player
   public void onPersonResult(PersonWrapper player, int result) {

      DefaultTableModel tableModel = getSummaryTableDefaultModel();
      tableModel.setValueAt(player.getBet(), player.getIndex(), 2);
      tableModel.setValueAt(result, player.getIndex(), 3);
   }

   // helper to get the default table model
   public DefaultTableModel getSummaryTableDefaultModel() {
      return (DefaultTableModel) getSummaryTable().getModel();
   }

   // helper to generate a row of table data for a given player
   @SuppressWarnings("SameParameterValue")
   protected Vector<Object> makeTableRowVector(PersonWrapper player, String resultString) {
      Object[] o = new Object[]{player.getPersonName(), player.getPoints(), player.getBet(), player.getResult(), resultString};
      return new Vector<>(Arrays.asList(o));
   }

   // after the house deals, we can redraw the summary table and include
   // some pretty colored WIN/LOSS strings in the "Last Result" column
   public void onHouseResult(int houseResult) {

      setStatus("The house has <b>dealt</b> <i>(Points: " + houseResult + ")");
      DefaultTableModel tableModel = getSummaryTableDefaultModel();

      // Remove all players from table before repopulating, because we are updating
      // every row in the table, this is far quicker (easier?) than updating every cell.
      tableModel.setRowCount(0);

      for (PersonWrapper player : gameEngineImplEx.getPersonList().getAll()) {
         String resultString = "";
         if (!player.isDealer()) {
            // update the summary table with the player's results
            int playerResult = player.getResult();
            int playerBet = player.getBet();

            if (playerResult > houseResult) {
               resultString = String.format("WON  %4d", playerBet);
            }
            else if (playerResult < houseResult) {
               resultString = String.format("LOST %4d", playerBet);
            }
            else {
               resultString = "DRAW";
            }
         }

         tableModel.addRow(makeTableRowVector(player, resultString));
      }

      jSummaryTable.setRowSelectionInterval(0, 0);
   }

   public ActionRouter getActionRouter() {
      return actionRouter;
   }

   /**
    * A routing table for Observers and Observables.
    * <p>
    * Effectively allows routing of an {@link ActionEvent}. Charmingly reminiscent
    * of the classic "Signals and Slots" IPC mechanism, this allows the {@link ActionListener}
    * to be linked to a previously registered (or yet-to-be registered) proxy ActionListener.
    */
   static public class ActionRouter {
      private final Map<String, ActionListener> actionRoutes = new HashMap<>();

      /**
       * Register destination {@link ActionListener} (Observer) for {@code routeName}
       *
       * @param routeName key for this route
       * @param listener  destination listener for this route
       *
       * @see #createActionRoute(String)
       */
      public void acceptActionRoute(String routeName, ActionListener listener) {
         synchronized (actionRoutes) {
            actionRoutes.put(routeName, listener);
         }
      }

      /**
       * Register route (Observable) and return proxy {@link ActionListener}
       *
       * @param routeName key for this route
       *
       * @return a proxy ActionListener
       *
       * @see #acceptActionRoute(String, ActionListener)
       */
      private ActionListener createActionRoute(String routeName) {
         if (!actionRoutes.containsKey(routeName)) {
            synchronized (actionRoutes) {
               actionRoutes.put(routeName, null);
            }
         }
         return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               if (actionRoutes.containsKey(routeName)) {
                  ActionListener listener = actionRoutes.get(routeName);
                  if (listener != null) {
                     listener.actionPerformed(e);
                     return;
                  }
                  throw new java.lang.RuntimeException("CreateActionRoute: Null route for: " + routeName);
               }
               throw new java.lang.RuntimeException("CreateActionRoute: No action route for: " + routeName);
            }
         };
      }
   }
}