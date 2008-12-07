package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorFactoryAdapter;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.business.IReviewItemListener;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItemLocationTracker extends EditorFactoryAdapter
  implements ProjectComponent, IReviewItemListener, IReviewListener
{
  private final Project project;
  private final Map<VirtualFile, Map<ReviewItem, RangeMarker>> markers;
  private final Map<Document, ChangeTracker> changeTrackers;

  public ReviewItemLocationTracker(Project project)
  {
    this.project = project;
    markers = new HashMap<VirtualFile, Map<ReviewItem, RangeMarker>>();
    changeTrackers = new HashMap<Document, ChangeTracker>();

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    reviewManager.addReviewListener(this);

    for (Review review : reviewManager.getReviews())
    {
      review.addReviewItemListener(this);
    }
  }

  @Override
  public void editorCreated(EditorFactoryEvent event)
  {
    Editor editor = event.getEditor();

    VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
    if (vFile == null)
    {
      return;
    }

    Map<ReviewItem, RangeMarker> fileMarkers = getFileMarkers(vFile, true);

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    for (Review review : reviewManager.getActiveReviews(vFile))
    {
      for (ReviewItem item : review.getItems(vFile))
      {
        fileMarkers.put(item, createMarker(item));
      }
    }

    changeTrackers.put(editor.getDocument(), new ChangeTracker(vFile, editor.getDocument()));
  }

  @Override
  public void editorReleased(EditorFactoryEvent event)
  {
    Editor editor = event.getEditor();

    VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
    if (vFile == null)
    {
      return;
    }

    Map<ReviewItem, RangeMarker> fileMarkers = getFileMarkers(vFile, false);
    if (fileMarkers != null)
    {
      fileMarkers.clear();
      markers.remove(vFile);
    }

    ChangeTracker changeTracker = changeTrackers.remove(editor.getDocument());
    changeTracker.release();
  }

  private RangeMarker createMarker(ReviewItem reviewItem)
  {
    Editor editor = RevuUtils.getEditor(reviewItem);
    return (editor != null) ? createMarker(reviewItem, editor) : null;
  }

  private void updateReviewItem(@NotNull ReviewItem item, @NotNull RangeMarker marker)
  {
    item.setLineStart(marker.getDocument().getLineNumber(marker.getStartOffset()));
    item.setLineEnd(marker.getDocument().getLineNumber(marker.getEndOffset()));
//    item.setHash(marker.getDocument().getCharsSequence().subSequence(marker.getStartOffset(), marker.getEndOffset()).hashCode());

    item.getReview().fireItemUpdated(item);
  }

  private RangeMarker createMarker(ReviewItem reviewItem, Editor editor)
  {
    return editor.getMarkupModel().getDocument().createRangeMarker(
      editor.getDocument().getLineStartOffset(reviewItem.getLineStart()),
      editor.getDocument().getLineEndOffset(reviewItem.getLineEnd()) + 1);
  }

  private Map<ReviewItem, RangeMarker> getFileMarkers(@NotNull VirtualFile vFile, boolean lazyInit)
  {
    Map<ReviewItem, RangeMarker> result = markers.get(vFile);
    if ((result == null) && (lazyInit))
    {
      result = new HashMap<ReviewItem, RangeMarker>();
      markers.put(vFile, result);
    }

    return result;
  }

  public void itemAdded(ReviewItem item)
  {
    Map<ReviewItem, RangeMarker> fileMarkers = getFileMarkers(item.getFile(), true);
    fileMarkers.put(item, createMarker(item));
  }

  public void itemDeleted(ReviewItem item)
  {
    Map<ReviewItem, RangeMarker> fileMarkers = getFileMarkers(item.getFile(), false);
    if (fileMarkers != null)
    {
      fileMarkers.remove(item);
    }
  }

  public void itemUpdated(ReviewItem item)
  {
  }

  public void reviewChanged(Review review)
  {
  }

  public void reviewAdded(Review review)
  {
    review.addReviewItemListener(this);
    for (ReviewItem item : review.getItems())
    {
      Map<ReviewItem, RangeMarker> fileMarkers = getFileMarkers(item.getFile(), true);
      fileMarkers.put(item, createMarker(item));
    }
  }

  public void reviewDeleted(Review review)
  {
    review.removeReviewItemListener(this);
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
    return RevuPlugin.PLUGIN_NAME + ".ReviewItemLocationTracker";
  }

  public void initComponent()
  {
    EditorFactory.getInstance().addEditorFactoryListener(this);
  }

  public void disposeComponent()
  {
    EditorFactory.getInstance().removeEditorFactoryListener(this);
  }

  public void updateItems(VirtualFile vFile)
  {
    Map<ReviewItem, RangeMarker> fileMarkers = getFileMarkers(vFile, false);
    if (fileMarkers != null)
    {
      Set<Review> reviews = new HashSet<Review>();
      for (Map.Entry<ReviewItem, RangeMarker> entry : fileMarkers.entrySet())
      {
        updateReviewItem(entry.getKey(), entry.getValue());
        reviews.add(entry.getKey().getReview());
      }

      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      for (Review review : reviews)
      {
        reviewManager.save(review);
      }
    }
  }

  private class ChangeTracker implements DocumentListener
  {
    private final VirtualFile vFile;
    private final Document document;

    public ChangeTracker(VirtualFile vFile, Document document)
    {
      this.vFile = vFile;
      this.document = document;
      document.addDocumentListener(this);
    }

    public void beforeDocumentChange(DocumentEvent event)
    {
        int lineStart = document.getLineNumber(event.getOffset());
        int lineEnd = document.getLineNumber(event.getOffset() + event.getOldLength());
//        if (!StringUtil.endsWithChar(event.getOldFragment(), '\n'))

        updateItems(lineStart, lineEnd);
    }

    public void documentChanged(DocumentEvent event)
    {
    }

    private void updateItems(int lineStart, int lineEnd)
    {
      Map<ReviewItem, RangeMarker> fileMarkers = getFileMarkers(vFile, false);
      if (fileMarkers != null)
      {
        for (Map.Entry<ReviewItem, RangeMarker> entry : fileMarkers.entrySet())
        {
          ReviewItem item = entry.getKey();
          RangeMarker rangeMarker = entry.getValue();
          if ((lineStart <= item.getLineStart()) && (lineEnd >= item.getLineStart()))
          {
            updateReviewItem(item, rangeMarker);
          }
        }
      }
    }

    void release()
    {
      document.removeDocumentListener(this);
    }
  }
}