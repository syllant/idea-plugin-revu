package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.impl;

import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.AbstractIssueTreeGrouper;

import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class AssigneeIssueTreeGrouper extends AbstractIssueTreeGrouper<User>
{
  @Override
  @NotNull
  protected List<User> getGroupObjects(@NotNull Issue issue)
  {
    return issue.getAssignees();
  }

  @Override
  @NotNull
  protected String getGroupName(@NotNull User user)
  {
    return user.getDisplayName();
  }

  public String getName()
  {
    return RevuBundle.message("browsing.filteringGrouping.assignee.text");
  }
}