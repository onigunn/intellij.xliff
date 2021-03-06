/*
 * Copyright (c) 2019 Onur Güngören <onur@guengoeren.eu>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
import com.intellij.psi.PsiFile;
import de.onigunn.intellij.xliff.InvalidXliffFileException;
import de.onigunn.intellij.xliff.TextReplaceProcessor;
import de.onigunn.intellij.xliff.XLIFFDocument;
import de.onigunn.intellij.xliff.ui.CreateTranslationInput;
import org.jetbrains.annotations.NotNull;

/**
 * Created by onigunn on 11.12.15.
 */
public class CreateXLIFFTranslationAction extends AbstractXLIFFAction {

    protected boolean preserveSpaces = false;
    protected boolean useInlineViewHelper = false;

    private void updateTranslationDocument(final Pair<String, Pair<Boolean, Boolean>> unitId, final String unitValue) throws InvalidXliffFileException {
        final XLIFFDocument xliffDocument = new XLIFFDocument(selectedFile);
        WriteCommandAction.runWriteCommandAction(selectedFile.getProject(), () -> {
            Project project = selectedFile.getProject();
            xliffDocument.createTranslationUnit(unitId, unitValue);
            reformatDocument(project);
            saveDocument(project);
        });
    }

    private void replaceSelectedInput(@NotNull final Editor editor, final Pair<String, Pair<Boolean, Boolean>> inputDialogResult) {
        final Project project = editor.getProject();
        WriteCommandAction.runWriteCommandAction(project, () -> {
            int selectionStart = editor.getSelectionModel().getSelectionStart();
            int selectionEnd = editor.getSelectionModel().getSelectionEnd();

            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
            TextReplaceProcessor replaceProcessor = new TextReplaceProcessor(inputDialogResult, psiFile);
            if (replaceProcessor.replacementNeedsOffset(editor.getSelectionModel())) {
                selectionStart -= 1;
                selectionEnd += 1;
            }
            final String replacement = replaceProcessor.replacement();

            if (replacement != null) {
                final Document editorDocument = editor.getDocument();
                editorDocument.replaceString(selectionStart, selectionEnd, replacement);
                FileDocumentManager.getInstance().saveDocument(editorDocument);
            } else {
                myNotificationGroup.createNotification("File type not supported", NotificationType.ERROR)
                        .notify(project);
            }
        });
    }

    @Override
    protected void doAction(AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        final String selectedText = editor.getSelectionModel().getSelectedText();

        Pair<String, Pair<Boolean, Boolean>> inputDialogResult = showInputDialog();
        preserveSpaces = inputDialogResult.getSecond().getFirst();
        useInlineViewHelper = inputDialogResult.getSecond().getSecond();
        if (selectedFile != null) {
            try {
                updateTranslationDocument(inputDialogResult, selectedText);
                replaceSelectedInput(editor, inputDialogResult);
            } catch (InvalidXliffFileException e1) {
                e1.printStackTrace();
                myNotificationGroup.createNotification(e1.getMessage(), NotificationType.ERROR)
                        .notify(e.getProject());
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

    @NotNull
    private Pair<String, Pair<Boolean, Boolean>> showInputDialog() {
        CreateTranslationInput dialog = new CreateTranslationInput(preserveSpaces, useInlineViewHelper);
        dialog.show();
        return Pair.create(dialog.getInputString(), Pair.create(dialog.shouldPreserveSpace(), dialog.useInlineViewHelper()));
    }

}
