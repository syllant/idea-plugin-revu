package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.impl;

import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.IssueTag;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.AbstractListUiIssueTreeFilter;

import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class TagIssueTreeFilter extends AbstractListUiIssueTreeFilter<IssueTag>
{
  @Override
  protected List<IssueTag> retrieveItemsForIssue(@NotNull Issue issue)
  {
    return issue.getTags();
  }

  @Override
  protected String getListItemText(IssueTag tag)
  {
    return tag.getName();
  }

  public String getName()
  {
    return RevuBundle.message("browsing.filteringGrouping.tag.text");
  }
}