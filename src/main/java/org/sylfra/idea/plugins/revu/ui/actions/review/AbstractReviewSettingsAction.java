package org.sylfra.idea.plugins.revu.ui.actions.review;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.Review;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractReviewSettingsAction extends AnAction
{
  protected AbstractReviewSettingsAction()
  {
  }

  protected AbstractReviewSettingsAction(String text)
  {
    super(text);
  }

  protected AbstractReviewSettingsAction(String text, String description, Icon icon)
  {
    super(text, description, icon);
  }

  @Override
  public void update(AnActionEvent e)
  {
    Component component = e.getData(DataKeys.CONTEXT_COMPONENT);
    if (!(component instanceof JList))
    {
      return;
    }

    JList liReviews = (JList) component;
    Object selectedValue = liReviews.getSelectedValue();
    if (!(selectedValue instanceof Review))
    {
      return;
    }

    e.getPresentation().setEnabled(isEnabledForReview((Review) selectedValue));
  }

  protected boolean isEnabledForReview(@NotNull Review review)
  {
    return true;
  }
}
