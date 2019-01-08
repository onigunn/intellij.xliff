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

package de.onigunn.intellij.xliff.ui;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;

import javax.swing.*;
import java.awt.*;

public class CreateTranslationInput extends Messages.InputDialog {

    private JCheckBox preserveSpaceCheckbox;
    private JCheckBox inlineViewHelperCheckbox;

    public CreateTranslationInput(boolean preserveSpace, boolean useInlineViewHelper) {
        super("Translation Key", "Translation Key",  Messages.getQuestionIcon(), "", new NonEmptyInputValidator());

        preserveSpaceCheckbox.setText("Preserve space?");
        preserveSpaceCheckbox.setSelected(preserveSpace);
        preserveSpaceCheckbox.setEnabled(true);

        inlineViewHelperCheckbox.setText("Use Inline View Helper syntax?");
        inlineViewHelperCheckbox.setSelected(useInlineViewHelper);
        inlineViewHelperCheckbox.setEnabled(true);
    }

    @Override
    protected JPanel createMessagePanel() {
        JPanel messagePanel = new JPanel(new BorderLayout());
        if (myMessage != null) {
            JComponent textComponent = createTextComponent();
            messagePanel.add(textComponent, BorderLayout.NORTH);
        }

        myField = createTextFieldComponent();
        messagePanel.add(createScrollableTextComponent(), BorderLayout.CENTER);

        preserveSpaceCheckbox = new JCheckBox();
        messagePanel.add(preserveSpaceCheckbox, BorderLayout.LINE_END);

        inlineViewHelperCheckbox = new JCheckBox();
        messagePanel.add(inlineViewHelperCheckbox, BorderLayout.SOUTH);

        return messagePanel;
    }

    public boolean shouldPreserveSpace() {
        return preserveSpaceCheckbox.isSelected();
    }

    public boolean useInlineViewHelper() {
        return inlineViewHelperCheckbox.isSelected();
    }
}
