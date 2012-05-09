/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.text.JTextComponent;
import myutils.MyHandyTextField;

/**
 * A modified JFileChooser (for Nimbus and other bugged look-and-feels)
 * setting the File Name text box as the default focused control.
 *
 * Details:
 * In Nimbus, by default the directory combo box at the top of the chooser is given
 * initial focus. This behavior is annoying and unintuitive since that control
 * is rarely used. The odd behavior forces the user to either
 * Tab about half a dozen times, or to click directly on the File Name box, when
 * saving a file. This class corrects the annoyance in Nimbus and other
 * look-and-feels with similar behavior.
 *
 * Implementation quick notes:
 * I use a JDialog to hack the File Name textbox in the JFileChooser, and then
 * install an AncestorListener to set focus to the File Name textbox each time the
 * JFileChooser is shown.
 *
 * The method to hack the text box is similar to that used in my
 * MyHandyOptionPane, but we cannot continue to use a
 * convenient JDialog container after the initial hack because of the complexity
 * of the methods used to display the JFileChooser.
 *
 * @author Tamara Orr
 * Sunday April 1, 2012
 * See MIT license in license.txt.
 */
public class MyFileChooser extends JFileChooser {

    private static final String HUNTING_TEXT = "Looking_for_file_name_textbox";

    public MyFileChooser(Frame owner) {
        super();
        focusFileNameWhenShown(owner);
    }

    private void focusFileNameWhenShown(Frame owner) {
        // Create a dialog to contain the chooser
        final JDialog dialog = new JDialog(owner, this.getDialogTitle(), true);

        dialog.setContentPane(this);

        // Find the "File Name" text component.
        // To help hunt down the right text box, set the Selected File to a known
        // value temporarily. We will search for it in the UI elements.
        File temp = this.getSelectedFile();
        this.setSelectedFile(new File(HUNTING_TEXT));

        JTextComponent txtFileName = findFileNameTextBox(this);

        // (Because setting SelectedFile back to null doesn't clear the text)
        if (temp == null) {
            this.setSelectedFile(new File(""));
        }
        else
            this.setSelectedFile(temp);

        // My workaround for the default-focus problem:
        focusFileName(txtFileName);

        // Install auto-highlighter while we're at it:
        MyHandyTextField.autoHighlight(txtFileName);
    }

    // Recursively search for the file name text box
    private JTextComponent findFileNameTextBox(Container cont) {

        JTextComponent txtReturn = null;
        txtReturn = findFileNameTextBox(txtReturn, cont);
        return txtReturn;
    }
    private JTextComponent findFileNameTextBox(JTextComponent txt
            , Container cont) {

        Component[] components = cont.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (txt != null)
                return txt;
            else if (components[i] instanceof JTextComponent) {
                JTextComponent txtReturn = (JTextComponent) components[i];
                if (txtReturn.getText().equals(HUNTING_TEXT)) {
                    //System.out.println("Found a text component! " + txtReturn.toString());
                    return txtReturn;
                }
            }
            else if ((components[i] instanceof Container)) {
                txt = findFileNameTextBox(txt, (Container) components[i]);
            }
        }
        return txt;

    }

    // Sets the JTextComponent in the given dialog to request focus when shown.
    // Works great if we can use setVisible().
    private void focusFileName(final JDialog dialog
            , final JTextComponent txt) {

        // (componentShown only fires when setVisible() occurs, and JFileChooser
        // does not use setVisible() on its own.)
        dialog.addComponentListener(new ComponentAdapter(){
          @Override
          public void componentShown(ComponentEvent e){
              if (txt != null)
                txt.requestFocusInWindow();
          }
        });
    }

    // Sets up the text component to request focus each time it's shown.
    private void focusFileName(final JTextComponent txt) {

        // A new ancestor is added each time the JFileChooser is shown.
        // So, request focus to the file name text box each time this happens.
        // GRR, this stopped working
        /*this.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                //System.out.println("Requesting Focus");
                try {
                    txt.requestFocusInWindow();
                } catch (Exception ignore) {
                }
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        }); */
        if (txt != null) {
            txt.addHierarchyListener(new HierarchyListener() {

                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0)
                        if (txt.isShowing())
                            txt.requestFocusInWindow();
                }
            });
        }
    }
}
