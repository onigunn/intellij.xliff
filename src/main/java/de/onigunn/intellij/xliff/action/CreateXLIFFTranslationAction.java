package de.onigunn.intellij.xliff.action;

import com.intellij.codeInsight.actions.AbstractLayoutCodeProcessor;
import com.intellij.codeInsight.actions.RearrangeCodeProcessor;
import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDocumentManager;
import de.onigunn.intellij.xliff.InvalidXliffFileException;
import de.onigunn.intellij.xliff.XLIFFDocument;
import de.onigunn.intellij.xliff.ui.CreateTranslationInput;

import java.io.IOException;

/**
 * Created by onigunn on 11.12.15.
 */
public class CreateXLIFFTranslationAction extends AbstractXLIFFAction {

    protected boolean preserveSpaces = false;

    private void updateTranslationDocument(final Pair<String, Pair<Boolean, Boolean>> unitId, final String unitValue) throws IOException, InvalidXliffFileException {
        final XLIFFDocument xliffDocument = new XLIFFDocument(selectedFile);
        WriteCommandAction.runWriteCommandAction(selectedFile.getProject(), new Runnable() {
            @Override
            public void run() {
                Project project = selectedFile.getProject();
                xliffDocument.createTranslationUnit(unitId, unitValue);
                reformatDocument(project);
                saveDocument(project);
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
                final Document editorDocument = editor.getDocument();

                editorDocument.replaceString(selectionStart, selectionEnd, replacement);
                FileDocumentManager.getInstance().saveDocument(editorDocument);
            }
        });
    }

    @Override
    protected void doAction(AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final String selectedText = editor.getSelectionModel().getSelectedText();
        final Project project = e.getProject();

        Pair<String, Pair<Boolean, Boolean>> inputDialog = showInputDialog();
        String translationKeyId = inputDialog.getFirst();
        preserveSpaces = inputDialog.getSecond().getFirst();
        if (selectedFile != null) {
            try {
                updateTranslationDocument(inputDialog, selectedText);
                replaceSelectedTextWithViewHelper(translationKeyId, project, editor);
            } catch (IOException | InvalidXliffFileException e1) {
                e1.printStackTrace();
                myNotificationGroup.createNotification(e1.getMessage(), NotificationType.ERROR)
                        .notify(project);
            }

        }
    }

    private void reformatDocument(Project project) {
        PsiDocumentManager.getInstance(project).commitAllDocuments();
        AbstractLayoutCodeProcessor processor = new ReformatCodeProcessor(project, selectedFile, null, false);
        processor = new RearrangeCodeProcessor(processor);
        processor.run();
    }

    private void saveDocument(Project project) {
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(selectedFile);

        // make sure all changes are applied to the document before save
        psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);

        FileDocumentManager.getInstance().saveDocument(document);
    }

    private Pair<String, Pair<Boolean, Boolean>> showInputDialog() {
        CreateTranslationInput dialog = new CreateTranslationInput(preserveSpaces);
        dialog.show();
        return Pair.create(dialog.getInputString(), Pair.create(dialog.shouldPreserveSpace(), dialog.useInlineViewHelper()));
    }

}
