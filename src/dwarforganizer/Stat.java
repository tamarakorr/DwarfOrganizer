/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.Vector;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class Stat {
    
    protected class StatHint {
        protected String hintText;
        protected int hintMin;
        protected int hintMax;
        public StatHint(String hintText, int hintMin, int hintMax) {
            this.hintText = hintText;
            this.hintMin = hintMin;
            this.hintMax = hintMax;
        }
    }
    
    protected String name;
    protected long[] range;
    protected String xmlName;
    protected Vector<StatHint> vStatHints = new Vector<StatHint>();
    
    public Stat(String name, long[] range) {
        this.name = name;
        this.range = range;
    };
    public void addStatHint(String hintText, int hintMin, int hintMax) {
        vStatHints.add(new StatHint(hintText, hintMin, hintMax));
    }
}
