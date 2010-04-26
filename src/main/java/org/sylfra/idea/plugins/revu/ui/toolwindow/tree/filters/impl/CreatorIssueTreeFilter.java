package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.impl;

import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.User;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class CreatorIssueTreeFilter extends AbstractUsersIssueTreeFilter
{
  @Override
  protected List<User> retrieveItemsForIssue(@NotNull Issue issue)
  {
    return Arrays.asList(issue.getHistory().getCreatedBy());
  }

  public String getName()
  {
    return RevuBundle.message("browsing.filteringGrouping.creator.text");
  }
}