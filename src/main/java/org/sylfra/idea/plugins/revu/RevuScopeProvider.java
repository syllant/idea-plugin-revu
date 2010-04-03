package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.scope.packageSet.CustomScopesProvider;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.business.FileScopeManager;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;

import java.util.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuScopeProvider implements CustomScopesProvider, IReviewListener
{
  private final Project project;
  private final Map<Review, NamedScope> scopes;

  public RevuScopeProvider(@NotNull Project project)
  {
    this.project = project;
    scopes = new IdentityHashMap<Review, NamedScope>();
    project.getComponent(ReviewManager.class).addReviewListener(this);
  }

  @NotNull
  public List<NamedScope> getCustomScopes()
  {
    return Collections.unmodifiableList(new ArrayList<NamedScope>(scopes.values()));
  }

  public void reviewChanged(Review review)
  {
    reviewAdded(review);
  }

  public void reviewAdded(Review review)
  {
    String scopeTitle = RevuBundle.message("scope.title", review.getName());
    scopes.put(review, new NamedScope(scopeTitle, new CustomPackageSet(project, review)));
  }

  public void reviewDeleted(Review review)
  {
    scopes.remove(review);
  }

  private final static class CustomPackageSet implements PackageSet
  {
    private final Project project;
    private final Review review;
    private final FileScopeManager fileScopeManager;

    private CustomPackageSet(@NotNull Project project, @NotNull Review review)
    {
      this.project = project;
      this.review = review;
      fileScopeManager = ApplicationManager.getApplication().getComponent(FileScopeManager.class);
    }

    public boolean contains(PsiFile file, NamedScopesHolder holder)
    {
      VirtualFile vFile = file.getVirtualFile();
      return (vFile != null) && fileScopeManager.belongsToScope(project, review.getFileScope(), vFile);
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
