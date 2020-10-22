package util.swing.containers;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Abstract base class for Foreground and Background ImagePanel
 */
public abstract class ImagePanel extends JPanel {
   private static final long serialVersionUID = 1L;

   private BufferedImage image;

   protected String paramString() {
      final String thisName = Objects.toString(getName(), "");
      final String invalid = isValid() ? "" : ",invalid";
      final String hidden = isVisible() ? "" : ",hidden";
      final String disabled = isEnabled() ? "" : ",disabled";
      final String im = image != null ? ",image" : "";
      final String lm = getLayout() != null ? ",layout=" + getLayout().getClass().getSimpleName() : "";
      return thisName + ',' + getX() + ',' + getY() + ',' + getWidth() + 'x' + getHeight()
            + invalid + hidden + disabled + im + lm;
   }

   @Override
   public String toString() {
      return getClass().getName() + '[' + paramString() + ']';
   }

   public BufferedImage getImage() {
      return image;
   }

   public void setImage(URL imgUrl) {
      try {
         image = imgUrl != null ? ImageIO.read(imgUrl) : null;
         repaint();
      } catch (IOException ex) {
         Logger.getLogger(ImagePanel.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

}