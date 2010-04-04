/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sylfra.idea.plugins.revu.ui;

import com.intellij.ide.SelectInTarget;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.scopeView.ScopePaneSelectInTarget;
import com.intellij.ide.scopeView.ScopeTreeViewPanel;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.scope.packageSet.*;
import com.intellij.ui.PopupHandler;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.FileScopeManager;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cdr
 */
public class RevuViewPane extends AbstractProjectViewPane implements IReviewListener
{
  @NonNls
  public static final String ID = "Revu";
  public static final Icon ICON = IconLoader.getIcon("/org/sylfra/idea/plugins/revu/resources/icons/revu.png");

  private final ProjectView myProjectView;
  private ScopeTreeViewPanel myViewPanel;
  private final Map<Review, NamedScope> scopes;

  public RevuViewPane(Project project, ProjectView projectView)
  {
    super(project);
    myProjectView = projectView;
    scopes = new IdentityHashMap<Review, NamedScope>();

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    reviewManager.addReviewListener(this);
    for (Review review : reviewManager.getReviews())
    {
      if (!review.isEmbedded())
      {
        reviewAdded(review);
      }
    }
  }

  public String getTitle()
  {
    return RevuBundle.message("general.plugin.title");
  }

  public Icon getIcon()
  {
    return ICON;
  }

  @NotNull
  public String getId()
  {
    return ID;
  }


  public JComponent createComponent()
  {
    myViewPanel = new ScopeTreeViewPanel(myProject);
    Disposer.register(this, myViewPanel);
    myViewPanel.initListeners();
    updateFromRoot(true);

    myTree = myViewPanel.getTree();
    PopupHandler.installPopupHandler(myTree, "RevuProjectViewPopupMenu", "RevuProjectViewPopup");
    enableDnD();

    return myViewPanel.getPanel();
  }

  public void dispose()
  {
    myViewPanel = null;
    super.dispose();
  }

  @NotNull
  public String[] getSubIds()
  {
    String[] ids = new String[scopes.size()];
    int i = 0;
    for (NamedScope namedScope : scopes.values())
    {
      ids[i++] = namedScope.getName();
    }

    return ids;
  }

  @NotNull
  public String getPresentableSubIdName(@NotNull final String subId)
  {
    return subId;
  }

  public void addToolbarActions(DefaultActionGroup actionGroup)
  {
    actionGroup.add(ActionManager.getInstance().getAction("revu.ProjectView.ToggleFilterIssues"));
  }

  public ActionCallback updateFromRoot(boolean restoreExpandedPaths)
  {
    for (NamedScope namedScope : scopes.values())
    {
      if (namedScope.getName().equals(getSubId()))
      {
        myViewPanel.selectScope(namedScope);
      }
    }
    
    return new ActionCallback.Done();
  }

  public void select(Object element, VirtualFile file, boolean requestFocus)
  {
    if (file == null)
    {
      return;
    }
    PsiFile psiFile = PsiManager.getInstance(myProject).findFile(file);
    if (psiFile == null)
    {
      return;
    }
    if (!(element instanceof PsiElement))
    {
      return;
    }

    List<NamedScope> allScopes = new ArrayList<NamedScope>(scopes.values());
    for (int i = 0; i < allScopes.size(); i++)
    {
      final NamedScope scope = allScopes.get(i);
      String name = scope.getName();
      if (name.equals(getSubId()))
      {
        allScopes.set(i, allScopes.get(0));
        allScopes.set(0, scope);
        break;
      }
    }
    for (NamedScope scope : allScopes)
    {
      String name = scope.getName();
      PackageSet packageSet = scope.getValue();
      if (packageSet == null)
      {
        continue;
      }
      if (changeView(packageSet, ((PsiElement) element), psiFile, name, requestFocus))
      {
        break;
      }
    }
  }

  private boolean changeView(final PackageSet packageSet, final PsiElement element, final PsiFile psiFile,
    final String name, boolean requestFocus)
  {
    if (packageSet.contains(psiFile, NamedScopesHolder.getHolder(myProject, name, null)))
    {
      if (!name.equals(getSubId()))
      {
        myProjectView.changeView(getId(), name);
      }
      myViewPanel.selectNode(element, psiFile, requestFocus);
      return true;
    }

    return false;
  }

  public int getWeight()
  {
    return Integer.MAX_VALUE;
  }

  public void installComparator()
  {
    myViewPanel.setSortByType();
  }

  public SelectInTarget createSelectInTarget()
  {
    return new ScopePaneSelectInTarget(myProject);
  }

  protected Object exhumeElementFromNode(final DefaultMutableTreeNode node)
  {
    if (node instanceof PackageDependenciesNode)
    {
      return ((PackageDependenciesNode) node).getPsiElement();
    }
    return super.exhumeElementFromNode(node);
  }

  public Object getData(final String dataId)
  {
    final Object data = super.getData(dataId);
    if (data != null)
    {
      return data;
    }
    return myViewPanel != null ? myViewPanel.getData(dataId) : null;
  }

  public void reviewChanged(Review review)
  {
    reviewAdded(review);
  }

  public void reviewAdded(Review review)
  {
    scopes.put(review, new NamedScope(getScopeName(review), new CustomPackageSet(myProject, review)));
    refreshView();
  }

  public void reviewDeleted(Review review)
  {
    scopes.remove(review);
    refreshView();
  }

  private void refreshView()
  {
    Alarm refreshProjectViewAlarm = new Alarm();
    // amortize batch scope changes
    refreshProjectViewAlarm.cancelAllRequests();
    refreshProjectViewAlarm.addRequest(new Runnable()
    {
      public void run()
      {
        if (myProject.isDisposed())
        {
          return;
        }
        myProjectView.removeProjectPane(RevuViewPane.this);
        myProjectView.addProjectPane(RevuViewPane.this);
      }
    }, 10);
  }

  private String getScopeName(Review review)
  {
    return review.getName();
  }

  private final static class CustomPackageSet implements PackageSet
  {
    private static final Logger LOGGER = Logger.getInstance(CustomPackageSet.class.getName());

    private final Project project;
    private final Review review;
    private final FileScopeManager fileScopeManager;
    private final PackageSet packageSet;

    private CustomPackageSet(@NotNull Project project, @NotNull Review review)
    {
      this.project = project;
      this.review = review;
      fileScopeManager = ApplicationManager.getApplication().getComponent(FileScopeManager.class);

      PackageSet packageSetTmp = null;
      String pathPattern = review.getFileScope().getPathPattern();
      try
      {
        if (pathPattern != null)
        {
          packageSetTmp = PackageSetFactory.getInstance().compile(pathPattern);
        }
      }
      catch (ParsingException e)
      {
        LOGGER.warn("Failed to compile file scope path pattern: <" + pathPattern + ">");
      }

      packageSet = packageSetTmp;
    }

    public boolean contains(PsiFile file, NamedScopesHolder holder)
    {
      VirtualFile vFile = file.getVirtualFile();
      return (vFile != null)
        && (checkFilter(vFile))
        && fileScopeManager.belongsToScope(project, review.getFileScope(), packageSet, vFile);
    }

    private boolean checkFilter(VirtualFile vFile)
    {
      return !RevuUtils.getWorkspaceSettings(project).isFilterFilesWithIssues() || review.hasIssues(vFile);
    }

    public PackageSet createCopy()
    {
      return this;
    }

    public String getText()
    {
      return "";
    }

    public int getNodePriority()
    {
      return 0;
    }
  }
}