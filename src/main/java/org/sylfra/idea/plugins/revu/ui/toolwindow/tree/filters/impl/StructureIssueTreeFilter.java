package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.impl;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.Icons;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.AbstractIssueTreeFilter;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class StructureIssueTreeFilter extends AbstractIssueTreeFilter<NodeDescriptor>
{
  private final TreeBuilder treeBuilder;
  private JScrollPane contentPane;
  private Tree tree;

  public StructureIssueTreeFilter()
  {
    treeBuilder = new TreeBuilder();
  }

  public String getName()
  {
    return RevuBundle.message("browsing.filteringGrouping.structure.text");
  }

  public JComponent buildUI()
  {
    if (contentPane == null)
    {
      tree = new Tree();
      tree.setRootVisible(false);
      tree.setShowsRootHandles(true);
      tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener()
      {
        public void valueChanged(final TreeSelectionEvent e)
        {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
          fireValueChanged(e, node.getUserObject());
        }
      });

      contentPane = new JScrollPane(tree);
    }

    ((DefaultTreeModel) tree.getModel()).setRoot(treeBuilder.buildRoot(getReview()));

    return contentPane;
  }

  @Override
  protected boolean match(Issue issue, NodeDescriptor filterValue)
  {
    VirtualFile vFile = issue.getFile();
    if (vFile == null)
    {
      return false;
    }

    if (filterValue instanceof CustomFileNodeDescriptor)
    {
      return VfsUtil.isAncestor((VirtualFile) filterValue.getElement(), vFile, false);
    }

    if (filterValue instanceof CustomModuleNodeDescriptor)
    {
      Module module = (Module) filterValue.getElement();
      VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();

      for (VirtualFile root : roots)
      {
        if (VfsUtil.isAncestor(root, vFile, false))
        {
          return true;
        }
      }

      return false;
    }

    return false;
  }

  private final static class TreeBuilder
  {
    private final Map<VirtualFile, Module> moduleRoots;
    private final Map<VirtualFile, DefaultMutableTreeNode> fileNodesCache;
    private final Map<Module, DefaultMutableTreeNode> moduleNodesCache;

    public TreeBuilder()
    {
      moduleRoots = new HashMap<VirtualFile, Module>();
      fileNodesCache = new HashMap<VirtualFile, DefaultMutableTreeNode>();
      moduleNodesCache = new HashMap<Module, DefaultMutableTreeNode>();
    }

    private TreeNode buildRoot(@NotNull Review review)
    {
      initModuleRoots();

      fileNodesCache.clear();
      moduleNodesCache.clear();

      DefaultMutableTreeNode result = new DefaultMutableTreeNode();

      for (Issue issue : review.getIssues())
      {
        VirtualFile vFile = issue.getFile();
        if (vFile != null)
        {
          addNode(result, vFile.getParent());
        }
      }

      return result;
    }

    private void initModuleRoots()
    {
      moduleRoots.clear();

      Module[] modules = ModuleManager.getInstance(RevuUtils.getProject()).getModules();
      for (Module module : modules)
      {
        VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
        for (VirtualFile contentRoot : contentRoots)
        {
          moduleRoots.put(contentRoot, module);
        }
      }
    }

    private DefaultMutableTreeNode addNode(DefaultMutableTreeNode rootNode, VirtualFile vFile)
    {
      if (vFile == null)
      {
        return null;
      }

      Module module = moduleRoots.get(vFile);

      Project project = RevuUtils.getProject();
      DefaultMutableTreeNode parentNode;
      if (module == null)
      {
        DefaultMutableTreeNode node = fileNodesCache.get(vFile);
        if (node != null)
        {
          return node;
        }

        parentNode = addNode(rootNode, vFile.getParent());

        if (parentNode == null)
        {
          // Should not happen
          return null;
        }
      }
      else
      {
        DefaultMutableTreeNode moduleNode = moduleNodesCache.get(module);
        if (moduleNode == null)
        {
          NodeDescriptor nodeDescriptor = new CustomModuleNodeDescriptor(project, module);
          moduleNode = new DefaultMutableTreeNode(nodeDescriptor);
          rootNode.insert(moduleNode, findIndex(rootNode, nodeDescriptor));

          moduleNodesCache.put(module, moduleNode);
        }

        parentNode = moduleNode;
      }

      NodeDescriptor nodeDescriptor = new CustomFileNodeDescriptor(project,
        (NodeDescriptor) parentNode.getUserObject(), vFile);

      DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeDescriptor);
      parentNode.insert(node, findIndex(parentNode, nodeDescriptor));

      fileNodesCache.put(vFile, node);

      return node;
    }

    private int findIndex(DefaultMutableTreeNode parentNode, NodeDescriptor nodeDescriptor)
    {
      for (int i=0; i<parentNode.getChildCount(); i++)
      {
        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) parentNode.getChildAt(i);
        NodeDescriptor childDescriptor = (NodeDescriptor) childNode.getUserObject();

        if (childDescriptor.toString().compareTo(nodeDescriptor.toString()) > 0)
        {
          return i;
        }
      }

      return parentNode.getChildCount();
    }

  }

  private static class CustomModuleNodeDescriptor extends NodeDescriptor<Module>
  {
    private final Module module;

    public CustomModuleNodeDescriptor(Project project, Module module)
    {
      super(project, null);
      this.module = module;

      myOpenIcon = Icons.CONTENT_ROOT_ICON_OPEN;
      myClosedIcon = Icons.CONTENT_ROOT_ICON_CLOSED;
      myName = module.getName();
    }

    @Override
    public boolean update()
    {
      return false;
    }

    @Override
    public Module getElement()
    {
      return module;
    }
  }

  private static class CustomFileNodeDescriptor extends NodeDescriptor<VirtualFile>
  {
    private final VirtualFile vFile;

    public CustomFileNodeDescriptor(Project project, NodeDescriptor parentDescriptor, VirtualFile vFile)
    {
      super(project, parentDescriptor);
      this.vFile = vFile;

      myOpenIcon = Icons.DIRECTORY_OPEN_ICON;
      myClosedIcon = Icons.DIRECTORY_CLOSED_ICON;
      myName = (parentDescriptor instanceof CustomModuleNodeDescriptor)
        ? vFile.getPresentableUrl() : vFile.getPresentableName();
    }

    @Override
    public boolean update()
    {
      return false;
    }

    @Override
    public VirtualFile getElement()
    {
      return vFile;
    }
  }
}