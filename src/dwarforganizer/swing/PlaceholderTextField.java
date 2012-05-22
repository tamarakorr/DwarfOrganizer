/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.text.Document;
import myutils.MyHandyTextField;

/**
 * Shows how to add TextPlaceholder to a JTextField.
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class PlaceholderTextField extends JTextField {

    private static final boolean AUTOHIGHLIGHT_DEFAULT = false;
    private static final int PLACEHOLDER_ALPHA = 128;

    private String mstrPlaceholderText = null;

    public PlaceholderTextField() {
        super();
    }
    public PlaceholderTextField(final Document doc, final String text
            , final int columns) {

        this(doc, text, columns, null, AUTOHIGHLIGHT_DEFAULT);
    }
    public PlaceholderTextField(final Document doc, final String text
            , final int columns, final String placeholder
            , final boolean autoHighlight) {

        super(doc, text, columns);
        mstrPlaceholderText = placeholder;
        createPlaceholder();
        if (autoHighlight)
            enableAutoHighlight();
    }
    public PlaceholderTextField(final int columns) {
        this(columns, null, AUTOHIGHLIGHT_DEFAULT);
    }
    public PlaceholderTextField(final int columns, final String placeholder
            , final boolean autoHighlight) {
        super(columns);
        mstrPlaceholderText = placeholder;
        createPlaceholder();
        if (autoHighlight)
            enableAutoHighlight();
    }
    public PlaceholderTextField(final String text) {
        this(text, null, AUTOHIGHLIGHT_DEFAULT);
    }
    public PlaceholderTextField(final String text, final String placeholder
            , final boolean autoHighlight) {
        super(text);
        mstrPlaceholderText = placeholder;
        createPlaceholder();
        if (autoHighlight)
            enableAutoHighlight();
    }
    public PlaceholderTextField(final String text, final int columns) {
        this(text, columns, null, AUTOHIGHLIGHT_DEFAULT);
    }
    public PlaceholderTextField(final String text, final int columns
            , final String placeholder, final boolean autoHighlight) {

        super(text, columns);
        mstrPlaceholderText = placeholder;
        createPlaceholder();
        if (autoHighlight)
            enableAutoHighlight();
    }

    private void createPlaceholder() {
        if (mstrPlaceholderText == null)
            return;
        final TextPlaceholder tp = new TextPlaceholder(mstrPlaceholderText, this
                , TextPlaceholder.Show.ALWAYS);
        tp.getLabel().setForeground(Color.GRAY);
        tp.setAlpha(PLACEHOLDER_ALPHA);
    }
    private void enableAutoHighlight() {
        MyHandyTextField.autoHighlight(this);
    }
}
