package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.impl;

import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.IssuePriority;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.AbstractIssueTreeGrouper;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class PriorityIssueTreeGrouper extends AbstractIssueTreeGrouper<IssuePriority>
{
  @Override
  @NotNull
  protected List<IssuePriority> getGroupObjects(@NotNull Issue issue)
  {
    return Arrays.asList(issue.getPriority());
  }

  @Override
  @NotNull
  protected String getGroupName(@NotNull IssuePriority priority)
  {
    return priority.getName();
  }

  public String getName()
  {
    return RevuBundle.message("browsing.filteringGrouping.priority.text");
  }
}
