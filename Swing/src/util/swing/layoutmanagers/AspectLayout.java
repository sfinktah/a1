package util.swing.layoutmanagers;

import javax.swing.*;
import java.awt.*;

/**
 * A little {@link LayoutManager} that exists entirely to make it's single component resizes
 * as large as possible, given the limits of the containing container.
 */
public class AspectLayout implements LayoutManager {

   // an empty component that we can use when there are no components, or we can use to store
   // the desired ratio (in it's dimensions)
   private static final Component emptyComponent = new JPanel();

   // if set, then we will expand to take extra room (or so goes the theory)
   // in practice, it may be necessary to override getPreferredSize()
   private boolean fill;

   /**
    * Construct with specified aspect ratio
    *
    * @param ratio desired aspect ratio
    */
   public AspectLayout(Dimension ratio) {
      emptyComponent.setSize(ratio);
      emptyComponent.setPreferredSize(ratio);
   }

   @Override
   public void addLayoutComponent(String name, Component component) {
   }

   @Override
   public void layoutContainer(Container parent) {
      synchronized (parent.getTreeLock()) {
         Insets insets = parent.getInsets();
         int maxWidth = parent.getWidth() - (insets.left + insets.right);
         int numComponents = parent.getComponentCount();
         Component ratioComponent = getRatioComponentFrom(parent);
         int maxHeight = parent.getHeight() - (insets.top + insets.bottom);

         Dimension preferredSize = ratioComponent.getPreferredSize();
         Dimension targetDimension = getScaledDimension(preferredSize, new Dimension(maxWidth, maxHeight));

         double width = targetDimension.getWidth();
         double height = targetDimension.getHeight();

         double paddingLeft = (maxWidth - width) / 2;
         double paddingTop = (maxHeight - height) / 2;

         if (this.fill) {
            if (paddingLeft > 0) {
               paddingLeft = 0;
               width = maxWidth;
            }
            if (paddingTop > 0) {
               paddingTop = 0;
               height = maxHeight;
            }
         }


         for (int i = 0; i < numComponents; i++) {
            Component component = parent.getComponent(i);
            if (component.isVisible()) {
               component.setBounds((int) paddingLeft, (int) paddingTop, (int) width, (int) height);

            }
         }
      }
   }

   private Component getRatioComponentFrom(Container parent) {
      return (parent.getComponentCount() > 0 && emptyComponent.getWidth() == 0) ? parent.getComponent(0) : emptyComponent;
   }

   private Dimension getScaledDimension(Dimension componentPreferredSize, Dimension parentSize) {
      double widthRatio = parentSize.getWidth() / componentPreferredSize.getWidth();
      double heightRatio = parentSize.getHeight() / componentPreferredSize.getHeight();
      double ratio = Math.min(widthRatio, heightRatio);
      return new Dimension((int) (componentPreferredSize.width * ratio), (int) (componentPreferredSize.height * ratio));
   }

   @Override
   public Dimension minimumLayoutSize(Container parent) {
      return preferredLayoutSize(parent);
   }

   @Override
   public Dimension preferredLayoutSize(Container parent) {
      synchronized (parent.getTreeLock()) {
         Dimension dimension;
         dimension = getRatioComponentFrom(parent).getPreferredSize();
         return dimension;
      }
   }

   @Override
   public void removeLayoutComponent(Component parent) {
   }

   public LayoutManager setFill(boolean enabled) {
      this.fill = enabled;
      return this;
   }

}