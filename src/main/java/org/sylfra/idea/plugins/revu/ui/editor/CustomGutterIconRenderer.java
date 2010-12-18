package org.sylfra.idea.plugins.revu.ui.editor;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.ui.toolwindow.IssueBrowsingPane;
import org.sylfra.idea.plugins.revu.ui.toolwindow.RevuToolWindowManager;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.text.DateFormat;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
* @version $Id$
*/
class CustomGutterIconRenderer extends GutterIconRenderer
{
  private boolean fullySynchronized;
  private final Integer lineStart;
  private final Map<Issue, RangeMarker> issues;
  private final RevuEditorHandler revuEditorHandler;

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

  public Map<Issue, RangeMarker> getIssues()
  {
    return issues;
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

    if (count == 1)
    {
      return RevuUtils.findIcon(issues.keySet().iterator().next(), fullySynchronized);
    }

    return RevuUtils.findIcon(issues.keySet(), fullySynchronized);
  }

  @Override
  public String getTooltipText()
  {
    StringBuilder buffer = new StringBuilder("<html><body>");
    for (Iterator<Issue> it = issues.keySet().iterator(); it.hasNext();)
    {
      Issue issue = it.next();
      buffer
        .append("[")
        .append(issue.getReview().getName())
        .append("]<br/><b>")
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
  // Want to use DataContext, but didn't find proper way to inject data into EditorComponent from this renderer
  private AnAction buildActionProxy(final AnAction templateAction, final Issue issue)
  {
    AnAction actionProxy = new AnAction()
    {
      @Override
      public void actionPerformed(final AnActionEvent e)
      {
        templateAction.actionPerformed(createActionEventProxy(e, issue));
      }

      @Override
      public void update(AnActionEvent e)
      {
        templateAction.update(createActionEventProxy(e, issue));
      }
    };
    actionProxy.copyFrom(templateAction);

    return actionProxy;
  }

  private AnActionEvent createActionEventProxy(final AnActionEvent e, final Issue issue)
  {
    DataContext dataContextProxy = new DataContext()
    {
      public Object getData(@NonNls String dataId)
      {
        if (RevuDataKeys.ISSUE.is(dataId))
        {
          return issue;
        }
        return e.getDataContext().getData(dataId);
      }
    };
    AnActionEvent eventProxy = new AnActionEvent(e.getInputEvent(), dataContextProxy, e.getPlace(),
      e.getPresentation(), e.getActionManager(), e.getModifiers());
    return eventProxy;
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
          editor.getCaretModel().moveToOffset(editor.getDocument().getLineStartOffset(lineStart));
        }

        // Could also be managed through a listener...
        Project project = e.getData(DataKeys.PROJECT);
        if (project != null)
        {
          IssueBrowsingPane browsingPane =
            project.getComponent(RevuToolWindowManager.class).getSelectedReviewBrowsingForm();
          if ((browsingPane != null) && (browsingPane.getContentPane().isShowing()))
          {
            browsingPane.getIssueTree().selectIssue(issues.keySet().iterator().next());
          }
        }
      }
    };
  }

  @Override
  public boolean isNavigateAction()
  {
    return true;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    CustomGutterIconRenderer that = (CustomGutterIconRenderer) o;

    if (fullySynchronized != that.fullySynchronized)
    {
      return false;
    }
    if (!issues.equals(that.issues))
    {
      return false;
    }
    if (!lineStart.equals(that.lineStart))
    {
      return false;
    }
    if (!revuEditorHandler.equals(that.revuEditorHandler))
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = (fullySynchronized ? 1 : 0);
    result = 31 * result + lineStart.hashCode();
    result = 31 * result + issues.hashCode();
    result = 31 * result + revuEditorHandler.hashCode();
    return result;
  }
}
