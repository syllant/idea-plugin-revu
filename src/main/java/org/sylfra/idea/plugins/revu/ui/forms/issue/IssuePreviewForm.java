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

  public void internalValidateInput(@Nullable Issue data)
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

    final VirtualFile file = (data == null) ? null : data.getFile();
    boolean currentIdModified = (data != null) && (file != null) && RevuVcsUtils.fileIsModifiedFromVcs(project, file);
    String currentRev = RevuVcsUtils.formatRevision((file == null)
        ? null : RevuVcsUtils.getVcsRevisionNumber(project, file), currentIdModified);
    String initialRev = RevuVcsUtils.formatRevision((data == null) ? null : data.getVcsRev(),
      (data != null) && (data.getLocalRev() != null));

    rbCurrent.setText(RevuBundle.message("issueForm.preview.currentRev.text", currentRev));
    rbInitial.setText(RevuBundle.message("issueForm.preview.initialRev.text", initialRev));

    boolean underVcs = (data != null) && (RevuVcsUtils.isUnderVcs(project, file));
    if (underVcs)
    {
      rbInitial.setEnabled(true);
    }
    else
    {
      rbInitial.setEnabled(false);
      rbCurrent.setSelected(true);
    }

    if (file != null)
    {
      fetchAndLoad();
    }
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
  protected void internalUpdateWriteAccess(Issue data, @Nullable User user)
  {
  }
}
