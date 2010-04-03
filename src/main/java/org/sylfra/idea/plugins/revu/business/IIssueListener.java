package org.sylfra.idea.plugins.revu.business;

import org.sylfra.idea.plugins.revu.model.Issue;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IIssueListener
{
  void issueAdded(Issue issue);

  void issueDeleted(Issue issue);

  void issueUpdated(Issue issue);
}