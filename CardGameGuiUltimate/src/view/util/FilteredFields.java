package view.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.function.Predicate;

public class FilteredFields {
   /* Filtered Text Field
    * @param   regex
    *          the regular expression to which this string is to be matched
    *
    * @return  {@code true} if, and only if, this string matches the
    *          given regular expression
    *
    * @throws  PatternSyntaxException
    *          if the regular expression's syntax is invalid
    *
    * @see     https://stackoverflow.com/a/63964109/912236 (This is **my** SO answer, not 3rd party code)
    */
   static public JTextField createFilteredField(String regex) {
      JTextField field = createField(regex);
      AbstractDocument document = (AbstractDocument) field.getDocument();
      final int maxCharacters = 20;
      document.setDocumentFilter(new DocumentFilter() {

         /**
          * Invoked prior to removal of the specified region in the
          * specified Document. Subclasses that want to conditionally allow
          * removal should override this and only call supers implementation as
          * necessary, or call directly into the <code>FilterBypass</code> as
          * necessary.
          *
          * @param fb FilterBypass that can be used to mutate Document
          * @param offset the offset from the beginning &gt;= 0
          * @param length the number of characters to remove &gt;= 0
          * @exception BadLocationException  some portion of the removal range
          *   was not a valid part of the document.  The location in the exception
          *   is the first bad position encountered.
          */
         public void remove(FilterBypass fb, int offset, int length) throws
               BadLocationException {
            String text = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = text.substring(0, offset) + text.substring(offset + length);
            if (newText.matches(regex) || newText.length() == 0) {
               super.remove(fb, offset, length);
            }
         }

         /**
          * Invoked prior to replacing a region of text in the
          * specified Document. Subclasses that want to conditionally allow
          * replace should override this and only call supers implementation as
          * necessary, or call directly into the FilterBypass.
          *
          * @param fb FilterBypass that can be used to mutate Document
          * @param offset Location in Document
          * @param length Length of text to delete
          * @param _text Text to insert, null indicates no text to insert
          * @param attrs AttributeSet indicating attributes of inserted text,
          *              null is legal.
          * @exception BadLocationException  the given insert position is not a
          *   valid position within the document
          */
         public void replace(FilterBypass fb, int offset, int length,
                             String _text, AttributeSet attrs) throws BadLocationException {

            String text = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = text.substring(0, offset) + _text + text.substring(offset + length);
            if (Objects.isNull(regex) || newText.length() <= maxCharacters && newText.matches(regex)) {
               super.replace(fb, offset, length, _text, attrs);
            }
            else {
               Toolkit.getDefaultToolkit().beep();
            }
         }

         /**
          * Invoked prior to insertion of text into the
          * specified Document. Subclasses that want to conditionally allow
          * insertion should override this and only call supers implementation as
          * necessary, or call directly into the FilterBypass.
          *
          * @param fb FilterBypass that can be used to mutate Document
          * @param offset  the offset into the document to insert the content &gt;= 0.
          *    All positions that track change at or after the given location
          *    will move.
          * @param string the string to insert
          * @param attr      the attributes to associate with the inserted
          *   content.  This may be null if there are no attributes.
          * @exception BadLocationException  the given insert position is not a
          *   valid position within the document
          */
         public void insertString(FilterBypass fb, int offset, String string,
                                  AttributeSet attr) throws BadLocationException {

            String text = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = text.substring(0, offset) + string + text.substring(offset);
            if ((fb.getDocument().getLength() + string.length()) <= maxCharacters
                  && newText.matches(regex)) {
               super.insertString(fb, offset, string, attr);
            }
            else {
               Toolkit.getDefaultToolkit().beep();
            }
         }
      });
      return field;
   }

