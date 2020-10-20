package view;

import model.GameEngineImplEx;
import viewmodel.PlayerWrapper;
import model.interfaces.GameEngine;
import util.swing.jComponent;

import javax.swing.*;
import java.awt.*;
import java.util.AbstractList;
import java.util.Vector;


/**
 * @author Christopher Anderson
 */
public class NewPlayerDialog extends javax.swing.JDialog {

   private static final long serialVersionUID = 1L;
   private final AbstractList<JTextField> jTextFields = new Vector<>(4);
   private final GameEngine model;
   private javax.swing.JButton jOkayButton;
   private javax.swing.JButton jCancelButton;

   /**
    * Creates new form NewPlayerDialog
    */
   public NewPlayerDialog(GameEngine model, Frame parent, boolean modal) {
      super(parent, modal);
      this.model = model;
      initComponents();
   }

   public static int getNewPlayerId(GameEngine model) {
      int highestId = -1;
      if (model instanceof GameEngineImplEx) {
         GameEngineImplEx gameEngineImplEx = (GameEngineImplEx) model;

         for (PlayerWrapper player : gameEngineImplEx.getPlayerList().getAll()) {
            int id;
            if ((id = Integer.parseInt(player.getPlayerId())) > highestId) {
               highestId = id;
            }
         }
      }
      return ++highestId;
   }

   private void initComponents() {
      this.setResizable(false);

      //Create and populate the panel.
      Container contentPane = getContentPane();

      // Wrap everything in a BorderLayout
      JPanel borderPanel = new JPanel(new BorderLayout());
      contentPane.add(borderPanel);

      // Put the heading/title in the NORTH position
      JPanel headerPanel = new JPanel(new BorderLayout());
      borderPanel.add(headerPanel, BorderLayout.PAGE_START);

      JLabel jHeading = new JLabel("Add Player", JLabel.CENTER);
      jHeading.setFont(new java.awt.Font("Calibri Light", Font.PLAIN, 24));
      jHeading.setText("Add Player");
      jHeading.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
      jHeading.setVerticalAlignment(JLabel.TOP);
      jHeading.setHorizontalTextPosition(SwingConstants.CENTER);
      jHeading.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));

      headerPanel.add(jHeading);

      // Put the buttons in the SOUTH position
      JPanel buttonPanel = new JPanel();
      borderPanel.add(buttonPanel, BorderLayout.PAGE_END);

      jOkayButton = new javax.swing.JButton();
      jOkayButton.setDefaultCapable(true);
      jComponent.builder(jOkayButton)
            .text("OK")
            .minimumSize(75, 26)
            .preferredSize(75, 26)
            .appendTo(buttonPanel);

      jCancelButton = new javax.swing.JButton();
      jComponent.builder(jCancelButton)
            .text("Cancel")
            .minimumSize(75, 26)
            .preferredSize(75, 26)
            .appendTo(buttonPanel);


      // Put the input fields in the CENTER position
      JPanel fieldPanel = new JPanel(null);
      borderPanel.add(fieldPanel, BorderLayout.CENTER);

      jTextFields.add(jComponent.builder(new JTextField())
            .bounds(194, 10, 144, 28)
            .text(String.valueOf(getNewPlayerId(model)))
            .enabled(false)
            .appendTo(fieldPanel));

      jTextFields.add(jComponent.builder(new JTextField())
            .bounds(194, 43, 144, 28)
            .appendTo(fieldPanel));

      jTextFields.add(jComponent.builder(new JTextField())
            .bounds(194, 76, 144, 28)
            .appendTo(fieldPanel));

      jComponent.builder(new JLabel())
            .text("Player ID: ")
            .bounds(110, 10, 74, 28)
            .appendTo(fieldPanel);

      jComponent.builder(new JLabel())
            .text("PlayerName: ")
            .bounds(110, 43, 74, 28)
            .appendTo(fieldPanel);

      jComponent.builder(new JLabel())
            .text("Initial Chips: ")
            .bounds(110, 76, 74, 28)
            .appendTo(fieldPanel);

      pack();

      // If you want to ensure that a particular component gains the focus the first time a window is activated,
      // you can call the requestFocusInWindow method on the component after the component has been realized,
      // but before the frame is displayed. -- https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html

      jTextFields.get(1).requestFocusInWindow();
      setSize(464, 344);
   }

   public JButton getOkayButton() {
      return jOkayButton;
   }

   public JButton getCancelButton() {
      return jCancelButton;
   }

   public String getTextFieldText(int index) {
      return jTextFields.get(index).getText();
   }

}
