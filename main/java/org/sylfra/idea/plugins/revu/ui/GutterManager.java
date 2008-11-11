package org.sylfra.idea.plugins.revu.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryAdapter;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuUtils;
import org.sylfra.idea.plugins.revu.business.IReviewItemListener;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import java.text.DateFormat;
import java.util.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class GutterManager extends EditorFactoryAdapter implements IReviewItemListener, IReviewListener
{
  private final Project project;
  private Map<VirtualFile, Map<Integer, CustomGutterIconRenderer>> renderersByFiles;

  public GutterManager(Project project)
  {
    this.project = project;
    renderersByFiles = new HashMap<VirtualFile, Map<Integer, CustomGutterIconRenderer>>();

    ReviewManager reviewManager = ServiceManager.getService(project, ReviewManager.class);
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

    VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
    List<ReviewItem> items = new ArrayList<ReviewItem>();

    ReviewManager reviewManager = ServiceManager.getService(project, ReviewManager.class);
    for (Review review : reviewManager.getReviews())
    {
      if (review.isActive())
      {
        items.addAll(review.getItems(file));
      }
    }

    for (ReviewItem item : items)
    {
      addGutter(item);
    }
  }

  private void addGutter(ReviewItem reviewItem)
  {
    Editor editor = RevuUtils.getEditor(reviewItem);
    if (editor != null)
    {
      Map<Integer, CustomGutterIconRenderer> renderersByLineStart = renderersByFiles.get(reviewItem.getFile());
      if (renderersByLineStart == null)
      {
        renderersByLineStart = new HashMap<Integer, CustomGutterIconRenderer>();
        renderersByFiles.put(reviewItem.getFile(), renderersByLineStart);
      }

      RangeHighlighter rangeHighlighter =
        editor.getMarkupModel().addRangeHighlighter(
          editor.getDocument().getLineStartOffset(reviewItem.getLineStart() - 1),
          editor.getDocument().getLineEndOffset(reviewItem.getLineEnd() - 1) + 1,
          HighlighterLayer.FIRST - 1,
          null,
          HighlighterTargetArea.LINES_IN_RANGE);

      CustomGutterIconRenderer renderer = renderersByLineStart.get(reviewItem.getLineStart());
      if (renderer == null)
      {
        renderer = new CustomGutterIconRenderer(reviewItem.getLineStart());
        renderersByLineStart.put(reviewItem.getLineStart(), renderer);

        // Only one renderer for same line start
        rangeHighlighter.setGutterIconRenderer(renderer);
      }

      renderer.addItem(reviewItem, rangeHighlighter);
    }
  }

  private void removeGutter(ReviewItem reviewItem)
  {
    Map<Integer, CustomGutterIconRenderer> renderersByLineStart = renderersByFiles.get(reviewItem.getFile());
    if (renderersByLineStart != null)
    {
      CustomGutterIconRenderer renderer = renderersByLineStart.get(reviewItem.getLineStart());
      if (renderer != null)
      {
        renderer.removeItem(reviewItem);
      }
    }
  }

  @Override
  public void editorReleased(EditorFactoryEvent event)
  {
    VirtualFile vFile = FileDocumentManager.getInstance().getFile(event.getEditor().getDocument());
    renderersByFiles.remove(vFile);
  }

  public void itemAdded(ReviewItem item)
  {
    addGutter(item);
  }

  public void itemDeleted(ReviewItem item)
  {
    removeGutter(item);
  }

  public void itemUpdated(ReviewItem item)
  {
  }

  public void reviewAdded(Review review)
  {
    review.addReviewItemListener(this);
  }

  public void reviewDeleted(Review review)
  {
    for (ReviewItem item : review.getItems())
    {
      removeGutter(item);
    }
  }

  private static class CustomGutterIconRenderer extends GutterIconRenderer
  {
    private final Integer lineStart;
    private final Map<ReviewItem, RangeHighlighter> itemsWithRangeHighlighters;

    public CustomGutterIconRenderer(Integer lineStart)
    {
      this.lineStart = lineStart;
      itemsWithRangeHighlighters = new HashMap<ReviewItem, RangeHighlighter>();
    }

    public void addItem(ReviewItem reviewItem, RangeHighlighter rangeHighlighter)
    {
      itemsWithRangeHighlighters.put(reviewItem, rangeHighlighter);
    }

    public void removeItem(ReviewItem reviewItem)
    {
      RangeHighlighter rangeHighlighter = itemsWithRangeHighlighters.remove(reviewItem);
      if (rangeHighlighter != null)
      {
        rangeHighlighter.setGutterIconRenderer(null);
      }
    }

    @NotNull
    @Override
    public Icon getIcon()
    {
      int count = itemsWithRangeHighlighters.size();

      // Should not have to return an empty icon, but renderer is not removed when unset from RangeHighlighter !?
      return (count == 0) ? new ImageIcon()
        : RevuIconProvider.getIcon((count == 1)
          ? RevuIconProvider.IconRef.GUTTER_REVU_ITEM
          : RevuIconProvider.IconRef.GUTTER_REVU_ITEMS);
    }

    @Override
    public String getTooltipText()
    {
      StringBuilder buffer = new StringBuilder("<html><body>");
      for (Iterator<ReviewItem> it = itemsWithRangeHighlighters.keySet().iterator(); it.hasNext();)
      {
        ReviewItem item = it.next();
        buffer.append("<b>")
          .append(item.getHistory().getCreatedBy().getDisplayName())
          .append("</b> - <i>")
          .append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
            new Date(item.getHistory().getCreatedOn())))
          .append("</i><br/>")
          .append(item.getTitle());
        if (it.hasNext())
        {
          buffer.append("<hr/>");
        }
      }

      buffer.append("</body><html");

      return buffer.toString();
    }

    @Override
    public ActionGroup getPopupMenuActions()
    {
      ActionGroup templateGroup = (ActionGroup) ActionManager.getInstance().getAction("revu.reviewItemGutter.popup");

      DefaultActionGroup result = new DefaultActionGroup();
      if (itemsWithRangeHighlighters.size() == 1)
      {
        for (final AnAction templateAction : templateGroup.getChildren(null))
        {
          result.add(buildActionProxy(templateAction, itemsWithRangeHighlighters.keySet().iterator().next()));
        }
      }
      else
      {
        for (AnAction templateAction : templateGroup.getChildren(null))
        {
          DefaultActionGroup actionGroup = new DefaultActionGroup("", true);
          actionGroup.copyFrom(templateAction);
          for (ReviewItem reviewItem : itemsWithRangeHighlighters.keySet())
          {
            AnAction action = buildActionProxy(templateAction, reviewItem);
            action.getTemplatePresentation().setText(reviewItem.getTitle());
            action.getTemplatePresentation().setIcon(null);
            actionGroup.add(action);
          }
          result.add(actionGroup);
        }
      }

      return result;
    }

    // Build a proxy on actions to inject review item
    // Want to use DataContext, but didn't find any way to inject data into EditorComponent from this renderer
    private AnAction buildActionProxy(final AnAction templateAction, final ReviewItem reviewItem)
    {
      AnAction actionProxy = new AnAction()
      {
        @Override
        public void actionPerformed(final AnActionEvent e)
        {
          DataContext dataContextProxy = new DataContext()
          {
            public Object getData(@NonNls String dataId)
            {
              if (RevuDataKeys.REVIEW_ITEM.getName().equals(dataId))
              {
                return reviewItem;
              }
              return e.getDataContext().getData(dataId);
            }
          };
          AnActionEvent eventProxy = new AnActionEvent(e.getInputEvent(), dataContextProxy, e.getPlace(),
            e.getPresentation(), e.getActionManager(), e.getModifiers());
          templateAction.actionPerformed(eventProxy);
        }
      };
      actionProxy.copyFrom(templateAction);

      return actionProxy;
    }

    @Override
    public boolean isNavigateAction()
    {
      return true;
    }

    @Override
    public AnAction getClickAction()
    {
      return new AnAction()
      {
        @Override
        public void actionPerformed(AnActionEvent e)
        {
          Editor editor = e.getData(DataKeys.EDITOR);
          if (editor != null)
          {
            editor.getCaretModel().moveToOffset(editor.getDocument().getLineStartOffset(lineStart - 1));
          }
        }
      };
    }
  }
}
