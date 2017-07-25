package de.onigunn.intellij.xliff.editor;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import de.onigunn.intellij.xliff.InvalidXliffFileException;
import de.onigunn.intellij.xliff.XLIFFDocument;
import org.jetbrains.annotations.NotNull;

public class XliffEditorProvider extends FileTypeFactory implements FileEditorProvider{
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        if (!file.isValid()) return false;
        final FileType type = file.getFileType();
        if (type != StdFileTypes.XML) return false;

        return ReadAction.compute(() -> {
            if (project.isDisposed()) return Boolean.FALSE;
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            XLIFFDocument xliffDocument = new XLIFFDocument(psiFile);
            return xliffDocument.isValid();
        });
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile == null) {
            throw new IllegalArgumentException("psifile cannot be null");
        }
        return new XliffEditor(new XLIFFDocument(psiFile), psiFile.getProject());
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return "Xliff";
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }

    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {

    }
}
