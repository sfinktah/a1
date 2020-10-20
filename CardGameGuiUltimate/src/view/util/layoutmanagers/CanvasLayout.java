package view.util.layoutmanagers;

import java.awt.*;

/**
 * A little {@link LayoutManager} that is basically a super-simplified version of
 * {@link FlowLayout}, but without any of the configurable options but with a couple
 * of hard coded fixes that were needed to get things "just right." Basically one step
 * up from having no {@link LayoutManager} at all, but saves you manually positioning
 * cards.
 * <p>
 * If it did nothing else, it provided essential debug information showing how Layouts
 * work (and don't work).
 */
public class CanvasLayout implements LayoutManager, java.io.Serializable {

   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private final int horizontalPadding = 0;

   public CanvasLayout() {
   }

   public synchronized void invalidateLayout() {
      checkContainer();
   }

   @SuppressWarnings("EmptyMethod")
   private void checkContainer() {
   }

   /**
    * Not used by this class.
    *
    * @param name the name of the component
    * @param comp the component
    */
   public void addLayoutComponent(String name, Component comp) {
      invalidateLayout();
   }

   /**
    * Not used by this class.
    *
    * @param comp the component
    */
   public void removeLayoutComponent(Component comp) {
      invalidateLayout();
   }

   /**
    * Calculates the preferred size dimensions for the specified
    * container, given the components it contains.
    *
    * @param target the container to be laid out
    *
    * @see #minimumLayoutSize
    */
   public Dimension preferredLayoutSize(Container target) {
      synchronized (target.getTreeLock()) {
         Dimension dim = new Dimension(0, 0);
         int numComponents = target.getComponentCount();
         boolean firstVisibleComponent = true;

         for (int i = 0; i < numComponents; i++) {
            Component m = target.getComponent(i);
            if (m.isVisible()) {
               Dimension d = m.getPreferredSize();
               dim.height = Math.max(dim.height, d.height);
               if (firstVisibleComponent) {
                  firstVisibleComponent = false;
               }
               else {
                  dim.width += horizontalPadding;
               }
               dim.width += d.width;
            }
         }
         Insets insets = target.getInsets();
         dim.width += insets.left + insets.right;
         dim.height += insets.top + insets.bottom;
         return dim;
      }
   }

   /**
    * Calculates the preferred size dimensions for the specified
    * container, given the components it contains.
    *
    * @param target the container to be laid out
    *
    * @see #minimumLayoutSize
    */
   public Dimension minimumLayoutSize(Container target) {
      synchronized (target.getTreeLock()) {
         Dimension dim = new Dimension(0, 0);
         int numComponents = target.getComponentCount();
         boolean firstVisibleComponent = true;

         for (int i = 0; i < numComponents; i++) {
            Component m = target.getComponent(i);
            if (m.isVisible()) {
               Dimension d = m.getMinimumSize();
               dim.height = Math.max(dim.height, d.height);
               if (firstVisibleComponent) {
                  firstVisibleComponent = false;
               }
               else {
                  dim.width += horizontalPadding;
               }
               dim.width += d.width;
            }
         }

         Insets insets = target.getInsets();
         dim.width += insets.left + insets.right;
         dim.height += insets.top + insets.bottom;
         return dim;
      }
   }

   /**
    * Centers the elements in the specified row, if there is any slack.
    *
    * @param target   the component which needs to be moved
    * @param x        the x coordinate
    * @param height   the height dimensions
    * @param rowStart the beginning of the row
    * @param rowEnd   the the ending of the row
    */
   private void moveComponents(Container target, int x, int height,
                               int rowStart, int rowEnd) {
      x += 0;
      for (int i = rowStart; i < rowEnd; i++) {
         Component component = target.getComponent(i);
         if (component.isMaximumSizeSet()) {
            continue;
         }
         if (component.isVisible()) {
            int cy = (target.getHeight() - height) / 2;
            component.setLocation(x, cy);
            x += component.getWidth() + height * 0.06; // hard coded horizontalPadding as a percentage of size;
         }
      }
   }

   /**
    * Lays out the container. This method lets each
    * <i>visible</i> component take
    * its preferred size by reshaping the components in the
    * target container in order to satisfy whatever.
    *
    * @param target the specified component being laid out
    *
    * @see Container
    * @see java.awt.Container#doLayout
    */
   public void layoutContainer(Container target) {
      synchronized (target.getTreeLock()) {
         Insets insets = target.getInsets();
         int maxWidth = target.getWidth() - (insets.left + insets.right);
         int numComponents = target.getComponentCount();
         int x = 0;
         int rowHeight = 0, start = 0;

         for (int i = 0; i < numComponents; i++) {
            Component component = target.getComponent(i);
            if (component.isMaximumSizeSet()) {
               continue;
            }
            if (component.isVisible()) {
               Dimension d = component.getPreferredSize();
               component.setSize(d.width, d.height);

               if ((x == 0) || ((x + d.width) <= maxWidth)) {
                  if (x > 0) {
                     x += horizontalPadding;
                  }
                  x += d.width;
                  rowHeight = Math.max(rowHeight, d.height);
               }
               else {
                  moveComponents(target, insets.left + horizontalPadding,
                        rowHeight, start, i
                  );
                  x = d.width;
                  rowHeight = d.height;
                  start = i;
               }
            }
         }
         moveComponents(target, insets.left + horizontalPadding, rowHeight,
               start, numComponents);
      }
   }

}
