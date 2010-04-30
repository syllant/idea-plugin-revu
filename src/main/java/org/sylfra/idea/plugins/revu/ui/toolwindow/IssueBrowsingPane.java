package org.sylfra.idea.plugins.revu.ui.toolwindow;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.util.containers.Convertor;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.business.IIssueListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.CustomAutoScrollToSourceHandler;
import org.sylfra.idea.plugins.revu.ui.forms.issue.IssuePane;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.IssueTree;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.IssueTreeModel;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.IIssueTreeFilter;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.IIssueTreeFilterListener;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.impl.PriorityIssueTreeGrouper;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.Collection;
import java.util.EventObject;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueBrowsingPane implements Disposable, DataProvider
{
  private JPanel contentPane;
  private final Project project;
  private final Review review;
  private JComponent tbMain;
  private IssuePane issuePane;
  private JSplitPane splitPane;
  private JLabel lbMessage;
  private JComponent fullTextFilterComponent;
  private JLabel lbCount;
  private IssueTree issueTree;
  private JPanel pnIssuePaneContainer;
  private JComponent toolbar;
  private Splitter splitFilter;
  private IIssueListener issueListener;
  private IIssueTreeFilterListener issueTreeFilterListener;

  public IssueBrowsingPane(@NotNull Project project, @Nullable Review review)
  {
    this.project = project;
    this.review = review;

    configureUI();

    installListeners();

    checkRowSelected();
  }

  public Review getReview()
  {
    return review;
  }

  private void createUIComponents()
  {
    issueTree = new IssueTree(project, review, new PriorityIssueTreeGrouper());
    issueTree.setSelectionModel(new DefaultTreeSelectionModel()
    {
      @Override
      public void setSelectionPaths(TreePath[] pPaths)
      {
        if (saveIfModified())
        {
          super.setSelectionPaths(pPaths);
          updateUI(false);
        }
      }
    });
    issueTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    TreeUtil.expandAll(issueTree);

    // Top toolbar
    toolbar = createToolbar("revu.issueBrowsingPane", true).getComponent();

    // Left toolbar
    tbMain = createToolbar("revu.toolWindow", false).getComponent();

    issuePane = new IssuePane(project, issueTree, false);

    new CustomAutoScrollToSourceHandler(project).install(issueTree);

    new TreeSpeedSearch(issueTree, new Convertor<TreePath, String>()
    {
      public String convert(TreePath o)
      {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) o.getLastPathComponent();
        return node.getUserObject().toString();
      }
    });
    fullTextFilterComponent = issueTree.buildFilterComponent();
  }

  private void configureUI()
  {
    // Later this label might display distinct message depending on app settings
    lbMessage.setIcon(Messages.getInformationIcon());
    lbMessage.setIconTextGap(20);

    splitFilter.setFirstComponent(null);
    splitFilter.setSecondComponent(new JScrollPane(issueTree));
  }

  public IssueTree getIssueTree()
  {
    return issueTree;
  }

  @Nullable
  public Review getSelectedReview()
  {
    Issue issue = issueTree.getSelectedIssue();

    return (issue == null) ? null : issue.getReview();
  }

  private void checkRowSelected()
  {
    if ((issueTree.getRowCount() > 0) && (issueTree.getSelectionPath() == null))
    {
      issueTree.setSelectionRow(1);
      updateUI(false);
    }
  }

  private void installListeners()
  {
    issueTreeFilterListener = new IIssueTreeFilterListener()
    {
      public void valueChanged(@NotNull EventObject event, @Nullable Object value)
      {
        IssueTreeModel treeModel = issueTree.getIssueTreeModel();
        treeModel.filter(value);
      }
    };

    // Issues
    issueListener = new IIssueListener()
    {
      public void issueAdded(Issue issue)
      {
        updateMessageCount();
      }

      public void issueDeleted(Issue issue)
      {
        updateMessageCount();
      }

      public void issueUpdated(final Issue issue)
      {
        // Don't waste time to update UI if form is not visible (but will have to update on show)
        if (!contentPane.isVisible())
        {
          return;
        }

        // Compare by identity since item content has changed
        if (issue == issueTree.getSelectedIssue())
        {
          issuePane.updateUI(issue.getReview(), issue, false);
        }
      }
    };

    review.addIssueListener(issueListener);
  }

  public JPanel getContentPane()
  {
    return contentPane;
  }

  public JSplitPane getSplitPane()
  {
    return splitPane;
  }

  public boolean saveIfModified()
  {
    Issue current = issueTree.getSelectedIssue();

    if (current == null)
    {
      return true;
    }

    // Already called in #updateData, but don't want to save review if item has not changed
    if (!issuePane.isModified(current))
    {
      return true;
    }

    if (issuePane.updateData(current))
    {
      project.getComponent(ReviewManager.class).saveSilently(current.getReview());
      return true;
    }

    return false;
  }

  public void updateUI(boolean requestFocus)
  {
    updateMessageCount();
    Issue current = issueTree.getSelectedIssue();
    if (current == null)
    {
      ((CardLayout) pnIssuePaneContainer.getLayout()).show(pnIssuePaneContainer, "message");
    }
    else
    {
      ((CardLayout) pnIssuePaneContainer.getLayout()).show(pnIssuePaneContainer, "issuePane");
      issuePane.updateUI(current.getReview(), current, requestFocus);
    }
  }

  private void updateMessageCount()
  {
    lbCount.setText(RevuBundle.message("browsing.count.text", issueTree.getIssueTreeModel().getIssueCount()));
  }

  public void updateReview()
  {
    updateUI(false);
  }

  private ActionToolbar createToolbar(@NotNull String toolbarId, boolean horizontal)
  {
    ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction(toolbarId);

    ActionToolbar actionToolbar = ActionManager.getInstance()
      .createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, horizontal);
    actionToolbar.setTargetComponent(issueTree);

    return actionToolbar;
  }

  public void dispose()
  {
    issuePane.dispose();

    Collection<Review> reviews = project.getComponent(ReviewManager.class).getReviews();
    for (Review review : reviews)
    {
      review.removeIssueListener(issueListener);
      // @TODO IssueTreeModel should do it by itslef
      review.removeIssueListener(issueTree.getIssueTreeModel());
    }
  }

  public void showFilter(@Nullable IIssueTreeFilter issueTreeFilter)
  {
    IssueTreeModel treeModel = issueTree.getIssueTreeModel();
    if (treeModel.getIssueTreeFilter() != null)
    {
      treeModel.getIssueTreeFilter().removeListener(issueTreeFilterListener);
    }

    treeModel.setIssueTreeFilter(issueTreeFilter);
    if (issueTreeFilter == null)
    {
      splitFilter.setFirstComponent(null);
      treeModel.filter(null);
    }
    else
    {
      issueTreeFilter.addListener(issueTreeFilterListener);
      splitFilter.setFirstComponent(issueTreeFilter.buildUI());
    }

    splitFilter.validate();
    splitFilter.repaint();
  }

  public Object getData(@NonNls String dataId)
  {
    if (RevuDataKeys.REVIEW.is(dataId))
    {
      return review;
    }

    return null;
  }
}
