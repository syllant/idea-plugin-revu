package org.sylfra.idea.plugins.revu.model;

import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IRevuHistoryHolderEntity<T> extends IRevuEntity<T>
{
  @NotNull History getHistory();
  
  void setHistory(@NotNull History history);
}
