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

import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by onigunn on 13.12.15.
 */
public class XLIFFDocument {

    private XmlTag bodySubTag;

    public XLIFFDocument(PsiFile file) throws InvalidXliffFileException {
        XmlFile xmlFile = (XmlFile) file;
        isValidXliffDocument(xmlFile);
        bodySubTag = xmlFile.getRootTag().findFirstSubTag("file").findFirstSubTag("body");
    }

    public void createTranslationUnit(Pair<String, Pair<Boolean, Boolean>> userInput, String value) {
        if (!this.bodySubTag.isWritable() && !this.bodySubTag.isValid()) return;

        String transUnitId = userInput.getFirst();
        XmlTag transUnitTag = findTransUnitByIdAttribute(transUnitId);
        if (transUnitTag == null) {
            transUnitTag = createTransUnitTag(transUnitId, userInput.getSecond().getFirst());
        }
        transUnitTag.findFirstSubTag("source").getValue().setText(value);
    }

    @NotNull
    private XmlTag createTransUnitTag(String transUnitId, Boolean preserveSpace) {
        XmlTag transUnitTag = bodySubTag.createChildTag("trans-unit", bodySubTag.getNamespace(), null, false);
        transUnitTag.setAttribute("id", transUnitId);

        if (preserveSpace) {
            transUnitTag.setAttribute("space", "xml", "preserve");
        }

        XmlTag sourceTag = transUnitTag.createChildTag("source", bodySubTag.getNamespace(), "", false);
        transUnitTag.addSubTag(sourceTag, false);
        return bodySubTag.addSubTag(transUnitTag, false);
    }

    @Nullable
    private XmlTag findTransUnitByIdAttribute(String id) {
        for (XmlTag xmlTag : bodySubTag.getSubTags()) {
            if (xmlTag.getAttribute("id") != null && xmlTag.getAttribute("id").getValue().equals(id)) {
                return xmlTag;
            }
        }
        return null;
    }

    private void isValidXliffDocument(XmlFile file) throws InvalidXliffFileException {
        XmlTag rootTag = file.getRootTag();
        if (rootTag == null) throw new InvalidXliffFileException("xliff");

        XmlTag fileTag = rootTag.findFirstSubTag("file");
        if (fileTag == null) throw new InvalidXliffFileException("file");

        if (fileTag.findFirstSubTag("body") == null) throw new InvalidXliffFileException("body");
    }
}
