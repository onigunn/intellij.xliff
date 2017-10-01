package de.onigunn.intellij.xliff;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.file.impl.FileManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by onigunn on 13.12.15.
 */
public class XLIFFDocument {

    final private VirtualFile virtualFile;
    private XmlTag bodySubTag;
    private boolean valid = false;

    public XLIFFDocument(PsiFile file)  {
        XmlFile xmlFile = (XmlFile) file;
        validateDocument(xmlFile);
        this.virtualFile = file.getVirtualFile();
        this.valid = true;
        bodySubTag = xmlFile.getRootTag().findFirstSubTag("file").findFirstSubTag("body");
    }

    public void createTranslationUnit(Pair<String, Boolean> userInput, String value) {
        if (!this.bodySubTag.isWritable() && !this.bodySubTag.isValid()) return;

        String transUnitId = userInput.getFirst();
        XmlTag transUnitTag = findTransUnitByIdAttribute(transUnitId);
        if (transUnitTag == null) {
            transUnitTag = createTransUnitTag(transUnitId, userInput.getSecond());
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
            if (xmlTag.getAttribute("id").getValue().equals(id)) {
                return xmlTag;
            }
        }
        return null;
    }

    private void validateDocument(XmlFile file) {
        XmlTag rootTag = file.getRootTag();
        this.valid = rootTag == null;

        XmlTag fileTag = rootTag.findFirstSubTag("file");
        this.valid = fileTag == null;

        this.valid = fileTag.findFirstSubTag("body") == null;
    }

    public boolean isValid() {
        return valid;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }
}
