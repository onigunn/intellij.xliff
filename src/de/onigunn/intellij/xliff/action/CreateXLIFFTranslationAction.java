package de.onigunn.intellij.xliff.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import de.onigunn.intellij.xliff.XLIFFDocument;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by onigunn on 11.12.15.
 */
public class CreateXLIFFTranslationAction extends AbstractXLIFFAction {


    private void updateTranslationDocument(String unitId, String unitValue) throws ParserConfigurationException, SAXException, IOException {
        final XLIFFDocument xliffDocument = new XLIFFDocument(selectedFile);
        xliffDocument.createTranslationUnit(unitId, unitValue);

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
                Document document = FileDocumentManager.getInstance().getDocument(selectedFile);
                if (document != null) {
                    document.setText(xliffDocument.toString());
                }

            }
        });
    }

    private void replaceSelectedTextWithViewHelper(final String translationKeyId, final Project project, final Editor editor) {
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

    @Override
    protected void doAction(AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        final String selectedText = editor.getSelectionModel().getSelectedText();
        final String unitId = Messages.showInputDialog(e.getProject(), "Please enter your translation key:", "Translation Key", Messages.getQuestionIcon());

        if (selectedFile != null) {

            try {
                updateTranslationDocument(unitId, selectedText);
                replaceSelectedTextWithViewHelper(unitId, e.getProject(), editor);
                CodeStyleManager.getInstance(e.getProject()).reformat(psiFile);
            } catch (SAXException | IOException | ParserConfigurationException e1) {
                e1.printStackTrace();
            }

        }
    }
}
