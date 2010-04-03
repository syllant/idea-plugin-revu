package org.sylfra.idea.plugins.revu.ui.actions.toolwindow;

import javax.swing.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class SplitReviewBrowsingFormHorizontallyAction extends AbstractSplitReviewBrowsingFormAction
{
  protected int getOrientation()
  {
    // Invert split, like IDEA does: action icon is vertical whereas split is horizontal and vice et versa
    return JSplitPane.VERTICAL_SPLIT;
  }
}
