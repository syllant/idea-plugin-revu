package org.sylfra.idea.plugins.revu.ui.actions.review;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Review;

import java.awt.*;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class PrepareCreateReviewAction extends AbstractReviewSettingsAction
{
  private List<Review> editedReviews;

  public void setEditedReviews(List<Review> editedReviews)
  {
    this.editedReviews = editedReviews;
  }

  public void actionPerformed(AnActionEvent e)
  {
    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(new CreateReviewAction(false, editedReviews));
    actionGroup.add(new CreateReviewAction(true, editedReviews));

    ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
      RevuBundle.message("projectSettings.review.addReview.title"), actionGroup, e.getDataContext(),
      JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false);

    Component component = (Component) e.getInputEvent().getSource();
    Point locationOnScreen = component.getLocationOnScreen();
    Point location = new Point(
      (int) (locationOnScreen.getX()),
      (int) locationOnScreen.getY() + component.getHeight());
    popup.showInScreenCoordinates(component, location);
  }
}
