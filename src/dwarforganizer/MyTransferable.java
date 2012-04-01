/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Tamara Orr
 */
public class MyTransferable implements Transferable {
    
    protected Object object;
    protected String string;
    protected ArrayList<DataFlavor> aFlavors = new ArrayList<DataFlavor>();
    protected DataFlavor objectFlavor;
    
    protected static List<DataFlavor> stringFlavors = new ArrayList<DataFlavor>(3);

    static {
        try {
            stringFlavors.add(new DataFlavor("text/plain;class=java.lang.String"));
            stringFlavors.add(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
                    + ";class=java.lang.String"));
            stringFlavors.add(DataFlavor.stringFlavor);
        } catch (ClassNotFoundException e) {
            System.err.println("Error initializing MyTransferable: " + e);
        }
    }
    
    public MyTransferable(Object object) {
        this(object, object.toString());
    }
    public MyTransferable(Object object, String string) {
        this.object = object;
        this.string = string;
        
        objectFlavor = new DataFlavor(object.getClass(), object.getClass().getSimpleName());
        aFlavors.add(objectFlavor);
        aFlavors.addAll(stringFlavors);
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] arrayFlavors = new DataFlavor[aFlavors.size()];
        return aFlavors.toArray(arrayFlavors);
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return aFlavors.contains(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        
        if (objectFlavor.equals(flavor)) {
            if (object.getClass().equals(flavor.getRepresentationClass()))
                return object;
        }
        else if (isStringFlavor(flavor)) {
            return string;
        }
        throw new UnsupportedFlavorException(flavor);
    }
    
    protected boolean isStringFlavor(DataFlavor flavor) {
        return stringFlavors.contains(flavor);
    }
}
