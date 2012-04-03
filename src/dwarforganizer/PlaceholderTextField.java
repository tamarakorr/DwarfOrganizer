/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.text.Document;
import myutils.MyHandyTextField;

/**
 * Shows how to add TextPlaceholder to a JTextField.
 * @author Tamara Orr
 */
public class PlaceholderTextField extends JTextField {

    private static final boolean AUTOHIGHLIGHT_DEFAULT = false;
    
    private String mstrPlaceholderText = null;
    
    public PlaceholderTextField() {
        super();
    }
    public PlaceholderTextField(Document doc, String text, int columns) {
        this(doc, text, columns, null, AUTOHIGHLIGHT_DEFAULT);
    }
    public PlaceholderTextField(Document doc, String text, int columns
            , String placeholder, boolean autoHighlight) {
        super(doc, text, columns);
        mstrPlaceholderText = placeholder;
        createPlaceholder();
        if (autoHighlight) enableAutoHighlight();
    }
    public PlaceholderTextField(int columns) {
        this(columns, null, AUTOHIGHLIGHT_DEFAULT);
    }
    public PlaceholderTextField(int columns, String placeholder
            , boolean autoHighlight) {
        super(columns);
        mstrPlaceholderText = placeholder;
        createPlaceholder();
        if (autoHighlight) enableAutoHighlight();
    }
    public PlaceholderTextField(String text) {
        this(text, null, AUTOHIGHLIGHT_DEFAULT);
    }
    public PlaceholderTextField(String text, String placeholder
            , boolean autoHighlight) {
        super(text);
        mstrPlaceholderText = placeholder;
        createPlaceholder();
        if (autoHighlight) enableAutoHighlight();
    }
    public PlaceholderTextField(String text, int columns) {
        this(text, columns, null, AUTOHIGHLIGHT_DEFAULT);
    }
    public PlaceholderTextField(String text, int columns, String placeholder
            , boolean autoHighlight) {
        super(text, columns);
        mstrPlaceholderText = placeholder;
        createPlaceholder();
        if (autoHighlight) enableAutoHighlight();
    }
    
    private void createPlaceholder() {
        if (mstrPlaceholderText == null)
            return;
        TextPlaceholder tp = new TextPlaceholder(mstrPlaceholderText, this
                , TextPlaceholder.Show.ALWAYS);
        tp.setForeground(Color.GRAY);
        tp.setAlpha(128);
    }
    private void enableAutoHighlight() {
        MyHandyTextField.autoHighlight(this);
    }
}
