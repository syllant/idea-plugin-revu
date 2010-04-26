package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.impl;

import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.IssuePriority;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.AbstractListUiIssueTreeFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class PriorityIssueTreeFilter extends AbstractListUiIssueTreeFilter<IssuePriority>
{
  @Override
  protected List<IssuePriority> retrieveItemsForIssue(@NotNull Issue issue)
  {
    return (issue.getPriority() == null) ? Collections.<IssuePriority>emptyList() : Arrays.asList(issue.getPriority());
  }

  @Override
  protected String getListItemText(IssuePriority priority)
  {
    return priority.getName();
  }

  public String getName()
  {
    return RevuBundle.message("browsing.filteringGrouping.priority.text");
  }
}