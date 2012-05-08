/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import dwarforganizer.deepclone.DeepCloneable;

/**
 *
 * @author Tamara Orr
 */
public class LaborRule implements MyPropertyGetter, DeepCloneable {
    private String type;
    private String firstLabor;
    private String secondLabor;
    private String comment;

    public LaborRule(String type, String firstLabor, String secondLabor
            , String comment) {
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

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setFirstLabor(String firstLabor) {
        this.firstLabor = firstLabor;
    }

    public void setSecondLabor(String secondLabor) {
        this.secondLabor = secondLabor;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Object getProperty(String propName, boolean humanReadable) {
        String prop = propName.toLowerCase();
        
        if (prop.equals("type"))
            return getType();
        else if (prop.equals("comment"))
            return getComment();
        else if (prop.equals("firstlabor"))
            return getFirstLabor();
        else if (prop.equals("secondlabor"))
            return getSecondLabor();
        else {
            String strReturn = "[LaborRule] Unknown property: " + propName;
            System.err.println(strReturn);
            return strReturn;
        }
    }

    @Override
    public long getKey() {
        return 0; // TODO
    }

    @Override
    public Object deepClone() {
        return new LaborRule(type, firstLabor, secondLabor, comment);
    }
    
}
