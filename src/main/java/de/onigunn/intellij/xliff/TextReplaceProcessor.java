package de.onigunn.intellij.xliff;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;

public class TextReplaceProcessor {

    private final Pair<String, Pair<Boolean, Boolean>> dialogResult;
    private final PsiFile psiFile;

    public TextReplaceProcessor(Pair<String, Pair<Boolean, Boolean>> dialogResult, PsiFile psiFile) {
        this.dialogResult = dialogResult;
        this.psiFile = psiFile;
    }

    public String replacement() {
        String translationKey = dialogResult.getFirst();
        if (isHtmlFile()) {
            if (dialogResult.getSecond().getSecond()) {
                return String.format("{f:translate(key: '%s')}", translationKey);
            } else {
                return String.format("<f:translate key=\"%s\" />", translationKey);
            }
        }
        if (isPHPFile()) {
            return String.format("\\TYPO3\\CMS\\Extbase\\Utility\\LocalizationUtility::translate('%s', $this->extensionName)", translationKey);
        }
        return null;
    }

    private boolean isHtmlFile() {
        return psiFile.getFileType().getName().equals(HtmlFileType.INSTANCE.getName());
    }

    private boolean isPHPFile() {
        return psiFile.getFileType().getName().toLowerCase().equals("php");
    }

    public boolean replacementNeedsOffset(SelectionModel selectionModel) {
        if (isPHPFile()) {
            return selectionModel.getSelectedText().startsWith("\"") || selectionModel.getSelectedText().startsWith("'");
        }
        return false;
    }
}
