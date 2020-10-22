package view;

import model.BlackEngineGuiDecor;
import model.SimplePerson;
import model.interfaces.Player;
import model.interfaces.PokerCard;
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
   private final BlackEngineGuiDecor gameEngine;
   private final HashMap<String, JPanel> playerPanels = new HashMap<>();
   private JLayeredPane jLayeredPane;
   private ImagePanel jBackgroundImagePanel;
   private JButton jAddPersonButton;
   private JButton jClearButton;
   private JButton jDealAllButton;
   private JButton jDealButton;
   private JButton jDealHouseButton;
   private JButton jRemovePersonButton;
   private JButton jSetButton;
   private JComboBox<JComboBoxItem<String, String>> jPersonSelection;
   private JLabel jBetLabel;
   private JLabel jStatusLabel;
   private JPanel jPersonPanelsContainer;
   @SuppressWarnings("FieldCanBeLocal")
   private JPanel jPersonPanelsContainerWrapper;
   private JPanel jSummaryPanel;
   private JScrollPane jScrollPanel;
   private JTable jSummaryTable;
   private JTextField jBetField;
   private Player activePerson;
   private Timer dealHouseTimer;

   /**
    * Creates our main form/frame
    *
    * @param gameEngine an instance of BlackEngine
    */
   public AppView(BlackEngineGuiDecor gameEngine) {
      this.gameEngine = gameEngine;
      gameEngine.setAppView(this);

      // set app icon, title, and components.
      setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("clubs.png")));
      setTitle("My First Swing GUI by Christopher Anderson");
      initComponents();
      initPersons();
   }

   private void initComponents() {

      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      setMinimumSize(new Dimension(FRAME_MIN_WIDTH, FRAME_MIN_HEIGHT));

      jSummaryPanel = namedComponent(JPanel.class, "jSummaryPanel");
      jScrollPanel = namedComponent(JScrollPane.class, "jScrollPanel");
      jPersonPanelsContainer = namedComponent(JPanel.class, "jPersonPanelsContainer");

      // When you absolutely positively need to have your panel take up exactly
      // the right amount of space for 7 cards, you have to override getPreferredSize
      jPersonPanelsContainerWrapper = new JPanel() {
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

      jPersonPanelsContainer.setOpaque(false);
      jPersonPanelsContainer.setLayout(new BoxLayout(jPersonPanelsContainer, BoxLayout.LINE_AXIS));

      jPersonPanelsContainerWrapper.setLayout(new AspectLayout(new Dimension(93, 23)).setFill(true));
      jPersonPanelsContainerWrapper.setOpaque(false);
      jPersonPanelsContainerWrapper.add(jPersonPanelsContainer);

      jStatusLabel = namedComponent(JLabel.class, "jStatusLabel");

      // create the center (main) panel, which has everything in it, except for status bar and tool bar
      JPanel centerPanel = namedComponent(JPanel.class, "centerPanel");
      centerPanel.setLayout(new BoxLayout(centerPanel, PAGE_AXIS));
      centerPanel.setOpaque(false);
      centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      // create the summary JTable within a JScrollPane
      centerPanel.add(initSummaryPanel());
      centerPanel.add(jPersonPanelsContainerWrapper);

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
      jBackgroundImagePanel.setBackground(getPersonColor(0));

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
      jPersonSelection = new JComboBox<>();
      jDealButton = namedComponent(JButton.class, "jDealButton");
      jSetButton = namedComponent(JButton.class, "jSetButton");
      jClearButton = namedComponent(JButton.class, "jClearButton");
      jDealAllButton = namedComponent(JButton.class, "jDealAllButton");
      jDealHouseButton = namedComponent(JButton.class, "jDealHouseButton");

      jAddPersonButton = namedComponent(JButton.class, "jAddPersonButton");
      jRemovePersonButton = namedComponent(JButton.class, "jRemovePersonButton");

      setTextAndToolTip(jDealButton, "Deal", "Deal Current Player");
      setTextAndToolTip(jSetButton, "Set", "Set Bet for Current Player");
      setTextAndToolTip(jClearButton, "Clear", "Clear Bet for Current Player");
      setTextAndToolTip(jDealAllButton, "Deal All", "Sequentially deals all un-dealt players with a bet of $100");
      setTextAndToolTip(jDealHouseButton, "Deal House", "Invisible Button, used to trigger House Deal");
      setTextAndToolTip(jAddPersonButton, "Add Player", "Add New Player");
      setTextAndToolTip(jRemovePersonButton, "Remove Player", "Remove Current Player");
      //@formatter: on

      jDealHouseButton.setEnabled(false);
      jDealHouseButton.setVisible(false);

      jPersonSelection.setModel(
            new DefaultComboBoxModel<>()
      );

      jBetLabel = new JLabel("Bet: ", JLabel.TRAILING);
      jBetLabel.setForeground(Color.WHITE);
      jBetField = FilteredFields.createFilteredField("^[0-9]*$", new Predicate<>() {
         @Override
         public boolean test(String e) {
            return e == null || e.length() == 0 || getActivePerson() != null && Integer.parseInt(e) <= getActivePerson().getPoints();
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
      buttonPane.add(jPersonSelection);
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
      buttonPane.add(jRemovePersonButton);
      buttonPane.add(jAddPersonButton);
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
                  gameEngine.placeBet(getActivePerson(), getActivePerson().getPoints());
               }
            });

      jScrollPanel = new JScrollPane(jSummaryTable);
      jSummaryPanel.add(jScrollPanel);
      jScrollPanel.setMinimumSize(new Dimension(600, 180));
      jScrollPanel.setPreferredSize(new Dimension(600, 180));
      jScrollPanel.getViewport().setBackground(new Color(0, 0, 0));
      return jSummaryPanel;
   }

   public void initPersons() {
      jPersonSelection.addItem(new JComboBoxItem<>("-- Select Player --", "-1"));

      // create two test players
      Player[] players = new Player[]{
            new SimplePerson("0", "House", 0),
            new SimplePerson("1", "The Loser", 500),
            new SimplePerson("2", "The Shark", 1000),
            new SimplePerson("3", "The Gambler", 2000),
            new SimplePerson("4", "The Rock", 1000),
            new SimplePerson("5", "The Fish", 2000),
            new SimplePerson("6", "The Whale", 2000),
            new SimplePerson("7", "The Book", 3000),
            new SimplePerson("8", "The Eagle", 1000),
      };

      for (Player player : players) {
         gameEngine.addPerson(player);
      }

      // Adding a new player automatically activates them as the current player,
      // so we want to reverse the effects of this after init.
      activatePersonPanel(gameEngine.getPersonByIndex(0)); // Activate dealer panel
      getSummaryTable().clearSelection(); // Remove selection from summary table
      setStatus("Click the Summary Table and use Arrow Keys, 1, and Enter to navigate, bet, and deal.");
   }

   public void setBetEnabled(boolean enabled) {
      jBetLabel.setForeground(enabled ? Color.WHITE : Color.GRAY);
      jBetField.setEnabled(enabled);
      jDealButton.setEnabled(enabled);
      jSetButton.setEnabled(enabled);
      jClearButton.setEnabled(enabled);
      jRemovePersonButton.setEnabled(enabled);
   }

   public void updatePersonTable() {
      DefaultTableModel tableModel = getPersonTableDefaultTableModel();

      // Remove all players from table before repopulating
      tableModel.setRowCount(0);

      for (Player player : gameEngine.getAllPersons()) {
         if (Integer.parseInt(player.getPersonId()) > -1) {
            tableModel.addRow(new Object[]{player.getPersonName(), player.getPoints(), (player.getBet()), player.getResult()});
         }
      }
   }

   public void onNewPerson(Player player) {
      DefaultTableModel tableModel = getPersonTableDefaultTableModel();
      if (Integer.parseInt(player.getPersonId()) == 0) {
         tableModel.addRow(new Object[]{player.getPersonName()});
      }
      else {
         tableModel.addRow(new Object[]{player.getPersonName(), player.getPoints()});
      }

      jPersonSelection.addItem(new JComboBoxItem<>(player.getPersonName(), player.getPersonId()));
      jPersonSelection.updateUI();

      addPersonPanel(player);
   }

   //@formatter: off
   public BlackEngineGuiDecor getBlackEngine() {
      return gameEngine;
   }

   public JButton getAddPersonButton() {
      return jAddPersonButton;
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

   public JButton getRemovePersonButton() {
      return jRemovePersonButton;
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

   public Player getActivePerson() {
      return activePerson;
   }

   public void setActivePerson(Player player) {
      activePerson = player;
   }

   public Timer getDealHouseTimer() {
      return dealHouseTimer;
   }

   public void setDealHouseTimer(Timer dealHouseTimer) {
      this.dealHouseTimer = dealHouseTimer;
   }

   public JPanel getPersonPanel(Player player) {
      return playerPanels.get(player.getPersonId());
   }

   public JComboBox<JComboBoxItem<String, String>> getPersonSelection() {
      return jPersonSelection;
   }

   public void setStatus(String status) {
      jStatusLabel.setText(status);
   }

   public void setBetFieldValue(int bet) {
      jBetField.setText(String.valueOf(bet));
   }


   //@formatter: on

   public void onPersonLeft(Integer index) {
      jPersonSelection.removeItemAt(index + 1);
      updatePersonTable();
   }

   public void onBetPlaced(Player player) {
      DefaultTableModel tableModel = getPersonTableDefaultTableModel();
      tableModel.setValueAt((player.getBet()), gameEngine.getPersonIndex(player), 2);
      tableModel.setValueAt("", gameEngine.getPersonIndex(player), 4);
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
   public void activatePersonPanel(Player player) {
      if (player == null) {
         System.err.println("activatePersonPanel: Attempt to activate panel for null player");
         return;
      }
      JPanel panel = getPersonPanel(player);

      for (Component c : jLayeredPane.getComponents()) {
         int layer = jLayeredPane.getLayer(c);
         if (layer >= 500) {
            c.setVisible((layer - 500) == gameEngine.getPersonIndex(player));
         }
      }

      if (panel == null) {
         return;
      }

      for (Component component : jPersonPanelsContainer.getComponents()) {
         if (component != panel) {
            component.setVisible(false);
         }
         else {
            jBackgroundImagePanel.setBackground(getPersonColor(player));
            component.setVisible(true);
            setActivePerson(player);
            if (getCardCount(player) > 0) {
               setStatus("<html>" + player.getPersonName() + " says: <i> \"Try dragging my cards with your mouse\"");
            }
            else {
               setStatus(player.getPersonName());
            }

            jSummaryTable.setRowSelectionInterval(gameEngine.getPersonIndex(player), gameEngine.getPersonIndex(player));
         }
      }
   }

   public void onNewCard(Player player, PokerCard card, boolean bust) {
      JPanel playerPanel = getPersonPanel(player);
      final int playerIndex = gameEngine.getPersonIndex(player);

      // If this card is a bust, draw it with a light grey background (will be translated into slight transparency)
      PokerCardPanel playingCardPanel = new PokerCardPanel(card, bust ? new Color(192, 192, 192) : new Color(224, 224, 224));

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
      JPanel playerPanel = getPersonPanel(player);
      // This still works when all the cards are floating, since there is also a border at the left
      return playerPanel.getComponentCount();
   }


   public void clearCards(Player player) {
      JPanel playerPanel = getPersonPanel(player);

      // trigger the animation that removes the cards
      Animation.easeOutCards(player, playerPanel, jLayeredPane, gameEngine);

      // all the cards are children of jLayeredPane, so we can go ahead and clean up this panel
      playerPanel.removeAll();
   }


   public void addPersonPanel(Player player) {
      JPanel jPersonPanel = createPersonPanel(player);

      // add panel to map of panels
      playerPanels.put(player.getPersonId(), jPersonPanel);

      // add panel to panel container
      jPersonPanelsContainer.add(jPersonPanel);

      // make this the active panel
      activatePersonPanel(player);

   }

   // called by addPersonPanel
   protected JPanel createPersonPanel(Player player) {
      ImagePanel jPersonPanel = namedComponent(ForegroundImagePanel.class, "jPersonPanel for " + player.getPersonName());
      jPersonPanel.setOpaque(false);
      jPersonPanel.setLayout(new CanvasLayout());
      jPersonPanel.setImage(getResourceUrl("bsg-logo.png"));
      return jPersonPanel;
   }

   // Clear result column of summary table
   public void clearResults() {
      DefaultTableModel tableModel = getPersonTableDefaultTableModel();
      for (int i = 0; i < tableModel.getRowCount(); i++) {
         tableModel.setValueAt("", i, 3);
      }
   }

   public void onPersonResult(Player player, int result) {
      DefaultTableModel tableModel = getPersonTableDefaultTableModel();
      tableModel.setValueAt((player.getBet()), gameEngine.getPersonIndex(player), 2);
      tableModel.setValueAt(result, gameEngine.getPersonIndex(player), 3);
   }

   public DefaultTableModel getPersonTableDefaultTableModel() {
      return (DefaultTableModel) getSummaryTable().getModel();
   }

   // Populate the summary table with win/loss information
   public void onHouseResult(int houseResult) {
      DefaultTableModel tableModel = getPersonTableDefaultTableModel();

      // Remove all players from table before repopulating
      tableModel.setRowCount(0);

      for (Player player : this.gameEngine.getAllPersons()) {
         gameEngine.setPersonFinished(player, true);
         // for real players
         if (Integer.parseInt(player.getPersonId()) > 0) {
            tableModel.addRow(new Object[]{player.getPersonName(), player.getPoints(), (player.getBet()), player.getResult()});
         }
         // for the dealer
         else if (Integer.parseInt(player.getPersonId()) == 0) {
            tableModel.addRow(new Object[]{player.getPersonName(), "", "", houseResult, ""});
         }

         // skip the rest, if this is the dealer
         if (Integer.parseInt(player.getPersonId()) < 1) {
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
         Player player = gameEngine.getPersonByIndex(row);

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
            rendererComp.setBackground(getPersonColor(player).darker().darker());
         }
         else {
            rendererComp.setBackground(Color.BLACK);
            rendererComp.setForeground(Color.WHITE.darker());
         }
         return rendererComp;
      }
   }
}
