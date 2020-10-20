package view;

import client.SimpleTestClientGUI;
import model.GameEngineGuiDecor;
import model.interfaces.Player;
import model.interfaces.PlayingCard;
import util.Debug;
import util.StringHelpers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.peer.ComponentPeer;
import java.awt.peer.LightweightPeer;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Objects;
import java.util.Vector;

import static view.AppViewHelpers.ContainerTransforms.*;

public class AppViewHelpers {
   public static final float HUE_OFFSET_PER_PLAYER = 180f + 30f;

   public static class Fonts {
      /**
       * Load a {@link Font} from a resource, and resize.
       * <p>
       * <pre>
       *    JButton button = new JButton("Press");
       *    button.setFont(fontFromResource("ABC123__.TTF", 16);
       *    add(button);
       * </pre>
       * <p>
       * To add the font to your project as a resource, create a directory
       * (e.g. "res") in the root of your project, and add it as a <b>source</b>
       * path in Eclipse, or a <b>resource</b> path in IntelliJ.  Put your fonts
       * in there.
       * <p>
       * See: <a href="https://docs.oracle.com/javase/tutorial/2d/text/fonts.html">docs.oracle.com</a>
       *
       * @param resourceName true-type font resource name (filename)
       * @param fontSize     try 16, work from there
       *
       * @return your font, or (on error) the standard dialog font
       */
      static public Font fontFromResource(String resourceName, float fontSize) {
         Font fontData = Font.getFont(Font.DIALOG);

         // If you need to put this method somewhere other than the main JFrame:
         // URL fontURL = Name_Of_Your_App.class.getClassLoader().getResource(resourceName);

         URL fontURL = SimpleTestClientGUI.class.getClassLoader().getResource(resourceName);
         if (fontURL == null) {
            System.err.println("Couldn't find resource " + resourceName);
            return fontData;
         }

         try {
            fontData = Font.createFont(Font.TRUETYPE_FONT, fontURL.openStream());
         } catch (FontFormatException | IOException e) {
            System.err.println("Exception creating front from " + resourceName);
            e.printStackTrace();
            return fontData;
         }

         return fontData.deriveFont(fontSize);
      }
   }

   public static class Animation {
      static private final Vector<Component> clearing = new Vector<>();
      static private Timer clearCardsTimer = null;

      static public void easeOutCards(Player player, JPanel playerPanel, JLayeredPane jLayeredPane, GameEngineGuiDecor gameEngine) {
         // move all the (visible) cards to the layered pane, store at level playerIndex + 500
         for (Component c : playerPanel.getComponents()) {
            if (c instanceof PlayingCardPanel) {
               Integer level = 500 + gameEngine.getPlayerIndex(player);
               if (c.isVisible()) {
                  moveComponentToContainer(c, jLayeredPane, level);
               }
            }
         }

         // add cards belonging to player to a Container to be eased away
         for (Component c : jLayeredPane.getComponentsInLayer(500 + gameEngine.getPlayerIndex(player))) {
            if (c.getParent() != null) {
               synchronized (clearing) {
                  clearing.add(c);
               }
            }
         }

         // establish a `requestNextAnimationFrame()` style timer to trigger the animation frames
         if (clearCardsTimer == null) {
            clearCardsTimer = (new Timer(25, new ActionListener() {
               @Override
               public synchronized void actionPerformed(ActionEvent e) {
                  synchronized (clearing) {
                     if (clearing.isEmpty()) {
                        clearCardsTimer.stop();
                        // nullify, so we know to start a new timer next time
                        clearCardsTimer = null;
                        return;
                     }

                     // Need to use an iterator to allow deleting elements within a loop
                     Iterator<Component> it = clearing.iterator();

                     while (it.hasNext()) {
                        Component c = it.next();
                        // if card is not visible, then it will throw, so recheck each time.
                        if (c instanceof PlayingCardPanel && c.isVisible()) {
                           // just in-case we have an orphaned container
                           if (c.getParent() == null) {
                              it.remove();
                           }
                           // if the card has left the visible window
                           else if (c.getY() + c.getHeight() < -c.getHeight()) {
                              c.getParent().remove(c);
                              it.remove();
                           }
                           // else move up (and a bit left or right) using a simple easing formula
                           else {
                              int moveBy = Integer.max(20, (int) Math.ceil(Math.sqrt((c.getY() * 5 + c.getX())))) / 2;
                              int shakeBy = (int) (
                                    Math.sin(Math.toRadians(Math.sqrt(c.getY()) * Math.sqrt(c.getX()))) * 10f
                              );
                              c.setLocation(c.getX() + shakeBy, c.getY() - moveBy);

                              // fade out
                              ((PlayingCardPanel) c).setAlpha(c.getY() / 500f);
                              c.repaint();
                           }
                        }
                     }
                  }
               }
            }));

            clearCardsTimer.start();
         }
      }

