package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EventObject;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IIssueTreeFilterListener<T>
{
  public void valueChanged(@NotNull EventObject event, @Nullable Object value);
}
