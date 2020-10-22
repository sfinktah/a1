package view;

import client.SwingClient;
import model.interfaces.PokerCard;
import util.swing.FontHelper;
import util.swing.jComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

import static util.MyMath.MyClamp;

/**
 * Panel with curved edges that embodies a playing card
 */
public class PokerCardPanel extends JPanel {
   private static final long serialVersionUID = 1L;
   private static final int CARD_WIDTH = 87;
   private static final int CARD_HEIGHT = 130;
   static private final Map<String, ImageIcon> suitIconMap = new TreeMap<>();
   static private Font cardFont;
   private final String iconSuitName;
   private final PokerCard card;
   private final Color bgColor;
   private final Consumer<String> setStatus;
   private BufferedImage bufferedCardImage;
   private BufferedImage lastBufferedCardImage = null;
   private Color colorSuit;
   private Dimension lastRenderedCardSize = null;
   private String fontRankString = "^";
   private String fontSuitString = "^";

   public PokerCardPanel(PokerCard card, boolean isBustCard, Consumer<String> setStatus) {
      super();
      this.card = card;
      this.bgColor = isBustCard ? new Color(196, 196, 196) : new Color(224, 224, 224);
      this.setStatus = setStatus;
      jComponent.builder(this)
            .opaque(false)
            .foreground(null)
            .background(null)
            .preferredSize(87, 130)
            .size(87, 130)
            .visible(true)
            .build();

      // add listeners to update status display when hovering over a playing card.
      // included on recommendation of Casper Ryan
      addMouseListeners();

      iconSuitName = card.getSuit().toString().toLowerCase();

      // get corresponding character for card suit in CardFont.
      switch (card.getSuit()) {
         case HEARTS:
            fontSuitString = "{";
            colorSuit = Color.RED;
            break;
         case SPADES:
            fontSuitString = "}";
            colorSuit = Color.BLACK;
            break;
         case CLUBS:
            fontSuitString = "]";
            colorSuit = new Color(0x0f852a); // Green
            break;
         case DIAMONDS:
            fontSuitString = "[";
            colorSuit = new Color(0x026ebc); // Blue
            break;
      }

      // get corresponding character for card rank in CardFont.
      switch (card.getValue()) {
         case EIGHT:
            fontRankString = "8";
            break;
         case NINE:
            fontRankString = "9";
            break;
         case TEN:
            fontRankString = "=";
            break;
         case JACK:
            fontRankString = "J";
            break;
         case QUEEN:
            fontRankString = "Q";
            break;
         case KING:
            fontRankString = "K";
            break;
         case ACE:
            fontRankString = "A";
            break;
      }
   }

   static public BufferedImage createTransparentBufferedImage(int width, int height) {
      BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = bufferedImage.createGraphics();

      // Store original composite
      Composite originalComposite = graphics.getComposite();

      // fill with 100% transparent pixels
      AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f);
      graphics.setComposite(alphaComposite);

      graphics.setColor(Color.WHITE);
      graphics.fillRect(0, 0, width, height);

      // Restore composite
      graphics.setComposite(originalComposite);
      graphics.dispose();

