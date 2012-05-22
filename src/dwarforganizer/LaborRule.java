/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.deepclone.DeepCloneable;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class LaborRule implements MyPropertyGetter, DeepCloneable<LaborRule> {
    private String type;
    private String firstLabor;
    private String secondLabor;
    private String comment;

    public LaborRule(final String type, final String firstLabor
            , final String secondLabor, final String comment) {

        super();

        this.type = type;
        this.firstLabor = firstLabor;
        this.secondLabor = secondLabor;
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public String getFirstLabor() {
        return firstLabor;
    }

    public String getSecondLabor() {
        return secondLabor;
    }

    public String getType() {
        return type;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public void setFirstLabor(final String firstLabor) {
        this.firstLabor = firstLabor;
    }

    public void setSecondLabor(final String secondLabor) {
        this.secondLabor = secondLabor;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public Object getProperty(final String propName
            , final boolean humanReadable) {

        final String prop = propName.toLowerCase();

        if (prop.equals("type"))
            return getType();
        else if (prop.equals("comment"))
            return getComment();
        else if (prop.equals("firstlabor"))
            return getFirstLabor();
        else if (prop.equals("secondlabor"))
            return getSecondLabor();
        else {
            final String strReturn = "[LaborRule] Unknown property: "
                    + propName;
            System.err.println(strReturn);
            return strReturn;
        }
    }

    @Override
    public long getKey() {
        return 0; // TODO
    }

    @Override
    public LaborRule deepClone() {
        return new LaborRule(type, firstLabor, secondLabor, comment);
    }

}
