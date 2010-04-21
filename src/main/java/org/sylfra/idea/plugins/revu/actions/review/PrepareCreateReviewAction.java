package org.sylfra.idea.plugins.revu.actions.review;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.sylfra.idea.plugins.revu.RevuBundle;

import java.awt.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id: InsidePrepareCreateReviewAction.java 22 2010-04-03 17:16:12Z syllant $
 */
public class PrepareCreateReviewAction extends AbstractReviewSettingsAction
{
  @Override
  public void actionPerformed(AnActionEvent e)
  {
    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(new CreateReviewAction(false));
    actionGroup.add(new CreateReviewAction(true));

    ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
      RevuBundle.message("projectSettings.review.addReview.title"), actionGroup, e.getDataContext(),
      JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false);

    Component component = (Component) e.getInputEvent().getSource();
    if (component.isShowing())
    {
      Point locationOnScreen = component.getLocationOnScreen();
      Point location = new Point(
        (int) (locationOnScreen.getX()),
        (int) locationOnScreen.getY() + component.getHeight());
      popup.showInScreenCoordinates(component, location);
    }
    else
    {
      // @TODO, using current mouse position
      popup.showInBestPositionFor(e.getDataContext());
    }
  }
}