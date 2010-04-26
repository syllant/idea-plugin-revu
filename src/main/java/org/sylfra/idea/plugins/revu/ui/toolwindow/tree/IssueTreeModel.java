package org.sylfra.idea.plugins.revu.ui.toolwindow.tree;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.business.IIssueListener;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.IIssueTreeFilter;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.IIssueTreeGrouper;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.INamedGroup;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueTreeModel extends DefaultTreeModel implements IIssueListener
{
  private final IssueTreeBuilder treeBuilder;
  private Review review;
  private String plainTextFilter;
  private IIssueTreeGrouper<? extends INamedGroup> grouper;
  private IIssueTreeFilter issueTreeFilter;
  private Object filterValue;
  private List<Issue> issues;

  public IssueTreeModel(Project project, @NotNull Review review,
    @NotNull IIssueTreeGrouper<? extends INamedGroup> grouper)
  {
    super(null);

    treeBuilder = new IssueTreeBuilder(project);
    this.grouper = grouper;
    setReview(review);

    rebuild();
  }

  public List<Issue> getIssues()
  {
    return issues;
  }

  public void setReview(@NotNull Review review)
  {
    if (this.review != null)
    {
      this.review.removeIssueListener(this);
    }

    this.review = review;
    review.addIssueListener(this);
  }

  public String getPlainTextFilter()
  {
    return plainTextFilter;
  }

  @Nullable
  public IIssueTreeFilter getIssueTreeFilter()
  {
    return issueTreeFilter;
  }

  public void setIssueTreeFilter(@Nullable IIssueTreeFilter issueTreeFilter)
  {
    this.issueTreeFilter = issueTreeFilter;
  }

  public IIssueTreeGrouper<? extends INamedGroup> getGrouper()
  {
    return grouper;
  }

  public void issueAdded(Issue issue)
  {
    rebuild();
  }

  public void issueDeleted(Issue issue)
  {
    rebuild();
  }

  public void issueUpdated(Issue issue)
  {
    rebuild();
  }

  public int getIssueCount()
  {
    return review.getIssues().size();
  }

  private void rebuild()
  {
    List<Issue> newIssues = review.getIssues();

    if ((plainTextFilter != null) && (plainTextFilter.length() > 0))
    {
      newIssues = applyPlainTextFilter(newIssues);
    }

    if (issueTreeFilter != null)
    {
      newIssues = issueTreeFilter.filter(newIssues, filterValue);
    }

    this.issues = newIssues;
    setRoot(treeBuilder.build(grouper, newIssues));
  }

  private List<Issue> applyPlainTextFilter(List<Issue> issues)
  {
    List<Issue> result = new ArrayList<Issue>(issues.size());
    for (Issue issue : issues)
    {
      if (issue.getSummary().toLowerCase().indexOf(plainTextFilter.toLowerCase()) != -1)
      {
        result.add(issue);
      }
    }

    return result;
  }

  public void group(@NotNull IIssueTreeGrouper<? extends INamedGroup> grouper)
  {
    this.grouper = grouper;
    rebuild();
  }

  public void filter(@Nullable Object filterValue)
  {
    this.filterValue = filterValue;
    rebuild();
  }

  public void filterWithPlainText(@NotNull String plainTextFilter)
  {
    this.plainTextFilter = plainTextFilter;
    rebuild();
  }

  @Nullable
  public TreePath getTreePath(@NotNull Issue issue)
  {
    for (int i=0; i<root.getChildCount(); i++)
    {
      TreeNode group = root.getChildAt(i);
      for (int j=0; j<group.getChildCount(); j++)
      {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) group.getChildAt(j);
        NodeDescriptor nodeDescriptor = (NodeDescriptor) node.getUserObject();
        if (issue.equals(nodeDescriptor.getElement()))
        {
          return new TreePath(new Object[] {group, node});
        }
      }
    }

    return null;
  }
}
