package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters;

import org.sylfra.idea.plugins.revu.model.Issue;

import javax.swing.*;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IIssueTreeFilter<T>
{
  String getName();

  JComponent buildUI();

  List<Issue> filter(List<Issue> issues, T filterValue);

  void addListener(IIssueTreeFilterListener<T> listener);

  void removeListener(IIssueTreeFilterListener<T> listener);
}