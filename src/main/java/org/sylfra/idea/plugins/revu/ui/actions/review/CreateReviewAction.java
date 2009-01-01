package org.sylfra.idea.plugins.revu.ui.actions.review;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.CreateReviewDialog;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import javax.swing.*;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
* @version $Id$
*/
@SuppressWarnings({"ComponentNotRegistered"})
public class CreateReviewAction extends AbstractReviewSettingsAction
{
  private List<Review> editedReviews;
  private final boolean shared;

  public CreateReviewAction(boolean shared, List<Review> editedReviews)
  {
    super(RevuBundle.message(shared
      ? "projectSettings.review.addReview.shared.title" : "projectSettings.review.addReview.local.title"), null,
      RevuIconProvider.getIcon(shared ? RevuIconProvider.IconRef.REVIEW_SHARED
        : RevuIconProvider.IconRef.REVIEW_LOCAL));

    this.shared = shared;
    this.editedReviews = editedReviews;
  }

  public void actionPerformed(AnActionEvent e)
  {
    JList liReviews = (JList) e.getData(DataKeys.CONTEXT_COMPONENT);
    Project project = e.getData(DataKeys.PROJECT);

    CreateReviewDialog dialog = new CreateReviewDialog(project, true);
    dialog.show(editedReviews, null);
    if (!dialog.isOK())
    {
      return;
    }

    DefaultListModel model = (DefaultListModel) liReviews.getModel();
    Review review = new Review();
    review.setStatus(ReviewStatus.DRAFT);
    review.setPath(RevuVfsUtils.buildAbsolutePath(dialog.getReviewFile()));
    review.setName(dialog.getReviewName());
    review.setShared(shared);
    switch (dialog.getImportType())
    {
      case COPY:
        review.copyFrom(dialog.getImportedReview());
        break;
      case LINK:
        review.setExtendedReview(dialog.getImportedReview());
        break;
    }

    User currentUser = RevuUtils.getCurrentUser();
    User reviewCurrentUser = review.getDataReferential().getUser(currentUser.getLogin(), false);
    if (reviewCurrentUser == null)
    {
      currentUser.addRole(User.Role.ADMIN);
      review.getDataReferential().addUser(currentUser);
    }
    else
    {
      if (!reviewCurrentUser.hasRole(User.Role.ADMIN))
      {
        reviewCurrentUser.addRole(User.Role.ADMIN);
      }
    }

    History history = new History();
    Date now = new Date();
    history.setCreatedBy(currentUser);
    history.setCreatedOn(now);
    history.setLastUpdatedBy(currentUser);
    history.setLastUpdatedOn(now);
    review.setHistory(history);

    model.addElement(review);
    liReviews.setSelectedValue(review, true);
  }
}
