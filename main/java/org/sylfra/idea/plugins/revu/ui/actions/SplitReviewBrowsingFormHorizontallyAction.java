package org.sylfra.idea.plugins.revu.ui.actions;

import javax.swing.*;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class SplitReviewBrowsingFormHorizontallyAction extends AbstractSplitReviewBrowsingFormAction
{
  protected int getOrientation()
  {
    return JSplitPane.HORIZONTAL_SPLIT;
  }
}
