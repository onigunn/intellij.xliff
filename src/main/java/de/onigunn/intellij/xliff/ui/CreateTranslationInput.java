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
