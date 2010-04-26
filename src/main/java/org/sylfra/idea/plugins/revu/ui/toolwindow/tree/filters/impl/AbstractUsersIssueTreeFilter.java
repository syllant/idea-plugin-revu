package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.impl;

import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters.AbstractListUiIssueTreeFilter;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractUsersIssueTreeFilter extends AbstractListUiIssueTreeFilter<User>
{
  @Override
  protected String getListItemText(User user)
  {
    return user.getDisplayName();
  }
}