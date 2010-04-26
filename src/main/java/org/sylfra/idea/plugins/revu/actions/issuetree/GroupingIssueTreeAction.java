package org.sylfra.idea.plugins.revu.actions.issuetree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.IssueTree;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.IIssueTreeGrouper;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.impl.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class GroupingIssueTreeAction extends AbstractComboIssueTreeAction<IIssueTreeGrouper>
{
  @Override
  protected String getLabel()
  {
    return RevuBundle.message("browsing.groupingBy.text");
  }

  @Override
  @NotNull
  protected String getItemName(@Nullable IIssueTreeGrouper issueTreeGrouper)
  {
    return (issueTreeGrouper == null)
      ? RevuBundle.message("browsing.filteringGrouping.none.text") : issueTreeGrouper.getName();
  }

  @Override
  protected Object[] buildComboItems()
  {
    return new Object[]
      {
        new PriorityIssueTreeGrouper(),
        new StatusIssueTreeGrouper(),
        new TagIssueTreeGrouper(),
        new AssigneeIssueTreeGrouper(),
        new CreatorIssueTreeGrouper()
      };
  }

  @Override
  protected void selectionChanged(@Nullable IssueTree issueTree, @Nullable IIssueTreeGrouper item)
  {
    // There is always one grouper
    assert item != null;

    if (issueTree != null)
    {
      issueTree.getIssueTreeModel().group(item);
    }
  }
}
