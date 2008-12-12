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
import org.sylfra.idea.plugins.revu.business.IReviewItemListener;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuEditorHandler implements ProjectComponent
{
  private final Project project;
  private Map<VirtualFile, DocumentChangeTracker> changeTrackers;
  private final Map<Editor, Map<Integer, CustomGutterIconRenderer>> renderers;
  private final Map<Editor, Map<ReviewItem, RangeHighlighter>> highlighters;
  private final IReviewItemListener reviewItemListener;
  private final IReviewListener reviewListener;
  private final EditorFactoryListener editorFactoryListener;

  public RevuEditorHandler(Project project)
  {
    this.project = project;

    highlighters = new HashMap<Editor, Map<ReviewItem, RangeHighlighter>>();
    renderers = new HashMap<Editor, Map<Integer, CustomGutterIconRenderer>>();
    changeTrackers = new HashMap<VirtualFile, DocumentChangeTracker>();

    // Listeners
    reviewItemListener = new CustomReviewItemListener();
    reviewListener = new CustomReviewListener();
    editorFactoryListener = new CustomEditorFactoryListener();
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
    return RevuPlugin.PLUGIN_NAME + ".RevuEditorHandler";
  }

  public void initComponent()
  {
    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    for (Review review : reviewManager.getReviews())
    {
      review.addReviewItemListener(reviewItemListener);
    }

    reviewManager.addReviewListener(reviewListener);

    EditorFactory.getInstance().addEditorFactoryListener(editorFactoryListener);
  }

  public void disposeComponent()
  {
    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    for (Review review : reviewManager.getReviews())
    {
      review.removeReviewItemListener(reviewItemListener);
    }

    reviewManager.removeReviewListener(reviewListener);

    EditorFactory.getInstance().removeEditorFactoryListener(editorFactoryListener);
  }
//
//  @Nullable
//  Map<ReviewItem, RangeHighlighter> getAllMarkers(@NotNull VirtualFile vFile, boolean lazyInit)
//  {
//    DocumentChangeTracker documentChangeTracker = changeTrackers.get(vFile);
//    if (documentChangeTracker == null)
//    {
//      return null;
//    }
//
//    Map<ReviewItem, RangeHighlighter> result = new IdentityHashMap<ReviewItem, RangeHighlighter>();
//    for (Editor editor : documentChangeTracker.getEditors())
//    {
//      result = markers.get(editor);
//      if ((result == null) && (lazyInit))
//      {
//        result = new IdentityHashMap<ReviewItem, RangeHighlighter>();
//        markers.put(editor, result);
//      }
//    }
//
//    return result;
//  }

  @Nullable
  private RangeMarker findMarker(@NotNull ReviewItem reviewItem)
  {
    DocumentChangeTracker documentChangeTracker = changeTrackers.get(reviewItem.getFile());
    if (documentChangeTracker == null)
    {
      return null;
    }

    return documentChangeTracker.getMarker(reviewItem);
  }

  private RangeMarker addMarker(@Nullable Editor editor, @NotNull ReviewItem reviewItem, boolean orphanMarker)
  {
    if (!reviewItem.getReview().isActive())
    {
      return null;
    }

    DocumentChangeTracker documentChangeTracker = changeTrackers.get(reviewItem.getFile());
    if (documentChangeTracker == null)
    {
      return null;
    }

    RangeMarker marker = documentChangeTracker.addMarker(reviewItem, orphanMarker);
    if (editor == null)
    {
      for (Editor editor2 : documentChangeTracker.getEditors())
      {
        installHightlighters(editor2, reviewItem, marker);
      }
    }
    else
    {
      installHightlighters(editor, reviewItem, marker);
    }

    return marker;
  }

  private void installHightlighters(@NotNull Editor editor, @NotNull ReviewItem reviewItem, @NotNull RangeMarker marker)
  {
    Map<Integer, CustomGutterIconRenderer> editorRenderers = renderers.get(editor);
    if (editorRenderers == null)
    {
      editorRenderers = new HashMap<Integer, CustomGutterIconRenderer>();
      renderers.put(editor, editorRenderers);
    }

    // Gutter renderer, only one renderer for same line start
    CustomGutterIconRenderer renderer = editorRenderers.get(reviewItem.getLineStart());
    if (renderer == null)
    {
      renderer = new CustomGutterIconRenderer(this, reviewItem.getLineStart());
      editorRenderers.put(reviewItem.getLineStart(), renderer);
    }

    Map<ReviewItem, RangeHighlighter> editorHighlighters = highlighters.get(editor);
    if (editorHighlighters == null)
    {
      editorHighlighters = new IdentityHashMap<ReviewItem, RangeHighlighter>();
      highlighters.put(editor, editorHighlighters);
    }

    RangeHighlighter highlighter =
      editor.getMarkupModel().addRangeHighlighter(
        marker.getStartOffset(),
        marker.getEndOffset(),
        HighlighterLayer.FIRST - 1,
        null,
        HighlighterTargetArea.LINES_IN_RANGE);
    editorHighlighters.put(reviewItem, highlighter);
    renderer.addItem(reviewItem, highlighter);

    highlighter.setGutterIconRenderer(renderer);
  }

  private void removeMarker(ReviewItem reviewItem)
  {
    DocumentChangeTracker documentChangeTracker = changeTrackers.get(reviewItem.getFile());
    if (documentChangeTracker == null)
    {
      return;
    }

    documentChangeTracker.removeMarker(reviewItem);
    for (Editor editor : documentChangeTracker.getEditors())
    {
      Map<ReviewItem, RangeHighlighter> editorHighlighters = highlighters.get(editor);
      if (editorHighlighters != null)
      {
        RangeHighlighter highlighter = editorHighlighters.remove(reviewItem);

        if (highlighter != null)
        {
          editor.getMarkupModel().removeHighlighter(highlighter);
          CustomGutterIconRenderer renderer = (CustomGutterIconRenderer) highlighter.getGutterIconRenderer();
          if (renderer != null)
          {
            renderer.removeItem(reviewItem);
            if (renderer.isEmpty())
            {
              Map<Integer, CustomGutterIconRenderer> editorRenderers = renderers.get(editor);
              editorRenderers.remove(renderer.getLineStart());
            }
          }
        }
      }
    }
  }


  public boolean isSynchronized(@NotNull ReviewItem reviewItem, boolean checkEvenIfEditorNotAvailable)
  {
    RangeMarker marker = findMarker(reviewItem);
    if ((marker == null) && (checkEvenIfEditorNotAvailable))
    {
      Document document = RevuUtils.getDocument(project, reviewItem);
      if (document != null)
      {
        marker = document.createRangeMarker(document.getLineStartOffset(reviewItem.getLineStart()),
          document.getLineEndOffset(reviewItem.getLineEnd()));
      }
    }

    return isSynchronized(reviewItem, marker);
  }

  public boolean isSynchronized(@NotNull ReviewItem reviewItem, @Nullable RangeMarker marker)
  {
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

    return (reviewItem.getHash() == fragment.toString().hashCode());
  }

  public int buildNewHash(ReviewItem reviewItem)
  {
    RangeMarker marker = findMarker(reviewItem);
    if (marker == null)
    {
      return 0;
    }

    CharSequence fragment = marker.getDocument().getCharsSequence()
      .subSequence(marker.getStartOffset(), marker.getEndOffset());

    return fragment.toString().hashCode();
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
        documentChangeTracker = new DocumentChangeTracker(RevuEditorHandler.this, vFile, editor.getDocument());
        changeTrackers.put(vFile, documentChangeTracker);
      }
      documentChangeTracker.getEditors().add(editor);

      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      for (Review review : reviewManager.getActiveReviews(vFile))
      {
        if (review.isActive())
        {
          for (ReviewItem item : review.getItems(vFile))
          {
            addMarker(editor, item, false);
          }
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

  private class CustomReviewItemListener implements IReviewItemListener
  {
    public void itemAdded(ReviewItem item)
    {
      addMarker(null, item, false);
    }

    public void itemDeleted(ReviewItem item)
    {
      removeMarker(item);
    }

    public void itemUpdated(ReviewItem item)
    {
      DocumentChangeTracker documentChangeTracker = changeTrackers.get(item.getFile());
      if (documentChangeTracker == null)
      {
        return;
      }

      RangeMarker marker = documentChangeTracker.getMarker(item);
      if (marker == null)
      {
        return;
      }

      boolean invalidMarker = !marker.isValid();
      if (invalidMarker || (documentChangeTracker.isOrphanMarker(marker)))
      {
        removeMarker(item);
        addMarker(null, item, invalidMarker);
      }
      else
      {
        for (Editor editor : documentChangeTracker.getEditors())
        {
          Map<Integer, CustomGutterIconRenderer> editorRenderers = renderers.get(editor);
          for (CustomGutterIconRenderer renderer : editorRenderers.values())
          {
            renderer.checkFullySynchronized();
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
      review.addReviewItemListener(reviewItemListener);

      for (ReviewItem item : review.getItems())
      {
        addMarker(null, item, false);
      }
    }

    public void reviewDeleted(Review review)
    {
      for (ReviewItem item : review.getItems())
      {
        removeMarker(item);
      }
      review.removeReviewItemListener(reviewItemListener);
    }
  }
}