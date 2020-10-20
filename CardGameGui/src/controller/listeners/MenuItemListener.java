package controller.listeners;

import model.GameEngineImplEx;
import util.Debug;
import view.AppView;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MenuItemListener implements ActionListener {
   private final GameEngineImplEx model;
   private final AppView appView;

   @SuppressWarnings("RedundantSuppression")
   public MenuItemListener(GameEngineImplEx model, AppView appView) {
      this.model = model;
      this.appView = appView;
   }

   public static void openUrl(String url) {
      try {
         Desktop.getDesktop().browse(new URI(url));
      } catch (URISyntaxException | IOException uriSyntaxException) {
         uriSyntaxException.printStackTrace();
         JOptionPane.showMessageDialog(null,
               "Unable to open URL\n" + url,
               "Error",
               JOptionPane.INFORMATION_MESSAGE);
      }
   }

   public static String simpleSerializeGameState(GameEngineImplEx model) {
      String encoded = "";
      try {
         try (ByteArrayOutputStream fileOut = new ByteArrayOutputStream()) {
            try (ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
               out.writeObject(model.getPlayerList());
            }
            encoded = sillyEncode(ByteBuffer.wrap(fileOut.toByteArray()));

            // Convert ugly serialization format to:
            // 0;0#House:0$
            // 1;1#The Loser:500$
            // 2;2#The Shark:1000$
            // 3;3#The Rock:500$
            // 4;4#The Whale:1000$
            // 5;5#The Fish:500$
            // 6;6#The Caller:1000$

            StringBuilder stringBuilder = new StringBuilder();
            for (String s : encoded.split(" \\^", 0)) {
               if (s.contains("$ ")) {
                  stringBuilder.append(s.replaceFirst("\\$ .*", "\\$\n"));
               }
            }
            encoded = stringBuilder.toString();


         }
      } catch (IOException i) {
         i.printStackTrace();
      }
      return encoded;
   }

   public static void setSystemClipboard(String encoded) {
      Toolkit.getDefaultToolkit()
            .getSystemClipboard()
            .setContents(new StringSelection(encoded), null);
   }

   public static String sillyEncode(ByteBuffer buffer) {
      StringBuilder s = new StringBuilder();

      while (buffer.hasRemaining()) {
         byte b = buffer.get();
         if (b > 31 && b < 127 && b != '[') {
            s.append((char) b);
         }
         else if (b > 0) {
            s.append("[");
            s.append(b);
            s.append("]");
         }
      }

      return s.toString();
   }

   @Override
   public void actionPerformed(ActionEvent e) {
      switch (e.getActionCommand()) {
         case "New Player":
            new AddPlayerButtonListener(model).actionPerformed(e);
            break;

         case "Save":
            String serialized = simpleSerializeGameState(model);
            try (FileOutputStream fileOut = new FileOutputStream("saved.txt")) {
               fileOut.write(serialized.getBytes(StandardCharsets.UTF_8));
               JOptionPane.showMessageDialog(null,
                     "<html><p><center><font size=+0>Player State " +
                           "has been saved to <b>saved.txt</b></font></center></p><p><p>" +
                           "<i>(what? File Selection Dialogs don't grow on trees you know!)" +
                           "<p><p>",
                     "Saved", JOptionPane.PLAIN_MESSAGE);
            } catch (IOException ioException) {
               ioException.printStackTrace();
               JOptionPane.showMessageDialog(null, "<html>Could not write to <b>saved.txt", "Error Saving", JOptionPane.ERROR_MESSAGE);
            }
            break;

         case "Exit":
            System.exit(0);
            break;

         case "Copy":
            serialized = simpleSerializeGameState(model);
            setSystemClipboard(serialized);
            appView.setStatus("Copied 'PlayerState' to clipboard");
            break;

         case "Nothing":
            break;

         case "About":
            openUrl("https://www.linkedin.com/in/n95/");
            break;

         default:
            Debug.println("Unhandled Menu Item: " + e.getActionCommand());

      }
   }
}
