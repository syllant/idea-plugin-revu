package org.sylfra.idea.plugins.revu.ui.actions.review;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.review.CreateReviewDialog;
import org.sylfra.idea.plugins.revu.ui.forms.settings.RevuProjectSettingsForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
* @version $Id$
*/
@SuppressWarnings({"ComponentNotRegistered"})
public class CreateReviewAction extends AbstractReviewSettingsAction
{
  private final boolean shared;

  public CreateReviewAction(boolean shared)
  {
    super(RevuBundle.message("projectSettings.review.addReview." + (shared ? "shared" : "local") + ".title"),
      RevuBundle.message("projectSettings.review.addReview." + (shared ? "shared" : "local") + ".tip"),
      RevuIconProvider.getIcon(shared ? RevuIconProvider.IconRef.REVIEW_SHARED
        : RevuIconProvider.IconRef.REVIEW_LOCAL));

    this.shared = shared;
  }

  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);

    Collection<Review> reviews = getExistingReviews(e);

    final CreateReviewDialog dialog = new CreateReviewDialog(project, true);
    dialog.show(reviews, null);
    if (!dialog.isOK())
    {
      return;
    }

    final RevuProjectSettingsForm form = project.getComponent(RevuProjectSettingsForm.class);
    if (!form.getContentPane().isShowing())
    {
      ShowSettingsUtil.getInstance().editConfigurable(project, form, new Runnable()
      {
        public void run()
        {
          execute(dialog, form);
        }
      });
    }
    else
    {
      execute(dialog, form);
    }
  }

  protected Collection<Review> getExistingReviews(AnActionEvent e)
  {
    List<Review> reviews = e.getData(RevuDataKeys.REVIEW_LIST);
    if (reviews != null)
    {
      return reviews;
    }

    Project project = e.getData(DataKeys.PROJECT);
    return project.getComponent(ReviewManager.class).getReviews();
  }

  private void execute(CreateReviewDialog dialog, RevuProjectSettingsForm form)
  {
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

    form.addItem(review);
  }
}
