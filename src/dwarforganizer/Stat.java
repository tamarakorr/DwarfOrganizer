/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.util.ArrayList;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class Stat {

    private String name;
    private int[] range;
    private String xmlName;
    private ArrayList<StatHint> lstStatHints;

    public Stat(final String name, final int[] range) {
        this.name = name;
        this.range = range;

        lstStatHints = new ArrayList<StatHint>();
    };
    public class StatHint {
        protected String hintText;
        protected int hintMin;
        protected int hintMax;
        public StatHint(final String hintText, final int hintMin
                , final int hintMax) {

            this.hintText = hintText;
            this.hintMin = hintMin;
            this.hintMax = hintMax;
        }
    }
    public void addStatHint(final String hintText, final int hintMin
            , final int hintMax) {

        lstStatHints.add(new StatHint(hintText, hintMin, hintMax));
    }

    public String getName() {
        return name;
    }

    public String getXmlName() {
        return xmlName;
    }

    public void setXmlName(final String xmlName) {
        this.xmlName = xmlName;
    }

    public int[] getRange() {
        return range;
    }

    public ArrayList<StatHint> getStatHints() {
        return lstStatHints;
    }

}
