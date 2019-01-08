package de.onigunn.intellij.xliff;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.fileTypes.FileType;
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
        FileType fileType = psiFile.getFileType();
        String translationKey = dialogResult.getFirst();
        if (fileType.getName().equals(HtmlFileType.INSTANCE.getName())) {
            if (dialogResult.getSecond().getSecond()) {
                return String.format("{f:translate(key: '%s')}", translationKey);
            } else {
                return String.format("<f:translate key=\"%s\" />", translationKey);
            }
        }
        if (fileType.getName().toLowerCase().equals("php")) {
            return String.format("\\TYPO3\\CMS\\Extbase\\Utility\\LocalizationUtility::translate('%s', $this->extensionName)", translationKey);
        }
        return null;
    }
}
