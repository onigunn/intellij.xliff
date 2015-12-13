package de.onigunn.intellij.xliff.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

/**
 * Created by onigunn on 13.12.15.
 */
public abstract class AbstractXLIFFAction extends AnAction {
    protected VirtualFile selectedFile;

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
        selectedFile = openFileChooserDialog(project);

        this.doAction(e);
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

    abstract protected void doAction(AnActionEvent e);
}
