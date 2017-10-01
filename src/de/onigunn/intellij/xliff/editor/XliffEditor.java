package de.onigunn.intellij.xliff.editor;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.ide.ui.customization.CustomActionsSchema;
import com.intellij.lang.properties.editor.ChooseSubsequentPropertyValueEditorAction;
import com.intellij.lang.properties.editor.ResourceBundleEditor;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.actions.EditorActionUtil;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseEventArea;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.FocusChangeListener;
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter;
import com.intellij.openapi.fileEditor.DocumentsEditor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.EditorPopupHandler;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import de.onigunn.intellij.xliff.XLIFFDocument;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class XliffEditor extends UserDataHolderBase implements DocumentsEditor {

    public static final Key<ResourceBundleEditor> XLIFF_EDITOR_KEY = Key.create("xliffEditor");
    @NonNls private static final String VALUES               = "values";
    @NonNls private static final String NO_PROPERTY_SELECTED = "noPropertySelected";

    private final ConcurrentHashMap<VirtualFile, EditorEx> editors;
    private final DataProviderPanel dataProviderPanel;
    private final Project project;
    private final XLIFFDocument xliffDocument;
    private JPanel valuesPanel;
    private JPanel structureViewPanel;
    private boolean disposed;

    public XliffEditor(@NotNull XLIFFDocument xliffDocument, Project project) {
        this.project = project;
        this.editors = new ConcurrentHashMap<>();
        this.dataProviderPanel = new DataProviderPanel(createSplitPanel());
        this.xliffDocument = xliffDocument;
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

    private class MyJPanel extends JPanel implements Scrollable{
        private MyJPanel(LayoutManager layout) {
            super(layout);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            Editor editor = editors.values().iterator().next();
            return editor.getLineHeight()*4;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return visibleRect.height;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private void reinitSettings(final EditorEx editor) {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        editor.setColorsScheme(scheme);
        editor.setBorder(BorderFactory.createLineBorder(JBColor.border(), 1));
        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(false);
        settings.setWhitespacesShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setIndentGuidesShown(false);
        settings.setFoldingOutlineShown(false);
        settings.setAdditionalColumnsCount(0);
        settings.setAdditionalLinesCount(0);
        settings.setRightMarginShown(true);
        settings.setRightMargin(60);
        settings.setVirtualSpace(false);
        editor.setHighlighter(new LexerEditorHighlighter(new PropertiesValueHighlighter(), scheme));
        editor.setVerticalScrollbarVisible(true);
        editor.setContextMenuGroupId(null); // disabling default context menu
        editor.addEditorMouseListener(new EditorPopupHandler() {
            @Override
            public void invokePopup(EditorMouseEvent event) {
                if (!event.isConsumed() && event.getArea() == EditorMouseEventArea.EDITING_AREA) {
                    DefaultActionGroup group = new DefaultActionGroup();
                    group.add(CustomActionsSchema.getInstance().getCorrectedAction(IdeActions.GROUP_CUT_COPY_PASTE));
                    group.add(CustomActionsSchema.getInstance().getCorrectedAction(IdeActions.ACTION_EDIT_SOURCE));
                    group.addSeparator();
                    group.add(new AnAction("Propagate Value Across of Resource Bundle") {
                        @Override
                        public void actionPerformed(AnActionEvent e) {
                            final String valueToPropagate = editor.getDocument().getText();
                            final String currentSelectedProperty = getSelectedPropertyName();
                            if (currentSelectedProperty == null) {
                                return;
                            }
                            ApplicationManager.getApplication().runWriteAction(() -> WriteCommandAction.runWriteCommandAction(myProject, () -> {
                                try {
                                    final PropertiesFile[] propertiesFiles = myResourceBundle.getPropertiesFiles().stream().filter(f -> {
                                        final IProperty property = f.findPropertyByKey(currentSelectedProperty);
                                        return property == null || !valueToPropagate.equals(property.getValue());
                                    }).toArray(PropertiesFile[]::new);
                                    final PsiFile[] filesToPrepare = Arrays.stream(propertiesFiles).map(PropertiesFile::getContainingFile).toArray(PsiFile[]::new);
                                    if (FileModificationService.getInstance().preparePsiElementsForWrite(filesToPrepare)) {
                                        for (PropertiesFile file : propertiesFiles) {
                                            myPropertiesInsertDeleteManager.insertOrUpdateTranslation(currentSelectedProperty, valueToPropagate, file);
                                        }
                                        recreateEditorsPanel();
                                    }
                                }
                                catch (final IncorrectOperationException e1) {
                                    LOG.error(e1);
                                }
                            }));
                        }
                    });
                    EditorPopupHandler handler = EditorActionUtil.createEditorPopupHandler(group);
                    handler.invokePopup(event);
                    event.consume();
                }
            }
        });
    }
    private EditorEx createEditor() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Document document = editorFactory.createDocument("");
        EditorEx editor = (EditorEx)editorFactory.createEditor(document);
        reinitSettings(editor);
        editor.putUserData(XLIFF_EDITOR_KEY, (T) this);
        return editor;
    }

    void recreateEditorsPanel() {
        if (!project.isOpen() || disposed) return;

        valuesPanel.removeAll();
        valuesPanel.setLayout(new CardLayout());

        JPanel valuesPanelComponent = new XliffEditor.MyJPanel(new GridBagLayout());
        valuesPanel.add(new JBScrollPane(valuesPanelComponent){
            @Override
            public void updateUI() {
                super.updateUI();
                getViewport().setBackground(UIUtil.getPanelBackground());
            }
        }, VALUES);
//        valuesPanel.add(myNoPropertySelectedPanel, NO_PROPERTY_SELECTED);

//        final List<PropertiesFile> propertiesFiles = myResourceBundle.getPropertiesFiles();
        final List<XLIFFDocument> xliffDocuments = xliffDocument.getXliffFiles();

        GridBagConstraints gc = new GridBagConstraints(0, 0, 0, 0, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
                JBUI.insets(5), 0, 0);
        releaseAllEditors();
        myTitledPanels.clear();
        int y = 0;
        Editor previousEditor = null;
        Editor firstEditor = null;
        for (final XLIFFDocument xliffFile : xliffDocuments) {
            final EditorEx editor = createEditor();
            final Editor oldEditor = editors.put(xliffFile.getVirtualFile(), editor);
            if (firstEditor == null) {
                firstEditor = editor;
            }
            if (previousEditor != null) {
                editor.putUserData(ChooseSubsequentPropertyValueEditorAction.PREV_EDITOR_KEY, previousEditor);
                previousEditor.putUserData(ChooseSubsequentPropertyValueEditorAction.NEXT_EDITOR_KEY, editor);
            }
            previousEditor = editor;
            if (oldEditor != null) {
                EditorFactory.getInstance().releaseEditor(oldEditor);
            }

            editor.setViewer(!xliffFile.getVirtualFile().isWritable());
            editor.getContentComponent().addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (editor.isViewer()) {
                        editor.setViewer( ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(xliffFile.getVirtualFile()).hasReadonlyFiles());
                    }
                }
            });

            editor.addFocusListener(new FocusChangeListener() {
                @Override
                public void focusGained(final Editor editor) {
                    mySelectedEditor = editor;
                }

                @Override
                public void focusLost(final Editor editor) {
                    if (!editor.isViewer() && xliffFile.getContainingFile().isValid()) {
                        writeEditorPropertyValue(null, editor, xliffFile.getVirtualFile());
                        myVfsListener.flush();
                    }
                }
            });
            gc.gridx = 0;
            gc.gridy = y++;
            gc.gridheight = 1;
            gc.gridwidth = GridBagConstraints.REMAINDER;
            gc.weightx = 1;
            gc.weighty = 1;
            gc.anchor = GridBagConstraints.CENTER;

            String title = xliffFile.getName();
            title += PropertiesUtil.getPresentableLocale(xliffFile.getLocale());
            JComponent comp = new JPanel(new BorderLayout()) {
                @Override
                public Dimension getPreferredSize() {
                    Insets insets = getBorder().getBorderInsets(this);
                    return new Dimension(100,editor.getLineHeight()*4+ insets.top + insets.bottom);
                }
            };
            comp.add(editor.getComponent(), BorderLayout.CENTER);
            comp.setBorder(IdeBorderFactory.createTitledBorder(title, false));
            myTitledPanels.put(xliffFile.getVirtualFile(), (JPanel)comp);

            valuesPanelComponent.add(comp, gc);
        }
        if (previousEditor != null) {
            previousEditor.putUserData(ChooseSubsequentPropertyValueEditorAction.NEXT_EDITOR_KEY, firstEditor);
            firstEditor.putUserData(ChooseSubsequentPropertyValueEditorAction.PREV_EDITOR_KEY, previousEditor);
        }

        gc.gridx = 0;
        gc.gridy = y;
        gc.gridheight = GridBagConstraints.REMAINDER;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        gc.weightx = 10;
        gc.weighty = 1;

        valuesPanelComponent.add(new JPanel(), gc);
//        selectionChanged();
        valuesPanel.repaint();
//        updateEditorsFromProperties(true);
    }
}
