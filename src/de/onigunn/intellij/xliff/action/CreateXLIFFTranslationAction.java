package de.onigunn.intellij.xliff.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.NotificationsManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import de.onigunn.intellij.utils.XmlUtility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * Created by onigunn on 11.12.15.
 */
public class CreateXLIFFTranslationAction extends AnAction {


    private VirtualFile selectedFile;

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (editor == null) {
            return;
        }

        final String selectedText = editor.getSelectionModel().getSelectedText();

        if (selectedText == null) {
            return;
        }

        final String translationKeyId = Messages.showInputDialog(project, "Please enter your translation key:", "Translation Key", Messages.getQuestionIcon());
        selectedFile = openFileChooserDialog(project);

        if (selectedFile != null) {

            try {
                updateTranslationDocument(selectedText, translationKeyId);
                replaceSelectedTextWithViewHelper(project, editor, translationKeyId);
            } catch (SAXException | IOException | ParserConfigurationException e1) {
                e1.printStackTrace();
            }

        }
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

    @NotNull
    private Element createTransUnit(String translationId, String selectedText, org.w3c.dom.Document xmlDocument) {
        Element transUnit = xmlDocument.createElement("trans-unit");
        transUnit.setAttribute("id", translationId);

        Element source = xmlDocument.createElement("source");
        source.appendChild(xmlDocument.createTextNode(selectedText));
        transUnit.appendChild(source);
        return transUnit;
    }

    private void updateTranslationDocument(String selectedText, String translationKeyId) throws ParserConfigurationException, SAXException, IOException {
        final org.w3c.dom.Document xmlDocument = XmlUtility.parseXmlDocument(selectedFile.getInputStream());
        final Node body = xmlDocument.getElementsByTagName("body").item(0);

        Element transUnit = createTransUnit(translationKeyId, selectedText, xmlDocument);
        body.appendChild(transUnit);


        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                Document document = FileDocumentManager.getInstance().getDocument(selectedFile);
                if (document != null) {
                    try {
                        document.setText(XmlUtility.documentToString(xmlDocument));
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }

    private void replaceSelectedTextWithViewHelper(Project project, final Editor editor, final String translationKeyId) {
        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {
                final int selectionStart = editor.getSelectionModel().getSelectionStart();
                final int selectionEnd = editor.getSelectionModel().getSelectionEnd();

                final String replacement = String.format("<f:translate key=\"%s\" />", translationKeyId);
                editor.getDocument().replaceString(selectionStart, selectionEnd, replacement);
            }
        });
    }

}
