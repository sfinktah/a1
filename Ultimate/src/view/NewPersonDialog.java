/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// https://stackoverflow.com/questions/1313390/is-there-any-way-to-accept-only-numeric-values-in-a-jtextfield
// https://stackoverflow.com/questions/28790820/jtextfield-with-regex-applied-wont-accept-value-until-after-the-document-is-app
package view;

import controller.FatController;
import model.interfaces.BlackEngine;
import view.util.FilteredFields;

import javax.swing.*;
import java.awt.*;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Vector;


/**
 * @author Christopher Anderson
 */
public class NewPersonDialog extends javax.swing.JDialog {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private final BlackEngine model;
   private AbstractList<JTextField> jTextFields = null;
   private javax.swing.JButton jOkayButton;
   private javax.swing.JButton jCancelButton;

   /**
    * Creates new form NewPersonDialog
    */
   public NewPersonDialog(BlackEngine model, Frame parent, boolean modal) {
      super(parent, modal);
      this.model = model;
      initComponents();
   }

   public static AbstractList<JTextField> makeFields(JPanel fieldPanel, AbstractList<Pair<String, String>> fieldPairs) {
      Vector<JTextField> jTextFields = new Vector<>(16);
      for (Pair<String, String> p : fieldPairs) {
         JTextField textField = FilteredFields.createFilteredField(p.getPredicate());
         JLabel label = new JLabel(p.getLabel(), JLabel.TRAILING);
         fieldPanel.add(label);
         label.setLabelFor(textField);
         fieldPanel.add(textField);
         jTextFields.add(textField);
      }
      return jTextFields;
   }

   private static SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
      SpringLayout layout = (SpringLayout) parent.getLayout();
      Component c = parent.getComponent(row * cols + col);
      return layout.getConstraints(c);
   }

   /**
    * Aligns the first <code>rows</code> * <code>cols</code>
    * components of <code>parent</code> in
    * a grid. Each component in a column is as wide as the maximum
    * preferred width of the components in that column;
    * height is similarly determined for each row.
    * The parent is made just big enough to fit them all.
    *
    * @param rows          number of rows
    * @param cols          number of columns
    * @param initialX      x location to start the grid at
    * @param initialY      y location to start the grid at
    * @param xPad          x padding between cells
    * @param yPad          y padding between cells
    * @param paddingTop    padding on north of cells
    * @param paddingRight  padding on east of cells
    * @param paddingBottom padding on south of cells
    * @param paddingLeft   padding on west of cells
    */
   public static void makeCompactGrid(Container parent,
                                      int rows, int cols,
                                      int initialX, int initialY,
                                      int xPad, int yPad,
                                      int paddingTop, int paddingRight,
                                      int paddingBottom, int paddingLeft) {
      SpringLayout layout;
      try {
         layout = (SpringLayout) parent.getLayout();
      } catch (ClassCastException e) {
         System.err.println("makeCompactGrid: parent container must use SpringLayout");
         return;
      }

      // Align all cells in each column and make them the same width.
      // Include paddingLeft
      Spring x = Spring.constant(initialX + paddingLeft);
      int row;
      int column;
      for (column = 0; column < cols; column++) {
         Spring width = Spring.constant(0);
         for (row = 0; row < rows; row++) {
            width = Spring.max(width, getConstraintsForCell(row, column, parent, cols).getWidth());
         }
         for (row = 0; row < rows; row++) {
            SpringLayout.Constraints constraints = getConstraintsForCell(row, column, parent, cols);
            constraints.setX(x);
            constraints.setWidth(width);
         }
         x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
      }

      // Align all cells in each row and make them the same height.
      // Include paddingTop
      Spring y = Spring.constant(initialY + paddingTop);
      for (row = 0; row < rows; row++) {
         Spring height = Spring.constant(0);
         for (column = 0; column < cols; column++) {
            height = Spring.max(height, getConstraintsForCell(row, column, parent, cols).getHeight());
         }
         for (column = 0; column < cols; column++) {
            SpringLayout.Constraints constraints = getConstraintsForCell(row, column, parent, cols);
            constraints.setY(y);
            constraints.setHeight(height);
         }
         y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
      }

      // Set the parent's size.
      // Include paddingRight and paddingBottom
      SpringLayout.Constraints constraints = layout.getConstraints(parent);
      constraints.setConstraint(SpringLayout.SOUTH, Spring.sum(y, Spring.constant(paddingBottom)));
      constraints.setConstraint(SpringLayout.EAST, Spring.sum(x, Spring.constant(paddingRight)));
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
      jOkayButton.setText("OK");
      jOkayButton.setMinimumSize(new java.awt.Dimension(75, 26));
      jOkayButton.setPreferredSize(new java.awt.Dimension(75, 26));
      jOkayButton.setDefaultCapable(true);

      jCancelButton = new javax.swing.JButton();
      jCancelButton.setText("Cancel");
      jCancelButton.setMinimumSize(new java.awt.Dimension(75, 26));
      jCancelButton.setPreferredSize(new java.awt.Dimension(75, 26));

      buttonPanel.add(jOkayButton);
      buttonPanel.add(jCancelButton);

      // Put the input fields in the CENTER position
      JPanel fieldPanel = new JPanel(new SpringLayout());
      borderPanel.add(fieldPanel, BorderLayout.CENTER);

      AbstractList<Pair<String, String>> fieldPairs = new ArrayList<>();

      fieldPairs.add(Pair.of("Player ID: ", null));
      fieldPairs.add(Pair.of("PersonName: ", "^[A-Za-z][A-Za-z0-9_ ]*$"));
      fieldPairs.add(Pair.of("Initial Chips: ", "^[1-9][0-9]*$"));

      // Create and associate the field inputs and labels and regex filters
      jTextFields = makeFields(fieldPanel, fieldPairs);

      jTextFields.get(0).setText(String.valueOf(FatController.getNewPersonId(model)));

      // Resize the fields into an aligned grid
      makeCompactGrid(fieldPanel,
            fieldPairs.size(), 2,
            10, 10,
            10, 5,
            0, 100, 50, 100);

      pack();

      // If you want to ensure that a particular component gains the focus the first time a window is activated,
      // you can call the requestFocusInWindow method on the component after the component has been realized,
      // but before the frame is displayed. -- https://docs.oracle.com/javase/tutorial/uiswing/misc/focus.html
      jTextFields.get(1).requestFocusInWindow();
   }

   public JButton getOkayButton() {
      return jOkayButton;
   }

   public JButton getCancelButton() {
      return jCancelButton;
   }

   /**
    * Returns the text from the element at the specified position in this Vector.
    *
    * @param index index of the element to return
    *
    * @return object at the specified index
    *
    * @throws ArrayIndexOutOfBoundsException if the index is out of range
    *                                        ({@code index < 0 || index >= size()})
    */
   public String getTextFieldText(int index) {
      return jTextFields.get(index).getText();
   }

   public static class Pair<L, P> {
      private final L label;
      private final P predicate;

      private Pair(L label, P predicate) {
         this.label = label;
         this.predicate = predicate;
      }

      public static <L, P> Pair<L, P> of(L label, P predicate) {
         return new Pair<>(label, predicate);
      }

      public L getLabel() {
         return this.label;
      }

      public P getPredicate() {
         return this.predicate;
      }

      public String toString() {
         return "SpringForms.Pair(label=" + this.getLabel() + ", predicate=" + this.getPredicate() + ")";
      }
   }

}
