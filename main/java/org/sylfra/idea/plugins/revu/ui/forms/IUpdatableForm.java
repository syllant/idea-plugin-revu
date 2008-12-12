package org.sylfra.idea.plugins.revu.ui.forms;

import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.Review;

import javax.swing.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IUpdatableForm<T> extends Disposable
{
  @Nullable
  JComponent getPreferredFocusedComponent();

  @NotNull
  JPanel getContentPane();

  @Nullable
  Review getEnclosingReview();

  void updateUI(Review enclosingReview, @Nullable T data, boolean requestFocus);

  boolean updateData(@NotNull T data);

  boolean validateInput();
}
