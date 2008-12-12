package org.sylfra.idea.plugins.revu.ui.actions.toolwindow;

import javax.swing.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class SplitReviewBrowsingFormHorizontallyAction extends AbstractSplitReviewBrowsingFormAction
{
  protected int getOrientation()
  {
    return JSplitPane.HORIZONTAL_SPLIT;
  }
}
