package org.sylfra.idea.plugins.revu.model;

import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IHistoryHolder
{
  @NotNull History getHistory();
  
  void setHistory(@NotNull History history);
}
