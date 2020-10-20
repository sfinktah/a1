package view;

import model.GameEngineGuiDecor;
import model.SimplePlayer;
import model.interfaces.Player;
import model.interfaces.PlayingCard;
import util.Debug;
import util.StringHelpers;
import view.util.FilteredFields;
import view.util.containers.BackgroundImagePanel;
import view.util.containers.ForegroundImagePanel;
import view.util.containers.ImagePanel;
import view.util.layoutmanagers.AspectLayout;
import view.util.layoutmanagers.CanvasLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashMap;
import java.util.function.Predicate;

import static javax.swing.BoxLayout.PAGE_AXIS;
import static view.AppViewHelpers.*;
import static view.AppViewHelpers.ContainerTransforms.*;
import static view.AppViewHelpers.Images.getResourceUrl;

/**
 * @author Christopher Anderson
 */
public class AppView extends JFrame {
   public static final float CARDS_RATIO = 23 / 93f;
   public static final int FRAME_MIN_WIDTH = 800;
   public static final int FRAME_MIN_HEIGHT = 1080 / 2;
   private static final long serialVersionUID = 1L;
   private final GameEngineGuiDecor gameEngine;
   private final HashMap<String, JPanel> playerPanels = new HashMap<>();
   private JLayeredPane jLayeredPane;
   private ImagePanel jBackgroundImagePanel;
   private JButton jAddPlayerButton;
   private JButton jClearButton;
   private JButton jDealAllButton;
   private JButton jDealButton;
   private JButton jDealHouseButton;
   private JButton jRemovePlayerButton;
   private JButton jSetButton;
   private JComboBox<JComboBoxItem<String, String>> jPlayerSelection;
   private JLabel jBetLabel;
   private JLabel jStatusLabel;
   private JPanel jPlayerPanelsContainer;
   @SuppressWarnings("FieldCanBeLocal")
   private JPanel jPlayerPanelsContainerWrapper;
   private JPanel jSummaryPanel;
   private JScrollPane jScrollPanel;
   private JTable jSummaryTable;
   private JTextField jBetField;
   private Player activePlayer;
   private Timer dealHouseTimer;

