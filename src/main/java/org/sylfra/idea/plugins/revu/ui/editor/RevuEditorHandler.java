package org.sylfra.idea.plugins.revu.ui.editor;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.IIssueListener;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuEditorHandler implements ProjectComponent
{
  private final Project project;
  private final Map<VirtualFile, DocumentChangeTracker> changeTrackers;
  private final Map<Editor, Map<Integer, CustomGutterIconRenderer>> renderers;
  private final Map<Editor, Map<Issue, RangeHighlighter>> highlighters;
  private final IIssueListener issueListener;
  private final IReviewListener reviewListener;
  private final EditorFactoryListener editorFactoryListener;
  private IRevuSettingsListener<RevuWorkspaceSettings> workspaceSettingsListener;

  public RevuEditorHandler(Project project)
  {
    this.project = project;

    highlighters = new HashMap<Editor, Map<Issue, RangeHighlighter>>();
    renderers = new HashMap<Editor, Map<Integer, CustomGutterIconRenderer>>();
    changeTrackers = new HashMap<VirtualFile, DocumentChangeTracker>();

    // Listeners
    issueListener = new CustomIssueListener();
    reviewListener = new CustomReviewListener();
    editorFactoryListener = new CustomEditorFactoryListener();
    workspaceSettingsListener = new CustomWorkspaceSettingsListener();
  }

  public void projectOpened()
  {
  }

  public void projectClosed()
  {
  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + "." + getClass().getSimpleName();
  }

  public void initComponent()
  {
    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    for (Review review : reviewManager.getReviews())
    {
      review.addIssueListener(issueListener);
    }

    reviewManager.addReviewListener(reviewListener);

    EditorFactory.getInstance().addEditorFactoryListener(editorFactoryListener);

    project.getComponent(RevuWorkspaceSettingsComponent.class).addListener(workspaceSettingsListener);
  }

  public void disposeComponent()
  {
    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    for (Review review : reviewManager.getReviews())
    {
      review.removeIssueListener(issueListener);
    }

    reviewManager.removeReviewListener(reviewListener);

    EditorFactory.getInstance().removeEditorFactoryListener(editorFactoryListener);

    project.getComponent(RevuWorkspaceSettingsComponent.class).removeListener(workspaceSettingsListener);
  }

  @Nullable
  private RangeMarker findMarker(@NotNull Issue issue)
  {
    DocumentChangeTracker documentChangeTracker = changeTrackers.get(issue.getFile());
    if (documentChangeTracker == null)
    {
      return null;
    }

    return documentChangeTracker.getMarker(issue);
  }

  private RangeMarker addMarker(@Nullable Editor editor, @NotNull Issue issue, boolean orphanMarker)
  {
    if (!RevuUtils.isActiveForCurrentUser(issue.getReview()))
    {
      return null;
    }

    DocumentChangeTracker documentChangeTracker = changeTrackers.get(issue.getFile());
    if (documentChangeTracker == null)
    {
      return null;
    }

    RangeMarker marker = documentChangeTracker.addMarker(issue, orphanMarker);
    if (editor == null)
    {
      for (Editor editor2 : documentChangeTracker.getEditors())
      {
        installHightlighters(editor2, issue, marker);
      }
    }
    else
    {
      installHightlighters(editor, issue, marker);
    }

    return marker;
  }

  private void installHightlighters(@NotNull Editor editor, @NotNull Issue issue, @NotNull RangeMarker marker)
  {
    Map<Integer, CustomGutterIconRenderer> editorRenderers = renderers.get(editor);
    if (editorRenderers == null)
    {
      editorRenderers = new HashMap<Integer, CustomGutterIconRenderer>();
      renderers.put(editor, editorRenderers);
    }

    Map<Issue, RangeHighlighter> editorHighlighters = highlighters.get(editor);
    if (editorHighlighters == null)
    {
      editorHighlighters = new IdentityHashMap<Issue, RangeHighlighter>();
      highlighters.put(editor, editorHighlighters);
    }

    RangeHighlighter highlighter =
      editor.getMarkupModel().addRangeHighlighter(
        marker.getStartOffset(),
        marker.getEndOffset(),
        HighlighterLayer.FIRST - 1,
        null,
        HighlighterTargetArea.LINES_IN_RANGE);
    editorHighlighters.put(issue, highlighter);

    // Issue on while file are displayed on first line
    int lineStart = issue.getLineStart();
    if (lineStart == -1)
    {
      lineStart = 0;
    }

    // Gutter renderer, only one renderer for same line start
    CustomGutterIconRenderer renderer = editorRenderers.get(lineStart);
    if (renderer == null)
    {
      renderer = new CustomGutterIconRenderer(this, lineStart);
      editorRenderers.put(lineStart, renderer);

      // Only set gutter icon for first highligther with same line start
      highlighter.setGutterIconRenderer(renderer);
    }

    renderer.addIssue(issue, highlighter);
  }

  private void removeMarker(Issue issue)
  {
    DocumentChangeTracker documentChangeTracker = changeTrackers.get(issue.getFile());
    if (documentChangeTracker == null)
    {
      return;
    }

    documentChangeTracker.removeMarker(issue);
    for (Editor editor : documentChangeTracker.getEditors())
    {
      Map<Issue, RangeHighlighter> editorHighlighters = highlighters.get(editor);
      if (editorHighlighters != null)
      {
        RangeHighlighter highlighter = editorHighlighters.remove(issue);

        if (highlighter != null)
        {
          editor.getMarkupModel().removeHighlighter(highlighter);

          Map<Integer, CustomGutterIconRenderer> editorRenderers = renderers.get(editor);
          int lineStart = (issue.getLineStart() == -1) ? 0 : issue.getLineStart();
          CustomGutterIconRenderer renderer = editorRenderers.get(lineStart);
          if (renderer != null)
          {
            renderer.removeIssue(issue);
            if (renderer.isEmpty())
            {
              editorRenderers.remove(renderer.getLineStart());
            }
            else if (highlighter.getGutterIconRenderer() != null)
            {
              // Reaffecct share gutter icon to first remaining highlighter
              RangeHighlighter marker = (RangeHighlighter) renderer.getIssues().values().iterator().next();
              marker.setGutterIconRenderer(renderer);
            }
          }
        }
      }
    }
  }

  public boolean isSynchronized(@NotNull Issue issue, boolean checkEvenIfEditorNotAvailable)
  {
    if (issue.getLineStart() == -1)
    {
      return true;
    }

    RangeMarker marker = findMarker(issue);
    if ((marker == null) && (checkEvenIfEditorNotAvailable))
    {
      Document document = RevuUtils.getDocument(project, issue);
      if (document != null)
      {
        marker = document.createRangeMarker(document.getLineStartOffset(issue.getLineStart()),
          document.getLineEndOffset(issue.getLineEnd()));
      }
    }

    return isSynchronized(issue, marker);
  }

  public boolean isSynchronized(@NotNull Issue issue, @Nullable RangeMarker marker)
  {
    if (issue.getLineStart() == -1)
    {
      return true;
    }

    if (marker == null)
    {
      return true;
    }

    if (!marker.isValid())
    {
      return false;
    }

    CharSequence fragment = marker.getDocument().getCharsSequence()
      .subSequence(marker.getStartOffset(), marker.getEndOffset());

    return (issue.getHash() == fragment.toString().hashCode());
  }

  public int buildNewHash(Issue issue)
  {
    RangeMarker marker = findMarker(issue);
    if (marker == null)
    {
      return 0;
    }

    CharSequence fragment = marker.getDocument().getCharsSequence()
      .subSequence(marker.getStartOffset(), marker.getEndOffset());

    return fragment.toString().hashCode();
  }

  private boolean hasLocationChanged(Issue issue, RangeMarker marker)
  {
    Document document = marker.getDocument();

    int lineStart = (issue.getLineStart() == -1) ? 0 : issue.getLineStart();
    int lineEnd = (issue.getLineEnd() == -1) ? 0 : issue.getLineEnd();

    return (marker.getStartOffset() != document.getLineStartOffset(lineStart)) 
      || (marker.getEndOffset() != document.getLineEndOffset(lineEnd));
  }

  private class CustomEditorFactoryListener implements EditorFactoryListener
  {
    public void editorCreated(EditorFactoryEvent event)
    {
      final Editor editor = event.getEditor();

      VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
      if (vFile == null)
      {
        return;
      }

      DocumentChangeTracker documentChangeTracker = changeTrackers.get(vFile);
      if (documentChangeTracker == null)
      {
        documentChangeTracker = new DocumentChangeTracker(editor.getDocument());
        changeTrackers.put(vFile, documentChangeTracker);
      }
      documentChangeTracker.getEditors().add(editor);

      for (Review review : RevuUtils.getActiveReviewsForCurrentUser(project))
      {
        List<Issue> issues = review.getIssues(vFile);

        for (Issue issue : issues)
        {
          addMarker(editor, issue, false);
        }
      }
    }

    public void editorReleased(EditorFactoryEvent event)
    {
      Editor editor = event.getEditor();

      VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
      if (vFile == null)
      {
        return;
      }

      DocumentChangeTracker documentChangeTracker = changeTrackers.get(vFile);
      if (documentChangeTracker != null)
      {
        Set<Editor> editors = documentChangeTracker.getEditors();
        editors.remove(editor);
        if (editors.isEmpty())
        {
          changeTrackers.remove(vFile);
          documentChangeTracker.release();
        }
      }

      renderers.remove(editor);
      highlighters.remove(editor);
    }
  }

  private class CustomIssueListener implements IIssueListener
  {
    public void issueAdded(Issue issue)
    {
      addMarker(null, issue, false);
    }

    public void issueDeleted(Issue issue)
    {
      removeMarker(issue);
    }

    public void issueUpdated(Issue issue)
    {
      DocumentChangeTracker documentChangeTracker = changeTrackers.get(issue.getFile());
      if (documentChangeTracker == null)
      {
        return;
      }

      RangeMarker marker = documentChangeTracker.getMarker(issue);
      if (marker == null)
      {
        return;
      }

      boolean invalidMarker = !marker.isValid();
      if (invalidMarker || (documentChangeTracker.isOrphanMarker(marker)) || hasLocationChanged(issue, marker))
      {
        removeMarker(issue);
        addMarker(null, issue, invalidMarker);
      }
      else
      {
        for (Editor editor : documentChangeTracker.getEditors())
        {
          Map<Integer, CustomGutterIconRenderer> editorRenderers = renderers.get(editor);
          if (editorRenderers != null)
          {
            for (CustomGutterIconRenderer renderer : editorRenderers.values())
            {
              renderer.checkFullySynchronized();
            }
          }
        }
      }
    }
  }

  private class CustomReviewListener implements IReviewListener
  {
    public void reviewChanged(Review review)
    {
    }

    public void reviewAdded(Review review)
    {
      review.addIssueListener(issueListener);

      List<Issue> issues = review.getIssues();
      for (Issue issue : issues)
      {
        addMarker(null, issue, false);
      }
    }

    public void reviewDeleted(Review review)
    {
      for (Issue issue : review.getIssues())
      {
        removeMarker(issue);
      }
      review.removeIssueListener(issueListener);
    }
  }

  private static class CustomWorkspaceSettingsListener implements IRevuSettingsListener<RevuWorkspaceSettings>
  {
    public void settingsChanged(RevuWorkspaceSettings oldSettings, RevuWorkspaceSettings newSettings)
    {
    }
  }
}