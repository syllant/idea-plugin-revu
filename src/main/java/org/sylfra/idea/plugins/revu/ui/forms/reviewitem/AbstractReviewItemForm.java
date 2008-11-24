package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractReviewItemForm extends AbstractUpdatableForm<ReviewItem>
{
  @NotNull
  protected final Project project;

  public AbstractReviewItemForm(@NotNull Project project)
  {
    this.project = project;
  }
}
