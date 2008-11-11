package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IReviewItemForm
{
  @Nullable JComponent getPreferredFocusedComponent();

  @NotNull
  JPanel getContentPane();

  @Nullable
  ReviewItem getReviewItem();

  void updateUI(@NotNull ReviewItem reviewItem);

  boolean updateData(@NotNull ReviewItem reviewItemToUpdate);

  boolean validateInput();
}
