package org.sylfra.idea.plugins.revu.business;

import org.sylfra.idea.plugins.revu.model.Issue;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IIssueListener
{
  void itemAdded(Issue item);

  void itemDeleted(Issue item);

  void itemUpdated(Issue item);
}