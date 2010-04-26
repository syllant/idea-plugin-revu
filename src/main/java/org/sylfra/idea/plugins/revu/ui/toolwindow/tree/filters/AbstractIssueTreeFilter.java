package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters;

import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.toolwindow.RevuToolWindowManager;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractIssueTreeFilter<T> implements IIssueTreeFilter<T>
{
  private List<IIssueTreeFilterListener<T>> listeners;

  protected AbstractIssueTreeFilter()
  {
    listeners = new LinkedList<IIssueTreeFilterListener<T>>();
  }

  public void addListener(IIssueTreeFilterListener<T> listener)
  {
    listeners.add(listener);
  }

  public void removeListener(IIssueTreeFilterListener<T> listener)
  {
    listeners.remove(listener);
  }

  protected void fireValueChanged(@NotNull EventObject eventObject, Object value)
  {
    for (IIssueTreeFilterListener<T> listener : listeners)
    {
      listener.valueChanged(eventObject, value);
    }
  }

  public List<Issue> filter(List<Issue> issues, T filterValue)
  {
    List<Issue> result = new ArrayList<Issue>(issues.size());
    for (Issue issue : issues)
    {
      if (match(issue, filterValue))
      {
        result.add(issue);
      }
    }

    return result;
  }

  protected Review getReview()
  {
    return RevuUtils.getProject().getComponent(RevuToolWindowManager.class).getSelectedReviewBrowsingForm().getReview();
  }

  protected abstract boolean match(Issue issue, T filterValue);
}