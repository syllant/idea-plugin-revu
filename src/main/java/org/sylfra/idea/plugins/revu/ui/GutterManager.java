package org.sylfra.idea.plugins.revu.ui;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorFactoryAdapter;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.IReviewItemListener;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class GutterManager extends EditorFactoryAdapter
  implements ProjectComponent, IReviewItemListener, IReviewListener
{
  private final Project project;
  private Map<VirtualFile, Map<Integer, CustomGutterIconRenderer>> renderersByFiles;

  public GutterManager(Project project)
  {
    this.project = project;
    renderersByFiles = new HashMap<VirtualFile, Map<Integer, CustomGutterIconRenderer>>();

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
    final Editor editor = event.getEditor();

    VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
    if (vFile == null)
    {
      return;
    }

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    for (Review review : reviewManager.getActiveReviews(vFile))
    {
      if (review.isActive())
      {
        for (ReviewItem item : review.getItems(vFile))
        {
          addGutter(item);
        }
      }
    }
  }

  private void addGutter(ReviewItem reviewItem)
  {
    if (!reviewItem.getReview().isActive())
    {
      return;
    }

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
          editor.getDocument().getLineStartOffset(reviewItem.getLineStart()),
          editor.getDocument().getLineEndOffset(reviewItem.getLineEnd()),
          HighlighterLayer.FIRST - 1,
          null,
          HighlighterTargetArea.LINES_IN_RANGE);
      rangeHighlighter.setEditorFilter(MarkupEditorFilterFactory.createIsNotDiffFilter());

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
    removeGutter(item);
    addGutter(item);
  }

  public void reviewChanged(Review review)
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

  public void projectOpened()
  {
  }

  public void projectClosed()
  {
  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + ".GutterManager";
  }

  public void initComponent()
  {
    EditorFactory.getInstance().addEditorFactoryListener(this);
  }

  public void disposeComponent()
  {
    EditorFactory.getInstance().removeEditorFactoryListener(this);
  }

  private static class CustomGutterIconRenderer extends GutterIconRenderer
  {
    private boolean desynchronized;
    private final Integer lineStart;
    private final Map<ReviewItem, RangeHighlighter> itemsWithRangeHighlighters;

    public CustomGutterIconRenderer(Integer lineStart)
    {
      this.lineStart = lineStart;

      // Use IdentityHashMap to retrieve reviewItems by reference equality instead of content equality
      itemsWithRangeHighlighters = new IdentityHashMap<ReviewItem, RangeHighlighter>();
      desynchronized = false;
    }

    public void addItem(@NotNull ReviewItem reviewItem, @NotNull RangeHighlighter rangeHighlighter)
    {
      itemsWithRangeHighlighters.put(reviewItem, rangeHighlighter);
      desynchronized = ((desynchronized) || (isDesynchronized(reviewItem, rangeHighlighter)));
    }

    public void removeItem(ReviewItem reviewItem)
    {
      RangeHighlighter rangeHighlighter = itemsWithRangeHighlighters.remove(reviewItem);
      if (rangeHighlighter != null)
      {
        rangeHighlighter.setGutterIconRenderer(null);
      }

      if (desynchronized)
      {
        boolean tmp = false;
        for (Map.Entry<ReviewItem, RangeHighlighter> entry : itemsWithRangeHighlighters.entrySet())
        {
          if (isDesynchronized(entry.getKey(), entry.getValue()))
          {
            tmp = true;
            break;
          }
        }

        desynchronized = tmp;
      }
    }

    private boolean isDesynchronized(@NotNull ReviewItem reviewItem, @NotNull RangeHighlighter rangeHighlighter)
    {
      CharSequence currentFragment =
        rangeHighlighter.getDocument().getCharsSequence().subSequence(rangeHighlighter.getStartOffset(),
          rangeHighlighter.getEndOffset());

      return (reviewItem.getHash() != currentFragment.toString().hashCode());
    }

    @NotNull
    @Override
    public Icon getIcon()
    {
      int count = itemsWithRangeHighlighters.size();

      // Should not have to return an empty icon, but renderer is not removed when unset from RangeHighlighter !?
      if (count == 0)
      {
        return new ImageIcon();
      }

      return RevuIconProvider.getIcon((count == 1)
        ? (desynchronized ? RevuIconProvider.IconRef.GUTTER_REVU_ITEM_DESYNCHRONIZED
          : RevuIconProvider.IconRef.GUTTER_REVU_ITEM)
        : (desynchronized ? RevuIconProvider.IconRef.GUTTER_REVU_ITEMS_DESYNCHRONIZED
          : RevuIconProvider.IconRef.GUTTER_REVU_ITEMS));
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
            item.getHistory().getCreatedOn()))
          .append("</i><br/>")
          .append(item.getSummary());
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
            action.getTemplatePresentation().setText(reviewItem.getSummary());
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
