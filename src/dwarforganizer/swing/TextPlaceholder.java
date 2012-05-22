/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

/**
 * A label to be shown over a textfield as a text placeholder (watermark).
 * It disappears when clicked on, or when the text component otherwise
 * receives focus.
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class TextPlaceholder { //extends JLabel {

    public enum Show {
        ALWAYS, FOCUS_GAINED, FOCUS_LOST;
    }

    private JTextComponent mtxtText;
    private Document mdocDoc;
    private Show meShow;
    private boolean mbShowOnce;
    private int mintFocusLost;
    private JLabel label;

    public TextPlaceholder(String text, JTextComponent component) {
        this(text, component, Show.FOCUS_LOST);
    }
    public TextPlaceholder(final String text, final JTextComponent component
            , final Show show) {

        mtxtText = component;
        meShow = show;
        mdocDoc = component.getDocument();

        label = new JLabel();
        label.setText(text);
        label.setFont(component.getFont());
        label.setForeground(component.getForeground());
        label.setBorder(new EmptyBorder(component.getInsets()));
        label.setHorizontalAlignment(JLabel.LEADING);

        component.addFocusListener(createFocusListener()); // this
        mdocDoc.addDocumentListener(createDocumentListener()); // this

        component.setLayout(new BorderLayout());
        component.add(label);
        checkForPlaceholder();
    }

    public Show getShow() { return meShow; }
    // Show.ALWAYS = always show the placeholder
    // Show.FOCUS_GAINED = show the prompt when the component gains focus,
    //                     and hide when focus lost
    // Show.FOCUS_LOST (default) = show when the component loses focus, and hide
    //                   when focus gained
    public void setShow(Show newValue) {
        meShow = newValue;
    }

    // Set setShowOnce(true) to show the component once only
    // false = show repeatedly
    public boolean showOnce() { return mbShowOnce; }
    public void setShowOnce(boolean newValue) {
        mbShowOnce = newValue;
    }

    // Change the alpha
    // @param alpha range 0 - 1.0
    public void setAlpha(float alpha) {
        setAlpha((int) (alpha * 255));
    }
    // Change the alpha
    // @param alpha range 0 - 255
    public void setAlpha(int alpha) {
        alpha = alpha > 255 ? 255 : alpha < 0 ? 0 : alpha;

        Color foreground = label.getForeground();
        int red = foreground.getRed();
        int green = foreground.getGreen();
        int blue = foreground.getBlue();

        Color withAlpha = new Color(red, green, blue, alpha);
        label.setForeground(withAlpha);
    }
    // Change the style
    // @param Font.BOLD, Font.ITALIC, Font.BOLD + Font.ITALIC, etc. (from Font class)
    public void setStyle(int style) {
        label.setFont(label.getFont().deriveFont(style));
    }

    private void checkForPlaceholder() {
        // Text entered -> remove placeholder
        if (mdocDoc.getLength() > 0) {
            label.setVisible(false);
            return;
        }

        // Placeholder has been shown once -> remove
        if (mbShowOnce && (mintFocusLost > 0)) {
            label.setVisible(false);
            return;
        }

        // Check Show property and focus to determine if we should display
        // the placeholder.
        if (mtxtText.hasFocus())
            label.setVisible(meShow == Show.ALWAYS
                    || meShow == Show.FOCUS_GAINED);
        else
            label.setVisible(meShow == Show.ALWAYS
                    || meShow == Show.FOCUS_LOST);
    }

    // FocusListener
    private FocusListener createFocusListener() {
        return new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                checkForPlaceholder();
            }

            @Override
            public void focusLost(FocusEvent e) {
                mintFocusLost = 1;
                checkForPlaceholder();
            }
        };
    }

    // DocumentListener
    private DocumentListener createDocumentListener() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                checkForPlaceholder();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkForPlaceholder();
            }

            @Override
            public void changedUpdate(DocumentEvent e) { }
        };
    }
    public JLabel getLabel() {
        return label;
    }
}
