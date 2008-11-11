package org.sylfra.idea.plugins.revu.ui.actions;

import javax.swing.*;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class SplitReviewBrowsingFormVerticallyAction extends AbstractSplitReviewBrowsingFormAction
{
  protected int getOrientation()
  {
    return JSplitPane.VERTICAL_SPLIT;
  }
}