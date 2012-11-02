package org.sylfra.idea.plugins.revu.ui.toolwindow.tree;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.projectView.impl.ProjectTreeStructure;
import com.intellij.ide.util.treeView.AlphaComparator;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.FilterComponent;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.ui.tree.TreeModelAdapter;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.projectView.RevuProjectViewPane;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.IIssueTreeGrouper;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.INamedGroup;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueTree extends Tree implements DataProvider, OccurenceNavigator
{
  private final Project project;
  private final Review review;

  public IssueTree(Project project, @NotNull Review review,
    @NotNull IIssueTreeGrouper<? extends INamedGroup> issueTreeGrouper)
  {
    this.project = project;
    this.review = review;

    IssueTreeModel model = new IssueTreeModel(review, issueTreeGrouper);
    IssueTreeBuilder treeBuilder = new IssueTreeBuilder(project, this, model,
      new ProjectTreeStructure(project, RevuProjectViewPane.ID){}, AlphaComparator.INSTANCE);
    model.setTreeBuilder(treeBuilder);
    model.rebuild();

    setModel(model);

    setRootVisible(false);
    setShowsRootHandles(true);

    UIUtil.setLineStyleAngled(this);
//    TreeToolTipHandler.install(this); // Not in IDEA 11
    TreeUtil.installActions(this);
    EditSourceOnDoubleClickHandler.install(this);

    installListeners();
  }

  private void installListeners()
  {
    TreeUIHelper.getInstance().installToolTipHandler(this);

    PopupHandler.installPopupHandler(this, "revu.issueTree.popup", "issueTree");

    getModel().addTreeModelListener(new TreeModelAdapter()
    {
      @Override
      public void treeStructureChanged(TreeModelEvent e)
      {
        final Issue issue = getSelectedIssue();
        SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            expandAll();
            if (issue != null)
            {
              selectIssue(issue);
            }
          }
        });
      }
    });
  }

  @NotNull
  public IssueTreeModel getIssueTreeModel()
  {
    return (IssueTreeModel) getModel();
  }

  @Nullable
  public Issue getSelectedIssue()
  {
    TreePath path = getSelectionPath();
    if ((path == null))
    {
      return null;
    }

    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
    final Object userObject = node.getUserObject();
    if (!(userObject instanceof NodeDescriptor)) {
      return null;
    }
    NodeDescriptor nodeDescriptor = (NodeDescriptor) userObject;
    Object object = nodeDescriptor.getElement();

    return (object instanceof Issue) ? (Issue) object : null;
  }

  public void expandAll()
  {
    TreeUtil.expandAll(this);
  }

  public void collapseAll()
  {
    TreeUtil.collapseAll(this, 0);
  }

  public JComponent buildFilterComponent()
  {
    return new FilterComponent("revuTableFilter", 5)
    {
      public void filter()
      {
        getIssueTreeModel().filterWithPlainText(getFilter());
      }
    };
  }

  public void selectIssue(@NotNull Issue issue)
  {
    TreePath treePath = getIssueTreeModel().getTreePath(issue);

    if (treePath != null)
    {
      setSelectionPath(treePath);
      scrollPathToVisible(treePath);
    }
  }

  public boolean hasNextOccurence()
  {
    TreePath path = getSelectionPath();
    return (path != null) && (((DefaultMutableTreeNode) path.getLastPathComponent()).getNextLeaf() != null);
  }

  public boolean hasPreviousOccurence()
  {
    TreePath path = getSelectionPath();
    return (path != null) && (((DefaultMutableTreeNode) path.getLastPathComponent()).getPreviousLeaf() != null);
  }

  public String getNextOccurenceActionName()
  {
    return RevuBundle.message("browsing.issues.next.description");
  }

  public String getPreviousOccurenceActionName()
  {
    return RevuBundle.message("browsing.issues.previous.description");
  }

  public OccurenceNavigator.OccurenceInfo goNextOccurence()
  {
    return buildOccurenceInfo(true);
  }

  public OccurenceNavigator.OccurenceInfo goPreviousOccurence()
  {
    return buildOccurenceInfo(false);
  }

  private OccurenceNavigator.OccurenceInfo buildOccurenceInfo(final boolean next)
  {
    Navigatable navigatable = new Navigatable()
    {
      public void navigate(boolean requestFocus)
      {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) getSelectionPath().getLastPathComponent();
        setSelectionPath(new TreePath(((next ? node.getNextLeaf() : node.getPreviousLeaf()).getPath())));
      }

      public boolean canNavigate()
      {
        return true;
      }

      public boolean canNavigateToSource()
      {
        return true;
      }
    };

    int[] rows = getSelectionRows();
    return new OccurenceNavigator.OccurenceInfo(navigatable, ((rows == null) || (rows.length == 0)) ? -1 : rows[0],
      getRowCount());
  }

  public Object getData(@NonNls String dataId)
  {
    final Issue selectedIssue = getSelectedIssue();

    if (PlatformDataKeys.NAVIGATABLE_ARRAY.is(dataId))
    {
      final VirtualFile selectedIssueFile = selectedIssue == null ? null : selectedIssue.getFile();
      if ((selectedIssue != null) && (selectedIssueFile != null)) {
        OpenFileDescriptor fileDescriptor = new OpenFileDescriptor(project, selectedIssueFile,
            selectedIssue.getLineStart(), 0);
        return new Navigatable[]{fileDescriptor};
      }

      return null;
    }

    if (RevuDataKeys.ISSUE.is(dataId))
    {
      return selectedIssue;
    }

    if (RevuDataKeys.ISSUE_LIST.is(dataId))
    {
      return selectedIssue == null ? Collections.emptyList() : Arrays.asList(selectedIssue);
    }

    if (RevuDataKeys.REVIEW.is(dataId))
    {
      return review;
    }

    return null;
  }
}
