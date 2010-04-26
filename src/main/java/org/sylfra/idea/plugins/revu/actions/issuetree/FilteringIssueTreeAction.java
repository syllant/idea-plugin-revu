package org.sylfra.idea.plugins.revu.actions.issuetree;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.ui.toolwindow.IssueBrowsingPane;
import org.sylfra.idea.plugins.revu.ui.toolwindow.RevuToolWindowManager;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.IssueTree;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.IIssueTreeFilter;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.impl.*;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class FilteringIssueTreeAction extends AbstractComboIssueTreeAction<IIssueTreeFilter>
{
  @Override
  protected String getLabel()
  {
    return RevuBundle.message("browsing.filteringBy.text");
  }

  @Override
  @NotNull
  protected String getItemName(@Nullable IIssueTreeFilter issueTreeFilter)
  {
    return (issueTreeFilter == null)
      ? RevuBundle.message("browsing.filteringGrouping.none.text") : issueTreeFilter.getName();
  }

  @Override
  protected Object[] buildComboItems()
  {
    return new Object[]
      {
        null,
        new PriorityIssueTreeFilter(),
        new StatusIssueTreeFilter(),
        new TagIssueTreeFilter(),
        new AssigneeIssueTreeFilter(),
        new CreatorIssueTreeFilter(),
        new StructureIssueTreeFilter()
      };
  }

  @Override
  protected void selectionChanged(@Nullable IssueTree issueTree, @Nullable IIssueTreeFilter item)
  {
    final Project project = RevuUtils.getProject();
    IssueBrowsingPane browsingPane = project.getComponent(RevuToolWindowManager.class).getSelectedReviewBrowsingForm();

    browsingPane.showFilter(item);
  }
}
