/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class LaborGroup {
    private String name;
    private String color;
    private int R;
    private int G;
    private int B;

    public LaborGroup(String name, String color, int colorR, int colorG
            , int colorB) {
        this.name = name;
        this.color = color;
        this.R = colorR;
        this.G = colorG;
        this.B = colorB;
    }

    public String getName() {
        return name;
    }

    public int getBlue() {
        return B;
    }

    public int getGreen() {
        return G;
    }

    public int getRed() {
        return R;
    }

}
