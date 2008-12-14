package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.scope.packageSet.CustomScopesProvider;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;

import java.util.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuScopeProvider implements CustomScopesProvider, IReviewListener
{
  private final Project project;
  private final Map<Review, NamedScope> scopes;

  public RevuScopeProvider(Project project)
  {
    this.project = project;
    scopes = new HashMap<Review, NamedScope>();
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
    scopes.put(review, new NamedScope(scopeTitle, new CustomPackageSet(review)));
  }

  public void reviewDeleted(Review review)
  {
    scopes.remove(review);
  }

  private final static class CustomPackageSet implements PackageSet
  {
    private final Review review;

    private CustomPackageSet(@NotNull Review review)
    {
      this.review = review;
    }

    public boolean contains(PsiFile file, NamedScopesHolder holder)
    {
      VirtualFile vFile = file.getVirtualFile();
      return (vFile != null) && review.getIssuesByFiles().containsKey(vFile);
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
