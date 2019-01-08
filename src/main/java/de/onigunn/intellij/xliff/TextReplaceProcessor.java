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
