package org.sylfra.idea.plugins.revu.ui.forms;

import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IUpdatableForm<T> extends Disposable
{
  @Nullable
  T getData();

  @Nullable
  JComponent getPreferredFocusedComponent();

  @NotNull
  JPanel getContentPane();

  void updateUI(@NotNull T data);

  boolean updateData(@NotNull T data);

  boolean validateInput();
}
