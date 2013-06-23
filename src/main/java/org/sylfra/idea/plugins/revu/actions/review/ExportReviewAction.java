package org.sylfra.idea.plugins.revu.actions.review;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileTextField;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.externalizing.IReviewExternalizer;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.statusbar.StatusBarComponent;
import org.sylfra.idea.plugins.revu.ui.statusbar.StatusBarMessage;
import org.sylfra.idea.plugins.revu.utils.ReviewFileChooser;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
* @version $Id$
*/
public class ExportReviewAction extends AbstractReviewSettingsAction
{
  private final static Logger LOGGER = Logger.getInstance(ExportReviewAction.class.getName());

  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    JList liReviews = (JList) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
    if ((project == null) || (liReviews == null))
    {
      return;
    }

    Review review = (Review) liReviews.getSelectedValue();
    if (!review.isExternalizable())
    {
      String errorTitle = RevuBundle.message("friendlyError.notExternalizableReview.title.text", review.getName());
      String errorDetails = RevuBundle.message("friendlyError.notExternalizableReview.details.details.text", review.getName());
      StatusBarComponent.showMessageInPopup(project, new StatusBarMessage(StatusBarMessage.Type.ERROR, errorTitle, errorDetails), true);
      return;
    }

    ExportDialog exportDialog = new ExportDialog(project);
    exportDialog.show(review);
    if (!exportDialog.isOK())
    {
      return;
    }

    File f = exportDialog.getFile();
    if (f != null)
    {
      try
      {
        project.getComponent(IReviewExternalizer.class).save(review, f);
      }
      catch (Exception ex)
      {
        LOGGER.warn("Failed to close exported review file", ex);
        String errorTitle = RevuBundle.message("friendlyError.exportFailed.title.text", review.getName());
        String errorDetails = RevuBundle.message("friendlyError.externalizing.save.error.details.text",
          f.getPath(), ex.getMessage());
        StatusBarComponent.showMessageInPopup(project, (new StatusBarMessage(StatusBarMessage.Type.ERROR, errorTitle,
          errorDetails)), false);
      }
    }
  }

  public static class ExportDialog extends DialogWrapper
  {
    private ReviewFileChooser fileChooser;
    private JTextField tfFile;
    private Review review;
    private File validatedFile;

    public ExportDialog(Project project)
    {
      super(project, true);

      fileChooser = new ReviewFileChooser(project);
      FileTextField fileTextField = FileChooserFactory.getInstance().createFileTextField(
        fileChooser.getDescriptor(), false, myDisposable);

      tfFile = fileTextField.getField();
      tfFile.setColumns(60);
      DocumentAdapter textFieldsListener = new DocumentAdapter()
      {
        public void textChanged(DocumentEvent event)
        {
          setOKActionEnabled(!tfFile.getText().trim().equals(review.getFile()));
        }
      };
      tfFile.getDocument().addDocumentListener(textFieldsListener);

      setOKActionEnabled(false);
      setTitle(RevuBundle.message("projectSettings.review.export.title"));
      init();
    }

    public void show(Review review)
    {
      this.review = review;
      tfFile.setText(RevuVfsUtils.buildPresentablePath(review.getFile()));

      pack();

      super.show();
    }

    File getFile()
    {
      return validatedFile;
    }

    @Override
    protected void doOKAction()
    {
      validatedFile = new File(tfFile.getText());

      if (validatedFile.equals(review.getFile()))
      {
        validatedFile = null;
        setErrorText(RevuBundle.message("projectSettings.review.export.sameFile.text"));
        return;
      }

      File parentDir;
      if (validatedFile.isDirectory())
      {
        parentDir = validatedFile;
        validatedFile = new File(validatedFile, RevuUtils.buildFileNameFromReviewName(review.getName()));
      }
      else
      {
        parentDir = validatedFile.getParentFile();
      }

      if (!parentDir.isDirectory())
      {
        validatedFile = null;
        setErrorText(RevuBundle.message("projectSettings.review.export.dirDoesntExist.text", parentDir.getPath()));
        return;
      }

      super.doOKAction();
    }

    protected JComponent createCenterPanel()
    {
      JLabel label = new JLabel(RevuBundle.message("projectSettings.review.export.exportTo.label"));
      label.setLabelFor(tfFile);

      JButton bnBrowse = new JButton("...");
      bnBrowse.setMargin(new Insets(0, 0, 0, 0));
      bnBrowse.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          VirtualFile defaultFile = (tfFile.getText().length() == 0)
            ? null : RevuVfsUtils.findFile(tfFile.getText());

          VirtualFile vFile = fileChooser.selectFileToSave(defaultFile);

          if (vFile != null)
          {
            tfFile.setText(vFile.getPath());
          }
        }
      });

      JPanel panel = new JPanel(new GridBagLayout());

      GridBagConstraints gc = new GridBagConstraints();
      gc.insets = new Insets(2, 2, 2, 2);
      gc.gridwidth = 1;
      gc.gridheight = 1;
      gc.gridx = 0;
      gc.gridy = 0;
      gc.anchor = GridBagConstraints.WEST;
      gc.fill = GridBagConstraints.NONE;
      gc.weightx = 0;
      gc.weighty = 0;

      panel.add(label, gc);

      gc.gridy++;
      gc.weightx = 1.0;
      gc.fill = GridBagConstraints.HORIZONTAL;
      panel.add(tfFile, gc);

      gc.gridx++;
      gc.weightx = 0.0;
      gc.fill = GridBagConstraints.NONE;
      panel.add(bnBrowse, gc);

      return panel;
    }
  }

  protected boolean isEnabledForReview(@NotNull Review review)
  {
    return !review.isEmbedded();
  }
}
