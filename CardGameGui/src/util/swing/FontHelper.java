package util.swing;

import client.SimpleTestClientGUI;
import util.Debug;
import view.PlayingCardPanel;
import view.SummaryTable;

import java.awt.*;
import java.io.IOException;
import java.net.URL;

/**
 * Used by {@link SummaryTable} and {@link PlayingCardPanel}
 * to load a font from a resource.
 */
public class FontHelper {
   /**
    * Load a {@link Font} from a resource, and resize.
    * <p>
    * <pre>{@code
    *    JButton button = new JButton("Press");
    *    button.setFont(fontFromResource("ABC123__.TTF", 16);
    *    add(button);
    * }</pre>
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
         Debug.println("Couldn't find resource " + resourceName);
         return fontData;
      }

      try {
         fontData = Font.createFont(Font.TRUETYPE_FONT, fontURL.openStream());
      } catch (FontFormatException | IOException e) {
         Debug.println("Exception creating front from " + resourceName);
         e.printStackTrace();
         return fontData;
      }

      return fontData.deriveFont(fontSize);
   }
}