      // https://docs.oracle.com/javase/tutorial/uiswing/events/mouselistener.html
      static public void addMouseListeners(PlayingCard card, int playerIndex, PlayingCardPanel playingCardPanel, JLabel jStatusLabel, JLayeredPane jLayeredPane) {
         playingCardPanel.addMouseMotionListener(new MouseMotionListener() {
            private Point oldPointOnScreen;
            private Point oldCardLocation;

            /**
             * Invoked when a mouse button is pressed on a component and then
             * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
             * delivered to the component where the drag originated until the
             * mouse button is released (regardless of whether the mouse position
             * is within the bounds of the component).
             * <p>
             * Due to platform-dependent Drag&amp;Drop implementations,
             * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
             * Drag&amp;Drop operation.
             *
             * @param e event
             */
            @Override
            public void mouseDragged(MouseEvent e) {
               if (!playingCardPanel.getParent().equals(jLayeredPane)) {
                  // Transferring the playing card container from it's lower panel to the full sized
                  // layered pane is going to take some tricky match to ensure it doesn't jump to the
                  // wrong position.
                  Integer level = 500 + playerIndex;

                  // adjust the oldCardLocation (set before drag starts) to account for the new
                  // parent (otherwise we get an initial jump when dragging)
                  Point containerLocationDiff = moveComponentToContainer(playingCardPanel, jLayeredPane, level);
                  oldCardLocation.translate(containerLocationDiff.x, containerLocationDiff.y);
               }
               else {
                  Point newPointOnScreen = e.getLocationOnScreen();
                  Point movement = pointSubtract(newPointOnScreen, oldPointOnScreen);
                  Point newCardLocation = pointAdd(oldCardLocation, movement);
                  playingCardPanel.setLocation(newCardLocation);
                  jLayeredPane.moveToFront(playingCardPanel);
               }
            }


            /**
             * Invoked when the mouse cursor has been moved onto a component
             * but no buttons have been pushed.
             *
             * @param e event
             */
            @Override
            public void mouseMoved(MouseEvent e) {
               oldPointOnScreen = e.getLocationOnScreen();
               oldCardLocation = e.getComponent().getLocation();
            }
         });

         playingCardPanel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            // This has to be declared in AppView to maintain access to jStatusLabel
            @Override
            public void mouseEntered(MouseEvent e) {
               jStatusLabel.setText(String.format("<html>%s. <strong>Click to drag this card.</strong> <i>(Current parent: %s)</i></html>",
                     card.toString(),
                     StringHelpers.isNullOrEmpty(playingCardPanel.getParent().getName())
                           ? playingCardPanel.getParent().getClass().getName()
                           : playingCardPanel.getParent().getName()
               ));
            }

            // This has to be declared in AppView to maintain access to jStatusLabel
            @Override
            public void mouseExited(MouseEvent e) {
               jStatusLabel.setText("");
            }
         });
      }

   }

   public static class ContainerTransforms {

      /**
       * Move {@link Component} from one {@link Container} to another, while retaining the same on-screen position
       *
       * @param c             component to move
       * @param destContainer destination container
       * @param level         destination layer level (or null if destination is not {@link JLayeredPane}
       *
       * @return positional adjustment made to {@link Component}
       *
       * @see #getLocationOnContainer(Component, Container)
       */
      static public Point moveComponentToContainer(Component c, Container destContainer, Integer level) {
         Point pt1 = ContainerTransforms.getLocationOnContainer(c, destContainer);
         Point newContainerLocation = destContainer.getLocationOnScreen();
         Point oldContainerLocation = c.getParent().getLocationOnScreen();
         Point containerLocationDiff = pointSubtract(oldContainerLocation, newContainerLocation);

         c.setLocation(pt1);
         destContainer.add(c, level);

         return containerLocationDiff;
      }

      public static Point pointSubtract(Point p1, Point p2) {
         return new Point(p1.x - p2.x, p1.y - p2.y);
      }

      public static Point pointAdd(Point p1, Point p2) {
         return new Point(p1.x + p2.x, p1.y + p2.y);
      }

      /**
       * Determine the position of component relative to container.
       *
       * @param component Component
       * @param host      Container
       *
       * @return position
       */
      @SuppressWarnings("deprecation")
      static public Point getLocationOnContainer(Component component, Container host) {
         // General output from debug:
         //     Component has peer and isShowing()
         //     LightweightPeer
         synchronized (component.getTreeLock()) {
            ComponentPeer peer = component.getPeer();
            if (peer != null && component.isShowing()) {
               if (peer instanceof LightweightPeer) {
                  Point containerOnScreen = component.getLocationOnScreen();
                  Point hostOnScreen = host.getLocationOnScreen();
                  return new Point(containerOnScreen.x - hostOnScreen.x, containerOnScreen.y - hostOnScreen.y);
               }
               else {
                  // lame effort, won't really work.  but haven't found a !lightweightpeer yet
                  Debug.format("getLocationOnContainer: Component is not a LeightweightPeer: %s", component);
                  return peer.getLocationOnScreen();
               }
            }
            else {
               // Component is useless, return useless co-ordinates rather than throw.
               return new Point(0, 0);
            }
         }
      }

      /**
       * Helper to create a new Component, and name it
       *
       * @param type      Component.class
       * @param debugName "My Component"
       * @param <T>       Magic Generic Stuff
       *
       * @return new instance of Component.class, with name set to <pre>debugName</pre>
       */
      public static <T> T namedComponent(Class<T> type, String debugName) {
         try {
            T panel = type.newInstance();
            if (panel instanceof Component) {
               ((Component) panel).setName(debugName);
            }
            return panel;
         } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
         }
         return null;
      }

      public static void setTextAndToolTip(AbstractButton component, String text, String toolTip) {
         component.setText(text);
         component.setToolTipText(toolTip);
      }

      /**
       * Generates a unique-ish color using a hue shift on #009900, rotating HUE_OFFSET_PER_PLAYER degrees per playerId.
       *
       * @param player player
       *
       * @return A new and unique java.awt.Color
       */
      public static Color getPlayerColor(Player player) {
         if (player == null) {
            return Color.BLACK;
         }
         return getPlayerColor(StringHelpers.stringAsInt(player.getPlayerId(), 0));
      }

      public static Color getPlayerColor(int playerIndex) {
         return new Color(
               Color.HSBtoRGB((130f / 360f) + (HUE_OFFSET_PER_PLAYER / 360f * playerIndex),
                     0.7f,
                     1.0f
               ));
      }

      /**
       * Return an empty string if input is 0 (makes tables look neater)
       *
       * @param i integer
       *
       * @return string containing number, or empty string is number is 0
       */
      public static String hiddenZero(int i) {
         return 0 != i ? String.valueOf(i) : "";
      }

      /**
       * Helper for generating dimensions that follow an aspect-ratio
       *
       * @param self       Component to get {@link Dimension} for
       * @param ratio      Ratio (1.5 for playing cards)
       * @param percentage set to 0.9 to leave 10% space between
       * @param widthBy    multiply resulting width by
       * @param heightBy   multiply resulting height by
       * @param count      account for <pre>count</pre> playing cards
       *
       * @return Dimension that fits the aspect ratio
       */
      static public Dimension getDim(JComponent self, double ratio, double percentage, double widthBy, double heightBy, int count) {
         Dimension parentDimension = self.getParent().getSize();
         parentDimension.getWidth();
         parentDimension.getHeight();

         int dim = Math.min((int) (ratio * parentDimension.width / count), parentDimension.height);
         if (dim == 0) {
            dim = 100;
         }
         else {
            // Add % padding between cards
            dim = (int) (dim * percentage);
         }
         return new Dimension((int) (dim * widthBy), (int) (dim * heightBy));
      }

   }

   public static class Images {

      public static URL getResourceUrl(String resourceName) {
         return SimpleTestClientGUI.class.getClassLoader().getResource(resourceName);
      }

      /**
       * Returns an ImageIcon, or null if the path was invalid.
       *
       * @param path resource path
       *
       * @return ImageIcon image
       */
      public static ImageIcon createImageIcon(String path) {
         URL imageUrl = SimpleTestClientGUI.class.getClassLoader().getResource(path);
         if (imageUrl != null) {
            return new ImageIcon(imageUrl);
         }
         else {
            System.err.println("Couldn't find resource: " + path);
            return null;
         }
      }

      public static BufferedImage convertToBufferedImage(Image image) {
         BufferedImage newImage = new BufferedImage(
               image.getWidth(null), image.getHeight(null),
               BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = newImage.createGraphics();
         g.drawImage(image, 0, 0, null);
         g.dispose();
         return newImage;
      }

      public static void drawRotated(Graphics2D g2d, String text, int x, int y, int angle) {
         g2d.translate((float) x, (float) y);
         g2d.rotate(Math.toRadians(angle));
         g2d.drawString(text, 0, 0);
         g2d.rotate(-Math.toRadians(angle));
         g2d.translate(-(float) x, -(float) y);
      }

      public static BufferedImage getScaledInstance(BufferedImage bufferedImage,
                                                    int targetWidth,
                                                    int targetHeight,
                                                    Object hint) {
         int type = bufferedImage.getTransparency() == Transparency.OPAQUE ?
               BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

         BufferedImage result = bufferedImage;
         int w = bufferedImage.getWidth();
         int h = bufferedImage.getHeight();

         do {
            if (w > targetWidth) {
               w /= 2;
               if (w < targetWidth) {
                  w = targetWidth;
               }
            }

            if (h > targetHeight) {
               h /= 2;
               if (h < targetHeight) {
                  h = targetHeight;
               }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(result, 0, 0, w, h, null);
            g2.dispose();

            result = tmp;
         } while (w > targetWidth || h > targetHeight);

         return result;
      }

      public static int half(int i) {
         return i / 2;
      }

      @SuppressWarnings({"unused", "RedundantSuppression"})
      public static double half(double i) {
         return i / 2;
      }

   }

   /**
    * A simple class to enable a hidden value to be used with JComboBox
    *
    * @param <N> Type of name (visible text in JComboBox)
    * @param <V> Type of hidden value
    */
   public static class JComboBoxItem<N, V> implements Comparable<JComboBoxItem<N, V>> {
      private final N name;
      private final V value;

      /**
       * @param name  text visible in JComboBox
       * @param value hidden associated value
       */
      public JComboBoxItem(N name, V value) {
         this.name = name;
         this.value = value;
      }

      public N getName() {
         return name;
      }

      public V getValue() {
         return value;
      }

      // not used unless you use a Sorted ComboBox -- see https://stackoverflow.com/questions/17061314/how-to-sort-the-jcombobox-elements-in-java-swing^kkk
      public int compareTo(JComboBoxItem<N, V> item) {
         return getValue().toString().compareTo(item.getValue().toString());
      }

      @Override
      public int hashCode() {
         return Objects.hash(name, value);
      }

      @Override
      public String toString() {
         return getName().toString();
      }
   }
}
