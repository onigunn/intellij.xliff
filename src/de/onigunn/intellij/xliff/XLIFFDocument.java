package de.onigunn.intellij.xliff;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.FileContentImpl;
import de.onigunn.intellij.utils.XmlUtility;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by onigunn on 13.12.15.
 */
public class XLIFFDocument {

    private final Node bodyNode;
    private final Document content;

    public XLIFFDocument(VirtualFile virtualFile) {
        try {
            this.content = XmlUtility.parseXmlDocument(virtualFile.getInputStream());
            this.bodyNode = this.content.getElementsByTagName("body").item(0);
        } catch (SAXException | IOException |ParserConfigurationException e) {
            throw new FileContentImpl.IllegalDataException("Given file couldn't parsed");
        }
    }


    public void createTranslationUnit(String id, String value) {
        Element transUnit = this.content.createElement("trans-unit");
        transUnit.setAttribute("id", id);

        Element source = this.content.createElement("source");
        source.appendChild(this.content.createTextNode(value));
        transUnit.appendChild(source);

        this.bodyNode.appendChild(transUnit);
    }

    @Override
    public String toString() {
        return XmlUtility.documentToString(this.content);
    }
}
