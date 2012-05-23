/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dwarforganizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static final Logger logger = Logger.getLogger(
            MyXMLReader.class.getName());

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
            logger.log(Level.SEVERE, null, e);
        } catch (final SAXException e) {
            logger.log(Level.SEVERE, null, e);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, null, e);
        } catch (final NullPointerException e) {
            logger.log(Level.SEVERE, "Null pointer exception for file = {0}"
                    , file.getName());
        }
    }
    public MyXMLReader(final InputStream is) {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            mDoc = docBuilder.parse(is);

        } catch (final ParserConfigurationException e) {
            logger.log(Level.SEVERE, null, e);
        } catch (final SAXException e) {
            logger.log(Level.SEVERE, null, e);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }
    public Document getDocument() {
        return mDoc;
    }
}
