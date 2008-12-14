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
    // Invert split, like IDEA does: action icon is vertical whereas split is horizontal and vice et versa
    return JSplitPane.HORIZONTAL_SPLIT;
  }
}