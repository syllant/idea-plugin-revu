package org.sylfra.idea.plugins.revu.ui.toolwindow.tree;

import com.intellij.ide.projectView.BaseProjectTreeBuilder;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.ide.util.treeView.PresentableNodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.LayeredIcon;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.xml.util.ColorIconCache;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.IIssueTreeGrouper;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers.INamedGroup;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueTreeBuilder extends BaseProjectTreeBuilder
{
  public IssueTreeBuilder(Project project, JTree tree, IssueTreeModel treeModel,
    AbstractTreeStructure treeStructure, Comparator<NodeDescriptor> comparator)
  {
    super(project, tree, treeModel, treeStructure, comparator);
  }

  public TreeNode build(IIssueTreeGrouper<? extends INamedGroup> grouper, List<Issue> issues)
  {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

    SortedMap<? extends INamedGroup, SortedSet<Issue>> map = grouper.group(issues);
    for (Map.Entry<? extends INamedGroup, SortedSet<Issue>> entry : map.entrySet())
    {
      GroupNodeDescriptor groupNodeDescriptor = new GroupNodeDescriptor(myProject, entry.getKey().getName());
      DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(groupNodeDescriptor);

      for (Issue issue : entry.getValue())
      {
        IssueNodeDescriptor nodeDescriptor = new IssueNodeDescriptor(groupNodeDescriptor, issue);
        groupNodeDescriptor.addChild(nodeDescriptor);
        groupNode.add(new DefaultMutableTreeNode(nodeDescriptor));
      }

      rootNode.add(groupNode);
    }

    return rootNode;
  }

  @Override
  protected boolean isDisposeOnCollapsing(NodeDescriptor nodeDescriptor) {
    return false;
  }

  private final static class GroupNodeDescriptor extends PresentableNodeDescriptor<String>
  {
    private List<IssueNodeDescriptor> children;

    public GroupNodeDescriptor(Project project, String name)
    {
      super(project, null);

      myName = name;
      children = new ArrayList<IssueNodeDescriptor>();
    }

    @NotNull
    protected PresentationData createPresentation()
    {
      PresentationData presentation = new PresentationData();
      if (myName == null)
      {
        presentation.addText(RevuBundle.message("browsing.group.values.none.text"),
          SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
      }
      else
      {
        presentation.addText(myName, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
      }

      presentation.addText(" [" + children.size() + "]", SimpleTextAttributes.GRAY_ATTRIBUTES);

      return presentation;
    }

    @Override
    protected void update(PresentationData presentation)
    {
    }

    @Override
    public PresentableNodeDescriptor getChildToHighlightAt(int index)
    {
      return children.get(index);
    }

    @Override
    public String getElement()
    {
      return myName;
    }

    public void addChild(IssueNodeDescriptor nodeDescriptor)
    {
      children.add(nodeDescriptor);
    }
  }

  private class IssueNodeDescriptor extends PresentableNodeDescriptor<Issue>
  {
    private final Issue issue;

    public IssueNodeDescriptor(GroupNodeDescriptor groupNodeDescriptor, Issue issue)
    {
      super(groupNodeDescriptor.getProject(), groupNodeDescriptor);
      this.issue = issue;

      myName = issue.getPresentableSummary();

      myClosedIcon = new LayeredIcon(new ColorIconCache.ColorIcon(16, RevuUtils.getIssueStatusColor(issue.getStatus())),
        RevuIconProvider.getIcon(RevuIconProvider.IconRef.GUTTER_ISSUE));
    }

    @NotNull
    protected PresentationData createPresentation()
    {
      PresentationData presentation = new PresentationData();

      presentation.addText(" " + issue.getSummary(), SimpleTextAttributes.REGULAR_ATTRIBUTES);

      presentation.setTooltip(issue.getDesc());

      return presentation;
    }

    @Override
    protected void update(PresentationData presentation)
    {
    }

    @Override
    public PresentableNodeDescriptor getChildToHighlightAt(int index)
    {
      return null;
    }

    @Override
    public Issue getElement()
    {
      return issue;
    }
  }
}
