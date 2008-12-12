package org.sylfra.idea.plugins.revu.ui.forms.settings.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.forms.HistoryForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.ReferentialTabbedPane;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewSettingsForm extends AbstractUpdatableForm<Review>
{
  private final List<Review> editedReviews;
  private final Project project;
  private HistoryForm historyForm;
  private JTextField tfName;
  private JPanel contentPane;
  private JCheckBox ckActive;
  private JCheckBox ckShare;
  private ReferentialTabbedPane referentialForm;
  private JCheckBox ckTemplate;
  private JLabel lbExtends;
  private JButton bnImport;
  private JLabel lbFile;
  private JTextPane tpGoal;
  private Review extendedReview;

  public ReviewSettingsForm(@NotNull final Project project, @NotNull List<Review> editedReviews)
  {
    this.project = project;
    this.editedReviews = editedReviews;

    configureUI(project);
  }

  private void configureUI(final Project project)
  {
    ckShare.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if ((ckShare.isSelected()) && (extendedReview != null) && (!extendedReview.isShared()))
        {
          int result = Messages.showOkCancelDialog(ckShare,
            RevuBundle.message("settings.project.review.shareWithPrivateLink.text", extendedReview.getName()),
            RevuBundle.message("settings.project.confirmRemoveReview.title"),
            Messages.getWarningIcon());
          if (result == DialogWrapper.OK_EXIT_CODE)
          {
            Review tmpReview = extendedReview;
            while (tmpReview != null)
            {
              tmpReview.setShared(true);
              tmpReview = tmpReview.getExtendedReview();
            }
          }
        }
      }
    });

    bnImport.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Review currentReview = new Review();
        internalUpdateData(currentReview);

        if (extendedReview == null)
        {
          CreateReviewDialog dialog = new CreateReviewDialog(project, false);
          dialog.show(editedReviews, currentReview);
          if (!dialog.isOK())
          {
            return;
          }

          switch (dialog.getImportType())
          {
            case COPY:
              currentReview.copyFrom(dialog.getImportedReview());
              break;
            case LINK:
              extendedReview = dialog.getImportedReview();
              currentReview.setExtendedReview(extendedReview);
              break;
          }

          updateUI(getEnclosingReview(), currentReview, true);
        }
        else
        {
          extendedReview = null;
          currentReview.setExtendedReview(null);
          updateUI(getEnclosingReview(), currentReview, true);
        }
      }
    });
  }

  @Override
  public void dispose()
  {
    historyForm.dispose();
    referentialForm.dispose();
  }

  public boolean isModified(@NotNull Review data)
  {
    if (!checkEquals(data.getExtendedReview(), extendedReview))
    {
      return true;
    }

    if (!checkEquals(tfName.getText(), data.getName()))
    {
      return true;
    }

    if (!checkEquals(ckTemplate.isSelected(), data.isTemplate()))
    {
      return true;
    }

    if (!checkEquals(ckActive.isSelected(), data.isActive()))
    {
      return true;
    }

    if (!checkEquals(ckShare.isSelected(), data.isShared()))
    {
      return true;
    }

    if (referentialForm.isModified(data.getDataReferential()))
    {
      return true;
    }

    return false;
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
    boolean canWrite = (getEnclosingReview() != null)
      && (!getEnclosingReview().isEmbedded())
      && (user != null)
      && (user.hasRole(User.Role.ADMIN));
    RevuUtils.setWriteAccess(canWrite, tfName, ckTemplate, ckActive, ckShare, bnImport);
  }

  protected void internalValidateInput()
  {
    updateRequiredError(tfName, "".equals(tfName.getText().trim()));
    updateError(referentialForm.getContentPane(), !referentialForm.validateInput(), null);

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    Review review = reviewManager.getReviewByName(tfName.getText());
    boolean nameAlreadyExists = ((review !=  null) && (getEnclosingReview() != null)
      && (!review.getPath().equals(getEnclosingReview().getPath())));
    updateError(tfName, nameAlreadyExists, 
      RevuBundle.message("settings.project.review.importDialog.nameAlreadyExists.text"));
  }

  protected void internalUpdateUI(Review data, boolean requestFocus)
  {
    tfName.setText((data == null) ? "" : data.getName());
    tpGoal.setText((data == null) ? "" : data.getGoal());
    lbFile.setText((data == null) ? "" : (data.getPath() == null) ? "" : data.getPath());
    ckTemplate.setSelected((data != null) && data.isTemplate());
    ckActive.setSelected((data != null) && data.isActive());
    ckShare.setSelected((data != null) && data.isShared());

    extendedReview = (data == null) ? null : data.getExtendedReview();

    boolean noExtendedReview = (data == null) || (data.getExtendedReview() == null);
    if (noExtendedReview)
    {
      lbExtends.setVisible(false);
    }
    else
    {
      lbExtends.setVisible(true);
      lbExtends.setText(RevuBundle.message("settings.project.review.referential.extends.text",
        data.getExtendedReview().getName()));
    }
    bnImport.setText(RevuBundle.message(noExtendedReview
      ? "settings.project.review.referential.import.text"
      : "settings.project.review.referential.deleteLink.text"));

    referentialForm.updateUI(getEnclosingReview(), (data == null) ? null : data.getDataReferential(), true);
  }

  protected void internalUpdateData(@NotNull Review data)
  {
    data.setName(tfName.getText());
    data.setGoal(tpGoal.getText());
    data.setPath(lbFile.getText());
    data.setTemplate(ckTemplate.isSelected());
    data.setActive(ckActive.isSelected());
    data.setShared(ckShare.isSelected());

    data.setExtendedReview(extendedReview);

    referentialForm.updateData(data.getDataReferential());
    historyForm.updateData(data);

    data.getHistory().setLastUpdatedBy(data.getDataReferential().getUser(RevuUtils.getCurrentUserLogin(), true));
    data.getHistory().setLastUpdatedOn(new Date());
  }

  public JComponent getPreferredFocusedComponent()
  {
    return tfName;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  private void createUIComponents()
  {
    referentialForm = new ReferentialTabbedPane(project);
  }
}
