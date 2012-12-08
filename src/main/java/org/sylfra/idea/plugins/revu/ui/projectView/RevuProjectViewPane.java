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

package org.sylfra.idea.plugins.revu.ui.projectView;

import com.intellij.ide.SelectInTarget;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.projectView.ProjectViewNodeDecorator;
import com.intellij.ide.projectView.impl.AbstractProjectViewPane;
import com.intellij.ide.scopeView.nodes.BasePsiNode;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.ui.PackageDependenciesNode;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.Alarm;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuProjectViewPane extends AbstractProjectViewPane
{
  private final static ReviewStatus[] VISIBLE_STATUSES_ARRAY
    = {ReviewStatus.DRAFT, ReviewStatus.FIXING, ReviewStatus.REVIEWING};
  private static final List<ReviewStatus> VISIBLE_STATUSES_LIST = Arrays.asList(VISIBLE_STATUSES_ARRAY);

  @NonNls
  public static final String ID = "Revu";
  public static final Icon ICON = IconLoader.getIcon("/org/sylfra/idea/plugins/revu/resources/icons/revu.png");

  private final ProjectView myProjectView;
  private RevuScopeTreeViewPanel myViewPanel;
  private final IdentityHashMap<Review, NamedScope> scopes;

  public RevuProjectViewPane(Project project, ProjectView projectView)
  {
    super(project);
    myProjectView = projectView;
    scopes = new IdentityHashMap<Review, NamedScope>();

    installListeners();
  }

  private void installListeners()
  {
    ReviewManager reviewManager = myProject.getComponent(ReviewManager.class);
    CustomReviewListener reviewListener = new CustomReviewListener();
    reviewManager.addReviewListener(reviewListener);
    for (Review review : reviewManager.getReviews(RevuUtils.getCurrentUserLogin(), VISIBLE_STATUSES_ARRAY))
    {
      reviewListener.reviewAdded(review);
    }

    myProject.getComponent(RevuWorkspaceSettingsComponent.class).addListener(new CustomWorkspaceSettingsListener());
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
    myViewPanel = new RevuScopeTreeViewPanel(myProject);
    Disposer.register(this, myViewPanel);
    myViewPanel.initListeners();

    myTree = myViewPanel.getTree();
    PopupHandler.installPopupHandler(myTree, "RevuProjectViewPopupMenu", "RevuProjectViewPopup");
    enableDnD();

    myTree.setCellRenderer(new CustomTreeCellRenderer(myProject, myViewPanel));

    updateFromRoot(true);

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
    actionGroup.add(ActionManager.getInstance().getAction("revu.ShowProjectSettings"));
  }

  public ActionCallback updateFromRoot(boolean restoreExpandedPaths)
  {
    saveExpandedPaths();
//    myViewPanel.selectScope(NamedScopesHolder.getScope(myProject, getSubId()));
    for (NamedScope namedScope : scopes.values())
    {
      if (namedScope.getName().equals(getSubId()))
      {
        myViewPanel.selectScope(namedScope);
        break;
      }
    }
    if (myTree != null) {
      restoreExpandedPaths();
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
    // Does not work anymore with IDEA 11
    //NamedScopesHolder holder = NamedScopesHolder.getHolder(myProject, name, null);
    NamedScopesHolder holder = NamedScopesHolder.getAllNamedScopeHolders(myProject)[0];
    if ((holder == null) || packageSet.contains(psiFile, holder))
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
    // http://code.google.com/p/idea-revu/issues/detail?id=18
    // bug when 2 panes have same weight?!
    return Integer.MAX_VALUE - 1;
  }

  public void installComparator()
  {
    myViewPanel.setSortByType();
  }

  public SelectInTarget createSelectInTarget()
  {
    return new RevuPaneSelectInTarget(myProject);
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
    if (RevuDataKeys.REVIEW.is(dataId))
    {
      return getSelectedReview();
    }

    final Object data = super.getData(dataId);
    if (data != null)
    {
      return data;
    }

    return myViewPanel != null ? myViewPanel.getData(dataId) : null;
  }

  private boolean isVisible(Review review)
  {
    String currentUserLogin = RevuUtils.getCurrentUserLogin();
    return (currentUserLogin != null)
      && VISIBLE_STATUSES_LIST.contains(review.getStatus())
      && review.getDataReferential().getUser(currentUserLogin, true) != null;
  }

  public void rebuildPane()
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
        myProjectView.removeProjectPane(RevuProjectViewPane.this);
        myProjectView.addProjectPane(RevuProjectViewPane.this);
      }
    }, 10);
  }

  private String getScopeName(Review review)
  {
    return review.getName();
  }

  @Nullable
  public Review getSelectedReview()
  {
    String reviewName = getSubId();
    return reviewName == null ? null : myProject.getComponent(ReviewManager.class).getReviewByName(reviewName);
  }

  // See com.intellij.ide.scopeView.ScopeTreeViewPanel.MyTreeCellRenderer
  private class CustomTreeCellRenderer extends ColoredTreeCellRenderer
  {
    private final Project project;
    private final RevuScopeTreeViewPanel scopeTreeViewPanel;

    public CustomTreeCellRenderer(Project project, RevuScopeTreeViewPanel scopeTreeViewPanel)
    {
      this.project = project;
      this.scopeTreeViewPanel = scopeTreeViewPanel;
    }

    public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
      int row, boolean hasFocus)
    {
      if (value instanceof PackageDependenciesNode)
      {
        PackageDependenciesNode node = (PackageDependenciesNode) value;
        try
        {
          setIcon(expanded ? node.getOpenIcon() : node.getClosedIcon());
        }
        catch (IndexNotReadyException ignored)
        {
        }

        final SimpleTextAttributes regularAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
        TextAttributes textAttributes = regularAttributes.toTextAttributes();
        if (node instanceof BasePsiNode && ((BasePsiNode) node).isDeprecated())
        {
          textAttributes =
            EditorColorsManager.getInstance().getGlobalScheme().getAttributes(CodeInsightColors.DEPRECATED_ATTRIBUTES)
              .clone();
        }

        PsiElement psiElement = node.getPsiElement();
        VirtualFile vFile = (psiElement == null)
          ? null
            : ((psiElement instanceof PsiDirectory)
              ? ((PsiDirectory) psiElement).getVirtualFile()
                : (psiElement.getContainingFile() == null) ? null : psiElement.getContainingFile().getVirtualFile());

        String reviewName = scopeTreeViewPanel.getCurrentScopeName();
        if (reviewName == null)
        {
          return;
        }

        Review review = project.getComponent(ReviewManager.class).getReviewByName(reviewName);
        if (review != null)
        {
          FileStatus fileStatus = retrieveFileStatus(review, vFile);
          textAttributes.setForegroundColor(fileStatus.getColor());
        }

        append(node.toString(), SimpleTextAttributes.fromTextAttributes(textAttributes));

        String oldToString = toString();
        for (ProjectViewNodeDecorator decorator : Extensions.getExtensions(ProjectViewNodeDecorator.EP_NAME, myProject))
        {
          decorator.decorate(node, this);
        }

        if (review != null)
        {
          int issueCount = retrieveIssueCount(review, vFile);
          if (issueCount > 0)
          {
            append(" [" + RevuBundle.message("projectView.issueCount.text", issueCount) + "]",
              SimpleTextAttributes.GRAY_ATTRIBUTES);
          }
        }

        if (toString().equals(oldToString))
        {   // nothing was decorated
          final String locationString = node.getComment();
          if (locationString != null && locationString.length() > 0)
          {
            append(" (" + locationString + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
          }
        }
      }
    }

    private FileStatus retrieveFileStatus(Review review, VirtualFile vFile)
    {
      if (review.getFileScope().getVcsAfterRev() != null)
      {
        // @TODO: handle ADDED/MODIFIED
        return FileStatus.MODIFIED;
      }

      if (vFile == null)
      {
        return FileStatus.NOT_CHANGED;
      }

      FileStatus fileStatus = FileStatusManager.getInstance(project).getStatus(vFile);
      return (fileStatus == null) ? FileStatus.NOT_CHANGED : fileStatus;
    }

    private int retrieveIssueCount(Review review, VirtualFile vFile)
    {
      return ((vFile == null) || (review == null)) ? 0 : review.getIssues(vFile).size();
    }
  }

  private class CustomReviewListener implements IReviewListener
  {
    public void reviewChanged(Review review)
    {
      if (isVisible(review))
      {
        scopes.put(review, new NamedScope(getScopeName(review), new RevuPackageSet(myProject, review)));
      }
      else
      {
        scopes.remove(review);
      }

      // @TODO refresh only current tree
      updateFromRoot(true);
    }

    public void reviewAdded(Review review)
    {
      if (isVisible(review))
      {
        scopes.put(review, new NamedScope(getScopeName(review), new RevuPackageSet(myProject, review)));
        rebuildPane();
      }
    }

    public void reviewDeleted(Review review)
    {
      scopes.remove(review);
      rebuildPane();
    }
  }

  private class CustomWorkspaceSettingsListener implements IRevuSettingsListener<RevuWorkspaceSettings>
  {
    public void settingsChanged(RevuWorkspaceSettings oldSettings, RevuWorkspaceSettings newSettings)
    {
      if (newSettings.isFilterFilesWithIssues() != oldSettings.isFilterFilesWithIssues())
      {
        updateFromRoot(true);
      }
    }
  }
}
