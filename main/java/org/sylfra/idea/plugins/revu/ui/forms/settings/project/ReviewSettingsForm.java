package org.sylfra.idea.plugins.revu.ui.forms.settings.project;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Review;
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
  private JTextField tfTitle;
  private JPanel contentPane;
  private JCheckBox ckActive;
  private JCheckBox ckShare;
  private ReferentialTabbedPane referentialForm;
  private JCheckBox ckTemplate;
  private JLabel lbExtends;
  private JButton bnImport;
  private JLabel lbFile;
  private JTabbedPane tabbedPane;
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
            RevuBundle.message("settings.project.review.shareWithPrivateLink.text", extendedReview.getTitle()),
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

          updateUI(getEnclosingReview(), currentReview);
        }
        else
        {
          extendedReview = null;
          currentReview.setExtendedReview(null);
          updateUI(getEnclosingReview(), currentReview);
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

    if (!checkEquals(tfTitle.getText(), data.getTitle()))
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

  protected void internalValidateInput()
  {
    updateRequiredError(tfTitle, "".equals(tfTitle.getText().trim()));
    updateError(referentialForm.getContentPane(), !referentialForm.validateInput(), null);
  }

  protected void internalUpdateUI(Review data)
  {
    tfTitle.setText((data == null) ? "" : data.getTitle());
    lbFile.setText((data == null) ? "" : (data.getPath() == null) ? "" : data.getPath());
    ckTemplate.setSelected((data != null) && data.isTemplate());
    ckActive.setSelected((data != null) && data.isActive());
    ckShare.setSelected((data != null) && data.isShared());

    extendedReview = (data == null) ? null : data.getExtendedReview();
    
    lbExtends.setText(((data == null) || (data.getExtendedReview() == null))
      ? ""
      : RevuBundle.message("settings.project.review.referential.extends.text", data.getExtendedReview().getTitle()));
    bnImport.setText(RevuBundle.message(((data == null) || (data.getExtendedReview() == null))
      ? "settings.project.review.referential.import.text"
      : "settings.project.review.referential.deleteLink.text"));

    referentialForm.updateUI(getEnclosingReview(), (data == null) ? null : data.getDataReferential());
    historyForm.updateUI(getEnclosingReview(), data);
  }

  protected void internalUpdateData(@NotNull Review data)
  {
    data.setTitle(tfTitle.getText());
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
    return tfTitle;
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
