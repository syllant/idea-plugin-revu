package org.sylfra.idea.plugins.revu.ui.actions.toolwindow;

import javax.swing.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class SplitReviewBrowsingFormVerticallyAction extends AbstractSplitReviewBrowsingFormAction
{
  protected int getOrientation()
  {
    return JSplitPane.VERTICAL_SPLIT;
  }
}