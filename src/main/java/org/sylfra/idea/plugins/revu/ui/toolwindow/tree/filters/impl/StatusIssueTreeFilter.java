package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.impl;

import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.IssueStatus;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.AbstractListUiIssueTreeFilter;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class StatusIssueTreeFilter extends AbstractListUiIssueTreeFilter<IssueStatus>
{
  @Override
  protected List<IssueStatus> retrieveItemsForIssue(@NotNull Issue issue)
  {
    return (issue.getStatus() == null) ? Collections.<IssueStatus>emptyList() : Arrays.asList(issue.getStatus());
  }

  @Override
  protected String getListItemText(IssueStatus status)
  {
    return RevuUtils.buildIssueStatusLabel(status);
  }

  public String getName()
  {
    return RevuBundle.message("browsing.filteringGrouping.status.text");
  }
}