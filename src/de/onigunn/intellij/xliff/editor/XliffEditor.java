package de.onigunn.intellij.xliff.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.DocumentsEditor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.util.containers.ContainerUtil;
import de.onigunn.intellij.xliff.XLIFFDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ConcurrentHashMap;

public class XliffEditor extends UserDataHolderBase implements DocumentsEditor {

    private final ConcurrentHashMap<Object, Object> editors;
    private final DataProviderPanel dataProviderPanel;
    private final Project project;
    private JPanel valuesPanel;
    private JPanel structureViewPanel;

    public XliffEditor(@NotNull XLIFFDocument xliffDocument, Project project) {
        this.project = project;
        this.editors = new ConcurrentHashMap<>();
        this.dataProviderPanel = new DataProviderPanel(createSplitPanel());
    }

    @Override
    public Document[] getDocuments() {
        return new Document[0];
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return dataProviderPanel;
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return structureViewPanel;
    }

    @NotNull
    @Override
    public String getName() {
        return "XLIFF";
    }

    @Override
    public void setState(@NotNull FileEditorState state) {

    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return !project.isDisposed();
    }

    @Override
    public void selectNotify() {

    }

    @Override
    public void deselectNotify() {

    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {

    }

    @Nullable
    @Override
    public BackgroundEditorHighlighter getBackgroundHighlighter() {
        return null;
    }

    @Nullable
    @Override
    public FileEditorLocation getCurrentLocation() {
        return null;
    }

    @Override
    public void dispose() {

    }

    private JPanel createSplitPanel() {
        final JPanel splitPanel = new JPanel();
        valuesPanel = new JPanel();
        structureViewPanel = new JPanel();
        JBSplitter splitter = new OnePixelSplitter(false);
        splitter.setFirstComponent(structureViewPanel);
        splitter.setSecondComponent(valuesPanel);
        splitter.setShowDividerControls(true);
        splitter.setHonorComponentsMinimumSize(true);
        splitter.setAndLoadSplitterProportionKey(getClass() + ".splitter");
        splitPanel.setLayout(new BorderLayout());
        splitPanel.add(splitter, BorderLayout.CENTER);
        return splitPanel;
    }

    private class DataProviderPanel extends JPanel implements DataProvider {
        private DataProviderPanel(final JPanel panel) {
            super(new BorderLayout());
            add(panel, BorderLayout.CENTER);
        }

        @Nullable
        @Override
        public Object getData(String dataId) {
            return null;
        }
    }
}
