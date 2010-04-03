package org.sylfra.idea.plugins.revu.ui.forms.issue;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.RevuFriendlyException;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.utils.RevuVcsUtils;
import org.sylfra.idea.plugins.revu.utils.VcsFetcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssuePreviewForm extends AbstractIssueForm
{
  private static final Key<Object> HIGHLIGHT_KEY = Key.create("revu.previewHighlight");

  private final VcsFetcher vcsFetcher;
  private JPanel contentPane;
  private JPanel pnViewer;
  private JLabel lbMessage;
  private JRadioButton rbCurrent;
  private JRadioButton rbInitial;
  private Editor currentEditor;
  private VirtualFile currentFile;

  public IssuePreviewForm(@NotNull Project project)
  {
    super(project);
    lbMessage.setIconTextGap(20);

    ActionListener actionListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        fetchAndLoad();
      }
    };
    rbInitial.addActionListener(actionListener);
    rbCurrent.addActionListener(actionListener);

    vcsFetcher = ApplicationManager.getApplication().getComponent(VcsFetcher.class);
  }

  public JComponent getPreferredFocusedComponent()
  {
    return pnViewer;
  }

  public void internalValidateInput()
  {
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  public void internalUpdateUI(@Nullable Issue data, boolean requestFocus)
  {
    super.internalUpdateUI(data, requestFocus);

    boolean currentIdModified = (data != null) && RevuVcsUtils.fileIsModifiedFromVcs(project, data.getFile());
    String currentRev = RevuVcsUtils.formatRevision((data == null)
      ? null : RevuVcsUtils.getVcsRevisionNumber(project, data.getFile()), currentIdModified);
    String initialRev = RevuVcsUtils.formatRevision((data == null) ? null : data.getVcsRev(),
      (data != null) && (data.getLocalRev() != null));

    rbCurrent.setText(RevuBundle.message("issueForm.preview.currentRev.text", currentRev));
    rbInitial.setText(RevuBundle.message("issueForm.preview.initialRev.text", initialRev));

    boolean underVcs = (data != null) && (RevuVcsUtils.isUnderVcs(project, data.getFile()));
    if (underVcs)
    {
      rbInitial.setEnabled(true);
    }
    else
    {
      rbInitial.setEnabled(false);
      rbCurrent.setSelected(true);
    }

    fetchAndLoad();
  }

  private void showMessage(@NotNull String message, @NotNull MessageType type)
  {
    CardLayout layout = (CardLayout) pnViewer.getLayout();
    layout.show(pnViewer, "label");

    lbMessage.setText(message);
    lbMessage.setIcon(type.getDefaultIcon());
  }

  private void fetchAndLoad()
  {
    showMessage(RevuBundle.message("issueForm.preview.loading.text"), MessageType.INFO);

    if (currentIssue != null)
    {
      if (rbCurrent.isSelected())
      {
        loadEditor(currentIssue.getFile());
      }
      else
      {
        vcsFetcher.fetch(project, currentIssue.getFile(), currentIssue.getVcsRev(),
          new VcsFetcher.IVcsFetchListener()
          {
            public void fetchSucceeded(@NotNull final VirtualFile vFile)
            {
              ApplicationManager.getApplication().runReadAction(new Runnable()
              {
                public void run()
                {
                  ApplicationManager.getApplication().invokeLater(new Runnable()
                  {
                    public void run()
                    {
                      loadEditor(vFile);
                    }
                  });
                }
              });
            }

            public void fetchFailed(@NotNull final RevuFriendlyException e)
            {
              Logger.getInstance(getClass().getName()).warn(e);
              ApplicationManager.getApplication().invokeLater(new Runnable()
              {
                public void run()
                {
                  showMessage(e.getHtmlDetails(), MessageType.ERROR);
                }
              });
            }
          }
        );
      }
    }
  }

  private void loadEditor(@NotNull VirtualFile vFile)
  {
    // Different file, release current editor
    if ((currentEditor != null) && (!vFile.equals(currentFile)))
    {
      pnViewer.remove(currentEditor.getComponent());
      EditorFactory.getInstance().releaseEditor(currentEditor);
      currentEditor = null;
    }

    currentFile = vFile;

    // Build editor
    if (currentEditor == null)
    {
      try
      {
        currentEditor = buildEditor(vFile);
        currentFile = vFile;
      }
      catch (RevuException e)
      {
        Logger.getInstance(getClass().getName()).debug(e);

        currentEditor = null;

        showMessage(RevuBundle.message("issueForm.preview.unavailableError.text", e.getMessage()),
          MessageType.WARNING);
      }
    }

    // Check if editor is really built
    if (currentEditor == null)
    {
      showMessage(RevuBundle.message("issueForm.preview.unavailable.text"), MessageType.INFO);
    }
    else
    {
      pnViewer.add("viewer", currentEditor.getComponent());
      highlight(currentIssue, currentEditor);

      CardLayout layout = (CardLayout) pnViewer.getLayout();
      layout.show(pnViewer, "viewer");
    }
  }

  protected void internalUpdateData(@NotNull Issue issueToUpdate)
  {
  }

  @NotNull
  private Editor buildEditor(@NotNull final VirtualFile vFile) throws RevuException
  {
    PsiFile psiFile = PsiManager.getInstance(project).findFile(vFile);
    if (psiFile == null)
    {
      throw new RevuException(RevuBundle.message("issueForm.preview.error.fileNotFound.text", vFile.getPath()));
    }

    Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);

    final Editor editor = EditorFactory.getInstance().createEditor(document, project, psiFile.getFileType(), true);

    EditorSettings settings = editor.getSettings();
    settings.setLineMarkerAreaShown(false);
    settings.setFoldingOutlineShown(false);
    settings.setAdditionalColumnsCount(0);
    settings.setAdditionalLinesCount(0);
    settings.setVirtualSpace(true);

    return editor;
  }

  private void highlight(@NotNull Issue issue, @NotNull final Editor editor)
  {
    if (issue.getLineStart() == -1)
    {
      return;
    }

    MarkupModel markupModel = editor.getMarkupModel();
    for (RangeHighlighter highlighter : markupModel.getAllHighlighters())
    {
      if (highlighter.getUserData(HIGHLIGHT_KEY) != null)
      {
        markupModel.removeHighlighter(highlighter);
      }
    }
    TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme()
      .getAttributes(EditorColors.SEARCH_RESULT_ATTRIBUTES);

    int startOffset = editor.getDocument().getLineStartOffset(issue.getLineStart());
    int endOffset = editor.getDocument().getLineEndOffset(issue.getLineEnd());
    RangeHighlighter highlighter = markupModel.addRangeHighlighter(
      startOffset,
      endOffset,
      HighlighterLayer.SYNTAX,
      attributes,
      HighlighterTargetArea.LINES_IN_RANGE);
    highlighter.putUserData(HIGHLIGHT_KEY, true);

    editor.getCaretModel().moveToOffset(endOffset);

    editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
  }

  public void dispose()
  {
    if (currentEditor != null)
    {
      EditorFactory.getInstance().releaseEditor(currentEditor);
    }
  }

  public boolean isModified(@NotNull Issue issue)
  {
    return false;
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
  }

  {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
    $$$setupUI$$$();
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$()
  {
    contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout(0, 0));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1, BorderLayout.NORTH);
    rbCurrent = new JRadioButton();
    rbCurrent.setSelected(true);
    this.$$$loadButtonText$$$(rbCurrent,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "issueForm.preview.currentRev.text"));
    panel1.add(rbCurrent, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    rbInitial = new JRadioButton();
    this.$$$loadButtonText$$$(rbInitial,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "issueForm.preview.initialRev.text"));
    panel1.add(rbInitial, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel1.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    pnViewer = new JPanel();
    pnViewer.setLayout(new CardLayout(0, 0));
    contentPane.add(pnViewer, BorderLayout.CENTER);
    lbMessage = new JLabel();
    lbMessage.setHorizontalAlignment(0);
    this.$$$loadLabelText$$$(lbMessage,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "issueForm.preview.unavailable.text"));
    pnViewer.add(lbMessage, "label");
    ButtonGroup buttonGroup;
    buttonGroup = new ButtonGroup();
    buttonGroup.add(rbCurrent);
    buttonGroup.add(rbInitial);
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadLabelText$$$(JLabel component, String text)
  {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++)
    {
      if (text.charAt(i) == '&')
      {
        i++;
        if (i == text.length())
        {
          break;
        }
        if (!haveMnemonic && text.charAt(i) != '&')
        {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic)
    {
      component.setDisplayedMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadButtonText$$$(AbstractButton component, String text)
  {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++)
    {
      if (text.charAt(i) == '&')
      {
        i++;
        if (i == text.length())
        {
          break;
        }
        if (!haveMnemonic && text.charAt(i) != '&')
        {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic)
    {
      component.setMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$()
  {
    return contentPane;
  }
}