   public static JTextField createField(String regex) {
      JTextField field = new JTextField(12);
      if (regex == null) {
         field.setEditable(false);
         field.setEnabled(true);
         Border border = field.getBorder();
         field.setBorder(new EmptyBorder(border.getBorderInsets(field)));
         field.setBackground(Color.LIGHT_GRAY);
         return field;
      }

      Insets bi = field.getBorder().getBorderInsets(field);
      int top = bi.top;
      int left = bi.left;
      int bottom = bi.bottom;
      int right = bi.right;
      int offset_w = 4;
      int offset_h = 4;
      Border b1 = new EmptyBorder((top - offset_h), (left - offset_w), (bottom - offset_h), (right - offset_w));
      Border b2 = new LineBorder(Color.DARK_GRAY, 1);
      Border b3 = new CompoundBorder(b2, b1);
      field.setBorder(b3);
      field.addKeyListener(new KeyAdapter() {
         @Override
         public void keyPressed(KeyEvent evt) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
               field.transferFocus();
            }
         }
      });

      return field;
   }

   public static JTextField createFilteredField(String regex, Predicate<String> predicate) {
      JTextField field = createField(regex);
      AbstractDocument document = (AbstractDocument) field.getDocument();
      final int maxCharacters = 20;
      document.setDocumentFilter(new DocumentFilter() {

         /**
          * Invoked prior to removal of the specified region in the
          * specified Document. Subclasses that want to conditionally allow
          * removal should override this and only call supers implementation as
          * necessary, or call directly into the <code>FilterBypass</code> as
          * necessary.
          *
          * @param fb FilterBypass that can be used to mutate Document
          * @param offset the offset from the beginning &gt;= 0
          * @param length the number of characters to remove &gt;= 0
          * @exception BadLocationException  some portion of the removal range
          *   was not a valid part of the document.  The location in the exception
          *   is the first bad position encountered.
          */
         public void remove(FilterBypass fb, int offset, int length) throws
               BadLocationException {
            String text = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = text.substring(0, offset) + text.substring(offset + length);
            if ((predicate.test(newText) && newText.matches(regex)) || newText.length() == 0) {
               super.remove(fb, offset, length);
            }
         }

         /**
          * Invoked prior to replacing a region of text in the
          * specified Document. Subclasses that want to conditionally allow
          * replace should override this and only call supers implementation as
          * necessary, or call directly into the FilterBypass.
          *
          * @param fb FilterBypass that can be used to mutate Document
          * @param offset Location in Document
          * @param length Length of text to delete
          * @param _text Text to insert, null indicates no text to insert
          * @param attrs AttributeSet indicating attributes of inserted text,
          *              null is legal.
          * @exception BadLocationException  the given insert position is not a
          *   valid position within the document
          */
         public void replace(FilterBypass fb, int offset, int length,
                             String _text, AttributeSet attrs) throws BadLocationException {

            String text = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = text.substring(0, offset) + _text + text.substring(offset + length);
            if (newText.length() <= maxCharacters && newText.matches(regex) && predicate.test(newText)) {
               super.replace(fb, offset, length, _text, attrs);
            }
            else {
               Toolkit.getDefaultToolkit().beep();
            }
         }

         /**
          * Invoked prior to insertion of text into the
          * specified Document. Subclasses that want to conditionally allow
          * insertion should override this and only call supers implementation as
          * necessary, or call directly into the FilterBypass.
          *
          * @param fb FilterBypass that can be used to mutate Document
          * @param offset  the offset into the document to insert the content &gt;= 0.
          *    All positions that track change at or after the given location
          *    will move.
          * @param string the string to insert
          * @param attr      the attributes to associate with the inserted
          *   content.  This may be null if there are no attributes.
          * @exception BadLocationException  the given insert position is not a
          *   valid position within the document
          */
         public void insertString(FilterBypass fb, int offset, String string,
                                  AttributeSet attr) throws BadLocationException {

            String text = fb.getDocument().getText(0, fb.getDocument().getLength());
            String newText = text.substring(0, offset) + string + text.substring(offset);
            if ((fb.getDocument().getLength() + string.length()) <= maxCharacters
                  && newText.matches(regex) && predicate.test(newText)) {
               super.insertString(fb, offset, string, attr);
            }
            else {
               Toolkit.getDefaultToolkit().beep();
            }
         }
      });
      return field;
   }

}
