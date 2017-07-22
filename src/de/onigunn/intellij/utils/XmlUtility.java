package de.onigunn.intellij.utils;

import com.intellij.openapi.application.Application;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import de.onigunn.intellij.xliff.InvalidXliffFileException;
import org.ini4j.InvalidFileFormatException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Created by onigunn on 12.12.15.
 */
public class XmlUtility {

    public static void isValidXliffDocument(XmlFile file) throws InvalidXliffFileException {
        XmlTag rootTag = file.getRootTag();
        if (rootTag == null) throw new InvalidXliffFileException("xliff");

        XmlTag fileTag = rootTag.findFirstSubTag("file");
        if (fileTag == null) throw new InvalidXliffFileException("file");

        if (fileTag.findFirstSubTag("body") == null) throw new InvalidXliffFileException("bodz");
    }
}
