package org.sylfra.idea.plugins.revu.ui.forms.settings.project;

import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.forms.HistoryForm;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential.ReferentialTabbedPane;
import org.sylfra.idea.plugins.revu.ui.statusbar.StatusBarComponent;
import org.sylfra.idea.plugins.revu.ui.statusbar.StatusBarMessage;
import org.sylfra.idea.plugins.revu.utils.ReviewFileChooser;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewSettingsForm extends AbstractUpdatableForm<Review>
{
  private final Project project;
  private JTabbedPane tabbedPane;
  private HistoryForm historyForm;
  private JTextField tfTitle;
  private JTextField tfFile;
  private JButton bnFileChooser;
  private JPanel contentPane;
  private JCheckBox ckActive;
  private JCheckBox ckShare;
  private ReferentialTabbedPane referentialForm;
  private JCheckBox ckTemplate;
  private JLabel lbExtends;
  private JButton bnImport;
  private ReviewFileChooser fileChooser;
  private Review extendedReview;

  public ReviewSettingsForm(@NotNull final Project project)
  {
    this.project = project;
    fileChooser = new ReviewFileChooser(project);

    bnFileChooser.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VirtualFile defaultFile = (tfFile.getText().length() == 0)
          ? null : RevuUtils.findFile(tfFile.getText());

        VirtualFile vFile = fileChooser.selectFileToSave(defaultFile);

        if (vFile != null)
        {
          tfFile.setText(vFile.getPath());
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
          ImportDataReferentialDialog dialog = new ImportDataReferentialDialog(project, fileChooser);
          dialog.show();
          if (dialog.isOK())
          {
            ReviewManager reviewManager = project.getComponent(ReviewManager.class);
            StatusBarComponent statusBarComponent = project.getComponent(StatusBarComponent.class);

            String path = dialog.getSelectedFile().getPath();
            Review linkedReview = reviewManager.load(path, true);

            if (linkedReview == null)
            {
              statusBarComponent.showPopup();
              return;
            }

            if (dialog.isLink())
            {
              try
              {
                reviewManager.checkCyclicLink(currentReview, linkedReview);
              }
              catch (RevuException exception)
              {
                statusBarComponent.addMessage(new StatusBarMessage(StatusBarMessage.Type.ERROR,
                  RevuBundle.message("friendlyError.externalizing.cyclicReview.title.text"),
                  exception.getMessage()));
                statusBarComponent.showPopup();
                return;
              }

              extendedReview = linkedReview;
            }
            else
            {
              currentReview.copyFrom(linkedReview);
            }

            updateUI(currentReview);
          }
        }
        else
        {
          extendedReview = null;

          updateUI(currentReview);
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

    if (!checkEquals(tfFile.getText(), data.getPath()))
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

    // @TODO check file path
    updateRequiredError(tfFile, "".equals(tfFile.getText().trim()));

    updateError(referentialForm.getContentPane(), !referentialForm.validateInput(), null);
  }

  protected void internalUpdateUI(Review data)
  {
    tfTitle.setText((data == null) ? "" : data.getTitle());
    tfFile.setText((data == null) ? "" : (data.getPath() == null) ? "" : data.getPath());
    ckTemplate.setSelected((data != null) && data.isTemplate());
    ckActive.setSelected((data != null) && data.isActive());
    ckShare.setSelected((data != null) && data.isShared());

    lbExtends.setText(((data == null) || (data.getExtendedReview() == null))
      ? ""
      : RevuBundle.message("settings.project.review.referential.extends.text", data.getExtendedReview().getTitle()));
    bnImport.setText(RevuBundle.message(((data == null) || (data.getExtendedReview() == null))
      ? "settings.project.review.referential.import.text"
      : "settings.project.review.referential.deleteLink.text"));

    referentialForm.updateUI((data == null) ? null : data.getDataReferential());
    historyForm.updateUI(data);
  }

  protected void internalUpdateData(@NotNull Review data)
  {
    data.setTitle(tfTitle.getText());
    data.setPath(RevuUtils.buildAbsolutePath(tfFile.getText()));
    data.setTemplate(ckTemplate.isSelected());
    data.setActive(ckActive.isSelected());
    data.setShared(ckShare.isSelected());

    data.setExtendedReview(extendedReview);

    referentialForm.updateData(data.getDataReferential());
    historyForm.updateData(data);

    data.getHistory().setLastUpdatedBy(data.getDataReferential().getUser(RevuUtils.getCurrentUserLogin()));
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
    fileChooser = new ReviewFileChooser(project);
    referentialForm = new ReferentialTabbedPane(project);
    tfFile = FileChooserFactory.getInstance().createFileTextField(fileChooser.getDescriptor(), false, this).getField();
  }

}
