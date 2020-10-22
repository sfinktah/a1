package view;

import model.interfaces.PokerCard;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Supplier;

import static util.MyMath.MyClamp;
import static util.Timers.debounce;
import static view.AppViewHelpers.Images.*;

/**
 * Panel with curved edges used for drawing playing cards
 */
public class PokerCardPanel extends JPanel {
   /**
    *
    */
   private static final long serialVersionUID = 1L;
   private static final int CARD_WIDTH = 87;
   private static final int CARD_HEIGHT = 130;
   private static final int SHADOW_BORDER = 12;
   private static final boolean DRAW_BACKGROUND_WITH_GRADIENT = false;
   static private Font cardFont = null;
   private final String iconSuitCharacter;
   private final PokerCard playingCard;
   private final Color bgColor;
   private BufferedImage bufferedCardImage;
   private BufferedImage lastBufferedCardImage = null;
   private Color colorSuit;
   private Dimension lastRenderedCardSize = null;
   private String fontRankString = "^";
   private String fontSuitString = "^";
   private Supplier<Void> debouncedUpdateScaledInstance = null;
   private float alpha = 1;

   public PokerCardPanel(PokerCard card, Color bgColor) {
      super();
      setOpaque(false);
      playingCard = card;
      this.bgColor = bgColor;
      this.setForeground(null);
      this.setBackground(null);
      setPreferredSize(new Dimension(87, 130));
      setSize(new Dimension(87, 130));
      setVisible(true);

      // Ensures transparency behind curved card background
      setBackground(null);
      iconSuitCharacter = card.getSuit().toString().toLowerCase();

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

   @Override
   protected void paintComponent(Graphics g) {
      final int oversample = 4;
      super.paintComponent(g);
      int width = CARD_WIDTH * oversample;
      int height = CARD_HEIGHT * oversample;

      // Everything is calibrated for a card that is 87 x 130 px

      // Don't scale things too small
      final double scale = MyClamp(oversample * 0.8f, width / 130f, Float.MAX_VALUE);
      Graphics2D g2d = (Graphics2D) g;

      // We only draw the card once, and consequently resize the buffered image.
      if (bufferedCardImage == null) {

         ImageIcon transparentIcon = createImageIcon("transparent.png");
         ImageIcon bgIcon = new ImageIcon(transparentIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
         bufferedCardImage = convertToBufferedImage(bgIcon.getImage());

         Graphics2D graphics = bufferedCardImage.createGraphics();

         Composite originalComposite = graphics.getComposite();
         // https://www.piwai.info/static/blog_img/porter-duff.png
         AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgColor.getRed() / 255f);
         graphics.setComposite(alphaComposite);

         // Background
         int cornerRadius = 20;
         Dimension arcs = new Dimension((int) (cornerRadius * scale), (int) (cornerRadius * scale));

         if (DRAW_BACKGROUND_WITH_GRADIENT) {
            Color c1 = Color.GRAY;
            Color c2 = getBackground();
            Point2D start = new Point2D.Float((float) (width / 2), 0);
            Point2D end = new Point2D.Float((float) (width / 2), (float) height);
            float[] dist = {0.0f, 0.2f, 0.8f, 1.0f};
            Color[] colors = {c1, c2, c2, c1};
            LinearGradientPaint p =
                  new LinearGradientPaint(start, end, dist, colors);
            graphics.setPaint(p);
         }
         else {
            graphics.setColor(Objects.nonNull(bgColor) ? bgColor : getBackground());
            graphics.fillRoundRect((int) (scale), (int) (scale), width - (int) (scale), height - (int) (scale), arcs.width, arcs.height);
         }


         // The centre suit png
         final int scaledIconSize = (int) (48 * scale);
         final ImageIcon largeIcon = createImageIcon(iconSuitCharacter + ".png");
         ImageIcon scaledIcon = new ImageIcon(largeIcon.getImage().getScaledInstance(scaledIconSize, scaledIconSize, Image.SCALE_SMOOTH));

         graphics.drawImage(scaledIcon.getImage(), width / 2 - half(scaledIconSize), height / 2 - half(scaledIconSize), scaledIconSize, scaledIconSize, null);

         // Load card font from resource
         if (cardFont == null) {
            cardFont = AppViewHelpers.Fonts.fontFromResource("CARDC___.TTF", (float) (20.0f * scale));
         }

         graphics.setColor(colorSuit);
         graphics.setFont(cardFont);

         // get relative offset to ensure rank is aligned horizontally with suit
         int fw1 = graphics.getFontMetrics().stringWidth(fontRankString);
         int fw2 = graphics.getFontMetrics().stringWidth(fontSuitString);

         // manual tweak
         final int offset = 0;

         // Centre rank and suit on top left of card
         graphics.drawString(fontRankString,
               (int) ((offset + 3) * scale + half(fw2 - fw1)),
               (int) ((offset + 26) * scale));
         graphics.drawString(fontSuitString,
               (int) ((offset + 3) * scale),
               (int) ((offset + 26 + 20) * scale));

         // Centre rank and suit on bottom right of card
         drawRotated(graphics, fontRankString,
               (int) (width - (offset + 3) * scale - half(fw2 - fw1)),
               (int) (height - (offset + 26) * scale), 180);
         drawRotated(graphics, fontSuitString,
               (int) (width - (offset + 3) * scale),
               (int) (height - ((offset + 26 + 20) * scale)), 180);

         // restore the context (was set for alpha blending)
         graphics.setComposite(originalComposite);
         graphics.dispose();


         // Now draw the completed card onto a drop-shadow
         ImageIcon dropShadowIcon = createImageIcon("drop-shadow.png");
         ImageIcon dropIcon = new ImageIcon(dropShadowIcon.getImage().getScaledInstance(width + SHADOW_BORDER * 2, height + SHADOW_BORDER * 2, Image.SCALE_SMOOTH));
         BufferedImage bufferedDropImage = convertToBufferedImage(dropIcon.getImage());
         graphics = bufferedDropImage.createGraphics();

         originalComposite = graphics.getComposite();
         alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1);
         graphics.setComposite(alphaComposite);
         graphics.drawImage(bufferedCardImage, SHADOW_BORDER, SHADOW_BORDER, null);

         graphics.setComposite(originalComposite);
         graphics.dispose();

         bufferedCardImage = bufferedDropImage;
      }

      // establish a debounced function to reduce resizes to a maximum of one every 100ms
      if (debouncedUpdateScaledInstance == null) {
         final PokerCardPanel self = this;
         Supplier<Void> updateScaledInstance = new Supplier<Void>() {
            public Void get() {
               if (Objects.isNull(lastRenderedCardSize) || !lastRenderedCardSize.equals(getSize())) {
                  lastRenderedCardSize = getSize();
                  lastBufferedCardImage = getScaledInstance(bufferedCardImage, getWidth(), getHeight(), RenderingHints.VALUE_INTERPOLATION_BILINEAR);
               }
               self.repaint();
               return null;
            }
         };
         debouncedUpdateScaledInstance = debounce(updateScaledInstance, 100);

         // Need to perform an initial draw
         updateScaledInstance.get();
      }

      // schedule a resizing for 100ms, if no more resizes occur before then
      debouncedUpdateScaledInstance.get();

      if (alpha < 1) {
         Composite originalComposite = g2d.getComposite();
         // https://www.piwai.info/static/blog_img/porter-duff.png
         AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
         g2d.setComposite(alphaComposite);
         g2d.drawImage(lastBufferedCardImage, 0, 0, getWidth(), getHeight(), null);
         g2d.setComposite(originalComposite);
      }
      else {
         g2d.drawImage(lastBufferedCardImage, 0, 0, getWidth(), getHeight(), null);
      }
   }

   @Override
   public Dimension getPreferredSize() {
      final double percentage = 1.0;
      final double ratio = 1.5;
      return AppViewHelpers.ContainerTransforms.getDim(this, ratio, percentage, 1 / ratio, 1, 7);
   }

   protected String paramStringImpl(String thisName) {
      final String invalid = isValid() ? "" : ",invalid";
      final String hidden = isVisible() ? "" : ",hidden";
      final String disabled = isEnabled() ? "" : ",disabled";
      final String lm = getLayout() != null ? ",layout=" + getLayout().getClass().getSimpleName() : "";
      return thisName + ',' + getX() + ',' + getY() + ',' + getWidth() + 'x' + getHeight()
            + invalid + hidden + disabled + lm;
   }

   @Override
   protected String paramString() {
      final String thisName = playingCard.toString();
      return paramStringImpl(thisName);
   }

   @Override
   public String toString() {
      return getClass().getName() + '[' + paramString() + ']';
   }

   public void setAlpha(float alpha) {
      this.alpha = MyClamp(alpha, 0f, 1f);
   }
}

