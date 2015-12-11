package de.onigunn.intellij.typo3;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by onigunn on 11.12.15.
 */
public class CreateXLFTranslationAction extends AnAction {


    private VirtualFile selectedFile;

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (null == editor) return;

        final String selectedText = editor.getSelectionModel().getSelectedText();

        if (null == selectedText) return;

        final String id = Messages.showInputDialog(project, "Please enter your translation-key", "Translation Key", null);
        selectedFile = openFileChooserDialog(project);

        if (null != selectedFile) {

            try {
                final org.w3c.dom.Document xmlDocument = parseXmlDocument(selectedFile);
                final Node body = xmlDocument.getElementsByTagName("body").item(0);

                Element transUnit = createTransUnit(id, selectedText, xmlDocument);
                body.appendChild(transUnit);


                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    public void run() {
                        Document document = FileDocumentManager.getInstance().getDocument(selectedFile);
                        document.setText(xmlDocToString(xmlDocument));
                    }
                });

            } catch (SAXException | IOException | ParserConfigurationException e1) {
                e1.printStackTrace();
            }

        }
    }

    @NotNull
    private Element createTransUnit(String translationId, String selectedText, org.w3c.dom.Document xmlDocument) {
        Element transUnit = xmlDocument.createElement("trans-unit");
        transUnit.setAttribute("id", translationId);

        Element source = xmlDocument.createElement("source");
        source.appendChild(xmlDocument.createTextNode(selectedText));
        transUnit.appendChild(source);
        return transUnit;
    }

    private org.w3c.dom.Document parseXmlDocument(VirtualFile selectedFile) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
        return documentBuilder.parse(selectedFile.getInputStream());
    }

    @Nullable
    private VirtualFile openFileChooserDialog(Project project) {
        FileChooserDialog fileChooserDialog = FileChooserFactory.getInstance().createFileChooser(FileChooserDescriptorFactory.createSingleLocalFileDescriptor(), project, null);
        VirtualFile[] virtualFiles = fileChooserDialog.choose(project, selectedFile != null ? selectedFile : project.getBaseDir());

        if (virtualFiles.length > 0) {
            return virtualFiles[0];
        }
        return null;
    }

    private String xmlDocToString(org.w3c.dom.Document doc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }
}
