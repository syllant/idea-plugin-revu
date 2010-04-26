package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers;

import org.sylfra.idea.plugins.revu.model.Issue;

import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IIssueTreeGrouper<T extends INamedGroup>
{
  SortedMap<T, SortedSet<Issue>> group(List<Issue> issues);

  String getName();
}
