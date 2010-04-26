package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.impl;

import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.IssueStatus;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.AbstractIssueTreeGrouper;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class StatusIssueTreeGrouper extends AbstractIssueTreeGrouper<IssueStatus>
{
  @Override
  @NotNull
  protected List<IssueStatus> getGroupObjects(@NotNull Issue issue)
  {
    return Arrays.asList(issue.getStatus());
  }

  @Override
  @NotNull 
  protected String getGroupName(@NotNull IssueStatus status)
  {
    return RevuUtils.buildIssueStatusLabel(status);
  }

  public String getName()
  {
    return RevuBundle.message("browsing.filteringGrouping.status.text");
  }
}