package org.sylfra.idea.plugins.revu.ui.actions.review;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Review;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class PrepareCreateReviewAction extends AbstractReviewSettingsAction
{
  public void actionPerformed(AnActionEvent e)
  {
    // TODO better retrieve data context
    Component component = e.getData(DataKeys.CONTEXT_COMPONENT);
    if (!(component instanceof JList))
    {
      return;
    }

    JList liReviews = (JList) component;
    int reviewCount = liReviews.getModel().getSize();
    List<Review> editedReviews = new ArrayList<Review>(reviewCount);
    for (int i = 0; i < reviewCount; i++)
    {
      editedReviews.add((Review) liReviews.getModel().getElementAt(i));
    }

    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(new CreateReviewAction(false, editedReviews));
    actionGroup.add(new CreateReviewAction(true, editedReviews));

    ListPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
      RevuBundle.message("projectSettings.review.addReview.title"), actionGroup, e.getDataContext(),
      JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, false);

    component = (Component) e.getInputEvent().getSource();
    Point locationOnScreen = component.getLocationOnScreen();
    Point location = new Point(
      (int) (locationOnScreen.getX()),
      (int) locationOnScreen.getY() + component.getHeight());
    popup.showInScreenCoordinates(component, location);
  }
}
