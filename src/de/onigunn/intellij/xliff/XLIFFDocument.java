package de.onigunn.intellij.xliff;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.indexing.FileContentImpl;
import com.intellij.xml.util.XmlTagUtil;
import com.intellij.xml.util.XmlUtil;
import de.onigunn.intellij.utils.XmlUtility;
import org.ini4j.InvalidFileFormatException;
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

    private XmlTag bodySubTag;

    public XLIFFDocument(PsiFile file) throws InvalidXliffFileException {
        XmlFile xmlFile = (XmlFile) file;

        XmlUtility.isValidXliffDocument(xmlFile);

        bodySubTag = xmlFile.getRootTag().findFirstSubTag("file").findFirstSubTag("body");
    }

    public void createTranslationUnit(Pair<String, Boolean> userInput, String value) {
        if (!this.bodySubTag.isWritable() && !this.bodySubTag.isValid()) return;


        XmlTag transUnitTag = bodySubTag.createChildTag("trans-unit", bodySubTag.getNamespace(), null, false);
        transUnitTag.setAttribute("id", userInput.getFirst());

        if (userInput.getSecond()) {
            transUnitTag.setAttribute("space", "xml", "preserve");
        }

        XmlTag sourceTag = transUnitTag.createChildTag("source", bodySubTag.getNamespace(), XmlTagUtil.getCDATAQuote(value), false);
        transUnitTag.addSubTag(sourceTag, false);

        bodySubTag.addSubTag(transUnitTag, false);
    }
}
