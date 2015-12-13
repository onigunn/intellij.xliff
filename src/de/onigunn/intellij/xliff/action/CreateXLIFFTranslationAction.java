package de.onigunn.intellij.xliff.action;

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
import com.intellij.openapi.vfs.VirtualFile;
import de.onigunn.intellij.xliff.XLIFFDocument;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by onigunn on 11.12.15.
 */
public class CreateXLIFFTranslationAction extends AnAction {


    private VirtualFile selectedFile;

    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);

        if (editor == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        String selectedText = editor.getSelectionModel().getSelectedText();
        e.getPresentation().setEnabledAndVisible(selectedText != null);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final String selectedText = editor.getSelectionModel().getSelectedText();

        final String unitId = Messages.showInputDialog(project, "Please enter your translation key:", "Translation Key", Messages.getQuestionIcon());
        selectedFile = openFileChooserDialog(project);

        if (selectedFile != null) {

            try {
                updateTranslationDocument(unitId, selectedText);
                replaceSelectedTextWithViewHelper(unitId, project, editor);
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
}
