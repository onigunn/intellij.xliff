package de.onigunn.intellij.xliff.action;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;

/**
 * Created by onigunn on 13.12.15.
 */
public abstract class AbstractXLIFFAction extends AnAction {
    protected PsiFile selectedFile;
    protected final NotificationGroup myNotificationGroup = NotificationGroup.toolWindowGroup("xliff-plugin", ToolWindowId.MESSAGES_WINDOW);

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

        if (selectedFile == null) {
            selectedFile = e.getData(CommonDataKeys.PSI_FILE);
        }

        selectedFile = openFileChooserDialog(project);


        this.doAction(e);
    }

    @Nullable
    private PsiFile openFileChooserDialog(Project project) {
        FileChooserDialog fileChooserDialog = FileChooserFactory.getInstance().createFileChooser(FileChooserDescriptorFactory.createSingleFileDescriptor(), project, null);
        VirtualFile[] virtualFiles = fileChooserDialog.choose(project, selectedFile.getVirtualFile());

        if (virtualFiles.length > 0) {
            PsiFile xliffFile = PsiManager.getInstance(project).findFile(virtualFiles[0]);
            if (xliffFile.isWritable() && xliffFile.isValid()) {
                return xliffFile;
            }
        }
        return null;
    }

    abstract protected void doAction(AnActionEvent e);
}