   /**
    * Creates our main form/frame
    *
    * @param gameEngine an instance of GameEngine
    */
   public AppView(GameEngineGuiDecor gameEngine) {
      this.gameEngine = gameEngine;
      gameEngine.setAppView(this);

      // set app icon, title, and components.
      setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("clubs.png")));
      setTitle("My First Swing GUI by Christopher Anderson");
      initComponents();
      initPlayers();
   }

   private void initComponents() {

      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      setMinimumSize(new Dimension(FRAME_MIN_WIDTH, FRAME_MIN_HEIGHT));

      jSummaryPanel = namedComponent(JPanel.class, "jSummaryPanel");
      jScrollPanel = namedComponent(JScrollPane.class, "jScrollPanel");
      jPlayerPanelsContainer = namedComponent(JPanel.class, "jPlayerPanelsContainer");

      // When you absolutely positively need to have your panel take up exactly
      // the right amount of space for 7 cards, you have to override getPreferredSize
      jPlayerPanelsContainerWrapper = new JPanel() {
         private static final long serialVersionUID = 1L;

         @Override
         public Dimension getPreferredSize() {
            if (getParent() != null) {
               float currentRatio = getWidth() / (float) (getHeight());
               if (currentRatio < CARDS_RATIO) {
                  return new Dimension(Math.round(getHeight() / CARDS_RATIO), getHeight());
               }
               else {
                  return new Dimension(getWidth(), Math.round(getWidth() * CARDS_RATIO));
               }
            }
            return new Dimension(3, 200);
         }
      };

      jPlayerPanelsContainer.setOpaque(false);
      jPlayerPanelsContainer.setLayout(new BoxLayout(jPlayerPanelsContainer, BoxLayout.LINE_AXIS));

      jPlayerPanelsContainerWrapper.setLayout(new AspectLayout(new Dimension(93, 23)).setFill(true));
      jPlayerPanelsContainerWrapper.setOpaque(false);
      jPlayerPanelsContainerWrapper.add(jPlayerPanelsContainer);

      jStatusLabel = namedComponent(JLabel.class, "jStatusLabel");

      // create the center (main) panel, which has everything in it, except for status bar and tool bar
      JPanel centerPanel = namedComponent(JPanel.class, "centerPanel");
      centerPanel.setLayout(new BoxLayout(centerPanel, PAGE_AXIS));
      centerPanel.setOpaque(false);
      centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      // create the summary JTable within a JScrollPane
      centerPanel.add(initSummaryPanel());
      centerPanel.add(jPlayerPanelsContainerWrapper);

      // Put everything together, using a BorderLayout with a BackgroundImage
      jBackgroundImagePanel = namedComponent(BackgroundImagePanel.class, "jBackgroundImagePanel");
      jBackgroundImagePanel = new BackgroundImagePanel() {
         private static final long serialVersionUID = 1L;

         @Override
         public Dimension getPreferredSize() {
            if (jBackgroundImagePanel.getParent() != null) {
               // This works because `this` context is AppView
               Debug.println("BackgroundImagePanel.size = " + getSize());
               return new Dimension(getSize());
            }
            return new Dimension(800, 600);
         }
      };
      jBackgroundImagePanel.setLayout(new BorderLayout());
      jBackgroundImagePanel.setImage(getResourceUrl("table-texture-alpha.png"));
      jBackgroundImagePanel.setOpaque(true);
      jBackgroundImagePanel.setBackground(getPlayerColor(0));

      // add the ComboBox and Buttons that comprise the top toolbar
      jBackgroundImagePanel.add(initToolBar(), BorderLayout.NORTH);
      // add panel containing the summary and card panels
      jBackgroundImagePanel.add(centerPanel, BorderLayout.CENTER);
      // add the status bar
      jBackgroundImagePanel.add(initStatusPanel(), BorderLayout.SOUTH);

      jLayeredPane = namedComponent(JLayeredPane.class, "jLayeredPane");
      jLayeredPane.add(jBackgroundImagePanel, JLayeredPane.DEFAULT_LAYER);
      add(jLayeredPane);

      pack();

      final Container contentPane = getContentPane();
      jBackgroundImagePanel.setBounds(0, 0, contentPane.getWidth(), contentPane.getHeight());

      // need to add this resize listener, otherwise the JLayeredPane masks window resizes and non
      // of the child components grow.
      addComponentListener(new ComponentAdapter() {
         public void componentResized(ComponentEvent componentEvent) {
            SwingUtilities.invokeLater(new Runnable() {
               @Override
               public void run() {
                  jBackgroundImagePanel.setBounds(0, 0, contentPane.getWidth(), contentPane.getHeight());
                  // the revalidate is only needed for Maximise and Restore functionality, but to specifically
                  // listen for that would be a heap more code.
                  jBackgroundImagePanel.revalidate();
               }
            });
         }
      });
   }

   private JPanel initStatusPanel() {
      JPanel statusPanel = namedComponent(JPanel.class, "statusPanel");
      statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
      statusPanel.setPreferredSize(new Dimension(0, 16 + 10 + 10 - 4));
      statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
      jStatusLabel.setHorizontalAlignment(SwingConstants.LEFT);
      statusPanel.add(jStatusLabel);
      return statusPanel;
   }

   private JPanel initToolBar() {
      //@formatter: off
      jPlayerSelection = new JComboBox<>();
      jDealButton = namedComponent(JButton.class, "jDealButton");
      jSetButton = namedComponent(JButton.class, "jSetButton");
      jClearButton = namedComponent(JButton.class, "jClearButton");
      jDealAllButton = namedComponent(JButton.class, "jDealAllButton");
      jDealHouseButton = namedComponent(JButton.class, "jDealHouseButton");

      jAddPlayerButton = namedComponent(JButton.class, "jAddPlayerButton");
      jRemovePlayerButton = namedComponent(JButton.class, "jRemovePlayerButton");

      setTextAndToolTip(jDealButton, "Deal", "Deal Current Player");
      setTextAndToolTip(jSetButton, "Set", "Set Bet for Current Player");
      setTextAndToolTip(jClearButton, "Clear", "Clear Bet for Current Player");
      setTextAndToolTip(jDealAllButton, "Deal All", "Sequentially deals all un-dealt players with a bet of $100");
      setTextAndToolTip(jDealHouseButton, "Deal House", "Invisible Button, used to trigger House Deal");
      setTextAndToolTip(jAddPlayerButton, "Add Player", "Add New Player");
      setTextAndToolTip(jRemovePlayerButton, "Remove Player", "Remove Current Player");
      //@formatter: on

      jDealHouseButton.setEnabled(false);
      jDealHouseButton.setVisible(false);

      jPlayerSelection.setModel(
            new DefaultComboBoxModel<>()
      );

      jBetLabel = new JLabel("Bet: ", JLabel.TRAILING);
      jBetLabel.setForeground(Color.WHITE);
      jBetField = FilteredFields.createFilteredField("^[0-9]*$", new Predicate<String>() {
         @Override
         public boolean test(String e) {
            return e == null || e.length() == 0 || getActivePlayer() != null && Integer.parseInt(e) <= getActivePlayer().getPoints();
         }
      });
      jBetField.setPreferredSize(new Dimension(32, 32));
      jBetField.setMaximumSize(new Dimension(32, 32));
      jBetField.setMinimumSize(new Dimension(32, 32));
      jBetField.setColumns(6);
      jBetField.setText("100");
      jBetLabel.setLabelFor(jBetField);

      setBetEnabled(false);

      JPanel buttonPane = namedComponent(JPanel.class, "buttonPane");
      buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
      buttonPane.setOpaque(false);
      buttonPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      buttonPane.add(jPlayerSelection);
      buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
      buttonPane.add(jBetLabel);
      buttonPane.add(jBetField);
      buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
      buttonPane.add(jSetButton);
      buttonPane.add(jClearButton);
      buttonPane.add(jDealButton);

      // Everything to above this line will stick left
      buttonPane.add(Box.createHorizontalGlue());

      // Everything after will stick right
      buttonPane.add(jRemovePlayerButton);
      buttonPane.add(jAddPlayerButton);
      buttonPane.add(jDealAllButton);
      buttonPane.add(jDealHouseButton);
      return buttonPane;
   }

   private JPanel initSummaryPanel() {
      final CustomCellRenderer customCellRenderer = new CustomCellRenderer();
      final CustomHeaderRenderer customHeaderRenderer = new CustomHeaderRenderer();
      jSummaryTable = new JTable() {
         private static final long serialVersionUID = 1L;

         public boolean isCellEditable(int row, int column) {
            return false;
         }

         public void setValueAt(Object value, int row, int col) {
            super.setValueAt(value, row, col);
         }

         @Override
         public TableCellRenderer getCellRenderer(int row, int column) {
            return customCellRenderer;
         }
      };
      jSummaryTable.setGridColor(Color.RED);
      jSummaryTable.getTableHeader().setDefaultRenderer(customHeaderRenderer);
      jSummaryPanel.setOpaque(false);
      jSummaryPanel.setLayout(new BoxLayout(jSummaryPanel, PAGE_AXIS));

      jSummaryTable.setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{
                  "Player", "Chips", "Bet", "Result", "Last Game"
            }
      ));
      jSummaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      jSummaryTable.setRowSelectionAllowed(true);
      jSummaryTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      jSummaryTable.setFillsViewportHeight(true);
      jSummaryTable.setOpaque(true);
      jSummaryTable.setBackground(new Color(0, 0, 0));
      jSummaryTable.getTableHeader().setBackground(new Color(0, 0, 0, 1));
      jSummaryTable.getTableHeader().setForeground(new Color(255, 255, 255, 255));
      jSummaryTable.getColumnModel().getColumn(0).setMinWidth(165);
      jSummaryTable.getColumnModel().getColumn(1).setMinWidth(100);
      jSummaryTable.getColumnModel().getColumn(2).setMinWidth(75);
      jSummaryTable.getColumnModel().getColumn(3).setMinWidth(100);
      jSummaryTable.getColumnModel().getColumn(4).setMinWidth(150);
      jSummaryTable.setMinimumSize(new Dimension(600, 180));

      Font tableFont = Fonts.fontFromResource("LED_DISPLAY-7.TTF", 16);
      jSummaryTable.setFont(tableFont);

      // Pressing enter while the JTable is active deals the current player,
      // while UP and DOWN will transition through players and dealt cards
      jSummaryTable.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "deal");
      jSummaryTable.getInputMap().put(KeyStroke.getKeyStroke("1"), "bet1");

      jSummaryTable.getActionMap().put("deal",
            new AbstractAction() {
               private static final long serialVersionUID = 1L;

               @Override
               public void actionPerformed(ActionEvent e) {
                  setStatus("Dealing current player (ENTER PRESSED)");
                  getDealButton().doClick();
               }
            });

      jSummaryTable.getActionMap().put("bet1",
            new AbstractAction() {
               private static final long serialVersionUID = 1L;

               @Override
               public void actionPerformed(ActionEvent e) {
                  setStatus("Setting bet to 100%");
                  gameEngine.placeBet(getActivePlayer(), getActivePlayer().getPoints());
               }
            });

      jScrollPanel = new JScrollPane(jSummaryTable);
      jSummaryPanel.add(jScrollPanel);
      jScrollPanel.setMinimumSize(new Dimension(600, 180));
      jScrollPanel.setPreferredSize(new Dimension(600, 180));
      jScrollPanel.getViewport().setBackground(new Color(0, 0, 0));
      return jSummaryPanel;
   }

   public void initPlayers() {
      jPlayerSelection.addItem(new JComboBoxItem<>("-- Select Player --", "-1"));

      // create two test players
      Player[] players = new Player[]{
            new SimplePlayer("0", "House", 0),
            new SimplePlayer("1", "The Loser", 500),
            new SimplePlayer("2", "The Shark", 1000),
            new SimplePlayer("3", "The Gambler", 2000),
            new SimplePlayer("4", "The Rock", 1000),
            new SimplePlayer("5", "The Fish", 2000),
            new SimplePlayer("6", "The Whale", 2000),
            new SimplePlayer("7", "The Book", 3000),
            new SimplePlayer("8", "The Eagle", 1000),
      };

      for (Player player : players) {
         gameEngine.addPlayer(player);
      }

      // Adding a new player automatically activates them as the current player,
      // so we want to reverse the effects of this after init.
      activatePlayerPanel(gameEngine.getPlayerByIndex(0)); // Activate dealer panel
      getSummaryTable().clearSelection(); // Remove selection from summary table
      setStatus("Click the Summary Table and use Arrow Keys, 1, and Enter to navigate, bet, and deal.");
   }

   public void setBetEnabled(boolean enabled) {
      jBetLabel.setForeground(enabled ? Color.WHITE : Color.GRAY);
      jBetField.setEnabled(enabled);
      jDealButton.setEnabled(enabled);
      jSetButton.setEnabled(enabled);
      jClearButton.setEnabled(enabled);
      jRemovePlayerButton.setEnabled(enabled);
   }

   public void updatePlayerTable() {
      DefaultTableModel tableModel = getPlayerTableDefaultTableModel();

      // Remove all players from table before repopulating
      tableModel.setRowCount(0);

      for (Player player : gameEngine.getAllPlayers()) {
         if (Integer.parseInt(player.getPlayerId()) > -1) {
            tableModel.addRow(new Object[]{player.getPlayerName(), player.getPoints(), (player.getBet()), player.getResult()});
         }
      }
   }

   public void onNewPlayer(Player player) {
      DefaultTableModel tableModel = getPlayerTableDefaultTableModel();
      if (Integer.parseInt(player.getPlayerId()) == 0) {
         tableModel.addRow(new Object[]{player.getPlayerName()});
      }
      else {
         tableModel.addRow(new Object[]{player.getPlayerName(), player.getPoints()});
      }

      jPlayerSelection.addItem(new JComboBoxItem<>(player.getPlayerName(), player.getPlayerId()));
      jPlayerSelection.updateUI();

      addPlayerPanel(player);
   }

   //@formatter: off
   public GameEngineGuiDecor getGameEngine() {
      return gameEngine;
   }

   public JButton getAddPlayerButton() {
      return jAddPlayerButton;
   }

   public JButton getClearButton() {
      return jClearButton;
   }

   public JButton getDealAllButton() {
      return jDealAllButton;
   }

   public JButton getDealButton() {
      return jDealButton;
   }

   public JButton getDealHouseButton() {
      return jDealHouseButton;
   }

   public JButton getRemovePlayerButton() {
      return jRemovePlayerButton;
   }

   public JButton getSetButton() {
      return jSetButton;
   }

   public JTable getSummaryTable() {
      return jSummaryTable;
   }

   public JTextField getBetField() {
      return jBetField;
   }

   public Player getActivePlayer() {
      return activePlayer;
   }

   public void setActivePlayer(Player player) {
      activePlayer = player;
   }

   public Timer getDealHouseTimer() {
      return dealHouseTimer;
   }

   public void setDealHouseTimer(Timer dealHouseTimer) {
      this.dealHouseTimer = dealHouseTimer;
   }

   public JPanel getPlayerPanel(Player player) {
      return playerPanels.get(player.getPlayerId());
   }

   public JComboBox<JComboBoxItem<String, String>> getPlayerSelection() {
      return jPlayerSelection;
   }

   public void setStatus(String status) {
      jStatusLabel.setText(status);
   }

   public void setBetFieldValue(int bet) {
      jBetField.setText(String.valueOf(bet));
   }


   //@formatter: on

   public void onPlayerLeft(Integer index) {
      jPlayerSelection.removeItemAt(index + 1);
      updatePlayerTable();
   }

   public void onBetPlaced(Player player) {
      DefaultTableModel tableModel = getPlayerTableDefaultTableModel();
      tableModel.setValueAt((player.getBet()), gameEngine.getPlayerIndex(player), 2);
      tableModel.setValueAt("", gameEngine.getPlayerIndex(player), 4);
   }


   /**
    * Hides all the player deal panels (the panels that wrap the cards dealt to each player), and shows
    * only the panel for the selected player (who becomes the active player).
    * <p>
    * Also updates the status bar, and the player table selection
    * </p>
    *
    * @param player The new active player
    */
   public void activatePlayerPanel(Player player) {
      if (player == null) {
         System.err.println("activatePlayerPanel: Attempt to activate panel for null player");
         return;
      }
      JPanel panel = getPlayerPanel(player);

      for (Component c : jLayeredPane.getComponents()) {
         int layer = jLayeredPane.getLayer(c);
         if (layer >= 500) {
            c.setVisible((layer - 500) == gameEngine.getPlayerIndex(player));
         }
      }

      if (panel == null) {
         return;
      }

      for (Component component : jPlayerPanelsContainer.getComponents()) {
         if (component != panel) {
            component.setVisible(false);
         }
         else {
            jBackgroundImagePanel.setBackground(getPlayerColor(player));
            component.setVisible(true);
            setActivePlayer(player);
            if (getCardCount(player) > 0) {
               setStatus("<html>" + player.getPlayerName() + " says: <i> \"Try dragging my cards with your mouse\"");
            }
            else {
               setStatus(player.getPlayerName());
            }

            jSummaryTable.setRowSelectionInterval(gameEngine.getPlayerIndex(player), gameEngine.getPlayerIndex(player));
         }
      }
   }

   public void onNewCard(Player player, PlayingCard card, boolean bust) {
      JPanel playerPanel = getPlayerPanel(player);
      final int playerIndex = gameEngine.getPlayerIndex(player);

      // If this card is a bust, draw it with a light grey background (will be translated into slight transparency)
      PlayingCardPanel playingCardPanel = new PlayingCardPanel(card, bust ? new Color(192, 192, 192) : new Color(224, 224, 224));

      int cardCount = getCardCount(player);
      // playerPanel.getComponentCount();

      // Add a left margin if this is the first card
      if (cardCount == 0) {
         playerPanel.add(Box.createRigidArea(new Dimension(10, 0)));
      }

      playerPanel.add(playingCardPanel);

      // Card won't appear unless we trigger validation
      playerPanel.revalidate();

      // Add mouse-drag and mouse-over functionality to card
      Animation.addMouseListeners(card,
            playerIndex,
            playingCardPanel,
            jStatusLabel,
            jLayeredPane);
   }

   private int getCardCount(Player player) {
      JPanel playerPanel = getPlayerPanel(player);
      // This still works when all the cards are floating, since there is also a border at the left
      return playerPanel.getComponentCount();
   }


   public void clearCards(Player player) {
      JPanel playerPanel = getPlayerPanel(player);

      // trigger the animation that removes the cards
      Animation.easeOutCards(player, playerPanel, jLayeredPane, gameEngine);

      // all the cards are children of jLayeredPane, so we can go ahead and clean up this panel
      playerPanel.removeAll();
   }


   public void addPlayerPanel(Player player) {
      JPanel jPlayerPanel = createPlayerPanel(player);

      // add panel to map of panels
      playerPanels.put(player.getPlayerId(), jPlayerPanel);

      // add panel to panel container
      jPlayerPanelsContainer.add(jPlayerPanel);

      // make this the active panel
      activatePlayerPanel(player);

   }

   // called by addPlayerPanel
   protected JPanel createPlayerPanel(Player player) {
      ImagePanel jPlayerPanel = namedComponent(ForegroundImagePanel.class, "jPlayerPanel for " + player.getPlayerName());
      jPlayerPanel.setOpaque(false);
      jPlayerPanel.setLayout(new CanvasLayout());
      jPlayerPanel.setImage(getResourceUrl("bsg-logo.png"));
      return jPlayerPanel;
   }

   // Clear result column of summary table
   public void clearResults() {
      DefaultTableModel tableModel = getPlayerTableDefaultTableModel();
      for (int i = 0; i < tableModel.getRowCount(); i++) {
         tableModel.setValueAt("", i, 3);
      }
   }

   public void onPlayerResult(Player player, int result) {
      DefaultTableModel tableModel = getPlayerTableDefaultTableModel();
      tableModel.setValueAt((player.getBet()), gameEngine.getPlayerIndex(player), 2);
      tableModel.setValueAt(result, gameEngine.getPlayerIndex(player), 3);
   }

   public DefaultTableModel getPlayerTableDefaultTableModel() {
      return (DefaultTableModel) getSummaryTable().getModel();
   }

   // Populate the summary table with win/loss information
   public void onHouseResult(int houseResult) {
      DefaultTableModel tableModel = getPlayerTableDefaultTableModel();

      // Remove all players from table before repopulating
      tableModel.setRowCount(0);

      for (Player player : this.gameEngine.getAllPlayers()) {
         gameEngine.setPlayerFinished(player, true);
         // for real players
         if (Integer.parseInt(player.getPlayerId()) > 0) {
            tableModel.addRow(new Object[]{player.getPlayerName(), player.getPoints(), (player.getBet()), player.getResult()});
         }
         // for the dealer
         else if (Integer.parseInt(player.getPlayerId()) == 0) {
            tableModel.addRow(new Object[]{player.getPlayerName(), "", "", houseResult, ""});
         }

         // skip the rest, if this is the dealer
         if (Integer.parseInt(player.getPlayerId()) < 1) {
            continue;
         }

         // update the summary table with the player's results
         int playerResult = player.getResult();
         int playerBet = player.getBet();
         String resultString;

         if (playerResult > houseResult) {
            resultString = String.format("WON  %4d", playerBet);
         }
         else if (playerResult < houseResult) {
            resultString = String.format("LOST %4d", playerBet);
         }
         else {
            resultString = "DRAW";
         }

         tableModel.setValueAt(resultString, tableModel.getRowCount() - 1, 4);
      }

      jSummaryTable.setRowSelectionInterval(0, 0);
   }

   private static class CustomHeaderRenderer extends DefaultTableCellRenderer {

      private static final long serialVersionUID = 1L;

      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {
         Component rendererComp = super.getTableCellRendererComponent(table,
               value, isSelected, hasFocus, row, column);

         rendererComp.setBackground(Color.BLACK);
         rendererComp.setForeground(Color.WHITE);
         return rendererComp;
      }
   }

   /**
    * Formats JTable cells to be really pretty
    *
    * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
    */
   private class CustomCellRenderer extends DefaultTableCellRenderer {

      private static final long serialVersionUID = 1L;

      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {
         Object value2;
         int i = StringHelpers.stringAsInt(StringHelpers.safeString(value), -1);
         if (i == 0) {
            value2 = hiddenZero(i);
         }
         else {
            value2 = value;
         }

         Component rendererComp = super.getTableCellRendererComponent(table,
               value2, isSelected, hasFocus, row, column);
         Player player = gameEngine.getPlayerByIndex(row);

         if (column == 4 && value != null) {
            if (value.toString().startsWith("WON")) {
               rendererComp.setForeground(Color.GREEN.brighter().brighter());
            }
            else if (value.toString().startsWith("LOST")) {
               rendererComp.setForeground(Color.RED);
            }
            else {
               rendererComp.setForeground(Color.WHITE.darker());
            }
            rendererComp.setBackground(Color.BLACK);
         }
         else if (isSelected) {
            rendererComp.setBackground(getPlayerColor(player).darker().darker());
         }
         else {
            rendererComp.setBackground(Color.BLACK);
            rendererComp.setForeground(Color.WHITE.darker());
         }
         return rendererComp;
      }
   }
}
