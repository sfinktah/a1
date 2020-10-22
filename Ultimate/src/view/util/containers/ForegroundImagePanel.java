package view.util.containers;

import java.awt.*;

/**
 * Show a centered foreground image.
 */
public class ForegroundImagePanel extends ImagePanel {
   private static final long serialVersionUID = 1L;

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);

      if (getImage() != null) {
         Graphics2D g2d = (Graphics2D) g;
         Composite originalComposite = g2d.getComposite();
         // https://www.piwai.info/static/blog_img/porter-duff.png
         AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f);
         g2d.setComposite(alphaComposite);

         float w = (float) getImage().getWidth();
         float h = (float) getImage().getHeight();
         g2d.drawImage(getImage(), (int) ((float) getWidth() / 2 - w / 2), (int) (getHeight() / 2 - h / 2), null);

         g2d.setComposite(originalComposite);
      }
   }
}