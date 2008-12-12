package org.sylfra.idea.plugins.revu.ui.editor;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import java.text.DateFormat;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
* @version $Id$
*/
class CustomGutterIconRenderer extends GutterIconRenderer
{
  private boolean fullySynchronized;
  private final Integer lineStart;
  private final Map<ReviewItem, RangeMarker> reviewItems;
  private RevuEditorHandler revuEditorHandler;

  public CustomGutterIconRenderer(RevuEditorHandler revuEditorHandler, Integer lineStart)
  {
    this.revuEditorHandler = revuEditorHandler;
    this.lineStart = lineStart;

    // Use IdentityHashMap to retrieve reviewItems by reference equality instead of content equality
    reviewItems = new IdentityHashMap<ReviewItem, RangeMarker>();
    fullySynchronized = true;
  }

  public Integer getLineStart()
  {
    return lineStart;
  }

  public void checkFullySynchronized()
  {
    boolean tmp = true;
    for (Map.Entry<ReviewItem, RangeMarker> entry : reviewItems.entrySet())
    {
      if (!revuEditorHandler.isSynchronized(entry.getKey(), entry.getValue()))
      {
        tmp = false;
        break;
      }
    }

    fullySynchronized = tmp;
  }

  public void addItem(@NotNull ReviewItem reviewItem, @NotNull RangeMarker marker)
  {
    reviewItems.put(reviewItem, marker);
    fullySynchronized = ((fullySynchronized) && (revuEditorHandler.isSynchronized(reviewItem, marker)));
  }

  public boolean isEmpty()
  {
    return reviewItems.isEmpty();
  }

  public void removeItem(ReviewItem reviewItem)
  {
    reviewItems.remove(reviewItem);

    if (!fullySynchronized)
    {
      checkFullySynchronized();
    }
  }

  @NotNull
  @Override
  public Icon getIcon()
  {
    int count = reviewItems.size();

    // Should not have to return an empty icon, but renderer is not removed when unset from RangeHighlighter !?
    if (count == 0)
    {
      return new ImageIcon();
    }

    return RevuIconProvider.getIcon((count == 1)
      ? (fullySynchronized
        ? RevuIconProvider.IconRef.GUTTER_REVU_ITEM : RevuIconProvider.IconRef.GUTTER_REVU_ITEM_DESYNCHRONIZED)
      : (fullySynchronized
        ? RevuIconProvider.IconRef.GUTTER_REVU_ITEMS : RevuIconProvider.IconRef.GUTTER_REVU_ITEMS_DESYNCHRONIZED));
  }

  @Override
  public String getTooltipText()
  {
    StringBuilder buffer = new StringBuilder("<html><body>");
    for (Iterator<ReviewItem> it = reviewItems.keySet().iterator(); it.hasNext();)
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
    if (reviewItems.size() == 1)
    {
      for (final AnAction templateAction : templateGroup.getChildren(null))
      {
        result.add(buildActionProxy(templateAction, reviewItems.keySet().iterator().next()));
      }
    }
    else
    {
      for (AnAction templateAction : templateGroup.getChildren(null))
      {
        DefaultActionGroup actionGroup = new DefaultActionGroup("", true);
        actionGroup.copyFrom(templateAction);
        for (ReviewItem reviewItem : reviewItems.keySet())
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
