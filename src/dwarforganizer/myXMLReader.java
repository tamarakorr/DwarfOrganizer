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
public class MyXMLReader {

    private Document mDoc;

    public MyXMLReader(final String fileName) {
        this(new File(fileName));
    }

    public MyXMLReader(final File file) {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            mDoc = docBuilder.parse(file);

        } catch (final ParserConfigurationException e) {
            System.err.println(e);
        } catch (final SAXException e) {
            System.err.println(e);
        } catch (final IOException e) {
            System.err.println(e);
        } catch (final NullPointerException e) {
            System.err.println("Null pointer exception for file = " + file.getName());
            e.printStackTrace(System.out);
        }
    }
    public MyXMLReader(final InputStream is) {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            mDoc = docBuilder.parse(is);

        } catch (final ParserConfigurationException e) {
            System.err.println(e);
        } catch (final SAXException e) {
            System.err.println(e);
        } catch (final IOException e) {
            System.err.println(e);
        }
    }
    public Document getDocument() {
        return mDoc;
    }
}
