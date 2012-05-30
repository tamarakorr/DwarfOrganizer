/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dwarforganizer;

import java.awt.Component;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;
import myutils.MyWindowUtils;

/**
 *
 * @author Tamara Orr
 */
public class MyProgress {
    //private int maxSteps;
    private String text;
    private int value;

    private final JProgressBar progBar;
    private final JFrame fProg;

    public MyProgress(final int numSteps, final String windowTitle
            , final Component relativeTo) {
        //this.maxSteps = numSteps;

        text = "";
        value = 0;

        progBar = new JProgressBar(0, numSteps);
        progBar.setStringPainted(true);

        fProg = MyWindowUtils.createSimpleWindow(windowTitle, progBar);
        fProg.setLocationRelativeTo(relativeTo); // Center in screen if null
        fProg.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        fProg.setResizable(false);
        fProg.setSize(300, 65);
        fProg.setVisible(true);
    }
    private String formatDesc() {
//            final int percent = Math.round(((float) value)
//                    / ((float) maxSteps) * 100f);
//            return String.format("%1$s (%2$s%%)", text, percent);
        return text;
    }
    public void setText(final String newText) {
        text = newText;
        showProgress();
    }
    public void setValue(final int newValue) {
        value = newValue;
        showProgress();
    }
    public void increment(final String newText, final int newValue) {
        setText(newText);
        setValue(newValue);
        showProgress();
    }
    private void showProgress() {
        progBar.setValue(value);
        progBar.setString(formatDesc());
    }
    public void done() {
        fProg.dispose();
    }
}
