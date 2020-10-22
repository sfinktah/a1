package view.util.containers;

import java.awt.*;

/**
 * Panel that uses a tiled background image to save on memory (we were using about 1.5 MB, now we are
 * down to 200 KB.
 */
public class BackgroundImagePanel extends ImagePanel {
   private static final long serialVersionUID = 1L;

   @Override
   protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;

      Composite originalComposite = g2d.getComposite();
      // https://www.piwai.info/static/blog_img/porter-duff.png
      AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1.0f);
      g2d.setComposite(alphaComposite);

      if (isOpaque()) {
         g2d.setColor(getBackground());
         g2d.fillRect(0, 0, getWidth(), getHeight());
      }


      if (getImage() != null) {
         TexturePaint paint = new TexturePaint(getImage(), new Rectangle(0, 0, getImage().getWidth(), getImage().getHeight()));
         g2d.setPaint(paint);
         g2d.fillRect(0, 0, getWidth(), getHeight());
         g2d.setPaint(null);
      }

      g2d.setComposite(originalComposite);
   }
}
