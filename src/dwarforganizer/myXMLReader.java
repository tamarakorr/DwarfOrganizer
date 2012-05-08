/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Tamara Orr
 * See MIT license in license.txt
 */
public class myXMLReader {
    
    private Document mDoc;
    
    public myXMLReader(String fileName) {
        this(new File(fileName));
    }
    
    public myXMLReader(File file) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            mDoc = docBuilder.parse(file);
            
        } catch (ParserConfigurationException e) {
            System.err.println(e);
        } catch (SAXException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        } catch (NullPointerException e) {
            System.err.println("Null pointer exception for file = " + file.getName());
            e.printStackTrace();
        }
    }
    public myXMLReader(InputStream is) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            mDoc = docBuilder.parse(is);
            
        } catch (ParserConfigurationException e) {
            System.err.println(e);
        } catch (SAXException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }        
    }
    public Document getDocument() { return mDoc; }
    
}
