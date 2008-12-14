package org.sylfra.idea.plugins.revu.ui.editor;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.model.Issue;

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
  private final Map<Issue, RangeMarker> issues;
  private RevuEditorHandler revuEditorHandler;

  public CustomGutterIconRenderer(RevuEditorHandler revuEditorHandler, Integer lineStart)
  {
    this.revuEditorHandler = revuEditorHandler;
    this.lineStart = lineStart;

    // Use IdentityHashMap to retrieve issues by reference equality instead of content equality
    issues = new IdentityHashMap<Issue, RangeMarker>();
    fullySynchronized = true;
  }

  public Integer getLineStart()
  {
    return lineStart;
  }

  public void checkFullySynchronized()
  {
    boolean tmp = true;
    for (Map.Entry<Issue, RangeMarker> entry : issues.entrySet())
    {
      if (!revuEditorHandler.isSynchronized(entry.getKey(), entry.getValue()))
      {
        tmp = false;
        break;
      }
    }

    fullySynchronized = tmp;
  }

  public void addIssue(@NotNull Issue issue, @NotNull RangeMarker marker)
  {
    issues.put(issue, marker);
    fullySynchronized = ((fullySynchronized) && (revuEditorHandler.isSynchronized(issue, marker)));
  }

  public boolean isEmpty()
  {
    return issues.isEmpty();
  }

  public void removeIssue(Issue issue)
  {
    issues.remove(issue);

    if (!fullySynchronized)
    {
      checkFullySynchronized();
    }
  }

  @NotNull
  @Override
  public Icon getIcon()
  {
    int count = issues.size();

    // Should not have to return an empty icon, but renderer is not removed when unset from RangeHighlighter !?
    if (count == 0)
    {
      return new ImageIcon();
    }

    return RevuIconProvider.getIcon((count == 1)
      ? (fullySynchronized
        ? RevuIconProvider.IconRef.GUTTER_ISSUE : RevuIconProvider.IconRef.GUTTER_ISSUE_DESYNCHRONIZED)
      : (fullySynchronized
        ? RevuIconProvider.IconRef.GUTTER_ISSUES : RevuIconProvider.IconRef.GUTTER_ISSUES_DESYNCHRONIZED));
  }

  @Override
  public String getTooltipText()
  {
    StringBuilder buffer = new StringBuilder("<html><body>");
    for (Iterator<Issue> it = issues.keySet().iterator(); it.hasNext();)
    {
      Issue issue = it.next();
      buffer.append("<b>")
        .append(issue.getHistory().getCreatedBy().getDisplayName())
        .append("</b> - <i>")
        .append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(
          issue.getHistory().getCreatedOn()))
        .append("</i><br/>")
        .append(issue.getSummary());
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
    ActionGroup templateGroup = (ActionGroup) ActionManager.getInstance().getAction("revu.issueGutter.popup");

    DefaultActionGroup result = new DefaultActionGroup();
    if (issues.size() == 1)
    {
      for (final AnAction templateAction : templateGroup.getChildren(null))
      {
        result.add(buildActionProxy(templateAction, issues.keySet().iterator().next()));
      }
    }
    else
    {
      for (AnAction templateAction : templateGroup.getChildren(null))
      {
        DefaultActionGroup actionGroup = new DefaultActionGroup("", true);
        actionGroup.copyFrom(templateAction);
        for (Issue issue : issues.keySet())
        {
          AnAction action = buildActionProxy(templateAction, issue);
          action.getTemplatePresentation().setText(issue.getSummary());
          action.getTemplatePresentation().setIcon(null);
          actionGroup.add(action);
        }
        result.add(actionGroup);
      }
    }

    return result;
  }

  // Build a proxy on actions to inject issue
  // Want to use DataContext, but didn't find any way to inject data into EditorComponent from this renderer
  private AnAction buildActionProxy(final AnAction templateAction, final Issue issue)
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
            if (RevuDataKeys.ISSUE.getName().equals(dataId))
            {
              return issue;
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
