/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.prefs.Preferences;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public abstract class MyPrefs {
    public abstract void savePrefs(Preferences prefs);
    public abstract void loadPrefs(Preferences prefs);
    protected void savePreferences() {
        final Preferences prefs = Preferences.userNodeForPackage(
                this.getClass());
        savePrefs(prefs);
    }
    protected void loadPreferences() {
        final Preferences prefs = Preferences.userNodeForPackage(
                this.getClass());
        loadPrefs(prefs);
    }
}
