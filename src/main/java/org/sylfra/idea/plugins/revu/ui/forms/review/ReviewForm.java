package org.sylfra.idea.plugins.revu.ui.forms.review;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.forms.HistoryForm;
import org.sylfra.idea.plugins.revu.ui.forms.review.referential.ReferentialTabbedPane;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewForm extends AbstractUpdatableForm<Review>
{
  private final List<Review> editedReviews;
  private final Project project;
  private HistoryForm<Review> historyForm;
  private JTextField tfName;
  private JPanel contentPane;
  private JCheckBox ckShare;
  private ReferentialTabbedPane referentialForm;
  private JLabel lbExtends;
  private JButton bnImport;
  private JTextField tfFile;
  private JTextArea taGoal;
  private ComboBox cbStatus;
  private JTabbedPane tabbedPane;
  private FileScopeForm fileScopeForm;
  private JLabel lbUserRoleMsg;
  private Review extendedReview;

  public ReviewForm(@NotNull final Project project, @NotNull List<Review> editedReviews)
  {
    this.project = project;
    this.editedReviews = editedReviews;

    configureUI(project);
  }

  private void createUIComponents()
  {
    referentialForm = new ReferentialTabbedPane(project);
    fileScopeForm = new FileScopeForm(project);
    cbStatus = new ComboBox(ReviewStatus.values(), -1);
  }

  private void configureUI(@NotNull final Project project)
  {
    RevuUtils.configureTextAreaAsStandardField(taGoal);

    tfFile.setBorder(BorderFactory.createEmptyBorder());
    
    ckShare.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if ((ckShare.isSelected()) && (extendedReview != null) && (!extendedReview.isShared()))
        {
          int result = Messages.showOkCancelDialog(ckShare,
            RevuBundle.message("projectSettings.review.shareWithPrivateLink.text", extendedReview.getName()),
            RevuBundle.message("projectSettings.confirmRemoveReview.title"),
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
              currentReview.getDataReferential().copyFrom(dialog.getImportedReview().getDataReferential());
              break;
            case LINK:
              extendedReview = dialog.getImportedReview();
              currentReview.setExtendedReview(extendedReview);
              break;
          }

          updateUI(currentReview, currentReview, true);
        }
        else
        {
          extendedReview = null;
          currentReview.setExtendedReview(null);
          updateUI(currentReview, currentReview, true);
        }
      }
    });

    cbStatus.setRenderer(new DefaultListCellRenderer()
    {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus)
      {
        value = (value == null) ? "?" : RevuUtils.buildReviewStatusLabel((ReviewStatus) value, false);
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });
  }

  @Override
  public void dispose()
  {
    historyForm.dispose();
    referentialForm.dispose();
    fileScopeForm.dispose();
  }

  @Override
  public Review getEnclosingReview(@Nullable Review data)
  {
    return data;
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

    if (!checkEquals(taGoal.getText(), data.getGoal()))
    {
      return true;
    }

    if (!checkEquals(cbStatus.getSelectedItem(), data.getStatus()))
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

    if (fileScopeForm.isModified(data.getFileScope()))
    {
      return true;
    }

    return false;
  }

  @Override
  protected void internalUpdateWriteAccess(Review data, @Nullable User user)
  {
    boolean canWrite = isHabilitedToEditReview(data, user);
    RevuUtils.setWriteAccess(canWrite, tfName, taGoal, cbStatus, ckShare, bnImport);
  }

  protected void internalValidateInput(@Nullable Review data)
  {
    updateRequiredError(tfName, (data != null) && "".equals(tfName.getText().trim()));
    updateError(referentialForm.getContentPane(),
      (data != null) && !referentialForm.validateInput(data.getDataReferential()), null);
    updateError(fileScopeForm.getContentPane(),
      (data != null) && !fileScopeForm.validateInput(data.getFileScope()), null);

    // Check if name already exists
    boolean nameAlreadyExists;
    if (data == null)
    {
      nameAlreadyExists = false;
    }
    else
    {
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      Review review = reviewManager.getReviewByName(tfName.getText());
      nameAlreadyExists = ((review != null)
        && review.isExternalizable()
        && data.isExternalizable()
        && (!FileUtil.filesEqual(review.getFile(), data.getFile())));
    }
    
    updateError(tfName, nameAlreadyExists,
      RevuBundle.message("projectSettings.review.importDialog.nameAlreadyExists.text"));

    updateTabIcons(tabbedPane);
  }

  protected void internalUpdateUI(@Nullable Review data, boolean requestFocus)
  {
    updateTabIcons(tabbedPane);

    boolean nullData = data == null;

    tfName.setText(nullData ? "" : data.getName());
    taGoal.setText(nullData ? "" : data.getGoal());
    cbStatus.setSelectedItem(nullData ? ReviewStatus.DRAFT : data.getStatus());
    ckShare.setSelected((data != null) && data.isShared());

    // Add space at the begining to avoid truncation
    tfFile.setText((nullData || (data.getFile() == null)) ? "" : (data.isEmbedded()) ? "" :
      " " + RevuVfsUtils.buildPresentablePath(data.getFile()));
    tfFile.setToolTipText(tfFile.getText());

    extendedReview = nullData ? null : data.getExtendedReview();

    boolean noExtendedReview = nullData || (data.getExtendedReview() == null);
    if (noExtendedReview)
    {
      lbExtends.setVisible(false);
    }
    else
    {
      lbExtends.setVisible(true);
      lbExtends.setText(RevuBundle.message("projectSettings.review.referential.extends.text",
        data.getExtendedReview().getName()));
    }
    bnImport.setText(RevuBundle.message(noExtendedReview
      ? "projectSettings.review.referential.import.text"
      : "projectSettings.review.referential.deleteLink.text"));

    if (nullData)
    {
      lbUserRoleMsg.setVisible(false);
    }
    else
    {
      lbUserRoleMsg.setVisible(true);

      String message;
      if (data.isEmbedded())
      {
        message = RevuBundle.message("reviewForm.cantModifyEmbeddedReview.text");
      }
      else
      {
        String currentLogin = RevuUtils.getCurrentUserLogin();
        User user = (currentLogin == null) ? null : data.getDataReferential().getUser(currentLogin, true);
        User.Role role = (user == null) ? null : user.getHigherRole();

        message = RevuBundle.message(
          role == null
            ? "reviewForm.userRole.none.text"
            : "reviewForm.userRole." + role.toString().toLowerCase() + ".text",
          currentLogin == null ? RevuBundle.message("general.noLoginValue.text") : currentLogin);
      }

      lbUserRoleMsg.setText(message);
    }

    referentialForm.updateUI(data, nullData ? null : data.getDataReferential(), true);
    fileScopeForm.updateUI(data, nullData ? null : data.getFileScope(), true);

    historyForm.updateUI(data, data, false);
  }

  protected void internalUpdateData(@NotNull Review data)
  {
    data.setName(tfName.getText());
    data.setGoal(taGoal.getText());
    data.setStatus((ReviewStatus) cbStatus.getSelectedItem());
    data.setShared(ckShare.isSelected());

    data.setExtendedReview(extendedReview);

    referentialForm.updateData(data.getDataReferential());
    fileScopeForm.updateData(data.getFileScope());

    data.getHistory().setLastUpdatedBy(RevuUtils.getCurrentUser(data));
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
}
