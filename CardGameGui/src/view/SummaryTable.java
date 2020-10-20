package view;

import model.GameEngineImplEx;
import util.swing.FontHelper;
import viewmodel.PlayerWrapper;
import util.StringHelpers;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Some Custom Cell and Header rendering to pretty up the Player Summary Panel (JTable)
 */
public class SummaryTable extends JTable {
   private static final long serialVersionUID = 1L;
   private final CustomCellRenderer customCellRenderer;

   public SummaryTable(GameEngineImplEx gameEngineImplEx) {
      super();
      customCellRenderer = new CustomCellRenderer(gameEngineImplEx);
      CustomHeaderRenderer customHeaderRenderer = new CustomHeaderRenderer();

      getTableHeader().setDefaultRenderer(customHeaderRenderer);

      setModel(new DefaultTableModel(
            new Object[][]{},
            new String[]{
                  "Player", "Chips", "Bet", "Result", "Last Game"
            }
      ));

      setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      setRowSelectionAllowed(true);
      setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
      setFillsViewportHeight(true);
      setOpaque(true);
      setBackground(Color.BLACK);
      getTableHeader().setBackground(Color.BLACK);
      getTableHeader().setForeground(Color.WHITE);
      getColumnModel().getColumn(0).setMinWidth(165);
      getColumnModel().getColumn(1).setMinWidth(100);
      getColumnModel().getColumn(2).setMinWidth(75);
      getColumnModel().getColumn(3).setMinWidth(100);
      getColumnModel().getColumn(4).setMinWidth(150);
      setMinimumSize(new Dimension(600, 180));
      setFont(FontHelper.fontFromResource("LED_DISPLAY-7.TTF", 16));
   }

   @Override
   public boolean isCellEditable(int row, int column) {
      return false;
   }

   @Override
   public TableCellRenderer getCellRenderer(int row, int column) {
      return customCellRenderer;
   }


   public static class CustomCellRenderer extends DefaultTableCellRenderer {

      private static final long serialVersionUID = 1L;
      private final GameEngineImplEx gameEngineImplEx;

      public CustomCellRenderer(GameEngineImplEx gameEngineImplEx) {
         this.gameEngineImplEx = gameEngineImplEx;
      }

      public Component getTableCellRendererComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     boolean hasFocus,
                                                     int row,
                                                     int column) {
         Object value2;
         int i = StringHelpers.stringAsInt(StringHelpers.safeString(value), -1);
         if (i == 0) {
            value2 = 0 != i ? String.valueOf(i) : "";
         }
         else {
            value2 = value;
         }

         Component rendererComp = super.getTableCellRendererComponent(table,
               value2, isSelected, hasFocus, row, column);

         // https://stackoverflow.com/questions/18801137/java-jtable-disable-single-cell-selection-border-highlight/18801181#18801181
         if (hasFocus) {
            setBorder(new EmptyBorder(getBorder().getBorderInsets(this)));
         }

         PlayerWrapper player = gameEngineImplEx.getPlayerList().at(row);

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
            rendererComp.setBackground(isSelected
                  ? player.getColor().darker().darker()
                  : Color.BLACK
            );
         }
         else if (isSelected) {
            rendererComp.setBackground(player.getColor().darker().darker());
         }
         else {
            rendererComp.setBackground(Color.BLACK);
            rendererComp.setForeground(Color.WHITE.darker());
         }
         return rendererComp;
      }
   }

   public static class CustomHeaderRenderer extends DefaultTableCellRenderer {

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

}