      return bufferedImage;
   }

   /**
    * Returns an ImageIcon, or null if the path was invalid.
    *
    * @param path resource path
    *
    * @return ImageIcon image
    */
   public static ImageIcon createImageIcon(String path) {
      URL imageUrl = SwingClient.class.getClassLoader().getResource(path);
      if (imageUrl != null) {
         return new ImageIcon(imageUrl);
      }
      else {
         System.err.println("Couldn't find resource: " + path);
         return null;
      }
   }

   public static void drawRotated(Graphics2D g2d, String text, int x, int y, int angle) {
      g2d.translate((float) x, (float) y);
      g2d.rotate(Math.toRadians(angle));
      g2d.drawString(text, 0, 0);
      g2d.rotate(-Math.toRadians(angle));
      g2d.translate(-(float) x, -(float) y);
   }

   /**
    * The standard getScaledInstance scales badly when reducing less than 50%, this function
    * is design to prevent that but rescaling in multiples of 50% when necessary.
    *
    * @param bufferedImage source image
    * @param targetWidth   target width
    * @param targetHeight  target height
    * @param hint          e.g. RenderingHints.VALUE_INTERPOLATION_BILINEAR
    *
    * @return new rescaled BufferedImage
    */
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

   @Override
   protected void paintComponent(Graphics g) {
      final int OVERSAMPLE = 4;
      super.paintComponent(g);
      int width = CARD_WIDTH * OVERSAMPLE;
      int height = CARD_HEIGHT * OVERSAMPLE;

      // everything here is calibrated for a card that is 87 x 130 px

      // don't scale things too small
      final double scale = MyClamp(OVERSAMPLE * 0.8f, width / 130f, Float.MAX_VALUE);
      Graphics2D g2d = (Graphics2D) g;

      // we only draw the card once, and consequently resize the buffered image.
      if (bufferedCardImage == null) {

         bufferedCardImage = createTransparentBufferedImage(width, height);

         Graphics2D graphics = bufferedCardImage.createGraphics();

         // save original composite
         Composite originalComposite = graphics.getComposite();

         // https://www.piwai.info/static/blog_img/porter-duff.png
         AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgColor.getRed() / 255f);
         graphics.setComposite(alphaComposite);

         // Draw background of card
         final int cornerRadius = 20;
         Dimension arcs = new Dimension((int) (cornerRadius * scale), (int) (cornerRadius * scale));
         graphics.setColor(Objects.nonNull(bgColor) ? bgColor : getBackground());
         graphics.fillRoundRect((int) scale,
               (int) scale,
               width - (int) scale,
               height - (int) scale,
               arcs.width,
               arcs.height);

         // The centre suit png
         final int scaledSuitIconSize = (int) (48 * scale);
         synchronized (suitIconMap) {
            // lets save ourselves some memory and CPU cycles, since these PNGs are 512x512 with transparency
            ImageIcon suitIcon = suitIconMap.computeIfAbsent(iconSuitName, new Function<String, ImageIcon>() {
               @Override
               public ImageIcon apply(String suitName) {
                  final ImageIcon rawSuitIcon = createImageIcon(suitName + ".png");
                  return new ImageIcon(rawSuitIcon.getImage().getScaledInstance(scaledSuitIconSize,
                        scaledSuitIconSize,
                        Image.SCALE_SMOOTH));
               }
            });

            graphics.drawImage(suitIcon.getImage(),
                  width / 2 - scaledSuitIconSize / 2,
                  height / 2 - scaledSuitIconSize / 2,
                  scaledSuitIconSize,
                  scaledSuitIconSize,
                  null);
         }

         // Load card font from resource
         if (cardFont == null) {
            cardFont = FontHelper.fontFromResource("CARDC___.TTF", (float) (20.0f * scale));
         }

         graphics.setColor(colorSuit);
         graphics.setFont(cardFont);

         // get relative offset to ensure rank is aligned horizontally with suit
         int line1 = graphics.getFontMetrics().stringWidth(fontRankString);
         int line2 = graphics.getFontMetrics().stringWidth(fontSuitString);

         // centre rank and suit (relative to each other) on top left of card
         graphics.drawString(fontRankString,
               (int) (3 * scale + (line2 - line1) / 2),
               (int) (26 * scale));
         graphics.drawString(fontSuitString,
               (int) (3 * scale),
               (int) ((26 + 20) * scale));

         // centre rank and suit (relative to each other) on bottom right of card
         drawRotated(graphics, fontRankString,
               (int) (width - 3 * scale - (line2 - line1) / 2),
               (int) (height - 26 * scale), 180);
         drawRotated(graphics, fontSuitString,
               (int) (width - 3 * scale),
               (int) (height - (26 + 20) * scale), 180);

         // restore the context (was set for alpha blending)
         graphics.setComposite(originalComposite);
         graphics.dispose();
      }

      if (isVisible() && getParent() != null && getParent().isVisible()) {
         if (Objects.isNull(lastRenderedCardSize) || !lastRenderedCardSize.equals(getSize())) {
            lastRenderedCardSize = getSize();
            lastBufferedCardImage = getScaledInstance(bufferedCardImage, getWidth(), getHeight(), RenderingHints.VALUE_INTERPOLATION_BILINEAR);
         }
         g2d.drawImage(lastBufferedCardImage, 0, 0, getWidth(), getHeight(), null);
      }
   }

   @Override
   public Dimension getPreferredSize() {
      Dimension parentSize = getParent().getSize();
      int dim = Math.min((int) (1.5 * parentSize.width / 7), parentSize.height);
      return new Dimension((int) (dim * (1 / 1.5)), dim);
   }

   /**
    * listeners to update status display when hovering over a playing card.
    * (included on recommendation of Caspar Ryan).
    * see: https://docs.oracle.com/javase/tutorial/uiswing/events/mouselistener.html
    */
   public void addMouseListeners() {
      this.addMouseListener(new MouseListener() {
         // @formatter:off
         @Override public void mouseClicked(MouseEvent e)  { }
         @Override public void mousePressed(MouseEvent e)  { }
         @Override public void mouseReleased(MouseEvent e) { }
         // @formatter:on

         @Override
         public void mouseEntered(MouseEvent e) {
            setStatus.accept(card.toString());
         }

         @Override
         public void mouseExited(MouseEvent e) {
            setStatus.accept("");
         }
      });
   }

}

