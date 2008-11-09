package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractReviewItemForm
{
  @NotNull
  protected final Project project;

  @Nullable
  protected ReviewItem reviewItem;

  public AbstractReviewItemForm(@NotNull Project project)
  {
    this.project = project;
  }

  @NotNull
  public abstract JPanel getContentPane();

  @Nullable
  public ReviewItem getReviewItem()
  {
    return reviewItem;
  }

  public final void updateUI(@Nullable ReviewItem reviewItem)
  {
    this.reviewItem = reviewItem;
    internalUpdateUI();
  }

  public final void updateData(@NotNull ReviewItem reviewItemToUpdate)
  {
    internalUpdateData(reviewItemToUpdate);
  }

  protected abstract void internalUpdateUI();

  protected abstract void internalUpdateData(@Nullable ReviewItem reviewItemToUpdate);
}
