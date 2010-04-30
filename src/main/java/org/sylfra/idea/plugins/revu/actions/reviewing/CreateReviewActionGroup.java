package org.sylfra.idea.plugins.revu.actions.reviewing;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.sylfra.idea.plugins.revu.actions.review.CreateReviewAction;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class CreateReviewActionGroup extends DefaultActionGroup
{
  @Override
  public void update(AnActionEvent e)
  {
    if (getChildrenCount() == 0)
    {
      add(new CreateReviewAction(false, null));
      add(new CreateReviewAction(true, null));
    }
  }
}
