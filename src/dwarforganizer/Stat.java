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

    private String name;
    private int[] range;
    private String xmlName;
    protected Vector<StatHint> vStatHints;

    public Stat(String name, int[] range) {
        this.name = name;
        this.range = range;

        vStatHints = new Vector<StatHint>();
    };
    public void addStatHint(String hintText, int hintMin, int hintMax) {
        vStatHints.add(new StatHint(hintText, hintMin, hintMax));
    }

    public String getName() {
        return name;
    }

    public String getXmlName() {
        return xmlName;
    }

    public void setXmlName(String xmlName) {
        this.xmlName = xmlName;
    }

    public int[] getRange() {
        return range;
    }

}
